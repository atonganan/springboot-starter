package net.gradle.springboot.rest.proxies;

import net.gradle.commons.utils.ShenStrings;
import net.gradle.springboot.rest.annotations.RestClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.ws.rs.Path;

public class RestClientProxyFactoryBean implements ApplicationContextAware, FactoryBean {

    /**
     * The application context.
     */
    private ApplicationContext applicationContext;

    /**
     * The target service class.
     */
    private Class<?> serviceClass;



    /**
     * Sets the service class.
     *
     * @param serviceClass the service class
     */
    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }


    /**
     * Sets the application context.
     *
     * @param applicationContext the application context
     * @throws BeansException if any error occurs
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Retrieves the proxy instance.
     *
     * @return the proxy instance
     * @throws Exception if any error occurs
     */
    @Override
    public Object getObject() throws Exception {

        return createServiceProxy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getObjectType() {
        return serviceClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Creates the service proxy.
     * <p/>
     * Delegates to registered {@link RestClientProxyFactory} to create the service proxy
     *
     * @return the service proxy
     */
    private Object createServiceProxy() {

        return getProxyFactory().createClientProxy(serviceClass,getServiceUrl());
    }

    /**
     * Retrieves the service url.
     *
     * @return the service url
     */
    private String getServiceUrl() {
        ConfigurableBeanFactory beanFactory = getBeanFactory();
        RestClient configs= serviceClass.getAnnotation(RestClient.class);
        String serviceUrl = configs.value();
        if(beanFactory != null) {
            serviceUrl = (String) beanFactory.getBeanExpressionResolver()
                    .evaluate(serviceUrl, new BeanExpressionContext(beanFactory, null));
        }

        Path basePath = serviceClass.getAnnotation(Path.class);
        if(basePath != null){
            serviceUrl += basePath.value().startsWith(ShenStrings.SLASH) || serviceUrl.endsWith(ShenStrings.SLASH) ?
                    basePath  : (ShenStrings.SLASH + basePath);
        }
        return serviceUrl;
    }

    private ConfigurableBeanFactory getBeanFactory() {

        AutowireCapableBeanFactory beanFactory = this.applicationContext.getAutowireCapableBeanFactory();
        if(beanFactory instanceof ConfigurableBeanFactory) {
            return (ConfigurableBeanFactory) beanFactory;
        }
        return null;
    }


    /**
     * Retrieves the proxy factory.
     *
     * @return the proxy factory
     */
    private RestClientProxyFactory getProxyFactory() {

        try {

            return applicationContext.getBean(RestClientProxyFactory.class);
        } catch (BeansException e) {
            throw new IllegalStateException(
                    "No JaxRsClientProxyFactory has been registered in the application context. " +
                            "Use one of @EnableResteasyClient annotations.", e);
        }
    }
}
