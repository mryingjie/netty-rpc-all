package com.github.mryingjie.netty.rpc.remoting;

import com.alibaba.fastjson.JSON;
import com.github.mryingjie.netty.rpc.common.HostAndPort;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by MACHENIKE on 2018-12-03.
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private RemotingConsumerClient client;

    private ConnectManage connectManage;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ConcurrentHashMap<String,SynchronousQueue<Object>> queueMap = new ConcurrentHashMap<>();

    public NettyClientHandler(RemotingConsumerClient consumerClient,ConnectManage manage){
        this.client = consumerClient;
        this.connectManage = manage;
    }

    public void channelActive(ChannelHandlerContext ctx)   {
        logger.info("已连接到RPC服务器.{}",ctx.channel().remoteAddress());
    }

    public void channelInactive(ChannelHandlerContext ctx)   {
        InetSocketAddress address =(InetSocketAddress) ctx.channel().remoteAddress();
        logger.info("与RPC服务器断开连接."+address);
        ctx.channel().close();

        connectManage.removeChannel(new HostAndPort(address.getHostName(), address.getPort()),ctx.channel());
    }
    public void channelRead(ChannelHandlerContext ctx, Object msg)throws Exception {
        Response response = JSON.parseObject(msg.toString(),Response.class);
        String requestId = response.getRequestId();
        SynchronousQueue<Object> queue = queueMap.get(requestId);
        queue.put(response);
        queueMap.remove(requestId);
    }

    public SynchronousQueue<Object> sendRequest(Request request,Channel channel) {
        SynchronousQueue<Object> queue = new SynchronousQueue<>();
        queueMap.put(request.getId(), queue);
        channel.writeAndFlush(request);
        return queue;
    }


    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)throws Exception {
        logger.info("已超过30秒未与RPC服务器进行读写操作!将发送心跳消息...");
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state()== IdleState.ALL_IDLE){
                Request request = new Request();
                request.setMethodName("heartBeat");
                ctx.channel().writeAndFlush(request);
            }
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        logger.info("RPC通信服务器发生异常.{}",cause.getMessage(),cause);
        ctx.channel().close();
    }
}
