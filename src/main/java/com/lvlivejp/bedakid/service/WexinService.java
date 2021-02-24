package com.lvlivejp.bedakid.service;

import com.lvlivejp.bedakid.utils.HttpClientUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WexinService {

    @Value("${openId:ofEqo6SIkJHtgKBLEXvbNTyNLVfY}")
    private String openId;

    @SneakyThrows
    @Async
    public void sendMsg(String msg){
        Map map = new HashMap();
        map.put("openId",openId);
        map.put("msg",msg);
        HttpClientUtils.doPost("http://lvincn.cn/weixin/sendMsg", null, map, null);
    }
}
