package com.shenit.springboot.jg.configs;

import cn.jiguang.common.ClientConfig;
import cn.jsms.api.JSMSClient;
import cn.jsms.api.common.JSMSConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 极光SMS配置项.
 * @author jiangnan
 */
@Configuration
@ConditionalOnProperty(value = "jg.sms.enabled",havingValue = "true")
@EnableConfigurationProperties({JgAppConfigs.class,JSmsAutoConfiguration.Configs.class})
public class JSmsAutoConfiguration{
    private Configs smsConfigs;
    private JgAppConfigs appConfigs;
    private Environment env;

    public JSmsAutoConfiguration(JgAppConfigs appConfigs,Configs configs,Environment env) {
        this.appConfigs = appConfigs;
        this.smsConfigs = configs;
        this.env = env;
    }

    @Bean
    public JSMSClient jSmsClient(){
        JSMSConfig smsConfigs = JSMSConfig.getInstance();
        ClientConfig clientConfigs = smsConfigs.getClientConfig();
        if(this.smsConfigs.connectTimeout != null) clientConfigs.setConnectionTimeout(this.smsConfigs.connectTimeout);
        if(this.smsConfigs.ttl != null) clientConfigs.setTimeToLive(this.smsConfigs.ttl);
        if(this.smsConfigs.retryTimes != null) clientConfigs.setMaxRetryTimes(this.smsConfigs.retryTimes);
        if(this.smsConfigs.readTimeout != null) clientConfigs.setReadTimeout(this.smsConfigs.readTimeout);
        if(this.smsConfigs.socketTimeout != null) clientConfigs.setSocketTimeout(this.smsConfigs.socketTimeout);
        if(this.smsConfigs.requestTimeout != null) clientConfigs.setConnectionRequestTimeout(this.smsConfigs.requestTimeout);

        return new JSMSClient(this.appConfigs.secret, this.appConfigs.appkey,null, smsConfigs);
    }


    /**
     * 配置项
     */
    @ConfigurationProperties("jg.sms")
    static class Configs {
        //连接超时时间，单位为秒
        private Integer connectTimeout;
        //socket超时时间，单位为秒
        private Integer socketTimeout;
        //读超时时间，单位为秒
        private Integer readTimeout;
        //请求超时时间，单位为秒
        private Integer requestTimeout;

        //默认的消息有效时间
        private Long ttl;
        //最大重试次数
        private Integer retryTimes;

        public void setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public void setSocketTimeout(Integer socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        public void setReadTimeout(Integer readTimeout) {
            this.readTimeout = readTimeout;
        }

        public void setRequestTimeout(Integer requestTimeout) {
            this.requestTimeout = requestTimeout;
        }

        public void setTtl(Long ttl) {
            this.ttl = ttl;
        }

        public void setRetryTimes(Integer retryTimes) {
            this.retryTimes = retryTimes;
        }
    }
}
