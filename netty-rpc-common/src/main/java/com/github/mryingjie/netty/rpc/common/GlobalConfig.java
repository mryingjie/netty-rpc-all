package com.github.mryingjie.netty.rpc.common;

/**
 * created by Yingjie Zheng at 2019-12-19 14:52
 */
public class GlobalConfig {


    // by default, host to registry
    private String host;

    // by default, port to registry
    private int port;

    private boolean enableConsumer;

    private boolean enableProvider;

    public boolean isEnableConsumer() {
        return enableConsumer;
    }

    public void setEnableConsumer(boolean enableConsumer) {
        this.enableConsumer = enableConsumer;
    }

    public boolean isEnableProvider() {
        return enableProvider;
    }

    public void setEnableProvider(boolean enableProvider) {
        this.enableProvider = enableProvider;
    }

    public GlobalConfig(String host, int port, boolean enableConsumer, boolean enableProvider) {
        this.host = host;
        this.port = port;
        this.enableConsumer = enableConsumer;
        this.enableProvider = enableProvider;
    }



    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
