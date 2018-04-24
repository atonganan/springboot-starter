package net.gradle.springboot.rest.annotations;

import net.gradle.springboot.rest.RestClientRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 利用这个接口生命使用<code>RestClientClassPathScanner</code>扫描的类.
 */
@Import(RestClientRegistrar.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableRestClient {
    /**
     * 扫描的包路径集合
     * @return
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * 扫描的包路径集合
     * @return
     */
    @AliasFor("value")
    String[] basePackages() default {};
}
