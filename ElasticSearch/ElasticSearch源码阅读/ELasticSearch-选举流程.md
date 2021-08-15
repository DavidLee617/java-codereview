# 设计思想
所有分布式系统都要处理一致性问题
两组:
试图避免不一致
定义发生不一致如何协调

# 为什么主从模式

两种选择
主从

分布式哈希表 

优点: 支持每小时数千个节点的离开和加入 

缺点: 在不了解异构网络中工作,响应时间约为4-10跳

综合考虑 网络稳定 不需要那么多节点 所以主从

# 选举算法

1. Bully 简单 高效 缺陷 当master节点不稳定 会进行重复推举 ES在这里通过延迟推举解决重复推举的问题,但是容易产生双主的情况, 这里需要再加一个 得票人数过半 来解决

2. Paxos 复杂 故障模式较为复杂

# 流程概述

1. 每个节点计算最小的已知节点ID,该节点为临时Master.向该节点发送领导投票
2. 如果一个节点收到足够多的票数,并且该节点也为自己投票,则为Master,并且开始发布集群状态.

**只有node.master为true的节点投票才有效**

法定人数可调 最小值应该是具有资格的节点数n/2+1

# 流程分析

所属模块 

```java
package org.elasticsearch.discovery.zen;
package org.elasticsearch.discovery;
```
常用的模块 ZenDiscovery:ES6.4

es 7.8被移除.  改成 peerRequest

1. 选举临时节点
2. 判断该节点是不是Master节点
   1. 确立Master 启动NodesFD
   2. 加入集群 启动MasterFD

执行本流程的线程池:**generic**

## 选举临时Master

1. ping 所有的节点,获取节点列表 fullPingResponses ping 结果不包含本节点 把本节点单独添加到fullPingResponses中
2. 构建两个列表.
activeMaster列表: 存储集群当前活跃Master列表.遍历第一步获取的所有节点,将每个节点所认为的当前Master节点加入acticeMasters列表.在遍历过程中,如果配置了discovery.zen.master_election.ignore_non_master_pings 为true,而节点又不具备Master资格,则跳过该节点

流程设计:

1. 遍历fullPingResponses
2. 是本节点
    1. 是本节点 跳过
    2. 否跳转3
3. ignore_non_master_pings为true
   1. 否 添加到节点认为的Master到acitiveMasters
   2. 是 node.master为true
      1. 是 重复 3.1
      2. 否 不执行

过程是将已存在的Master加入activeMaster列表.
如果集群已存在Master 每个节点 都记录了当前Master是哪个 考虑到异常情况, 各个节点看到的Master不同

masterCandidates列表:存储master候选者列表.遍历第一步获取列表,去掉不具备Master的节点,添加到这个列表中.

如果activeMaster为空,则从masterCandidates中选举,成败都有可能. 如果不为空,则从acitveMaster中选择最合适的作为Master.

流程:
1. ping 获取节点列表
2. 构建activeMaster和masterCandidates
3. 若activeMasters为空
   1. 是 从 masterCandidates中选主
      1. 从masterCandidates中选主
      2. 候选者达到法定人数
         1. 是 选主成功
         2. 否 返回1. 重新开始
   2. 否 从activeMastes中选择
      1. 选主成功
源码如下

是否拥有足够的候选者

```
public boolean hasEnoughCandidates(Collection<MasterCandidate> candidates) {
        if (candidates.isEmpty()) {
            return false;
        }
        if (minimumMasterNodes < 1) {
            return true;
        }
        assert candidates.stream().map(MasterCandidate::getNode).collect(Collectors.toSet()).size() == candidates.size() :
            "duplicates ahead: " + candidates;
        return candidates.size() >= minimumMasterNodes;
    }
```

候选者选举
```
public MasterCandidate electMaster(Collection<MasterCandidate> candidates) {
        assert hasEnoughCandidates(candidates);
        List<MasterCandidate> sortedCandidates = new ArrayList<>(candidates);
        sortedCandidates.sort(MasterCandidate::compare);
        return sortedCandidates.get(0);
        //一句话的bully算法
    }
```

```
        public static int compare(MasterCandidate c1, MasterCandidate c2) {
            // we explicitly swap c1 and c2 here. the code expects "better" is lower in a sorted
            // list, so if c2 has a higher cluster state version, it needs to come first.
            int ret = Long.compare(c2.clusterStateVersion, c1.clusterStateVersion);
            if (ret == 0) {
                ret = compareNodes(c1.getNode(), c2.getNode());
            }
            return ret;
        }
```

activeMaster选举

```
    public DiscoveryNode tieBreakActiveMasters(Collection<DiscoveryNode> activeMasters) {
        return activeMasters.stream().min(ElectMasterService::compareNodes).get();
    }

```

## 投票和得票的实现

```
    public static class JoinRequest extends TransportRequest {

        DiscoveryNode node;

        public JoinRequest() {
        }

        private JoinRequest(DiscoveryNode node) {
            this.node = node;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            node = new DiscoveryNode(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            node.writeTo(out);
        }
    }
```
```
    private class JoinRequestRequestHandler implements TransportRequestHandler<JoinRequest> {

        @Override
        public void messageReceived(final JoinRequest request, final TransportChannel channel) throws Exception {
            listener.onJoin(request.node, new JoinCallback() {
                @Override
                public void onSuccess() {
                    try {
                        channel.sendResponse(TransportResponse.Empty.INSTANCE);
                    } catch (Exception e) {
                        onFailure(e);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    try {
                        channel.sendResponse(e);
                    } catch (Exception inner) {
                        inner.addSuppressed(e);
                        logger.warn("failed to send back failure on join request", inner);
                    }
                }
            });
        }
    }
```

## 确立Master或加入集群

临时Master有两种情况
临时Master是否是本节点

是:
1. 等待足够多的具备Master资格的节点加入本节点,完成选举
2. 超时后还没有满足数量的join,则选举失败,进行新一轮的选举
3. 成功后发布新的clusterState
   
如果其他节点被选为Master:

1. 不再接受其他节点的join请求
2. 向Master发送加入请求,并等待回复.超时时间默认为1分钟,如果遇到异常,则默认重试三次.步骤在joinElectedMaster方法中实现.
3. 最终当选的Master辉县发布集群状态,才确认客户的join请求,joinElectedMaster的返回代表收到了join请求的确认,并且已经收到了集群状态.本步骤检查收到的集群状态中的Master节点如果为空,或者当选的Master不是之前选择的节点,则重新选举

# 节点失效检测

选主流程已执行完毕,Master身份已确认,非Master节点已加入集群.

节点失效结案侧会监控节点是否离线,然后处理其中的异常.失效检测是选主流程之后不可或缺的步骤,不执行失效检测可能会产生多主. 

失效探测器:

1. 在Master节点,启动NodeFaultDetection,简称NodesFD.定期探测加入集群的节点是否活跃.
2. 在非Master节点的启动MasterFaultDetection,简称MasterFD.定期探测Master节点是否活跃.


NodesFaultDetection和MasterFaultDetection都是通过定期发送ping请求,探测节点是否正常,但失败次数达到一定次数,或者受到底层链接模块的节点离线通知时,开始处理节点离开事件.

## NodeFaultDetection

```
package org.elasticsearch.discovery.zen;
```

检测总结点数是否达到了法定节点数,如果不足,则会放弃Master身份,重新加入集群.

NodesFaultDetection避免因为网络分区场景下产生双主.
事件处理主要实现如下:
ZenDiscovery::handleNodeFailure 中执行NodeRemovalClusterTaskExecutor.execute 线程池操作

```

private void handleNodeFailure(final DiscoveryNode node, final String reason) {
        if (lifecycleState() != Lifecycle.State.STARTED) {
            // not started, ignore a node failure
            return;
        }
        if (!localNodeMaster()) {
            // nothing to do here...
            return;
        }
        removeNode(node, "zen-disco-node-failed", reason);
}
private void removeNode(final DiscoveryNode node, final String source, final String reason) {
        masterService.submitStateUpdateTask(
                source + "(" + node + "), reason(" + reason + ")",
                new NodeRemovalClusterStateTaskExecutor.Task(node, reason),
                ClusterStateTaskConfig.build(Priority.IMMEDIATE),
                nodeRemovalExecutor,
                nodeRemovalExecutor);
    }
    public <T> void submitStateUpdateTask(String source, T task,
                                          ClusterStateTaskConfig config,
                                          ClusterStateTaskExecutor<T> executor,
                                          ClusterStateTaskListener listener) {
        submitStateUpdateTasks(source, Collections.singletonMap(task, listener), config, executor);
    }
    public <T> void submitStateUpdateTasks(final String source,
                                           final Map<T, ClusterStateTaskListener> tasks, final ClusterStateTaskConfig config,
                                           final ClusterStateTaskExecutor<T> executor) {
        if (!lifecycle.started()) {
            return;
        }
        final ThreadContext threadContext = threadPool.getThreadContext();
        final Supplier<ThreadContext.StoredContext> supplier = threadContext.newRestorableContext(true);
        try (ThreadContext.StoredContext ignore = threadContext.stashContext()) {
            threadContext.markAsSystemContext();

            List<Batcher.UpdateTask> safeTasks = tasks.entrySet().stream()
                .map(e -> taskBatcher.new UpdateTask(config.priority(), source, e.getKey(), safe(e.getValue(), supplier), executor))
                .collect(Collectors.toList());
            taskBatcher.submitTasks(safeTasks, config.timeout());
        } catch (EsRejectedExecutionException e) {
            // ignore cases where we are shutting down..., there is really nothing interesting
            // to be done here...
            if (!lifecycle.stoppedOrClosed()) {
                throw e;
            }
        }
    }
```
调用链如上所示

## MasterFaultDetection

重新加入集群,本质上就是该节点重新执行一遍选主的流程.

```

    private void handleMasterGone(final DiscoveryNode masterNode, final Throwable cause, final String reason) {
        if (lifecycleState() != Lifecycle.State.STARTED) {
            // not started, ignore a master failure
            return;
        }
        if (localNodeMaster()) {
            // we might get this on both a master telling us shutting down, and then the disconnect failure
            return;
        }

        logger.info(() -> new ParameterizedMessage("master_left [{}], reason [{}]", masterNode, reason), cause);

        synchronized (stateMutex) {
            if (localNodeMaster() == false && masterNode.equals(committedState.get().nodes().getMasterNode())) {
                // flush any pending cluster states from old master, so it will not be set as master again
                pendingStatesQueue.failAllStatesAndClear(new ElasticsearchException("master left [{}]", reason));
                rejoin("master left (reason = " + reason + ")");
            }
        }
    }
```

综上所述,选举流程在集群中启动,从无助状态到产生新主时执行,同事集群在正常运行过程中,Master探测到节点离开,非Master节点探测到Master离开都会执行.