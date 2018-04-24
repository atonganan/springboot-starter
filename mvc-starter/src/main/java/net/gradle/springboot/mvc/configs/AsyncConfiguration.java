package net.gradle.springboot.mvc.configs;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步配置
 * Created by jiangnan on 18/07/2017.
 */
@Configuration
@EnableConfigurationProperties(AsyncConfiguration.Properties.class)
public class AsyncConfiguration implements AsyncConfigurer{
    private Properties props;

    public AsyncConfiguration(Properties props){
        this.props = props;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int core = Runtime.getRuntime().availableProcessors() * 4;
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(props.max > 0 ?  Math.max(core,props.max) : Math.max(core,20));
        if(props.capacity > 0 ) executor.setQueueCapacity(props.capacity);
        executor.setKeepAliveSeconds(props.keepAlive > 0 ? props.keepAlive : 60);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

    @ConfigurationProperties("async.pool")
    public static class Properties{
        public int max;
        public int capacity;
        public int keepAlive;
    }
}