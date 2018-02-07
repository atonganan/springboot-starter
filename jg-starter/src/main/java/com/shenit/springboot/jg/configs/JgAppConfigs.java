package com.shenit.springboot.jg.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 极光APP配置
 */
@ConfigurationProperties("jg")
public class JgAppConfigs {
    String appkey;
    String secret;

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }


}
