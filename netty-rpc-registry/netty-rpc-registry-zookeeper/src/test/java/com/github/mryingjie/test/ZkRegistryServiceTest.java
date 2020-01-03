package com.github.mryingjie.test;

import com.github.mryingjie.netty.rpc.registry.NettyRPCRegistryCenter;
import com.github.mryingjie.netty.rpc.registry.RegistryConfig;
import com.github.mryingjie.netty.rpc.registry.ZkRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.imageio.spi.RegisterableService;
import javax.xml.soap.Node;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * created by Yingjie Zheng at 2019-12-20 11:22
 */
@Slf4j
public class ZkRegistryServiceTest {



    @Test
    public void test(){
        log.info("start");
        ZkRegistryService zkRegistryService = new ZkRegistryService();
        RegistryConfig registryConfig = new RegistryConfig();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String hostAddress = localHost.getHostAddress();

            registryConfig.setHost(hostAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        registryConfig.setPort(10081);
        registryConfig.setAddress("localhost:2181");
        zkRegistryService.init(registryConfig);

        zkRegistryService.registryProvider(NettyRPCRegistryCenter.class.getName());
        zkRegistryService.registryProvider(ZkRegistryServiceTest.class.getName());

        while (true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    @Test
    public void test2(){
        log.info("start");
        ZkRegistryService zkRegistryService = new ZkRegistryService();
        RegistryConfig registryConfig = new RegistryConfig();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String hostAddress = localHost.getHostAddress();

            registryConfig.setHost(hostAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        registryConfig.setPort(10080);
        registryConfig.setAddress("localhost:2181");
        zkRegistryService.init(registryConfig);

        zkRegistryService.registryProvider(NettyRPCRegistryCenter.class.getName());
        zkRegistryService.registryProvider(ZkRegistryServiceTest.class.getName());


        while (true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    @Test
    public void test3(){
        log.info("start");
        ZkRegistryService zkRegistryService = new ZkRegistryService();
        RegistryConfig registryConfig = new RegistryConfig();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String hostAddress = localHost.getHostAddress();

            registryConfig.setHost(hostAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        registryConfig.setPort(10082);
        registryConfig.setAddress("localhost:2181");
        zkRegistryService.init(registryConfig);

        zkRegistryService.registryProvider(RegisterableService.class.getName());
        zkRegistryService.registryProvider(Node.class.getName());

        while (true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }
}
