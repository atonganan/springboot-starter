package com.shenit.spring.wechat.app;

import com.shenit.commons.utils.GsonUtils;
import com.shenit.springboot.wechat.app.WxAppClient;
import com.shenit.springboot.wechat.app.pojos.WxAppUserInfo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;


public class WxAppClientTest {
    private WxAppClient client = new WxAppClient(new RestTemplate(),"wx4f4bc4dec97d474b",null);
    private String sessionKey = "tiihtNczf5v6AKRyjwEUhQ==";
    private String encryptedData = "CiyLU1Aw2KjvrjMdj8YKliAjtP4gsMZMQmRzooG2xrDcvSnxIMXFufNstNGTyaGS9uT5geRa0W4oTOb1WT7fJlAC+oNPdbB+3hVbJSRgv+4lGOETKUQz6OYStslQ142dNCuabNPGBzlooOmB231qMM85d2/fV6ChevvXvQP8Hkue1poOFtnEtpyxVLW1zAo6/1Xx1COxFvrc2d7UL/lmHInNlxuacJXwu0fjpXfz/YqYzBIBzD6WUfTIF9GRHpOn/Hz7saL8xz+W//FRAUid1OksQaQx4CMs8LOddcQhULW4ucetDf96JcR3g0gfRK4PC7E/r7Z6xNrXd2UIeorGj5Ef7b1pJAYB6Y5anaHqZ9J6nKEBvB4DnNLIVWSgARns/8wR2SiRS7MNACwTyrGvt9ts8p12PKFdlqYTopNHR1Vf7XjfhQlVsAJdNiKdYmYVoKlaRv85IfVunYzO0IKXsyl7JCUjCpoG20f0a04COwfneQAGGwd5oa+T8yO5hzuyDb/XcxxmK01EpqOyuxINew==";
    private String iv = "r7BXXKkLb8qrSNn05n0qiA==";

    @Test
    public void testDecrypt(){
        WxAppUserInfo info = client.decryptUserInfo(encryptedData,sessionKey,iv);
        Assert.assertNotNull(info);
        Assert.assertEquals("Band",info.nickName);
        System.out.println(GsonUtils.format(info));
    }


}