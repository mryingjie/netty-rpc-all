package com.github.mryingjie.netty.rpc.registry;

import com.github.mryingjie.netty.rpc.common.HostAndPort;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * created by Yingjie Zheng at 2019-12-20 10:49
 */
@Slf4j
@SuppressWarnings("all")
public class ZkRegistryService implements NettyRPCRegistryCenter {

    private static final int SESSION_TIMEOUT = 10000000;

    private static final int CONNECTION_TIMEOUT = 10000000;

    private static final String ROOT_NODE = "/netty_rpc";

    private static final String PROVIDER_NODE = "/provider";

    private static final String CONSUMER_NODE = "/consumer";

    private Map<String, List<HostAndPort>> providerMap = new ConcurrentHashMap<>();

    private Map<String, List<HostAndPort>> consumerMap = new ConcurrentHashMap<>();


    private Set<String> localProvider = new CopyOnWriteArraySet<>();

    private Set<String> localConsumer = new CopyOnWriteArraySet<>();

    private Set<String> providers = new CopyOnWriteArraySet<>();

    private Set<String> consumers = new CopyOnWriteArraySet<>();

    private ZkClient client;

    private RegistryConfig registryConfig;

    private volatile boolean init;

    @Override
    public void init(RegistryConfig registryConfig) {
        if(!init){
            this.registryConfig = registryConfig;
            client = new ZkClient(registryConfig.getAddress(), SESSION_TIMEOUT, CONNECTION_TIMEOUT, new SerializableSerializer());
            addClientSessionListener(registryConfig);
            List<String> services = addRootNodeListener();
            //监听每个服务的节点
            addServerNodeListener(services);
            init = true;
            log.info("connect zookeeper ok! address:[{}]", registryConfig.getAddress());
        }
    }

    private void createZkNode(String path, Object data, CreateMode createMode) {
        try {
            client.create(path, data, createMode);
        } catch (ZkNodeExistsException e) {

        }
    }

    private void addServerNodeListener(List<String> services) {
        for (String service : services) {
            if (!client.exists(ROOT_NODE + "/" + service + PROVIDER_NODE)) {
                createZkNode(ROOT_NODE + "/" + service + PROVIDER_NODE, null, CreateMode.EPHEMERAL);
            }
            if (!client.exists(ROOT_NODE + "/" + service + CONSUMER_NODE)) {
                createZkNode(ROOT_NODE + "/" + service + CONSUMER_NODE, null, CreateMode.EPHEMERAL);
            }

            client.subscribeChildChanges(ROOT_NODE + "/" + service + PROVIDER_NODE, new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> addresses) throws Exception {
                    try {
                        updateProvider(service, addresses);
                    } catch (Exception e) {
                    }

                }
            });
            client.subscribeChildChanges(ROOT_NODE + "/" + service + CONSUMER_NODE, new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> addresses) throws Exception {
                    try {
                        updateConsumer(service, addresses);
                    } catch (Exception e) {
                    }

                }
            });

            try {
                updateService(service);
            } catch (Exception e) {
            }
        }
    }

    private List<String> addRootNodeListener() {
        if (!client.exists(ROOT_NODE)) {
            createZkNode(ROOT_NODE, null, CreateMode.PERSISTENT);
        }
        //监听 ROOT路径加载所有服务
        return client.subscribeChildChanges(ROOT_NODE, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> services) throws Exception {
                if (services == null || services.size() == 0) {
                    return;
                }
                for (String service : services) {
                    if (!providers.contains(service)) {
                        if (!client.exists(ROOT_NODE + "/" + service + PROVIDER_NODE)) {
                            createZkNode(ROOT_NODE + "/" + service + PROVIDER_NODE, null, CreateMode.PERSISTENT);
                        }
                        client.subscribeChildChanges(ROOT_NODE + "/" + service + PROVIDER_NODE, new IZkChildListener() {
                            @Override
                            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                                try {
                                    updateProvider(service);
                                } catch (Exception e) {
                                }
                            }
                        });

                        try {
                            updateProvider(service);
                        } catch (Exception e) {
                        }
                    }
                    if (!consumers.contains(service)) {
                        if (!client.exists(ROOT_NODE + "/" + service + CONSUMER_NODE)) {
                            createZkNode(ROOT_NODE + "/" + service + CONSUMER_NODE, null, CreateMode.PERSISTENT);
                        }
                        client.subscribeChildChanges(ROOT_NODE + "/" + service + CONSUMER_NODE, new IZkChildListener() {
                            @Override
                            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                                try {
                                    updateConsumer(service);
                                } catch (Exception e) {
                                }
                            }
                        });
                        try {
                            updateConsumer(service);
                        } catch (Exception e) {
                        }
                    }
                }
                for (String provider : providers) {
                    if (!services.contains(provider)) {
                        providerMap.remove(provider);
                        providers.remove(provider);
                    }
                }
                for (String consumer : consumers) {
                    if (!services.contains(consumer)) {
                        consumerMap.remove(consumer);
                        consumers.remove(consumer);
                    }
                }
            }
        });
    }

    private void addClientSessionListener(RegistryConfig registryConfig) {
        client.subscribeStateChanges(new IZkStateListener() {
            @Override
            public void handleNewSession() throws Exception {
                //重建session
                System.out.println("handleNewSession()");
            }

            @Override
            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                if (state == Watcher.Event.KeeperState.SyncConnected) {
                    //当重新启动后start，监听触发 将本地服务重新注册到zk上
                    for (String service : localProvider) {
                        ZkRegistryService.this.addProviderAddress(service, new HostAndPort(registryConfig.getHost(), registryConfig.getPort()));
                    }
                    for (String service : localConsumer) {
                        ZkRegistryService.this.addAddress(service, new HostAndPort(registryConfig.getHost(), 0), CONSUMER_NODE);
                    }
                    log.info("reconnect zookeeper ok! address:[{}]", registryConfig.getAddress());
                } else if (state == Watcher.Event.KeeperState.Disconnected) {
                    //当zk服务stop时，监听触发

                }
            }
        });
    }

    private void updateService(String service) throws Exception {
        updateProvider(service);
        updateConsumer(service);
    }

    private void updateConsumer(String service) throws Exception {
        List<String> consumers = client.getChildren(ROOT_NODE + "/" + service + CONSUMER_NODE);
        if (consumers == null || consumers.size() == 0) {
            consumerMap.remove(service);
            this.consumers.remove(service);
            // client.delete(ROOT_NODE + "/" + service + CONSUMER_NODE);
            // if (client.exists(ROOT_NODE + "/" + service + PROVIDER_NODE)) {
            //     List<String> consumerAddress = client.getChildren(ROOT_NODE + "/" + service + PROVIDER_NODE);
            //     if (consumerAddress == null || consumerAddress.size() == 0) {
            //         client.delete(ROOT_NODE + "/" + service + PROVIDER_NODE);
            //         client.delete(ROOT_NODE + "/" + service);
            //         this.consumerMap.remove(service);
            //         this.consumers.remove(service);
            //     }
            // }
            return;
        }
        consumerMap.put(
                service,
                consumers.stream().map(s -> {
                    String[] split = s.split(":");
                    return new HostAndPort(split[0], Integer.parseInt(split[1]));
                }).collect(Collectors.toList())
        );
        this.consumers.add(service);
    }

    private void updateConsumer(String service, List<String> address) throws Exception {
        if (address == null || address.size() == 0) {
            consumerMap.remove(service);
            this.consumers.remove(service);
            // client.delete(ROOT_NODE + "/" + service + CONSUMER_NODE);
            // if (client.exists(ROOT_NODE + "/" + service + PROVIDER_NODE)) {
            //     List<String> consumerAddress = client.getChildren(ROOT_NODE + "/" + service + PROVIDER_NODE);
            //     if (consumerAddress == null || consumerAddress.size() == 0) {
            //         client.delete(ROOT_NODE + "/" + service + PROVIDER_NODE);
            //         client.delete(ROOT_NODE + "/" + service);
            //         this.consumerMap.remove(service);
            //         this.consumers.remove(service);
            //     }
            // }


            return;
        }
        consumerMap.put(
                service,
                address.stream().map(s -> {
                    String[] split = s.split(":");
                    return new HostAndPort(split[0], Integer.parseInt(split[1]));
                }).collect(Collectors.toList())
        );
        this.consumers.add(service);
    }

    private void updateProvider(String service) throws Exception {
        List<String> providers = client.getChildren(ROOT_NODE + "/" + service + PROVIDER_NODE);
        if (providers == null || providers.size() == 0) {
            providerMap.remove(service);
            this.providers.remove(service);
            // client.delete(ROOT_NODE + "/" + service + PROVIDER_NODE);
            // if (client.exists(ROOT_NODE + "/" + service + CONSUMER_NODE)) {
            //     List<String> consumerAddress = client.getChildren(ROOT_NODE + "/" + service + CONSUMER_NODE);
            //     if (consumerAddress == null || consumerAddress.size() == 0) {
            //         client.delete(ROOT_NODE + "/" + service + CONSUMER_NODE);
            //         client.delete(ROOT_NODE + "/" + service);
            //         this.consumers.remove(service);
            //         this.consumerMap.remove(service);
            //     }
            // }


            return;
        }
        providerMap.put(
                service,
                providers.stream().map(s -> {
                    String[] split = s.split(":");
                    return new HostAndPort(split[0], Integer.parseInt(split[1]));
                }).collect(Collectors.toList())
        );
        this.providers.add(service);
    }

    private void updateProvider(String service, List<String> address) throws Exception {
        if (address == null || address.size() == 0) {
            providerMap.remove(service);
            this.providers.remove(service);
            // client.delete(ROOT_NODE + "/" + service + PROVIDER_NODE);
            // if (client.exists(ROOT_NODE + "/" + service + CONSUMER_NODE)) {
            //     List<String> consumerAddress = client.getChildren(ROOT_NODE + "/" + service + CONSUMER_NODE);
            //     if (consumerAddress == null || consumerAddress.size() == 0) {
            //         client.delete(ROOT_NODE + "/" + service + CONSUMER_NODE);
            //         client.delete(ROOT_NODE + "/" + service);
            //         this.consumers.remove(service);
            //         this.consumerMap.remove(service);
            //     }
            // }
            return;
        }
        providerMap.put(
                service,
                address.stream().map(s -> {
                    String[] split = s.split(":");
                    return new HostAndPort(split[0], Integer.parseInt(split[1]));
                }).collect(Collectors.toList())
        );
        this.providers.add(service);
    }


    @Override
    public List<HostAndPort> getProviderAddress(String service) {
        List<HostAndPort> hostAndPorts = providerMap.get(service);
        if (hostAndPorts == null || hostAndPorts.size() == 0) {
            synchronized (this) {
                hostAndPorts = providerMap.get(service);
                if (hostAndPorts == null || hostAndPorts.size() == 0) {
                    List<String> children = client.getChildren(ROOT_NODE + "/" + service + PROVIDER_NODE);
                    List<HostAndPort> serviceList = children.stream().map(s -> {
                        String[] split = s.split(":");
                        return new HostAndPort(split[0], Integer.parseInt(split[1]));
                    }).collect(Collectors.toList());
                    providerMap.put(service, serviceList);
                }
            }

        }
        return providerMap.get(service);
    }


    @Override
    public final void registryProvider(String service) {

        addProviderAddress(service, new HostAndPort(registryConfig.getHost(), registryConfig.getPort()));
    }

    private void addProviderAddress(String service, HostAndPort hostAndPort) {
        addAddress(service, hostAndPort, PROVIDER_NODE);
    }

    @Override
    public boolean deleteProviderAddress(String services, HostAndPort hostAndPort) {
        localProvider.remove(services);
        String path = ROOT_NODE + "/" + services + PROVIDER_NODE + "/" + hostAndPort.toString();
        return client.delete(path);
    }


    @Override
    public List<HostAndPort> getConsumerAddress(String service) {
        return consumerMap.get(service);
    }

    @Override
    public void registryConsumer(String service) {
        if (!localConsumer.contains(service)) {
            addAddress(service, new HostAndPort(registryConfig.getHost(), 0), CONSUMER_NODE);
        }
    }

    private void addAddress(String service, HostAndPort hostAndPort, String nodeType) {

        String parent = ROOT_NODE + "/" + service;
        if (!client.exists(parent)) {
            Map<String, Object> map = new HashMap<>();
            try {
                map.put(
                        "methods",
                        Arrays.stream(Class.forName(service).getMethods())
                                .map(Method::getName)
                                .collect(Collectors.toList())
                );
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            createZkNode(parent, map, CreateMode.PERSISTENT);
        }


        if (!client.exists(parent + nodeType)) {
            createZkNode(parent + nodeType, null, CreateMode.PERSISTENT);
        }

        String childPath = parent + nodeType + "/" + hostAndPort.toString();
        if (!client.exists(childPath)) {
            createZkNode(childPath, null, CreateMode.EPHEMERAL);
        }
        if (CONSUMER_NODE.equals(nodeType)) {
            localConsumer.add(service);
        } else {
            localProvider.add(service);
        }

    }

    @Override
    public void deleteConsumerAddress(String services, HostAndPort hostAndPort) {
        client.delete(ROOT_NODE + "/" + services + CONSUMER_NODE + "/" + hostAndPort.toString());
    }

}
