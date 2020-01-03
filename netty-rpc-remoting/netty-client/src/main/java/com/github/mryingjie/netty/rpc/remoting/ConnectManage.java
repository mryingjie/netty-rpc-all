package com.github.mryingjie.netty.rpc.remoting;

import com.github.mryingjie.netty.rpc.common.HostAndPort;
import com.github.mryingjie.netty.rpc.registry.NettyRPCRegistryCenter;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConnectManage {

    private NettyRPCRegistryCenter nettyRPCRegistryService;

    private RemotingConsumerClient consumerClient;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    //Map<ClassName,Set<Channel>>  每一个服务都有多个提供者
    private Map<String, Set<Channel>> classNameChannel = new ConcurrentHashMap<>();

    private LoadBalance loadBalance;

    //Map<address,Set<ClassName>> 每一个提供者都能提供多个服务
    // private Map<String, Set<String>> addressClassName = new ConcurrentHashMap<>();

    //所有存活的提供者
    private Map<HostAndPort, Channel> providers = new ConcurrentHashMap<>();

    public ConnectManage(NettyRPCRegistryCenter nettyRPCRegistryService, RemotingConsumerClient consumerClient, LoadBalance loadBalance) {
        this.nettyRPCRegistryService = nettyRPCRegistryService;
        this.consumerClient = consumerClient;
        this.loadBalance = loadBalance;
    }

    public Channel chooseChannel(String className) throws InterruptedException {
        Set<Channel> channels = classNameChannel.get(className);
        if (channels == null || channels.size() == 0) {
            List<HostAndPort> providerAddress = nettyRPCRegistryService.getProviderAddress(className);
            if (providerAddress != null && providerAddress.size() > 0) {
                channels = channels == null ? new CopyOnWriteArraySet<>() : channels;
                for (HostAndPort address : providerAddress) {
                    Channel channel;
                    if (!providers.containsKey(address)) {
                        channel = consumerClient.doConnect(address);
                    }else {
                        channel = providers.get(address);
                    }
                    channels.add(channel);
                }

            }

        }
        if(channels == null || channels.size() == 0){
            throw new RuntimeException("no provider of service:["+className+"]");
        }

        classNameChannel.put(className, channels);
        return loadBalance.loadBalance(channels);
    }


    public void removeChannel(HostAndPort hostAndPort, Channel channel) {
        providers.remove(hostAndPort);
        for (Set<Channel> value : classNameChannel.values()) {
            value.remove(channel);
        }
    }
}
