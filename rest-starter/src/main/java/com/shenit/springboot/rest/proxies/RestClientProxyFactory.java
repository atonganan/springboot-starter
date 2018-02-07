package com.shenit.springboot.rest.proxies;

/**
 * 创建RestClient的代理接口.
 */
public interface RestClientProxyFactory {
    /**
     * Creates the proxy class out of specific interface. The proxy is being created for the service interface.
     *
     * @param serviceClass the service class
     * @param <T> the service type
     * @param serviceUrl The service url that all methods will call based on
     * @return the created proxy
     */
    <T> T createClientProxy(Class<T> serviceClass, String serviceUrl);
}
