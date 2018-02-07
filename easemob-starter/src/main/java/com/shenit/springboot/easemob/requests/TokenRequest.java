package com.shenit.springboot.easemob.requests;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jiangnan on 18/06/2017.
 */
public class TokenRequest {
    private static final String GRANT_TYPE= "client_credentials";
    @SerializedName("grant_type")
    private  final String grantType = GRANT_TYPE;
    @SerializedName("client_id")
    private final String clientId;
    @SerializedName("client_secret")
    private  final String secret;

    public TokenRequest(String clientId, String secret){
        this.clientId = clientId;
        this.secret = secret;
    }

}
