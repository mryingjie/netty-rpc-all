package com.github.mryingjie.netty.rpc.remoting;

import com.github.mryingjie.netty.rpc.registry.NettyRPCRegistryCenter;

/**
 * created by Yingjie Zheng at 2019-12-23 14:07
 */

public interface RemotingProviderClient {

    void start(int port, NettyRPCRegistryCenter registry);

    void exportService(Class serviceClass, Object service);

}
