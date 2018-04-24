package net.gradle.springboot.rest.proxies;

import com.google.common.collect.Maps;
import net.gradle.springboot.rest.annotations.Retry;
import net.gradle.springboot.rest.invokers.BasicRestInvoker;
import net.gradle.springboot.rest.invokers.RestInvoker;
import net.gradle.springboot.rest.invokers.RetryRestInvoker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.Path;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Resteasy的客户端代理实现类.
 */
@Component
public class ResteasyClientProxyFactory implements RestClientProxyFactory {
    private RestTemplate restTemplate;
    private RetryTemplate retryTemplate;
    private ApplicationContext applicationContext;

    public ResteasyClientProxyFactory(
            RestTemplate restTemplate,
            @Qualifier("simpleFixed") RetryTemplate retryTemplate,
            ApplicationContext applicationContext) {
        this.restTemplate = restTemplate;
        this.retryTemplate = retryTemplate;
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T createClientProxy(Class<T> serviceClass,String serviceUrl) {
        RestClientProxy proxy = new RestClientProxy(serviceClass,serviceUrl);
        proxy.restTemplate = restTemplate;
        proxy.applicationContext = applicationContext;
        proxy.loadMethodConfigs();

        return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(),
                new Class<?>[]{serviceClass},
                proxy);
    }


    /**
     * Rest客户端代理类.
     */
    private class RestClientProxy implements InvocationHandler {
        private static final String TO_STRING = "toString";
        private static final String EQUALS = "equals";
        private static final String HASH_CODE = "hashCode";
        private Class<?> clazz;
        private String baseUrl;
        private MultiValueMap<String,String[]> headers = new LinkedMultiValueMap<>();
        private Map<Method,RestInvoker> methods = Maps.newHashMap();
        private RestTemplate restTemplate;
        private ApplicationContext applicationContext;

        public <T> RestClientProxy(Class<T> serviceClass,String serviceUrl) {
            this.clazz = serviceClass;
            this.baseUrl = serviceUrl;
        }

        /**
         * 解释每个方法，加载其请求相关配置.
         */
        private void loadMethodConfigs() {
            for(Method method : clazz.getMethods()){
                if(method.getAnnotation(Path.class) == null) continue;  //skip this method
                Retry retry = method.getAnnotation(Retry.class);
                if(retry != null) {
                    RetryRestInvoker invoker = new RetryRestInvoker(method, baseUrl);
                    invoker.setRestTemplate(restTemplate);
                    invoker.setRetryTemplate(applicationContext.getBean(retry.value(),RetryTemplate.class));
                    methods.put(method, invoker);
                }else{
                    BasicRestInvoker invoker = new BasicRestInvoker(method, baseUrl);
                    invoker.setRestTemplate(restTemplate);
                    methods.put(method, invoker);
                }
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            RestInvoker invoker = methods.get(method);
            if (invoker == null) {
                switch (method.getName()) {
                    case TO_STRING:
                        return this.toString();
                    case HASH_CODE:
                        return this.hashCode();
                    case EQUALS:
                        return proxy == args[0];
                }
            }

            if(invoker == null) throw new NoSuchMethodException("Method "+method.getName()+" not allowed");
            return methods.get(method).invoke(args);
        }

    }

}
