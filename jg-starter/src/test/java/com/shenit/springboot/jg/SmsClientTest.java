package com.shenit.springboot.jg;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jsms.api.JSMSClient;
import cn.jsms.api.SendSMSResult;
import cn.jsms.api.common.model.SMSPayload;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootApplication(scanBasePackages = "com.shenit.springboot")
public class SmsClientTest {
    @Autowired private JSMSClient client;


    @Test
    public void testSendVerifyCode() throws APIConnectionException, APIRequestException {
        SendSMSResult result = client.sendTemplateSMS(SMSPayload.newBuilder()
                .setMobileNumber("18620697874")
                .setTempId(141258)
                .addTempPara("code","1234")
                .setTTL(600)
                .build());
        Assert.assertTrue(result.isResultOK());
        System.out.println("Message ID -> "+ result.getMessageId());
        System.out.println("Message Content -> "+ result.getOriginalContent());
    }
}
