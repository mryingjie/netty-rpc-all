package com.github.mryingjie.netty.rpc.demo.service;

import java.util.List;
import java.util.Map;

/**
 * created by Yingjie Zheng at 2020-01-02 14:26
 */
public interface InfoUserService {

    List<InfoUser> insertInfoUser(InfoUser infoUser);

    InfoUser getInfoUserById(String id);

    void deleteInfoUserById(String id);

    String getNameById(String id);

    Map<String,InfoUser> getAllUser();


}
