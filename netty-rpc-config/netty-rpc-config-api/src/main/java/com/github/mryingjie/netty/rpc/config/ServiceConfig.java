package com.github.mryingjie.netty.rpc.config;

import com.github.mryingjie.netty.rpc.registry.RegistryConfig;
import com.github.mryingjie.netty.rpc.remoting.RemotingProviderClient;


/**
 * created by Yingjie Zheng at 2019-12-19 15:21
 */
public class ServiceConfig extends BaseConfig<Object> {

    private Object ref;

    private RemotingProviderClient client;

    public ServiceConfig(Class<?> interfaceClass, RegistryConfig registryConfig) {
        super(interfaceClass, registryConfig);
    }

    public synchronized void export() {
        rpcBootStrap = RPCBootStrap.getInstance();
        client = rpcBootStrap.initProvider(getRegistryConfig());
        client.exportService(getInterfaceClass(), ref);
    }

    public ServiceConfig() {
        super();
    }


    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
