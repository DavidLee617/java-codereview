# GET流程
ES的读取分为GET和Search两种操作，GET/MGET必须指定三元组
**{_index,_type,_id}**.根据文档id从正排索引中获取内容。

## 可选参数

|参数|简介|
|--|--|
|realtime|默认为true。不受索引刷新频率设置的影响。若文档已经更新，但未书信，会发出一个刷新调用，使文档可见|
|source filtering|返回文档的全部内容在source字段中。可以设置为false，不返回文档内容。可以使用include和exclude过滤返回原始文档的部分字段|
|stored Fields|store 设置为true的字段，本选项用来指定返回的那些字段|
|_source|通过/{index}/{type}/{id}/_source，只返回原始文档内容，其他的id等元信息不返回|
|routing|自定义routing|
|preference|从分片的多个副本中随机选择一个，通过指定优先级 (preference）可以选择从主分片读取，或者尝试从本地读取|
|refresh|默认为false，若设置refresh为true，则可以在读取之前先执行刷新操作|
|version|指定版本号，当实际版本号与请求不符时，ES返回409|

## GET流程

搜索和读取文档都属于度操作，可以从主分片或者副分片读取数据

步骤：
1. 客户端向master发送读请求
2. master使用文档ID来确定文档属于哪个分片，它通过内容路由表获知分片0有三个副本数据，位于所有的三个节点中，可以将请求发送到任意节点。
3. 副节点中的副分片将文档返回给Master，master将文档返回给客户端

master节点会将请求进行轮训发送到集群的所有副本来实现负载均衡

## GET消息分析

协调节点

1. 获取集群状态
2. 计算目标shard列表
3. 目标activeshard迭代器 ActiveShardCount
4. sendRequest
5. 目标node 是否为本地
   1. 是 sendLocalRequest -->6
   2. 否 transport.sendRequest
数据节点
6. processMessageReceived
7. 是否刷盘
   1. 是 刷新shard -->8
   2. 否 -->8
8. getFromSearcher
9. Engine::getResult package org.elasticsearch.index.engine;
10. 过滤_source
11. GetResult
12. 是否成功
    1.  是 回复客户端
    2.  否 onFailure 下一个 --> 3

### 协调节点
执行本流程的线程池 ： http_server_worker
**TransportSingleShardAction** 类 用于处理存在于一个单个主分片上的读请求。将请求转发到目标节点，请求执行失败，则尝试转发到其他节点读取。收到读请求后，处理过程如下

**内容路由**

1. 在**TransportSingleShardAction::AsyncSingleAction**的构造函数 AsyncSingleAction中，准备集群状态，节点列表等信息
2. 根据内容路由算法计算目标shardid，也就是文档该落在哪个分片
3. 计算出目标shardid后，结合请求参数指定的优先级和集群状态确定目标节点，由于分片可能存在多个副本，计算出一个列表

```
private AsyncSingleAction(Request request, ActionListener<Response> listener) {
            this.listener = listener;

            ClusterState clusterState = clusterService.state();
            if (logger.isTraceEnabled()) {
                logger.trace("executing [{}] based on cluster state version [{}]", request, clusterState.version());
            }
            nodes = clusterState.nodes();
            ClusterBlockException blockException = checkGlobalBlock(clusterState);
            if (blockException != null) {
                throw blockException;
            }

            String concreteSingleIndex;
            if (resolveIndex(request)) {
                concreteSingleIndex = indexNameExpressionResolver.concreteSingleIndex(clusterState, request).getName();
            } else {
                concreteSingleIndex = request.index();
            }
            this.internalRequest = new InternalRequest(request, concreteSingleIndex);
            resolveRequest(clusterState, internalRequest);

            blockException = checkRequestBlock(clusterState, internalRequest);
            if (blockException != null) {
                throw blockException;
            }

            this.shardIt = shards(clusterState, internalRequest);
        }
```
发送的具体过程:
1.TransportService.sendRequest 中检查目标是否是本地node
2.如果本地node，则进入TransportService.sendLocalRequest流程，sendLocalRequest不发送到网络，直接根据action获取注册的reg，执行processMessageReceived
```

    private void sendLocalRequest(long requestId, final String action, final TransportRequest request, TransportRequestOptions options) {
        final DirectResponseChannel channel = new DirectResponseChannel(logger, localNode, action, requestId, this, threadPool);
        try {
            onRequestSent(localNode, requestId, action, request, options);
            onRequestReceived(requestId, action);
            final RequestHandlerRegistry reg = getRequestHandler(action);
            if (reg == null) {
                throw new ActionNotFoundTransportException("Action [" + action + "] not found");
            }
            final String executor = reg.getExecutor();
            if (ThreadPool.Names.SAME.equals(executor)) {
                //noinspection unchecked
                reg.processMessageReceived(request, channel);
            } else {
                threadPool.executor(executor).execute(new AbstractRunnable() {
                    @Override
                    protected void doRun() throws Exception {
                        //noinspection unchecked
                        reg.processMessageReceived(request, channel);
                    }

                    @Override
                    public boolean isForceExecution() {
                        return reg.isForceExecution();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        try {
                            channel.sendResponse(e);
                        } catch (Exception inner) {
                            inner.addSuppressed(e);
                            logger.warn(() -> new ParameterizedMessage(
                                    "failed to notify channel of error message for action [{}]", action), inner);
                        }
                    }
                });
            }

        } catch (Exception e) {
            try {
                channel.sendResponse(e);
            } catch (Exception inner) {
                inner.addSuppressed(e);
                logger.warn(
                    () -> new ParameterizedMessage(
                        "failed to notify channel of error message for action [{}]", action), inner);
            }
        }
    }
```
3. 如果发送到网络，则请求被异步发送，"sendRequest"的时候注册handle，等待处理Response，直到超时
4. 等待数据节点的回复，如果数据节点处理成功，则返回给客户端，如果数据节点处理失败，则进行重试
5. 也就是onFailure函数
```
          @Override
                    public void onFailure(Exception e) {
                        try {
                            channel.sendResponse(e);
                        } catch (Exception inner) {
                            inner.addSuppressed(e);
                            logger.warn(() -> new ParameterizedMessage(
                                    "failed to notify channel of error message for action [{}]", action), inner);
                        }
                    }
```

## 数据节点

执行本流程的线程池：get
数据节点接受协调节点请求的入口：TransportSingleShardAction.ShardTransportHandler::messageReceived

读取数据并组织成Response，给客户端channel返回
```
    private class ShardTransportHandler implements TransportRequestHandler<Request> {

        @Override
        public void messageReceived(final Request request, final TransportChannel channel) throws Exception {
            if (logger.isTraceEnabled()) {
                logger.trace("executing [{}] on shard [{}]", request, request.internalShardId);
            }
            Response response = shardOperation(request, request.internalShardId);
            channel.sendResponse(response);
        }
    }
```
shardOperation先检查是否需要refresh,然后调用indexShard.getService.get 读取数据并存储到GetResult对象中。

读取和过滤
在ShardGetService.get()函数中，调用
```
GetResult getResult=innerGet();
```
获取结果。GetResult类用于存储读取的真实数据内容。核心的数据读取实现在ShardGetService.innerGet()函数中

```
private GetResult innerGet(String type, String id, String[] gFields, boolean realtime, long version, VersionType versionType,
                               FetchSourceContext fetchSourceContext, boolean readFromTranslog) {
        fetchSourceContext = normalizeFetchSourceContent(fetchSourceContext, gFields);
        final Collection<String> types;
        if (type == null || type.equals("_all")) {
            types = mapperService.types();
        } else {
            types = Collections.singleton(type);
        }

        Engine.GetResult get = null;
        for (String typeX : types) {
            Term uidTerm = mapperService.createUidTerm(typeX, id);
            if (uidTerm != null) {
                get = indexShard.get(new Engine.Get(realtime, readFromTranslog, typeX, id, uidTerm)
                        .version(version)
                        .versionType(versionType));
                if (get.exists()) {
                    type = typeX;
                    break;
                } else {
                    get.release();
                }
            }
        }

        if (get == null || get.exists() == false) {
            return new GetResult(shardId.getIndexName(), type, id, -1, false, null, null);
        }

        try {
            return innerGetLoadFromStoredFields(type, id, gFields, fetchSourceContext, get, mapperService);
        } finally {
            get.release();
        }
    }

```
1. 通过indexShard.get()获取Engine 的GetResult类。两者返回是同名的类，然是实现不同。indexShard.get()最终调用InterEngine.get 读取数据。

```
        Engine.GetResult get = null;
        indexShard.get(new Engine.Get(realtime, readFromTranslog, typeX, id, uidTerm)
                        .version(version)
                        .versionType(versionType));
```

2. 调用ShardGetService.innerGetLoadFromStoredFields(),根据type，id，DocumentMapper等信息从刚刚获取的信息中获取数据，对指定的field、source进行过滤，把结果存于GetResult对象中。

**InternalEngine的读取过程**

InternalEngine.get 这个过程会增加一个读锁。处理realtime选项，如果为true，则先判断是否有数据可以刷盘，然后调用Searcher进行读取，是对IndexSearcher的封装。

早期版本 realtime开启 都会从translog中读取，新版本 只从lucene读取，实现机制变成依靠refresh实现。 过程在InternalEngine中

```
package org.elasticsearch.index.engine;

 @Override
    public GetResult get(Get get, BiFunction<String, SearcherScope, Searcher> searcherFactory) throws EngineException {
        assert Objects.equals(get.uid().field(), uidField) : get.uid().field();
        try (ReleasableLock ignored = readLock.acquire()) {
            ensureOpen();
            SearcherScope scope;
            if (get.realtime()) {
                VersionValue versionValue = null;
                try (Releasable ignore = versionMap.acquireLock(get.uid().bytes())) {
                    // we need to lock here to access the version map to do this truly in RT
                    versionValue = getVersionFromMap(get.uid().bytes());
                }
                if (versionValue != null) {
                    if (versionValue.isDelete()) {
                        return GetResult.NOT_EXISTS;
                    }
                    if (get.versionType().isVersionConflictForReads(versionValue.version, get.version())) {
                        throw new VersionConflictEngineException(shardId, get.type(), get.id(),
                            get.versionType().explainConflictForReads(versionValue.version, get.version()));
                    }
                    if (get.isReadFromTranslog()) {
                        // this is only used for updates - API _GET calls will always read form a reader for consistency
                        // the update call doesn't need the consistency since it's source only + _parent but parent can go away in 7.0
                        if (versionValue.getLocation() != null) {
                            try {
                                Translog.Operation operation = translog.readOperation(versionValue.getLocation());
                                if (operation != null) {
                                    // in the case of a already pruned translog generation we might get null here - yet very unlikely
                                    TranslogLeafReader reader = new TranslogLeafReader((Translog.Index) operation, engineConfig
                                        .getIndexSettings().getIndexVersionCreated());
                                    return new GetResult(new Searcher("realtime_get", new IndexSearcher(reader)),
                                        new VersionsAndSeqNoResolver.DocIdAndVersion(0, ((Translog.Index) operation).version(), reader, 0));
                                }
                            } catch (IOException e) {
                                maybeFailEngine("realtime_get", e); // lets check if the translog has failed with a tragic event
                                throw new EngineException(shardId, "failed to read operation from translog", e);
                            }
                        } else {
                            trackTranslogLocation.set(true);
                        }
                    }
                    refresh("realtime_get", SearcherScope.INTERNAL);
                }
                scope = SearcherScope.INTERNAL;
            } else {
                // we expose what has been externally expose in a point in time snapshot via an explicit refresh
                scope = SearcherScope.EXTERNAL;
            }

            // no version, get the version from the index, we know that we refresh on flush
            return getFromSearcher(get, searcherFactory, scope);
        }
    }
```

## MGET流程分析

MGET主要处理类：TeansportMultiGetAction.通过封装单个GET请求实现，

流程如下

1. 调用线程池doExcecute
2. 准备元数据
3. 循环计算shardid，组织Request
4. 循环执行请求 --异步-->5
5. 处理成功
   1. 是 onResponse -> counter
   2. 否 onFailure ->counter
6. counter==0?
   1. 是 回复客户端
   2. 否 Wait

1. 遍历请求，计算出每个doc的路由信息，得到由shardid为key组成的request map. 这个过程没有在TransportSingleShardAction中实现，如果在这里实现，shardid就会重复，这也是合并为基于分片的请求的过程
2. 循环处理组织好的每个shard级请求，调用处理GET请求实使用TransportSingleShardAction.AsyncSingleAction处理单个doc的流程
3. 收集Response，全部Response返回后执行finishHim,给客户端返回结果

```
package org.elasticsearch.action.get;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

public class MultiGetAction extends Action<MultiGetRequest, MultiGetResponse, MultiGetRequestBuilder> {

    public static final MultiGetAction INSTANCE = new MultiGetAction();
    public static final String NAME = "indices:data/read/mget";

    private MultiGetAction() {
        super(NAME);
    }

    @Override
    public MultiGetResponse newResponse() {
        return new MultiGetResponse();
    }

    @Override
    public MultiGetRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new MultiGetRequestBuilder(client, this);
    }
}

```

```
package org.elasticsearch.action.get;
public class TransportMultiGetAction extends HandledTransportAction<MultiGetRequest, MultiGetResponse> {

    private final ClusterService clusterService;

    private final TransportShardMultiGetAction shardAction;

    @Inject
    public TransportMultiGetAction(Settings settings, ThreadPool threadPool, TransportService transportService,
                                   ClusterService clusterService, TransportShardMultiGetAction shardAction,
                                   ActionFilters actionFilters, IndexNameExpressionResolver resolver) {
        super(settings, MultiGetAction.NAME, threadPool, transportService, actionFilters, resolver, MultiGetRequest::new);
        this.clusterService = clusterService;
        this.shardAction = shardAction;
    }
    //1
    /**
    *1. 调用线程池doExcecute
    *2. 准备元数据
    *3. 循环计算shardid，组织Request
    *4. 循环执行请求 --异步-->5
    *5. 处理成功
    *   1. 是 onResponse -> counter
    *   2. 否 onFailure ->counter
    *6. counter==0?
    *    1. 是 回复客户端
    *    2. 否 Wait
    */
    @Override
    protected void doExecute(final MultiGetRequest request, final ActionListener<MultiGetResponse> listener) {
        //2
        ClusterState clusterState = clusterService.state();
        clusterState.blocks().globalBlockedRaiseException(ClusterBlockLevel.READ);

        final AtomicArray<MultiGetItemResponse> responses = new AtomicArray<>(request.items.size());
        final Map<ShardId, MultiGetShardRequest> shardRequests = new HashMap<>();
        //3
        for (int i = 0; i < request.items.size(); i++) {
            //获取请求
            MultiGetRequest.Item item = request.items.get(i);

            String concreteSingleIndex;
            try {
                //组织请求
                concreteSingleIndex = indexNameExpressionResolver.concreteSingleIndex(clusterState, item).getName();
                //异步处理
                item.routing(clusterState.metaData().resolveIndexRouting(item.parent(), item.routing(), item.index()));
                if ((item.routing() == null) && (clusterState.getMetaData().routingRequired(concreteSingleIndex, item.type()))) {
                    String message = "routing is required for [" + concreteSingleIndex + "]/[" + item.type() + "]/[" + item.id() + "]";
                    responses.set(i, newItemFailure(concreteSingleIndex, item.type(), item.id(), new IllegalArgumentException(message)));
                    continue;
                }
            } catch (Exception e) {
                responses.set(i, newItemFailure(item.index(), item.type(), item.id(), e));
                continue;
            }
            //计算shardID
            ShardId shardId = clusterService.operationRouting()
                    .getShards(clusterState, concreteSingleIndex, item.id(), item.routing(), null)
                    .shardId();
            //
            MultiGetShardRequest shardRequest = shardRequests.get(shardId);
            if (shardRequest == null) {
                shardRequest = new MultiGetShardRequest(request, shardId.getIndexName(), shardId.getId());
                shardRequests.put(shardId, shardRequest);
            }
            //循环等待
            shardRequest.add(i, item);
        }

        if (shardRequests.isEmpty()) {
            // only failures..
            //处理成功  onResponse
            listener.onResponse(new MultiGetResponse(responses.toArray(new MultiGetItemResponse[responses.length()])));
        }
        //计算counter
        //    *5. 处理成功
        //      *1. 是 onResponse -> counter
        //      *2. 否 onFailure ->counter
        final AtomicInteger counter = new AtomicInteger(shardRequests.size());

        for (final MultiGetShardRequest shardRequest : shardRequests.values()) {
            shardAction.execute(shardRequest, new ActionListener<MultiGetShardResponse>() {
                @Override
                //采用listeners
                //收集Response，全部Response返回后执行finishHim,给客户端返回结果
                public void onResponse(MultiGetShardResponse response) {
                    for (int i = 0; i < response.locations.size(); i++) {
                        MultiGetItemResponse itemResponse = new MultiGetItemResponse(response.responses.get(i), response.failures.get(i));
                        responses.set(response.locations.get(i), itemResponse);
                    }
                    if (counter.decrementAndGet() == 0) {
                        finishHim();
                    }
                }

                @Override
                //计算counter
                public void onFailure(Exception e) {
                    // create failures for all relevant requests
                    for (int i = 0; i < shardRequest.locations.size(); i++) {
                        MultiGetRequest.Item item = shardRequest.items.get(i);
                        responses.set(shardRequest.locations.get(i), newItemFailure(shardRequest.index(), item.type(), item.id(), e));
                    }
                    if (counter.decrementAndGet() == 0) {
                        finishHim();
                    }
                }

                private void finishHim() {
                    listener.onResponse(new MultiGetResponse(responses.toArray(new MultiGetItemResponse[responses.length()])));
                }
            });
        }
    }

    private static MultiGetItemResponse newItemFailure(String index, String type, String id, Exception exception) {
        return new MultiGetItemResponse(null, new MultiGetResponse.Failure(index, type, id, exception));
    }
}

```
回复的信息中文档顺序与请求的顺序一只。乳沟部分文档读取失败，则不影响其他结果，检索的事摆的doc会在恢复中标出

## 总结 
**GET失败如何处理** 尝试从别的分片进行读取
**优先级** 优先级策略只是将匹配到优先级的节点放在目标节点列表的前面
