/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.upgrades;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.Version;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.Booleans;
import org.elasticsearch.common.CheckedFunction;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.test.NotEqualMessageBuilder;
import org.elasticsearch.test.rest.ESRestTestCase;
import org.elasticsearch.test.rest.yaml.ObjectPath;
import org.junit.Before;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.elasticsearch.cluster.routing.UnassignedInfo.INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING;
import static org.elasticsearch.cluster.routing.allocation.decider.MaxRetryAllocationDecider.SETTING_ALLOCATION_MAX_RETRY;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests to run before and after a full cluster restart. This is run twice,
 * one with {@code tests.is_old_cluster} set to {@code true} against a cluster
 * of an older version. The cluster is shutdown and a cluster of the new
 * version is started with the same data directories and then this is rerun
 * with {@code tests.is_old_cluster} set to {@code false}.
 */
public class FullClusterRestartIT extends ESRestTestCase {
    private final boolean runningAgainstOldCluster = Booleans.parseBoolean(System.getProperty("tests.is_old_cluster"));
    private final Version oldClusterVersion = Version.fromString(System.getProperty("tests.old_cluster_version"));
    private final boolean supportsLenientBooleans = oldClusterVersion.before(Version.V_6_0_0_alpha1);
    private static final Version VERSION_5_1_0_UNRELEASED = Version.fromString("5.1.0");

    private String index;

    @Before
    public void setIndex() {
        index = getTestName().toLowerCase(Locale.ROOT);
    }

    @Override
    protected boolean preserveIndicesUponCompletion() {
        return true;
    }

    @Override
    protected boolean preserveSnapshotsUponCompletion() {
        return true;
    }

    @Override
    protected boolean preserveReposUponCompletion() {
        return true;
    }

    @Override
    protected boolean preserveTemplatesUponCompletion() {
        return true;
    }

    public void testSearch() throws Exception {
        int count;
        if (runningAgainstOldCluster) {
            XContentBuilder mappingsAndSettings = jsonBuilder();
            mappingsAndSettings.startObject();
            {
                mappingsAndSettings.startObject("settings");
                mappingsAndSettings.field("number_of_shards", 1);
                mappingsAndSettings.field("number_of_replicas", 0);
                mappingsAndSettings.endObject();
            }
            {
                mappingsAndSettings.startObject("mappings");
                mappingsAndSettings.startObject("doc");
                mappingsAndSettings.startObject("properties");
                {
                    mappingsAndSettings.startObject("string");
                    mappingsAndSettings.field("type", "text");
                    mappingsAndSettings.endObject();
                }
                {
                    mappingsAndSettings.startObject("dots_in_field_names");
                    mappingsAndSettings.field("type", "text");
                    mappingsAndSettings.endObject();
                }
                {
                    mappingsAndSettings.startObject("binary");
                    mappingsAndSettings.field("type", "binary");
                    mappingsAndSettings.field("store", "true");
                    mappingsAndSettings.endObject();
                }
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
            }
            mappingsAndSettings.endObject();
            Request createIndex = new Request("PUT", "/" + index);
            createIndex.setJsonEntity(Strings.toString(mappingsAndSettings));
            client().performRequest(createIndex);

            count = randomIntBetween(2000, 3000);
            byte[] randomByteArray = new byte[16];
            random().nextBytes(randomByteArray);
            indexRandomDocuments(count, true, true, i -> {
                return JsonXContent.contentBuilder().startObject()
                .field("string", randomAlphaOfLength(10))
                .field("int", randomInt(100))
                .field("float", randomFloat())
                // be sure to create a "proper" boolean (True, False) for the first document so that automapping is correct
                .field("bool", i > 0 && supportsLenientBooleans ? randomLenientBoolean() : randomBoolean())
                .field("field.with.dots", randomAlphaOfLength(10))
                .field("binary", Base64.getEncoder().encodeToString(randomByteArray))
                .endObject();
            });
            refresh();
        } else {
            count = countOfIndexedRandomDocuments();
        }

        ensureGreenLongWait(index);
        assertBasicSearchWorks(count);
        assertAllSearchWorks(count);
        assertBasicAggregationWorks();
        assertRealtimeGetWorks();
        assertStoredBinaryFields(count);
    }

    public void testNewReplicasWork() throws Exception {
        if (runningAgainstOldCluster) {
            XContentBuilder mappingsAndSettings = jsonBuilder();
            mappingsAndSettings.startObject();
            {
                mappingsAndSettings.startObject("settings");
                mappingsAndSettings.field("number_of_shards", 1);
                mappingsAndSettings.field("number_of_replicas", 0);
                mappingsAndSettings.endObject();
            }
            {
                mappingsAndSettings.startObject("mappings");
                mappingsAndSettings.startObject("doc");
                mappingsAndSettings.startObject("properties");
                {
                    mappingsAndSettings.startObject("field");
                    mappingsAndSettings.field("type", "text");
                    mappingsAndSettings.endObject();
                }
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
            }
            mappingsAndSettings.endObject();
            Request createIndex = new Request("PUT", "/" + index);
            createIndex.setJsonEntity(Strings.toString(mappingsAndSettings));
            client().performRequest(createIndex);

            int numDocs = randomIntBetween(2000, 3000);
            indexRandomDocuments(numDocs, true, false, i -> {
                return JsonXContent.contentBuilder().startObject()
                    .field("field", "value")
                    .endObject();
            });
            logger.info("Refreshing [{}]", index);
            client().performRequest(new Request("POST", "/" + index + "/_refresh"));
        } else {
            final int numReplicas = 1;
            final long startTime = System.currentTimeMillis();
            logger.debug("--> creating [{}] replicas for index [{}]", numReplicas, index);
            Request setNumberOfReplicas = new Request("PUT", "/" + index + "/_settings");
            setNumberOfReplicas.setJsonEntity("{ \"index\": { \"number_of_replicas\" : " + numReplicas + " }}");
            Response response = client().performRequest(setNumberOfReplicas);

            ensureGreenLongWait(index);

            logger.debug("--> index [{}] is green, took [{}] ms", index, (System.currentTimeMillis() - startTime));
            Map<String, Object> recoverRsp = entityAsMap(client().performRequest(new Request("GET", "/" + index + "/_recovery")));
            logger.debug("--> recovery status:\n{}", recoverRsp);

            Request primarySearchRequest = new Request("GET", "/" + index + "/_search");
            primarySearchRequest.addParameter("preference", "_primary");
            Map<String, Object> primarySearchResponse = entityAsMap(client().performRequest(primarySearchRequest));
            assertNoFailures(primarySearchResponse);
            int foundHits1 = (int) XContentMapValues.extractValue("hits.total", primarySearchResponse);

            Request replicaSearchRequest = new Request("GET", "/" + index + "/_search");
            replicaSearchRequest.addParameter("preference", "_replica");
            Map<String, Object> replicaSearchResponse = entityAsMap(client().performRequest(replicaSearchRequest));
            assertNoFailures(replicaSearchResponse);
            int foundHits2 = (int) XContentMapValues.extractValue("hits.total", replicaSearchResponse);
            assertEquals(foundHits1, foundHits2);
            // TODO: do something more with the replicas! index?
        }
    }

    /**
     * Search on an alias that contains illegal characters that would prevent it from being created after 5.1.0. It should still be
     * search-able though.
     */
    public void testAliasWithBadName() throws Exception {
        assumeTrue("Can only test bad alias name if old cluster is on 5.1.0 or before",
            oldClusterVersion.before(VERSION_5_1_0_UNRELEASED));

        int count;
        if (runningAgainstOldCluster) {
            XContentBuilder mappingsAndSettings = jsonBuilder();
            mappingsAndSettings.startObject();
            {
                mappingsAndSettings.startObject("settings");
                mappingsAndSettings.field("number_of_shards", 1);
                mappingsAndSettings.field("number_of_replicas", 0);
                mappingsAndSettings.endObject();
            }
            {
                mappingsAndSettings.startObject("mappings");
                mappingsAndSettings.startObject("doc");
                mappingsAndSettings.startObject("properties");
                {
                    mappingsAndSettings.startObject("key");
                    mappingsAndSettings.field("type", "keyword");
                    mappingsAndSettings.endObject();
                }
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
            }
            mappingsAndSettings.endObject();
            Request createIndex = new Request("PUT", "/" + index);
            createIndex.setJsonEntity(Strings.toString(mappingsAndSettings));
            client().performRequest(createIndex);

            String aliasName = "%23" + index; // %23 == #
            client().performRequest(new Request("PUT", "/" + index + "/_alias/" + aliasName));
            Response response = client().performRequest(new Request("HEAD", "/" + index + "/_alias/" + aliasName));
            assertEquals(200, response.getStatusLine().getStatusCode());

            count = randomIntBetween(32, 128);
            indexRandomDocuments(count, true, true, i -> {
                return JsonXContent.contentBuilder().startObject()
                    .field("key", "value")
                    .endObject();
            });
            refresh();
        } else {
            count = countOfIndexedRandomDocuments();
        }

        Request request = new Request("GET", "/_cluster/state");
        request.addParameter("metric", "metadata");
        logger.error("clusterState=" + entityAsMap(client().performRequest(request)));
        // We can read from the alias just like we can read from the index.
        String aliasName = "%23" + index; // %23 == #
        Map<String, Object> searchRsp = entityAsMap(client().performRequest(new Request("GET", "/" + aliasName + "/_search")));
        int totalHits = (int) XContentMapValues.extractValue("hits.total", searchRsp);
        assertEquals(count, totalHits);
        if (runningAgainstOldCluster == false) {
            // We can remove the alias.
            Response response = client().performRequest(new Request("DELETE", "/" + index + "/_alias/" + aliasName));
            assertEquals(200, response.getStatusLine().getStatusCode());
            // and check that it is gone:
            response = client().performRequest(new Request("HEAD", "/" + index + "/_alias/" + aliasName));
            assertEquals(404, response.getStatusLine().getStatusCode());
        }
    }

    public void testClusterState() throws Exception {
        if (runningAgainstOldCluster) {
            XContentBuilder mappingsAndSettings = jsonBuilder();
            mappingsAndSettings.startObject();
            mappingsAndSettings.field("template", index);
            {
                mappingsAndSettings.startObject("settings");
                mappingsAndSettings.field("number_of_shards", 1);
                mappingsAndSettings.field("number_of_replicas", 0);
                mappingsAndSettings.endObject();
            }
            mappingsAndSettings.endObject();
            Request createTemplate = new Request("PUT", "/_template/template_1");
            createTemplate.setJsonEntity(Strings.toString(mappingsAndSettings));
            client().performRequest(createTemplate);
            client().performRequest(new Request("PUT", "/" + index));
        }

        // verifying if we can still read some properties from cluster state api:
        Map<String, Object> clusterState = entityAsMap(client().performRequest(new Request("GET", "/_cluster/state")));

        // Check some global properties:
        String clusterName = (String) clusterState.get("cluster_name");
        assertEquals("full-cluster-restart", clusterName);
        String numberOfShards = (String) XContentMapValues.extractValue(
            "metadata.templates.template_1.settings.index.number_of_shards", clusterState);
        assertEquals("1", numberOfShards);
        String numberOfReplicas = (String) XContentMapValues.extractValue(
            "metadata.templates.template_1.settings.index.number_of_replicas", clusterState);
        assertEquals("0", numberOfReplicas);

        // Check some index properties:
        numberOfShards = (String) XContentMapValues.extractValue("metadata.indices." + index +
            ".settings.index.number_of_shards", clusterState);
        assertEquals("1", numberOfShards);
        numberOfReplicas = (String) XContentMapValues.extractValue("metadata.indices." + index +
                ".settings.index.number_of_replicas", clusterState);
        assertEquals("0", numberOfReplicas);
        Version version = Version.fromId(Integer.valueOf((String) XContentMapValues.extractValue("metadata.indices." + index +
            ".settings.index.version.created", clusterState)));
        assertEquals(oldClusterVersion, version);

    }

    public void testShrink() throws IOException {
        String shrunkenIndex = index + "_shrunk";
        int numDocs;
        if (runningAgainstOldCluster) {
            XContentBuilder mappingsAndSettings = jsonBuilder();
            mappingsAndSettings.startObject();
            {
                mappingsAndSettings.startObject("mappings");
                mappingsAndSettings.startObject("doc");
                mappingsAndSettings.startObject("properties");
                {
                    mappingsAndSettings.startObject("field");
                    mappingsAndSettings.field("type", "text");
                    mappingsAndSettings.endObject();
                }
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
            }
            mappingsAndSettings.endObject();
            Request createIndex = new Request("PUT", "/" + index);
            createIndex.setJsonEntity(Strings.toString(mappingsAndSettings));
            client().performRequest(createIndex);

            numDocs = randomIntBetween(512, 1024);
            indexRandomDocuments(numDocs, true, true, i -> {
                return JsonXContent.contentBuilder().startObject()
                    .field("field", "value")
                    .endObject();
            });

            ensureGreen(index); // wait for source index to be available on both nodes before starting shrink

            Request updateSettingsRequest = new Request("PUT", "/" + index + "/_settings");
            updateSettingsRequest.setJsonEntity("{\"settings\": {\"index.blocks.write\": true}}");
            client().performRequest(updateSettingsRequest);

            Request shrinkIndexRequest = new Request("PUT", "/" + index + "/_shrink/" + shrunkenIndex);
            shrinkIndexRequest.setJsonEntity("{\"settings\": {\"index.number_of_shards\": 1}}");
            client().performRequest(shrinkIndexRequest);

            client().performRequest(new Request("POST", "/_refresh"));
        } else {
            numDocs = countOfIndexedRandomDocuments();
        }

        Map<?, ?> response = entityAsMap(client().performRequest(new Request("GET", "/" + index + "/_search")));
        assertNoFailures(response);
        int totalShards = (int) XContentMapValues.extractValue("_shards.total", response);
        assertThat(totalShards, greaterThan(1));
        int successfulShards = (int) XContentMapValues.extractValue("_shards.successful", response);
        assertEquals(totalShards, successfulShards);
        int totalHits = (int) XContentMapValues.extractValue("hits.total", response);
        assertEquals(numDocs, totalHits);

        response = entityAsMap(client().performRequest(new Request("GET", "/" + shrunkenIndex+ "/_search")));
        assertNoFailures(response);
        totalShards = (int) XContentMapValues.extractValue("_shards.total", response);
        assertEquals(1, totalShards);
        successfulShards = (int) XContentMapValues.extractValue("_shards.successful", response);
        assertEquals(1, successfulShards);
        totalHits = (int) XContentMapValues.extractValue("hits.total", response);
        assertEquals(numDocs, totalHits);
    }

    public void testShrinkAfterUpgrade() throws IOException {
        String shrunkenIndex = index + "_shrunk";
        int numDocs;
        if (runningAgainstOldCluster) {
            XContentBuilder mappingsAndSettings = jsonBuilder();
            mappingsAndSettings.startObject();
            // single type was added in 5.5.0 (see #24317)
            if (oldClusterVersion.onOrAfter(Version.V_5_5_0) &&
                oldClusterVersion.before(Version.V_6_0_0_beta1) &&
                randomBoolean()) {
                {
                    // test that mapping.single_type is correctly propagated on the shrinked index,
                    // if not, search will fail.
                    mappingsAndSettings.startObject("settings");
                    mappingsAndSettings.startObject("mapping");
                    mappingsAndSettings.field("single_type", true);
                    mappingsAndSettings.endObject();
                    mappingsAndSettings.endObject();
                }
            }
            {
                mappingsAndSettings.startObject("mappings");
                mappingsAndSettings.startObject("doc");
                mappingsAndSettings.startObject("properties");
                {
                    mappingsAndSettings.startObject("field");
                    mappingsAndSettings.field("type", "text");
                    mappingsAndSettings.endObject();
                }
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
                mappingsAndSettings.endObject();
            }
            mappingsAndSettings.endObject();
            Request createIndex = new Request("PUT", "/" + index);
            createIndex.setJsonEntity(Strings.toString(mappingsAndSettings));
            client().performRequest(createIndex);

            numDocs = randomIntBetween(512, 1024);
            indexRandomDocuments(numDocs, true, true, i -> {
                return JsonXContent.contentBuilder().startObject()
                    .field("field", "value")
                    .endObject();
            });
        } else {
            ensureGreen(index); // wait for source index to be available on both nodes before starting shrink

            Request updateSettingsRequest = new Request("PUT", "/" + index + "/_settings");
            updateSettingsRequest.setJsonEntity("{\"settings\": {\"index.blocks.write\": true}}");
            client().performRequest(updateSettingsRequest);

            Request shrinkIndexRequest = new Request("PUT", "/" + index + "/_shrink/" + shrunkenIndex);
            shrinkIndexRequest.setJsonEntity("{\"settings\": {\"index.number_of_shards\": 1}}");
            client().performRequest(shrinkIndexRequest);

            numDocs = countOfIndexedRandomDocuments();
        }

        client().performRequest(new Request("POST", "/_refresh"));

        Map<?, ?> response = entityAsMap(client().performRequest(new Request("GET", "/" + index + "/_search")));
        assertNoFailures(response);
        int totalShards = (int) XContentMapValues.extractValue("_shards.total", response);
        assertThat(totalShards, greaterThan(1));
        int successfulShards = (int) XContentMapValues.extractValue("_shards.successful", response);
        assertEquals(totalShards, successfulShards);
        int totalHits = (int) XContentMapValues.extractValue("hits.total", response);
        assertEquals(numDocs, totalHits);

        if (runningAgainstOldCluster == false) {
            response = entityAsMap(client().performRequest(new Request("GET", "/" + shrunkenIndex + "/_search")));
            assertNoFailures(response);
            totalShards = (int) XContentMapValues.extractValue("_shards.total", response);
            assertEquals(1, totalShards);
            successfulShards = (int) XContentMapValues.extractValue("_shards.successful", response);
            assertEquals(1, successfulShards);
            totalHits = (int) XContentMapValues.extractValue("hits.total", response);
            assertEquals(numDocs, totalHits);
        }
    }

    void assertBasicSearchWorks(int count) throws IOException {
        logger.info("--> testing basic search");
        {
            Map<String, Object> response = entityAsMap(client().performRequest(new Request("GET", "/" + index + "/_search")));
            assertNoFailures(response);
            int numDocs = (int) XContentMapValues.extractValue("hits.total", response);
            logger.info("Found {} in old index", numDocs);
            assertEquals(count, numDocs);
        }

        logger.info("--> testing basic search with sort");
        {
            Request searchRequest = new Request("GET", "/" + index + "/_search");
            searchRequest.setJsonEntity("{ \"sort\": [{ \"int\" : \"asc\" }]}");
            Map<String, Object> response = entityAsMap(client().performRequest(searchRequest));
            assertNoFailures(response);
            assertTotalHits(count, response);
        }

        logger.info("--> testing exists filter");
        {
            Request searchRequest = new Request("GET", "/" + index + "/_search");
            searchRequest.setJsonEntity("{ \"query\": { \"exists\" : {\"field\": \"string\"} }}");
            Map<String, Object> response = entityAsMap(client().performRequest(searchRequest));
            assertNoFailures(response);
            assertTotalHits(count, response);
        }

        logger.info("--> testing field with dots in the name");
        {
            Request searchRequest = new Request("GET", "/" + index + "/_search");
            searchRequest.setJsonEntity("{ \"query\": { \"exists\" : {\"field\": \"field.with.dots\"} }}");
            Map<String, Object> response = entityAsMap(client().performRequest(searchRequest));
            assertNoFailures(response);
            assertTotalHits(count, response);
        }
    }

    void assertAllSearchWorks(int count) throws IOException {
        logger.info("--> testing _all search");
        Map<String, Object> response = entityAsMap(client().performRequest(new Request("GET", "/" + index + "/_search")));
        assertNoFailures(response);
        assertTotalHits(count, response);
        Map<?, ?> bestHit = (Map<?, ?>) ((List<?>) (XContentMapValues.extractValue("hits.hits", response))).get(0);

        // Make sure there are payloads and they are taken into account for the score
        // the 'string' field has a boost of 4 in the mappings so it should get a payload boost
        String stringValue = (String) XContentMapValues.extractValue("_source.string", bestHit);
        assertNotNull(stringValue);
        String type = (String) bestHit.get("_type");
        String id = (String) bestHit.get("_id");
        Request explanationRequest = new Request("GET", "/" + index + "/" + type + "/" + id + "/_explain");
        explanationRequest.setJsonEntity("{ \"query\": { \"match_all\" : {} }}");
        String explanation = toStr(client().performRequest(explanationRequest));
        assertFalse("Could not find payload boost in explanation\n" + explanation, explanation.contains("payloadBoost"));

        // Make sure the query can run on the whole index
        Request searchRequest = new Request("GET", "/" + index + "/_search");
        searchRequest.setEntity(explanationRequest.getEntity());
        searchRequest.addParameter("explain", "true");
        Map<?, ?> matchAllResponse = entityAsMap(client().performRequest(searchRequest));
        assertNoFailures(matchAllResponse);
        assertTotalHits(count, matchAllResponse);
    }

    void assertBasicAggregationWorks() throws IOException {
        // histogram on a long
        Request longHistogramRequest = new Request("GET", "/" + index + "/_search");
        longHistogramRequest.setJsonEntity("{ \"aggs\": { \"histo\" : {\"histogram\" : {\"field\": \"int\", \"interval\": 10}} }}");
        Map<?, ?> longHistogram = entityAsMap(client().performRequest(longHistogramRequest));
        assertNoFailures(longHistogram);
        List<?> histoBuckets = (List<?>) XContentMapValues.extractValue("aggregations.histo.buckets", longHistogram);
        int histoCount = 0;
        for (Object entry : histoBuckets) {
            Map<?, ?> bucket = (Map<?, ?>) entry;
            histoCount += (Integer) bucket.get("doc_count");
        }
        assertTotalHits(histoCount, longHistogram);

        // terms on a boolean
        Request boolTermsRequest = new Request("GET", "/" + index + "/_search");
        boolTermsRequest.setJsonEntity("{ \"aggs\": { \"bool_terms\" : {\"terms\" : {\"field\": \"bool\"}} }}");
        Map<?, ?> boolTerms = entityAsMap(client().performRequest(boolTermsRequest));
        List<?> termsBuckets = (List<?>) XContentMapValues.extractValue("aggregations.bool_terms.buckets", boolTerms);
        int termsCount = 0;
        for (Object entry : termsBuckets) {
            Map<?, ?> bucket = (Map<?, ?>) entry;
            termsCount += (Integer) bucket.get("doc_count");
        }
        assertTotalHits(termsCount, boolTerms);
    }

    void assertRealtimeGetWorks() throws IOException {
        Request disableAutoRefresh = new Request("PUT", "/" + index + "/_settings");
        disableAutoRefresh.setJsonEntity("{ \"index\": { \"refresh_interval\" : -1 }}");
        client().performRequest(disableAutoRefresh);

        Request searchRequest = new Request("GET", "/" + index + "/_search");
        searchRequest.setJsonEntity("{ \"query\": { \"match_all\" : {} }}");
        Map<?, ?> searchResponse = entityAsMap(client().performRequest(searchRequest));
        Map<?, ?> hit = (Map<?, ?>) ((List<?>)(XContentMapValues.extractValue("hits.hits", searchResponse))).get(0);
        String docId = (String) hit.get("_id");

        Request updateRequest = new Request("POST", "/" + index + "/doc/" + docId + "/_update");
        updateRequest.setJsonEntity("{ \"doc\" : { \"foo\": \"bar\"}}");
        client().performRequest(updateRequest);

        Map<String, Object> getRsp = entityAsMap(client().performRequest(new Request("GET", "/" + index + "/doc/" + docId)));
        Map<?, ?> source = (Map<?, ?>) getRsp.get("_source");
        assertTrue("doc does not contain 'foo' key: " + source, source.containsKey("foo"));

        Request enableAutoRefresh = new Request("PUT", "/" + index + "/_settings");
        enableAutoRefresh.setJsonEntity("{ \"index\": { \"refresh_interval\" : \"1s\" }}");
        client().performRequest(enableAutoRefresh);
    }

    void assertStoredBinaryFields(int count) throws Exception {
        Request request = new Request("GET", "/" + index + "/_search");
        request.setJsonEntity("{ \"query\": { \"match_all\" : {} }, \"size\": 100, \"stored_fields\": \"binary\"}");
        Map<String, Object> rsp = entityAsMap(client().performRequest(request));

        assertTotalHits(count, rsp);
        List<?> hits = (List<?>) XContentMapValues.extractValue("hits.hits", rsp);
        assertEquals(100, hits.size());
        for (Object hit : hits) {
            Map<?, ?> hitRsp = (Map<?, ?>) hit;
            List<?> values = (List<?>) XContentMapValues.extractValue("fields.binary", hitRsp);
            assertEquals(1, values.size());
            String value = (String) values.get(0);
            byte[] binaryValue = Base64.getDecoder().decode(value);
            assertEquals("Unexpected string length [" + value + "]", 16, binaryValue.length);
        }
    }

    static String toStr(Response response) throws IOException {
        return EntityUtils.toString(response.getEntity());
    }

    static void assertNoFailures(Map<?, ?> response) {
        int failed = (int) XContentMapValues.extractValue("_shards.failed", response);
        assertEquals(0, failed);
    }

    static void assertTotalHits(int expectedTotalHits, Map<?, ?> response) {
        int actualTotalHits = (Integer) XContentMapValues.extractValue("hits.total", response);
        assertEquals(expectedTotalHits, actualTotalHits);
    }

    /**
     * Tests that a single document survives. Super basic smoke test.
     */
    public void testSingleDoc() throws IOException {
        String docLocation = "/" + index + "/doc/1";
        String doc = "{\"test\": \"test\"}";

        if (runningAgainstOldCluster) {
            Request createDoc = new Request("PUT", docLocation);
            createDoc.setJsonEntity(doc);
            client().performRequest(createDoc);
        }

        assertThat(toStr(client().performRequest(new Request("GET", docLocation))), containsString(doc));
    }

    /**
     * Tests that a single empty shard index is correctly recovered. Empty shards are often an edge case.
     */
    public void testEmptyShard() throws IOException {
        final String index = "test_empty_shard";

        if (runningAgainstOldCluster) {
            Settings.Builder settings = Settings.builder()
                .put(IndexMetaData.INDEX_NUMBER_OF_SHARDS_SETTING.getKey(), 1)
                .put(IndexMetaData.INDEX_NUMBER_OF_REPLICAS_SETTING.getKey(), 1)
                // if the node with the replica is the first to be restarted, while a replica is still recovering
                // then delayed allocation will kick in. When the node comes back, the master will search for a copy
                // but the recovering copy will be seen as invalid and the cluster health won't return to GREEN
                // before timing out
                .put(INDEX_DELAYED_NODE_LEFT_TIMEOUT_SETTING.getKey(), "100ms")
                .put(SETTING_ALLOCATION_MAX_RETRY.getKey(), "0"); // fail faster
            createIndex(index, settings.build());
        }
        ensureGreen(index);
    }


    /**
     * Tests recovery of an index with or without a translog and the
     * statistics we gather about that.
     */
    public void testRecovery() throws Exception {
        int count;
        boolean shouldHaveTranslog;
        if (runningAgainstOldCluster) {
            count = between(200, 300);
            /* We've had bugs in the past where we couldn't restore
             * an index without a translog so we randomize whether
             * or not we have one. */
            shouldHaveTranslog = randomBoolean();

            indexRandomDocuments(count, true, true, i -> jsonBuilder().startObject().field("field", "value").endObject());

            // make sure all recoveries are done
            ensureGreen(index);
            // Recovering a synced-flush index from 5.x to 6.x might be subtle as a 5.x index commit does not have all 6.x commit tags.
            if (randomBoolean()) {
                // We have to spin synced-flush requests here because we fire the global checkpoint sync for the last write operation.
                // A synced-flush request considers the global checkpoint sync as an going operation because it acquires a shard permit.
                assertBusy(() -> {
                    try {
                        Response resp = client().performRequest(new Request("POST", index + "/_flush/synced"));
                        Map<String, Object> result = ObjectPath.createFromResponse(resp).evaluate("_shards");
                        assertThat(result.get("successful"), equalTo(result.get("total")));
                        assertThat(result.get("failed"), equalTo(0));
                    } catch (ResponseException ex) {
                        throw new AssertionError(ex); // cause assert busy to retry
                    }
                });
            } else {
                // Explicitly flush so we're sure to have a bunch of documents in the Lucene index
                assertOK(client().performRequest(new Request("POST", "/_flush")));
            }
            if (shouldHaveTranslog) {
                // Update a few documents so we are sure to have a translog
                indexRandomDocuments(count / 10, false /* Flushing here would invalidate the whole thing....*/, false,
                    i -> jsonBuilder().startObject().field("field", "value").endObject());
            }
            saveInfoDocument("should_have_translog", Boolean.toString(shouldHaveTranslog));
        } else {
            count = countOfIndexedRandomDocuments();
            shouldHaveTranslog = Booleans.parseBoolean(loadInfoDocument("should_have_translog"));
        }

        // Count the documents in the index to make sure we have as many as we put there
        Request countRequest = new Request("GET", "/" + index + "/_search");
        countRequest.addParameter("size", "0");
        String countResponse = toStr(client().performRequest(countRequest));
        assertThat(countResponse, containsString("\"total\":" + count));

        if (false == runningAgainstOldCluster) {
            boolean restoredFromTranslog = false;
            boolean foundPrimary = false;
            Request recoveryRequest = new Request("GET", "/_cat/recovery/" + index);
            recoveryRequest.addParameter("h", "index,shard,type,stage,translog_ops_recovered");
            recoveryRequest.addParameter("s", "index,shard,type");
            String recoveryResponse = toStr(client().performRequest(recoveryRequest));
            for (String line : recoveryResponse.split("\n")) {
                // Find the primaries
                foundPrimary = true;
                if (false == line.contains("done") && line.contains("existing_store")) {
                    continue;
                }
                /* Mark if we see a primary that looked like it restored from the translog.
                 * Not all primaries will look like this all the time because we modify
                 * random documents when we want there to be a translog and they might
                 * not be spread around all the shards. */
                Matcher m = Pattern.compile("(\\d+)$").matcher(line);
                assertTrue(line, m.find());
                int translogOps = Integer.parseInt(m.group(1));
                if (translogOps > 0) {
                    restoredFromTranslog = true;
                }
            }
            assertTrue("expected to find a primary but didn't\n" + recoveryResponse, foundPrimary);
            assertEquals("mismatch while checking for translog recovery\n" + recoveryResponse, shouldHaveTranslog, restoredFromTranslog);

        String currentLuceneVersion = Version.CURRENT.luceneVersion.toString();
        String bwcLuceneVersion = oldClusterVersion.luceneVersion.toString();
        if (shouldHaveTranslog && false == currentLuceneVersion.equals(bwcLuceneVersion)) {
            int numCurrentVersion = 0;
            int numBwcVersion = 0;
            Request segmentsRequest = new Request("GET", "/_cat/segments/" + index);
            segmentsRequest.addParameter("h", "prirep,shard,index,version");
            segmentsRequest.addParameter("s", "prirep,shard,index");
            String segmentsResponse = toStr(client().performRequest(segmentsRequest));
            for (String line : segmentsResponse.split("\n")) {
                if (false == line.startsWith("p")) {
                    continue;
                }
                Matcher m = Pattern.compile("(\\d+\\.\\d+\\.\\d+)$").matcher(line);
                assertTrue(line, m.find());
                String version = m.group(1);
                if (currentLuceneVersion.equals(version)) {
                    numCurrentVersion++;
                } else if (bwcLuceneVersion.equals(version)) {
                    numBwcVersion++;
                } else {
                    fail("expected version to be one of [" + currentLuceneVersion + "," + bwcLuceneVersion + "] but was " + line);
                }
            }
            assertNotEquals("expected at least 1 current segment after translog recovery. segments:\n" + segmentsResponse,
                0, numCurrentVersion);
            assertNotEquals("expected at least 1 old segment. segments:\n" + segmentsResponse, 0, numBwcVersion);}
        }
    }

    /**
     * Tests snapshot/restore by creating a snapshot and restoring it. It takes
     * a snapshot on the old cluster and restores it on the old cluster as a
     * sanity check and on the new cluster as an upgrade test. It also takes a
     * snapshot on the new cluster and restores that on the new cluster as a
     * test that the repository is ok with containing snapshot from both the
     * old and new versions. All of the snapshots include an index, a template,
     * and some routing configuration.
     */
    public void testSnapshotRestore() throws IOException {
        int count;
        if (runningAgainstOldCluster) {
            // Create the index
            count = between(200, 300);
            indexRandomDocuments(count, true, true, i -> jsonBuilder().startObject().field("field", "value").endObject());
        } else {
            count = countOfIndexedRandomDocuments();
        }

        // Refresh the index so the count doesn't fail
        refresh();

        // Count the documents in the index to make sure we have as many as we put there
        Request countRequest = new Request("GET", "/" + index + "/_search");
        countRequest.addParameter("size", "0");
        String countResponse = toStr(client().performRequest(countRequest));
        assertThat(countResponse, containsString("\"total\":" + count));

        // Stick a routing attribute into to cluster settings so we can see it after the restore
        Request addRoutingSettings = new Request("PUT", "/_cluster/settings");
        addRoutingSettings.setJsonEntity(
                    "{\"persistent\": {\"cluster.routing.allocation.exclude.test_attr\": \"" + oldClusterVersion + "\"}}");
        client().performRequest(addRoutingSettings);

        // Stick a template into the cluster so we can see it after the restore
        XContentBuilder templateBuilder = JsonXContent.contentBuilder().startObject();
        templateBuilder.field("template", "evil_*"); // Don't confuse other tests by applying the template
        templateBuilder.startObject("settings"); {
            templateBuilder.field("number_of_shards", 1);
        }
        templateBuilder.endObject();
        templateBuilder.startObject("mappings"); {
            templateBuilder.startObject("doc"); {
                templateBuilder.startObject("_source"); {
                    templateBuilder.field("enabled", true);
                }
                templateBuilder.endObject();
            }
            templateBuilder.endObject();
        }
        templateBuilder.endObject();
        templateBuilder.startObject("aliases"); {
            templateBuilder.startObject("alias1").endObject();
            templateBuilder.startObject("alias2"); {
                templateBuilder.startObject("filter"); {
                    templateBuilder.startObject("term"); {
                        templateBuilder.field("version", runningAgainstOldCluster ? oldClusterVersion : Version.CURRENT);
                    }
                    templateBuilder.endObject();
                }
                templateBuilder.endObject();
            }
            templateBuilder.endObject();
        }
        templateBuilder.endObject().endObject();
        Request createTemplateRequest = new Request("PUT", "/_template/test_template");
        createTemplateRequest.setJsonEntity(Strings.toString(templateBuilder));
        client().performRequest(createTemplateRequest);

        if (runningAgainstOldCluster) {
            // Create the repo
            XContentBuilder repoConfig = JsonXContent.contentBuilder().startObject(); {
                repoConfig.field("type", "fs");
                repoConfig.startObject("settings"); {
                    repoConfig.field("compress", randomBoolean());
                    repoConfig.field("location", System.getProperty("tests.path.repo"));
                }
                repoConfig.endObject();
            }
            repoConfig.endObject();
            Request createRepoRequest = new Request("PUT", "/_snapshot/repo");
            createRepoRequest.setJsonEntity(Strings.toString(repoConfig));
            client().performRequest(createRepoRequest);
        }

        Request createSnapshot = new Request("PUT", "/_snapshot/repo/" + (runningAgainstOldCluster ? "old_snap" : "new_snap"));
        createSnapshot.addParameter("wait_for_completion", "true");
        createSnapshot.setJsonEntity("{\"indices\": \"" + index + "\"}");
        client().performRequest(createSnapshot);

        checkSnapshot("old_snap", count, oldClusterVersion);
        if (false == runningAgainstOldCluster) {
            checkSnapshot("new_snap", count, Version.CURRENT);
        }
    }

    public void testHistoryUUIDIsAdded() throws Exception {
        if (runningAgainstOldCluster) {
            XContentBuilder mappingsAndSettings = jsonBuilder();
            mappingsAndSettings.startObject();
            {
                mappingsAndSettings.startObject("settings");
                mappingsAndSettings.field("number_of_shards", 1);
                mappingsAndSettings.field("number_of_replicas", 1);
                mappingsAndSettings.endObject();
            }
            mappingsAndSettings.endObject();
            Request createIndex = new Request("PUT", "/" + index);
            createIndex.setJsonEntity(Strings.toString(mappingsAndSettings));
            client().performRequest(createIndex);
        } else {
            Request statsRequest = new Request("GET", index + "/_stats");
            statsRequest.addParameter("level", "shards");
            Response response = client().performRequest(statsRequest);
            List<Object> shardStats = ObjectPath.createFromResponse(response).evaluate("indices." + index + ".shards.0");
            String globalHistoryUUID = null;
            for (Object shard : shardStats) {
                final String nodeId = ObjectPath.evaluate(shard, "routing.node");
                final Boolean primary = ObjectPath.evaluate(shard, "routing.primary");
                logger.info("evaluating: {} , {}", ObjectPath.evaluate(shard, "routing"), ObjectPath.evaluate(shard, "commit"));
                String historyUUID = ObjectPath.evaluate(shard, "commit.user_data.history_uuid");
                assertThat("no history uuid found on " + nodeId + " (primary: " + primary + ")", historyUUID, notNullValue());
                if (globalHistoryUUID == null) {
                    globalHistoryUUID = historyUUID;
                } else {
                    assertThat("history uuid mismatch on " + nodeId + " (primary: " + primary + ")", historyUUID,
                        equalTo(globalHistoryUUID));
                }
            }
        }
    }

    private void checkSnapshot(String snapshotName, int count, Version tookOnVersion) throws IOException {
        // Check the snapshot metadata, especially the version
        Request listSnapshotRequest = new Request("GET", "/_snapshot/repo/" + snapshotName);
        if (false == (runningAgainstOldCluster && oldClusterVersion.before(Version.V_5_5_0))) {
            listSnapshotRequest.addParameter("verbose", "true");
        }
        Map<String, Object> listSnapshotResponse = entityAsMap(client().performRequest(listSnapshotRequest));
        assertEquals(singletonList(snapshotName), XContentMapValues.extractValue("snapshots.snapshot", listSnapshotResponse));
        assertEquals(singletonList("SUCCESS"), XContentMapValues.extractValue("snapshots.state", listSnapshotResponse));
        assertEquals(singletonList(tookOnVersion.toString()), XContentMapValues.extractValue("snapshots.version", listSnapshotResponse));

        // Remove the routing setting and template so we can test restoring them.
        Request clearRoutingFromSettings = new Request("PUT", "/_cluster/settings");
        clearRoutingFromSettings.setJsonEntity("{\"persistent\":{\"cluster.routing.allocation.exclude.test_attr\": null}}");
        client().performRequest(clearRoutingFromSettings);
        client().performRequest(new Request("DELETE", "/_template/test_template"));

        // Restore
        XContentBuilder restoreCommand = JsonXContent.contentBuilder().startObject();
        restoreCommand.field("include_global_state", true);
        restoreCommand.field("indices", index);
        restoreCommand.field("rename_pattern", index);
        restoreCommand.field("rename_replacement", "restored_" + index);
        restoreCommand.endObject();
        Request restoreRequest = new Request("POST", "/_snapshot/repo/" + snapshotName + "/_restore");
        restoreRequest.addParameter("wait_for_completion", "true");
        restoreRequest.setJsonEntity(Strings.toString(restoreCommand));
        client().performRequest(restoreRequest);

        // Make sure search finds all documents
        Request countRequest = new Request("GET", "/restored_" + index + "/_search");
        countRequest.addParameter("size", "0");
        String countResponse = toStr(client().performRequest(countRequest));
        assertThat(countResponse, containsString("\"total\":" + count));

        // Add some extra documents to the index to be sure we can still write to it after restoring it
        int extras = between(1, 100);
        StringBuilder bulk = new StringBuilder();
        for (int i = 0; i < extras; i++) {
            bulk.append("{\"index\":{\"_id\":\"").append(count + i).append("\"}}\n");
            bulk.append("{\"test\":\"test\"}\n");
        }
        Request writeToRestoredRequest = new Request("POST", "/restored_" + index + "/doc/_bulk");
        writeToRestoredRequest.addParameter("refresh", "true");
        writeToRestoredRequest.setJsonEntity(bulk.toString());
        client().performRequest(writeToRestoredRequest);

        // And count to make sure the add worked
        // Make sure search finds all documents
        Request countAfterWriteRequest = new Request("GET", "/restored_" + index + "/_search");
        countAfterWriteRequest.addParameter("size", "0");
        String countAfterWriteResponse = toStr(client().performRequest(countAfterWriteRequest));
        assertThat(countAfterWriteResponse, containsString("\"total\":" + (count + extras)));

        // Clean up the index for the next iteration
        client().performRequest(new Request("DELETE", "/restored_*"));

        // Check settings added by the restore process
        Request clusterSettingsRequest = new Request("GET", "/_cluster/settings");
        clusterSettingsRequest.addParameter("flat_settings", "true");
        Map<String, Object> clusterSettingsResponse = entityAsMap(client().performRequest(clusterSettingsRequest));
        Map<String, Object> expectedClusterSettings = new HashMap<>();
        expectedClusterSettings.put("transient", emptyMap());
        expectedClusterSettings.put("persistent",
                singletonMap("cluster.routing.allocation.exclude.test_attr", oldClusterVersion.toString()));
        if (expectedClusterSettings.equals(clusterSettingsResponse) == false) {
            NotEqualMessageBuilder builder = new NotEqualMessageBuilder();
            builder.compareMaps(clusterSettingsResponse, expectedClusterSettings);
            fail("settings don't match:\n" + builder.toString());
        }

        // Check that the template was restored successfully
        Map<String, Object> getTemplateResponse = entityAsMap(client().performRequest(new Request("GET", "/_template/test_template")));
        Map<String, Object> expectedTemplate = new HashMap<>();
        if (runningAgainstOldCluster && oldClusterVersion.before(Version.V_6_0_0_beta1)) {
            expectedTemplate.put("template", "evil_*");
        } else {
            expectedTemplate.put("index_patterns", singletonList("evil_*"));
        }
        expectedTemplate.put("settings", singletonMap("index", singletonMap("number_of_shards", "1")));
        expectedTemplate.put("mappings", singletonMap("doc", singletonMap("_source", singletonMap("enabled", true))));
        expectedTemplate.put("order", 0);
        Map<String, Object> aliases = new HashMap<>();
        aliases.put("alias1", emptyMap());
        aliases.put("alias2", singletonMap("filter", singletonMap("term", singletonMap("version", tookOnVersion.toString()))));
        expectedTemplate.put("aliases", aliases);
        expectedTemplate = singletonMap("test_template", expectedTemplate);
        if (false == expectedTemplate.equals(getTemplateResponse)) {
            NotEqualMessageBuilder builder = new NotEqualMessageBuilder();
            builder.compareMaps(getTemplateResponse, expectedTemplate);
            fail("template doesn't match:\n" + builder.toString());
        }
    }

    // TODO tests for upgrades after shrink. We've had trouble with shrink in the past.

    private void indexRandomDocuments(int count, boolean flushAllowed, boolean saveInfo,
                                      CheckedFunction<Integer, XContentBuilder, IOException> docSupplier) throws IOException {
        logger.info("Indexing {} random documents", count);
        for (int i = 0; i < count; i++) {
            logger.debug("Indexing document [{}]", i);
            Request createDocument = new Request("POST", "/" + index + "/doc/" + i);
            createDocument.setJsonEntity(Strings.toString(docSupplier.apply(i)));
            client().performRequest(createDocument);
            if (rarely()) {
                refresh();
            }
            if (flushAllowed && rarely()) {
                logger.debug("Flushing [{}]", index);
                client().performRequest(new Request("POST", "/" + index + "/_flush"));
            }
        }
        if (saveInfo) {
            saveInfoDocument("count", Integer.toString(count));
        }
    }

    private int countOfIndexedRandomDocuments() throws IOException {
        return Integer.parseInt(loadInfoDocument("count"));
    }

    private void saveInfoDocument(String type, String value) throws IOException {
        XContentBuilder infoDoc = JsonXContent.contentBuilder().startObject();
        infoDoc.field("value", value);
        infoDoc.endObject();
        // Only create the first version so we know how many documents are created when the index is first created
        Request request = new Request("PUT", "/info/doc/" + index + "_" + type);
        request.addParameter("op_type", "create");
        request.setJsonEntity(Strings.toString(infoDoc));
        client().performRequest(request);
    }

    private String loadInfoDocument(String type) throws IOException {
        Request request = new Request("GET", "/info/doc/" + index + "_" + type);
        request.addParameter("filter_path", "_source");
        String doc = toStr(client().performRequest(request));
        Matcher m = Pattern.compile("\"value\":\"(.+)\"").matcher(doc);
        assertTrue(doc, m.find());
        return m.group(1);
    }

    private Object randomLenientBoolean() {
        return randomFrom(new Object[] {"off", "no", "0", 0, "false", false, "on", "yes", "1", 1, "true", true});
    }

    private void refresh() throws IOException {
        logger.debug("Refreshing [{}]", index);
        client().performRequest(new Request("POST", "/" + index + "/_refresh"));
    }

    /**
     * Wait for an index to have green health, waiting longer than
     * {@link ESRestTestCase#ensureGreen}.
     */
    protected void ensureGreenLongWait(String index) throws IOException {
        Request request = new Request("GET", "/_cluster/health/" + index);
        request.addParameter("timeout", "2m");
        request.addParameter("wait_for_status", "green");
        request.addParameter("wait_for_no_relocating_shards", "true");
        request.addParameter("wait_for_events", "languid");
        request.addParameter("level", "shards");
        Map<String, Object> healthRsp = entityAsMap(client().performRequest(request));
        logger.info("health api response: {}", healthRsp);
        assertEquals("green", healthRsp.get("status"));
        assertFalse((Boolean) healthRsp.get("timed_out"));
    }
}
