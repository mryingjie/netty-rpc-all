package com.github.mryingjie.netty.rpc.registry;

import com.github.mryingjie.netty.rpc.common.HostAndPort;

import java.util.List;

/**
 * created by Yingjie Zheng at 2019-12-19 16:55
 */
public interface NettyRPCRegistryCenter {


    /**
     * 根据配置 连接到注册中心
     */
    void init(RegistryConfig registryConfig);

    /**
     * 获取服务提供者的节点IP和端口信息
     */
    List<HostAndPort> getProviderAddress(String service);

    boolean deleteProviderAddress(String services,HostAndPort hostAndPort);

    void registryProvider(String service);




    List<HostAndPort> getConsumerAddress(String service);

    void registryConsumer(String service);

    void deleteConsumerAddress(String services,HostAndPort hostAndPort);






}
