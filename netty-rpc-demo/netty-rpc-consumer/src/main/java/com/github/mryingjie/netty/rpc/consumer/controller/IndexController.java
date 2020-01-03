package com.github.mryingjie.netty.rpc.consumer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.mryingjie.netty.rpc.demo.service.InfoUser;
import com.github.mryingjie.netty.rpc.demo.service.InfoUserService;
import com.github.mryingjie.netty.rpc.starter.annotation.Reference;
import com.github.mryingjie.netty.rpc.starter.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by MACHENIKE on 2018-12-03.
 */
@Controller
public class IndexController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Reference
    private
    InfoUserService userService;

    @RequestMapping("index")
    @ResponseBody
    public String index(){
        return  new Date().toString();
    }

    @RequestMapping("insert")
    @ResponseBody
    public List<InfoUser> getUserList() throws InterruptedException {

        long start = System.currentTimeMillis();
        int thread_count = 1;
        CountDownLatch countDownLatch = new CountDownLatch(thread_count);
        for (int i=0;i<thread_count;i++){
            new Thread(() -> {
                InfoUser infoUser = new InfoUser(IdUtil.getId(),"Jeen","BeiJing");
                List<InfoUser> users = userService.insertInfoUser(infoUser);
                for (InfoUser user : users) {
                    String id = user.getId();
                    logger.info("返回用户id:{}", id);
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
        long end = System.currentTimeMillis();
        logger.info("线程数：{},执行时间:{}",thread_count,(end-start));
        return null;
    }

    @RequestMapping("getById")
    @ResponseBody
    public InfoUser getById(String id){
        logger.info("根据ID查询用户信息:{}",id);
        return userService.getInfoUserById(id);
    }

    @RequestMapping("getNameById")
    @ResponseBody
    public String getNameById(String id){
        logger.info("根据ID查询用户名称:{}",id);
        return userService.getNameById(id);
    }

    @RequestMapping("getAllUser")
    @ResponseBody
    public Map<String,InfoUser> getAllUser() throws InterruptedException {

        Map<String, InfoUser> allUser = userService.getAllUser();
        for (Map.Entry<String, InfoUser> entry : allUser.entrySet()) {
            logger.info(entry.getValue().getId() );
        }
        return allUser;
    }
}
