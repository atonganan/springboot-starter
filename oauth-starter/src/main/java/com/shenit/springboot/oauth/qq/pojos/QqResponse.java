package com.shenit.springboot.oauth.qq.pojos;

/**
 * QQ互联通用响应.
 */
public abstract class QqResponse {
    public int ret;
    public String msg;

    /**
     * 判断是否有错
     * @return
     */
    public boolean hasError(){
        return ret != 0;
    }
}
