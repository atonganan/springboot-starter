package com.shenit.springboot.wechat.pub;

import com.google.common.collect.Maps;
import com.shenit.commons.utils.GsonUtils;
import com.shenit.commons.utils.ShenStrings;
import com.shenit.springboot.wechat.WechatBaseClient;
import com.shenit.springboot.wechat.pojos.WechatResponse;
import com.shenit.springboot.wechat.pub.pojos.AccessTokenWrapper;
import com.shenit.springboot.wechat.pub.pojos.JsApiTicketWrapper;
import com.shenit.springboot.wechat.pub.pojos.WebAccessTokenWrapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.TreeMap;

/**
 * 腾讯公众号客户端
 */
public class WxPubClient extends WechatBaseClient{
    private static final Logger LOG = LoggerFactory.getLogger(WxPubClient.class);
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String JSAPI_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
    private static final String ACCESS_TOKEN_CODE_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    private static final String REFRESH_ACCESS_TOKEN_URL= "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=%s&grant_type=refresh_token&refresh_token=%s";
    private static final String AUTHO_URL = "https://api.weixin.qq.com/sns/auth?access_token=%s&openid=%s";
    private static final String OAUTH_REQUEST_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=%s#wechat_redirect";

    private static final String PARAM_TIMESTAMP = "timestamp";
    private static final String PARAM_NONCESTR= "noncestr";
    private static final String PARAM_SIGNATURE = "signature";
    private static final String PARAM_APPID = "appid";
    private final String appid;
    private final String appkey;


    public WxPubClient(RestTemplate tmpl,String appid, String appkey) {
        super(tmpl);
        this.appid  = appid;
        this.appkey = appkey;
        getAccessToken();
    }

    /**
     * 验证用户有效性
     * @param accessToken
     * @param openid
     * @return
     */
    public boolean authUser(String accessToken, String openid){
        ResponseEntity<WechatResponse> resp = rest.exchange(String.format(AUTHO_URL,accessToken,openid), HttpMethod.GET,null,WechatResponse.class);
        if(!resp.getStatusCode().equals(HttpStatus.OK)){
            if (LOG.isDebugEnabled()) LOG.debug("[authUser([])] Error response code -> {}", resp.getStatusCodeValue());
            return false;
        }
        WechatResponse result = resp.getBody();
        if (LOG.isDebugEnabled()) LOG.debug("[getWebAccessToken([code])] receive access token -> {}", GsonUtils.format(accessToken));
        return result.errcode == 0;

    }

    /**
     * 通过RefreshToken获取AccessToken
     * @param refreshToken
     * @return
     */
    public WebAccessTokenWrapper getAccessCodeByRefresh(String refreshToken){
        ResponseEntity<String> resp = rest.exchange(String.format(REFRESH_ACCESS_TOKEN_URL,appid,refreshToken), HttpMethod.GET,null,String.class);
        if(!resp.getStatusCode().equals(HttpStatus.OK)){
            if (LOG.isDebugEnabled()) LOG.debug("[getWebAccessToken([])] Error response code -> {}", resp.getStatusCodeValue());
            throw new IllegalStateException("加载AccessToken失败");
        }
        WebAccessTokenWrapper accessToken = GsonUtils.parse(resp.getBody(),WebAccessTokenWrapper.class);
        if (LOG.isDebugEnabled()) LOG.debug("[getWebAccessToken([code])] receive access token -> {}", GsonUtils.format(accessToken));
        if(accessToken.errcode != 0) throw new IllegalStateException("获取AccessToken失败, "+accessToken.errmsg);
        return accessToken;
    }

    /**
     * 通过Code获取用户使用的AccessToken
     * @param code
     * @return
     */
    public WebAccessTokenWrapper getWebAccessToken(String code){
        ResponseEntity<String> resp = rest.exchange(String.format(ACCESS_TOKEN_CODE_URL,appid,appkey,code), HttpMethod.GET,null,String.class);
        if(!resp.getStatusCode().equals(HttpStatus.OK)){
            if (LOG.isDebugEnabled()) LOG.debug("[getWebAccessToken([])] Error response code -> {}", resp.getStatusCodeValue());
            throw new IllegalStateException("加载AccessToken失败");
        }
        WebAccessTokenWrapper accessToken = GsonUtils.parse(resp.getBody(),WebAccessTokenWrapper.class);
        if (LOG.isInfoEnabled()) LOG.info("[getWebAccessToken([code])] receive access token -> {}, with code -> {}", GsonUtils.format(accessToken),code);
        if(accessToken.errcode != 0) throw new IllegalStateException("获取AccessToken失败, "+accessToken.errmsg);
        return accessToken;
    }


    /**
     * 获取AccessToken, 应用需要自己进行缓存管理.
     * @return
     */
    public AccessTokenWrapper getAccessToken(){
        ResponseEntity<String> resp = rest.exchange(String.format(ACCESS_TOKEN_URL, appid, appkey), HttpMethod.GET, null, String.class);
        if (!resp.getStatusCode().equals(HttpStatus.OK)) {
            if (LOG.isDebugEnabled())
                LOG.debug("[getAccessToken([])] Error response code -> {}", resp.getStatusCodeValue());
            throw new IllegalStateException("加载AccessToken失败");
        }
        AccessTokenWrapper accessToken = GsonUtils.parse(resp.getBody(), AccessTokenWrapper.class);
        if (accessToken.errcode != 0)  LOG.error("[getAccessToken([])] 获取AccessToken失败, {}", accessToken.errmsg);
        return accessToken;
    }

    /**
     * 获取JsAPiTicket, 应用需要自己进行缓存管理.
     * @return
     */
    public JsApiTicketWrapper getJsApiTicket(String token){
        if(StringUtils.isEmpty(token)) return JsApiTicketWrapper.EMPTY;
        ResponseEntity<JsApiTicketWrapper> resp = rest.exchange(String.format(JSAPI_TICKET_URL,token), HttpMethod.GET,null,JsApiTicketWrapper.class);
        if(!resp.getStatusCode().equals(HttpStatus.OK)){
            if (LOG.isDebugEnabled()) LOG.debug("[getJsApiTicket([])] Error response code -> {}", resp.getStatusCodeValue());
            throw new IllegalStateException("加载JSApiTicket失败");
        }
        JsApiTicketWrapper wrapper = resp.getBody();
        if(wrapper.errcode != 0) throw new IllegalStateException("获取JSApiTicket失败, "+wrapper.errmsg);

        return wrapper;
    }


    /**
     * 生成JSConfig
     * @param url
     * @param jsTicket
     * @return
     */
    public Map<String,String> jsConfig(String url,String jsTicket){
        if(StringUtils.isAnyEmpty(url,jsTicket)) return null;
        int poundIdx= url.indexOf(ShenStrings.POUND);
        if(poundIdx >0) url = url.substring(0,poundIdx);

        String noncestr = RandomStringUtils.random(16,true,true);
        String ts = ShenStrings.str(System.currentTimeMillis()/1000);
        TreeMap<String,String> params = new TreeMap<>();
        params.put("url",url);
        params.put(PARAM_NONCESTR, noncestr);
        params.put(PARAM_TIMESTAMP,ts);
        params.put("jsapi_ticket",jsTicket);
        StringBuilder build = new StringBuilder();
        params.forEach((k,v) -> {
            build.append(k+ ShenStrings.EQ+v).append(ShenStrings.AMP);
        });
        build.deleteCharAt(build.length() - 1);
        String origin = build.toString();
        String encrypted = DigestUtils.sha1DigestAsHex(origin);
        if (LOG.isDebugEnabled()) LOG.debug("[jsConfig([url, jsTicket])] To sign -> {}, encrypted -> {}",origin, encrypted);

        Map<String,String> result = Maps.newHashMap();
        result.put(PARAM_TIMESTAMP,ts);
        result.put(PARAM_APPID,this.appid);
        result.put(PARAM_NONCESTR, noncestr);
        result.put(PARAM_SIGNATURE, encrypted);
        return result;
    }


    /**
     * 组装授权地址
     * @param url
     * @param state
     * @return
     */
    public String getAuthorizeUrl(String url,String state) {
        return String.format(OAUTH_REQUEST_URL,appid,url,state);
    }
}
