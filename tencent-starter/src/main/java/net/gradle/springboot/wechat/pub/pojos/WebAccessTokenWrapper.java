package net.gradle.springboot.wechat.pub.pojos;

import com.google.gson.annotations.SerializedName;

public class WebAccessTokenWrapper extends AccessTokenWrapper {
    @SerializedName("openid")
    public String openid;
    @SerializedName("scope")
    public String scope;
    @SerializedName("refresh_token")
    public String refreshToken;
}
