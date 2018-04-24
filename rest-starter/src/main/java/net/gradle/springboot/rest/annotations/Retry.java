package net.gradle.springboot.rest.annotations;

import org.springframework.core.annotation.AliasFor;

/**
 * 是否重试
 */
public @interface Retry {
    /**
     * use policy bean name
     * @return
     */
    String value() default "simpleFixed";

}
