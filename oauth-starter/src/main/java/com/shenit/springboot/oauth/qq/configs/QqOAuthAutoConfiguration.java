package com.shenit.springboot.oauth.qq.configs;

import com.shenit.springboot.oauth.qq.QqOAuthClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

@Order(Ordered.LOWEST_PRECEDENCE) //低优先级加载
@ConditionalOnProperty("oauth.qq.appid")
@ConfigurationProperties("oauth.qq")
@Configuration
public class QqOAuthAutoConfiguration {
    private String appid;
    public void setAppid(String appid) {
        this.appid = appid;
    }
    private RestTemplate restTemplate;

    public QqOAuthAutoConfiguration(){}

    @Autowired
    public QqOAuthAutoConfiguration(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 生成QQ互联客户端.
     * @return
     */
    @Bean
    public QqOAuthClient qqOAuthClient(){
        QqOAuthClient client = new QqOAuthClient(appid);
        client.setRestTemplate(restTemplate);
        return client;
    }

}
