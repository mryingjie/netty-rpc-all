package com.github.mryingjie.netty.rpc.registry;

/**
 * created by Yingjie Zheng at 2019-12-19 16:02
 */
public class RegistryConfig {

    private String registryClass;

    private String providerClass ;

    private String consumerClass ;

    private String loadBalanceClass;

    /**
     * 本地ip host
     */
    private String host;

    private int port;

    private String userName;

    private String password;

    /**
     * 注册中心的地址
     */
    private String address;


    public String getLoadBalanceClass() {
        return loadBalanceClass;
    }

    public void setLoadBalanceClass(String loadBalanceClass) {
        this.loadBalanceClass = loadBalanceClass;
    }

    public RegistryConfig() {
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegistryClass() {
        return registryClass;
    }

    public void setRegistryClass(String registryClass) {
        this.registryClass = registryClass;
    }
}
