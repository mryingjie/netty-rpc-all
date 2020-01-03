package com.github.mryingjie.netty.rpc.config;

import com.github.mryingjie.netty.rpc.registry.RegistryConfig;
import com.github.mryingjie.netty.rpc.remoting.RemotingConsumerClient;

/**
 * created by Yingjie Zheng at 2019-12-19 15:21
 */
public class ReferenceConfig<T> extends BaseConfig<T> {

    private RemotingConsumerClient remotingConsumerClient;


    public ReferenceConfig(Class interfaceClass, RegistryConfig registryConfig) {
        super(interfaceClass, registryConfig);
    }

    public synchronized RemotingConsumerClient init(){
        if(initialized && remotingConsumerClient != null){
            return remotingConsumerClient;
        }
        if (rpcBootStrap == null) {
            rpcBootStrap = RPCBootStrap.getInstance();
            try {
                remotingConsumerClient = rpcBootStrap.initConsumer(getRegistryConfig());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        initialized = true;
        return remotingConsumerClient;
    }


}
