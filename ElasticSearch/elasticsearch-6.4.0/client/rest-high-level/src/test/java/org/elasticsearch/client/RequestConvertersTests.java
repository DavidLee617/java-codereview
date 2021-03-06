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

package org.elasticsearch.client;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.cancel.CancelTasksRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksRequest;
import org.elasticsearch.action.admin.cluster.repositories.delete.DeleteRepositoryRequest;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesRequest;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryRequest;
import org.elasticsearch.action.admin.cluster.repositories.verify.VerifyRepositoryRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsRequest;
import org.elasticsearch.action.admin.cluster.storedscripts.DeleteStoredScriptRequest;
import org.elasticsearch.action.admin.cluster.storedscripts.GetStoredScriptRequest;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotsStatusRequest;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.SyncedFlushRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.rollover.RolloverRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.shrink.ResizeRequest;
import org.elasticsearch.action.admin.indices.shrink.ResizeType;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkShardRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.ingest.DeletePipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineRequest;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.ingest.SimulatePipelineRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.MasterNodeReadRequest;
import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.action.support.replication.ReplicationRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestConverters.EndpointBuilder;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.CheckedBiConsumer;
import org.elasticsearch.common.CheckedFunction;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.CollectionUtils;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.RandomCreateIndexGenerator;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.rankeval.PrecisionAtK;
import org.elasticsearch.index.rankeval.RankEvalRequest;
import org.elasticsearch.index.rankeval.RankEvalSpec;
import org.elasticsearch.index.rankeval.RatedRequest;
import org.elasticsearch.index.rankeval.RestRankEvalAction;
import org.elasticsearch.protocol.xpack.XPackInfoRequest;
import org.elasticsearch.protocol.xpack.watcher.PutWatchRequest;
import org.elasticsearch.repositories.fs.FsRepository;
import org.elasticsearch.rest.action.search.RestSearchAction;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.MultiSearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.rescore.QueryRescorerBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.tasks.TaskId;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.RandomObjects;
import org.hamcrest.CoreMatchers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;
import static org.elasticsearch.client.RequestConverters.REQUEST_BODY_CONTENT_TYPE;
import static org.elasticsearch.client.RequestConverters.enforceSameContentType;
import static org.elasticsearch.index.RandomCreateIndexGenerator.randomAliases;
import static org.elasticsearch.index.RandomCreateIndexGenerator.randomCreateIndexRequest;
import static org.elasticsearch.index.RandomCreateIndexGenerator.randomIndexSettings;
import static org.elasticsearch.index.alias.RandomAliasActionsGenerator.randomAliasAction;
import static org.elasticsearch.search.RandomSearchRequestGenerator.randomSearchRequest;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertToXContentEquivalent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class RequestConvertersTests extends ESTestCase {
    public void testPing() {
        Request request = RequestConverters.ping();
        assertEquals("/", request.getEndpoint());
        assertEquals(0, request.getParameters().size());
        assertNull(request.getEntity());
        assertEquals(HttpHead.METHOD_NAME, request.getMethod());
    }

    public void testInfo() {
        Request request = RequestConverters.info();
        assertEquals("/", request.getEndpoint());
        assertEquals(0, request.getParameters().size());
        assertNull(request.getEntity());
        assertEquals(HttpGet.METHOD_NAME, request.getMethod());
    }

    public void testGet() {
        getAndExistsTest(RequestConverters::get, HttpGet.METHOD_NAME);
    }

    public void testMultiGet() throws IOException {
        Map<String, String> expectedParams = new HashMap<>();
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        if (randomBoolean()) {
            String preference = randomAlphaOfLength(4);
            multiGetRequest.preference(preference);
            expectedParams.put("preference", preference);
        }
        if (randomBoolean()) {
            multiGetRequest.realtime(randomBoolean());
            if (multiGetRequest.realtime() == false) {
                expectedParams.put("realtime", "false");
            }
        }
        if (randomBoolean()) {
            multiGetRequest.refresh(randomBoolean());
            if (multiGetRequest.refresh()) {
                expectedParams.put("refresh", "true");
            }
        }

        int numberOfRequests = randomIntBetween(0, 32);
        for (int i = 0; i < numberOfRequests; i++) {
            MultiGetRequest.Item item = new MultiGetRequest.Item(randomAlphaOfLength(4), randomAlphaOfLength(4), randomAlphaOfLength(4));
            if (randomBoolean()) {
                item.routing(randomAlphaOfLength(4));
            }
            if (randomBoolean()) {
                item.parent(randomAlphaOfLength(4));
            }
            if (randomBoolean()) {
                item.storedFields(generateRandomStringArray(16, 8, false));
            }
            if (randomBoolean()) {
                item.version(randomNonNegativeLong());
            }
            if (randomBoolean()) {
                item.versionType(randomFrom(VersionType.values()));
            }
            if (randomBoolean()) {
                randomizeFetchSourceContextParams(item::fetchSourceContext, new HashMap<>());
            }
            multiGetRequest.add(item);
        }

        Request request = RequestConverters.multiGet(multiGetRequest);
        assertEquals(HttpPost.METHOD_NAME, request.getMethod());
        assertEquals("/_mget", request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertToXContentBody(multiGetRequest, request.getEntity());
    }

    public void testDelete() {
        String index = randomAlphaOfLengthBetween(3, 10);
        String type = randomAlphaOfLengthBetween(3, 10);
        String id = randomAlphaOfLengthBetween(3, 10);
        DeleteRequest deleteRequest = new DeleteRequest(index, type, id);

        Map<String, String> expectedParams = new HashMap<>();

        setRandomTimeout(deleteRequest::timeout, ReplicationRequest.DEFAULT_TIMEOUT, expectedParams);
        setRandomRefreshPolicy(deleteRequest::setRefreshPolicy, expectedParams);
        setRandomVersion(deleteRequest, expectedParams);
        setRandomVersionType(deleteRequest::versionType, expectedParams);

        if (frequently()) {
            if (randomBoolean()) {
                String routing = randomAlphaOfLengthBetween(3, 10);
                deleteRequest.routing(routing);
                expectedParams.put("routing", routing);
            }
            if (randomBoolean()) {
                String parent = randomAlphaOfLengthBetween(3, 10);
                deleteRequest.parent(parent);
                expectedParams.put("parent", parent);
            }
        }

        Request request = RequestConverters.delete(deleteRequest);
        assertEquals("/" + index + "/" + type + "/" + id, request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertEquals(HttpDelete.METHOD_NAME, request.getMethod());
        assertNull(request.getEntity());
    }

    public void testExists() {
        getAndExistsTest(RequestConverters::exists, HttpHead.METHOD_NAME);
    }

    public void testIndicesExist() {
        String[] indices = randomIndicesNames(1, 10);

        GetIndexRequest getIndexRequest = new GetIndexRequest().indices(indices);

        Map<String, String> expectedParams = new HashMap<>();
        setRandomIndicesOptions(getIndexRequest::indicesOptions, getIndexRequest::indicesOptions, expectedParams);
        setRandomLocal(getIndexRequest, expectedParams);
        setRandomHumanReadable(getIndexRequest, expectedParams);
        setRandomIncludeDefaults(getIndexRequest, expectedParams);

        final Request request = RequestConverters.indicesExist(getIndexRequest);

        assertEquals(HttpHead.METHOD_NAME, request.getMethod());
        assertEquals("/" + String.join(",", indices), request.getEndpoint());
        assertThat(expectedParams, equalTo(request.getParameters()));
        assertNull(request.getEntity());
    }

    public void testIndicesExistEmptyIndices() {
        expectThrows(IllegalArgumentException.class, () -> RequestConverters.indicesExist(new GetIndexRequest()));
        expectThrows(IllegalArgumentException.class, () -> RequestConverters.indicesExist(new GetIndexRequest().indices((String[]) null)));
    }

    private static void getAndExistsTest(Function<GetRequest, Request> requestConverter, String method) {
        String index = randomAlphaOfLengthBetween(3, 10);
        String type = randomAlphaOfLengthBetween(3, 10);
        String id = randomAlphaOfLengthBetween(3, 10);
        GetRequest getRequest = new GetRequest(index, type, id);

        Map<String, String> expectedParams = new HashMap<>();
        if (randomBoolean()) {
            if (randomBoolean()) {
                String preference = randomAlphaOfLengthBetween(3, 10);
                getRequest.preference(preference);
                expectedParams.put("preference", preference);
            }
            if (randomBoolean()) {
                String routing = randomAlphaOfLengthBetween(3, 10);
                getRequest.routing(routing);
                expectedParams.put("routing", routing);
            }
            if (randomBoolean()) {
                boolean realtime = randomBoolean();
                getRequest.realtime(realtime);
                if (realtime == false) {
                    expectedParams.put("realtime", "false");
                }
            }
            if (randomBoolean()) {
                boolean refresh = randomBoolean();
                getRequest.refresh(refresh);
                if (refresh) {
                    expectedParams.put("refresh", "true");
                }
            }
            if (randomBoolean()) {
                long version = randomLong();
                getRequest.version(version);
                if (version != Versions.MATCH_ANY) {
                    expectedParams.put("version", Long.toString(version));
                }
            }
            setRandomVersionType(getRequest::versionType, expectedParams);
            if (randomBoolean()) {
                int numStoredFields = randomIntBetween(1, 10);
                String[] storedFields = new String[numStoredFields];
                String storedFieldsParam = randomFields(storedFields);
                getRequest.storedFields(storedFields);
                expectedParams.put("stored_fields", storedFieldsParam);
            }
            if (randomBoolean()) {
                randomizeFetchSourceContextParams(getRequest::fetchSourceContext, expectedParams);
            }
        }
        Request request = requestConverter.apply(getRequest);
        assertEquals("/" + index + "/" + type + "/" + id, request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertNull(request.getEntity());
        assertEquals(method, request.getMethod());
    }

    public void testCreateIndex() throws IOException {
        CreateIndexRequest createIndexRequest = randomCreateIndexRequest();

        Map<String, String> expectedParams = new HashMap<>();
        setRandomTimeout(createIndexRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        setRandomMasterTimeout(createIndexRequest, expectedParams);
        setRandomWaitForActiveShards(createIndexRequest::waitForActiveShards, ActiveShardCount.DEFAULT, expectedParams);
        setRandomUpdateAllTypes(createIndexRequest::updateAllTypes, expectedParams);

        Request request = RequestConverters.createIndex(createIndexRequest);
        assertEquals("/" + createIndexRequest.index(), request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertEquals(HttpPut.METHOD_NAME, request.getMethod());
        assertToXContentBody(createIndexRequest, request.getEntity());
    }

    public void testCreateIndexNullIndex() {
        ActionRequestValidationException validationException = new CreateIndexRequest(null).validate();
        assertNotNull(validationException);
    }

    public void testUpdateAliases() throws IOException {
        IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        AliasActions aliasAction = randomAliasAction();
        indicesAliasesRequest.addAliasAction(aliasAction);

        Map<String, String> expectedParams = new HashMap<>();
        setRandomTimeout(indicesAliasesRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        setRandomMasterTimeout(indicesAliasesRequest, expectedParams);

        Request request = RequestConverters.updateAliases(indicesAliasesRequest);
        assertEquals("/_aliases", request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertToXContentBody(indicesAliasesRequest, request.getEntity());
    }

    public void testPutMapping() throws IOException {
        PutMappingRequest putMappingRequest = new PutMappingRequest();

        String[] indices = randomIndicesNames(0, 5);
        putMappingRequest.indices(indices);

        String type = randomAlphaOfLengthBetween(3, 10);
        putMappingRequest.type(type);

        Map<String, String> expectedParams = new HashMap<>();

        setRandomTimeout(putMappingRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        setRandomMasterTimeout(putMappingRequest, expectedParams);
        setRandomUpdateAllTypes(putMappingRequest::updateAllTypes, expectedParams);

        Request request = RequestConverters.putMapping(putMappingRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        String index = String.join(",", indices);
        if (Strings.hasLength(index)) {
            endpoint.add(index);
        }
        endpoint.add("_mapping");
        endpoint.add(type);
        assertEquals(endpoint.toString(), request.getEndpoint());

        assertEquals(expectedParams, request.getParameters());
        assertEquals(HttpPut.METHOD_NAME, request.getMethod());
        assertToXContentBody(putMappingRequest, request.getEntity());
    }

    public void testGetMapping() throws IOException {
        GetMappingsRequest getMappingRequest = new GetMappingsRequest();

        String[] indices = Strings.EMPTY_ARRAY;
        if (randomBoolean()) {
            indices = randomIndicesNames(0, 5);
            getMappingRequest.indices(indices);
        } else if (randomBoolean()) {
            getMappingRequest.indices((String[]) null);
        }

        String type = null;
        if (randomBoolean()) {
            type = randomAlphaOfLengthBetween(3, 10);
            getMappingRequest.types(type);
        } else if (randomBoolean()) {
            getMappingRequest.types((String[]) null);
        }

        Map<String, String> expectedParams = new HashMap<>();

        setRandomIndicesOptions(getMappingRequest::indicesOptions, getMappingRequest::indicesOptions, expectedParams);
        setRandomMasterTimeout(getMappingRequest, expectedParams);
        setRandomLocal(getMappingRequest, expectedParams);

        Request request = RequestConverters.getMappings(getMappingRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        String index = String.join(",", indices);
        if (Strings.hasLength(index)) {
            endpoint.add(index);
        }
        endpoint.add("_mapping");
        if (type != null) {
            endpoint.add(type);
        }
        assertThat(endpoint.toString(), equalTo(request.getEndpoint()));

        assertThat(expectedParams, equalTo(request.getParameters()));
        assertThat(HttpGet.METHOD_NAME, equalTo(request.getMethod()));
    }

    public void testGetFieldMapping() throws IOException {
        GetFieldMappingsRequest getFieldMappingsRequest = new GetFieldMappingsRequest();

        String[] indices = Strings.EMPTY_ARRAY;
        if (randomBoolean()) {
            indices = randomIndicesNames(0, 5);
            getFieldMappingsRequest.indices(indices);
        } else if (randomBoolean()) {
            getFieldMappingsRequest.indices((String[]) null);
        }

        String type = null;
        if (randomBoolean()) {
            type = randomAlphaOfLengthBetween(3, 10);
            getFieldMappingsRequest.types(type);
        } else if (randomBoolean()) {
            getFieldMappingsRequest.types((String[]) null);
        }

        String[] fields = null;
        if (randomBoolean()) {
            fields = new String[randomIntBetween(1, 5)];
            for (int i = 0; i < fields.length; i++) {
                fields[i] = randomAlphaOfLengthBetween(3, 10);
            }
            getFieldMappingsRequest.fields(fields);
        } else if (randomBoolean()) {
            getFieldMappingsRequest.fields((String[]) null);
        }

        Map<String, String> expectedParams = new HashMap<>();

        setRandomIndicesOptions(getFieldMappingsRequest::indicesOptions, getFieldMappingsRequest::indicesOptions, expectedParams);
        setRandomLocal(getFieldMappingsRequest::local, expectedParams);

        Request request = RequestConverters.getFieldMapping(getFieldMappingsRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        String index = String.join(",", indices);
        if (Strings.hasLength(index)) {
            endpoint.add(index);
        }
        endpoint.add("_mapping");
        if (type != null) {
            endpoint.add(type);
        }
        endpoint.add("field");
        if (fields != null) {
            endpoint.add(String.join(",", fields));
        }
        assertThat(endpoint.toString(), equalTo(request.getEndpoint()));

        assertThat(expectedParams, equalTo(request.getParameters()));
        assertThat(HttpGet.METHOD_NAME, equalTo(request.getMethod()));
    }

    public void testDeleteIndex() {
        String[] indices = randomIndicesNames(0, 5);
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indices);

        Map<String, String> expectedParams = new HashMap<>();
        setRandomTimeout(deleteIndexRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        setRandomMasterTimeout(deleteIndexRequest, expectedParams);

        setRandomIndicesOptions(deleteIndexRequest::indicesOptions, deleteIndexRequest::indicesOptions, expectedParams);

        Request request = RequestConverters.deleteIndex(deleteIndexRequest);
        assertEquals("/" + String.join(",", indices), request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertEquals(HttpDelete.METHOD_NAME, request.getMethod());
        assertNull(request.getEntity());
    }

    public void testGetSettings() throws IOException {
        String[] indicesUnderTest = randomBoolean() ? null : randomIndicesNames(0, 5);

        GetSettingsRequest getSettingsRequest = new GetSettingsRequest().indices(indicesUnderTest);

        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(getSettingsRequest, expectedParams);
        setRandomIndicesOptions(getSettingsRequest::indicesOptions, getSettingsRequest::indicesOptions, expectedParams);

        setRandomLocal(getSettingsRequest, expectedParams);

        if (randomBoolean()) {
            //the request object will not have include_defaults present unless it is set to true
            getSettingsRequest.includeDefaults(randomBoolean());
            if (getSettingsRequest.includeDefaults()) {
                expectedParams.put("include_defaults", Boolean.toString(true));
            }
        }

        StringJoiner endpoint = new StringJoiner("/", "/", "");
        if (indicesUnderTest != null && indicesUnderTest.length > 0) {
            endpoint.add(String.join(",", indicesUnderTest));
        }
        endpoint.add("_settings");

        if (randomBoolean()) {
            String[] names = randomBoolean() ? null : new String[randomIntBetween(0, 3)];
            if (names != null) {
                for (int x = 0; x < names.length; x++) {
                    names[x] = randomAlphaOfLengthBetween(3, 10);
                }
            }
            getSettingsRequest.names(names);
            if (names != null && names.length > 0) {
                endpoint.add(String.join(",", names));
            }
        }

        Request request = RequestConverters.getSettings(getSettingsRequest);

        assertThat(endpoint.toString(), equalTo(request.getEndpoint()));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request.getEntity(), nullValue());
    }

    public void testGetIndex() throws IOException {
        String[] indicesUnderTest = randomBoolean() ? null : randomIndicesNames(0, 5);

        GetIndexRequest getIndexRequest = new GetIndexRequest().indices(indicesUnderTest);

        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(getIndexRequest, expectedParams);
        setRandomIndicesOptions(getIndexRequest::indicesOptions, getIndexRequest::indicesOptions, expectedParams);
        setRandomLocal(getIndexRequest, expectedParams);
        setRandomHumanReadable(getIndexRequest, expectedParams);

        if (randomBoolean()) {
            // the request object will not have include_defaults present unless it is set to
            // true
            getIndexRequest.includeDefaults(randomBoolean());
            if (getIndexRequest.includeDefaults()) {
                expectedParams.put("include_defaults", Boolean.toString(true));
            }
        }

        StringJoiner endpoint = new StringJoiner("/", "/", "");
        if (indicesUnderTest != null && indicesUnderTest.length > 0) {
            endpoint.add(String.join(",", indicesUnderTest));
        }

        Request request = RequestConverters.getIndex(getIndexRequest);

        assertThat(endpoint.toString(), equalTo(request.getEndpoint()));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request.getEntity(), nullValue());
    }

    public void testDeleteIndexEmptyIndices() {
        String[] indices = randomBoolean() ? null : Strings.EMPTY_ARRAY;
        ActionRequestValidationException validationException = new DeleteIndexRequest(indices).validate();
        assertNotNull(validationException);
    }

    public void testOpenIndex() {
        String[] indices = randomIndicesNames(1, 5);
        OpenIndexRequest openIndexRequest = new OpenIndexRequest(indices);
        openIndexRequest.indices(indices);

        Map<String, String> expectedParams = new HashMap<>();
        setRandomTimeout(openIndexRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        setRandomMasterTimeout(openIndexRequest, expectedParams);
        setRandomIndicesOptions(openIndexRequest::indicesOptions, openIndexRequest::indicesOptions, expectedParams);
        setRandomWaitForActiveShards(openIndexRequest::waitForActiveShards, ActiveShardCount.NONE, expectedParams);

        Request request = RequestConverters.openIndex(openIndexRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "").add(String.join(",", indices)).add("_open");
        assertThat(endpoint.toString(), equalTo(request.getEndpoint()));
        assertThat(expectedParams, equalTo(request.getParameters()));
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
        assertThat(request.getEntity(), nullValue());
    }

    public void testOpenIndexEmptyIndices() {
        String[] indices = randomBoolean() ? null : Strings.EMPTY_ARRAY;
        ActionRequestValidationException validationException = new OpenIndexRequest(indices).validate();
        assertNotNull(validationException);
    }

    public void testCloseIndex() {
        String[] indices = randomIndicesNames(1, 5);
        CloseIndexRequest closeIndexRequest = new CloseIndexRequest(indices);

        Map<String, String> expectedParams = new HashMap<>();
        setRandomTimeout(closeIndexRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        setRandomMasterTimeout(closeIndexRequest, expectedParams);
        setRandomIndicesOptions(closeIndexRequest::indicesOptions, closeIndexRequest::indicesOptions, expectedParams);

        Request request = RequestConverters.closeIndex(closeIndexRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "").add(String.join(",", indices)).add("_close");
        assertThat(endpoint.toString(), equalTo(request.getEndpoint()));
        assertThat(expectedParams, equalTo(request.getParameters()));
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
        assertThat(request.getEntity(), nullValue());
    }

    public void testCloseIndexEmptyIndices() {
        String[] indices = randomBoolean() ? null : Strings.EMPTY_ARRAY;
        ActionRequestValidationException validationException = new CloseIndexRequest(indices).validate();
        assertNotNull(validationException);
    }

    public void testIndex() throws IOException {
        String index = randomAlphaOfLengthBetween(3, 10);
        String type = randomAlphaOfLengthBetween(3, 10);
        IndexRequest indexRequest = new IndexRequest(index, type);

        String id = randomBoolean() ? randomAlphaOfLengthBetween(3, 10) : null;
        indexRequest.id(id);

        Map<String, String> expectedParams = new HashMap<>();

        String method = HttpPost.METHOD_NAME;
        if (id != null) {
            method = HttpPut.METHOD_NAME;
            if (randomBoolean()) {
                indexRequest.opType(DocWriteRequest.OpType.CREATE);
            }
        }

        setRandomTimeout(indexRequest::timeout, ReplicationRequest.DEFAULT_TIMEOUT, expectedParams);
        setRandomRefreshPolicy(indexRequest::setRefreshPolicy, expectedParams);

        // There is some logic around _create endpoint and version/version type
        if (indexRequest.opType() == DocWriteRequest.OpType.CREATE) {
            indexRequest.version(randomFrom(Versions.MATCH_ANY, Versions.MATCH_DELETED));
            expectedParams.put("version", Long.toString(Versions.MATCH_DELETED));
        } else {
            setRandomVersion(indexRequest, expectedParams);
            setRandomVersionType(indexRequest::versionType, expectedParams);
        }

        if (frequently()) {
            if (randomBoolean()) {
                String routing = randomAlphaOfLengthBetween(3, 10);
                indexRequest.routing(routing);
                expectedParams.put("routing", routing);
            }
            if (randomBoolean()) {
                String parent = randomAlphaOfLengthBetween(3, 10);
                indexRequest.parent(parent);
                expectedParams.put("parent", parent);
            }
            if (randomBoolean()) {
                String pipeline = randomAlphaOfLengthBetween(3, 10);
                indexRequest.setPipeline(pipeline);
                expectedParams.put("pipeline", pipeline);
            }
        }

        XContentType xContentType = randomFrom(XContentType.values());
        int nbFields = randomIntBetween(0, 10);
        try (XContentBuilder builder = XContentBuilder.builder(xContentType.xContent())) {
            builder.startObject();
            for (int i = 0; i < nbFields; i++) {
                builder.field("field_" + i, i);
            }
            builder.endObject();
            indexRequest.source(builder);
        }

        Request request = RequestConverters.index(indexRequest);
        if (indexRequest.opType() == DocWriteRequest.OpType.CREATE) {
            assertEquals("/" + index + "/" + type + "/" + id + "/_create", request.getEndpoint());
        } else if (id != null) {
            assertEquals("/" + index + "/" + type + "/" + id, request.getEndpoint());
        } else {
            assertEquals("/" + index + "/" + type, request.getEndpoint());
        }
        assertEquals(expectedParams, request.getParameters());
        assertEquals(method, request.getMethod());

        HttpEntity entity = request.getEntity();
        assertTrue(entity instanceof ByteArrayEntity);
        assertEquals(indexRequest.getContentType().mediaTypeWithoutParameters(), entity.getContentType().getValue());
        try (XContentParser parser = createParser(xContentType.xContent(), entity.getContent())) {
            assertEquals(nbFields, parser.map().size());
        }
    }

    public void testRefresh() {
        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 5);
        RefreshRequest refreshRequest;
        if (randomBoolean()) {
            refreshRequest = new RefreshRequest(indices);
        } else {
            refreshRequest = new RefreshRequest();
            refreshRequest.indices(indices);
        }
        Map<String, String> expectedParams = new HashMap<>();
        setRandomIndicesOptions(refreshRequest::indicesOptions, refreshRequest::indicesOptions, expectedParams);
        Request request = RequestConverters.refresh(refreshRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        if (indices != null && indices.length > 0) {
            endpoint.add(String.join(",", indices));
        }
        endpoint.add("_refresh");
        assertThat(request.getEndpoint(), equalTo(endpoint.toString()));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), nullValue());
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
    }

    public void testFlush() {
        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 5);
        FlushRequest flushRequest;
        if (randomBoolean()) {
            flushRequest = new FlushRequest(indices);
        } else {
            flushRequest = new FlushRequest();
            flushRequest.indices(indices);
        }
        Map<String, String> expectedParams = new HashMap<>();
        setRandomIndicesOptions(flushRequest::indicesOptions, flushRequest::indicesOptions, expectedParams);
        if (randomBoolean()) {
            flushRequest.force(randomBoolean());
        }
        expectedParams.put("force", Boolean.toString(flushRequest.force()));
        if (randomBoolean()) {
            flushRequest.waitIfOngoing(randomBoolean());
        }
        expectedParams.put("wait_if_ongoing", Boolean.toString(flushRequest.waitIfOngoing()));

        Request request = RequestConverters.flush(flushRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        if (indices != null && indices.length > 0) {
            endpoint.add(String.join(",", indices));
        }
        endpoint.add("_flush");
        assertThat(request.getEndpoint(), equalTo(endpoint.toString()));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), nullValue());
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
    }

    public void testSyncedFlush() {
        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 5);
        SyncedFlushRequest syncedFlushRequest;
        if (randomBoolean()) {
            syncedFlushRequest = new SyncedFlushRequest(indices);
        } else {
            syncedFlushRequest = new SyncedFlushRequest();
            syncedFlushRequest.indices(indices);
        }
        Map<String, String> expectedParams = new HashMap<>();
        setRandomIndicesOptions(syncedFlushRequest::indicesOptions, syncedFlushRequest::indicesOptions, expectedParams);
        Request request = RequestConverters.flushSynced(syncedFlushRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        if (indices != null && indices.length > 0) {
                endpoint.add(String.join(",", indices));
            }
        endpoint.add("_flush/synced");
        assertThat(request.getEndpoint(), equalTo(endpoint.toString()));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), nullValue());
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
    }

    public void testForceMerge() {
        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 5);
        ForceMergeRequest forceMergeRequest;
        if (randomBoolean()) {
            forceMergeRequest = new ForceMergeRequest(indices);
        } else {
            forceMergeRequest = new ForceMergeRequest();
            forceMergeRequest.indices(indices);
        }

        Map<String, String> expectedParams = new HashMap<>();
        setRandomIndicesOptions(forceMergeRequest::indicesOptions, forceMergeRequest::indicesOptions, expectedParams);
        if (randomBoolean()) {
            forceMergeRequest.maxNumSegments(randomInt());
        }
        expectedParams.put("max_num_segments", Integer.toString(forceMergeRequest.maxNumSegments()));
        if (randomBoolean()) {
            forceMergeRequest.onlyExpungeDeletes(randomBoolean());
        }
        expectedParams.put("only_expunge_deletes", Boolean.toString(forceMergeRequest.onlyExpungeDeletes()));
        if (randomBoolean()) {
            forceMergeRequest.flush(randomBoolean());
        }
        expectedParams.put("flush", Boolean.toString(forceMergeRequest.flush()));

        Request request = RequestConverters.forceMerge(forceMergeRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        if (indices != null && indices.length > 0) {
            endpoint.add(String.join(",", indices));
        }
        endpoint.add("_forcemerge");
        assertThat(request.getEndpoint(), equalTo(endpoint.toString()));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), nullValue());
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
    }

    public void testClearCache() {
        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 5);
        ClearIndicesCacheRequest clearIndicesCacheRequest;
        if (randomBoolean()) {
            clearIndicesCacheRequest = new ClearIndicesCacheRequest(indices);
        } else {
            clearIndicesCacheRequest = new ClearIndicesCacheRequest();
            clearIndicesCacheRequest.indices(indices);
        }
        Map<String, String> expectedParams = new HashMap<>();
        setRandomIndicesOptions(clearIndicesCacheRequest::indicesOptions, clearIndicesCacheRequest::indicesOptions, expectedParams);
        if (randomBoolean()) {
            clearIndicesCacheRequest.queryCache(randomBoolean());
        }
        expectedParams.put("query", Boolean.toString(clearIndicesCacheRequest.queryCache()));
        if (randomBoolean()) {
            clearIndicesCacheRequest.fieldDataCache(randomBoolean());
        }
        expectedParams.put("fielddata", Boolean.toString(clearIndicesCacheRequest.fieldDataCache()));
        if (randomBoolean()) {
            clearIndicesCacheRequest.requestCache(randomBoolean());
        }
        expectedParams.put("request", Boolean.toString(clearIndicesCacheRequest.requestCache()));
        if (randomBoolean()) {
            clearIndicesCacheRequest.fields(randomIndicesNames(1, 5));
            expectedParams.put("fields", String.join(",", clearIndicesCacheRequest.fields()));
        }

        Request request = RequestConverters.clearCache(clearIndicesCacheRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        if (indices != null && indices.length > 0) {
            endpoint.add(String.join(",", indices));
        }
        endpoint.add("_cache/clear");
        assertThat(request.getEndpoint(), equalTo(endpoint.toString()));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), nullValue());
        assertThat(request.getMethod(), equalTo(HttpPost.METHOD_NAME));
    }

    public void testUpdate() throws IOException {
        XContentType xContentType = randomFrom(XContentType.values());

        Map<String, String> expectedParams = new HashMap<>();
        String index = randomAlphaOfLengthBetween(3, 10);
        String type = randomAlphaOfLengthBetween(3, 10);
        String id = randomAlphaOfLengthBetween(3, 10);

        UpdateRequest updateRequest = new UpdateRequest(index, type, id);
        updateRequest.detectNoop(randomBoolean());

        if (randomBoolean()) {
            BytesReference source = RandomObjects.randomSource(random(), xContentType);
            updateRequest.doc(new IndexRequest().source(source, xContentType));

            boolean docAsUpsert = randomBoolean();
            updateRequest.docAsUpsert(docAsUpsert);
            if (docAsUpsert) {
                expectedParams.put("doc_as_upsert", "true");
            }
        } else {
            updateRequest.script(mockScript("_value + 1"));
            updateRequest.scriptedUpsert(randomBoolean());
        }
        if (randomBoolean()) {
            BytesReference source = RandomObjects.randomSource(random(), xContentType);
            updateRequest.upsert(new IndexRequest().source(source, xContentType));
        }
        if (randomBoolean()) {
            String routing = randomAlphaOfLengthBetween(3, 10);
            updateRequest.routing(routing);
            expectedParams.put("routing", routing);
        }
        if (randomBoolean()) {
            String parent = randomAlphaOfLengthBetween(3, 10);
            updateRequest.parent(parent);
            expectedParams.put("parent", parent);
        }
        if (randomBoolean()) {
            String timeout = randomTimeValue();
            updateRequest.timeout(timeout);
            expectedParams.put("timeout", timeout);
        } else {
            expectedParams.put("timeout", ReplicationRequest.DEFAULT_TIMEOUT.getStringRep());
        }
        if (randomBoolean()) {
            WriteRequest.RefreshPolicy refreshPolicy = randomFrom(WriteRequest.RefreshPolicy.values());
            updateRequest.setRefreshPolicy(refreshPolicy);
            if (refreshPolicy != WriteRequest.RefreshPolicy.NONE) {
                expectedParams.put("refresh", refreshPolicy.getValue());
            }
        }
        setRandomWaitForActiveShards(updateRequest::waitForActiveShards, ActiveShardCount.DEFAULT, expectedParams);
        setRandomVersion(updateRequest, expectedParams);
        setRandomVersionType(updateRequest::versionType, expectedParams);
        if (randomBoolean()) {
            int retryOnConflict = randomIntBetween(0, 5);
            updateRequest.retryOnConflict(retryOnConflict);
            if (retryOnConflict > 0) {
                expectedParams.put("retry_on_conflict", String.valueOf(retryOnConflict));
            }
        }
        if (randomBoolean()) {
            randomizeFetchSourceContextParams(updateRequest::fetchSource, expectedParams);
        }

        Request request = RequestConverters.update(updateRequest);
        assertEquals("/" + index + "/" + type + "/" + id + "/_update", request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertEquals(HttpPost.METHOD_NAME, request.getMethod());

        HttpEntity entity = request.getEntity();
        assertTrue(entity instanceof ByteArrayEntity);

        UpdateRequest parsedUpdateRequest = new UpdateRequest();

        XContentType entityContentType = XContentType.fromMediaTypeOrFormat(entity.getContentType().getValue());
        try (XContentParser parser = createParser(entityContentType.xContent(), entity.getContent())) {
            parsedUpdateRequest.fromXContent(parser);
        }

        assertEquals(updateRequest.scriptedUpsert(), parsedUpdateRequest.scriptedUpsert());
        assertEquals(updateRequest.docAsUpsert(), parsedUpdateRequest.docAsUpsert());
        assertEquals(updateRequest.detectNoop(), parsedUpdateRequest.detectNoop());
        assertEquals(updateRequest.fetchSource(), parsedUpdateRequest.fetchSource());
        assertEquals(updateRequest.script(), parsedUpdateRequest.script());
        if (updateRequest.doc() != null) {
            assertToXContentEquivalent(updateRequest.doc().source(), parsedUpdateRequest.doc().source(), xContentType);
        } else {
            assertNull(parsedUpdateRequest.doc());
        }
        if (updateRequest.upsertRequest() != null) {
            assertToXContentEquivalent(updateRequest.upsertRequest().source(), parsedUpdateRequest.upsertRequest().source(), xContentType);
        } else {
            assertNull(parsedUpdateRequest.upsertRequest());
        }
    }

    public void testUpdateWithDifferentContentTypes() {
        IllegalStateException exception = expectThrows(IllegalStateException.class, () -> {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.doc(new IndexRequest().source(singletonMap("field", "doc"), XContentType.JSON));
            updateRequest.upsert(new IndexRequest().source(singletonMap("field", "upsert"), XContentType.YAML));
            RequestConverters.update(updateRequest);
        });
        assertEquals("Update request cannot have different content types for doc [JSON] and upsert [YAML] documents",
                exception.getMessage());
    }

    public void testBulk() throws IOException {
        Map<String, String> expectedParams = new HashMap<>();

        BulkRequest bulkRequest = new BulkRequest();
        if (randomBoolean()) {
            String timeout = randomTimeValue();
            bulkRequest.timeout(timeout);
            expectedParams.put("timeout", timeout);
        } else {
            expectedParams.put("timeout", BulkShardRequest.DEFAULT_TIMEOUT.getStringRep());
        }

        setRandomRefreshPolicy(bulkRequest::setRefreshPolicy, expectedParams);

        XContentType xContentType = randomFrom(XContentType.JSON, XContentType.SMILE);

        int nbItems = randomIntBetween(10, 100);
        for (int i = 0; i < nbItems; i++) {
            String index = randomAlphaOfLength(5);
            String type = randomAlphaOfLength(5);
            String id = randomAlphaOfLength(5);

            BytesReference source = RandomObjects.randomSource(random(), xContentType);
            DocWriteRequest.OpType opType = randomFrom(DocWriteRequest.OpType.values());

            DocWriteRequest<?> docWriteRequest;
            if (opType == DocWriteRequest.OpType.INDEX) {
                IndexRequest indexRequest = new IndexRequest(index, type, id).source(source, xContentType);
                docWriteRequest = indexRequest;
                if (randomBoolean()) {
                    indexRequest.setPipeline(randomAlphaOfLength(5));
                }
                if (randomBoolean()) {
                    indexRequest.parent(randomAlphaOfLength(5));
                }
            } else if (opType == DocWriteRequest.OpType.CREATE) {
                IndexRequest createRequest = new IndexRequest(index, type, id).source(source, xContentType).create(true);
                docWriteRequest = createRequest;
                if (randomBoolean()) {
                    createRequest.parent(randomAlphaOfLength(5));
                }
            } else if (opType == DocWriteRequest.OpType.UPDATE) {
                final UpdateRequest updateRequest = new UpdateRequest(index, type, id).doc(new IndexRequest().source(source, xContentType));
                docWriteRequest = updateRequest;
                if (randomBoolean()) {
                    updateRequest.retryOnConflict(randomIntBetween(1, 5));
                }
                if (randomBoolean()) {
                    randomizeFetchSourceContextParams(updateRequest::fetchSource, new HashMap<>());
                }
                if (randomBoolean()) {
                    updateRequest.parent(randomAlphaOfLength(5));
                }
            } else if (opType == DocWriteRequest.OpType.DELETE) {
                docWriteRequest = new DeleteRequest(index, type, id);
            } else {
                throw new UnsupportedOperationException("optype [" + opType + "] not supported");
            }

            if (randomBoolean()) {
                docWriteRequest.routing(randomAlphaOfLength(10));
            }
            if (randomBoolean()) {
                docWriteRequest.version(randomNonNegativeLong());
            }
            if (randomBoolean()) {
                docWriteRequest.versionType(randomFrom(VersionType.values()));
            }
            bulkRequest.add(docWriteRequest);
        }

        Request request = RequestConverters.bulk(bulkRequest);
        assertEquals("/_bulk", request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertEquals(HttpPost.METHOD_NAME, request.getMethod());
        assertEquals(xContentType.mediaTypeWithoutParameters(), request.getEntity().getContentType().getValue());
        byte[] content = new byte[(int) request.getEntity().getContentLength()];
        try (InputStream inputStream = request.getEntity().getContent()) {
            Streams.readFully(inputStream, content);
        }

        BulkRequest parsedBulkRequest = new BulkRequest();
        parsedBulkRequest.add(content, 0, content.length, xContentType);
        assertEquals(bulkRequest.numberOfActions(), parsedBulkRequest.numberOfActions());

        for (int i = 0; i < bulkRequest.numberOfActions(); i++) {
            DocWriteRequest<?> originalRequest = bulkRequest.requests().get(i);
            DocWriteRequest<?> parsedRequest = parsedBulkRequest.requests().get(i);

            assertEquals(originalRequest.opType(), parsedRequest.opType());
            assertEquals(originalRequest.index(), parsedRequest.index());
            assertEquals(originalRequest.type(), parsedRequest.type());
            assertEquals(originalRequest.id(), parsedRequest.id());
            assertEquals(originalRequest.routing(), parsedRequest.routing());
            assertEquals(originalRequest.parent(), parsedRequest.parent());
            assertEquals(originalRequest.version(), parsedRequest.version());
            assertEquals(originalRequest.versionType(), parsedRequest.versionType());

            DocWriteRequest.OpType opType = originalRequest.opType();
            if (opType == DocWriteRequest.OpType.INDEX) {
                IndexRequest indexRequest = (IndexRequest) originalRequest;
                IndexRequest parsedIndexRequest = (IndexRequest) parsedRequest;

                assertEquals(indexRequest.getPipeline(), parsedIndexRequest.getPipeline());
                assertToXContentEquivalent(indexRequest.source(), parsedIndexRequest.source(), xContentType);
            } else if (opType == DocWriteRequest.OpType.UPDATE) {
                UpdateRequest updateRequest = (UpdateRequest) originalRequest;
                UpdateRequest parsedUpdateRequest = (UpdateRequest) parsedRequest;

                assertEquals(updateRequest.retryOnConflict(), parsedUpdateRequest.retryOnConflict());
                assertEquals(updateRequest.fetchSource(), parsedUpdateRequest.fetchSource());
                if (updateRequest.doc() != null) {
                    assertToXContentEquivalent(updateRequest.doc().source(), parsedUpdateRequest.doc().source(), xContentType);
                } else {
                    assertNull(parsedUpdateRequest.doc());
                }
            }
        }
    }

    public void testBulkWithDifferentContentTypes() throws IOException {
        {
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.add(new DeleteRequest("index", "type", "0"));
            bulkRequest.add(new UpdateRequest("index", "type", "1").script(mockScript("test")));
            bulkRequest.add(new DeleteRequest("index", "type", "2"));

            Request request = RequestConverters.bulk(bulkRequest);
            assertEquals(XContentType.JSON.mediaTypeWithoutParameters(), request.getEntity().getContentType().getValue());
        }
        {
            XContentType xContentType = randomFrom(XContentType.JSON, XContentType.SMILE);
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.add(new DeleteRequest("index", "type", "0"));
            bulkRequest.add(new IndexRequest("index", "type", "0").source(singletonMap("field", "value"), xContentType));
            bulkRequest.add(new DeleteRequest("index", "type", "2"));

            Request request = RequestConverters.bulk(bulkRequest);
            assertEquals(xContentType.mediaTypeWithoutParameters(), request.getEntity().getContentType().getValue());
        }
        {
            XContentType xContentType = randomFrom(XContentType.JSON, XContentType.SMILE);
            UpdateRequest updateRequest = new UpdateRequest("index", "type", "0");
            if (randomBoolean()) {
                updateRequest.doc(new IndexRequest().source(singletonMap("field", "value"), xContentType));
            } else {
                updateRequest.upsert(new IndexRequest().source(singletonMap("field", "value"), xContentType));
            }

            Request request = RequestConverters.bulk(new BulkRequest().add(updateRequest));
            assertEquals(xContentType.mediaTypeWithoutParameters(), request.getEntity().getContentType().getValue());
        }
        {
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.add(new IndexRequest("index", "type", "0").source(singletonMap("field", "value"), XContentType.SMILE));
            bulkRequest.add(new IndexRequest("index", "type", "1").source(singletonMap("field", "value"), XContentType.JSON));
            IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> RequestConverters.bulk(bulkRequest));
            assertEquals(
                    "Mismatching content-type found for request with content-type [JSON], " + "previous requests have content-type [SMILE]",
                    exception.getMessage());
        }
        {
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.add(new IndexRequest("index", "type", "0").source(singletonMap("field", "value"), XContentType.JSON));
            bulkRequest.add(new IndexRequest("index", "type", "1").source(singletonMap("field", "value"), XContentType.JSON));
            bulkRequest.add(new UpdateRequest("index", "type", "2")
                    .doc(new IndexRequest().source(singletonMap("field", "value"), XContentType.JSON))
                    .upsert(new IndexRequest().source(singletonMap("field", "value"), XContentType.SMILE)));
            IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> RequestConverters.bulk(bulkRequest));
            assertEquals(
                    "Mismatching content-type found for request with content-type [SMILE], " + "previous requests have content-type [JSON]",
                    exception.getMessage());
        }
        {
            XContentType xContentType = randomFrom(XContentType.CBOR, XContentType.YAML);
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.add(new DeleteRequest("index", "type", "0"));
            bulkRequest.add(new IndexRequest("index", "type", "1").source(singletonMap("field", "value"), XContentType.JSON));
            bulkRequest.add(new DeleteRequest("index", "type", "2"));
            bulkRequest.add(new DeleteRequest("index", "type", "3"));
            bulkRequest.add(new IndexRequest("index", "type", "4").source(singletonMap("field", "value"), XContentType.JSON));
            bulkRequest.add(new IndexRequest("index", "type", "1").source(singletonMap("field", "value"), xContentType));
            IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> RequestConverters.bulk(bulkRequest));
            assertEquals("Unsupported content-type found for request with content-type [" + xContentType
                    + "], only JSON and SMILE are supported", exception.getMessage());
        }
    }

    public void testSearchNullSource() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        Request request = RequestConverters.search(searchRequest);
        assertEquals(HttpPost.METHOD_NAME, request.getMethod());
        assertEquals("/_search", request.getEndpoint());
        assertNull(request.getEntity());
    }

    public void testSearch() throws Exception {
        String[] indices = randomIndicesNames(0, 5);
        SearchRequest searchRequest = new SearchRequest(indices);

        int numTypes = randomIntBetween(0, 5);
        String[] types = new String[numTypes];
        for (int i = 0; i < numTypes; i++) {
            types[i] = "type-" + randomAlphaOfLengthBetween(2, 5);
        }
        searchRequest.types(types);

        Map<String, String> expectedParams = new HashMap<>();
        setRandomSearchParams(searchRequest, expectedParams);
        setRandomIndicesOptions(searchRequest::indicesOptions, searchRequest::indicesOptions, expectedParams);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // rarely skip setting the search source completely
        if (frequently()) {
            // frequently set the search source to have some content, otherwise leave it
            // empty but still set it
            if (frequently()) {
                if (randomBoolean()) {
                    searchSourceBuilder.size(randomIntBetween(0, Integer.MAX_VALUE));
                }
                if (randomBoolean()) {
                    searchSourceBuilder.from(randomIntBetween(0, Integer.MAX_VALUE));
                }
                if (randomBoolean()) {
                    searchSourceBuilder.minScore(randomFloat());
                }
                if (randomBoolean()) {
                    searchSourceBuilder.explain(randomBoolean());
                }
                if (randomBoolean()) {
                    searchSourceBuilder.profile(randomBoolean());
                }
                if (randomBoolean()) {
                    searchSourceBuilder.highlighter(new HighlightBuilder().field(randomAlphaOfLengthBetween(3, 10)));
                }
                if (randomBoolean()) {
                    searchSourceBuilder.query(new TermQueryBuilder(randomAlphaOfLengthBetween(3, 10), randomAlphaOfLengthBetween(3, 10)));
                }
                if (randomBoolean()) {
                    searchSourceBuilder.aggregation(new TermsAggregationBuilder(randomAlphaOfLengthBetween(3, 10), ValueType.STRING)
                            .field(randomAlphaOfLengthBetween(3, 10)));
                }
                if (randomBoolean()) {
                    searchSourceBuilder.suggest(new SuggestBuilder().addSuggestion(randomAlphaOfLengthBetween(3, 10),
                            new CompletionSuggestionBuilder(randomAlphaOfLengthBetween(3, 10))));
                }
                if (randomBoolean()) {
                    searchSourceBuilder.addRescorer(new QueryRescorerBuilder(
                            new TermQueryBuilder(randomAlphaOfLengthBetween(3, 10), randomAlphaOfLengthBetween(3, 10))));
                }
                if (randomBoolean()) {
                    searchSourceBuilder.collapse(new CollapseBuilder(randomAlphaOfLengthBetween(3, 10)));
                }
            }
            searchRequest.source(searchSourceBuilder);
        }

        Request request = RequestConverters.search(searchRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        String index = String.join(",", indices);
        if (Strings.hasLength(index)) {
            endpoint.add(index);
        }
        String type = String.join(",", types);
        if (Strings.hasLength(type)) {
            endpoint.add(type);
        }
        endpoint.add("_search");
        assertEquals(HttpPost.METHOD_NAME, request.getMethod());
        assertEquals(endpoint.toString(), request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertToXContentBody(searchSourceBuilder, request.getEntity());
    }

    public void testSearchNullIndicesAndTypes() {
        expectThrows(NullPointerException.class, () -> new SearchRequest((String[]) null));
        expectThrows(NullPointerException.class, () -> new SearchRequest().indices((String[]) null));
        expectThrows(NullPointerException.class, () -> new SearchRequest().types((String[]) null));
    }

    public void testMultiSearch() throws IOException {
        int numberOfSearchRequests = randomIntBetween(0, 32);
        MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
        for (int i = 0; i < numberOfSearchRequests; i++) {
            SearchRequest searchRequest = randomSearchRequest(() -> {
                // No need to return a very complex SearchSourceBuilder here, that is tested
                // elsewhere
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.from(randomInt(10));
                searchSourceBuilder.size(randomIntBetween(20, 100));
                return searchSourceBuilder;
            });
            // scroll is not supported in the current msearch api, so unset it:
            searchRequest.scroll((Scroll) null);
            // only expand_wildcards, ignore_unavailable and allow_no_indices can be
            // specified from msearch api, so unset other options:
            IndicesOptions randomlyGenerated = searchRequest.indicesOptions();
            IndicesOptions msearchDefault = new MultiSearchRequest().indicesOptions();
            searchRequest.indicesOptions(IndicesOptions.fromOptions(randomlyGenerated.ignoreUnavailable(),
                    randomlyGenerated.allowNoIndices(), randomlyGenerated.expandWildcardsOpen(), randomlyGenerated.expandWildcardsClosed(),
                    msearchDefault.allowAliasesToMultipleIndices(), msearchDefault.forbidClosedIndices(), msearchDefault.ignoreAliases()));
            multiSearchRequest.add(searchRequest);
        }

        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put(RestSearchAction.TYPED_KEYS_PARAM, "true");
        if (randomBoolean()) {
            multiSearchRequest.maxConcurrentSearchRequests(randomIntBetween(1, 8));
            expectedParams.put("max_concurrent_searches", Integer.toString(multiSearchRequest.maxConcurrentSearchRequests()));
        }

        Request request = RequestConverters.multiSearch(multiSearchRequest);
        assertEquals("/_msearch", request.getEndpoint());
        assertEquals(HttpPost.METHOD_NAME, request.getMethod());
        assertEquals(expectedParams, request.getParameters());

        List<SearchRequest> requests = new ArrayList<>();
        CheckedBiConsumer<SearchRequest, XContentParser, IOException> consumer = (searchRequest, p) -> {
            SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.fromXContent(p, false);
            if (searchSourceBuilder.equals(new SearchSourceBuilder()) == false) {
                searchRequest.source(searchSourceBuilder);
            }
            requests.add(searchRequest);
        };
        MultiSearchRequest.readMultiLineFormat(new BytesArray(EntityUtils.toByteArray(request.getEntity())),
                REQUEST_BODY_CONTENT_TYPE.xContent(), consumer, null, multiSearchRequest.indicesOptions(), null, null, null,
                xContentRegistry(), true);
        assertEquals(requests, multiSearchRequest.requests());
    }

    public void testSearchScroll() throws IOException {
        SearchScrollRequest searchScrollRequest = new SearchScrollRequest();
        searchScrollRequest.scrollId(randomAlphaOfLengthBetween(5, 10));
        if (randomBoolean()) {
            searchScrollRequest.scroll(randomPositiveTimeValue());
        }
        Request request = RequestConverters.searchScroll(searchScrollRequest);
        assertEquals(HttpPost.METHOD_NAME, request.getMethod());
        assertEquals("/_search/scroll", request.getEndpoint());
        assertEquals(0, request.getParameters().size());
        assertToXContentBody(searchScrollRequest, request.getEntity());
        assertEquals(REQUEST_BODY_CONTENT_TYPE.mediaTypeWithoutParameters(), request.getEntity().getContentType().getValue());
    }

    public void testClearScroll() throws IOException {
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        int numScrolls = randomIntBetween(1, 10);
        for (int i = 0; i < numScrolls; i++) {
            clearScrollRequest.addScrollId(randomAlphaOfLengthBetween(5, 10));
        }
        Request request = RequestConverters.clearScroll(clearScrollRequest);
        assertEquals(HttpDelete.METHOD_NAME, request.getMethod());
        assertEquals("/_search/scroll", request.getEndpoint());
        assertEquals(0, request.getParameters().size());
        assertToXContentBody(clearScrollRequest, request.getEntity());
        assertEquals(REQUEST_BODY_CONTENT_TYPE.mediaTypeWithoutParameters(), request.getEntity().getContentType().getValue());
    }

    public void testSearchTemplate() throws Exception {
        // Create a random request.
        String[] indices = randomIndicesNames(0, 5);
        SearchRequest searchRequest = new SearchRequest(indices);

        Map<String, String> expectedParams = new HashMap<>();
        setRandomSearchParams(searchRequest, expectedParams);
        setRandomIndicesOptions(searchRequest::indicesOptions, searchRequest::indicesOptions, expectedParams);

        SearchTemplateRequest searchTemplateRequest = new SearchTemplateRequest(searchRequest);

        searchTemplateRequest.setScript("{\"query\": { \"match\" : { \"{{field}}\" : \"{{value}}\" }}}");
        searchTemplateRequest.setScriptType(ScriptType.INLINE);
        searchTemplateRequest.setProfile(randomBoolean());

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("field", "name");
        scriptParams.put("value", "soren");
        searchTemplateRequest.setScriptParams(scriptParams);

        // Verify that the resulting REST request looks as expected.
        Request request = RequestConverters.searchTemplate(searchTemplateRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        String index = String.join(",", indices);
        if (Strings.hasLength(index)) {
            endpoint.add(index);
        }
        endpoint.add("_search/template");

        assertEquals(HttpGet.METHOD_NAME, request.getMethod());
        assertEquals(endpoint.toString(), request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertToXContentBody(searchTemplateRequest, request.getEntity());
    }

    public void testRenderSearchTemplate() throws Exception {
        // Create a simple request.
        SearchTemplateRequest searchTemplateRequest = new SearchTemplateRequest();
        searchTemplateRequest.setSimulate(true); // Setting simulate true means the template should only be rendered.

        searchTemplateRequest.setScript("template1");
        searchTemplateRequest.setScriptType(ScriptType.STORED);
        searchTemplateRequest.setProfile(randomBoolean());

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("field", "name");
        scriptParams.put("value", "soren");
        searchTemplateRequest.setScriptParams(scriptParams);

        // Verify that the resulting REST request looks as expected.
        Request request = RequestConverters.searchTemplate(searchTemplateRequest);
        String endpoint = "_render/template";

        assertEquals(HttpGet.METHOD_NAME, request.getMethod());
        assertEquals(endpoint, request.getEndpoint());
        assertEquals(Collections.emptyMap(), request.getParameters());
        assertToXContentBody(searchTemplateRequest, request.getEntity());
    }

    public void testMultiSearchTemplate() throws Exception {
        final int numSearchRequests = randomIntBetween(1, 10);
        MultiSearchTemplateRequest multiSearchTemplateRequest = new MultiSearchTemplateRequest();

        for (int i = 0; i < numSearchRequests; i++) {
            // Create a random request.
            String[] indices = randomIndicesNames(0, 5);
            SearchRequest searchRequest = new SearchRequest(indices);

            Map<String, String> expectedParams = new HashMap<>();
            setRandomSearchParams(searchRequest, expectedParams);

            // scroll is not supported in the current msearch or msearchtemplate api, so unset it:
            searchRequest.scroll((Scroll) null);
            // batched reduce size is currently not set-able on a per-request basis as it is a query string parameter only
            searchRequest.setBatchedReduceSize(SearchRequest.DEFAULT_BATCHED_REDUCE_SIZE);

            setRandomIndicesOptions(searchRequest::indicesOptions, searchRequest::indicesOptions, expectedParams);

            SearchTemplateRequest searchTemplateRequest = new SearchTemplateRequest(searchRequest);

            searchTemplateRequest.setScript("{\"query\": { \"match\" : { \"{{field}}\" : \"{{value}}\" }}}");
            searchTemplateRequest.setScriptType(ScriptType.INLINE);
            searchTemplateRequest.setProfile(randomBoolean());

            Map<String, Object> scriptParams = new HashMap<>();
            scriptParams.put("field", "name");
            scriptParams.put("value", randomAlphaOfLengthBetween(2, 5));
            searchTemplateRequest.setScriptParams(scriptParams);

            multiSearchTemplateRequest.add(searchTemplateRequest);
        }

        Request multiRequest = RequestConverters.multiSearchTemplate(multiSearchTemplateRequest);

        assertEquals(HttpPost.METHOD_NAME, multiRequest.getMethod());
        assertEquals("/_msearch/template", multiRequest.getEndpoint());
        List<SearchTemplateRequest> searchRequests = multiSearchTemplateRequest.requests();
        assertEquals(numSearchRequests, searchRequests.size());

        HttpEntity actualEntity = multiRequest.getEntity();
        byte[] expectedBytes = MultiSearchTemplateRequest.writeMultiLineFormat(multiSearchTemplateRequest, XContentType.JSON.xContent());
        assertEquals(XContentType.JSON.mediaTypeWithoutParameters(), actualEntity.getContentType().getValue());
        assertEquals(new BytesArray(expectedBytes), new BytesArray(EntityUtils.toByteArray(actualEntity)));
    }

    public void testExistsAlias() {
        GetAliasesRequest getAliasesRequest = new GetAliasesRequest();
        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 5);
        getAliasesRequest.indices(indices);
        // the HEAD endpoint requires at least an alias or an index
        boolean hasIndices = indices != null && indices.length > 0;
        String[] aliases;
        if (hasIndices) {
            aliases = randomBoolean() ? null : randomIndicesNames(0, 5);
        } else {
            aliases = randomIndicesNames(1, 5);
        }
        getAliasesRequest.aliases(aliases);
        Map<String, String> expectedParams = new HashMap<>();
        setRandomLocal(getAliasesRequest, expectedParams);
        setRandomIndicesOptions(getAliasesRequest::indicesOptions, getAliasesRequest::indicesOptions, expectedParams);

        Request request = RequestConverters.existsAlias(getAliasesRequest);
        StringJoiner expectedEndpoint = new StringJoiner("/", "/", "");
        if (indices != null && indices.length > 0) {
            expectedEndpoint.add(String.join(",", indices));
        }
        expectedEndpoint.add("_alias");
        if (aliases != null && aliases.length > 0) {
            expectedEndpoint.add(String.join(",", aliases));
        }
        assertEquals(HttpHead.METHOD_NAME, request.getMethod());
        assertEquals(expectedEndpoint.toString(), request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertNull(request.getEntity());
    }

    public void testExistsAliasNoAliasNoIndex() {
        {
            GetAliasesRequest getAliasesRequest = new GetAliasesRequest();
            IllegalArgumentException iae = expectThrows(IllegalArgumentException.class,
                    () -> RequestConverters.existsAlias(getAliasesRequest));
            assertEquals("existsAlias requires at least an alias or an index", iae.getMessage());
        }
        {
            GetAliasesRequest getAliasesRequest = new GetAliasesRequest((String[]) null);
            getAliasesRequest.indices((String[]) null);
            IllegalArgumentException iae = expectThrows(IllegalArgumentException.class,
                    () -> RequestConverters.existsAlias(getAliasesRequest));
            assertEquals("existsAlias requires at least an alias or an index", iae.getMessage());
        }
    }

    public void testExplain() throws IOException {
        String index = randomAlphaOfLengthBetween(3, 10);
        String type = randomAlphaOfLengthBetween(3, 10);
        String id = randomAlphaOfLengthBetween(3, 10);

        ExplainRequest explainRequest = new ExplainRequest(index, type, id);
        explainRequest.query(QueryBuilders.termQuery(randomAlphaOfLengthBetween(3, 10), randomAlphaOfLengthBetween(3, 10)));

        Map<String, String> expectedParams = new HashMap<>();

        if (randomBoolean()) {
            String routing = randomAlphaOfLengthBetween(3, 10);
            explainRequest.routing(routing);
            expectedParams.put("routing", routing);
        }
        if (randomBoolean()) {
            String preference = randomAlphaOfLengthBetween(3, 10);
            explainRequest.preference(preference);
            expectedParams.put("preference", preference);
        }
        if (randomBoolean()) {
            String[] storedFields = generateRandomStringArray(10, 5, false, false);
            String storedFieldsParams = randomFields(storedFields);
            explainRequest.storedFields(storedFields);
            expectedParams.put("stored_fields", storedFieldsParams);
        }
        if (randomBoolean()) {
            randomizeFetchSourceContextParams(explainRequest::fetchSourceContext, expectedParams);
        }

        Request request = RequestConverters.explain(explainRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        endpoint.add(index)
            .add(type)
            .add(id)
            .add("_explain");

        assertEquals(HttpGet.METHOD_NAME, request.getMethod());
        assertEquals(endpoint.toString(), request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertToXContentBody(explainRequest, request.getEntity());
    }

    public void testFieldCaps() {
        // Create a random request.
        String[] indices = randomIndicesNames(0, 5);
        String[] fields = generateRandomStringArray(5, 10, false, false);

        FieldCapabilitiesRequest fieldCapabilitiesRequest = new FieldCapabilitiesRequest()
            .indices(indices)
            .fields(fields);

        Map<String, String> indicesOptionsParams = new HashMap<>();
        setRandomIndicesOptions(fieldCapabilitiesRequest::indicesOptions,
            fieldCapabilitiesRequest::indicesOptions,
            indicesOptionsParams);

        Request request = RequestConverters.fieldCaps(fieldCapabilitiesRequest);

        // Verify that the resulting REST request looks as expected.
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        String joinedIndices = String.join(",", indices);
        if (!joinedIndices.isEmpty()) {
            endpoint.add(joinedIndices);
        }
        endpoint.add("_field_caps");

        assertEquals(endpoint.toString(), request.getEndpoint());
        assertEquals(4, request.getParameters().size());

        // Note that we don't check the field param value explicitly, as field names are passed through
        // a hash set before being added to the request, and can appear in a non-deterministic order.
        assertThat(request.getParameters(), hasKey("fields"));
        String[] requestFields = Strings.splitStringByCommaToArray(request.getParameters().get("fields"));
        assertEquals(new HashSet<>(Arrays.asList(fields)),
            new HashSet<>(Arrays.asList(requestFields)));

        for (Map.Entry<String, String> param : indicesOptionsParams.entrySet()) {
            assertThat(request.getParameters(), hasEntry(param.getKey(), param.getValue()));
        }

        assertNull(request.getEntity());
    }

    public void testRankEval() throws Exception {
        RankEvalSpec spec = new RankEvalSpec(
                Collections.singletonList(new RatedRequest("queryId", Collections.emptyList(), new SearchSourceBuilder())),
                new PrecisionAtK());
        String[] indices = randomIndicesNames(0, 5);
        RankEvalRequest rankEvalRequest = new RankEvalRequest(spec, indices);
        Map<String, String> expectedParams = new HashMap<>();
        setRandomIndicesOptions(rankEvalRequest::indicesOptions, rankEvalRequest::indicesOptions, expectedParams);

        Request request = RequestConverters.rankEval(rankEvalRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        String index = String.join(",", indices);
        if (Strings.hasLength(index)) {
            endpoint.add(index);
        }
        endpoint.add(RestRankEvalAction.ENDPOINT);
        assertEquals(endpoint.toString(), request.getEndpoint());
        assertEquals(3, request.getParameters().size());
        assertEquals(expectedParams, request.getParameters());
        assertToXContentBody(spec, request.getEntity());
    }

    public void testSplit() throws IOException {
        resizeTest(ResizeType.SPLIT, RequestConverters::split);
    }

    public void testSplitWrongResizeType() {
        ResizeRequest resizeRequest = new ResizeRequest("target", "source");
        resizeRequest.setResizeType(ResizeType.SHRINK);
        IllegalArgumentException iae = expectThrows(IllegalArgumentException.class, () -> RequestConverters.split(resizeRequest));
        assertEquals("Wrong resize type [SHRINK] for indices split request", iae.getMessage());
    }

    public void testShrinkWrongResizeType() {
        ResizeRequest resizeRequest = new ResizeRequest("target", "source");
        resizeRequest.setResizeType(ResizeType.SPLIT);
        IllegalArgumentException iae = expectThrows(IllegalArgumentException.class, () -> RequestConverters.shrink(resizeRequest));
        assertEquals("Wrong resize type [SPLIT] for indices shrink request", iae.getMessage());
    }

    public void testShrink() throws IOException {
        resizeTest(ResizeType.SHRINK, RequestConverters::shrink);
    }

    private static void resizeTest(ResizeType resizeType, CheckedFunction<ResizeRequest, Request, IOException> function)
            throws IOException {
        String[] indices = randomIndicesNames(2, 2);
        ResizeRequest resizeRequest = new ResizeRequest(indices[0], indices[1]);
        resizeRequest.setResizeType(resizeType);
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(resizeRequest, expectedParams);
        setRandomTimeout(resizeRequest::timeout, resizeRequest.timeout(), expectedParams);

        if (randomBoolean()) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(randomAlphaOfLengthBetween(3, 10));
            if (randomBoolean()) {
                createIndexRequest.settings(randomIndexSettings());
            }
            if (randomBoolean()) {
                randomAliases(createIndexRequest);
            }
            resizeRequest.setTargetIndex(createIndexRequest);
        }
        setRandomWaitForActiveShards(resizeRequest::setWaitForActiveShards, ActiveShardCount.DEFAULT, expectedParams);

        Request request = function.apply(resizeRequest);
        assertEquals(HttpPut.METHOD_NAME, request.getMethod());
        String expectedEndpoint = "/" + resizeRequest.getSourceIndex() + "/_" + resizeType.name().toLowerCase(Locale.ROOT) + "/"
                + resizeRequest.getTargetIndexRequest().index();
        assertEquals(expectedEndpoint, request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertToXContentBody(resizeRequest, request.getEntity());
    }

    public void testClusterPutSettings() throws IOException {
        ClusterUpdateSettingsRequest request = new ClusterUpdateSettingsRequest();
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(request, expectedParams);
        setRandomTimeout(request::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request expectedRequest = RequestConverters.clusterPutSettings(request);
        assertEquals("/_cluster/settings", expectedRequest.getEndpoint());
        assertEquals(HttpPut.METHOD_NAME, expectedRequest.getMethod());
        assertEquals(expectedParams, expectedRequest.getParameters());
    }

    public void testClusterGetSettings() throws IOException {
        ClusterGetSettingsRequest request = new ClusterGetSettingsRequest();
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(request, expectedParams);
        request.includeDefaults(randomBoolean());
        if (request.includeDefaults()) {
            expectedParams.put("include_defaults", String.valueOf(true));
        }

        Request expectedRequest = RequestConverters.clusterGetSettings(request);
        assertEquals("/_cluster/settings", expectedRequest.getEndpoint());
        assertEquals(HttpGet.METHOD_NAME, expectedRequest.getMethod());
        assertEquals(expectedParams, expectedRequest.getParameters());
    }

    public void testPutPipeline() throws IOException {
        String pipelineId = "some_pipeline_id";
        PutPipelineRequest request = new PutPipelineRequest(
            "some_pipeline_id",
            new BytesArray("{}".getBytes(StandardCharsets.UTF_8)),
            XContentType.JSON
        );
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(request, expectedParams);
        setRandomTimeout(request::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request expectedRequest = RequestConverters.putPipeline(request);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        endpoint.add("_ingest/pipeline");
        endpoint.add(pipelineId);
        assertEquals(endpoint.toString(), expectedRequest.getEndpoint());
        assertEquals(HttpPut.METHOD_NAME, expectedRequest.getMethod());
        assertEquals(expectedParams, expectedRequest.getParameters());
    }

    public void testGetPipeline() {
        String pipelineId = "some_pipeline_id";
        Map<String, String> expectedParams = new HashMap<>();
        GetPipelineRequest request = new GetPipelineRequest("some_pipeline_id");
        setRandomMasterTimeout(request, expectedParams);
        Request expectedRequest = RequestConverters.getPipeline(request);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        endpoint.add("_ingest/pipeline");
        endpoint.add(pipelineId);
        assertEquals(endpoint.toString(), expectedRequest.getEndpoint());
        assertEquals(HttpGet.METHOD_NAME, expectedRequest.getMethod());
        assertEquals(expectedParams, expectedRequest.getParameters());
    }

    public void testDeletePipeline() {
        String pipelineId = "some_pipeline_id";
        Map<String, String> expectedParams = new HashMap<>();
        DeletePipelineRequest request = new DeletePipelineRequest(pipelineId);
        setRandomMasterTimeout(request, expectedParams);
        setRandomTimeout(request::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        Request expectedRequest = RequestConverters.deletePipeline(request);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        endpoint.add("_ingest/pipeline");
        endpoint.add(pipelineId);
        assertEquals(endpoint.toString(), expectedRequest.getEndpoint());
        assertEquals(HttpDelete.METHOD_NAME, expectedRequest.getMethod());
        assertEquals(expectedParams, expectedRequest.getParameters());
    }

    public void testSimulatePipeline() throws IOException {
        String pipelineId = randomBoolean() ? "some_pipeline_id" : null;
        boolean verbose = randomBoolean();
        String json = "{\"pipeline\":{" +
            "\"description\":\"_description\"," +
            "\"processors\":[{\"set\":{\"field\":\"field2\",\"value\":\"_value\"}}]}," +
            "\"docs\":[{\"_index\":\"index\",\"_type\":\"_doc\",\"_id\":\"id\",\"_source\":{\"foo\":\"rab\"}}]}";
        SimulatePipelineRequest request = new SimulatePipelineRequest(
            new BytesArray(json.getBytes(StandardCharsets.UTF_8)),
            XContentType.JSON
        );
        request.setId(pipelineId);
        request.setVerbose(verbose);
        Map<String, String> expectedParams = new HashMap<>();
        expectedParams.put("verbose", Boolean.toString(verbose));

        Request expectedRequest = RequestConverters.simulatePipeline(request);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        endpoint.add("_ingest/pipeline");
        if (pipelineId != null && !pipelineId.isEmpty())
            endpoint.add(pipelineId);
        endpoint.add("_simulate");
        assertEquals(endpoint.toString(), expectedRequest.getEndpoint());
        assertEquals(HttpPost.METHOD_NAME, expectedRequest.getMethod());
        assertEquals(expectedParams, expectedRequest.getParameters());
        assertToXContentBody(request, expectedRequest.getEntity());
    }

    public void testClusterHealth() {
        ClusterHealthRequest healthRequest = new ClusterHealthRequest();
        Map<String, String> expectedParams = new HashMap<>();
        setRandomLocal(healthRequest, expectedParams);
        String timeoutType = randomFrom("timeout", "masterTimeout", "both", "none");
        String timeout = randomTimeValue();
        String masterTimeout = randomTimeValue();
        switch (timeoutType) {
            case "timeout":
                healthRequest.timeout(timeout);
                expectedParams.put("timeout", timeout);
                // If Master Timeout wasn't set it uses the same value as Timeout
                expectedParams.put("master_timeout", timeout);
                break;
            case "masterTimeout":
                expectedParams.put("timeout", "30s");
                healthRequest.masterNodeTimeout(masterTimeout);
                expectedParams.put("master_timeout", masterTimeout);
                break;
            case "both":
                healthRequest.timeout(timeout);
                expectedParams.put("timeout", timeout);
                healthRequest.masterNodeTimeout(timeout);
                expectedParams.put("master_timeout", timeout);
                break;
            case "none":
                expectedParams.put("timeout", "30s");
                expectedParams.put("master_timeout", "30s");
                break;
            default:
                throw new UnsupportedOperationException();
        }
        setRandomWaitForActiveShards(healthRequest::waitForActiveShards, ActiveShardCount.NONE, expectedParams);
        if (randomBoolean()) {
            ClusterHealthRequest.Level level = randomFrom(ClusterHealthRequest.Level.values());
            healthRequest.level(level);
            expectedParams.put("level", level.name().toLowerCase(Locale.ROOT));
        } else {
            expectedParams.put("level", "shards");
        }
        if (randomBoolean()) {
            Priority priority = randomFrom(Priority.values());
            healthRequest.waitForEvents(priority);
            expectedParams.put("wait_for_events", priority.name().toLowerCase(Locale.ROOT));
        }
        if (randomBoolean()) {
            ClusterHealthStatus status = randomFrom(ClusterHealthStatus.values());
            healthRequest.waitForStatus(status);
            expectedParams.put("wait_for_status", status.name().toLowerCase(Locale.ROOT));
        }
        if (randomBoolean()) {
            boolean waitForNoInitializingShards = randomBoolean();
            healthRequest.waitForNoInitializingShards(waitForNoInitializingShards);
            if (waitForNoInitializingShards) {
                expectedParams.put("wait_for_no_initializing_shards", Boolean.TRUE.toString());
            }
        }
        if (randomBoolean()) {
            boolean waitForNoRelocatingShards = randomBoolean();
            healthRequest.waitForNoRelocatingShards(waitForNoRelocatingShards);
            if (waitForNoRelocatingShards) {
                expectedParams.put("wait_for_no_relocating_shards", Boolean.TRUE.toString());
            }
        }
        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 5);
        healthRequest.indices(indices);

        Request request = RequestConverters.clusterHealth(healthRequest);
        assertThat(request, CoreMatchers.notNullValue());
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request.getEntity(), nullValue());
        if (indices != null && indices.length > 0) {
            assertThat(request.getEndpoint(), equalTo("/_cluster/health/" + String.join(",", indices)));
        } else {
            assertThat(request.getEndpoint(), equalTo("/_cluster/health"));
        }
        assertThat(request.getParameters(), equalTo(expectedParams));
    }

    public void testRollover() throws IOException {
        RolloverRequest rolloverRequest = new RolloverRequest(randomAlphaOfLengthBetween(3, 10),
                randomBoolean() ? null : randomAlphaOfLengthBetween(3, 10));
        Map<String, String> expectedParams = new HashMap<>();
        setRandomTimeout(rolloverRequest::timeout, rolloverRequest.timeout(), expectedParams);
        setRandomMasterTimeout(rolloverRequest, expectedParams);
        if (randomBoolean()) {
            rolloverRequest.dryRun(randomBoolean());
            if (rolloverRequest.isDryRun()) {
                expectedParams.put("dry_run", "true");
            }
        }
        if (randomBoolean()) {
            rolloverRequest.addMaxIndexAgeCondition(new TimeValue(randomNonNegativeLong()));
        }
        if (randomBoolean()) {
            String type = randomAlphaOfLengthBetween(3, 10);
            rolloverRequest.getCreateIndexRequest().mapping(type, RandomCreateIndexGenerator.randomMapping(type));
        }
        if (randomBoolean()) {
            RandomCreateIndexGenerator.randomAliases(rolloverRequest.getCreateIndexRequest());
        }
        if (randomBoolean()) {
            rolloverRequest.getCreateIndexRequest().settings(RandomCreateIndexGenerator.randomIndexSettings());
        }
        setRandomWaitForActiveShards(rolloverRequest.getCreateIndexRequest()::waitForActiveShards,
                ActiveShardCount.DEFAULT, expectedParams);

        Request request = RequestConverters.rollover(rolloverRequest);
        if (rolloverRequest.getNewIndexName() == null) {
            assertEquals("/" + rolloverRequest.getAlias() + "/_rollover", request.getEndpoint());
        } else {
            assertEquals("/" + rolloverRequest.getAlias() + "/_rollover/" + rolloverRequest.getNewIndexName(), request.getEndpoint());
        }
        assertEquals(HttpPost.METHOD_NAME, request.getMethod());
        assertToXContentBody(rolloverRequest, request.getEntity());
        assertEquals(expectedParams, request.getParameters());
    }

    public void testGetAlias() {
        GetAliasesRequest getAliasesRequest = new GetAliasesRequest();

        Map<String, String> expectedParams = new HashMap<>();
        setRandomLocal(getAliasesRequest, expectedParams);
        setRandomIndicesOptions(getAliasesRequest::indicesOptions, getAliasesRequest::indicesOptions, expectedParams);

        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 2);
        String[] aliases = randomBoolean() ? null : randomIndicesNames(0, 2);
        getAliasesRequest.indices(indices);
        getAliasesRequest.aliases(aliases);

        Request request = RequestConverters.getAlias(getAliasesRequest);
        StringJoiner expectedEndpoint = new StringJoiner("/", "/", "");

        if (false == CollectionUtils.isEmpty(indices)) {
            expectedEndpoint.add(String.join(",", indices));
        }
        expectedEndpoint.add("_alias");

        if (false == CollectionUtils.isEmpty(aliases)) {
            expectedEndpoint.add(String.join(",", aliases));
        }

        assertEquals(HttpGet.METHOD_NAME, request.getMethod());
        assertEquals(expectedEndpoint.toString(), request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertNull(request.getEntity());
    }

    public void testIndexPutSettings() throws IOException {
        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 2);
        UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(indices);
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(updateSettingsRequest, expectedParams);
        setRandomTimeout(updateSettingsRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        setRandomIndicesOptions(updateSettingsRequest::indicesOptions, updateSettingsRequest::indicesOptions, expectedParams);
        if (randomBoolean()) {
            updateSettingsRequest.setPreserveExisting(randomBoolean());
            if (updateSettingsRequest.isPreserveExisting()) {
                expectedParams.put("preserve_existing", "true");
            }
        }

        Request request = RequestConverters.indexPutSettings(updateSettingsRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        if (indices != null && indices.length > 0) {
            endpoint.add(String.join(",", indices));
        }
        endpoint.add("_settings");
        assertThat(endpoint.toString(), equalTo(request.getEndpoint()));
        assertEquals(HttpPut.METHOD_NAME, request.getMethod());
        assertToXContentBody(updateSettingsRequest, request.getEntity());
        assertEquals(expectedParams, request.getParameters());
    }

    public void testCancelTasks() {
        CancelTasksRequest request = new CancelTasksRequest();
        Map<String, String> expectedParams = new HashMap<>();
        TaskId taskId = new TaskId(randomAlphaOfLength(5), randomNonNegativeLong());
        TaskId parentTaskId = new TaskId(randomAlphaOfLength(5), randomNonNegativeLong());
        request.setTaskId(taskId);
        request.setParentTaskId(parentTaskId);
        expectedParams.put("task_id", taskId.toString());
        expectedParams.put("parent_task_id", parentTaskId.toString());
        Request httpRequest = RequestConverters.cancelTasks(request);
        assertThat(httpRequest, notNullValue());
        assertThat(httpRequest.getMethod(), equalTo(HttpPost.METHOD_NAME));
        assertThat(httpRequest.getEntity(), nullValue());
        assertThat(httpRequest.getEndpoint(), equalTo("/_tasks/_cancel"));
        assertThat(httpRequest.getParameters(), equalTo(expectedParams));
    }

    public void testListTasks() {
        {
            ListTasksRequest request = new ListTasksRequest();
            Map<String, String> expectedParams = new HashMap<>();
            if (randomBoolean()) {
                request.setDetailed(randomBoolean());
                if (request.getDetailed()) {
                    expectedParams.put("detailed", "true");
                }
            }
            if (randomBoolean()) {
                request.setWaitForCompletion(randomBoolean());
                if (request.getWaitForCompletion()) {
                    expectedParams.put("wait_for_completion", "true");
                }
            }
            if (randomBoolean()) {
                String timeout = randomTimeValue();
                request.setTimeout(timeout);
                expectedParams.put("timeout", timeout);
            }
            if (randomBoolean()) {
                if (randomBoolean()) {
                    TaskId taskId = new TaskId(randomAlphaOfLength(5), randomNonNegativeLong());
                    request.setParentTaskId(taskId);
                    expectedParams.put("parent_task_id", taskId.toString());
                } else {
                    request.setParentTask(TaskId.EMPTY_TASK_ID);
                }
            }
            if (randomBoolean()) {
                String[] nodes = generateRandomStringArray(10, 8, false);
                request.setNodes(nodes);
                if (nodes.length > 0) {
                    expectedParams.put("nodes", String.join(",", nodes));
                }
            }
            if (randomBoolean()) {
                String[] actions = generateRandomStringArray(10, 8, false);
                request.setActions(actions);
                if (actions.length > 0) {
                    expectedParams.put("actions", String.join(",", actions));
                }
            }
            expectedParams.put("group_by", "none");
            Request httpRequest = RequestConverters.listTasks(request);
            assertThat(httpRequest, notNullValue());
            assertThat(httpRequest.getMethod(), equalTo(HttpGet.METHOD_NAME));
            assertThat(httpRequest.getEntity(), nullValue());
            assertThat(httpRequest.getEndpoint(), equalTo("/_tasks"));
            assertThat(httpRequest.getParameters(), equalTo(expectedParams));
        }
        {
            ListTasksRequest request = new ListTasksRequest();
            request.setTaskId(new TaskId(randomAlphaOfLength(5), randomNonNegativeLong()));
            IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> RequestConverters.listTasks(request));
            assertEquals("TaskId cannot be used for list tasks request", exception.getMessage());
        }
    }

    public void testGetRepositories() {
        Map<String, String> expectedParams = new HashMap<>();
        StringBuilder endpoint = new StringBuilder("/_snapshot");

        GetRepositoriesRequest getRepositoriesRequest = new GetRepositoriesRequest();
        setRandomMasterTimeout(getRepositoriesRequest, expectedParams);
        setRandomLocal(getRepositoriesRequest, expectedParams);

        if (randomBoolean()) {
            String[] entries = new String[] { "a", "b", "c" };
            getRepositoriesRequest.repositories(entries);
            endpoint.append("/" + String.join(",", entries));
        }

        Request request = RequestConverters.getRepositories(getRepositoriesRequest);
        assertThat(endpoint.toString(), equalTo(request.getEndpoint()));
        assertThat(HttpGet.METHOD_NAME, equalTo(request.getMethod()));
        assertThat(expectedParams, equalTo(request.getParameters()));
    }

    public void testCreateRepository() throws IOException {
        String repository = randomIndicesNames(1, 1)[0];
        String endpoint = "/_snapshot/" + repository;
        Path repositoryLocation = PathUtils.get(".");
        PutRepositoryRequest putRepositoryRequest = new PutRepositoryRequest(repository);
        putRepositoryRequest.type(FsRepository.TYPE);
        putRepositoryRequest.verify(randomBoolean());

        putRepositoryRequest.settings(
            Settings.builder()
                .put(FsRepository.LOCATION_SETTING.getKey(), repositoryLocation)
                .put(FsRepository.COMPRESS_SETTING.getKey(), randomBoolean())
                .put(FsRepository.CHUNK_SIZE_SETTING.getKey(), randomIntBetween(100, 1000), ByteSizeUnit.BYTES)
                .build());

        Request request = RequestConverters.createRepository(putRepositoryRequest);
        assertThat(endpoint, equalTo(request.getEndpoint()));
        assertThat(HttpPut.METHOD_NAME, equalTo(request.getMethod()));
        assertToXContentBody(putRepositoryRequest, request.getEntity());
    }

    public void testDeleteRepository() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = randomIndicesNames(1, 1)[0];

        StringBuilder endpoint = new StringBuilder("/_snapshot/" + repository);

        DeleteRepositoryRequest deleteRepositoryRequest = new DeleteRepositoryRequest();
        deleteRepositoryRequest.name(repository);
        setRandomMasterTimeout(deleteRepositoryRequest, expectedParams);
        setRandomTimeout(deleteRepositoryRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = RequestConverters.deleteRepository(deleteRepositoryRequest);
        assertThat(endpoint.toString(), equalTo(request.getEndpoint()));
        assertThat(HttpDelete.METHOD_NAME, equalTo(request.getMethod()));
        assertThat(expectedParams, equalTo(request.getParameters()));
        assertNull(request.getEntity());
    }

    public void testVerifyRepository() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = randomIndicesNames(1, 1)[0];
        String endpoint = "/_snapshot/" + repository + "/_verify";

        VerifyRepositoryRequest verifyRepositoryRequest = new VerifyRepositoryRequest(repository);
        setRandomMasterTimeout(verifyRepositoryRequest, expectedParams);
        setRandomTimeout(verifyRepositoryRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);

        Request request = RequestConverters.verifyRepository(verifyRepositoryRequest);
        assertThat(endpoint, equalTo(request.getEndpoint()));
        assertThat(HttpPost.METHOD_NAME, equalTo(request.getMethod()));
        assertThat(expectedParams, equalTo(request.getParameters()));
    }

    public void testCreateSnapshot() throws IOException {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = randomIndicesNames(1, 1)[0];
        String snapshot = "snapshot-" + generateRandomStringArray(1, randomInt(10), false, false)[0];
        String endpoint = "/_snapshot/" + repository + "/" + snapshot;

        CreateSnapshotRequest createSnapshotRequest = new CreateSnapshotRequest(repository, snapshot);
        setRandomMasterTimeout(createSnapshotRequest, expectedParams);
        Boolean waitForCompletion = randomBoolean();
        createSnapshotRequest.waitForCompletion(waitForCompletion);

        if (waitForCompletion) {
            expectedParams.put("wait_for_completion", waitForCompletion.toString());
        }

        Request request = RequestConverters.createSnapshot(createSnapshotRequest);
        assertThat(endpoint, equalTo(request.getEndpoint()));
        assertThat(HttpPut.METHOD_NAME, equalTo(request.getMethod()));
        assertThat(expectedParams, equalTo(request.getParameters()));
        assertToXContentBody(createSnapshotRequest, request.getEntity());
    }

    public void testGetSnapshots() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = randomIndicesNames(1, 1)[0];
        String snapshot1 = "snapshot1-" + randomAlphaOfLengthBetween(2, 5).toLowerCase(Locale.ROOT);
        String snapshot2 = "snapshot2-" + randomAlphaOfLengthBetween(2, 5).toLowerCase(Locale.ROOT);

        String endpoint = String.format(Locale.ROOT, "/_snapshot/%s/%s,%s", repository, snapshot1, snapshot2);

        GetSnapshotsRequest getSnapshotsRequest = new GetSnapshotsRequest();
        getSnapshotsRequest.repository(repository);
        getSnapshotsRequest.snapshots(Arrays.asList(snapshot1, snapshot2).toArray(new String[0]));
        setRandomMasterTimeout(getSnapshotsRequest, expectedParams);

        if (randomBoolean()) {
            boolean ignoreUnavailable = randomBoolean();
            getSnapshotsRequest.ignoreUnavailable(ignoreUnavailable);
            expectedParams.put("ignore_unavailable", Boolean.toString(ignoreUnavailable));
        } else {
            expectedParams.put("ignore_unavailable", Boolean.FALSE.toString());
        }

        if (randomBoolean()) {
            boolean verbose = randomBoolean();
            getSnapshotsRequest.verbose(verbose);
            expectedParams.put("verbose", Boolean.toString(verbose));
        } else {
            expectedParams.put("verbose", Boolean.TRUE.toString());
        }

        Request request = RequestConverters.getSnapshots(getSnapshotsRequest);
        assertThat(endpoint, equalTo(request.getEndpoint()));
        assertThat(HttpGet.METHOD_NAME, equalTo(request.getMethod()));
        assertThat(expectedParams, equalTo(request.getParameters()));
        assertNull(request.getEntity());
    }

    public void testGetAllSnapshots() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = randomIndicesNames(1, 1)[0];

        String endpoint = String.format(Locale.ROOT, "/_snapshot/%s/_all", repository);

        GetSnapshotsRequest getSnapshotsRequest = new GetSnapshotsRequest(repository);
        setRandomMasterTimeout(getSnapshotsRequest, expectedParams);

        boolean ignoreUnavailable = randomBoolean();
        getSnapshotsRequest.ignoreUnavailable(ignoreUnavailable);
        expectedParams.put("ignore_unavailable", Boolean.toString(ignoreUnavailable));

        boolean verbose = randomBoolean();
        getSnapshotsRequest.verbose(verbose);
        expectedParams.put("verbose", Boolean.toString(verbose));

        Request request = RequestConverters.getSnapshots(getSnapshotsRequest);
        assertThat(endpoint, equalTo(request.getEndpoint()));
        assertThat(HttpGet.METHOD_NAME, equalTo(request.getMethod()));
        assertThat(expectedParams, equalTo(request.getParameters()));
        assertNull(request.getEntity());
    }

    public void testSnapshotsStatus() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = randomIndicesNames(1, 1)[0];
        String[] snapshots = randomIndicesNames(1, 5);
        StringBuilder snapshotNames = new StringBuilder(snapshots[0]);
        for (int idx = 1; idx < snapshots.length; idx++) {
            snapshotNames.append(",").append(snapshots[idx]);
        }
        boolean ignoreUnavailable = randomBoolean();
        String endpoint = "/_snapshot/" + repository + "/" + snapshotNames.toString() + "/_status";

        SnapshotsStatusRequest snapshotsStatusRequest = new SnapshotsStatusRequest(repository, snapshots);
        setRandomMasterTimeout(snapshotsStatusRequest, expectedParams);
        snapshotsStatusRequest.ignoreUnavailable(ignoreUnavailable);
        expectedParams.put("ignore_unavailable", Boolean.toString(ignoreUnavailable));

        Request request = RequestConverters.snapshotsStatus(snapshotsStatusRequest);
        assertThat(request.getEndpoint(), equalTo(endpoint));
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), is(nullValue()));
    }

    public void testDeleteSnapshot() {
        Map<String, String> expectedParams = new HashMap<>();
        String repository = randomIndicesNames(1, 1)[0];
        String snapshot = "snapshot-" + randomAlphaOfLengthBetween(2, 5).toLowerCase(Locale.ROOT);

        String endpoint = String.format(Locale.ROOT, "/_snapshot/%s/%s", repository, snapshot);

        DeleteSnapshotRequest deleteSnapshotRequest = new DeleteSnapshotRequest();
        deleteSnapshotRequest.repository(repository);
        deleteSnapshotRequest.snapshot(snapshot);
        setRandomMasterTimeout(deleteSnapshotRequest, expectedParams);

        Request request = RequestConverters.deleteSnapshot(deleteSnapshotRequest);
        assertThat(endpoint, equalTo(request.getEndpoint()));
        assertThat(HttpDelete.METHOD_NAME, equalTo(request.getMethod()));
        assertThat(expectedParams, equalTo(request.getParameters()));
        assertNull(request.getEntity());
    }

    public void testPutTemplateRequest() throws Exception {
        Map<String, String> names = new HashMap<>();
        names.put("log", "log");
        names.put("template#1", "template%231");
        names.put("-#template", "-%23template");
        names.put("foo^bar", "foo%5Ebar");

        PutIndexTemplateRequest putTemplateRequest = new PutIndexTemplateRequest().name(randomFrom(names.keySet()))
                .patterns(Arrays.asList(generateRandomStringArray(20, 100, false, false)));
        if (randomBoolean()) {
            putTemplateRequest.order(randomInt());
        }
        if (randomBoolean()) {
            putTemplateRequest.version(randomInt());
        }
        if (randomBoolean()) {
            putTemplateRequest.settings(Settings.builder().put("setting-" + randomInt(), randomTimeValue()));
        }
        if (randomBoolean()) {
            putTemplateRequest.mapping("doc-" + randomInt(), "field-" + randomInt(), "type=" + randomFrom("text", "keyword"));
        }
        if (randomBoolean()) {
            putTemplateRequest.alias(new Alias("alias-" + randomInt()));
        }
        Map<String, String> expectedParams = new HashMap<>();
        if (randomBoolean()) {
            expectedParams.put("create", Boolean.TRUE.toString());
            putTemplateRequest.create(true);
        }
        if (randomBoolean()) {
            String cause = randomUnicodeOfCodepointLengthBetween(1, 50);
            putTemplateRequest.cause(cause);
            expectedParams.put("cause", cause);
        }
        setRandomMasterTimeout(putTemplateRequest, expectedParams);
        Request request = RequestConverters.putTemplate(putTemplateRequest);
        assertThat(request.getEndpoint(), equalTo("/_template/" + names.get(putTemplateRequest.name())));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertToXContentBody(putTemplateRequest, request.getEntity());
    }

    public void testValidateQuery() throws Exception {
        String[] indices = randomBoolean() ? null : randomIndicesNames(0, 5);
        String[] types = randomBoolean() ? generateRandomStringArray(5, 5, false, false) : null;
        ValidateQueryRequest validateQueryRequest;
        if (randomBoolean()) {
            validateQueryRequest = new ValidateQueryRequest(indices);
        } else {
            validateQueryRequest = new ValidateQueryRequest();
            validateQueryRequest.indices(indices);
        }
        validateQueryRequest.types(types);
        Map<String, String> expectedParams = new HashMap<>();
        setRandomIndicesOptions(validateQueryRequest::indicesOptions, validateQueryRequest::indicesOptions, expectedParams);
        validateQueryRequest.explain(randomBoolean());
        validateQueryRequest.rewrite(randomBoolean());
        validateQueryRequest.allShards(randomBoolean());
        expectedParams.put("explain", Boolean.toString(validateQueryRequest.explain()));
        expectedParams.put("rewrite", Boolean.toString(validateQueryRequest.rewrite()));
        expectedParams.put("all_shards", Boolean.toString(validateQueryRequest.allShards()));
        Request request = RequestConverters.validateQuery(validateQueryRequest);
        StringJoiner endpoint = new StringJoiner("/", "/", "");
        if (indices != null && indices.length > 0) {
            endpoint.add(String.join(",", indices));
            if (types != null && types.length > 0) {
                endpoint.add(String.join(",", types));
            }
        }
        endpoint.add("_validate/query");
        assertThat(request.getEndpoint(), equalTo(endpoint.toString()));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertToXContentBody(validateQueryRequest, request.getEntity());
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
    }

    public void testGetTemplateRequest() throws Exception {
        Map<String, String> encodes = new HashMap<>();
        encodes.put("log", "log");
        encodes.put("1", "1");
        encodes.put("template#1", "template%231");
        encodes.put("template-*", "template-*");
        encodes.put("foo^bar", "foo%5Ebar");
        List<String> names = randomSubsetOf(1, encodes.keySet());
        GetIndexTemplatesRequest getTemplatesRequest = new GetIndexTemplatesRequest().names(names.toArray(new String[0]));
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(getTemplatesRequest, expectedParams);
        setRandomLocal(getTemplatesRequest, expectedParams);
        Request request = RequestConverters.getTemplates(getTemplatesRequest);
        assertThat(request.getEndpoint(), equalTo("/_template/" + names.stream().map(encodes::get).collect(Collectors.joining(","))));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), nullValue());
    }

    public void testAnalyzeRequest() throws Exception {
        AnalyzeRequest indexAnalyzeRequest = new AnalyzeRequest()
            .text("Here is some text")
            .index("test_index")
            .analyzer("test_analyzer");

        Request request = RequestConverters.analyze(indexAnalyzeRequest);
        assertThat(request.getEndpoint(), equalTo("/test_index/_analyze"));
        assertToXContentBody(indexAnalyzeRequest, request.getEntity());

        AnalyzeRequest analyzeRequest = new AnalyzeRequest()
            .text("more text")
            .analyzer("test_analyzer");
        assertThat(RequestConverters.analyze(analyzeRequest).getEndpoint(), equalTo("/_analyze"));
    }

    public void testGetScriptRequest() {
        GetStoredScriptRequest getStoredScriptRequest = new GetStoredScriptRequest("x-script");
        Map<String, String> expectedParams = new HashMap<>();
        setRandomMasterTimeout(getStoredScriptRequest, expectedParams);

        Request request = RequestConverters.getScript(getStoredScriptRequest);
        assertThat(request.getEndpoint(), equalTo("/_scripts/" + getStoredScriptRequest.id()));
        assertThat(request.getMethod(), equalTo(HttpGet.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), nullValue());
    }

    public void testDeleteScriptRequest() {
        DeleteStoredScriptRequest deleteStoredScriptRequest = new DeleteStoredScriptRequest("x-script");

        Map<String, String> expectedParams = new HashMap<>();
        setRandomTimeout(deleteStoredScriptRequest::timeout, AcknowledgedRequest.DEFAULT_ACK_TIMEOUT, expectedParams);
        setRandomMasterTimeout(deleteStoredScriptRequest, expectedParams);

        Request request = RequestConverters.deleteScript(deleteStoredScriptRequest);
        assertThat(request.getEndpoint(), equalTo("/_scripts/" + deleteStoredScriptRequest.id()));
        assertThat(request.getMethod(), equalTo(HttpDelete.METHOD_NAME));
        assertThat(request.getParameters(), equalTo(expectedParams));
        assertThat(request.getEntity(), nullValue());
    }

    private static void assertToXContentBody(ToXContent expectedBody, HttpEntity actualEntity) throws IOException {
        BytesReference expectedBytes = XContentHelper.toXContent(expectedBody, REQUEST_BODY_CONTENT_TYPE, false);
        assertEquals(XContentType.JSON.mediaTypeWithoutParameters(), actualEntity.getContentType().getValue());
        assertEquals(expectedBytes, new BytesArray(EntityUtils.toByteArray(actualEntity)));
    }

    public void testEndpointBuilder() {
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder();
            assertEquals("/", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart(Strings.EMPTY_ARRAY);
            assertEquals("/", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("");
            assertEquals("/", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("a", "b");
            assertEquals("/a/b", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("a").addPathPart("b").addPathPartAsIs("_create");
            assertEquals("/a/b/_create", endpointBuilder.build());
        }

        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("a", "b", "c").addPathPartAsIs("_create");
            assertEquals("/a/b/c/_create", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("a").addPathPartAsIs("_create");
            assertEquals("/a/_create", endpointBuilder.build());
        }
    }

    public void testEndpointBuilderEncodeParts() {
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("-#index1,index#2", "type", "id");
            assertEquals("/-%23index1,index%232/type/id", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("index", "type#2", "id");
            assertEquals("/index/type%232/id", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("index", "type", "this/is/the/id");
            assertEquals("/index/type/this%2Fis%2Fthe%2Fid", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("index", "type", "this|is|the|id");
            assertEquals("/index/type/this%7Cis%7Cthe%7Cid", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("index", "type", "id#1");
            assertEquals("/index/type/id%231", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("<logstash-{now/M}>", "_search");
            assertEquals("/%3Clogstash-%7Bnow%2FM%7D%3E/_search", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("??????");
            assertEquals("/??????", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("foo bar");
            assertEquals("/foo%20bar", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("foo+bar");
            assertEquals("/foo+bar", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("foo+bar");
            assertEquals("/foo+bar", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("foo/bar");
            assertEquals("/foo%2Fbar", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("foo^bar");
            assertEquals("/foo%5Ebar", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addPathPart("cluster1:index1,index2").addPathPartAsIs("_search");
            assertEquals("/cluster1:index1,index2/_search", endpointBuilder.build());
        }
        {
            EndpointBuilder endpointBuilder = new EndpointBuilder().addCommaSeparatedPathParts(new String[] { "index1", "index2" })
                    .addPathPartAsIs("cache/clear");
            assertEquals("/index1,index2/cache/clear", endpointBuilder.build());
        }
    }

    public void testEndpoint() {
        assertEquals("/index/type/id", RequestConverters.endpoint("index", "type", "id"));
        assertEquals("/index/type/id/_endpoint", RequestConverters.endpoint("index", "type", "id", "_endpoint"));
        assertEquals("/index1,index2", RequestConverters.endpoint(new String[] { "index1", "index2" }));
        assertEquals("/index1,index2/_endpoint", RequestConverters.endpoint(new String[] { "index1", "index2" }, "_endpoint"));
        assertEquals("/index1,index2/type1,type2/_endpoint",
                RequestConverters.endpoint(new String[] { "index1", "index2" }, new String[] { "type1", "type2" }, "_endpoint"));
        assertEquals("/index1,index2/_endpoint/suffix1,suffix2",
                RequestConverters.endpoint(new String[] { "index1", "index2" }, "_endpoint", new String[] { "suffix1", "suffix2" }));
    }

    public void testCreateContentType() {
        final XContentType xContentType = randomFrom(XContentType.values());
        assertEquals(xContentType.mediaTypeWithoutParameters(), RequestConverters.createContentType(xContentType).getMimeType());
    }

    public void testEnforceSameContentType() {
        XContentType xContentType = randomFrom(XContentType.JSON, XContentType.SMILE);
        IndexRequest indexRequest = new IndexRequest().source(singletonMap("field", "value"), xContentType);
        assertEquals(xContentType, enforceSameContentType(indexRequest, null));
        assertEquals(xContentType, enforceSameContentType(indexRequest, xContentType));

        XContentType bulkContentType = randomBoolean() ? xContentType : null;

        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class,
                () -> enforceSameContentType(new IndexRequest().source(singletonMap("field", "value"), XContentType.CBOR),
                        bulkContentType));
        assertEquals("Unsupported content-type found for request with content-type [CBOR], only JSON and SMILE are supported",
                exception.getMessage());

        exception = expectThrows(IllegalArgumentException.class,
                () -> enforceSameContentType(new IndexRequest().source(singletonMap("field", "value"), XContentType.YAML),
                        bulkContentType));
        assertEquals("Unsupported content-type found for request with content-type [YAML], only JSON and SMILE are supported",
                exception.getMessage());

        XContentType requestContentType = xContentType == XContentType.JSON ? XContentType.SMILE : XContentType.JSON;

        exception = expectThrows(IllegalArgumentException.class,
                () -> enforceSameContentType(new IndexRequest().source(singletonMap("field", "value"), requestContentType), xContentType));
        assertEquals("Mismatching content-type found for request with content-type [" + requestContentType + "], "
                + "previous requests have content-type [" + xContentType + "]", exception.getMessage());
    }

    public void testXPackInfo() {
        XPackInfoRequest infoRequest = new XPackInfoRequest();
        Map<String, String> expectedParams = new HashMap<>();
        infoRequest.setVerbose(randomBoolean());
        if (false == infoRequest.isVerbose()) {
            expectedParams.put("human", "false");
        }
        int option = between(0, 2);
        switch (option) {
        case 0:
            infoRequest.setCategories(EnumSet.allOf(XPackInfoRequest.Category.class));
            break;
        case 1:
            infoRequest.setCategories(EnumSet.of(XPackInfoRequest.Category.FEATURES));
            expectedParams.put("categories", "features");
            break;
        case 2:
            infoRequest.setCategories(EnumSet.of(XPackInfoRequest.Category.FEATURES, XPackInfoRequest.Category.BUILD));
            expectedParams.put("categories", "build,features");
            break;
        default:
            throw new IllegalArgumentException("invalid option [" + option + "]");
        }

        Request request = RequestConverters.xPackInfo(infoRequest);
        assertEquals(HttpGet.METHOD_NAME, request.getMethod());
        assertEquals("/_xpack", request.getEndpoint());
        assertNull(request.getEntity());
        assertEquals(expectedParams, request.getParameters());
    }

    public void testXPackPutWatch() throws Exception {
        PutWatchRequest putWatchRequest = new PutWatchRequest();
        String watchId = randomAlphaOfLength(10);
        putWatchRequest.setId(watchId);
        String body = randomAlphaOfLength(20);
        putWatchRequest.setSource(new BytesArray(body), XContentType.JSON);

        Map<String, String> expectedParams = new HashMap<>();
        if (randomBoolean()) {
            putWatchRequest.setActive(false);
            expectedParams.put("active", "false");
        }

        if (randomBoolean()) {
            long version = randomLongBetween(10, 100);
            putWatchRequest.setVersion(version);
            expectedParams.put("version", String.valueOf(version));
        }

        Request request = RequestConverters.xPackWatcherPutWatch(putWatchRequest);
        assertEquals(HttpPut.METHOD_NAME, request.getMethod());
        assertEquals("/_xpack/watcher/watch/" + watchId, request.getEndpoint());
        assertEquals(expectedParams, request.getParameters());
        assertThat(request.getEntity().getContentType().getValue(), is(XContentType.JSON.mediaTypeWithoutParameters()));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        request.getEntity().writeTo(bos);
        assertThat(bos.toString("UTF-8"), is(body));
    }

    /**
     * Randomize the {@link FetchSourceContext} request parameters.
     */
    private static void randomizeFetchSourceContextParams(Consumer<FetchSourceContext> consumer, Map<String, String> expectedParams) {
        if (randomBoolean()) {
            if (randomBoolean()) {
                boolean fetchSource = randomBoolean();
                consumer.accept(new FetchSourceContext(fetchSource));
                if (fetchSource == false) {
                    expectedParams.put("_source", "false");
                }
            } else {
                int numIncludes = randomIntBetween(0, 5);
                String[] includes = new String[numIncludes];
                String includesParam = randomFields(includes);
                if (numIncludes > 0) {
                    expectedParams.put("_source_include", includesParam);
                }
                int numExcludes = randomIntBetween(0, 5);
                String[] excludes = new String[numExcludes];
                String excludesParam = randomFields(excludes);
                if (numExcludes > 0) {
                    expectedParams.put("_source_exclude", excludesParam);
                }
                consumer.accept(new FetchSourceContext(true, includes, excludes));
            }
        }
    }

    private static void setRandomSearchParams(SearchRequest searchRequest,
                                              Map<String, String> expectedParams) {
        expectedParams.put(RestSearchAction.TYPED_KEYS_PARAM, "true");
        if (randomBoolean()) {
            searchRequest.routing(randomAlphaOfLengthBetween(3, 10));
            expectedParams.put("routing", searchRequest.routing());
        }
        if (randomBoolean()) {
            searchRequest.preference(randomAlphaOfLengthBetween(3, 10));
            expectedParams.put("preference", searchRequest.preference());
        }
        if (randomBoolean()) {
            searchRequest.searchType(randomFrom(SearchType.CURRENTLY_SUPPORTED));
        }
        expectedParams.put("search_type", searchRequest.searchType().name().toLowerCase(Locale.ROOT));
        if (randomBoolean()) {
            searchRequest.requestCache(randomBoolean());
            expectedParams.put("request_cache", Boolean.toString(searchRequest.requestCache()));
        }
        if (randomBoolean()) {
            searchRequest.allowPartialSearchResults(randomBoolean());
            expectedParams.put("allow_partial_search_results", Boolean.toString(searchRequest.allowPartialSearchResults()));
        }
        if (randomBoolean()) {
            searchRequest.setBatchedReduceSize(randomIntBetween(2, Integer.MAX_VALUE));
        }
        expectedParams.put("batched_reduce_size", Integer.toString(searchRequest.getBatchedReduceSize()));
        if (randomBoolean()) {
            searchRequest.scroll(randomTimeValue());
            expectedParams.put("scroll", searchRequest.scroll().keepAlive().getStringRep());
        }
    }

    private static void setRandomIndicesOptions(Consumer<IndicesOptions> setter, Supplier<IndicesOptions> getter,
            Map<String, String> expectedParams) {

        if (randomBoolean()) {
            setter.accept(IndicesOptions.fromOptions(randomBoolean(), randomBoolean(), randomBoolean(), randomBoolean()));
        }
        expectedParams.put("ignore_unavailable", Boolean.toString(getter.get().ignoreUnavailable()));
        expectedParams.put("allow_no_indices", Boolean.toString(getter.get().allowNoIndices()));
        if (getter.get().expandWildcardsOpen() && getter.get().expandWildcardsClosed()) {
            expectedParams.put("expand_wildcards", "open,closed");
        } else if (getter.get().expandWildcardsOpen()) {
            expectedParams.put("expand_wildcards", "open");
        } else if (getter.get().expandWildcardsClosed()) {
            expectedParams.put("expand_wildcards", "closed");
        } else {
            expectedParams.put("expand_wildcards", "none");
        }
    }

    private static void setRandomIncludeDefaults(GetIndexRequest request, Map<String, String> expectedParams) {
        if (randomBoolean()) {
            boolean includeDefaults = randomBoolean();
            request.includeDefaults(includeDefaults);
            if (includeDefaults) {
                expectedParams.put("include_defaults", String.valueOf(includeDefaults));
            }
        }
    }

    private static void setRandomHumanReadable(GetIndexRequest request, Map<String, String> expectedParams) {
        if (randomBoolean()) {
            boolean humanReadable = randomBoolean();
            request.humanReadable(humanReadable);
            if (humanReadable) {
                expectedParams.put("human", String.valueOf(humanReadable));
            }
        }
    }

    private static void setRandomLocal(Consumer<Boolean> setter, Map<String, String> expectedParams) {
        if (randomBoolean()) {
            boolean local = randomBoolean();
            setter.accept(local);
            if (local) {
                expectedParams.put("local", String.valueOf(local));
            }
        }
    }

    private static void setRandomLocal(MasterNodeReadRequest<?> request, Map<String, String> expectedParams) {
        setRandomLocal(request::local, expectedParams);
    }

    private static void setRandomTimeout(Consumer<String> setter, TimeValue defaultTimeout, Map<String, String> expectedParams) {
        if (randomBoolean()) {
            String timeout = randomTimeValue();
            setter.accept(timeout);
            expectedParams.put("timeout", timeout);
        } else {
            expectedParams.put("timeout", defaultTimeout.getStringRep());
        }
    }

    private static void setRandomMasterTimeout(MasterNodeRequest<?> request, Map<String, String> expectedParams) {
        if (randomBoolean()) {
            String masterTimeout = randomTimeValue();
            request.masterNodeTimeout(masterTimeout);
            expectedParams.put("master_timeout", masterTimeout);
        } else {
            expectedParams.put("master_timeout", MasterNodeRequest.DEFAULT_MASTER_NODE_TIMEOUT.getStringRep());
        }
    }

    private static void setRandomWaitForActiveShards(Consumer<ActiveShardCount> setter, ActiveShardCount defaultActiveShardCount,
                                                     Map<String, String> expectedParams) {
        if (randomBoolean()) {
            int waitForActiveShardsInt = randomIntBetween(-1, 5);
            String waitForActiveShardsString;
            if (waitForActiveShardsInt == -1) {
                waitForActiveShardsString = "all";
            } else {
                waitForActiveShardsString = String.valueOf(waitForActiveShardsInt);
            }
            ActiveShardCount activeShardCount = ActiveShardCount.parseString(waitForActiveShardsString);
            setter.accept(activeShardCount);
            if (defaultActiveShardCount.equals(activeShardCount) == false) {
                expectedParams.put("wait_for_active_shards", waitForActiveShardsString);
            }
        }
    }

    private static void setRandomUpdateAllTypes(Consumer<Boolean> setter, Map<String, String> expectedParams) {
        if (randomBoolean()){
        boolean updateAllTypes=randomBoolean();
        setter.accept(updateAllTypes);
        if(updateAllTypes){
        expectedParams.put("update_all_types",Boolean.TRUE.toString());
        }
        }
    }

    private static void setRandomRefreshPolicy(Consumer<WriteRequest.RefreshPolicy> setter, Map<String, String> expectedParams) {
        if (randomBoolean()) {
            WriteRequest.RefreshPolicy refreshPolicy = randomFrom(WriteRequest.RefreshPolicy.values());
            setter.accept(refreshPolicy);
            if (refreshPolicy != WriteRequest.RefreshPolicy.NONE) {
                expectedParams.put("refresh", refreshPolicy.getValue());
            }
        }
    }

    private static void setRandomVersion(DocWriteRequest<?> request, Map<String, String> expectedParams) {
        if (randomBoolean()) {
            long version = randomFrom(Versions.MATCH_ANY, Versions.MATCH_DELETED, Versions.NOT_FOUND, randomNonNegativeLong());
            request.version(version);
            if (version != Versions.MATCH_ANY) {
                expectedParams.put("version", Long.toString(version));
            }
        }
    }

    private static void setRandomVersionType(Consumer<VersionType> setter, Map<String, String> expectedParams) {
        if (randomBoolean()) {
            VersionType versionType = randomFrom(VersionType.values());
            setter.accept(versionType);
            if (versionType != VersionType.INTERNAL) {
                expectedParams.put("version_type", versionType.name().toLowerCase(Locale.ROOT));
            }
        }
    }

    private static String randomFields(String[] fields) {
        StringBuilder excludesParam = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String exclude = randomAlphaOfLengthBetween(3, 10);
            fields[i] = exclude;
            excludesParam.append(exclude);
            if (i < fields.length - 1) {
                excludesParam.append(",");
            }
        }
        return excludesParam.toString();
    }

    private static String[] randomIndicesNames(int minIndicesNum, int maxIndicesNum) {
        int numIndices = randomIntBetween(minIndicesNum, maxIndicesNum);
        String[] indices = new String[numIndices];
        for (int i = 0; i < numIndices; i++) {
            indices[i] = "index-" + randomAlphaOfLengthBetween(2, 5).toLowerCase(Locale.ROOT);
        }
        return indices;
    }
}
