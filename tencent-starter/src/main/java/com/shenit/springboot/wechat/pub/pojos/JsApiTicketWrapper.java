package com.shenit.springboot.wechat.pub.pojos;

import com.google.gson.annotations.SerializedName;
import com.shenit.springboot.wechat.pojos.WechatResponse;
import org.apache.commons.lang3.StringUtils;

public class JsApiTicketWrapper extends WechatResponse {
    public static final JsApiTicketWrapper EMPTY = new JsApiTicketWrapper();
    public String ticket;
    @SerializedName("expires_in")
    public long expires;

    public JsApiTicketWrapper(){
        ticket = StringUtils.EMPTY;
        expires = 3600l;
    }
}
