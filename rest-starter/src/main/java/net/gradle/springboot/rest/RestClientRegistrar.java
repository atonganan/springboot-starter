package net.gradle.springboot.rest;

import net.gradle.springboot.rest.annotations.EnableRestClient;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RestClientRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * The annotation class.
     */
    private static final Class<EnableRestClient> ANNOTATION_CLASS = EnableRestClient.class;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {

        final List<String> basePackages = new ArrayList<>();
        final Map<String, Object> attributes = annotationMetadata
                .getAnnotationAttributes(ANNOTATION_CLASS.getName(), false);

        addAll(basePackages, attributes, "value");
        addAll(basePackages, attributes, "basePackages");

        final RestClientClassPathScanner scanner = new RestClientClassPathScanner(registry);
        scanner.scan(toArray(basePackages));
    }

    /**
     * Retrieves the attribute by it's name
     *
     * @param attributes    the attribute map
     * @param attributeName the attribute name
     * @param <T>           the attribute type
     * @return the attribute value
     */
    @SuppressWarnings("unchecked")
    private static <T> T get(Map<String, Object> attributes, String attributeName) {
        return (T) attributes.get(attributeName);
    }

    /**
     * Adds all values associated with the given attribute.
     *
     * @param list          the list of values
     * @param attributes    the attributes map
     * @param attributeName the attribute name
     */
    private static void addAll(List<String> list, Map<String, Object> attributes, String attributeName) {
        addAll(list, get(attributes, attributeName));
    }

    /**
     * Ads all values to the value list.
     *
     * @param list   the list of values
     * @param values the values
     */
    private static void addAll(List<String> list, String[] values) {
        list.addAll(Arrays.asList(values));
    }

    /**
     * Converts the list into the array.
     *
     * @param list the list
     * @return the array
     */
    private static String[] toArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }
}