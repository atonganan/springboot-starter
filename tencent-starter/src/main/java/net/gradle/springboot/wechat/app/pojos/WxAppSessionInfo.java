package net.gradle.springboot.wechat.app.pojos;

import com.google.gson.annotations.SerializedName;
import net.gradle.springboot.wechat.pojos.WechatResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * 微信小程序的session信息.
 */
public class WxAppSessionInfo extends WechatResponse {
    public String openid;
    @SerializedName("session_key")
    public String sessionKey;
    public String unionid;

    public String getUserId(){
        return StringUtils.isNotEmpty(unionid) ? unionid : openid;
    }
}
