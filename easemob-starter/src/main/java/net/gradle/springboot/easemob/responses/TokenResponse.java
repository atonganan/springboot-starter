package net.gradle.springboot.easemob.responses;

import com.google.gson.annotations.SerializedName;

/**
 * token请求响应对象.
 * Created by jiangnan on 18/06/2017.
 */
public class TokenResponse {
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("expires_in")
    public int expireSeconds;
    @SerializedName("application")
    public String appId;
}
