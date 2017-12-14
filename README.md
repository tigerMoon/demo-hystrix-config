### 项目说明

通过zookeeper的动态配置功能 实现 hystrix 动态配置

### 访问和启动方式
sprig boot项目， 可以直接通过ide run， 用访问 http://localhost:8080/cache , 查看日志 

### 实现方式

默认 hello command 的超时时间是1000毫秒，我们在程序中sleep 2000毫秒，就会走fallback的流程，日志中可以看到

通过动态修改 hystrix.command.ExampleKey.execution.isolation.thread.timeoutInMilliseconds=4000 属性，程序就会走完正常的run方法

其中commandKey 相关的配置请查看hystrix文档。

### zookeeper 环境搭建

[zookeeper docker](https://hub.docker.com/_/zookeeper/)

ZookeeperConfig (znode和ip配置)

```

docker run --name some-zookeeper --restart always -d zookeeper -P

//端口映射
docker port some-zookeeper

//创建path

docker run -it --rm --link some-zookeeper:zookeeper zookeeper zkCli.sh -server zookeeper

// 创建节点，数据存储在节点上，nodecache
create path /myapp/config "hystrix.command.ExampleKey.execution.isolation.thread.timeoutInMilliseconds=4000"

// 创建节点，pathcache
create path /myapp/config/hystrix.command.ExampleKey.execution.isolation.thread.timeoutInMilliseconds 4000

``` 


### curator 实现

[curator](http://curator.apache.org) 是操作zookeeper的客户端操作库，可以方便的连接到zookeeper，同时还提供方便的许多功能。

hystrix 这个项目使用Caches 这个Recipes 

zookeeper 的watch机制是 one-short的，所以当我们获取通知之后还需要重新再去  watch and get（get的时候再去和缓存做比较，触发事件）。

curator支持的cache种类有3种Path Cache，Node Cache，Tree Cache

1）Path Cache

Path Cache用来观察ZNode的子节点并缓存状态，如果ZNode的子节点被创建，更新或者删除，那么Path Cache会更新缓存，并且触发事件给注册的监听器。

Path Cache是通过PathChildrenCache类来实现的，监听器注册是通过PathChildrenCacheListener。

2）Node Cache

Node Cache用来观察ZNode自身，如果ZNode节点本身被创建，更新或者删除，那么Node Cache会更新缓存，并触发事件给注册的监听器。

Node Cache是通过NodeCache类来实现的，监听器对应的接口为NodeCacheListener。

3）Tree Cache

可以看做是上两种的合体，Tree Cache观察的是所有节点的所有数据。



两种cache的切换在 Application.class 中通过注释切换