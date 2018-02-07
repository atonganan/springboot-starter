package com.shenit.springboot.wechat.app.pojos;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * 微信用户信息.
 */
public class WxAppUserInfo {
    public String nickName;
    public int gender;
    public String language;
    public String city;
    public String province;
    public String country;
    public String avatarUrl;
    public String openId;
    public String unionId;
    public Watermark watermark;

    public String getUserId(){
        return StringUtils.isNotEmpty(unionId) ? unionId : openId;
    }

    public WxAppUserInfo() {}
    public WxAppUserInfo(Map<String,Object> data) {
        nickName = MapUtils.getString(data,"nickName");
        gender = MapUtils.getIntValue(data,"gender",0);
        language = MapUtils.getString(data,"language");
        city = MapUtils.getString(data,"city");
        province = MapUtils.getString(data,"province");
        country = MapUtils.getString(data,"country");
        avatarUrl = MapUtils.getString(data,"avatarUrl");
        unionId = MapUtils.getString(data,"unionId");
        watermark =  new Watermark();
        Map<String,Object> watermarkData = (Map<String, Object>) MapUtils.getMap(data,"watermark");
        watermark.appid = MapUtils.getString(watermarkData,"appid");
        watermark.timestamp= new Date(MapUtils.getLongValue(watermarkData,"timestamp",-1l));

    }

    public static class Watermark{
        public Date timestamp;
        public String appid;
    }
}
