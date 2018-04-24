package net.gradle.springboot.wechat.app.pojos;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;

public class WxAppUserCredentialInfo {
    public WxAppUserInfo userInfo;
    public String rawData;
    public String signature;
    public String encryptedData;
    public String iv;

    public WxAppUserCredentialInfo() {}

    public WxAppUserCredentialInfo(Object rawData) {
        if(!(rawData instanceof Map)) return;
        Map<String,Object> data = (Map<String,Object>) rawData;

        userInfo = new WxAppUserInfo((Map<String, Object>) MapUtils.getMap(data,"userInfo"));
        rawData = MapUtils.getString(data,"rawData");
        signature = MapUtils.getString(data,"signature");
        encryptedData = MapUtils.getString(data,"encryptedData");
        iv = MapUtils.getString(data,"iv");
    }
}
