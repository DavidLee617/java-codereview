# agg

```
package org.elasticsearch.search.aggregations.bucket.nested;
```

在aggeration中对于Nested类型的聚合有专门的package负责这一部分


## Interface
Nested-Aggeration nested对象聚合。 

定义单个存储容器，其中包含特定路径的所有嵌套文档。 整个package中都实现了这两个接口

### Nested
 ``` 
 public interface Nested extends SingleBucketAggregation { }  
 ```  
###  ReverseNested
```
public interface ReverseNested extends SingleBucketAggregation {
}
```

## Parse
Parse对象的设计也相同
### ParsedNested
```
public class ParsedNested extends ParsedSingleBucketAggregation implements Nested {

    @Override
    public String getType() {
        return NestedAggregationBuilder.NAME;
    }

    public static ParsedNested fromXContent(XContentParser parser, final String name) throws IOException {
        return parseXContent(parser, new ParsedNested(), name);
    }
}
```

### ParsedReverseNested
```

public class ParsedReverseNested extends ParsedSingleBucketAggregation implements Nested {

    @Override
    public String getType() {
        return ReverseNestedAggregationBuilder.NAME;
    }

    public static ParsedReverseNested fromXContent(XContentParser parser, final String name) throws IOException {
        return parseXContent(parser, new ParsedReverseNested(), name);
    }
}

```

## Service 
两个类的构造方法已经接口设置都大相径庭，从顶层来看 两个类的抽象并没有细致差别，区别应当在高一层
### InternalNested
```
public class InternalNested extends InternalSingleBucketAggregation implements Nested
```
### InternalReverseNested
```
public class InternalReverseNested extends InternalSingleBucketAggregation implements ReverseNested
```

## Builder
这两个建造者模式，大部分细节都相对一只，主要是在doBuild上有些许差异，Nest 对象直接向后遍历 nestScope 队列。 Nested 会在 
```
ObjectMapper parentObjectMapper = queryShardContext.nestedScope().nextLevel(childObjectMapper);
```
这个部分去找父节点

ReverseNestedAggregatorBuilder 则会从父结点开始向下找，然后回拨。

### NestedAggregatorBuilder
```
  @Override
    protected AggregatorFactory doBuild(QueryShardContext queryShardContext,
                                            AggregatorFactory parent,
                                            Builder subFactoriesBuilder) throws IOException {
        ObjectMapper childObjectMapper = queryShardContext.getObjectMapper(path);
        if (childObjectMapper == null) {
            // in case the path has been unmapped:
            return new NestedAggregatorFactory(name, null, null, queryShardContext,
                parent, subFactoriesBuilder, metadata);
        }

        if (childObjectMapper.nested().isNested() == false) {
            throw new AggregationExecutionException("[nested] nested path [" + path + "] is not nested");
        }
        try {
            ObjectMapper parentObjectMapper = queryShardContext.nestedScope().nextLevel(childObjectMapper);
            return new NestedAggregatorFactory(name, parentObjectMapper, childObjectMapper, queryShardContext,
                parent, subFactoriesBuilder, metadata);
        } finally {
            queryShardContext.nestedScope().previousLevel();
        }
    }
```
### ReverseNestedAggregatorBuilder
```
  @Override
    protected AggregatorFactory doBuild(QueryShardContext queryShardContext, AggregatorFactory parent, Builder subFactoriesBuilder)
            throws IOException {
        if (findNestedAggregatorFactory(parent) == null) {
            throw new IllegalArgumentException("Reverse nested aggregation [" + name + "] can only be used inside a [nested] aggregation");
        }

        ObjectMapper parentObjectMapper = null;
        if (path != null) {
            parentObjectMapper = queryShardContext.getObjectMapper(path);
            if (parentObjectMapper == null) {
                return new ReverseNestedAggregatorFactory(name, true, null, queryShardContext, parent, subFactoriesBuilder, metadata);
            }
            if (parentObjectMapper.nested().isNested() == false) {
                throw new AggregationExecutionException("[reverse_nested] nested path [" + path + "] is not nested");
            }
        }

        NestedScope nestedScope = queryShardContext.nestedScope();
        try {
            nestedScope.nextLevel(parentObjectMapper);
            return new ReverseNestedAggregatorFactory(name, false, parentObjectMapper, queryShardContext, parent, subFactoriesBuilder,
                    metadata);
        } finally {
            nestedScope.previousLevel();
        }
    }
```
## Factory
这里是主要功能区，从代码层面上来看，这里似乎并没有区别
### NestedAggregatorFactory
```
    public Aggregator createInternal(SearchContext searchContext,
                                        Aggregator parent,
                                        boolean collectsFromSingleBucket,
                                        Map<String, Object> metadata) throws IOException {
        if (childObjectMapper == null) {
            return new Unmapped(name, searchContext, parent, metadata);
        }
        return new NestedAggregator(name, factories, parentObjectMapper, childObjectMapper, searchContext, parent,
            metadata, collectsFromSingleBucket);
    }

    private static final class Unmapped extends NonCollectingAggregator {

        Unmapped(String name,
                    SearchContext context,
                    Aggregator parent,
                    Map<String, Object> metadata) throws IOException {
            super(name, context, parent, metadata);
        }

        @Override
        public InternalAggregation buildEmptyAggregation() {
            return new InternalNested(name, 0, buildEmptySubAggregations(), metadata());
        }
    }
```
### ReverseNestedAggregatorFactory
```
 @Override
    public Aggregator createInternal(SearchContext searchContext,
                                        Aggregator parent,
                                        boolean collectsFromSingleBucket,
                                        Map<String, Object> metadata) throws IOException {
        if (unmapped) {
            return new Unmapped(name, searchContext, parent, metadata);
        } else {
            return new ReverseNestedAggregator(name, factories, parentObjectMapper,
                searchContext, parent, metadata);
        }
    }

    private static final class Unmapped extends NonCollectingAggregator {

        Unmapped(String name,
                    SearchContext context,
                    Aggregator parent,
                    Map<String, Object> metadata) throws IOException {
            super(name, context, parent, metadata);
        }

        @Override
        public InternalAggregation buildEmptyAggregation() {
            return new InternalReverseNested(name, 0, buildEmptySubAggregations(), metadata());
        }
    }
```
## Aggerator
在该部分 相应的差异性就显现出来了
### NestedAggregator
```
public class NestedAggregator extends BucketsAggregator implements SingleBucketAggregator {

    static final ParseField PATH_FIELD = new ParseField("path");

    private final BitSetProducer parentFilter;
    private final Query childFilter;
    private final boolean collectsFromSingleBucket;

    private BufferingNestedLeafBucketCollector bufferingNestedLeafBucketCollector;

    NestedAggregator(String name, AggregatorFactories factories, ObjectMapper parentObjectMapper, ObjectMapper childObjectMapper,
                     SearchContext context, Aggregator parentAggregator,
                     Map<String, Object> metadata, boolean collectsFromSingleBucket) throws IOException {
        super(name, factories, context, parentAggregator, metadata);

        Query parentFilter = parentObjectMapper != null ? parentObjectMapper.nestedTypeFilter()
            : Queries.newNonNestedFilter();
        this.parentFilter = context.bitsetFilterCache().getBitSetProducer(parentFilter);
        this.childFilter = childObjectMapper.nestedTypeFilter();
        this.collectsFromSingleBucket = collectsFromSingleBucket;
    }

    @Override
    public LeafBucketCollector getLeafCollector(final LeafReaderContext ctx, final LeafBucketCollector sub) throws IOException {
        IndexReaderContext topLevelContext = ReaderUtil.getTopLevelContext(ctx);
        IndexSearcher searcher = new IndexSearcher(topLevelContext);
        searcher.setQueryCache(null);
        Weight weight = searcher.createWeight(searcher.rewrite(childFilter), ScoreMode.COMPLETE_NO_SCORES, 1f);
        Scorer childDocsScorer = weight.scorer(ctx);

        final BitSet parentDocs = parentFilter.getBitSet(ctx);
        final DocIdSetIterator childDocs = childDocsScorer != null ? childDocsScorer.iterator() : null;
        if (collectsFromSingleBucket) {
            return new LeafBucketCollectorBase(sub, null) {
                @Override
                public void collect(int parentDoc, long bucket) throws IOException {
                    // if parentDoc is 0 then this means that this parent doesn't have child docs (b/c these appear always before the parent
                    // doc), so we can skip:
                    if (parentDoc == 0 || parentDocs == null || childDocs == null) {
                        return;
                    }

                    final int prevParentDoc = parentDocs.prevSetBit(parentDoc - 1);
                    int childDocId = childDocs.docID();
                    if (childDocId <= prevParentDoc) {
                        childDocId = childDocs.advance(prevParentDoc + 1);
                    }

                    for (; childDocId < parentDoc; childDocId = childDocs.nextDoc()) {
                        collectBucket(sub, childDocId, bucket);
                    }
                }
            };
        } else {
            return bufferingNestedLeafBucketCollector = new BufferingNestedLeafBucketCollector(sub, parentDocs, childDocs);
        }
    }

    @Override
    protected void preGetSubLeafCollectors() throws IOException {
        processBufferedDocs();
    }

    @Override
    protected void doPostCollection() throws IOException {
        processBufferedDocs();
    }

    private void processBufferedDocs() throws IOException {
        if (bufferingNestedLeafBucketCollector != null) {
            bufferingNestedLeafBucketCollector.processBufferedChildBuckets();
        }
    }

    @Override
    public InternalAggregation[] buildAggregations(long[] owningBucketOrds) throws IOException {
        return buildAggregationsForSingleBucket(owningBucketOrds, (owningBucketOrd, subAggregationResults) ->
            new InternalNested(name, bucketDocCount(owningBucketOrd), subAggregationResults, metadata()));
    }

    @Override
    public InternalAggregation buildEmptyAggregation() {
        return new InternalNested(name, 0, buildEmptySubAggregations(), metadata());
    }

    class BufferingNestedLeafBucketCollector extends LeafBucketCollectorBase {

        final BitSet parentDocs;
        final LeafBucketCollector sub;
        final DocIdSetIterator childDocs;
        final LongArrayList bucketBuffer = new LongArrayList();

        Scorable scorer;
        int currentParentDoc = -1;
        final CachedScorable cachedScorer = new CachedScorable();

        BufferingNestedLeafBucketCollector(LeafBucketCollector sub, BitSet parentDocs, DocIdSetIterator childDocs) {
            super(sub, null);
            this.sub = sub;
            this.parentDocs = parentDocs;
            this.childDocs = childDocs;
        }

        @Override
        public void setScorer(Scorable scorer) throws IOException {
            this.scorer = scorer;
            super.setScorer(cachedScorer);
        }

        @Override
        public void collect(int parentDoc, long bucket) throws IOException {
            // if parentDoc is 0 then this means that this parent doesn't have child docs (b/c these appear always before the parent
            // doc), so we can skip:
            if (parentDoc == 0 || parentDocs == null || childDocs == null) {
                return;
            }

            if (currentParentDoc != parentDoc) {
                processBufferedChildBuckets();
                if (scoreMode().needsScores()) {
                    // cache the score of the current parent
                    cachedScorer.score = scorer.score();
                }
                currentParentDoc = parentDoc;

            }
            bucketBuffer.add(bucket);
        }

        void processBufferedChildBuckets() throws IOException {
            if (bucketBuffer.isEmpty()) {
                return;
            }


            final int prevParentDoc = parentDocs.prevSetBit(currentParentDoc - 1);
            int childDocId = childDocs.docID();
            if (childDocId <= prevParentDoc) {
                childDocId = childDocs.advance(prevParentDoc + 1);
            }

            for (; childDocId < currentParentDoc; childDocId = childDocs.nextDoc()) {
                cachedScorer.doc = childDocId;
                final long[] buffer = bucketBuffer.buffer;
                final int size = bucketBuffer.size();
                for (int i = 0; i < size; i++) {
                    collectBucket(sub, childDocId, buffer[i]);
                }
            }
            bucketBuffer.clear();
        }
    }

    private static class CachedScorable extends Scorable {
        int doc;
        float score;

        @Override
        public final float score() { return score; }

        @Override
        public int docID() {
            return doc;
        }

    }

}
```

首先就是在NestedAggeration中所定义的两个内部类CachedScorable和BufferingNestedLeafBucketCollector，这两个类并没有在ReverseNesterAggerator中得到体现，因此先关注这两个类

1. CachedScorable 将文档id和文档的评分相绑定，是一个只提供了读方法 在BufferingNestedLeafBucketCollector中得到实现，通过LeafBucketCollector 进行超类的初始化
   
   对于LeafBucketCollectorBase类的解释如下
   一个{@link LeafBucketCollector}，它将所有调用委派给子叶子聚合器，并在实现{@link ScorerAware}的情况下在其值源上设置计分器。
2. BufferingNestedLeafBucketCollector 该类创建了如下几个变量
    ```
        final BitSet parentDocs; //父亲文档
        final LeafBucketCollector sub; //子文档
        final DocIdSetIterator childDocs; //子文档迭代器
        final LongArrayList bucketBuffer = new LongArrayList(); //缓存空间

         Scorable scorer;//评分器
        int currentParentDoc = -1;//当前父文档id
        final CachedScorable cachedScorer = new CachedScorable();//评分缓存
    ```
    再来看类内方法
    collect 方法
    搜集所有父亲docid的子文档，然后加入缓存

    processBufferedChildBuckets 该方法用于处理子文档缓存。
    首先获取前父文档 当子文档id小于前父文档id，则调用childDocs的advance(prevParentDoc + 1)赋值给子文档id
    然后开始遍历，如果当前子文档的id小于当前父文档
        设置cache评分的文档id为当前子文档id
        然后获取buffer 往buffer里面初始化的写数据
        完成遍历后
        最后临时清空bucketbuffer

    再看类内调用 getLeafCollector 也就是获取叶结点的集合
    ```
        //遍历阅读器树并返回给定上下文的顶级阅读器上下文，或者换句话说，阅读器树的根上下文
        IndexReaderContext topLevelContext = ReaderUtil.getTopLevelContext(ctx);
        //
        IndexSearcher searcher = new IndexSearcher(topLevelContext);
        //设置QueryCache以在不需要分数时使用。 值为null表示永远不要缓存查询匹配项。 在开始使用此IndexSearcher之前，应调用此方法。
        //注意：使用查询缓存时，将查询传递给IndexSearcher后不应对其进行修改。
        searcher.setQueryCache(null);
        //创建权重
        Weight weight = searcher.createWeight(searcher.rewrite(childFilter), ScoreMode.COMPLETE_NO_SCORES, 1f);
        //子文档积分器
        Scorer childDocsScorer = weight.scorer(ctx);

        final BitSet parentDocs = parentFilter.getBitSet(ctx);
        final DocIdSetIterator childDocs = childDocsScorer != null ? childDocsScorer.iterator() : null;
    ```
    继续向下走 如果从单个bucket收集的判断变量collectsFromSingleBucket为true，则 返回新的LeafBucketCollectorBase方法，内部实现 新的collect方法 开始遍历查找。且buffer不清空
    否则就返回一个新的BufferingNestedLeafBucketCollector对象
### ReverseNestedAggregator
```

public class ReverseNestedAggregator extends BucketsAggregator implements SingleBucketAggregator {

    static final ParseField PATH_FIELD = new ParseField("path");

    private final Query parentFilter;
    private final BitSetProducer parentBitsetProducer;

    public ReverseNestedAggregator(String name, AggregatorFactories factories, ObjectMapper objectMapper,
            SearchContext context, Aggregator parent, Map<String, Object> metadata)
            throws IOException {
        super(name, factories, context, parent, metadata);
        if (objectMapper == null) {
            parentFilter = Queries.newNonNestedFilter();
        } else {
            parentFilter = objectMapper.nestedTypeFilter();
        }
        parentBitsetProducer = context.bitsetFilterCache().getBitSetProducer(parentFilter);
    }

    @Override
    protected LeafBucketCollector getLeafCollector(LeafReaderContext ctx, final LeafBucketCollector sub) throws IOException {
        // In ES if parent is deleted, then also the children are deleted, so the child docs this agg receives
        // must belong to parent docs that is alive. For this reason acceptedDocs can be null here.
        final BitSet parentDocs = parentBitsetProducer.getBitSet(ctx);
        if (parentDocs == null) {
            return LeafBucketCollector.NO_OP_COLLECTOR;
        }
        final LongIntHashMap bucketOrdToLastCollectedParentDoc = new LongIntHashMap(32);
        return new LeafBucketCollectorBase(sub, null) {
            @Override
            public void collect(int childDoc, long bucket) throws IOException {
                // fast forward to retrieve the parentDoc this childDoc belongs to
                final int parentDoc = parentDocs.nextSetBit(childDoc);
                assert childDoc <= parentDoc && parentDoc != DocIdSetIterator.NO_MORE_DOCS;

                int keySlot = bucketOrdToLastCollectedParentDoc.indexOf(bucket);
                if (bucketOrdToLastCollectedParentDoc.indexExists(keySlot)) {
                    int lastCollectedParentDoc = bucketOrdToLastCollectedParentDoc.indexGet(keySlot);
                    if (parentDoc > lastCollectedParentDoc) {
                        collectBucket(sub, parentDoc, bucket);
                        bucketOrdToLastCollectedParentDoc.indexReplace(keySlot, parentDoc);
                    }
                } else {
                    collectBucket(sub, parentDoc, bucket);
                    bucketOrdToLastCollectedParentDoc.indexInsert(keySlot, bucket, parentDoc);
                }
            }
        };
    }

    @Override
    public InternalAggregation[] buildAggregations(long[] owningBucketOrds) throws IOException {
        return buildAggregationsForSingleBucket(owningBucketOrds, (owningBucketOrd, subAggregationResults) ->
            new InternalReverseNested(name, bucketDocCount(owningBucketOrd), subAggregationResults, metadata()));
    }

    @Override
    public InternalAggregation buildEmptyAggregation() {
        return new InternalReverseNested(name, 0, buildEmptySubAggregations(), metadata());
    }

    Query getParentFilter() {
        return parentFilter;
    }
}

```

首先没有定义类内方法 他的构造方法是public的，且需要判断objectMapper，如果objectMapper为空 则新建一个父filter是为非Nested方法的Filter，否则就调用同objectMapper的nestedTypeFilter

最后调用context的chache方法返回的BitFilterCache对象，去获得创造着BitSetProducer对象


在来看getLeafCollector 
首先是非空判断，在ES中，如果删除了父文档，那么子文档也将被删除，因此该agg收到的子文档必须属于有效的父文档。 因此，acceptableDocs在此处可以为空。
如果parentDocs为空则返回LeafBucketCollector.NO_OP_COLLECTOR;

同样的初始化一个LongIntHashMap对象，初始空间为32位，
```
  public LongIntHashMap(int expectedElements, double loadFactor, HashOrderMixingStrategy orderMixer) {
    this.orderMixer = orderMixer;
    this.loadFactor = verifyLoadFactor(loadFactor);
    ensureCapacity(expectedElements);
  }

```
最后同样重载LeafBucketCollectorBase中的collect方法
用于快进检索此childDoc所属的parentDoc

需要保证 assert childDoc <= parentDoc && parentDoc != DocIdSetIterator.NO_MORE_DOCS;

将bucket作为数值，获取keyslot
解析来开始判断keyslot是否在hashmap中存在 
不存在就写入
存在如果当前的docid比之前的大 则进行搜集 写入 缓存并进行替代
```
    public final void collectBucket(LeafBucketCollector subCollector, int doc, long bucketOrd) throws IOException {
        grow(bucketOrd + 1);
        collectExistingBucket(subCollector, doc, bucketOrd);
    }
```