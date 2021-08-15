# Nested 数据结构 

## 什么是Nested

嵌套文档，又称为父子文档，相对比较其他数据结构例如array以及Object，多了更多的内联关系

nested聚合隶属于聚合分类中的Bucket聚合分类。

举个例子
```
{
  "title": "Invest Money",
  "body": "Please start investing money as soon...",
  "tags": ["money", "invest"],
  "published_on": "18 Oct 2017",
  "comments": [
    {
      "name": "William",
      "age": 34,
      "rating": 8,
      "comment": "Nice article..",
      "commented_on": "30 Nov 2017"
    },
    {
      "name": "John",
      "age": 38,
      "rating": 9,
      "comment": "I started investing after reading this.",
      "commented_on": "25 Nov 2017"
    },
    {
      "name": "Smith",
      "age": 33,
      "rating": 7,
      "comment": "Very good post",
      "commented_on": "20 Nov 2017"
    }
  ]
}
```
上述给出的是官网demo
如果直接query，便会出现映射上的问题
```
GET /blog/_search?pretty
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "comments.name": "John"
          }
        },
        {
          "match": {
            "comments.age": 34
          }
        }
      ]
    }
  }
}
```
返回是demo document。

在lucene库中没有内部对象的概念，内部对象会被抽象成简单的KV对列表。

内部存储是
```
{
  "title":                    [ invest, money ],
  "body":                     [ as, investing, money, please, soon, start ],
  "tags":                     [ invest, money ],
  "published_on":             [ 18 Oct 2017 ]
  "comments.name":            [ smith, john, william ],
  "comments.comment":         [ after, article, good, i, investing, nice, post, reading, started, this, very ],
  "comments.age":             [ 33, 34, 38 ],
  "comments.rating":          [ 7, 8, 9 ],
  "comments.commented_on":    [ 20 Nov 2017, 25 Nov 2017, 30 Nov 2017 ]
}
```
返回的便是第一个数值，所以导致关系丢失，从而会丢失数据。
因此需要将 comments字段更新为nested类型
```
 "comments": {
          "type": "nested",
          "properties": {
            "name": {
              "type": "text"
            },
            "comment": {
              "type": "text"
            },
            "age": {
              "type": "short"
            },
            "rating": {
              "type": "short"
            },
            "commented_on": {
              "type": "text"
            }
          }
```


之后再次进行查询，将返回为null,因为内部没有相对应的结果进行返回。
更改之后的数据为
```
{
  {
    "comments.name":    [ john ],
    "comments.comment": [ after i investing started reading this ],
    "comments.age":     [ 38 ],
    "comments.rating":  [ 9 ],
    "comments.date":    [ 25 Nov 2017 ]
  },
  {
    "comments.name":    [ william ],
    "comments.comment": [ article, nice ],
    "comments.age":     [ 34 ],
    "comments.rating":   [ 8 ],
    "comments.date":    [ 30 Nov 2017 ]
  },
  {
    "comments.name":    [ smith ],
    "comments.comment": [ good, post, very],
    "comments.age":     [ 33 ],
    "comments.rating":   [ 7 ],
    "comments.date":    [ 20 Nov 2017 ]
  },
  {
    "title":            [ invest, money ],
    "body":             [ as, investing, money, please, soon, start ],
    "tags":             [ invest, money ],
    "published_on":     [ 18 Oct 2017 ]
  }
}
```
所以 什么是nested类型，是对象数据类型的专用版本，允许对象数组可以彼此独立查询的方式进行索引操作。我们从mapping的源码开始逐一解读，完成Nested操作的CURD部分

## Nested的CURD
Nested类型的增删改查如下。

### Nested类型———add
```
POST blog_new/blog/2
{
  "title": "Hero",
  "body": "Hero test body...",
  "tags": ["Heros", "happy"],
  "published_on": "6 Oct 2018",
  "comments": [
    {
      "name": "steve",
      "age": 24,
      "rating": 18,
      "comment": "Nice article..",
      "commented_on": "3 Nov 2018"
    }
  ]
}
```

### Nested类型——Delete
序号为1的评论原来有三条，删除后评论数为2条
```
POST  blog_new/blog/1/_update
{
 "script": {
    "lang": "painless",
    "source": "ctx._source.comments.removeIf(it -> it.name == 'John');"
 }
}
```
### Nested类型——改 Update
```
POST blog_new/blog/2/_update
{
  "script": {
    "source": "for(e in ctx._source.comments){
                if (e.name == 'steve') {
                  e.age = 25; 
                  e.comment= 'very very good article...';
                  }
                }" 
  }
}
```
### Nested类型——查
```
GET /blog_new/_search?pretty
{
  "query": {
    "bool": {
      "must": [
        {
          "nested": {
            "path": "comments",
            "query": {
              "bool": {
                "must": [
                  {
                    "match": {
                      "comments.name": "William"
                    }
                  },
                  {
                    "match": {
                      "comments.age": 34
                    }
                  }
                ]
              }
            }
          }
        }
      ]
    }
  }
}
```
### Nested类型 aggeration
nested聚合隶属于聚合分类中的Bucket聚合分类。
聚合blog_new 中评论者年龄最小的值。
```
GET blog_new/_search
{
  "size": 0,
  "aggs": {
    "comm_aggs": {
      "nested": {
        "path": "comments"
      },
      "aggs": {
        "min_age": {
          "min": {
            "field": "comments.age"
          }
        }
      }
    }
  }
}
```
## Nested相关源码

### MapperSerivce

MapperService 是加载整个索引的基类，因为要检索到nested内容，则必须在mapping中有所修改，甚至是实现相关功能，因此从这里入手。索引对nested字段的限制如下
```
 public static final Setting<Long> INDEX_MAPPING_NESTED_FIELDS_LIMIT_SETTING =
        Setting.longSetting("index.mapping.nested_fields.limit", 50L, 0, Property.Dynamic, Property.IndexScope);
```
同时添加boolean变量判断是否需要更新nested对象
这里定义了一个判断方法
```
    public boolean hasNested() {
        return this.hasNested;
    }
```
也是就是boolean对象字段，首先寻找类内调用了该字段的方法，也就是internalMerge 方法。
```
private synchronized DocumentMapper internalMerge(DocumentMapper mapper, MergeReason reason) {
        boolean hasNested = this.hasNested;
        Map<String, ObjectMapper> fullPathObjectMappers = this.fullPathObjectMappers;

        assert mapper != null;
        // check naming
        validateTypeName(mapper.type());
        DocumentMapper oldMapper = this.mapper;
        DocumentMapper newMapper;
        if (oldMapper != null) {
            newMapper = oldMapper.merge(mapper.mapping(), reason);
        } else {
            newMapper = mapper;
        }
        newMapper.root().fixRedundantIncludes();
        List<ObjectMapper> objectMappers = new ArrayList<>();
        List<FieldMapper> fieldMappers = new ArrayList<>();
        List<FieldAliasMapper> fieldAliasMappers = new ArrayList<>();
        MetadataFieldMapper[] metadataMappers = newMapper.mapping().metadataMappers;
        Collections.addAll(fieldMappers, metadataMappers);
        MapperUtils.collect(newMapper.mapping().root(), objectMappers, fieldMappers, fieldAliasMappers);

        MapperMergeValidator.validateNewMappers(objectMappers, fieldMappers, fieldAliasMappers);
        checkPartitionedIndexConstraints(newMapper);

        // update lookup data-structures
        FieldTypeLookup newFieldTypes = new FieldTypeLookup(fieldMappers, fieldAliasMappers);

        for (ObjectMapper objectMapper : objectMappers) {
            if (fullPathObjectMappers == this.fullPathObjectMappers) {
                fullPathObjectMappers = new HashMap<>(this.fullPathObjectMappers);
            }
            fullPathObjectMappers.put(objectMapper.fullPath(), objectMapper);

            if (objectMapper.nested().isNested()) {
                hasNested = true;
            }
        }

        MapperMergeValidator.validateFieldReferences(fieldMappers, fieldAliasMappers,
            fullPathObjectMappers, newFieldTypes, metadataMappers, newMapper);

        ContextMapping.validateContextPaths(indexSettings.getIndexVersionCreated(), fieldMappers, newFieldTypes::get);

        if (reason != MergeReason.MAPPING_RECOVERY) {
            
            checkTotalFieldsLimit(objectMappers.size() + fieldMappers.size() - metadataMappers.length
                + fieldAliasMappers.size() );
            checkFieldNameSoftLimit(objectMappers, fieldMappers, fieldAliasMappers);
            checkNestedFieldsLimit(fullPathObjectMappers);
            checkDepthLimit(fullPathObjectMappers.keySet());
        }
        checkIndexSortCompatibility(indexSettings.getIndexSortConfig(), hasNested);

        if (reason == MergeReason.MAPPING_UPDATE_PREFLIGHT) {
            return newMapper;
        }

        // only need to immutably rewrap these if the previous reference was changed.
        // if not then they are already implicitly immutable.
        if (fullPathObjectMappers != this.fullPathObjectMappers) {
            fullPathObjectMappers = Collections.unmodifiableMap(fullPathObjectMappers);
        }

        // commit the change
        this.mapper = newMapper;
        this.fieldTypes = newFieldTypes;
        this.hasNested = hasNested;
        this.fullPathObjectMappers = fullPathObjectMappers;

        assert assertSerialization(newMapper);

        return newMapper;
    }
```
该方法具有多态性，这里调用该方法，是用于区分写入的对象是否是nested类型
合并写入.包括后续的检查checkNestedFieldLimit方法，这里也调用的nested的标志位继续判断，这里的判断
```
        for (ObjectMapper objectMapper : fullPathObjectMappers.values()) {
            if (objectMapper.nested().isNested()) {
                actualNestedFields++;
            }
        }
```
这里都会看到ObjectMapper类，内部封装了nested方法，以及静态的一个Nested类
```
public static class Nested {

        public static final Nested NO = new Nested(false, new Explicit<>(false, false), new Explicit<>(false, false));

        public static Nested newNested() {
            return new Nested(true, new Explicit<>(false, false), new Explicit<>(false, false));
        }

        public static Nested newNested(Explicit<Boolean> includeInParent, Explicit<Boolean> includeInRoot) {
            return new Nested(true, includeInParent, includeInRoot);
        }

        private final boolean nested;
        private Explicit<Boolean> includeInParent;
        private Explicit<Boolean> includeInRoot;

        private Nested(boolean nested, Explicit<Boolean> includeInParent, Explicit<Boolean> includeInRoot) {
            this.nested = nested;
            this.includeInParent = includeInParent;
            this.includeInRoot = includeInRoot;
        }

        public void merge(Nested mergeWith, MergeReason reason) {
            if (isNested()) {
                if (!mergeWith.isNested()) {
                    throw new IllegalArgumentException("cannot change object mapping from nested to non-nested");
                }
            } else {
                if (mergeWith.isNested()) {
                    throw new IllegalArgumentException("cannot change object mapping from non-nested to nested");
                }
            }

            if (reason == MergeReason.INDEX_TEMPLATE) {
                if (mergeWith.includeInParent.explicit()) {
                    includeInParent = mergeWith.includeInParent;
                }
                if (mergeWith.includeInRoot.explicit()) {
                    includeInRoot = mergeWith.includeInRoot;
                }
            } else {
                if (includeInParent.value() != mergeWith.includeInParent.value()) {
                    throw new MapperException("the [include_in_parent] parameter can't be updated on a nested object mapping");
                }
                if (includeInRoot.value() != mergeWith.includeInRoot.value()) {
                    throw new MapperException("the [include_in_root] parameter can't be updated on a nested object mapping");
                }
            }
        }

        public boolean isNested() {
            return nested;
        }

        public boolean isIncludeInParent() {
            return includeInParent.value();
        }

        public boolean isIncludeInRoot() {
            return includeInRoot.value();
        }

        public void setIncludeInParent(boolean value) {
            includeInParent = new Explicit<>(value, true);
        }

        public void setIncludeInRoot(boolean value) {
            includeInRoot = new Explicit<>(value, true);
        }
    }
```
首先看merge部分，这里有两个变量 includeInParent，includeInRoot。
Explicit 的解释如下
a）隐式设置，例如 通过一些默认值
b）明确设置，例如 从用户选择
合并有冲突的配置设置时，例如 字段映射设置，最好保留显式，选择而不是默认情况下仅隐式做出的选择。
因此这两个变量的含义也很清晰了，判断是否是父子文档或者是rootsource文档。
```
 protected void doMerge(final ObjectMapper mergeWith, MergeReason reason) {
        nested().merge(mergeWith.nested(), reason);

        if (mergeWith.dynamic != null) {
            this.dynamic = mergeWith.dynamic;
        }

        if (reason == MergeReason.INDEX_TEMPLATE) {
            if (mergeWith.enabled.explicit()) {
                this.enabled = mergeWith.enabled;
            }
        } else if (isEnabled() != mergeWith.isEnabled()) {
            throw new MapperException("the [enabled] parameter can't be updated for the object mapping [" + name() + "]");
        }

        for (Mapper mergeWithMapper : mergeWith) {
            Mapper mergeIntoMapper = mappers.get(mergeWithMapper.simpleName());

            Mapper merged;
            if (mergeIntoMapper == null) {
                merged = mergeWithMapper;
            } else if (mergeIntoMapper instanceof ObjectMapper) {
                ObjectMapper objectMapper = (ObjectMapper) mergeIntoMapper;
                merged = objectMapper.merge(mergeWithMapper, reason);
            } else {
                assert mergeIntoMapper instanceof FieldMapper || mergeIntoMapper instanceof FieldAliasMapper;
                if (mergeWithMapper instanceof ObjectMapper) {
                    throw new IllegalArgumentException("can't merge a non object mapping [" +
                        mergeWithMapper.name() + "] with an object mapping");
                }

                // If we're merging template mappings when creating an index, then a field definition always
                // replaces an existing one.
                if (reason == MergeReason.INDEX_TEMPLATE) {
                    merged = mergeWithMapper;
                } else {
                    merged = mergeIntoMapper.merge(mergeWithMapper);
                }
            }
            putMapper(merged);
        }
    }
```
完成Nested合并之后，放入到新的Mapper中。
相同的 在内部类Builder中也用到了Nested
同样的toXContent方法中通过调用XContentBuilder也加入了nested字段，
```
 if (nested.isNested()) {
            builder.field("type", NESTED_CONTENT_TYPE);
            if (nested.isIncludeInParent()) {
                builder.field("include_in_parent", true);
            }
            if (nested.isIncludeInRoot()) {
                builder.field("include_in_root", true);
            }
        }
```
至此 完成MapperService中Nested的分析

由于Nested的功能并不仅仅在Mapping中，在CURD中也有他的身影。同样的 还有aggeration部分，也有nested的作用。
从这里开始我们去看看Nested在其它类中调用，从而理清楚整个思路。

#### Nested类的调用
##### DocumentParser
在DocumentParser类中，parseObjectOrNested方法用到了nested的判断，判断是否是嵌套结构 如果是嵌套结构 就调用nestedContext 方法，得到新的context,然后innerParseObject，进行对象分解。
```
static void parseObjectOrNested(ParseContext context, ObjectMapper mapper) throws IOException {
        if (mapper.isEnabled() == false) {
            context.parser().skipChildren();
            return;
        }
        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.VALUE_NULL) {
            // the object is null ("obj1" : null), simply bail
            return;
        }

        String currentFieldName = parser.currentName();
        if (token.isValue()) {
            throw new MapperParsingException("object mapping for [" + mapper.name() + "] tried to parse field [" + currentFieldName
                + "] as object, but found a concrete value");
        }

        ObjectMapper.Nested nested = mapper.nested();
        if (nested.isNested()) {
            context = nestedContext(context, mapper);
        }

        // if we are at the end of the previous object, advance
        if (token == XContentParser.Token.END_OBJECT) {
            token = parser.nextToken();
        }
        if (token == XContentParser.Token.START_OBJECT) {
            // if we are just starting an OBJECT, advance, this is the object we are parsing, we need the name first
            token = parser.nextToken();
        }

        innerParseObject(context, mapper, parser, currentFieldName, token);
        // restore the enable path flag
        if (nested.isNested()) {
            nested(context, nested);
        }
    }

  private static ParseContext nestedContext(ParseContext context, ObjectMapper mapper) {
        context = context.createNestedContext(mapper.fullPath());
        ParseContext.Document nestedDoc = context.doc();
        ParseContext.Document parentDoc = nestedDoc.getParent();
        IndexableField idField = parentDoc.getField(IdFieldMapper.NAME);
        if (idField != null) {
           
            nestedDoc.add(new Field(IdFieldMapper.NAME, idField.binaryValue(), IdFieldMapper.Defaults.NESTED_FIELD_TYPE));
        } else {
            throw new IllegalStateException("The root document of a nested document should have an _id field");
        }

        nestedDoc.add(NestedPathFieldMapper.field(context.indexSettings().getSettings(), mapper.nestedTypePath()));
        return context;
    }
```
在nestedContext中，该方法的主要作用如下

将uid或id添加到此嵌套的Lucene文档中，如果不这样做，则在删除文档时，仅删除根Lucene文档，而不删除嵌套的Lucene文档！ 除了将拥有僵尸Lucene文档这一事实外，Lucene索引（文档块）内的文档顺序也将不正确，因为不同根文档的嵌套文档随后将与其他根文档对齐。 这将导致嵌套查询，排序，聚合和内部匹配失败或产生错误的结果。
在这里  只需要将id存储为索引字段，以便IndexWriter＃deleteDocuments（term）也可以在删除根文档时将其删除。
mapper.nested()方法
```
   private static void nested(ParseContext context, ObjectMapper.Nested nested) {
        ParseContext.Document nestedDoc = context.doc();
        ParseContext.Document parentDoc = nestedDoc.getParent();
        Settings settings = context.indexSettings().getSettings();
        if (nested.isIncludeInParent()) {
            addFields(settings, nestedDoc, parentDoc);
        }
        if (nested.isIncludeInRoot()) {
            ParseContext.Document rootDoc = context.rootDoc();
            // don't add it twice, if its included in parent, and we are handling the master doc...
            if (!nested.isIncludeInParent() || parentDoc != rootDoc) {
                addFields(settings, nestedDoc, rootDoc);
            }
        }
    }
```
```
private static Tuple<Integer, ObjectMapper> getDynamicParentMapper(ParseContext context, final String[] paths,
            ObjectMapper currentParent) {
                    ......
                        mapper = (ObjectMapper) builder.build(builderContext);
                        if (mapper.nested() != ObjectMapper.Nested.NO) {
                            throw new MapperParsingException("It is forbidden to create dynamic nested objects (["
                                + context.path().pathAsText(paths[i]) + "]) through `copy_to` or dots in field names");
                        }
                    ......        
    }
```
##### RootObjectMapper
```
 @Override
        protected ObjectMapper createMapper(String name, String fullPath, Explicit<Boolean> enabled, Nested nested, Dynamic dynamic,
                Map<String, Mapper> mappers, @Nullable Settings settings) {
            assert !nested.isNested();
            return new RootObjectMapper(name, enabled, dynamic, mappers,
                    dynamicDateTimeFormatters,
                    dynamicTemplates,
                    dateDetection, numericDetection, settings);
        }
       /**
      * 删除{@link ObjectMapper.Nested}树中的多余根目录，以避免重复
      *当{@code isIncludeInRoot}为{@code true}的节点时，根映射器上的字段
      *本身包含在父节点中，对于该父节点，{@ code isIncludeInRoot}为
      * {@code true}或由根节点链可传递地包含在根中
      * {@code isIncludeInParent}返回{@code true}。
     */
    public void fixRedundantIncludes() {
       fixRedundantIncludes(this, true);
    }

    private static void fixRedundantIncludes(ObjectMapper objectMapper, boolean parentIncluded) {
        for (Mapper mapper : objectMapper) {
            if (mapper instanceof ObjectMapper) {
                ObjectMapper child = (ObjectMapper) mapper;
                Nested nested = child.nested();
                boolean isNested = nested.isNested();
                boolean includeInRootViaParent = parentIncluded && isNested && nested.isIncludeInParent();
                boolean includedInRoot = isNested && nested.isIncludeInRoot();
                if (includeInRootViaParent && includedInRoot) {
                    nested.setIncludeInParent(true);
                    nested.setIncludeInRoot(false);
                }
                fixRedundantIncludes(child, includeInRootViaParent || includedInRoot);
            }
        }
    }
```

在这里结合前文的document以及index的相关只是，我们便可以发现，nested一定程度上，也是一颗Trie树，对于树的一些合并或者是变换操作，从而使得树中的子树相对容易读取。

### IndexFieldData

这是一个接口类 它在内部抽象类XFieldComparatorSource 又嵌套了一个Nested对象。

这个对象的构造和ObjectMapper中的Nested完全不同。

内部抽象类XFieldComparatorSource 是一个拓展源，有自定义比较器可以在这种情况下重用字段数据，同时也可以减少在另一个节点上减少搜索结果时将要使用的类型
```
/ **
*围绕与父文档匹配的过滤器和与子文档匹配的过滤器的简单包装器类。 对于每个根文档R，R将位于父筛选器中，其子文档将是包含在前一个父+ 1（如果没有前一个父）与R（不包括）之间的内部集中的文档。
* /

public static class Nested {

            private final BitSetProducer rootFilter;
            private final Query innerQuery;
            private final NestedSortBuilder nestedSort;
            private final IndexSearcher searcher;

            public Nested(BitSetProducer rootFilter, Query innerQuery, NestedSortBuilder nestedSort, IndexSearcher searcher) {
                this.rootFilter = rootFilter;
                this.innerQuery = innerQuery;
                this.nestedSort = nestedSort;
                this.searcher = searcher;
            }

            public Query getInnerQuery() {
                return innerQuery;
            }

            public NestedSortBuilder getNestedSort() { return nestedSort; }

            /**
             * Get a {@link BitDocIdSet} that matches the root documents.
             */
            public BitSet rootDocs(LeafReaderContext ctx) throws IOException {
                return rootFilter.getBitSet(ctx);
            }

            /**
             * Get a {@link DocIdSet} that matches the inner documents.
             */
            public DocIdSetIterator innerDocs(LeafReaderContext ctx) throws IOException {
                Weight weight = searcher.createWeight(searcher.rewrite(innerQuery), ScoreMode.COMPLETE_NO_SCORES, 1f);
                Scorer s = weight.scorer(ctx);
                return s == null ? null : s.iterator();
            }
        }
```
单单看代码很难看出来他的作用到底是什么。我们从测试用例入手，看看如何调用
```

    /**
     * Test that the sort builder nested object gets created in the SortField
     */
    public void testBuildNested() throws IOException {
        QueryShardContext shardContextMock = createMockShardContext();

        FieldSortBuilder sortBuilder = new FieldSortBuilder("fieldName")
                .setNestedSort(new NestedSortBuilder("path").setFilter(QueryBuilders.termQuery(MAPPED_STRING_FIELDNAME, "value")));
        SortField sortField = sortBuilder.build(shardContextMock).field;
        assertThat(sortField.getComparatorSource(), instanceOf(XFieldComparatorSource.class));
        XFieldComparatorSource comparatorSource = (XFieldComparatorSource) sortField.getComparatorSource();
        Nested nested = comparatorSource.nested();
        assertNotNull(nested);
        assertEquals(new TermQuery(new Term(MAPPED_STRING_FIELDNAME, "value")), nested.getInnerQuery());

        NestedSortBuilder nestedSort = new NestedSortBuilder("path");
        sortBuilder = new FieldSortBuilder("fieldName").setNestedSort(nestedSort);
        sortField = sortBuilder.build(shardContextMock).field;
        assertThat(sortField.getComparatorSource(), instanceOf(XFieldComparatorSource.class));
        comparatorSource = (XFieldComparatorSource) sortField.getComparatorSource();
        nested = comparatorSource.nested();
        assertNotNull(nested);
        assertEquals(new TermQuery(new Term(NestedPathFieldMapper.NAME, "path")), nested.getInnerQuery());

        nestedSort.setFilter(QueryBuilders.termQuery(MAPPED_STRING_FIELDNAME, "value"));
        sortBuilder = new FieldSortBuilder("fieldName").setNestedSort(nestedSort);
        sortField = sortBuilder.build(shardContextMock).field;
        assertThat(sortField.getComparatorSource(), instanceOf(XFieldComparatorSource.class));
        comparatorSource = (XFieldComparatorSource) sortField.getComparatorSource();
        nested = comparatorSource.nested();
        assertNotNull(nested);
        assertEquals(new TermQuery(new Term(MAPPED_STRING_FIELDNAME, "value")), nested.getInnerQuery());
    }
```
在这里
Nested nested = comparatorSource.nested();
实现实例化 并且调用了 getInnerQuery方法 确保不为空。
才执行下一步，因此 这个Nested得作用也是起到了修饰作用，从而保证query的能够正确执行。




