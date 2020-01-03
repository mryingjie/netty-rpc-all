package com.github.mryingjie.netty.rpc.remoting;

import com.github.mryingjie.netty.rpc.common.HostAndPort;
import com.github.mryingjie.netty.rpc.registry.NettyRPCRegistryCenter;
import com.github.mryingjie.netty.rpc.remoting.json.JSONDecoder;
import com.github.mryingjie.netty.rpc.remoting.json.JSONEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.SynchronousQueue;

/**
 * created by Yingjie Zheng at 2019-12-24 15:15
 */
public class ConsumerClient implements RemotingConsumerClient{


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private EventLoopGroup group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
    private Bootstrap bootstrap = new Bootstrap();

    private NettyClientHandler clientHandler;

    private ConnectManage connectManage;

    private NettyRPCRegistryCenter nettyRPCRegistryService;

    @Override
    public void start(NettyRPCRegistryCenter nettyRPCRegistryService, LoadBalance loadBalance){
        this.nettyRPCRegistryService = nettyRPCRegistryService;
        connectManage = new ConnectManage(nettyRPCRegistryService,this,loadBalance);
        clientHandler = new NettyClientHandler(this,connectManage);
        bootstrap.group(group).
                channel(NioSocketChannel.class).
                option(ChannelOption.TCP_NODELAY, true).
                option(ChannelOption.SO_KEEPALIVE,true).
                handler(new ChannelInitializer<SocketChannel>() {
                    //创建NIOSocketChannel成功后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络IO事件
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 0, 30));
                        pipeline.addLast(new JSONEncoder());
                        pipeline.addLast(new JSONDecoder());
                        pipeline.addLast("handler",clientHandler);
                    }
                });

    }


    @PreDestroy
    public void destroy(){
        logger.info("RPC客户端退出,释放资源!");
        group.shutdownGracefully();
    }

    @Override
    public Response send(Request request) throws InterruptedException, ClassNotFoundException {

        Channel channel = connectManage.chooseChannel(request.getClassName());
        if (channel!=null && channel.isActive()) {
            SynchronousQueue<Object> queue = clientHandler.sendRequest(request,channel);
            Response result = (Response) queue.take();
            return result;
        }else{
            Response res = new Response();
            res.setCode(1);
            res.setError_msg("未正确连接到服务器.请检查相关配置信息!");
            return res;
        }
    }
    @Override
    public Channel doConnect(HostAndPort hostAndPort) throws InterruptedException {
        ChannelFuture future = bootstrap.connect(hostAndPort.getHost(),hostAndPort.getPort());
        Channel channel = future.sync().channel();
        return channel;
    }
}
