package net.gradle.springboot.jg.configs;

import cn.jiguang.common.ClientConfig;
import cn.jpush.api.JPushClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 极光推送配置项.
 * @author jiangnan
 */
@Configuration
@ConditionalOnProperty(value = "jg.push.enabled",havingValue = "true")
@EnableConfigurationProperties({JgAppConfigs.class,JPushAutoConfiguration.Configs.class})
public class JPushAutoConfiguration{
    private JgAppConfigs appConfigs;
    private Configs pushConfigs;

    public JPushAutoConfiguration(JgAppConfigs appConfigs,Configs configs){
        this.appConfigs = appConfigs;
        this.pushConfigs = configs;
    }

    @Bean
    public JPushClient jPushClient(){
        ClientConfig jPushConfig = ClientConfig.getInstance();
        //判断是否使用生产环境证书
        jPushConfig.setApnsProduction(pushConfigs.apnsProduction);
        if(pushConfigs.connectTimeout != null) jPushConfig.setConnectionTimeout(pushConfigs.connectTimeout);
        if(pushConfigs.ttl != null) jPushConfig.setTimeToLive(pushConfigs.ttl);
        if(pushConfigs.retryTimes != null) jPushConfig.setMaxRetryTimes(pushConfigs.retryTimes);
        if(pushConfigs.readTimeout != null) jPushConfig.setReadTimeout(pushConfigs.readTimeout);
        if(pushConfigs.socketTimeout != null) jPushConfig.setSocketTimeout(pushConfigs.socketTimeout);
        if(pushConfigs.requestTimeout != null) jPushConfig.setConnectionRequestTimeout(pushConfigs.requestTimeout);

        //更改设备上报地址
        if(StringUtils.isNotEmpty(pushConfigs.deviceHost)) jPushConfig.setDeviceHostName(pushConfigs.deviceHost);
        //更改数据报告服务地址
        if(StringUtils.isNotEmpty(pushConfigs.reportHost)) jPushConfig.setReportHostName(pushConfigs.reportHost);
        //更改推送服务地址
        if(StringUtils.isNotEmpty(pushConfigs.pushHost)) jPushConfig.setPushHostName(pushConfigs.pushHost);
        //更改定时服务地址
        if(StringUtils.isNotEmpty(pushConfigs.scheduleHost))jPushConfig.setScheduleHostName(pushConfigs.scheduleHost);
        return new JPushClient(appConfigs.secret, appConfigs.appkey,null, jPushConfig);
    }


    /**
     * 配置项
     */
    @ConfigurationProperties("jg.push")
    static class Configs {
        //连接超时时间，单位为秒
        private Integer connectTimeout;
        //socket超时时间，单位为秒
        private Integer socketTimeout;
        //读超时时间，单位为秒
        private Integer readTimeout;
        //请求超时时间，单位为秒
        private Integer requestTimeout;
        //设备服务地址
        private String deviceHost;
        //报告服务地址
        private String reportHost;
        //推送服务地址
        private String pushHost;
        //定时任务服务地址
        private String scheduleHost;

        //默认的消息有效时间
        public Long ttl;
        //最大重试次数
        public Integer retryTimes;
        public boolean apnsProduction = false;

        public void setApnsProduction(boolean apnsProduction) {
            this.apnsProduction = apnsProduction;
        }

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

        public void setDeviceHost(String deviceHost) {
            this.deviceHost = deviceHost;
        }

        public void setReportHost(String reportHost) {
            this.reportHost = reportHost;
        }

        public void setPushHost(String pushHost) {
            this.pushHost = pushHost;
        }

        public void setScheduleHost(String scheduleHost) {
            this.scheduleHost = scheduleHost;
        }

        public void setTtl(Long ttl) {
            this.ttl = ttl;
        }

        public void setRetryTimes(Integer retryTimes) {
            this.retryTimes = retryTimes;
        }
    }
}
