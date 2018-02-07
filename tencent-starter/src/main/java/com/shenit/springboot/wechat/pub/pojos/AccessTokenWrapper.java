package com.shenit.springboot.wechat.pub.pojos;

import com.google.gson.annotations.SerializedName;
import com.shenit.springboot.wechat.pojos.WechatResponse;

public class AccessTokenWrapper extends WechatResponse {
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("expires_in")
    public long expires;
}
