package com.github.mryingjie.netty.rpc.remoting;

import com.github.mryingjie.netty.rpc.registry.NettyRPCRegistryCenter;
import com.github.mryingjie.netty.rpc.remoting.json.JSONDecoder;
import com.github.mryingjie.netty.rpc.remoting.json.JSONEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * created by Yingjie Zheng at 2019-12-24 14:25
 */
public class ProviderClient implements RemotingProviderClient{


    private static final Logger logger = LoggerFactory.getLogger(RemotingProviderClient.class);
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(4);

    private Map<String, Object> serviceMap = new HashMap<>();

    private NettyServerHandler handler;

    private NettyRPCRegistryCenter registry;


    @Override
    public void start(final int port, NettyRPCRegistryCenter registry){
        this.registry = registry;
        handler = new NettyServerHandler(serviceMap);

        new Thread(() -> {
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup,workerGroup).
                        channel(NioServerSocketChannel.class).
                        option(ChannelOption.SO_BACKLOG,1024).
                        childOption(ChannelOption.SO_KEEPALIVE,true).
                        childOption(ChannelOption.TCP_NODELAY,true).
                        childHandler(new ChannelInitializer<SocketChannel>() {
                            //创建NIOSocketChannel成功后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络IO事件
                            protected void initChannel(SocketChannel channel) throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new IdleStateHandler(0, 0, 60));
                                pipeline.addLast(new JSONEncoder());
                                pipeline.addLast(new JSONDecoder());
                                pipeline.addLast(handler);
                            }
                        });

                ChannelFuture cf = bootstrap.bind(port).sync();
                logger.info("RPC 服务器启动.监听端口:"+port);
                //等待服务端监听端口关闭
                cf.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }



    @Override
    public void exportService(Class serviceClass, Object service) {
        serviceMap.put(serviceClass.getName(),service);
        registry.registryProvider(serviceClass.getName());
    }




}
