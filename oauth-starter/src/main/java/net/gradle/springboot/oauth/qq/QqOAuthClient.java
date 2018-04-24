package net.gradle.springboot.oauth.qq;

import net.gradle.commons.utils.GsonUtils;
import net.gradle.commons.utils.ShenValidates;
import net.gradle.springboot.oauth.qq.pojos.QqUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 腾讯开放平台客户端.
 */
public class QqOAuthClient {
    private static final Logger LOG = LoggerFactory.getLogger(QqOAuthClient.class);
    private static final String GET_USER_INFO = "https://graph.qq.com/user/get_user_info?oauth_consumer_key=%s&access_token=%s&openid=%s";

    private final String appid;
    private RestTemplate rest;

    public QqOAuthClient(String appid){
        this.appid = appid;
    }

    /**
     * 设置<code>org.springframework.web.client.RestTemplate</code>实例.
     * @param rest
     */
    public void setRestTemplate(RestTemplate rest){
        this.rest = rest;
        checkRestTemplate();
    }


    /**
     * 获取用户信息.
     * @param accessToken ACCESS TOKEN
     * @param openId OPENID
     * @return
     */
    public QqUserInfo getUserInfo(String accessToken, String openId){
        ShenValidates.notNullOrEmpty("accessToken",accessToken,"openId",openId);
        checkRestTemplate();
        String resp = rest.getForObject(
                String.format(GET_USER_INFO,appid,accessToken,openId),
                String.class);
        if (LOG.isDebugEnabled()) LOG.debug("[getUserInfo([accessToken, openId])] response -> {}", resp);
        return GsonUtils.parse(resp,QqUserInfo.class);
    }

    private void checkRestTemplate() {
        if(rest  == null) throw new IllegalArgumentException("RestTemplate should not be null");
    }
}
