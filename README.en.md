# beecomb

* beecomb是一个大规模、高可靠的任务调度系统，与传统定时任务调度系统不同的是beecomb特别适合大规模的延迟（delay）任务、调度（schedule）任务
* 如果你有诸如延时退款、抢票平台等面向N个有各自调度对象的任务场景，beecomb将会特别适合
* beecomb也能作为传统定时任务调度系统

## 架构

![Architecture](./imgs/architecture.png)

* Registry&Metrics 注册中心、高性能轻量数据读写，使用zookeeper
* Master 感知Worker的压力，负载均衡分发任务给Worker；任务自动恢复处理；RESTAPI；可视化web
* Worker 实际的任务调度引擎，管理延迟（delay）任务、调度（schedule）任务；感知Executor的压力，负载均衡决定任务让哪个Executor执行
* Executor 任务执行器，任务执行代码在这里
* Application 使用beecomb的业务应用系统，可以使用beecomb client SDK，可以跟Executor是一个应用

## 特性

* 大规模任务，由于beecomb分布式部署水平扩容、数据水平切分、调度与执行器解耦等设计，大规模集群可支持千万级任务（活跃任务）
* 高可靠保障，数据持久化，集群实例故障时任务自动恢复
* 高精度时间，每个任务在什么时间执行是比较精确的
* 智能压力负载均衡，任务在集群中将根据调度引擎、执行器的cpu、内存、已分配的任务数量进行负载均衡
* 分片并行执行任务
* 集群内NIO通讯
* 可视化Web

## 环境要求
* Java 8及以上
* Zookeeper（推荐3.7.0及以上）
* Mysql 5.7（推荐8.0及以上）


## 快速开始
下面展示如何快速开始使用

### 创建数据库
创建2个database（beecomb使用shardingsphere分库），可以在相同mysql实例上
```bash
create DATABASE `beecomb_0`;
create DATABASE `beecomb_1`;
```

### 初始化数据库
下载 [scripts/mysql文件夹](./scripts/mysql) ，在2个database中都执行初始化脚本 init.sql、mysql_sequence.sql

### 启动master
master是springboot项目
```bash
java -jar beecomb-master.jar --zookeeper.connectString={假设已部署好zookeeper，例如127.0.0.1:2181} --spring.shardingsphere.datasource.ds0.username={beecomb_0的用户名} --spring.shardingsphere.datasource.ds0.password={beecomb_0的密码} --spring.shardingsphere.datasource.ds1.username={beecomb_1的用户名} --spring.shardingsphere.datasource.ds1.password={beecomb_1的密码} 
```

### 启动worker
worker是springboot项目
```bash
java -jar beecomb-worker.jar ...参数与master一样
```

### 编写JobHandler
下载 [beecomb-executor-sample](./beecomb-executor-sample)，下面是QuickStartJobHandler.java代码
```java
public class QuickStartJobHandler implements JobHandler {
	public static final String NAME = "QuickStartAppJobHandler";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public ExecuteJobResult handle(Job job) throws Exception {
		System.out.println("handle job:" + job);

		if (job instanceof DelayJob) {
			int delay = ((DelayJob) job).getDelay();
			System.out.println(delay);
		}

		if (new Random().nextInt(3) == 0) {
			return new ExecuteJobResult();// 执行成功
		} else {
			throw new Exception("执行失败");
		}
	}
}
```

### 启动Executor和注册JobHandler
下载 [beecomb-executor-sample](./beecomb-executor-sample)，下面的代码在 QuickStartApp.java
```java
ZooKeeper zookeeper = new ZooKeeper(zkConnectString);
ZooKeeperSupportInstanceProperties properties = new ZooKeeperSupportInstanceProperties(zookeeper);
BeeCombExecutor beeCombExecutor = BeeCombExecutor.start(EXECUTOR_NAME, properties);
List<JobHandler> jobHandlers = Arrays.asList(new QuickStartJobHandler());
beeCombExecutor.registerReplace(jobHandlers);
```

### 创建Client
下载 [beecomb-executor-sample](./beecomb-executor-sample)，下面的代码在 QuickStartApp.java，在本例中Executor和Application是同一个应用
```java
Authentication authentication = new BasicAuthentication("beecomb", "beecomb");//client认证方式
ZooKeeper zooKeeper = new ZooKeeper(zkConnectString);
ZooKeeperClientProperties clientProperties = new ZooKeeperClientProperties(authentication, zooKeeper);
BeeCombClient beeCombClient = new ZooKeeperBeeCombClient(clientProperties);
```

### 创建任务
下载 [beecomb-executor-sample](./beecomb-executor-sample)，下面的代码在 QuickStartApp.java
```java
/**
 * 创建延迟任务，达到延迟后 {@link QuickStartAppJobHandler} 将触发任务执行
*/
Delay delay = new CreateDelayJobDTO.Delay(3000);
CreateDelayJobDTO job = new CreateDelayJobDTO("QuickStartDelayJob", EXECUTOR_NAME, QuickStartJobHandler.NAME,
    delay);
CreateJobResponse response = beeCombClient.createJob(job);
```

### 查看Web


*更多示例见 SampleApp.java*

