package com.github.mryingjie.netty.rpc.provider.serviceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.mryingjie.netty.rpc.demo.service.InfoUser;
import com.github.mryingjie.netty.rpc.demo.service.InfoUserService;
import com.github.mryingjie.netty.rpc.starter.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * created by Yingjie Zheng at 2020-01-02 14:40
 */
@RpcService
public class InfoUserServiceImpl implements InfoUserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    Map<String,InfoUser> infoUserMap = new ConcurrentHashMap<>();

    @Override
    public List<InfoUser> insertInfoUser(InfoUser infoUser) {
        logger.info("新增用户信息:{}", JSONObject.toJSONString(infoUser));
        infoUserMap.put(infoUser.getId(),infoUser);
        return getInfoUserList();
    }

    @Override
    public InfoUser getInfoUserById(String id) {
        InfoUser infoUser = infoUserMap.get(id);
        logger.info("查询用户ID:{}",id);
        return infoUser;
    }

    public List<InfoUser> getInfoUserList() {
        List<InfoUser> userList = new ArrayList<>();
        Iterator<Map.Entry<String, InfoUser>> iterator = infoUserMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, InfoUser> next = iterator.next();
            userList.add(next.getValue());
        }
        logger.info("返回用户信息记录:{}", JSON.toJSONString(userList));
        return userList;
    }

    @Override
    public void deleteInfoUserById(String id) {
        logger.info("删除用户信息:{}",JSONObject.toJSONString(infoUserMap.remove(id)));
    }

    @Override
    public String getNameById(String id){
        logger.info("根据ID查询用户名称:{}",id);
        return infoUserMap.get(id).getName();
    }
    @Override
    public Map<String,InfoUser> getAllUser(){
        logger.info("查询所有用户信息{}",JSONObject.toJSONString(infoUserMap));
        return infoUserMap;
    }
}
