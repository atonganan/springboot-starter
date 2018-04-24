package net.gradle.springboot.oauth.wechat.configs;

import net.gradle.springboot.wechat.app.WxAppClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 微信小程序的配置项.
 * 当应用配置了wechat.app.appid的时候启用这个配置.
 */
@ConditionalOnClass(WxAppClient.class)
@ConfigurationProperties("wechat.app")
@Configuration
public class WxAppAutoConfiguration {
    private String appid;
    private String appkey;

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    @Bean
    public WxAppClient wxAppClient(RestTemplate rest){
        WxAppClient appClient = new WxAppClient(rest,appid,appkey);
        return appClient;
    }
}
