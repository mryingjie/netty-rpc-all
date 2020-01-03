# netty-rpc

## 总述
整体代码结构类似于dubbo的架构，但是并没有实现dubbo的spi机制，使用的还是原生的jdk的spi机制。适合于想了解学习dubbo-rpc但是直接看dubbo源码又有些困难且无从下手的同学学习和参考。代码的实现参考了dubbo源码以及这位同学的代码(https://github.com/taoxun/netty_rpc)
   
## 前提
1、熟悉java反射机制  
2、熟悉netty通讯框架  
3、熟悉java的spi机制  
4、熟悉zookeeper的原理及客户端使用  
5、熟悉spring及spring-boot框架 

## 实现的功能
1、基本的rpc实现使用方法与dubbo类似但不支持xml配置  
2、注册中心(实现了使用zookeeper为注册中心，支持扩展)  
3、负载均衡(仅简单实现了随机的策略，支持扩展)  
4、远程通讯框架使用的netty，支持扩展  
5、仅支持在spring-boot中配置使用  
6、底层序列化使用fastJson暂时不支持扩展  

## 快速开始
1、将代码clone到本地  
2、在netty-rpc-demo项目中有consumer和provider两个springboot项目，分别在application.yaml中配置zk地址。启动  
3、浏览器访问http://localhost:8080/insert   和  http://localhost:8080/getAllUser  
   