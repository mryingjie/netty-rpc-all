package com.github.mryingjie.netty.rpc.config;


import com.github.mryingjie.netty.rpc.registry.RegistryConfig;

/**
 * created by Yingjie Zheng at 2019-12-19 16:00
 */
public abstract class BaseConfig<T> {

    private Class<T> interfaceClass;

    private RegistryConfig registryConfig;


    RPCBootStrap rpcBootStrap;

    volatile boolean initialized;


    public RPCBootStrap getRpcBootStrap() {
        return rpcBootStrap;
    }



    public void setRpcBootStrap(RPCBootStrap rpcBootStrap) {
        this.rpcBootStrap = rpcBootStrap;
    }

    public BaseConfig(Class interfaceClass, RegistryConfig registryConfig) {
        this.interfaceClass = interfaceClass;
        this.registryConfig = registryConfig;
    }

    public BaseConfig() {
    }

    public Class getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }


}
