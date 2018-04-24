package net.gradle.springboot.oauth.qq;

import net.gradle.springboot.oauth.qq.pojos.QqUserInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试OpenQQClient接口.
 * @author jiangnan
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@SpringBootApplication(scanBasePackages = "net.gradle.springboot")
public class QqOAuthClientTest {
    @Autowired private QqOAuthClient client;

    @Test
    public void testGetUserInfo() throws InterruptedException {
        //测试Token的获取地址：https://connect.qq.com/sdk/webtools/index.html#get_user_info
        QqUserInfo user = client.getUserInfo("B95CF257300B6A02B5C08E699CBA1EF2","EDAF8A4B8956ACE69C1EAA0313089AFB");
        Assert.assertNotNull(user);
        Assert.assertFalse(user.msg,user.hasError());
    }
}
