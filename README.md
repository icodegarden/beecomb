# 概览

* beecomb是一个大规模、高可靠的任务调度系统，与传统定时任务调度系统不同的是beecomb特别适合大规模的延迟（delay）任务、调度（schedule）任务
* 如果你有诸如延时退款、抢票平台等面向N个有各自调度对象的任务场景，beecomb将会特别适合
* beecomb也能作为传统定时任务调度系统

# 架构

![Architecture](./imgs/architecture.png)

* Registry&Metrics 注册中心、高性能轻量数据读写，使用zookeeper
* Master 感知Worker的压力，负载均衡分发任务给Worker；任务自动恢复处理；RESTAPI；可视化web
* Worker 实际的任务调度引擎，管理延迟（delay）任务、调度（schedule）任务；感知Executor的压力，负载均衡决定任务让哪个Executor执行
* Executor 任务执行器，任务执行代码在这里
* Application 使用beecomb的业务应用系统，可以使用beecomb client SDK，可以跟Executor是一个应用

# 特性

* 大规模任务，由于beecomb分布式部署水平扩容、数据水平切分、调度与执行器解耦等设计，大规模集群可支持千万级任务（活跃任务）
* 高可靠保障，数据持久化，集群实例故障时任务自动恢复
* 高精度时间，每个任务在什么时间执行是比较精确的
* 智能压力负载均衡，任务在集群中将根据调度引擎、执行器的cpu、内存、已分配的任务数量进行负载均衡
* 分片并行执行任务
* 集群内NIO通讯
* 可视化Web

# 环境要求
* Java 8及以上
* Zookeeper（推荐3.7.0及以上）
* Mysql 5.7（推荐8.0及以上）


# 快速开始
下面展示如何快速开始使用

## 创建数据库
创建2个database（beecomb使用shardingsphere分库），可以在相同mysql实例上
```bash
create DATABASE `beecomb_0`;
create DATABASE `beecomb_1`;
```

## 初始化数据库
下载 [scripts/mysql文件夹](./scripts/mysql) ，在2个database中都执行初始化脚本 init.sql、mysql_sequence.sql

## 启动master
master是springboot项目
```bash
java -jar beecomb-master.jar --zookeeper.connectString={假设已部署好zookeeper，例如127.0.0.1:2181} --spring.shardingsphere.datasource.ds0.jdbc-url=jdbc:mysql://{ip:port}/beecomb_0 --spring.shardingsphere.datasource.ds0.username={beecomb_0的用户名} --spring.shardingsphere.datasource.ds0.password={beecomb_0的密码} --spring.shardingsphere.datasource.ds1.jdbc-url=jdbc:mysql://{ip:port}/beecomb_1 --spring.shardingsphere.datasource.ds1.username={beecomb_1的用户名} --spring.shardingsphere.datasource.ds1.password={beecomb_1的密码} 
```

## 启动worker
worker是springboot项目
```bash
java -jar beecomb-worker.jar ...参数与master一样
```

## 编写JobHandler
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

## 启动Executor和注册JobHandler
下载 [beecomb-executor-sample](./beecomb-executor-sample)，下面的代码在 QuickStartApp.java
```java
ZooKeeper zookeeper = new ZooKeeper(zkConnectString);
ZooKeeperSupportInstanceProperties properties = new ZooKeeperSupportInstanceProperties(zookeeper);
BeeCombExecutor beeCombExecutor = BeeCombExecutor.start(EXECUTOR_NAME, properties);
List<JobHandler> jobHandlers = Arrays.asList(new QuickStartJobHandler());
beeCombExecutor.registerReplace(jobHandlers);
```

## 创建Client
下载 [beecomb-executor-sample](./beecomb-executor-sample)，下面的代码在 QuickStartApp.java，在本例中Executor和Application是同一个应用
```java
Authentication authentication = new BasicAuthentication("beecomb", "beecomb");//client认证方式
ZooKeeper zooKeeper = new ZooKeeper(zkConnectString);
ZooKeeperClientProperties clientProperties = new ZooKeeperClientProperties(authentication, zooKeeper);
BeeCombClient beeCombClient = new ZooKeeperBeeCombClient(clientProperties);
```

## 创建任务
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

## 更多示例
更多示例见 SampleApp.java

# 开发者
## Executor
任务的执行是在Executor中的JobHandler中进行的，每个Executor都可以有N个JobHandler，开发者需要做的就是编写JobHandler
```java
public class QuickStartJobHandler implements JobHandler {	
        @Override
	public String name() {		
	}
	@Override
	public ExecuteJobResult handle(Job job) throws Exception {		
	}
}
```
启动Executor并注册JobHandler
```java
BeeCombExecutor beeCombExecutor = BeeCombExecutor.start(EXECUTOR_NAME, properties);
List<JobHandler> jobHandlers = Arrays.asList(new QuickStartJobHandler());
beeCombExecutor.registerReplace(jobHandlers);
```
可以看到JobHandler有name，BeeCombExecutor也有name，任务该由哪个Executor的哪个JobHandler处理，正是由name决定的，创建job时每个job都需要executorName和jobHandlerName

## Application
业务应用需要能够创建、查询任务，java语言可以直接使用Client SDK，非java语言可以使用restapi
```java
BeeCombClient beeCombClient = new ZooKeeperBeeCombClient(clientProperties);
beeCombClient.createJob(...);
```
## restapi

# 部署
## Zookeeper
请自行部署，这里不再赘述
## Mysql
beecomb使用shardingsphere分库，默认需要2个库（可以在相同的mysql实例），支持自定义多个库，下面以4个库为例
```properties
# 配置真实数据源
spring.shardingsphere.datasource.names=ds0,ds1,ds2,ds3  名称可以自定义，但需要跟下面对应，最好按此规则编写

# 配置第 1 个数据源
spring.shardingsphere.datasource.ds0.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds0.jdbc-url=jdbc:mysql://127.0.0.1:3306/beecomb_0?setUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=123456

# 配置第 2 个数据源
spring.shardingsphere.datasource.ds1.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.jdbc-url=jdbc:mysql://127.0.0.1:3306/beecomb_1?setUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=123456

# 配置第 3 个数据源
spring.shardingsphere.datasource.ds2.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds2.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds2.jdbc-url=jdbc:mysql://127.0.0.1:3306/beecomb_2?setUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai
spring.shardingsphere.datasource.ds2.username=root
spring.shardingsphere.datasource.ds2.password=123456

# 配置第 4 个数据源
spring.shardingsphere.datasource.ds3.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds3.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds3.jdbc-url=jdbc:mysql://127.0.0.1:3306/beecomb_3?setUnicode=true&characterEncoding=utf8&useSSL=false&autoReconnect=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai
spring.shardingsphere.datasource.ds3.username=root
spring.shardingsphere.datasource.ds3.password=123456

# 标准分片表配置
spring.shardingsphere.rules.sharding.tables.job_main.actual-data-nodes=ds$->{0..3}.job_main   这里只可以修改ds名和{}内的值，0..3表示库0，1，2，3
spring.shardingsphere.rules.sharding.tables.job_detail.actual-data-nodes=ds$->{0..3}.job_detail
spring.shardingsphere.rules.sharding.tables.delay_job.actual-data-nodes=ds$->{0..3}.delay_job
spring.shardingsphere.rules.sharding.tables.schedule_job.actual-data-nodes=ds$->{0..3}.schedule_job
spring.shardingsphere.rules.sharding.tables.job_execute_record.actual-data-nodes=ds$->{0..3}.job_execute_record
spring.shardingsphere.rules.sharding.tables.job_recovery_record.actual-data-nodes=ds$->{0..3}.job_recovery_record

# 分片算法配置    
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].name=group0
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].rangeGte=0
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].rangeLt=40000000
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].mod=4
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].modLoadBalance={"ds0":[0],"ds1":[1],"ds2":[2],"ds3":[3]}
```
通过以上示例可以看出分多少库是可以自定义的，并且分片算法配置可以让数据 避免热点、避免迁移，下面展示当上面的几个库即将不够用时，继续增加库如何避免迁移
```properties
# 分片算法配置    这是原来那些库的配置，不用变
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].name=group0  组名称可以自定义
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].rangeGte=0   表示该组的库的任务id范围支持从0开始
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].rangeLt=40000000  表示该组的库的任务id范围支持到4000万结束
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].mod=4   该组任务id以4取模，因为4个库平均分摊
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].modLoadBalance={"ds0":[0],"ds1":[1],"ds2":[2],"ds3":[3]}  取模结果是多少分别存入哪个库

# 分片算法配置    这是新增的2个库
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].name=group1  组名称可以自定义
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].rangeGte=40000000  表示该组的库的任务id范围支持从4000万开始，跟上面的组衔接
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].rangeLt=70000000  表示该组的库的任务id范围支持到7000万结束
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].mod=3  该组任务id以2取模，尽管库是2个，但这里假设的是2个库的硬件不一样，其中1个的硬件是2倍能力，因此2倍的库可以承担2倍的数据
spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].modLoadBalance={"ds4":[0,1],"ds5":[2]}  取模结果是0和1的存到高性能的库，这里上面省略了新增库的配置
```
通过以上示例可以看出数据库可以随着业务发展逐渐的增加，实现水平扩容
## Master

# 参数说明

