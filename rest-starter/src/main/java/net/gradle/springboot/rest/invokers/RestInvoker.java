package net.gradle.springboot.rest.invokers;

/**
 * 具体执行REST请求的方法.
 */
public interface RestInvoker {
    /**
     * 调用方法.
     * @param args
     * @return
     */
    Object invoke(Object[] args);
}
