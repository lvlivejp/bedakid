package com.lvlivejp.bedakid.config;

import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat")
@Data
public class WeChatConfig {

    /**
     * 公众号AppId
     **/
    private String mpAppId;

    /**
     * 公众号密钥
     **/
    private String mpAppSecret;

    /**
     * 管理员openId
     **/
    private String openId;

    /**
     * token
     **/
    private String token;

    @Bean
    public WxMpService wxMpService(){
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage());
        return wxMpService;
    }

    public WxMpConfigStorage wxMpConfigStorage(){
        WxMpInMemoryConfigStorage wxMpConfigStorage = new WxMpInMemoryConfigStorage();
        wxMpConfigStorage.setAppId(mpAppId);
        wxMpConfigStorage.setSecret(mpAppSecret);
        wxMpConfigStorage.setToken(token);
        return wxMpConfigStorage;
    }
}
