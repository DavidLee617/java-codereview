### NestedQuery
```
package org.elasticsearch.xpack.ql.querydsl.query;
```
从xpack中的NestedQuery,着手看Nested文档的查询过程
xpack是es的增值服务包。这里通过调用实现安全加密的查询。
```
    @Override
    public boolean containsNestedField(String path, String field) {
        boolean iContainThisField = this.path.equals(path) && fields.containsKey(field);
        boolean myChildContainsThisField = child.containsNestedField(path, field);
        return iContainThisField || myChildContainsThisField;
    }

    @Override
    public Query addNestedField(String path, String field, String format, boolean hasDocValues) {
        if (false == this.path.equals(path)) {
            // I'm not at the right path so let my child query have a crack at it
            Query rewrittenChild = child.addNestedField(path, field, format, hasDocValues);
            if (rewrittenChild == child) {
                return this;
            }
            return new NestedQuery(source(), path, fields, rewrittenChild);
        }
        if (fields.containsKey(field)) {
            // I already have the field, no rewriting needed
            return this;
        }
        Map<String, Map.Entry<Boolean, String>> newFields = new HashMap<>(fields.size() + 1);
        newFields.putAll(fields);
        newFields.put(field, new AbstractMap.SimpleImmutableEntry<>(hasDocValues, format));
        return new NestedQuery(source(), path, unmodifiableMap(newFields), child);
    }

    @Override
    public void enrichNestedSort(NestedSortBuilder sort) {
        child.enrichNestedSort(sort);
        if (false == sort.getPath().equals(path)) {
            return;
        }

        //TODO: Add all filters in nested sorting when https://github.com/elastic/elasticsearch/issues/33079 is implemented
        // Adding multiple filters to sort sections makes sense for nested queries where multiple conditions belong to the same
        // nested query. The current functionality creates one nested query for each condition involving a nested field.
        QueryBuilder childAsBuilder = child.asBuilder();
        if (sort.getFilter() != null && false == sort.getFilter().equals(childAsBuilder)) {
            // throw new SqlIllegalArgumentException("nested query should have been grouped in one place");
            return;
        }
        sort.setFilter(childAsBuilder);
    }

    @Override
    public QueryBuilder asBuilder() {
        // disable score
        NestedQueryBuilder query = nestedQuery(path, child.asBuilder(), ScoreMode.None);

        if (!fields.isEmpty()) {
            InnerHitBuilder ihb = new InnerHitBuilder();
            ihb.setSize(0);
            ihb.setSize(MAX_INNER_HITS);
            ihb.setName(path + "_" + COUNTER++);

            boolean noSourceNeeded = true;
            List<String> sourceFields = new ArrayList<>();

            for (Map.Entry<String, Map.Entry<Boolean, String>> entry : fields.entrySet()) {
                if (entry.getValue().getKey()) {
                    ihb.addDocValueField(entry.getKey(), entry.getValue().getValue());
                }
                else {
                    sourceFields.add(entry.getKey());
                    noSourceNeeded = false;
                }
            }

            if (noSourceNeeded) {
                ihb.setFetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
                ihb.setStoredFieldNames(NO_STORED_FIELD);
            }
            else {
                ihb.setFetchSourceContext(new FetchSourceContext(true, sourceFields.toArray(new String[sourceFields.size()]), null));
            }

            query.innerHit(ihb);
        }

        return query;
    }

```
这些方法都是实现了Query抽象类。而Query抽象类是查询的中间表示形式，可重写以获取其他未引用的嵌套字段，然后用于构建Elasticsearch {@link QueryBuilder}。
首先是containsNestedField方法，判断父类和子类的fiedld是否相同，换句话说 是不是nested嵌套结构

addNestedField 添加nest嵌套结构的Field,首先判断路径是否相等，不同的话则需要创建一个子类的方法 如果fields里面已经有这个key就不需要增加，同时创建一个新的嵌套map Map<String, Map.Entry<Boolean, String>> newFields 将所有的fields放入一个全新的map中，最后新建一个权限NestedQuery

```
        newFields.putAll(fields);
        newFields.put(field, new AbstractMap.SimpleImmutableEntry<>(hasDocValues, format));
```

enrichNestedSort
同样子节点也会去调用这个方法

对于多个条件属于同一嵌套查询的嵌套查询，添加多个过滤器以对部分进行排序是有意义的。 当前功能为涉及嵌套字段的每个条件创建一个嵌套查询。也就是如下这两句
```
   QueryBuilder childAsBuilder = child.asBuilder();
    sort.setFilter(childAsBuilder);
```
再来看 asBuilder方法，这里是将整个类封装成Builder模式，返回一个QueryBuilder，方便其他类进行调用
采用建造者模式，逐步的进行set，主要的部分是
```
ihb.setFetchSourceContext(new FetchSourceContext(true, sourceFields.toArray(new String[sourceFields.size()]), null));
```
从这里看，就是设置获取源文本
完成这些之后，通过innterHit开始创建query

## query
```
package org.elasticsearch.index.query;
```


### NestedQueryBuilder

XContent

受JSON和请求解析启发的处理内容之上的通用抽象。
(ie json). 换句话说 数据的json化

这里有两个方法，一个toXContent，fromXContent两个方法进行数据的json化 和反json化

相关nest的部分在doToQuery(QueryShardContext context)方法 ,源码如下 逐一分析
```
    @Override
    protected Query doToQuery(QueryShardContext context) throws IOException {
        if (context.allowExpensiveQueries() == false) {
            throw new ElasticsearchException("[joining] queries cannot be executed when '" +
                    ALLOW_EXPENSIVE_QUERIES.getKey() + "' is set to false.");
        }

        ObjectMapper nestedObjectMapper = context.getObjectMapper(path);
        if (nestedObjectMapper == null) {
            if (ignoreUnmapped) {
                return new MatchNoDocsQuery();
            } else {
                throw new IllegalStateException("[" + NAME + "] failed to find nested object under path [" + path + "]");
            }
        }
        if (!nestedObjectMapper.nested().isNested()) {
            throw new IllegalStateException("[" + NAME + "] nested object under path [" + path + "] is not of nested type");
        }
        final BitSetProducer parentFilter;
        Query innerQuery;
        ObjectMapper objectMapper = context.nestedScope().getObjectMapper();
        if (objectMapper == null) {
            parentFilter = context.bitsetFilter(Queries.newNonNestedFilter());
        } else {
            parentFilter = context.bitsetFilter(objectMapper.nestedTypeFilter());
        }

        try {
            context.nestedScope().nextLevel(nestedObjectMapper);
            innerQuery = this.query.toQuery(context);
        } finally {
            context.nestedScope().previousLevel();
        }

        // ToParentBlockJoinQuery requires that the inner query only matches documents
        // in its child space
        if (new NestedHelper(context.getMapperService()).mightMatchNonNestedDocs(innerQuery, path)) {
            innerQuery = Queries.filtered(innerQuery, nestedObjectMapper.nestedTypeFilter());
        }

        return new ESToParentBlockJoinQuery(innerQuery, parentFilter, scoreMode,
                objectMapper == null ? null : objectMapper.fullPath());
    }
```
allowExpensiveQueries 是否允许查询
从上下文的path里面通过getObjectMapper创建ObjectMapper对象，保证对象不为空，且Nested方法成立。

继续从NestedScope 这个类可以在查询解析期间，这会跟踪当前的嵌套级别。利用栈进行出入栈的方法，存放父子级别的嵌套节点。
```
public ObjectMapper getObjectMapper() {
        return levelStack.peek();
    }
```
将获取到的类 都进行Filter过滤 采用bitsetFilter方法从cache中去读取
```
    public BitSetProducer bitsetFilter(Query filter) {
        return bitsetFilterCache.getBitSetProducer(filter);
    }

```
具体调用生产者模式，也就是每段{@link BitSet}的生产者。利用装饰器模式实现。

```
    public BitSetProducer getBitSetProducer(Query query) {
        return new QueryWrapperBitSetProducer(query);
    }

        QueryWrapperBitSetProducer(Query query) {
            this.query = Objects.requireNonNull(query);
        }
```
如果获取到的类不为空，
就调用nestedTypeFilter
```
    public Query nestedTypeFilter() {
        return this.nestedTypeFilter;
    }
```
为空则调用Queries.newNonNestedFilter()方法
```
    public static Query newNonNestedFilter() {
        return new DocValuesFieldExistsQuery(SeqNoFieldMapper.PRIMARY_TERM_NAME);
    }
```

接下来的部分就是innerQuery变量的获取
这里从两个方便获取innerQuery
然而innerQuery有两种获取方法。
```
innerQuery = this.query.toQuery(context);

innerQuery = Queries.filtered(innerQuery, nestedObjectMapper.nestedTypeFilter());
```

```
    public ESToParentBlockJoinQuery(Query childQuery, BitSetProducer parentsFilter, ScoreMode scoreMode, String path) {
        this(new ToParentBlockJoinQuery(childQuery, parentsFilter, scoreMode), path, scoreMode);
    }

  public ToParentBlockJoinQuery(Query childQuery, BitSetProducer parentsFilter, ScoreMode scoreMode) {
    super();
    this.childQuery = childQuery;
    this.parentsFilter = parentsFilter;
    this.scoreMode = scoreMode;
  }
```
最终写入ToParentBlockJoinQuery类中
该类是lucene的内核模块，
要求使用{@link IndexWriter＃addDocuments IndexWriter.addDocuments（）}或{@link IndexWriter＃updateDocuments IndexWriter.updateDocuments（）} API将子级文档和父级文档作为一个整体进行索引。 在每个块中，子文档必须首先出现，并以父文档结尾。 在搜索时，您提供了一个标识父母的过滤器，但是该过滤器必须为每个子阅读器提供一个{@link BitSet}。

一旦构建了块索引，就可以使用此查询来包装仅与子文档匹配的所有子查询，并将该子文档空间中的匹配项连接到父文档空间。 然后，您可以将此查询与父文档空间中的其他查询一起作为子句使用。如果需要以相反的顺序加入，请参见{@link ToChildBlockJoinQuery}。

子文档必须与父文档正交：包装的子查询绝不能返回父文档。有关概述，请参见{@link org.apache.lucene.search.join}


### NestedSortBuilder

新增功能 建造者模式 嵌套排序
也一样具有写出和写入
排序根据深度进行计算
```
  public NestedSortBuilder rewrite(QueryRewriteContext ctx) throws IOException {
        if (filter == null && nestedSort == null) {
            return this;
        }
        QueryBuilder rewriteFilter = this.filter;
        NestedSortBuilder rewriteNested = this.nestedSort;
        if (filter != null) {
            rewriteFilter = filter.rewrite(ctx);
        }
        if (nestedSort != null) {
            rewriteNested = nestedSort.rewrite(ctx);
        }
        if (rewriteFilter != this.filter || rewriteNested != this.nestedSort) {
            return new NestedSortBuilder(this.path).setFilter(rewriteFilter).setMaxChildren(this.maxChildren).setNestedSort(rewriteNested);
        } else {
            return this;
        }
    }
```

在ES接口中的调用
```
SortBuilders.fieldSort("bulkOrders.expiryDate").order(SortOrder.asc).setNestedSort(new NestedSortBuilder("bulkOrders"));
```



