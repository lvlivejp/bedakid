package com.lvlivejp.bedakid.service;

import com.lvlivejp.bedakid.config.WeChatConfig;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class WxMessageService {

    @Autowired
    WxMpService wxMpService;

    @Autowired
    WeChatConfig weChatConfig;

    public void uploadMessage(String msgId,String content){
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(weChatConfig.getOpenId())
                .templateId(msgId)
                .build();

        List<WxMpTemplateData> data = Arrays.asList(
                new WxMpTemplateData("content", content));
        templateMessage.setData(data);
        try {
            wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
        } catch (WxErrorException e) {
            log.error("微信消息发送失败：",e);
        }
    }
}
