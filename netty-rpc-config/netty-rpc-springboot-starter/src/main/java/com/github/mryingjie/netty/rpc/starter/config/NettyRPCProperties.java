package com.github.mryingjie.netty.rpc.starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * created by Yingjie Zheng at 2019-12-23 14:22
 */
@ConditionalOnProperty(prefix = "netty.rpc")
@ConfigurationProperties(prefix = "netty.rpc")
public class NettyRPCProperties {


    private String registryClass = "com.github.mryingjie.netty.rpc.registry.ZkRegistryService";

    private String protocolClass;

    private String providerClass = "com.github.mryingjie.netty.rpc.remoting.ProviderClient";

    private String consumerClass = "com.github.mryingjie.netty.rpc.remoting.ConsumerClient";

    private String loadBalanceClass = "com.github.mryingjie.netty.rpc.remoting.RandomLoadBalance";


    private boolean enableProvider;

    private boolean enableConsumer;


    /**
     * 注册中心的地址
     */
    private String registryAddress;

    /**
     * 本地服务端口
     */
    private int serverPort;

    public String getLoadBalanceClass() {
        return loadBalanceClass;
    }

    public void setLoadBalanceClass(String loadBalanceClass) {
        this.loadBalanceClass = loadBalanceClass;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getRegistryClass() {
        return registryClass;
    }

    public void setRegistryClass(String registryClass) {
        this.registryClass = registryClass;
    }

    public String getProtocolClass() {
        return protocolClass;
    }

    public void setProtocolClass(String protocolClass) {
        this.protocolClass = protocolClass;
    }


    public boolean isEnableProvider() {
        return enableProvider;
    }

    public void setEnableProvider(boolean enableProvider) {
        this.enableProvider = enableProvider;
    }

    public boolean isEnableConsumer() {
        return enableConsumer;
    }

    public void setEnableConsumer(boolean enableConsumer) {
        this.enableConsumer = enableConsumer;
    }


    public String getProviderClass() {
        return providerClass;
    }

    public void setProviderClass(String providerClass) {
        this.providerClass = providerClass;
    }

    public String getConsumerClass() {
        return consumerClass;
    }

    public void setConsumerClass(String consumerClass) {
        this.consumerClass = consumerClass;
    }
}
