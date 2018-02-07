package com.shenit.springboot.rest.invokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.lang.reflect.Method;

/**
 * 重试处理器.
 */
public class RetryRestInvoker extends BasicRestInvoker{
    private static final Logger LOG = LoggerFactory.getLogger(RetryRestInvoker.class);
    /**
     * 构造一个基础的REST调用类.
     *
     * @param method
     * @param baseUrl
     */
    public RetryRestInvoker(Method method, String baseUrl) {
        super(method, baseUrl);
    }

    private RetryTemplate retryTemplate;

    public void setRetryTemplate(RetryTemplate retry) {
        this.retryTemplate = retry;
    }

    @Override
    public Object invoke(Object[] args) {
        try {
            return retryTemplate.execute(new RetryCallback<Object, Throwable>() {
                @Override
                public Object doWithRetry(RetryContext context) throws Throwable {
                    return RetryRestInvoker.super.invoke(args);
                }
            });
        } catch (Throwable throwable) {
            LOG.warn("[invoke([args])] Invoke retry failed", throwable);
        }
        return null;
    }

}
