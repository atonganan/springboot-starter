package net.gradle.springboot.wechat;

import net.gradle.commons.utils.GsonUtils;
import net.gradle.commons.utils.ShenValidates;
import net.gradle.springboot.wechat.open.pojos.WxOpenUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 腾讯开放平台客户端.
 */
public abstract class WechatBaseClient {
    private static final Logger LOG = LoggerFactory.getLogger(WechatBaseClient.class);
    private static final String USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";

    protected final RestTemplate rest;

    public WechatBaseClient(RestTemplate tmpl){
        if(tmpl  == null) throw new IllegalArgumentException("RestTemplate should not be null");
        this.rest = tmpl;
    }


    /**
     * 获取用户信息.
     * @param accessToken Access Token
     * @param openid Openid
     * @return
     */
    public WxOpenUserInfo getUserInfo(String accessToken, String openid){
        ShenValidates.notNullOrEmpty("accessToken",accessToken, "openId",openid);
        String resp = rest.getForObject(String.format(USER_INFO_URL,accessToken,openid),String.class);
        if (LOG.isDebugEnabled()) LOG.debug("[getUserInfo([accessToken, openId])] response -> {}", resp);
        return GsonUtils.parse(resp,WxOpenUserInfo.class);
    }

}
