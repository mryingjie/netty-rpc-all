package com.github.mryingjie.netty.rpc.remoting;

import com.github.mryingjie.netty.rpc.common.HostAndPort;
import com.github.mryingjie.netty.rpc.registry.NettyRPCRegistryCenter;
import io.netty.channel.Channel;


/**
 * created by Yingjie Zheng at 2019-12-24 14:27
 */
public interface RemotingConsumerClient {



    void start(NettyRPCRegistryCenter nettyRPCRegistryService, LoadBalance loadBalance);

    Response send(Request request) throws InterruptedException, ClassNotFoundException;


    Channel doConnect(HostAndPort address) throws InterruptedException;
}
