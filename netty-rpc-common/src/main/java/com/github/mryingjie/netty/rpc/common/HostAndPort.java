package com.github.mryingjie.netty.rpc.common;

import java.util.Objects;

/**
 * created by Yingjie Zheng at 2019-12-19 17:50
 */
public class HostAndPort {

    private String host;

    private int port;

    public HostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
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

    @Override
    public String toString() {
        return host + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HostAndPort)) return false;
        HostAndPort that = (HostAndPort) o;
        return getPort() == that.getPort() &&
                Objects.equals(getHost(), that.getHost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getPort());
    }
}
