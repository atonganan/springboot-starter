package com.shenit.springboot.rest;

import com.shenit.springboot.rest.annotations.RestClient;
import com.shenit.springboot.rest.proxies.RestClientProxyFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * 扫描RestClient实例
 */
public class RestClientClassPathScanner extends ClassPathBeanDefinitionScanner {

    public RestClientClassPathScanner(BeanDefinitionRegistry registry) {
        super(registry,false);
        registerFilters();
    }

    /**
     * Registers the filters.
     */
    protected void registerFilters() {
        // registers the RestClient annotation
        addIncludeFilter(new AnnotationTypeFilter(RestClient.class));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        final Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (!beanDefinitions.isEmpty()) {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    /**
     * Process the bean definitions.
     *
     * @param beanDefinitions the bean definitions
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {

        for (BeanDefinitionHolder beanDefinition : beanDefinitions) {
            GenericBeanDefinition definition = (GenericBeanDefinition) beanDefinition.getBeanDefinition();
            final String serviceClassName = definition.getBeanClassName();

            definition.setBeanClass(RestClientProxyFactoryBean.class);
            definition.getPropertyValues().add("serviceClass", serviceClassName);
        }
    }
}
