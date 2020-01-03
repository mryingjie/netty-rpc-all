package com.github.mryingjie.netty.rpc.remoting;

import io.netty.channel.Channel;

import java.util.Set;

/**
 * created by Yingjie Zheng at 2019-12-26 15:50
 */
public interface LoadBalance {

    Channel loadBalance(Set<Channel> channels);

}
