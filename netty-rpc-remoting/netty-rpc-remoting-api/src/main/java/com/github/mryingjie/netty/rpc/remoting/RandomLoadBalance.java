package com.github.mryingjie.netty.rpc.remoting;

import io.netty.channel.Channel;

import java.util.Random;
import java.util.Set;

/**
 * created by Yingjie Zheng at 2019-12-26 15:51
 */
public class RandomLoadBalance implements LoadBalance{
    @Override
    public Channel loadBalance(Set<Channel> channels) {
        int index = new Random().nextInt(channels.size());
        Channel channel = null;
        int i = 0;
        for (Channel c : channels) {
            if(i == index){
                channel = c;
                break;
            }
            i++;
        }
        return channel;
    }
}
