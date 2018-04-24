package net.gradle.springboot.wechat.pojos;

/**
 * 微信接口通用响应.
 */
public abstract class WechatResponse {
    public int errcode;
    public String errmsg;

    /**
     * 判断是否有错
     * @return
     */
    public boolean hasError(){
        return errcode != 0;
    }
}
