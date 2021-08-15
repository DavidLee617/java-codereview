
## ES集群启动流程 

```
electmaster->gateway->allocation->recovery
```
选举过程
1. 参选节点数过半选出临时master
2. 得票过半确定master
3. 节点离开检测集群节点数

gateway
1. 完成选举的Master索取MasterState
2. 参与元信息的选举节点数过半
3. 选举元信息 发布

allocation
1. 向所有节点询问shard信息
2. 磁盘且in-sync列表存在为主分片
3. 磁盘存在,选为副本 否则延迟分配

recovery

1. 从translog恢复主分片
2. 副分片第一阶段
3. 副分片第二节点

### 选取主节点方法

Bully算法的改进 对节点ID进行排序,取ID值最大的节点作为Master
每个节点都运行这个流程 

具体则是 先确定 一个唯一的 大家公认的主节点,再讲最新的机器元数据复制到选举出的主节点上

1. 参选人数需要过半 达到quorum
2. 得票数需要过半 某节点被选为主节点 必须判断加入它的节点数过半 才确认Master身份
3. 当探测到节点离开时间时候,必须判断当前节点数是否过半.
   
```
    public static final ElectionStrategy DEFAULT_INSTANCE = new ElectionStrategy() {
        @Override
        protected boolean satisfiesAdditionalQuorumConstraints(DiscoveryNode localNode, long localCurrentTerm, long localAcceptedTerm,
                                                               long localAcceptedVersion, VotingConfiguration lastCommittedConfiguration,
                                                               VotingConfiguration lastAcceptedConfiguration, VoteCollection joinVotes) {
            return true;
        }
    };

```
配置节点 参数
discovey.zen.minimum_master_nodes

### 选举集群元信息

被选出的Master节点和元信息的新旧程度没有关系.

1. 选举元信息
2. 根据版本号确定最新的元信息
3. 广播该元信息

集群原信息的选举包括两个级别:集群级和索引级. 并不定向存取
信息以节点磁盘存储的为准,需要上报. 
Master只负责调度 不负责读写. 不知道各个shard之间的数据差异

选举完毕后,Master发布首次的集群状态,开始选举shard级元信息

### allocation过程

选举shard级元信息,构建内容路由表,在allocation主要的工作.
```
package org.elasticsearch.cluster.routing.allocation;
```
需要关注的内部类有 **AllocationDecision**,**AllocationService**,**NodeAllocationResult**,**RoutingAllocation** ,**BalancedShardsAllocator**

在初始阶段,所有的shard都处于UNASSIGNED(未分配)状态.ES中通过分配过程决定那个节点拥有哪个分片,从而重构路由表

过程
1. 选取主分片 给每个 shard 都设置一个 UUID ，然后在集群级 的元信息中记录哪个
shard 是最新的 主分片选举过程是通过集群级元信息中记录的“最新主分片的列表”来确定主分片的 ： 汇报信息中存在，并且这个列表中也存在。
2. 选取副分片 从上一个过程汇总的 shard 信息中选择一个副本作为副分片。如果汇
总信息中不存在，则分配一个全新副本的操作依赖于延迟配置项：
```
index.unassigned.node_left.delayed_timeout
```

### 索引恢复

分片分配成功后会进去recovery过程.
主分片的recovery过程不会等待其副分片分配成功才开始revovery.是独立的过程.

这个动作可以简称为刷盘

1. 主分片

类似mysql的设计思路 log恢复 这里主分片从tranlog恢复,重放,最后建立lucene索引,完成主分片的恢复

2. 副分片

副分片恢复复杂 在ES的版本迭代中, 也就是在6.0版本中

phase1: 在朱芬片所在节点,获取translog保留所,从获取到保留锁开始,不受气刷盘清空的影响,调用lucene接口把shard做快照.把这些shard数据复制到副本节点.在phase1完毕前,会向副分片节点发送告知对方启动engine,在phase2开始之前,副分片就可以开始正常处理些请求
phase2: 对translog做快照,快照内包含对phase1开始 ,到执行translog快照期间的新增索引.从而将translog发送到副分片节点进行重放.


这两个阶段需要关注的问题

CAP理论 完整性 一致性 分区容错性 中的

完整性: 如何做到副分片不丢数据? 第二阶段的translog快照包括第一阶段所有的新增操作. 若第一阶段出现lucenecommit 清除translog如何处理 translog.view被移除 引入TranslogDeletionPolicy的概念, 将translog做一个快照来保持translog不被清理 实现在第一阶段允许Lucene commit 换句话说 做个备份

数据一致性:副分片恢复过程有三个阶段,第三阶段会阻塞新的索引操作, 传输第二阶段执行期间新增的translog. 第三阶段被删除,恢复期间没有任何写阻塞过程.**在副分片节点,重放translog时,phase1和phase2之间的写操作与phase2重放操作之间的时序错误和冲突,通过写流程中进行异常处理,对比版本号来过滤过期操作.**

集群启动日志

1. 加载本地集群和索引级别元数据
2. gateway
3. allocation
4. recovery

## 节点的启动与关闭

###  节点的启动包含了哪些东西

1. 解析配置,包括配置文件和命令行参数
2. 检查外部环境和内部环境,JVM版本,操作系统内核参数 例如ulimit等等
3. 初始化内部资源,创建内部模块,初始化探测器
4. 启动各个子模块以及keep-Alive 线程

### 启动流程分析

启动脚本
```
exec \ ＃执行命令
”$JAVA” \ #Java 程序路径
$ES_JAVA_OPTS \ ＃JVM选项
-Des.path.home ＝"$ES_HOME” ＼＃设置 path.home E各径
-Des.path.conf ＝"$ES_PATH_CONF" \ ＃设直 path.conf E各径
-cp "$ES_CLASSPATH” ＼＃设置 java classpath
org.elasticsearch.bootstrap.Elasticsearch \ ＃指定 main 函数所在类
"$＠＂ ＃传递给 main 函数命令行参数
```

```

class Elasticsearch extends EnvironmentAwareCommand {

    private final OptionSpecBuilder versionOption;
    private final OptionSpecBuilder daemonizeOption;
    private final OptionSpec<Path> pidfileOption;
    private final OptionSpecBuilder quietOption;

    // visible for testing
    Elasticsearch() {
        super("Starts Elasticsearch", () -> {}); // we configure logging later so we override the base class from configuring logging
        versionOption = parser.acceptsAll(Arrays.asList("V", "version"),
            "Prints Elasticsearch version information and exits");
        daemonizeOption = parser.acceptsAll(Arrays.asList("d", "daemonize"),
            "Starts Elasticsearch in the background")
            .availableUnless(versionOption);
        pidfileOption = parser.acceptsAll(Arrays.asList("p", "pidfile"),
            "Creates a pid file in the specified path on start")
            .availableUnless(versionOption)
            .withRequiredArg()
            .withValuesConvertedBy(new PathConverter());
        quietOption = parser.acceptsAll(Arrays.asList("q", "quiet"),
            "Turns off standard output/error streams logging in console")
            .availableUnless(versionOption)
            .availableUnless(daemonizeOption);
    }

    /**
     * Main entry point for starting elasticsearch
     */
    public static void main(final String[] args) throws Exception {
        overrideDnsCachePolicyProperties();
        /*
         * We want the JVM to think there is a security manager installed so that if internal policy decisions that would be based on the
         * presence of a security manager or lack thereof act as if there is a security manager present (e.g., DNS cache policy). This
         * forces such policies to take effect immediately.
         */
        System.setSecurityManager(new SecurityManager() {

            @Override
            public void checkPermission(Permission perm) {
                // grant all permissions so that we can later set the security manager to the one that we want
            }

        });
        LogConfigurator.registerErrorListener();
        final Elasticsearch elasticsearch = new Elasticsearch();
        int status = main(args, elasticsearch, Terminal.DEFAULT);
        if (status != ExitCodes.OK) {
            final String basePath = System.getProperty("es.logs.base_path");
            // It's possible to fail before logging has been configured, in which case there's no point
            // suggesting that the user look in the log file.
            if (basePath != null) {
                Terminal.DEFAULT.errorPrintln(
                    "ERROR: Elasticsearch did not exit normally - check the logs at "
                        + basePath
                        + System.getProperty("file.separator")
                        + System.getProperty("es.logs.cluster_name") + ".log"
                );
            }
            exit(status);
        }
    }

    private static void overrideDnsCachePolicyProperties() {
        for (final String property : new String[] {"networkaddress.cache.ttl", "networkaddress.cache.negative.ttl" }) {
            final String overrideProperty = "es." + property;
            final String overrideValue = System.getProperty(overrideProperty);
            if (overrideValue != null) {
                try {
                    // round-trip the property to an integer and back to a string to ensure that it parses properly
                    Security.setProperty(property, Integer.toString(Integer.valueOf(overrideValue)));
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "failed to parse [" + overrideProperty + "] with value [" + overrideValue + "]", e);
                }
            }
        }
    }

    static int main(final String[] args, final Elasticsearch elasticsearch, final Terminal terminal) throws Exception {
        return elasticsearch.main(args, terminal);
    }

    @Override
    protected void execute(Terminal terminal, OptionSet options, Environment env) throws UserException {
        if (options.nonOptionArguments().isEmpty() == false) {
            throw new UserException(ExitCodes.USAGE, "Positional arguments not allowed, found " + options.nonOptionArguments());
        }
        if (options.has(versionOption)) {
            final String versionOutput = String.format(
                Locale.ROOT,
                "Version: %s, Build: %s/%s/%s/%s, JVM: %s",
                Build.CURRENT.getQualifiedVersion(),
                Build.CURRENT.flavor().displayName(),
                Build.CURRENT.type().displayName(),
                Build.CURRENT.hash(),
                Build.CURRENT.date(),
                JvmInfo.jvmInfo().version()
            );
            terminal.println(versionOutput);
            return;
        }

        final boolean daemonize = options.has(daemonizeOption);
        final Path pidFile = pidfileOption.value(options);
        final boolean quiet = options.has(quietOption);

        // a misconfigured java.io.tmpdir can cause hard-to-diagnose problems later, so reject it immediately
        try {
            env.validateTmpFile();
        } catch (IOException e) {
            throw new UserException(ExitCodes.CONFIG, e.getMessage());
        }

        try {
            init(daemonize, pidFile, quiet, env);
        } catch (NodeValidationException e) {
            throw new UserException(ExitCodes.CONFIG, e.getMessage());
        }
    }

    void init(final boolean daemonize, final Path pidFile, final boolean quiet, Environment initialEnv)
        throws NodeValidationException, UserException {
        try {
            Bootstrap.init(!daemonize, pidFile, quiet, initialEnv);
        } catch (BootstrapException | RuntimeException e) {
            // format exceptions to the console in a special way
            // to avoid 2MB stacktraces from guice, etc.
            throw new StartupException(e);
        }
    }

    /**
     * Required method that's called by Apache Commons procrun when
     * running as a service on Windows, when the service is stopped.
     *
     * http://commons.apache.org/proper/commons-daemon/procrun.html
     *
     * NOTE: If this method is renamed and/or moved, make sure to
     * update elasticsearch-service.bat!
     */
    static void close(String[] args) throws IOException {
        Bootstrap.stop();
    }
```

ES_JAVA_OPTS 保存了jvm参数.

### 解析命令行

-E  设定某项配置
-V 打印版本号
-d 后台启动
-h 打印帮助信息
-p 启动时在指定路径创建一个pid文件,保存之前的pid,查看pid来关闭进程
-q 关闭控制台的标准输出和错误输出
-s 终端输出最少信息
-v 终端输出详细信息


配置文件:
elasticsearch.yml
log4j2.properties

### 加载安全配置
这里一般是ES商业版秘钥

### 检查内部环境 
检查lucene版本
检测jar冲突
### 检测外部环境

ES中的节点再是现实被封装为Node模块.在Node中调用其他内部组件,同时对外提供气功和关闭方法,检测在start方法内

回过头 什么事外部环境 -> "Bootstrap Check". JVM OS相关参数
检查被封装在BootstrapCheck接口的实现类中.

```
 public interface BootstrapCheck {
    final class BootstrapCheckResult {
        private final String message;
        private static final BootstrapCheckResult SUCCESS = new BootstrapCheckResult(null);
        public static BootstrapCheckResult success() {
            return SUCCESS;
        }
        public static BootstrapCheckResult failure(final String message) {
            Objects.requireNonNull(message);
            return new BootstrapCheckResult(message);
        }
        private BootstrapCheckResult(final String message) {
            this.message = message;
        }
        public boolean isSuccess() {
            return this == SUCCESS;
        }
        public boolean isFailure() {
            return !isSuccess();
        }
        public String getMessage() {
            assert isFailure();
            assert message != null;
            return message;
        }
    }
    BootstrapCheckResult check(BootstrapContext context);
    default boolean alwaysEnforce() {
        return false;
    }
}
```
在BootstrapChecks(org.elasticsearch.BootstrapChecks)
主要的方面有:
1. 堆大小检测 Xms Xmx 值不同, 要通过检查 必须配备堆大小 //HeapSizeCheck
2. 文件描述符检查 fd需要过多 主要是ulimit //OsXFileDescriptorCheck 10240 FileDescriptorCheck 65535
3. 内存锁定检查 禁用swap 允许进程只使用物理内存 RamRamUsageEstimator 避免使用交换分区. 因为内存足够 用完内存 交换到硬盘会出现很多问题 //MlockallCheck
   开启 Bootstrap.memory_lock选项来让ES锁定内存,在开启本项检查时,而锁定失败的情况下,本项检查会执行失败
4. 最大线程数检查 保证ES进程有创建足够多线程的权限 //MaxNumberOfThreadsCheck
5. 最大虚拟内存 ES集成拥有足够多的地址空间 使用mmap映射到进程地址空间 修改limits.conf //MaxSizeVirtualMemoryCheck
6. 最大文件大小 文件会很大//MaxFileSizeCheck
7. 虚拟内存区域最大数量检查 确保内核允许创建至少262144的内存映射区//MaxMapCountCheck
8. JVM Client模式检查 //ClientJvmCheck
9. 串行搜集检查  串行收集器适合单逻辑CPU机器,不适合ES,对ES心梗有负面影响//UseSerialGCCheck
10. 系统调用过滤器检查 seccomp 过滤漏洞 //SystemCallFilterCheck
11. OnError和OnOOM检查 如果出现上述问题 可以执行人移民过来 会和 seccomp冲突  解决方法使用ExitOnOutOfMemoryError//OnErrorCheck OnOutOfMemoryErrorCheck extends MightForkCheck
12. Easrly-access检查 openjdk提供early-access快照.若要通过 需要运载在稳定的JVM上 //EarlyAccessCheck
13. G1GC检查 //G1GCCheck

ES7.8新增
14. AllPermissionCheck
15. DiscoveryConfiguredCheck


### 启动内部模块 

### 启动 KeepAlive线程

## 节点关闭流程

ES进程会捕获SIGTERM 信号（ kill 命令默认信号）进行处理，调用各模块的 stop方法， 让它们有机会停止服务，安全退出。

进程重启期间,如果主节点被关闭集群会重新选主,会处于无主状态.如果主节点是单独部署的,新主当选后,可以跳过gateway和recovery流程,负责就需要重新分配旧分片;提成其他副本为朱芬片.


### 关闭流程分析
Bootstrap#setup方法中添加了shutdownhook 当进程收到信号调用Node.close方法执行节点关闭流程.
每个模块的Service都实现了doStop和doClose,处理模块的正常关闭流程.总的关闭流程位于Node.close 思路还是通过反射来实现.
顺序是 
1. 关闭快照和HTTPServer 不在响应用户请求
2. 关闭集群拓扑管理不在响应ping请求
3. 关闭网络模块 让节点离线
4. 执行插件关闭流程
5. 关闭索引服务
   
```java
 public synchronized void close() throws IOException {
        synchronized (lifecycle) {
            if (lifecycle.started()) {
                stop();
            }
            if (!lifecycle.moveToClosed()) {
                return;
            }
        }

        logger.info("closing ...");
        List<Closeable> toClose = new ArrayList<>();
        //线程池
        StopWatch stopWatch = new StopWatch("node_close");
        toClose.add(() -> stopWatch.start("node_service"));
        toClose.add(nodeService);
        //关闭快照和HTTPServer 
        toClose.add(() -> stopWatch.stop().start("http"));
        toClose.add(injector.getInstance(HttpServerTransport.class));
        toClose.add(() -> stopWatch.stop().start("snapshot_service"));
        toClose.add(injector.getInstance(SnapshotsService.class));
        toClose.add(injector.getInstance(SnapshotShardsService.class));

        //不在响应用户请求
        toClose.add(() -> stopWatch.stop().start("client"));
        Releasables.close(injector.getInstance(Client.class));
        toClose.add(() -> stopWatch.stop().start("indices_cluster"));
        toClose.add(injector.getInstance(IndicesClusterStateService.class));
        //关闭索引服务
        toClose.add(() -> stopWatch.stop().start("indices"));
        toClose.add(injector.getInstance(IndicesService.class));
        // close filter/fielddata caches after indices
        toClose.add(injector.getInstance(IndicesStore.class));
        toClose.add(injector.getInstance(PeerRecoverySourceService.class));
        //关闭集群拓扑管理
        toClose.add(() -> stopWatch.stop().start("cluster"));
        toClose.add(injector.getInstance(ClusterService.class));
                // 3. 关闭网络模块 让节点离线
        toClose.add(() -> stopWatch.stop().start("node_connections_service"));
        toClose.add(injector.getInstance(NodeConnectionsService.class));
        toClose.add(() -> stopWatch.stop().start("discovery"));
        toClose.add(injector.getInstance(Discovery.class));
        toClose.add(() -> stopWatch.stop().start("monitor"));
        toClose.add(nodeService.getMonitorService());

        toClose.add(() -> stopWatch.stop().start("gateway"));
        toClose.add(injector.getInstance(GatewayService.class));
        toClose.add(() -> stopWatch.stop().start("search"));
        toClose.add(injector.getInstance(SearchService.class));
        toClose.add(() -> stopWatch.stop().start("transport"));
        toClose.add(injector.getInstance(TransportService.class));
        // 执行插件关闭流程
        for (LifecycleComponent plugin : pluginLifecycleComponents) {
            toClose.add(() -> stopWatch.stop().start("plugin(" + plugin.getClass().getName() + ")"));
            toClose.add(plugin);
        }
        toClose.addAll(pluginsService.filterPlugins(Plugin.class));
        //关闭脚本
        toClose.add(() -> stopWatch.stop().start("script"));
        toClose.add(injector.getInstance(ScriptService.class));
        //关闭线程池
        toClose.add(() -> stopWatch.stop().start("thread_pool"));
        toClose.add(() -> injector.getInstance(ThreadPool.class).shutdown());
        //不要实现shutdownNow
        // Don't call shutdownNow here, it might break ongoing operations on Lucene indices.
        // See https://issues.apache.org/jira/browse/LUCENE-7248. We call shutdownNow in
        // awaitClose if the node doesn't finish closing within the specified time.

        toClose.add(() -> stopWatch.stop().start("gateway_meta_state"));
        toClose.add(injector.getInstance(GatewayMetaState.class));

        toClose.add(() -> stopWatch.stop().start("node_environment"));
        toClose.add(injector.getInstance(NodeEnvironment.class));
        //关闭自己
        toClose.add(stopWatch::stop);

        if (logger.isTraceEnabled()) {
            toClose.add(() -> logger.trace("Close times for each service:\n{}", stopWatch.prettyPrint()));
        }
        IOUtils.close(toClose);
        logger.info("closed");
    }
```
Node的stop方法
```java
    private Node stop() {
        if (!lifecycle.moveToStopped()) {
            return this;
        }
        logger.info("stopping ...");

        injector.getInstance(ResourceWatcherService.class).close();
        injector.getInstance(HttpServerTransport.class).stop();

        injector.getInstance(SnapshotsService.class).stop();
        injector.getInstance(SnapshotShardsService.class).stop();
        injector.getInstance(RepositoriesService.class).stop();
        // stop any changes happening as a result of cluster state changes
        injector.getInstance(IndicesClusterStateService.class).stop();
        // close discovery early to not react to pings anymore.
        // This can confuse other nodes and delay things - mostly if we're the master and we're running tests.
        injector.getInstance(Discovery.class).stop();
        // we close indices first, so operations won't be allowed on it
        injector.getInstance(ClusterService.class).stop();
        injector.getInstance(NodeConnectionsService.class).stop();
        nodeService.getMonitorService().stop();
        injector.getInstance(GatewayService.class).stop();
        injector.getInstance(SearchService.class).stop();
        injector.getInstance(TransportService.class).stop();

        pluginLifecycleComponents.forEach(LifecycleComponent::stop);
        // we should stop this last since it waits for resources to get released
        // if we had scroll searchers etc or recovery going on we wait for to finish.
        injector.getInstance(IndicesService.class).stop();
        logger.info("stopped");

        return this;
    }
```
这里运用线程池获取回执是
```java
public synchronized boolean awaitClose
```
方法

### 分片读写过程关闭
写入关闭 写锁 Engine IndiceService的doStop方法会对本届上的索引进并行执行removeIndex,当执行到Engine的flushAndCLose 也会对Engine加写锁. 写入操作已经加入了写锁,此时写锁会等待,知道写入执行完毕. 客户端响应失败,但是写过程继续


```java
    public void flushAndClose() throws IOException {
        if (isClosed.get() == false) {
            logger.trace("flushAndClose now acquire writeLock");
            try (ReleasableLock lock = writeLock.acquire()) {
                logger.trace("flushAndClose now acquired writeLock");
                try {
                    logger.debug("flushing shard on close - this might take some time to sync files to disk");
                    try {
                        // TODO we might force a flush in the future since we have the write lock already even though recoveries
                        // are running.
                        flush();
                    } catch (AlreadyClosedException ex) {
                        logger.debug("engine already closed - skipping flushAndClose");
                    }
                } finally {
                    close(); // double close is not a problem
                }
            }
        }
        awaitPendingClose();
    }
```
读取关闭 读锁  Engine **ES 4.8中只有读锁定义,未实现调用**

### 主节点被关闭

没有特殊处理 正常执行关闭流程当TransportService被关闭之后,集群重新选举新的Master.