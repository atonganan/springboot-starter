package com.shenit.springboot.oauth.wechat.configs;

import com.shenit.springboot.wechat.open.WxOpenClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 微信开放平台配置.
 */
@ConditionalOnClass(WxOpenClient.class)
@Configuration
public class WxOpenAutoConfiguration {
    private RestTemplate restTemplate;

    @Autowired
    public WxOpenAutoConfiguration(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 生成QQ互联客户端.
     * @return
     */
    @Bean
    public WxOpenClient wxOpenClient(){
        WxOpenClient client = new WxOpenClient(restTemplate);
        return client;
    }

}
