package com.github.mryingjie.netty.rpc.starter.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.mryingjie.netty.rpc.config.RPCBootStrap;
import com.github.mryingjie.netty.rpc.config.ReferenceConfig;
import com.github.mryingjie.netty.rpc.config.ServiceConfig;
import com.github.mryingjie.netty.rpc.registry.NettyRPCRegistryCenter;
import com.github.mryingjie.netty.rpc.registry.RegistryConfig;
import com.github.mryingjie.netty.rpc.remoting.RemotingConsumerClient;
import com.github.mryingjie.netty.rpc.remoting.Request;
import com.github.mryingjie.netty.rpc.remoting.Response;
import com.github.mryingjie.netty.rpc.starter.annotation.Reference;
import com.github.mryingjie.netty.rpc.starter.annotation.RpcService;
import com.github.mryingjie.netty.rpc.starter.util.IdUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.lang.reflect.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * created by Yingjie Zheng at 2019-12-23 14:21
 */

@EnableConfigurationProperties(NettyRPCProperties.class)
@Configuration
public class NettyRPCAutoConfiguration {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private NettyRPCProperties properties;

    private RegistryConfig registryConfig;

    /**
     * 暴露服务
     *
     * @throws Exception
     */
    @PostConstruct
    public void init() throws Exception {
        Map<String, Object> beanMap = this.applicationContext.getBeansWithAnnotation(RpcService.class);
        if (beanMap.size() > 0) {
            for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
                initProviderBean(entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * 初始化被Reference注解修饰的字段
     */
    @Bean
    @ConditionalOnClass(Reference.class)
    @ConditionalOnProperty(name = "netty.rpc.enableConsumer", havingValue = "true")
    public BeanPostProcessor beanPostProcessor() {
        initRegistryConfig();

        return new BeanPostProcessor() {


            RPCBootStrap rpcBootStrap = RPCBootStrap.getInstance();

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                Class<?> objClz;
                if (AopUtils.isAopProxy(bean)) {
                    objClz = AopUtils.getTargetClass(bean);
                } else {
                    objClz = bean.getClass();
                }
                try {
                    rpcBootStrap.initConsumer(registryConfig);
                    final RemotingConsumerClient consumerClient = rpcBootStrap.getConsumerClient();

                    for (Field field : objClz.getDeclaredFields()) {
                        Reference reference = field.getAnnotation(Reference.class);

                        if (reference != null) {
                            Class<?> type = field.getType();
                            if (!type.isInterface()) {
                                throw new RuntimeException("Fields modified by @Reference must be interfaces ");
                            }
                            Object object = Proxy.newProxyInstance(
                                    type.getClassLoader(),
                                    new Class[]{type},
                                    new InvocationHandler() {


                                        @Override
                                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                            Request request = new Request();
                                            request.setClassName(method.getDeclaringClass().getName());
                                            request.setMethodName(method.getName());
                                            request.setParameters(args);
                                            request.setParameterTypes(method.getParameterTypes());
                                            request.setId(IdUtil.getId());

                                            Response response = consumerClient.send(request);
                                            Class<?> returnType = method.getReturnType();

                                            if (response.getCode() == 1) {
                                                throw new Exception(response.getError_msg());
                                            }
                                            if (returnType.isPrimitive() || String.class.isAssignableFrom(returnType)) {
                                                return response.getData();
                                            } else if (Collection.class.isAssignableFrom(returnType)) {
                                                Type genericReturnType = method.getGenericReturnType();
                                                //获取返回值的泛型参
                                                Type[] actualTypeArguments = null;
                                                Class genericClass = Object.class;
                                                if (genericReturnType instanceof ParameterizedType) {
                                                    actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                                                }
                                                if(actualTypeArguments != null && actualTypeArguments.length>0){
                                                    genericClass = (Class) actualTypeArguments[0];
                                                }

                                                return JSONArray.parseArray(response.getData().toString(), genericClass);
                                            } else if (Map.class.isAssignableFrom(returnType)) {

                                                Type genericReturnType = method.getGenericReturnType();
                                                //获取返回值的泛型参
                                                Type[] actualTypeArguments = null;
                                                Class keyClass = Object.class;
                                                Class valueClass = Object.class;
                                                if (genericReturnType instanceof ParameterizedType) {
                                                    actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                                                }
                                                if(actualTypeArguments != null && actualTypeArguments.length>0){
                                                    keyClass = (Class) actualTypeArguments[0];
                                                    valueClass = (Class) actualTypeArguments[1];
                                                }
                                                Map<String,String> map = JSON.parseObject(response.getData().toString(), new TypeReference<Map<String,String>>(){});
                                                Map<Object, Object> resultMap = new HashMap<>(map.size());
                                                for (Map.Entry<String, String> entry : map.entrySet()) {
                                                    String key = entry.getKey();
                                                    String value = entry.getValue();
                                                    resultMap.put(JSON.parseObject(key,keyClass),JSON.parseObject(value,valueClass));
                                                }

                                                return resultMap;
                                            } else {
                                                Object data = response.getData();
                                                return JSONObject.parseObject(data.toString(), returnType);
                                            }
                                        }
                                    });
                            rpcBootStrap.getNettyRPCRegistryService().registryConsumer(type.getName());
                            field.setAccessible(true);
                            field.set(bean, object);
                        }

                    }
                } catch (Exception e) {
                    throw new BeanCreationException(beanName, e);
                }
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }
        };
    }


    /**
     * @param beanName
     * @param bean
     * @throws Exception
     */
    private void initProviderBean(String beanName, Object bean) throws Exception {
        // RpcService service = this.applicationContext.findAnnotationOnBean(beanName, RpcService.class);
        initRegistryConfig();
        Class<?>[] interfaces = bean.getClass().getInterfaces();
        Class<?> anInterface = interfaces[0];
        ServiceConfig serviceConfig = new ServiceConfig(anInterface, registryConfig);
        serviceConfig.setRef(bean);
        serviceConfig.export();
    }

    private void initRegistryConfig() {
        if (registryConfig == null) {
            synchronized (this) {
                if (registryConfig == null) {
                    registryConfig = new RegistryConfig();
                    try {
                        InetAddress localHost = InetAddress.getLocalHost();
                        String hostAddress = localHost.getHostAddress();

                        registryConfig.setHost(hostAddress);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException("unknow host of locolhost");
                    }
                    registryConfig.setConsumerClass(properties.getConsumerClass());
                    registryConfig.setProviderClass(properties.getProviderClass());
                    registryConfig.setPort(properties.getServerPort());
                    registryConfig.setAddress(properties.getRegistryAddress());
                    registryConfig.setRegistryClass(properties.getRegistryClass());
                    registryConfig.setLoadBalanceClass(properties.getLoadBalanceClass());
                }
            }
        }
    }


}
