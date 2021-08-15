# ES写流程

单写 Index请求

批写 Bulk请求

## 文档操作的定义

enum OnType{
    INDEX(0),
    CREATE(1),
    UPDATE(2),
    DELETE(3);
}

INDEX: 向索引中 put 
CREATE: put可以通过on_type参数设置操作类型为create,如果文档已存在 则请求失败
UPDATE: 默认情况下, put一个文档 如果文档已存在 则更新它
DELETE:删除文档
在put API中,通过 on_type参数来制定操作类型

## 写基本流程

1. 客户端向某一个节点发送写请求 一般是Master节点
2. Node1 使用文档ID来确定文档属于分片0,通过集群状态中的内容路由表信息获知分片0的主分片位于Node3,因此请求被转发到Node3上.
3. Node3上的主分片执行写操作,如果写入成功,则它便将请求并行转发到Node1和Node2的副分片上,等待返回结果.当所有的副分片都报告成功,Node3将向协调节点报告成功,协调节点再向客户端报告成功

写一致性默认策略还是quorum 也就是投票策略

quorum=(int)(primary+numofReplicas)/2+1

## Index/Bulk详细流程

协调节点
1. 参数检查
2. 处理pipeline
3. 创建索引
4. 自动生成id
5. 合并请求
6. 并行转发
主分片节点
7. 参数检测
   1. delay?
      1. true->relocated
      2. fasle 放入队列
   2. relocated?
      1. true-> 一致性检查
      2. false -> 转发到新节点
   3. 一致性检查
8. 顺序写每条数据 失败放入失败队列 f1
9. 并行转发
副分片节点
10. 顺序写每条 失败放入失败队列 f1
11. 回复主分片节点 告知完成
主分片节点
12. handleResponse
13. 收到全部恢复
协调节点
14. handleResponse
15. 回复客户端

```
package org.elasticsearch.transport;

    private void handleResponse(InetSocketAddress remoteAddress, final StreamInput stream, final TransportResponseHandler handler) {
        final TransportResponse response;
        try {
            response = handler.read(stream);
            response.remoteAddress(new TransportAddress(remoteAddress));
        } catch (Exception e) {
            handleException(handler, new TransportSerializationException(
                "Failed to deserialize response from handler [" + handler.getClass().getName() + "]", e));
            return;
        }
        threadPool.executor(handler.executor()).execute(new AbstractRunnable() {
            @Override
            public void onFailure(Exception e) {
                handleException(handler, new ResponseHandlerFailureTransportException(e));
            }

            @Override
            protected void doRun() throws Exception {
                handler.handleResponse(response);
            }
        });

    }
```

### 协调节点流程
协调节点负责创建索引,转发请求到主分片节点,等待相应,回复客户端
这是Bulk的抽象类,具体的协调流程为org.elasticsearch.action.bulk.TransportBulkAction, 线程池 **http_server_worker** 封装在 http模块org.elasticsearch.http 的HttpServerTransport 所有的CRUD发送请求其实都用这个线程池作为入口
ES7.8对于这部分模块进行了很多的新增 新增了很多新的Channel模块
```
public class BulkAction extends Action<BulkRequest, BulkResponse, BulkRequestBuilder> {

    public static final BulkAction INSTANCE = new BulkAction();
    public static final String NAME = "indices:data/write/bulk";

    private BulkAction() {
        super(NAME);
    }

    @Override
    public BulkResponse newResponse() {
        return new BulkResponse();
    }

    @Override
    public BulkRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new BulkRequestBuilder(client, this);
    }

    @Override
    public TransportRequestOptions transportOptions(Settings settings) {
        return TransportRequestOptions.builder()
                .withType(TransportRequestOptions.Type.BULK)
                .withCompress(settings.getAsBoolean("action.bulk.compress", true)
                ).build();
    }
}

```

**参数检查**

|参数|检查|
|---|--|
|index|not null|
|type|not null|
|source| not null|
|contentType| not null|
|opType|创建 VersionType 必须为internal,Version不可为 MATCH_DELETED|
|resolvedVersion|校验解析的Version是否合法|
|versionType|不可为FORCE类型,类型已废弃|
|id|非空时,长度不可大于512.以及为空时对versionType和resolvedVersion的检查|

**处理pipeline请求**

数据预处理工作通过定义pipeline和processors实现.pipeline是一系列processors的定义,processors按照声明的顺序执行.

如果index或者bulk请求执行了pipeline参数,优先使用相应的pipeline进行处理.

**自动创建索引**

若配置允许自动创建,则计算请求中涉及的多个索引,判断是否存在,若不存在则创建.若部分创建失败,则涉及的请求被标记为失败.其他索引正常执行写流程.

位于TransportBulkAction 第254行
```java
final AtomicInteger counter = new AtomicInteger(autoCreateIndices.size());
                for (String index : autoCreateIndices) {
                    createIndex(index, bulkRequest.timeout(), minNodeVersion, new ActionListener<>() {
                        @Override
                        public void onResponse(CreateIndexResponse result) {
                            if (counter.decrementAndGet() == 0) {
                                threadPool.executor(ThreadPool.Names.WRITE).execute(
                                    () -> executeBulk(task, bulkRequest, startTime, listener, responses, indicesThatCannotBeCreated));
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (!(ExceptionsHelper.unwrapCause(e) instanceof ResourceAlreadyExistsException)) {
                                // fail all requests involving this index, if create didn't work
                                for (int i = 0; i < bulkRequest.requests.size(); i++) {
                                    DocWriteRequest<?> request = bulkRequest.requests.get(i);
                                    if (request != null && setResponseFailureIfIndexMatches(responses, i, request, index, e)) {
                                        bulkRequest.requests.set(i, null);
                                    }
                                }
                            }
                            if (counter.decrementAndGet() == 0) {
                                executeBulk(task, bulkRequest, startTime, ActionListener.wrap(listener::onResponse, inner -> {
                                    inner.addSuppressed(e);
                                    listener.onFailure(inner);
                                }), responses, indicesThatCannotBeCreated);
                            }
                        }
                    });
```

**对请求的预先处理**

```java
        protected void doRun() {
            assert bulkRequest != null; //确保请求不为空
            final ClusterState clusterState = observer.setAndGetObservedState(); //获取集群状态
            if (handleBlockExceptions(clusterState)) { //状态不对 直接返回
                return;
            }
            final ConcreteIndices concreteIndices = new ConcreteIndices(clusterState, indexNameExpressionResolver); //创建ConcreteIndices对象
            Metadata metadata = clusterState.metadata();//获取额外信息 位于org.elasticsearch.cluster.ClusterState
            for (int i = 0; i < bulkRequest.requests.size(); i++) {//遍历请求
                DocWriteRequest<?> docWriteRequest = bulkRequest.requests.get(i);//单个请求装入org.elasticsearch.action.DocWriteRequest类
                //the request can only be null because we set it to null in the previous step, so it gets ignored
                if (docWriteRequest == null) {
                    continue;
                }
                if (addFailureIfIndexIsUnavailable(docWriteRequest, i, concreteIndices, metadata)) {
                    continue;
                }
                Index concreteIndex = concreteIndices.resolveIfAbsent(docWriteRequest);//获取Index
                try {
                    switch (docWriteRequest.opType()) {
                        case CREATE:
                        case INDEX:
                            prohibitAppendWritesInBackingIndices(docWriteRequest, metadata);
                            IndexRequest indexRequest = (IndexRequest) docWriteRequest;
                            final IndexMetadata indexMetadata = metadata.index(concreteIndex);
                            MappingMetadata mappingMd = indexMetadata.mapping();
                            Version indexCreated = indexMetadata.getCreationVersion();
                            indexRequest.resolveRouting(metadata);
                            indexRequest.process(indexCreated, mappingMd, concreteIndex.getName());
                            break;
                        case UPDATE:
                            TransportUpdateAction.resolveAndValidateRouting(metadata, concreteIndex.getName(),
                                (UpdateRequest) docWriteRequest);
                            break;
                        case DELETE:
                            docWriteRequest.routing(metadata.resolveWriteIndexRouting(docWriteRequest.routing(), docWriteRequest.index()));
                            // check if routing is required, if so, throw error if routing wasn't specified
                            if (docWriteRequest.routing() == null && metadata.routingRequired(concreteIndex.getName())) {
                                throw new RoutingMissingException(concreteIndex.getName(), docWriteRequest.id());
                            }
                            break;
                        default: throw new AssertionError("request type not supported: [" + docWriteRequest.opType() + "]");
                        //根据type 去进行crud
                    }
                } catch (ElasticsearchParseException | IllegalArgumentException | RoutingMissingException e) {
                    BulkItemResponse.Failure failure = new BulkItemResponse.Failure(concreteIndex.getName(),
                        docWriteRequest.id(), e);
                    BulkItemResponse bulkItemResponse = new BulkItemResponse(i, docWriteRequest.opType(), failure);
                    responses.set(i, bulkItemResponse);
                    // make sure the request gets never processed again
                    bulkRequest.requests.set(i, null);
                }
            }

            // first, go over all the requests and create a ShardId -> Operations mapping 创建 shardid和 请求的hashmap
            Map<ShardId, List<BulkItemRequest>> requestsByShard = new HashMap<>();
            for (int i = 0; i < bulkRequest.requests.size(); i++) {
                DocWriteRequest<?> request = bulkRequest.requests.get(i);
                if (request == null) {
                    continue;
                }
                String concreteIndex = concreteIndices.getConcreteIndex(request.index()).getName();
                ShardId shardId = clusterService.operationRouting().indexShards(clusterState, concreteIndex, request.id(),
                    request.routing()).shardId();
                List<BulkItemRequest> shardRequests = requestsByShard.computeIfAbsent(shardId, shard -> new ArrayList<>());
                shardRequests.add(new BulkItemRequest(i, request));
            }

            if (requestsByShard.isEmpty()) {
                listener.onResponse(new BulkResponse(responses.toArray(new BulkItemResponse[responses.length()]),
                    buildTookInMillis(startTimeNanos)));
                return;
            }

            final AtomicInteger counter = new AtomicInteger(requestsByShard.size());
            String nodeId = clusterService.localNode().getId();
            for (Map.Entry<ShardId, List<BulkItemRequest>> entry : requestsByShard.entrySet()) {
                final ShardId shardId = entry.getKey();
                final List<BulkItemRequest> requests = entry.getValue();
                BulkShardRequest bulkShardRequest = new BulkShardRequest(shardId, bulkRequest.getRefreshPolicy(),
                        requests.toArray(new BulkItemRequest[requests.size()]));
                bulkShardRequest.waitForActiveShards(bulkRequest.waitForActiveShards());
                bulkShardRequest.timeout(bulkRequest.timeout());
                bulkShardRequest.routedBasedOnClusterVersion(clusterState.version());
                if (task != null) {
                    bulkShardRequest.setParentTask(nodeId, task.getId());
                }
                client.executeLocally(TransportShardBulkAction.TYPE, bulkShardRequest, new ActionListener<>() {
                    @Override
                    public void onResponse(BulkShardResponse bulkShardResponse) {
                        for (BulkItemResponse bulkItemResponse : bulkShardResponse.getResponses()) {
                            // we may have no response if item failed
                            if (bulkItemResponse.getResponse() != null) {
                                bulkItemResponse.getResponse().setShardInfo(bulkShardResponse.getShardInfo());
                            }
                            responses.set(bulkItemResponse.getItemId(), bulkItemResponse);
                        }
                        if (counter.decrementAndGet() == 0) {
                            finishHim();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // create failures for all relevant requests
                        for (BulkItemRequest request : requests) {
                            final String indexName = concreteIndices.getConcreteIndex(request.index()).getName();
                            DocWriteRequest<?> docWriteRequest = request.request();
                            responses.set(request.id(), new BulkItemResponse(request.id(), docWriteRequest.opType(),
                                    new BulkItemResponse.Failure(indexName, docWriteRequest.id(), e)));
                        }
                        if (counter.decrementAndGet() == 0) {
                            finishHim();
                        }
                    }

                    private void finishHim() {
                        listener.onResponse(new BulkResponse(responses.toArray(new BulkItemResponse[responses.length()]),
                            buildTookInMillis(startTimeNanos)));
                    }
                });
            }
            bulkRequest = null; // allow memory for bulk request items to be reclaimed before all items have been completed
        }

```

**检测集群状态**

协调节点在开始处理时会先检测集群状态, 若集群异常则取消写入.

**内容路由,构建基于shard请求**

bulkRequest重新组织为基于shard的请求列表
```
Map<ShardId, List<BulkItemRequest>> requestsByShard = new HashMap<>();
```

ShardID

```
public class ShardID{
    private Index index;
    private int shardId;
    private int hashCode;
}
```

**路由算法**

根据routing和文档id计算目标shardID的过程.

shard_num=hash(_routing)%num_primary_shards

当id或者routing不够随机的时候,会造成数据倾斜,这时候需要进行routing_partition_size进行修正

shard_num=(hash(_routing)+hash(_id)%routing_partition_size)%num_primary_shards

_routing计算索引中的分片,_id来选择该组内的分片.

Murmur3对有效路由进行hash处理 加上偏执位 

offset=Math.floorMod(Murmur3HashFunction.hash(id),indexMetaData.getRoutingPartitionSize());

**转发请求并等待响应**

根据集群状态中的内容路由表确定主分片所在节点,转发请求并等待响应.

遍历所有需要写的shard,将位于某个shard的请求封装为BulkShardRequest类,调用transportShardBulkAction的execute方法执行发送,在listener中等待相应,每个响应以shard为单位.如果某个shard的相应中部分doc写失败,将异常信息填充到Response中,整体请求做成功处理.

收到响应后回复给客户端
转发的具体实现

org.elasticsearch.action.support.replication.TransportReplicationAction;

```
 final class ReroutePhase extends AbstractRunnable {
        private final ActionListener<Response> listener;
        private final Request request;
        private final ReplicationTask task;
        private final ClusterStateObserver observer;
        private final AtomicBoolean finished = new AtomicBoolean();

        ReroutePhase(ReplicationTask task, Request request, ActionListener<Response> listener) {
            this.request = request;
            if (task != null) {
                this.request.setParentTask(clusterService.localNode().getId(), task.getId());
            }
            this.listener = listener;
            this.task = task;
            this.observer = new ClusterStateObserver(clusterService, request.timeout(), logger, threadPool.getThreadContext());
        }

        @Override
        public void onFailure(Exception e) {
            finishWithUnexpectedFailure(e);
        }

        @Override
        protected void doRun() {
            setPhase(task, "routing");
            final ClusterState state = observer.setAndGetObservedState();
            final ClusterBlockException blockException = blockExceptions(state, request.shardId().getIndexName());
            if (blockException != null) {
                if (blockException.retryable()) {
                    logger.trace("cluster is blocked, scheduling a retry", blockException);
                    retry(blockException);
                } else {
                    finishAsFailed(blockException);
                }
            } else {
                final IndexMetadata indexMetadata = state.metadata().index(request.shardId().getIndex());
                if (indexMetadata == null) {
                    // ensure that the cluster state on the node is at least as high as the node that decided that the index was there
                    if (state.version() < request.routedBasedOnClusterVersion()) {
                        logger.trace("failed to find index [{}] for request [{}] despite sender thinking it would be here. " +
                                "Local cluster state version [{}]] is older than on sending node (version [{}]), scheduling a retry...",
                            request.shardId().getIndex(), request, state.version(), request.routedBasedOnClusterVersion());
                        retry(new IndexNotFoundException("failed to find index as current cluster state with version [" + state.version() +
                            "] is stale (expected at least [" + request.routedBasedOnClusterVersion() + "]",
                            request.shardId().getIndexName()));
                        return;
                    } else {
                        finishAsFailed(new IndexNotFoundException(request.shardId().getIndex()));
                        return;
                    }
                }

                if (indexMetadata.getState() == IndexMetadata.State.CLOSE) {
                    finishAsFailed(new IndexClosedException(indexMetadata.getIndex()));
                    return;
                }

                if (request.waitForActiveShards() == ActiveShardCount.DEFAULT) {
                    // if the wait for active shard count has not been set in the request,
                    // resolve it from the index settings
                    request.waitForActiveShards(indexMetadata.getWaitForActiveShards());
                }
                assert request.waitForActiveShards() != ActiveShardCount.DEFAULT :
                    "request waitForActiveShards must be set in resolveRequest";

                final ShardRouting primary = state.getRoutingTable().shardRoutingTable(request.shardId()).primaryShard();
                if (primary == null || primary.active() == false) {
                    logger.trace("primary shard [{}] is not yet active, scheduling a retry: action [{}], request [{}], "
                        + "cluster state version [{}]", request.shardId(), actionName, request, state.version());
                    retryBecauseUnavailable(request.shardId(), "primary shard is not active");
                    return;
                }
                if (state.nodes().nodeExists(primary.currentNodeId()) == false) {
                    logger.trace("primary shard [{}] is assigned to an unknown node [{}], scheduling a retry: action [{}], request [{}], "
                        + "cluster state version [{}]", request.shardId(), primary.currentNodeId(), actionName, request, state.version());
                    retryBecauseUnavailable(request.shardId(), "primary shard isn't assigned to a known node.");
                    return;
                }
                final DiscoveryNode node = state.nodes().get(primary.currentNodeId());
                if (primary.currentNodeId().equals(state.nodes().getLocalNodeId())) {
                    performLocalAction(state, primary, node, indexMetadata);
                } else {
                    performRemoteAction(state, primary, node);
                }
            }
        }
```

### 主分片节点写流程 
线程池 bulk
主分片所在节点负责在本地写主分片,写成功后,转发写副本片请求,等待响应,恢复协调节点

1. 检查请求
   主分片所在节点收到协调节点发来的请求后先做校验工作,主要检测要写的是否是主分片,AllocationId是否符合预期,索引是否处于关闭状态
2. 是否延迟执行
   判断请求是否需要延迟执行,如有需要延迟者放入队列,否则继续下面的流程

```
org.elasticsearch.action.support.ActiveShardsObserver;
//等待活动分片
public void waitForActiveShards()
```
3. 判断主分片是否已经发生迁移
4. 检测写一致性,检测本次写操作设计的shard,活跃shard的数量是否足够 不足则不执行,默认为1,只要主分片可用即可写入

```java
//对下面方法的二次封装和补充
//package org.elasticsearch.action.support.ActiveShardCount;
 public boolean enoughShardsActive(final ClusterState clusterState, final String... indices) {
        if (this == ActiveShardCount.NONE) {
            // not waiting for any active shards
            return true;
        }

        for (final String indexName : indices) {
            final IndexMetadata indexMetadata = clusterState.metadata().index(indexName);
            if (indexMetadata == null) {
                // its possible the index was deleted while waiting for active shard copies,
                // in this case, we'll just consider it that we have enough active shard copies
                // and we can stop waiting
                continue;
            }
            final IndexRoutingTable indexRoutingTable = clusterState.routingTable().index(indexName);
            if (indexRoutingTable == null && indexMetadata.getState() == IndexMetadata.State.CLOSE) {
                // its possible the index was closed while waiting for active shard copies,
                // in this case, we'll just consider it that we have enough active shard copies
                // and we can stop waiting
                continue;
            }
            assert indexRoutingTable != null;
            if (indexRoutingTable.allPrimaryShardsActive() == false) {
                // all primary shards aren't active yet
                return false;
            }
            ActiveShardCount waitForActiveShards = this;
            if (waitForActiveShards == ActiveShardCount.DEFAULT) {
                waitForActiveShards = SETTING_WAIT_FOR_ACTIVE_SHARDS.get(indexMetadata.getSettings());
            }
            for (final IntObjectCursor<IndexShardRoutingTable> shardRouting : indexRoutingTable.getShards()) {
                if (waitForActiveShards.enoughShardsActive(shardRouting.value) == false) {
                    // not enough active shard copies yet
                    return false;
                }
            }
        }

        return true;
    }
//ES6.4原有方法 一共三种方法
    public boolean enoughShardsActive(final IndexShardRoutingTable shardRoutingTable) {
        final int activeShardCount = shardRoutingTable.activeShards().size();
        if (this == ActiveShardCount.ALL) {
            // adding 1 for the primary in addition to the total number of replicas,
            // which gives us the total number of shard copies
            return activeShardCount == shardRoutingTable.replicaShards().size() + 1;
        } else if (this == ActiveShardCount.DEFAULT) {
            return activeShardCount >= 1;
        } else {
            return activeShardCount >= value;
        }
    }
```

```java
public void execute() throws Exception {
        final String activeShardCountFailure = checkActiveShardCount();
        final ShardRouting primaryRouting = primary.routingEntry();
        final ShardId primaryId = primaryRouting.shardId();
        if (activeShardCountFailure != null) {
            finishAsFailed(new UnavailableShardsException(primaryId,
                "{} Timeout: [{}], request: [{}]", activeShardCountFailure, request.timeout(), request));
            return;
        }

        totalShards.incrementAndGet();
        pendingActions.incrementAndGet(); // increase by 1 until we finish all primary coordination
        primaryResult = primary.perform(request);
        primary.updateLocalCheckpointForShard(primaryRouting.allocationId().getId(), primary.localCheckpoint());
        final ReplicaRequest replicaRequest = primaryResult.replicaRequest();
        if (replicaRequest != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] op [{}] completed on primary for request [{}]", primaryId, opType, request);
            }
            final long globalCheckpoint = primary.globalCheckpoint();
            final ReplicationGroup replicationGroup = primary.getReplicationGroup();
            markUnavailableShardsAsStale(replicaRequest, replicationGroup);
            performOnReplicas(replicaRequest, globalCheckpoint, replicationGroup);
        }

        successfulShards.incrementAndGet();  // mark primary as successful
        decPendingAndFinishIfNeeded();
    }

```

5. 写入Lucene和Translog

```
//package org.elasticsearch.index.engine;
```

InternalEngine.index 逐条写入doc

Engine 封装了Lucene和translog的调用,对外提供读写接口.

生成SequenceNumber和Version,不出意料的看见了原子类.
AtomicLong,都能够在InternalEngine类中实现.Sequence Number每次递增1,Version根据当前doc的最大版本加1.

索引过程为先写Lucene,后写translog.

因为Lucene写入时对数据有检查,写操作可能会失败.如果先写translog,写入Lucene时失败,则还需要对translog进行回滚处理.

6. flush translog

根据的配置的translog flush策略进行刷盘空值,定时或立即刷盘

```
package org.elasticsearch.index.seqno; 
private void maybeSyncTranslog(final IndexShard indexShard) throws IOException {
        if (indexShard.getTranslogDurability() == Translog.Durability.REQUEST &&
            indexShard.getLastSyncedGlobalCheckpoint() < indexShard.getLastKnownGlobalCheckpoint()) {
            indexShard.sync();
        }
    }
```

7. 写入副分片

现在已经为要写的副本shard准备了一个列表,循环处理每个shard,跳过unassigned状态的shard,向目标节点发送请求,等待响应.这个过程是异步并行的.

转发请求的时候会将这些参数传递给副分片
```
//inner class ReplicasProxy 
public void performOn(
                final ShardRouting replica,
                final ReplicaRequest request,
                final long primaryTerm,
                final long globalCheckpoint,
                final long maxSeqNoOfUpdatesOrDeletes,
                final ActionListener<ReplicationOperation.ReplicaResponse> listener){
                    String nodeId = replica.currentNodeId();
            final DiscoveryNode node = clusterService.state().nodes().get(nodeId);
            if (node == null) {
                listener.onFailure(new NoNodeAvailableException("unknown node [" + nodeId + "]"));
                return;
            }
            final ConcreteReplicaRequest<ReplicaRequest> replicaRequest = new ConcreteReplicaRequest<>(
                request, replica.allocationId().getId(), primaryTerm, globalCheckpoint, maxSeqNoOfUpdatesOrDeletes);
            final ActionListenerResponseHandler<ReplicaResponse> handler = new ActionListenerResponseHandler<>(listener,
                ReplicaResponse::new);
            transportService.sendRequest(node, transportReplicaAction, replicaRequest, transportOptions, handler);
                }

```

可以看出来,整个过程都在**TransportReplicationAction**.

等待过程中发出多少个Request,就要等待多少个Response.无论这些Response是成功还是失败,直到超时.
收集到全部Response后执行finish 在ReplicationOperation
```
package org.elasticsearch.action.support.replication;
    private void decPendingAndFinishIfNeeded() {
        assert pendingActions.get() > 0 : "pending action count goes below 0 for request [" + request + "]";
        if (pendingActions.decrementAndGet() == 0) {
            finish();
        }
    }

    private void finish() {
        if (finished.compareAndSet(false, true)) {
            final ReplicationResponse.ShardInfo.Failure[] failuresArray;
            if (shardReplicaFailures.isEmpty()) {
                failuresArray = ReplicationResponse.EMPTY;
            } else {
                failuresArray = new ReplicationResponse.ShardInfo.Failure[shardReplicaFailures.size()];
                shardReplicaFailures.toArray(failuresArray);
            }
            primaryResult.setShardInfo(new ReplicationResponse.ShardInfo(
                    totalShards.get(),
                    successfulShards.get(),
                    failuresArray
                )
            );
            resultListener.onResponse(primaryResult);
        }
    }

    private void finishAsFailed(Exception exception) {
        if (finished.compareAndSet(false, true)) {
            resultListener.onFailure(exception);
        }
    }
```



题外话

ReplicationOperation类,此操作中的挂起子操作数。 当以下操作开始时，此值递增；完成以下操作时，此值递减：

```
performOnReplicas
```

8. 处理副分片写失败情况

主分片所在节点会发送一个shardFailed请求给Master,该操作主要在ReplicationOperation中实现.

```
package org.elasticsearch.action.support.replication;
 public void onFailure(Exception replicaException) {
                logger.trace(() -> new ParameterizedMessage(
                    "[{}] failure while performing [{}] on replica {}, request [{}]",
                    shard.shardId(), opType, shard, replicaRequest), replicaException);
                // Only report "critical" exceptions - TODO: Reach out to the master node to get the latest shard state then report.
                if (TransportActions.isShardNotAvailableException(replicaException) == false) {
                    RestStatus restStatus = ExceptionsHelper.status(replicaException);
                    shardReplicaFailures.add(new ReplicationResponse.ShardInfo.Failure(
                        shard.shardId(), shard.currentNodeId(), replicaException, restStatus, false));
                }
                String message = String.format(Locale.ROOT, "failed to perform %s on replica %s", opType, shard);
                replicasProxy.failShardIfNeeded(shard, message,
                    replicaException, ReplicationOperation.this::decPendingAndFinishIfNeeded,
                    ReplicationOperation.this::onPrimaryDemoted, throwable -> decPendingAndFinishIfNeeded());
            }
        });
public void failShardIfNeeded(ShardRouting replica, String message, Exception exception,
                                      Runnable onSuccess, Consumer<Exception> onPrimaryDemoted, Consumer<Exception> onIgnoredFailure) {
            // This does not need to fail the shard. The idea is that this
            // is a non-write operation (something like a refresh or a global
            // checkpoint sync) and therefore the replica should still be
            // "alive" if it were to fail.
            onSuccess.run();
        }
```

向Master发送分片失败请求在ShardStateAction中:

```
package org.elasticsearch.cluster.action.shard;
private void sendShardAction(final String actionName, final ClusterState currentState, final TransportRequest request, final Listener listener) {
        ClusterStateObserver observer = new ClusterStateObserver(currentState, clusterService, null, logger, threadPool.getThreadContext());
        DiscoveryNode masterNode = currentState.nodes().getMasterNode();
        Predicate<ClusterState> changePredicate = MasterNodeChangePredicate.build(currentState);
        if (masterNode == null) {
            logger.warn("no master known for action [{}] for shard entry [{}]", actionName, request);
            waitForNewMasterAndRetry(actionName, observer, request, listener, changePredicate);
        } else {
            logger.debug("sending [{}] to [{}] for shard entry [{}]", actionName, masterNode.getId(), request);
            transportService.sendRequest(masterNode,
                actionName, request, new EmptyTransportResponseHandler(ThreadPool.Names.SAME) {
                    @Override
                    public void handleResponse(TransportResponse.Empty response) {
                        listener.onSuccess();
                    }

                    @Override
                    public void handleException(TransportException exp) {
                        if (isMasterChannelException(exp)) {
                            waitForNewMasterAndRetry(actionName, observer, request, listener, changePredicate);
                        } else {
                            logger.warn(new ParameterizedMessage("unexpected failure while sending request [{}] to [{}] for shard entry [{}]", actionName, masterNode, request), exp);
                            listener.onFailure(exp instanceof RemoteTransportException ? (Exception) (exp.getCause() instanceof Exception ? exp.getCause() : new ElasticsearchException(exp.getCause())) : exp);
                        }
                    }
                });
        }
    }
```

然后Master会更新集群状态,在新的集群状态中,这个shard将:

1. 从in_sync_allocation列表中删除
2. 在routing_table的shard列表中将状态改成UNASSIGNED
3. 添加到routingNode的unassignedShards列表

```
public void localShardFailed(final ShardRouting shardRouting, final String message, @Nullable final Exception failure, Listener listener) {
        localShardFailed(shardRouting, message, failure, listener, clusterService.state());
    }
public void localShardFailed(final ShardRouting shardRouting, final String message, @Nullable final Exception failure, Listener listener,
                                 final ClusterState currentState) {
        FailedShardEntry shardEntry = new FailedShardEntry(shardRouting.shardId(), shardRouting.allocationId().getId(), 0L, message, failure, true);
        sendShardAction(SHARD_FAILED_ACTION_NAME, currentState, shardEntry, listener);
    }
```

### 副分片节点流程

执行线程池:bulk
执行与主分片基本相同的写doc过程,完成后回复主节点

```
package org.elasticsearch.action.support.replication;
//ShardStateAction
protected void doRun() throws Exception {
            setPhase(task, "replica");
            final String actualAllocationId = this.replica.routingEntry().allocationId().getId();
            if (actualAllocationId.equals(targetAllocationID) == false) {
                throw new ShardNotFoundException(this.replica.shardId(), "expected aID [{}] but found [{}]", targetAllocationID,
                    actualAllocationId);
            }
            replica.acquireReplicaOperationPermit(primaryTerm, globalCheckpoint, this, executor, request);
}
package org.elasticsearch.index.shard;
//IndexShard
 public void acquireReplicaOperationPermit(final long opPrimaryTerm, final long globalCheckpoint,
                                              final ActionListener<Releasable> onPermitAcquired, final String executorOnDelay,
                                              final Object debugInfo) {
        verifyNotClosed();
        verifyReplicationTarget();
        if (opPrimaryTerm > pendingPrimaryTerm) {
            synchronized (mutex) {
                if (opPrimaryTerm > pendingPrimaryTerm) {
                    IndexShardState shardState = state();
                    // only roll translog and update primary term if shard has made it past recovery
                    // Having a new primary term here means that the old primary failed and that there is a new primary, which again
                    // means that the master will fail this shard as all initializing shards are failed when a primary is selected
                    // We abort early here to prevent an ongoing recovery from the failed primary to mess with the global / local checkpoint
                    if (shardState != IndexShardState.POST_RECOVERY &&
                        shardState != IndexShardState.STARTED) {
                        throw new IndexShardNotStartedException(shardId, shardState);
                    }

                    if (opPrimaryTerm > pendingPrimaryTerm) {
                        bumpPrimaryTerm(opPrimaryTerm, () -> {
                                updateGlobalCheckpointOnReplica(globalCheckpoint, "primary term transition");
                                final long currentGlobalCheckpoint = getGlobalCheckpoint();
                                final long localCheckpoint;
                                if (currentGlobalCheckpoint == SequenceNumbers.UNASSIGNED_SEQ_NO) {
                                    localCheckpoint = SequenceNumbers.NO_OPS_PERFORMED;
                                } else {
                                    localCheckpoint = currentGlobalCheckpoint;
                                }
                                logger.trace(
                                    "detected new primary with primary term [{}], resetting local checkpoint from [{}] to [{}]",
                                    opPrimaryTerm,
                                    getLocalCheckpoint(),
                                    localCheckpoint);
                                getEngine().resetLocalCheckpoint(localCheckpoint);
                                getEngine().rollTranslogGeneration();
                        });
                    }
                }
            }
        }

        assert opPrimaryTerm <= pendingPrimaryTerm
                : "operation primary term [" + opPrimaryTerm + "] should be at most [" + pendingPrimaryTerm + "]";
        indexShardOperationPermits.acquire(
                new ActionListener<Releasable>() {
                    @Override
                    public void onResponse(final Releasable releasable) {
                        if (opPrimaryTerm < operationPrimaryTerm) {
                            releasable.close();
                            final String message = String.format(
                                    Locale.ROOT,
                                    "%s operation primary term [%d] is too old (current [%d])",
                                    shardId,
                                    opPrimaryTerm,
                                    operationPrimaryTerm);
                            onPermitAcquired.onFailure(new IllegalStateException(message));
                        } else {
                            try {
                                updateGlobalCheckpointOnReplica(globalCheckpoint, "operation");
                            } catch (Exception e) {
                                releasable.close();
                                onPermitAcquired.onFailure(e);
                                return;
                            }
                            onPermitAcquired.onResponse(releasable);
                        }
                    }

                    @Override
                    public void onFailure(final Exception e) {
                        onPermitAcquired.onFailure(e);
                    }
                },
                executorOnDelay,
                true, debugInfo);
    }

```

**在副分片的写入过程中参数检查都要调用IndexShardOperationsPermits.acquire 判断是否需要delay**
从而继续后面的写流程

## I/O 异常处理

在shard上执行的操作会产生I/O异常.一个shard上的CRUD等操作有一个ENGINE对象封装,在Engine处理过程中,部分操作产生的部分异常ES会认为有必要关闭这个Engine,上报Master.

对于异常的捕获主要通过IOException实现. 实现在InternalEngine中,是私有方法,调用在index函数中

```
 public IndexResult index(Index index) throws IOException{
     assert Objects.equals(index.uid().field(), uidField) : index.uid().field();
        final boolean doThrottle = index.origin().isRecovery() == false;
        try (ReleasableLock releasableLock = readLock.acquire()) {
            ensureOpen();
            assert assertIncomingSequenceNumber(index.origin(), index.seqNo());
            assert assertVersionType(index);
            try (Releasable ignored = versionMap.acquireLock(index.uid().bytes());
                Releasable indexThrottle = doThrottle ? () -> {} : throttle.acquireThrottle()) {
                lastWriteNanos = index.startTime();
                /* A NOTE ABOUT APPEND ONLY OPTIMIZATIONS:
                 * if we have an autoGeneratedID that comes into the engine we can potentially optimize
                 * and just use addDocument instead of updateDocument and skip the entire version and index lookupVersion across the board.
                 * Yet, we have to deal with multiple document delivery, for this we use a property of the document that is added
                 * to detect if it has potentially been added before. We use the documents timestamp for this since it's something
                 * that:
                 *  - doesn't change per document
                 *  - is preserved in the transaction log
                 *  - and is assigned before we start to index / replicate
                 * NOTE: it's not important for this timestamp to be consistent across nodes etc. it's just a number that is in the common
                 * case increasing and can be used in the failure case when we retry and resent documents to establish a happens before relationship.
                 * for instance:
                 *  - doc A has autoGeneratedIdTimestamp = 10, isRetry = false
                 *  - doc B has autoGeneratedIdTimestamp = 9, isRetry = false
                 *
                 *  while both docs are in in flight, we disconnect on one node, reconnect and send doc A again
                 *  - now doc A' has autoGeneratedIdTimestamp = 10, isRetry = true
                 *
                 *  if A' arrives on the shard first we update maxUnsafeAutoIdTimestamp to 10 and use update document. All subsequent
                 *  documents that arrive (A and B) will also use updateDocument since their timestamps are less than maxUnsafeAutoIdTimestamp.
                 *  While this is not strictly needed for doc B it is just much simpler to implement since it will just de-optimize some doc in the worst case.
                 *
                 *  if A arrives on the shard first we use addDocument since maxUnsafeAutoIdTimestamp is < 10. A` will then just be skipped or calls
                 *  updateDocument.
                 */
                final IndexingStrategy plan;

                if (index.origin() == Operation.Origin.PRIMARY) {
                    plan = planIndexingAsPrimary(index);
                } else {
                    // non-primary mode (i.e., replica or recovery)
                    plan = planIndexingAsNonPrimary(index);
                }

                final IndexResult indexResult;
                if (plan.earlyResultOnPreFlightError.isPresent()) {
                    indexResult = plan.earlyResultOnPreFlightError.get();
                    assert indexResult.getResultType() == Result.Type.FAILURE : indexResult.getResultType();
                } else if (plan.indexIntoLucene) {
                    indexResult = indexIntoLucene(index, plan);
                } else {
                    indexResult = new IndexResult(
                            plan.versionForIndexing, getPrimaryTerm(), plan.seqNoForIndexing, plan.currentNotFoundOrDeleted);
                }
                if (index.origin() != Operation.Origin.LOCAL_TRANSLOG_RECOVERY) {
                    final Translog.Location location;
                    if (indexResult.getResultType() == Result.Type.SUCCESS) {
                        location = translog.add(new Translog.Index(index, indexResult));
                    } else if (indexResult.getSeqNo() != SequenceNumbers.UNASSIGNED_SEQ_NO) {
                        // if we have document failure, record it as a no-op in the translog with the generated seq_no
                        location = translog.add(new Translog.NoOp(indexResult.getSeqNo(), index.primaryTerm(), indexResult.getFailure().getMessage()));
                    } else {
                        location = null;
                    }
                    indexResult.setTranslogLocation(location);
                }
                if (plan.indexIntoLucene && indexResult.getResultType() == Result.Type.SUCCESS) {
                    final Translog.Location translogLocation = trackTranslogLocation.get() ? indexResult.getTranslogLocation() : null;
                    versionMap.maybePutIndexUnderLock(index.uid().bytes(),
                        new IndexVersionValue(translogLocation, plan.versionForIndexing, plan.seqNoForIndexing, index.primaryTerm()));
                }
                if (indexResult.getSeqNo() != SequenceNumbers.UNASSIGNED_SEQ_NO) {
                    localCheckpointTracker.markSeqNoAsCompleted(indexResult.getSeqNo());
                }
                indexResult.setTook(System.nanoTime() - index.startTime());
                indexResult.freeze();
                return indexResult;
            }
        } catch (RuntimeException | IOException e) {
            try {
                maybeFailEngine("index", e);
            } catch (Exception inner) {
                e.addSuppressed(inner);
            }
            throw e;
        }
 }
 
```
ES6.4还是采用try catch的方法.

实现:
```
package org.elasticsearch.index.engine;
    private IndexResult indexIntoLucene(Index index, IndexingStrategy plan)
        throws IOException {
        assert assertSequenceNumberBeforeIndexing(index.origin(), plan.seqNoForIndexing);
        assert plan.versionForIndexing >= 0 : "version must be set. got " + plan.versionForIndexing;
        assert plan.indexIntoLucene;
        index.parsedDoc().updateSeqID(plan.seqNoForIndexing, index.primaryTerm());
        index.parsedDoc().version().setLongValue(plan.versionForIndexing);
        try {
            if (plan.useLuceneUpdateDocument) {
                updateDocs(index.uid(), index.docs(), indexWriter);
            } else {
                assert assertDocDoesNotExist(index, canOptimizeAddDocument(index) == false);
                addDocs(index.docs(), indexWriter);
            }
            return new IndexResult(plan.versionForIndexing, getPrimaryTerm(), plan.seqNoForIndexing, plan.currentNotFoundOrDeleted);
        } catch (Exception ex) {
            if (indexWriter.getTragicException() == null) {
                return new IndexResult(ex, Versions.MATCH_ANY, getPrimaryTerm(), plan.seqNoForIndexing);
            } else {
                throw ex;
            }
        }
    }
```

maybeFailEngine() 从检查是否应当关闭Engine failEngine()
RuntimeException,IOException 两部分
|操作|简介|异常|
|---|----|----|
|createSearchManager|创建搜索管理器|IOException|
|index|索引文档|RuntimeException,IOException|
|delete|删除文档|RuntimeException,IOException|
|sync flush|同步刷新|IOException|
|sync commit|同步提交|IOException|
|flush|刷新|FlushFailedEngineException|
|force merge|手工合并Lucene片段|Exception|

### Engine关闭过程

从maybeFailEngine入手
```
    @Override
    protected boolean maybeFailEngine(String source, Exception e) {
        boolean shouldFail = super.maybeFailEngine(source, e);
        if (shouldFail) {
            return true;
        }
        // Check for AlreadyClosedException -- ACE is a very special
        // exception that should only be thrown in a tragic event. we pass on the checks to failOnTragicEvent which will
        // throw and AssertionError if the tragic event condition is not met.
        if (e instanceof AlreadyClosedException) {
            return failOnTragicEvent((AlreadyClosedException)e);
        } else if (e != null &&
                ((indexWriter.isOpen() == false && indexWriter.getTragicException() == e)
                        || (translog.isOpen() == false && translog.getTragicException() == e))) {
            // this spot on - we are handling the tragic event exception here so we have to fail the engine
            // right away
            failEngine(source, e);
            return true;
        }
        return false;
    }
```

可以发现 failEngine
看看内容
```
 public void failEngine(String reason, @Nullable Exception failure) {
        if (failure != null) {
            maybeDie(reason, failure);
        }
        if (failEngineLock.tryLock()) {
            store.incRef();
            try {
                if (failedEngine.get() != null) {
                    logger.warn(() -> new ParameterizedMessage("tried to fail engine but engine is already failed. ignoring. [{}]", reason), failure);
                    return;
                }
                // this must happen before we close IW or Translog such that we can check this state to opt out of failing the engine
                // again on any caught AlreadyClosedException
                failedEngine.set((failure != null) ? failure : new IllegalStateException(reason));
                try {
                    closeNoLock("engine failed on: [" + reason + "]", closedLatch);
                } finally {
                    if (Lucene.isCorruptionException(failure)) {
                        try {
                            store.markStoreCorrupted(new IOException("failed engine (reason: [" + reason + "])", ExceptionsHelper.unwrapCorruption(failure)));
                        } catch (IOException e) {
                            logger.warn("Couldn't mark store corrupted", e);
                        }
                    }
                    eventListener.onFailedEngine(reason, failure);
                }
            } catch (Exception inner) {
                if (failure != null) inner.addSuppressed(failure);
                // don't bubble up these exceptions up
                logger.warn("failEngine threw exception", inner);
            } finally {
                store.decRef();
            }
        } else {
            logger.debug(() -> new ParameterizedMessage("tried to fail engine but could not acquire lock - engine should be failed by now [{}]", reason), failure);
        }
    }
```

继续看 可以发现
```
failedEngine.set((failure != null) ? failure : new IllegalStateException(reason));

closeNoLock("engine failed on: [" + reason + "]", closedLatch);

store.markStoreCorrupted(new IOException("failed engine (reason: [" + reason + "])", ExceptionsHelper.unwrapCorruption(failure)));

store.decRef();
```

这四句话,查看触发条件,这里先关闭了shard;然后关闭engine;将此Store标记为已损坏;减少此Store实例的refCount。 如果refCount降至0，则此存储区关闭。最后将消息汇报给Master 汇报通过 failAndRemoveShard方法实现或者,查看调用 可以发现
在索引的创建更新失败时候 触发Exception都会调用该方法
failAndRemoveShard
包括在线程池模块在accept的失败时也会触发该方法

### Master的对应处理

收到SHARD_FAILED_ACTION_NAME消息后,Master通过reroute将失败的shard通过reroute迁移到新的节点,并更新集群状态.

### 异常流程总结

1. 如果请求在协调节点的路由节点失败,则会等待集群更新,拿到更新后,进行充实,如果再次失败,则仍旧等集群状态更新,直到超时1分钟为止.若仍失败整体进行失败处理
2. 在主分片写入时,写入是阻塞的.synchronized 重量级锁.只有写入成功才会发起写副本请求.如果主shard写失败,则整个请求被认为处理失败.
3. 无论主分片还是副分片,当写一个doc失败时,集群不会重试,而是关不本地shard,向master汇报,删除以shard为单位.

## 系统特性 一致性 可用性 分区容错性

1. 数据可靠性:通过分片副本和事务日志机制保障数据安全
2. 服务可用性:在可用性和一致性的取舍,默认情况下ES更倾向于可用性,只要主分片可用即可执行写入操作
3. 一致性:写入部分是弱一致性. 只要主分片写成功,数据就可被读取.读取操作在主副分片上可能会得到不同的个结果.
4. 原子性:索引的读写别名更新是原子操作,不会出现中间态.Bulk不是,不能用来实现**事务**
5. 扩展性:主副分片都可以承担读请求,分担系统的负载.

## 思考

1. 副分片写入过程需要重新生成索引,不能单纯复制数据,浪费算力,影响入库速度
2. 磁盘管理能力较差,对坏盘检查和容忍性比HDFS差不少.