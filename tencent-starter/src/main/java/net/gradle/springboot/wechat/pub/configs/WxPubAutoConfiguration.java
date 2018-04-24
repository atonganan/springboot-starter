package net.gradle.springboot.wechat.pub.configs;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.gradle.commons.utils.GsonUtils;
import net.gradle.springboot.redis.RedisClient;
import net.gradle.springboot.wechat.pub.WxPubClient;
import net.gradle.springboot.wechat.pub.pojos.AccessTokenWrapper;
import net.gradle.springboot.wechat.pub.pojos.JsApiTicketWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 微信公众号配置.
 * 当应用配置了wechat.pub.appid的时候启用这个配置.
 */
@ConditionalOnProperty({"wechat.pub.appid","wechat.pub.appkey"})
@Configuration
public class WxPubAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(WxPubAutoConfiguration.class);

    @Bean
    public WxPubClient wxPubClient(RestTemplate rest,
                                   @Value("${wechat.pub.appid}")String appid,
                                   @Value("${wechat.pub.appkey}")String appkey){
        return new WxPubClient(rest,appid,appkey);
    }

    @Bean(name = "wxPubAccessToken")
    public LoadingCache<String,String> wxPubAccessToken(RedisClient redis, WxPubClient client){
        return CacheBuilder.newBuilder()
                .expireAfterWrite(7200, TimeUnit.SECONDS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String appid) throws Exception {
                        if(redis == null) return client.getAccessToken().accessToken;

                        String path = "/wechat/pub/accessToken";
                        AccessTokenWrapper wrapper = redis.getObject(path,AccessTokenWrapper.class);
                        if(wrapper == null) {
                            wrapper = client.getAccessToken();
                            redis.set(path, GsonUtils.format(wrapper),wrapper.expires);
                        }
                        return wrapper.accessToken;
                    }
                });
    }

    @Bean(name = "wxPubJsApiTicket")
    public LoadingCache<String,String> wxPubJsApiTicket(RedisClient redis,
                                                        @Qualifier("wxPubAccessToken") LoadingCache<String,String> token,
                                                        WxPubClient client){
        return CacheBuilder.newBuilder()
                .expireAfterWrite(7200, TimeUnit.SECONDS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String appid) throws Exception {
                        if(redis == null) return client.getJsApiTicket(token.get(StringUtils.EMPTY)).ticket;
                        String path = "/wechat/pub/jsapi";

                        JsApiTicketWrapper wrapper = null;
                        try {
                            wrapper = redis.getObject(path,JsApiTicketWrapper.class);
                            if(wrapper == null) {
                                wrapper = client.getJsApiTicket(token.get(StringUtils.EMPTY));
                                redis.set(path, GsonUtils.format(wrapper), wrapper.expires);
                            }
                        }catch(Exception ex){
                            LOG.error("[load([appid])] No js wrapper to appid -> " + appid, ex);
                            wrapper = JsApiTicketWrapper.EMPTY;
                        }
                        return wrapper.ticket;
                    }
                });
    }

}
