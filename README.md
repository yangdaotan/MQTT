# MQTT
mqtt broker

## 功能
- 基于mqtt的通信协议
- 基于netty的通信框架
- 客户端心跳/连接管理
- 下发推送/上行/中转

## 实现
- 去掉了qos1和qos2的复杂实现
- 连接唯一标示clientId:  $groupId-xxx, username: $uid, 将uid放入groupId中
- topic：$groupId/xxx/$uid，表示给groupId中的uid连接发送消息。
- 组播可以很方便的实现
- acceptor线程->io线程->biz logic公共线程->processor线程， 线程隔离

## 集群化
- 利用Store存储clientId在哪个broker上
- mqtt-dispatcher组件读取消息的接收者在哪些broker，进行调度
- mqtt-auth组件进行建连和topic订阅/发布鉴权，可在mqtt-broker的preHandler很方便处理
- mqtt-broker、mqtt-dispatcher、mqtt-auth都是无状态，水平扩展。
- 离线消息和路由等可以放在redis或者其他存储中

## 感谢
- [sofa-bolt](https://github.com/alipay/sofa-bolt) 作为优秀的通信中间件
- [mqtt](http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.pdf)
