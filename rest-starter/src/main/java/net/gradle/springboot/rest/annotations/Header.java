package net.gradle.springboot.rest.annotations;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 用于声明请求的HTTP Header.
 * @author jiangnan
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Header {
    /**
     * 设置头名称
     * @return
     */
    String name();

    /**
     * 设置头的值
     * @return
     */
    String value() default StringUtils.EMPTY;

}
