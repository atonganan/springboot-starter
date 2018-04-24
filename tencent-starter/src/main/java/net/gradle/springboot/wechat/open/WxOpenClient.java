package net.gradle.springboot.wechat.open;

import net.gradle.springboot.wechat.WechatBaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 腾讯开放平台客户端.
 */
public class WxOpenClient extends WechatBaseClient {
    private static final Logger LOG = LoggerFactory.getLogger(WxOpenClient.class);

    public WxOpenClient(RestTemplate tmpl) {
        super(tmpl);
    }
}

