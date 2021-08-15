# Search流程

在协调节点，搜索任务被分解成两部分，也就是query &fetch
每个分片都要参与搜索，通过协调节点把结果进行合并，再根据ID获取文档内容。
常见的搜索结构如下
结果都在hits里面，took表示耗费的时间，total命中文档的数量，max_source 文档匹配度，hits结果列表

## 索引和搜索

数据可以分为两类：精确数据和全文检索

查询时不同的，对于精确值得比较是二进制的，例如BinaryRange，查询要么匹配要么不匹配，儿全文检索的只能找到相似度较大得。


**原始数据**

数值 -->  进行正排和倒排
全文 --> 正排和倒排得通是，进入分析器
 
**分析器**

字符过滤器 , & -> and

分词器 Tokenizer 分词器去除标点，空客输出token

语言处理组件 过滤器小写 去掉a and the 等停用词


**检索语句**

检测字段类型

全文：WS -> 词条列表 Token 查找匹配文档

全文检索 通过分析器 得到 倒排索引

从而开始查找匹配为文档 

精确值 查找

计算相关性 以及匹配度

### 建立索引

分析器：

1. 字符过滤器 ，对字符串进行预处理，去掉HTML，将符号进行转换， &->and
2. 分词器。将字符串分割为单个词条 根据空格和标点符号分割，输出的词条也就是Token， 也就是lucene-token调用链
3. 语言处理。对于上一步得到的Token做一些语言相关的处理。

分析后，将分析器输出的词 Term 传递给索引组件，生成倒排和正排索引，在存储到文件系统中

### 执行搜索

搜索调用Lucene完成，如果是全文检索

1. 对检索之端使用建立索引时相同的分析器进行分析，产生Token列表
2. 根据查询语句的语法规则转换成一颗语法树
3. 查找符合语法树的文档
4. 对匹配到的文档列表进行相关性评分，一般使用TF/IDF方法
5. 根据评分结果进行排序


## 搜索类型

两种

DFS_QUERY_THEN_FETCH
QUERY_THEN_FETCH

区别在于查询节点，DFS查询阶段的流程要多一些，使用全局信息来获取更准确的评分

## 分布式搜索过程

流程

**协调节点**

1. 将请求体解析为SearchRequest
2. 构造目的shard列表
3. 遍历shard列表，并行发送Query --异步请求-->

**数据节点**

4. 接收到异步请求 相应Query
5. 放入Cache模块
6. 执行queryPhase.execute任务
   1. 调用Lucene
   2. rescorePhase
   3. suggestPhase
   4. aggregationPhase
7. sendResponse -->异步请求发送到协调节点

**协调节点**

8. 收到Query Response和合并结果
9. 等待所有的Response

-------------------------query/fetch--------------------------------


**协调节点**

1. 遍历非空shard列表并行发送Fetch--异步请求-->

**数据节点**

1. 响应Fetch
2. fetchPhase.execute
3. sendResponse --异步请求-->

**协调节点**

1. --异步请求--> ExpandSearchPhase
2. 回复客户端


### 协调节点流程

两阶段得相应得实现位置

Query阶段——search.InitialSearchPhase
Fetch阶段——search.FetchSearchPhase


Query阶段

在初始查询阶段，查询会广播到索引中的每个分片副本。每个分片在本地执行搜索并构建一个匹配文档的优先队列。

优先队列是一个存有topN匹配文档的有序队列。优先队列大小参数为 from+size

QUERY_THEM_FETCH 查询节点步骤

1. 客户端发送search请求到分节点 node3
2. node3 将查询请求转发到索引的每个主分片或副分片
3. 每个分片在本地执行查询，并使用本地的Term/Document Frequency 信息进行打分，添加结果到大小为from+size的本地有序优先队列中。
4. 每个分片返回各自的优先队列中的所有文档的ID和排序值给协调节点，协调节点合并数值到自己的优先队列，产生一个全局排序后的列表。


协调节点广播查询请求到所有相关分片，无论主分片还是副分片，协调节点将在之后的请求中轮询所有的分片进行负载均衡。

查询阶段不会对请求的内容进行解析，无论搜索什么内容，只看本次需要命中那些，征对特定shard选择副本，转发所有请求。


Query阶段源码分析

线程池：http_server_work

1. 解析请求
   RestSearchAction.prepareRequest 方法中将请求体解析为SearchRequst数据结构
```java
@Override
public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        IntConsumer setSize = size -> searchRequest.source().size(size);
        request.withContentOrSourceParamParserOrNull(parser ->
            parseSearchRequest(searchRequest, request, parser, setSize));

        return channel -> {
            RestCancellableNodeClient cancelClient = new RestCancellableNodeClient(client, request.getHttpChannel());
            cancelClient.execute(SearchAction.INSTANCE, searchRequest, new RestStatusToXContentListener<>(channel));
        };
    }
```
2. 构造目的shard列表
将请求设计的本集群shard列表和远程集群的shard列表合并，这样的类一般都在Transport中 也就是TransportSearchAction

```java
package org.elasticsearch.action.search;
 private void executeSearch(SearchTask task, SearchTimeProvider timeProvider, SearchRequest searchRequest,
                               OriginalIndices localIndices, List<SearchShardIterator> remoteShardIterators,
                               BiFunction<String, String, DiscoveryNode> remoteConnections, ClusterState clusterState,
                               Map<String, AliasFilter> remoteAliasMap, ActionListener<SearchResponse> listener,
                               SearchResponse.Clusters clusters) {

        clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.READ);
        // TODO: I think startTime() should become part of ActionRequest and that should be used both for index name
        // date math expressions and $now in scripts. This way all apis will deal with now in the same way instead
        // of just for the _search api
        final Index[] indices = resolveLocalIndices(localIndices, clusterState, timeProvider);
        Map<String, AliasFilter> aliasFilter = buildPerIndexAliasFilter(searchRequest, clusterState, indices, remoteAliasMap);
        Map<String, Set<String>> routingMap = indexNameExpressionResolver.resolveSearchRouting(clusterState, searchRequest.routing(),
            searchRequest.indices());
        routingMap = routingMap == null ? Collections.emptyMap() : Collections.unmodifiableMap(routingMap);
        String[] concreteIndices = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            concreteIndices[i] = indices[i].getName();
        }
        Map<String, Long> nodeSearchCounts = searchTransportService.getPendingSearchRequests();
        GroupShardsIterator<ShardIterator> localShardsIterator = clusterService.operationRouting().searchShards(clusterState,
                concreteIndices, routingMap, searchRequest.preference(), searchService.getResponseCollectorService(), nodeSearchCounts);
        GroupShardsIterator<SearchShardIterator> shardIterators = mergeShardsIterators(localShardsIterator, localIndices,
            searchRequest.getLocalClusterAlias(), remoteShardIterators);

        failIfOverShardCountLimit(clusterService, shardIterators.size());

        Map<String, Float> concreteIndexBoosts = resolveIndexBoosts(searchRequest, clusterState);

        // optimize search type for cases where there is only one shard group to search on
        if (shardIterators.size() == 1) {
            // if we only have one group, then we always want Q_T_F, no need for DFS, and no need to do THEN since we hit one shard
            searchRequest.searchType(QUERY_THEN_FETCH);
        }
        if (searchRequest.allowPartialSearchResults() == null) {
           // No user preference defined in search request - apply cluster service default
            searchRequest.allowPartialSearchResults(searchService.defaultAllowPartialSearchResults());
        }
        if (searchRequest.isSuggestOnly()) {
            // disable request cache if we have only suggest
            searchRequest.requestCache(false);
            switch (searchRequest.searchType()) {
                case DFS_QUERY_THEN_FETCH:
                    // convert to Q_T_F if we have only suggest
                    searchRequest.searchType(QUERY_THEN_FETCH);
                    break;
            }
        }

        final DiscoveryNodes nodes = clusterState.nodes();
        BiFunction<String, String, Transport.Connection> connectionLookup = buildConnectionLookup(searchRequest.getLocalClusterAlias(),
            nodes::get, remoteConnections, searchTransportService::getConnection);
        boolean preFilterSearchShards = shouldPreFilterSearchShards(clusterState, searchRequest, indices, shardIterators.size());
        searchAsyncAction(task, searchRequest, shardIterators, timeProvider, connectionLookup, clusterState,
            Collections.unmodifiableMap(aliasFilter), concreteIndexBoosts, routingMap, listener, preFilterSearchShards, clusters).start();
    }
```
 
3. 遍历所有shard发送请求

**package org.elasticsearch.action.search;**

请求是基于shard遍历的，如果由N个shard位于同一节点，发送N次请求，并不会把请求合并为一个 位于AbstractSearchAsyncAction
   
```java
   @Override
    public final void run() {
        for (final SearchShardIterator iterator : toSkipShardsIts) {
            assert iterator.skip();
            skipShard(iterator);
        }
        if (shardsIts.size() > 0) {
            assert request.allowPartialSearchResults() != null : "SearchRequest missing setting for allowPartialSearchResults";
            if (request.allowPartialSearchResults() == false) {
                final StringBuilder missingShards = new StringBuilder();
                // Fail-fast verification of all shards being available
                for (int index = 0; index < shardsIts.size(); index++) {
                    final SearchShardIterator shardRoutings = shardsIts.get(index);
                    if (shardRoutings.size() == 0) {
                        if(missingShards.length() > 0){
                            missingShards.append(", ");
                        }
                        missingShards.append(shardRoutings.shardId());
                    }
                }
                if (missingShards.length() > 0) {
                    //Status red - shard is missing all copies and would produce partial results for an index search
                    final String msg = "Search rejected due to missing shards ["+ missingShards +
                        "]. Consider using `allow_partial_search_results` setting to bypass this error.";
                    throw new SearchPhaseExecutionException(getName(), msg, null, ShardSearchFailure.EMPTY_ARRAY);
                }
            }
            for (int index = 0; index < shardsIts.size(); index++) {
                final SearchShardIterator shardRoutings = shardsIts.get(index);
                assert shardRoutings.skip() == false;
                performPhaseOnShard(index, shardRoutings, shardRoutings.nextOrNull());
            }
        }
    }

   ```
   shardsIts为本次搜索涉及的所有分片，shardRoutings.nextOrNull()从某个个分片的所有副本中选择一个 AbstractSearchAsyncAction
   ```java
    executePhaseOnShard(shardIt, shard,
                        new SearchActionListener<Result>(shardIt.newSearchShardTarget(shard.currentNodeId()), shardIndex) {
                            @Override
                            public void innerOnResponse(Result result) {
                                try {
                                    onShardResult(result, shardIt);
                                } finally {
                                    executeNext(pendingExecutions, thread);
                                }
                            }

                            @Override
                            public void onFailure(Exception t) {
                                try {
                                    onShardFailure(shardIndex, shard, shard.currentNodeId(), shardIt, t);
                                } finally {
                                    executeNext(pendingExecutions, thread);
                                }
                            }
                        });
   ```

1. 收集返回结果
   本过程在search线程池中执行
    ```java
     protected void onShardResult(Result result, SearchShardIterator shardIt) {
        assert result.getShardIndex() != -1 : "shard index is not set";
        assert result.getSearchShardTarget() != null : "search shard target must not be null";
        successfulOps.incrementAndGet();
        results.consumeResult(result);
        hasShardResponse.set(true);
        if (logger.isTraceEnabled()) {
            logger.trace("got first-phase result from {}", result != null ? result.getSearchShardTarget() : null);
        }
        // clean a previous error on this shard group (note, this code will be serialized on the same shardIndex value level
        // so its ok concurrency wise to miss potentially the shard failures being created because of another failure
        // in the #addShardFailure, because by definition, it will happen on *another* shardIndex
        AtomicArray<ShardSearchFailure> shardFailures = this.shardFailures.get();
        if (shardFailures != null) {
            shardFailures.set(result.getShardIndex(), null);
        }
        // we need to increment successful ops first before we compare the exit condition otherwise if we
        // are fast we could concurrently update totalOps but then preempt one of the threads which can
        // cause the successor to read a wrong value from successfulOps if second phase is very fast ie. count etc.
        // increment all the "future" shards to update the total ops since we some may work and some may not...
        // and when that happens, we break on total ops, so we must maintain them
        successfulShardExecution(shardIt);
    }
    ```
   onShardSuccess对收集到的结果进行合并，调用successfulShardExecution方法检查所有请求都已收到回复，是否进入下一阶段
   ```java
   private void successfulShardExecution(SearchShardIterator shardsIt) {
        final int remainingOpsOnIterator;
        if (shardsIt.skip()) {
            remainingOpsOnIterator = shardsIt.remaining();
        } else {
            remainingOpsOnIterator = shardsIt.remaining() + 1;
        }
        final int xTotalOps = totalOps.addAndGet(remainingOpsOnIterator);
        if (xTotalOps == expectedTotalOps) {
            onPhaseDone();//调用executeNextPhase，从而开始执行取回阶段
        } else if (xTotalOps > expectedTotalOps) {
            throw new AssertionError("unexpected higher total ops [" + xTotalOps + "] compared to expected ["
                + expectedTotalOps + "]");
        }
    }
   ```

**Fetch阶段**

Query阶段知道了要取哪些数据，但是并不知道具体的数据

步骤如下：

1. 协调节点向相关Node发送GET请求
2. 分片所在节点向协调节点返回数据
3. 协调节点等待所有文档被取回，然后返回给客户端

分片所在节点在返回文档数据时候，处理有可能出现的_source字段和高亮参数

协调节点首先决定那些文档 确实需要被取回。

为了避免在协调节点中创建的number_of_shards*(from+size)优先队列过大，应尽量控制分页深度

源码解析

通过文档ID获取完整的文档内容。

执行线程池:search

1. 发送Fetch请求

onPhaseDone();//调用executeNextPhase，从而开始执行取回阶段

调用链 :successfulShardExecution()-->onPhaseDone()-->executeNextPhase()-->executePhase()-->phase.run()-->doRun()-->innerRun();
Query阶段的executeNextPhase方法触发Fetch阶段，Fetch阶段的起点是FetchSearchPhase.innerRun函数，从查询阶段的shard列表中遍历，跳过查询结果为空的shard，对特定目标shard执行executeFetch来获取数据，其中包括分页信息。对scroll请求的处理也在FetchSearchPhase.innerRun函数中
```java
    private void innerRun() throws IOException {
        final int numShards = context.getNumShards();
        final boolean isScrollSearch = context.getRequest().scroll() != null;
        final List<SearchPhaseResult> phaseResults = queryResults.asList();
        final String scrollId;
        if (isScrollSearch) {
            final boolean includeContextUUID = clusterState.nodes().getMinNodeVersion().onOrAfter(Version.V_7_7_0);
            scrollId = TransportSearchHelper.buildScrollId(queryResults, includeContextUUID);
        } else {
            scrollId = null;
        }
        final SearchPhaseController.ReducedQueryPhase reducedQueryPhase = resultConsumer.reduce();
        final boolean queryAndFetchOptimization = queryResults.length() == 1;
        final Runnable finishPhase = ()
            -> moveToNextPhase(searchPhaseController, scrollId, reducedQueryPhase, queryAndFetchOptimization ?
            queryResults : fetchResults);
        if (queryAndFetchOptimization) {
            assert phaseResults.isEmpty() || phaseResults.get(0).fetchResult() != null : "phaseResults empty [" + phaseResults.isEmpty()
                + "], single result: " +  phaseResults.get(0).fetchResult();
            // query AND fetch optimization
            finishPhase.run();
        } else {
            ScoreDoc[] scoreDocs = reducedQueryPhase.sortedTopDocs.scoreDocs;
            final IntArrayList[] docIdsToLoad = searchPhaseController.fillDocIdsToLoad(numShards, scoreDocs);
            // no docs to fetch -- sidestep everything and return
            if (scoreDocs.length == 0) {
                // we have to release contexts here to free up resources
                phaseResults.stream()
                    .map(SearchPhaseResult::queryResult)
                    .forEach(this::releaseIrrelevantSearchContext);
                finishPhase.run();
            } else {
                final ScoreDoc[] lastEmittedDocPerShard = isScrollSearch ?
                    searchPhaseController.getLastEmittedDocPerShard(reducedQueryPhase, numShards)
                    : null;
                final CountedCollector<FetchSearchResult> counter = new CountedCollector<>(r -> fetchResults.set(r.getShardIndex(), r),
                    docIdsToLoad.length, // we count down every shard in the result no matter if we got any results or not
                    finishPhase, context);
                for (int i = 0; i < docIdsToLoad.length; i++) {
                    IntArrayList entry = docIdsToLoad[i];
                    SearchPhaseResult queryResult = queryResults.get(i);
                    if (entry == null) { // no results for this shard ID
                        if (queryResult != null) {
                            // if we got some hits from this shard we have to release the context there
                            // we do this as we go since it will free up resources and passing on the request on the
                            // transport layer is cheap.
                            releaseIrrelevantSearchContext(queryResult.queryResult());
                            progressListener.notifyFetchResult(i);
                        }
                        // in any case we count down this result since we don't talk to this shard anymore
                        counter.countDown();
                    } else {
                        SearchShardTarget searchShardTarget = queryResult.getSearchShardTarget();
                        Transport.Connection connection = context.getConnection(searchShardTarget.getClusterAlias(),
                            searchShardTarget.getNodeId());
                        ShardFetchSearchRequest fetchSearchRequest = createFetchRequest(queryResult.queryResult().getContextId(), i, entry,
                            lastEmittedDocPerShard, searchShardTarget.getOriginalIndices());
                        executeFetch(i, searchShardTarget, counter, fetchSearchRequest, queryResult.queryResult(),
                            connection);
                    }
                }
            }
        }
    }

```
executeFetch的主要实现：

```java
private void executeFetch(final int shardIndex, final SearchShardTarget shardTarget,
                              final CountedCollector<FetchSearchResult> counter,
                              final ShardFetchSearchRequest fetchSearchRequest, final QuerySearchResult querySearchResult,
                              final Transport.Connection connection) {
        context.getSearchTransport().sendExecuteFetch(connection, fetchSearchRequest, context.getTask(),
            new SearchActionListener<FetchSearchResult>(shardTarget, shardIndex) {
                @Override
                public void innerOnResponse(FetchSearchResult result) {
                    try {
                        progressListener.notifyFetchResult(shardIndex);
                        counter.onResult(result);
                    } catch (Exception e) {
                        context.onPhaseFailure(FetchSearchPhase.this, "", e);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    try {
                        logger.debug(
                            () -> new ParameterizedMessage("[{}] Failed to execute fetch phase", fetchSearchRequest.contextId()), e);
                        progressListener.notifyFetchFailure(shardIndex, shardTarget, e);
                        counter.onFailure(shardIndex, shardTarget, e);
                    } finally {
                        // the search context might not be cleared on the node where the fetch was executed for example
                        // because the action was rejected by the thread pool. in this case we need to send a dedicated
                        // request to clear the search context.
                        releaseIrrelevantSearchContext(querySearchResult);
                    }
                }
            });
    }
```

QuerySearchResult 对象中包含分页信息，最后定义一个Listener，没成功获取一个shard数据后执行counter.onResult,其中调用对结果的处理回调，把result保存到数组中，之后执行countdown
```java
    void countDown() {
        assert counter.isCountedDown() == false : "more operations executed than specified";
        if (counter.countDown()) {
            onFinish.run();
        }
    }

    /**
     * Sets the result to the given array index and then runs {@link #countDown()}
     */
    void onResult(R result) {
        try {
            resultConsumer.accept(result);
        } finally {
            countDown();
        }
    }
```
2. 收集结果
    收集器在FetchSearchPhase类中innerRun中定义，包括shard数据的存放和收集，如何处理，谁来处理
    
```
final CountedCollector<FetchSearchResult> counter = new CountedCollector<>(r -> fetchResults.set(r.getShardIndex(), r),
                    docIdsToLoad.length, // we count down every shard in the result no matter if we got any results or not
                    finishPhase, context);
```
这里有一点 每一个都要减去，就算里面没有，因为这样子也是一种纠错

fetchResults用于存储从某个shard收集到的结果，每接收到一个shard数据就执行一次counter.counterDown。到最后 countDown开始出发OnFinish任务 如上所示。
```
 final Runnable finishPhase = ()
            -> moveToNextPhase(searchPhaseController, scrollId, reducedQueryPhase, queryAndFetchOptimization ?
            queryResults : fetchResults);
```

moveToNextPhase方法会执行下一阶段，下一阶段要执行的任务定义在FetchSearchPhase构造函数中，触发ExpandSearchPhase

```java
    private void moveToNextPhase(SearchPhaseController searchPhaseController,
                                 String scrollId, SearchPhaseController.ReducedQueryPhase reducedQueryPhase,
                                 AtomicArray<? extends SearchPhaseResult> fetchResultsArr) {
        final InternalSearchResponse internalResponse = searchPhaseController.merge(context.getRequest().scroll() != null,
            reducedQueryPhase, fetchResultsArr.asList(), fetchResultsArr::get);
        context.executeNextPhase(this, nextPhaseFactory.apply(internalResponse, scrollId));
    }
```

调用了AbstractSearchAsyncAction 中的executeNextPhase 方法

```java
        FetchSearchPhase(SearchPhaseResults<SearchPhaseResult> resultConsumer,
                     SearchPhaseController searchPhaseController,
                     SearchPhaseContext context,
                     ClusterState clusterState) {
        this(resultConsumer, searchPhaseController, context, clusterState,
            (response, scrollId) -> new ExpandSearchPhase(context, response, scrollId));
    }
```

3. ExpandSearchPhase 这个是一个继承Executor的类，也就是线程池对象，
   该对象执行run方法，判断是否启用一个字段折叠功能。
4. ExpandSearchPhase执行完之后，在sendResponsePhase方法中实现
```java
    protected SearchPhase sendResponsePhase(SearchPhaseController.ReducedQueryPhase queryPhase,
                                            final AtomicArray<? extends SearchPhaseResult> fetchResults) {
        return new SearchPhase("fetch") {
            @Override
            public void run() throws IOException {
                sendResponse(queryPhase, fetchResults);
            }
        };
    }
        protected final void sendResponse(SearchPhaseController.ReducedQueryPhase queryPhase,
                                      final AtomicArray<? extends SearchPhaseResult> fetchResults) {
        try {
            final InternalSearchResponse internalResponse = searchPhaseController.merge(true, queryPhase, fetchResults.asList(),
                fetchResults::get);
            // the scroll ID never changes we always return the same ID. This ID contains all the shards and their context ids
            // such that we can talk to them again in the next roundtrip.
            String scrollId = null;
            if (request.scroll() != null) {
                scrollId = request.scrollId();
            }
            listener.onResponse(new SearchResponse(internalResponse, scrollId, this.scrollId.getContext().length, successfulOps.get(),
                0, buildTookInMillis(), buildShardFailures(), SearchResponse.Clusters.EMPTY));
        } catch (Exception e) {
            listener.onFailure(new ReduceSearchPhaseException("fetch", "inner finish failed", e, buildShardFailures()));
        }
    }
```
和ES6.4相比 相应的方法有所变更，添加了Listener方法用于处理各种各样的请求

### 执行搜索的数据节点流程

线程池 search
处理入口注册于 SearchTransportService.registerRequestHandler.

响应Query请求
对于常见的Query请求，其action为
```java
indices:data/read/search[phase/query]
```
QUERY_ACTION_NAME = "indices:data/read/search[phase/query]";
主要的代码如下：
```java
public static void registerRequestHandler(TransportService transportService, SearchService searchService) {
        transportService.registerRequestHandler(FREE_CONTEXT_SCROLL_ACTION_NAME, ThreadPool.Names.SAME, ScrollFreeContextRequest::new,
            (request, channel, task) -> {
                boolean freed = searchService.freeContext(request.id());
                channel.sendResponse(new SearchFreeContextResponse(freed));
        });
        TransportActionProxy.registerProxyAction(transportService, FREE_CONTEXT_SCROLL_ACTION_NAME, SearchFreeContextResponse::new);
        transportService.registerRequestHandler(FREE_CONTEXT_ACTION_NAME, ThreadPool.Names.SAME, SearchFreeContextRequest::new,
            (request, channel, task) -> {
                boolean freed = searchService.freeContext(request.id());
                channel.sendResponse(new SearchFreeContextResponse(freed));
        });
        TransportActionProxy.registerProxyAction(transportService, FREE_CONTEXT_ACTION_NAME, SearchFreeContextResponse::new);
        transportService.registerRequestHandler(CLEAR_SCROLL_CONTEXTS_ACTION_NAME, ThreadPool.Names.SAME,
            TransportRequest.Empty::new,
            (request, channel, task) -> {
                searchService.freeAllScrollContexts();
                channel.sendResponse(TransportResponse.Empty.INSTANCE);
        });
        TransportActionProxy.registerProxyAction(transportService, CLEAR_SCROLL_CONTEXTS_ACTION_NAME,
            (in) -> TransportResponse.Empty.INSTANCE);

        transportService.registerRequestHandler(DFS_ACTION_NAME, ThreadPool.Names.SAME, ShardSearchRequest::new,
            (request, channel, task) ->
                searchService.executeDfsPhase(request, (SearchShardTask) task,
                    new ChannelActionListener<>(channel, DFS_ACTION_NAME, request))
        );

        TransportActionProxy.registerProxyAction(transportService, DFS_ACTION_NAME, DfsSearchResult::new);

        transportService.registerRequestHandler(QUERY_ACTION_NAME, ThreadPool.Names.SAME, ShardSearchRequest::new,
            (request, channel, task) -> {
                searchService.executeQueryPhase(request, (SearchShardTask) task,
                    new ChannelActionListener<>(channel, QUERY_ACTION_NAME, request));
            });
        TransportActionProxy.registerProxyActionWithDynamicResponseType(transportService, QUERY_ACTION_NAME,
            (request) -> ((ShardSearchRequest)request).numberOfShards() == 1 ? QueryFetchSearchResult::new : QuerySearchResult::new);

        transportService.registerRequestHandler(QUERY_ID_ACTION_NAME, ThreadPool.Names.SAME, QuerySearchRequest::new,
            (request, channel, task) -> {
                searchService.executeQueryPhase(request, (SearchShardTask) task,
                    new ChannelActionListener<>(channel, QUERY_ID_ACTION_NAME, request));
            });
        TransportActionProxy.registerProxyAction(transportService, QUERY_ID_ACTION_NAME, QuerySearchResult::new);

        transportService.registerRequestHandler(QUERY_SCROLL_ACTION_NAME, ThreadPool.Names.SAME, InternalScrollSearchRequest::new,
            (request, channel, task) -> {
                searchService.executeQueryPhase(request, (SearchShardTask) task,
                    new ChannelActionListener<>(channel, QUERY_SCROLL_ACTION_NAME, request));
            });
        TransportActionProxy.registerProxyAction(transportService, QUERY_SCROLL_ACTION_NAME, ScrollQuerySearchResult::new);

        transportService.registerRequestHandler(QUERY_FETCH_SCROLL_ACTION_NAME, ThreadPool.Names.SAME, InternalScrollSearchRequest::new,
            (request, channel, task) -> {
                searchService.executeFetchPhase(request, (SearchShardTask) task,
                    new ChannelActionListener<>(channel, QUERY_FETCH_SCROLL_ACTION_NAME, request));
            });
        TransportActionProxy.registerProxyAction(transportService, QUERY_FETCH_SCROLL_ACTION_NAME, ScrollQueryFetchSearchResult::new);

        transportService.registerRequestHandler(FETCH_ID_SCROLL_ACTION_NAME, ThreadPool.Names.SAME, ShardFetchRequest::new,
            (request, channel, task) -> {
                searchService.executeFetchPhase(request, (SearchShardTask) task,
                    new ChannelActionListener<>(channel, FETCH_ID_SCROLL_ACTION_NAME, request));
            });
        TransportActionProxy.registerProxyAction(transportService, FETCH_ID_SCROLL_ACTION_NAME, FetchSearchResult::new);

        transportService.registerRequestHandler(FETCH_ID_ACTION_NAME, ThreadPool.Names.SAME, true, true, ShardFetchSearchRequest::new,
            (request, channel, task) -> {
                searchService.executeFetchPhase(request, (SearchShardTask) task,
                    new ChannelActionListener<>(channel, FETCH_ID_ACTION_NAME, request));
            });
        TransportActionProxy.registerProxyAction(transportService, FETCH_ID_ACTION_NAME, FetchSearchResult::new);

        // this is cheap, it does not fetch during the rewrite phase, so we can let it quickly execute on a networking thread
        transportService.registerRequestHandler(QUERY_CAN_MATCH_NAME, ThreadPool.Names.SAME, ShardSearchRequest::new,
            (request, channel, task) -> {
                searchService.canMatch(request, new ChannelActionListener<>(channel, QUERY_CAN_MATCH_NAME, request));
            });
        TransportActionProxy.registerProxyAction(transportService, QUERY_CAN_MATCH_NAME, SearchService.CanMatchResponse::new);
    }
```
在顶层采用代理模式进行过滤底层跳到了tranpostService去进行相应的线程池注册

```java
    /**
     * Registers a new request handler
     *
     * @param action         The action the request handler is associated with
     * @param requestReader  a callable to be used construct new instances for streaming
     * @param executor       The executor the request handling will be executed on
     * @param handler        The handler itself that implements the request handling
     */
    public <Request extends TransportRequest> void registerRequestHandler(String action, String executor,
                                                                          Writeable.Reader<Request> requestReader,
                                                                          TransportRequestHandler<Request> handler) {
        validateActionName(action);
        handler = interceptor.interceptHandler(action, executor, false, handler);
        RequestHandlerRegistry<Request> reg = new RequestHandlerRegistry<>(
            action, requestReader, taskManager, handler, executor, false, true);
        transport.registerRequestHandler(reg);
    }

    /**
     * Registers a new request handler
     *
     * @param action                The action the request handler is associated with
     * @param requestReader               The request class that will be used to construct new instances for streaming
     * @param executor              The executor the request handling will be executed on
     * @param forceExecution        Force execution on the executor queue and never reject it
     * @param canTripCircuitBreaker Check the request size and raise an exception in case the limit is breached.
     * @param handler               The handler itself that implements the request handling
     */
    public <Request extends TransportRequest> void registerRequestHandler(String action,
                                                                          String executor, boolean forceExecution,
                                                                          boolean canTripCircuitBreaker,
                                                                          Writeable.Reader<Request> requestReader,
                                                                          TransportRequestHandler<Request> handler) {
        validateActionName(action);
        handler = interceptor.interceptHandler(action, executor, forceExecution, handler);
        RequestHandlerRegistry<Request> reg = new RequestHandlerRegistry<>(
            action, requestReader, taskManager, handler, executor, forceExecution, canTripCircuitBreaker);
        transport.registerRequestHandler(reg);
    }

```

其中的需要去验证action是否有效，然后创建注册类，调用线程池，向下调用transport的registerRequestHandler方法。返回的是TransportMessageListener listener方法 是基于Request的基类。 完成注册后 通过TransportChannel类 进行发送请求
```java

        @Override
        public void sendResponse(TransportResponse response) throws IOException {
            service.onResponseSent(requestId, action, response);
            final TransportResponseHandler handler = service.responseHandlers.onResponseReceived(requestId, service);
            // ignore if its null, the service logs it
            if (handler != null) {
                final String executor = handler.executor();
                if (ThreadPool.Names.SAME.equals(executor)) {
                    processResponse(handler, response);
                } else {
                    threadPool.executor(executor).execute(new Runnable() {
                        @Override
                        public void run() {
                            processResponse(handler, response);
                        }

                        @Override
                        public String toString() {
                            return "delivery of response to [" + requestId + "][" + action + "]: " + response;
                        }
                    });
                }
            }
        }
```
这里有多态，采用DirectResponseChannel类的该方法进行讨论，可以清楚的看到线程池的调用。
```java
        @SuppressWarnings("unchecked")
        protected void processResponse(TransportResponseHandler handler, TransportResponse response) {
            try {
                handler.handleResponse(response);
            } catch (Exception e) {
                processException(handler, wrapInRemote(new ResponseHandlerFailureTransportException(e)));
            }
        }
```

再来看实现查询路口 
查询接口的实在searchService.executeQueryPhase中。查询时看是否允许使用cache，由以下配置决定
index.requests.cache.enable

默认为true，会把查询结果放入cache，查询有先从cache中获取。cache由节点的所有分片共享，基于LRU算法实现。

核心的查询封装在queryPhase.execute(context),其中调用Lucene实现检索，同时实现聚合：
```
 @Override
    public void execute(SearchContext searchContext) throws QueryPhaseExecutionException {
        if (searchContext.hasOnlySuggest()) {
            suggestPhase.execute(searchContext);
            searchContext.queryResult().topDocs(new TopDocsAndMaxScore(
                    new TopDocs(new TotalHits(0, TotalHits.Relation.EQUAL_TO), Lucene.EMPTY_SCORE_DOCS), Float.NaN),
                new DocValueFormat[0]);
            return;
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("{}", new SearchContextSourcePrinter(searchContext));
        }

        // Pre-process aggregations as late as possible. In the case of a DFS_Q_T_F
        // request, preProcess is called on the DFS phase phase, this is why we pre-process them
        // here to make sure it happens during the QUERY phase
        aggregationPhase.preProcess(searchContext);
        boolean rescore = executeInternal(searchContext);

        if (rescore) { // only if we do a regular search
            rescorePhase.execute(searchContext);
        }
        suggestPhase.execute(searchContext);
        aggregationPhase.execute(searchContext);

        if (searchContext.getProfilers() != null) {
            ProfileShardResult shardResults = SearchProfileShardResults
                .buildShardResults(searchContext.getProfilers());
            searchContext.queryResult().profileResults(shardResults);
        }
    }

```

核心功能块

execute() 调用Lucene searcher.search()实现

rescorePhase 全文检索并打分

suggestPhase 自动补全和纠错

aggregationPhase 实现聚合

总结：

慢查询Query日志的统计时间在于本阶段的处理时间

聚合操作在本阶段实现，在Lucene检索后完成

**响应Fetch请求**

执行Fetch，发送Response

也是基于registerRequestHandler，对于Fetch的响应的实现。封装在searchService.executeFetchPhase中，核心是调用fetchPhase.execute().按照评分的doc取得相关数据，填充到SearchHits，封装到FetchSearchResult中

总结：

**慢查询Query日志的统计时间在于本阶段的处理时间**


## 小结

聚合在ES中实现，而非Lucene

除了scroll这种关乎上下文的方式，Query和Fetch请求之间无状态

分页搜索不会单独cache，cache和分页无关

每一次的分页都是一次重新搜索的过程，分页深会导致效率慢

搜索会遍历分片上所有的Lucene，因此合并对搜索性能的提升是有好处的。
