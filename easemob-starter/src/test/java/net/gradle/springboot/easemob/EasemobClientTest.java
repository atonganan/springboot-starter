package net.gradle.springboot.easemob;

import net.gradle.springboot.easemob.responses.TokenResponse;
import org.easymock.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

/**
 * Created by jiangnan on 17/06/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EasemobClientTest.class)
@SpringBootApplication
public class EasemobClientTest extends EasyMockSupport{
    @Rule public EasyMockRule rule = new EasyMockRule(this);
    @TestSubject @Autowired private EasemobClient client;
    @Mock RestTemplate tmpl;

    @Before
    public void prepare(){
        client.setRestTemplate(tmpl);
    }

    /**
     * 测试获取token的时候是否会从缓存来
     */
    @Test
    public void testGetTokenWithCache(){
        ResponseEntity<TokenResponse> resp = new ResponseEntity<TokenResponse>(
                new TokenResponse(), HttpStatus.OK);
        resp.getBody().accessToken = "testToken";
        resp.getBody().expireSeconds = 3;    //expire in 3 seconds
        EasyMock.expect(tmpl.exchange(
                EasyMock.eq(client.getBaseUrl()+"/token"),
                EasyMock.eq(HttpMethod.POST),
                EasyMock.anyObject(),
                EasyMock.eq(TokenResponse.class)))
                .andReturn(resp)
                .times(1);
        //now test cache
        replayAll();
        Assert.assertEquals("testToken",client.getToken(true));
        Assert.assertEquals("testToken",client.getToken(false));
        EasyMock.verify(tmpl);
    }

    @Test
    public void testGetTokenCacheExpire() throws InterruptedException {
        ResponseEntity<TokenResponse> resp = new ResponseEntity<TokenResponse>(
                new TokenResponse(), HttpStatus.OK);
        resp.getBody().accessToken = "testToken";
        resp.getBody().expireSeconds = 3;    //expire in 3 seconds
        EasyMock.expect(tmpl.exchange(
                EasyMock.eq(client.getBaseUrl()+"/token"),
                EasyMock.eq(HttpMethod.POST),
                EasyMock.anyObject(),
                EasyMock.eq(TokenResponse.class)))
                .andReturn(resp)
                .times(2);
        //now test cache
        replayAll();
        Assert.assertEquals("testToken",client.getToken(true));
        Thread.sleep(5000);
        Assert.assertEquals("testToken",client.getToken(false));
        EasyMock.verify(tmpl);
    }


}
