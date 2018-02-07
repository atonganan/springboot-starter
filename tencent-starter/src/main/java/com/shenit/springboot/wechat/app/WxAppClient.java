package com.shenit.springboot.wechat.app;

import com.shenit.commons.utils.GsonUtils;
import com.shenit.springboot.wechat.WechatBaseClient;
import com.shenit.springboot.wechat.app.pojos.WxAppSessionInfo;
import com.shenit.springboot.wechat.app.pojos.WxAppUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

/**
 * 微信小程序客户端.
 */
public class WxAppClient extends WechatBaseClient {
    private static final Logger LOG = LoggerFactory.getLogger(WxAppClient.class);
    private final static Base64.Decoder DECODER = Base64.getDecoder();
    private static final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";
    private static final String KEY_SPEC_TYPE = "AES";
    private static final String GET_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
    private final String appid;
    private final String appkey;

    public WxAppClient(RestTemplate rest,String appid, String appkey) {
        super(rest);
        this.appid = appid;
        this.appkey = appkey;
    }



    /**
     * 获取Session信息.
     * @return
     */
    public WxAppSessionInfo getUserSession(String code){
        String resp = rest.getForObject(String.format(GET_SESSION_URL,appid,appkey,code),String.class);
        if (LOG.isTraceEnabled()) LOG.trace("[getUserSession([appid, appkey, code])] response -> {}",resp );
        return GsonUtils.parse(resp,WxAppSessionInfo.class);
    }

    /**
     * 解码信息.
     * @param encrypted
     * @param sessionKey
     * @param iv
     * @return
     */
    public WxAppUserInfo decryptUserInfo(String encrypted, String sessionKey, String iv){
        byte[] keyData = DECODER.decode(sessionKey);
        byte[] encryptedData = DECODER.decode(encrypted);
        byte[] ivData = DECODER.decode(iv);
        byte[] decoded = decode(keyData,ivData,encryptedData);
        if(decoded == null) return null;
        String decrypt = new String(decoded, StandardCharsets.UTF_8);
        if (LOG.isTraceEnabled()) LOG.trace("[decryptUserInfo([encrypted, sessionKey, iv])] decryptUserInfo data -> {}", decrypt);
        return GsonUtils.parse(decrypt,WxAppUserInfo.class);
    }

    /**
     * 签名数据.
     * @param data
     * @param sessionKey
     * @param iv
     * @return
     */
    public String signData(Object data,String sessionKey,String iv){
        byte[] keyData = DECODER.decode(sessionKey);
        byte[] encryptedData = GsonUtils.format(data).getBytes(StandardCharsets.UTF_8);
        byte[] ivData = DECODER.decode(iv);
        return new String(encode(keyData,ivData,encryptedData),StandardCharsets.UTF_8);

    }

    /**
     * 解码
     * @param skey
     * @param iv
     * @param data
     * @return
     */
    private byte[] decode(byte[] skey, byte[] iv, byte[] data) {
        return codec(Cipher.DECRYPT_MODE, skey, iv, data);
    }
    /**
     * 解码
     * @param skey
     * @param iv
     * @param data
     * @return
     */
    private byte[] encode(byte[] skey, byte[] iv, byte[] data) {
        return codec(Cipher.ENCRYPT_MODE, skey, iv, data);
    }

    /**
     * 编解码代码
     * @param mode
     * @param skey
     * @param iv
     * @param data
     * @return
     */
    private static byte[] codec(int mode, byte[] skey, byte[] iv, byte[] data) {
        SecretKeySpec key = new SecretKeySpec(skey, KEY_SPEC_TYPE);
        AlgorithmParameterSpec param = new IvParameterSpec(iv);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(mode, key, param);
            return cipher.doFinal(data);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) LOG.debug("[process([mode, skey, iv, data])] Could not process data", e);
        }
        return null;
    }
}
