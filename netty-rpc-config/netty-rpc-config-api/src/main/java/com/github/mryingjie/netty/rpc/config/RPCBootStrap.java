package com.github.mryingjie.netty.rpc.config;

import com.github.mryingjie.netty.rpc.registry.NettyRPCRegistryCenter;
import com.github.mryingjie.netty.rpc.registry.RegistryConfig;
import com.github.mryingjie.netty.rpc.remoting.LoadBalance;
import com.github.mryingjie.netty.rpc.remoting.RemotingConsumerClient;
import com.github.mryingjie.netty.rpc.remoting.RemotingProviderClient;

import java.util.ServiceLoader;

/**
 * created by Yingjie Zheng at 2019-12-19 15:22
 * 启动netty服务器的引导类
 */
public class RPCBootStrap {

    private static volatile RPCBootStrap rpcBootStrap;

    private NettyRPCRegistryCenter nettyRPCRegistryService;

    private RemotingProviderClient providerClient;

    private RemotingConsumerClient consumerClient;

    private volatile boolean initedConsumer;

    private volatile  boolean initedProvider;

    private RPCBootStrap() {

    }

    public static RPCBootStrap getInstance() {
        if (rpcBootStrap == null) {
            synchronized (RPCBootStrap.class) {
                if (rpcBootStrap == null) {
                    rpcBootStrap = new RPCBootStrap();
                }
            }
        }
        return rpcBootStrap;
    }

    public RemotingProviderClient initProvider(RegistryConfig registryConfig){
        if(!initedProvider){
            //初始化注册中心实现类
            initRegistryImpl(registryConfig);

            //初始化远程访问实现类
            initProviderClient(registryConfig);
            initedProvider = true;

        }
        return providerClient;
    }

    private void initProviderClient(RegistryConfig registryConfig) {
        if(providerClient == null){
            synchronized (RPCBootStrap.class){
                if(providerClient == null){
                    ServiceLoader<RemotingProviderClient> serviceLoader = ServiceLoader.load(RemotingProviderClient.class);
                    for (RemotingProviderClient remotingProviderClient : serviceLoader) {
                        if(remotingProviderClient.getClass().getName().equals(registryConfig.getProviderClass())){
                            providerClient = remotingProviderClient;
                            break;
                        }
                    }
                    if(providerClient == null){
                        throw new RuntimeException("no type of ["+registryConfig.getProviderClass()+"] find with providerClient");
                    }
                }
            }
        }
        providerClient.start(registryConfig.getPort(), nettyRPCRegistryService);
    }

    public RemotingConsumerClient initConsumer(RegistryConfig registryConfig) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if(!initedConsumer){
            //初始化注册中心实现类
            initRegistryImpl(registryConfig);

            //初始化远程访问类
            initConsumerClient(registryConfig);
            initedConsumer = true;

        }
        return  consumerClient;
    }

    private void initConsumerClient(RegistryConfig registryConfig) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(consumerClient == null){
            synchronized (RPCBootStrap.class){
                if(consumerClient == null){
                    ServiceLoader<RemotingConsumerClient> remotingConsumerClients = ServiceLoader.load(RemotingConsumerClient.class);
                    for (RemotingConsumerClient remotingConsumerClient : remotingConsumerClients) {
                        if(remotingConsumerClient.getClass().getName().equals(registryConfig.getConsumerClass())){
                            consumerClient = remotingConsumerClient;
                            break;
                        }
                    }
                    if(consumerClient == null){
                        throw new RuntimeException("no type of ["+registryConfig.getConsumerClass()+"] find with consumerClient");
                    }
                }
            }
        }
        consumerClient.start(nettyRPCRegistryService, (LoadBalance) Class.forName(registryConfig.getLoadBalanceClass()).newInstance());
    }


    private void initRegistryImpl(RegistryConfig registryConfig) {
        if(nettyRPCRegistryService == null){
            synchronized (RPCBootStrap.class){
                if(nettyRPCRegistryService == null){
                    //加载 注册中心的实现类
                    ServiceLoader<NettyRPCRegistryCenter> serviceLoader = ServiceLoader.load(NettyRPCRegistryCenter.class);
                    for (NettyRPCRegistryCenter next : serviceLoader) {
                        if (next.getClass().getName().equals(registryConfig.getRegistryClass())) {
                            next.init(registryConfig);
                            nettyRPCRegistryService = next;
                            break;
                        }
                    }
                    if(nettyRPCRegistryService == null){
                        throw new RuntimeException("no type of ["+registryConfig.getRegistryClass()+"] config find with classpath of META-INFO/service ");
                    }
                }
            }
        }
    }

    public NettyRPCRegistryCenter getNettyRPCRegistryService() {
        return nettyRPCRegistryService;
    }

    public RemotingProviderClient getProviderClient() {
        return providerClient;
    }

    public RemotingConsumerClient getConsumerClient() {
        return consumerClient;
    }
}
