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

package org.elasticsearch.index.mapper;

import com.carrotsearch.hppc.ObjectArrayList;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocValuesFieldExistsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteArrayDataOutput;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.util.CollectionUtils;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.plain.BytesBinaryIndexFieldData;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.QueryShardException;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.support.CoreValuesSourceType;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.TypeParsers.parseField;

public class BinaryFieldMapper extends FieldMapper {

    public static final String CONTENT_TYPE = "binary";

    public static class Defaults {
        public static final FieldType FIELD_TYPE = new FieldType();

        static {
            FIELD_TYPE.setIndexOptions(IndexOptions.NONE);
            FIELD_TYPE.setOmitNorms(true);
            FIELD_TYPE.freeze();
        }
    }

    public static class Builder extends FieldMapper.Builder<Builder> {

        public Builder(String name) {
            super(name, Defaults.FIELD_TYPE);
            hasDocValues = false;
            builder = this;
        }

        @Override
        public BinaryFieldMapper build(BuilderContext context) {
            return new BinaryFieldMapper(name, fieldType, new BinaryFieldType(buildFullName(context), hasDocValues, meta),
                    multiFieldsBuilder.build(this, context), copyTo);
        }

        @Override
        public Builder index(boolean index) {
            if (index) {
                throw new MapperParsingException("Binary field [" + name() + "] cannot be indexed");
            }
            return builder;
        }
    }

    public static class TypeParser implements Mapper.TypeParser {
        @Override
        public BinaryFieldMapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            BinaryFieldMapper.Builder builder = new BinaryFieldMapper.Builder(name);
            parseField(builder, name, node, parserContext);
            return builder;
        }
    }

    public static final class BinaryFieldType extends MappedFieldType {

        public BinaryFieldType(String name, boolean hasDocValues, Map<String, String> meta) {
            super(name, false, hasDocValues, TextSearchInfo.NONE, meta);
        }

        public BinaryFieldType(String name) {
            this(name, true, Collections.emptyMap());
        }

        protected BinaryFieldType(BinaryFieldType ref) {
            super(ref);
        }

        @Override
        public MappedFieldType clone() {
            return new BinaryFieldType(this);
        }


        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        @Override
        public DocValueFormat docValueFormat(String format, ZoneId timeZone) {
            return DocValueFormat.BINARY;
        }

        @Override
        public BytesReference valueForDisplay(Object value) {
            if (value == null) {
                return null;
            }

            BytesReference bytes;
            if (value instanceof BytesRef) {
                bytes = new BytesArray((BytesRef) value);
            } else if (value instanceof BytesReference) {
                bytes = (BytesReference) value;
            } else if (value instanceof byte[]) {
                bytes = new BytesArray((byte[]) value);
            } else {
                bytes = new BytesArray(Base64.getDecoder().decode(value.toString()));
            }
            return bytes;
        }

        @Override
        public IndexFieldData.Builder fielddataBuilder(String fullyQualifiedIndexName) {
            failIfNoDocValues();
            return new BytesBinaryIndexFieldData.Builder(CoreValuesSourceType.BYTES);
        }

        @Override
        public Query existsQuery(QueryShardContext context) {
            if (hasDocValues()) {
                return new DocValuesFieldExistsQuery(name());
            } else {
                return new TermQuery(new Term(FieldNamesFieldMapper.NAME, name()));
            }
        }

        @Override
        public Query termQuery(Object value, QueryShardContext context) {
            throw new QueryShardException(context, "Binary fields do not support searching");
        }
    }

    protected BinaryFieldMapper(String simpleName, FieldType fieldType, MappedFieldType mappedFieldType,
                                MultiFields multiFields, CopyTo copyTo) {
        super(simpleName, fieldType, mappedFieldType, multiFields, copyTo);
    }

    @Override
    protected void parseCreateField(ParseContext context) throws IOException {
        if (!fieldType.stored() && !fieldType().hasDocValues()) {
            return;
        }
        byte[] value = context.parseExternalValue(byte[].class);
        if (value == null) {
            if (context.parser().currentToken() == XContentParser.Token.VALUE_NULL) {
                return;
            } else {
                value = context.parser().binaryValue();
            }
        }
        if (value == null) {
            return;
        }
        if (fieldType.stored()) {
            context.doc().add(new StoredField(fieldType().name(), value));
        }

        if (fieldType().hasDocValues()) {
            CustomBinaryDocValuesField field = (CustomBinaryDocValuesField) context.doc().getByKey(fieldType().name());
            if (field == null) {
                field = new CustomBinaryDocValuesField(fieldType().name(), value);
                context.doc().addWithKey(fieldType().name(), field);
            } else {
                field.add(value);
            }
        } else {
            // Only add an entry to the field names field if the field is stored
            // but has no doc values so exists query will work on a field with
            // no doc values
            createFieldNamesField(context);
        }

    }

    @Override
    protected boolean indexedByDefault() {
        return false;
    }

    @Override
    protected boolean docValuesByDefault() {
        return false;
    }

    @Override
    protected void mergeOptions(FieldMapper other, List<String> conflicts) {

    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    public static class CustomBinaryDocValuesField extends CustomDocValuesField {

        private final ObjectArrayList<byte[]> bytesList;

        private int totalSize = 0;

        public CustomBinaryDocValuesField(String name, byte[] bytes) {
            super(name);
            bytesList = new ObjectArrayList<>();
            add(bytes);
        }

        public void add(byte[] bytes) {
            bytesList.add(bytes);
            totalSize += bytes.length;
        }

        @Override
        public BytesRef binaryValue() {
            try {
                CollectionUtils.sortAndDedup(bytesList);
                int size = bytesList.size();
                final byte[] bytes = new byte[totalSize + (size + 1) * 5];
                ByteArrayDataOutput out = new ByteArrayDataOutput(bytes);
                out.writeVInt(size);  // write total number of values
                for (int i = 0; i < size; i ++) {
                    final byte[] value = bytesList.get(i);
                    int valueLength = value.length;
                    out.writeVInt(valueLength);
                    out.writeBytes(value, 0, valueLength);
                }
                return new BytesRef(bytes, 0, out.getPosition());
            } catch (IOException e) {
                throw new ElasticsearchException("Failed to get binary value", e);
            }

        }
    }
}