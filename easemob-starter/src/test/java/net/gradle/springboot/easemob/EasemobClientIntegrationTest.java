package net.gradle.springboot.easemob;

import com.google.common.collect.Lists;
import net.gradle.commons.utils.GsonUtils;
import net.gradle.commons.utils.ShenCollections;
import net.gradle.springboot.easemob.entities.EasemobRoom;
import net.gradle.springboot.easemob.entities.EasemobUser;
import net.gradle.springboot.easemob.forms.UserForm;
import org.easymock.EasyMockRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Created by jiangnan on 17/06/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EasemobClientIntegrationTest.class)
@SpringBootApplication
public class EasemobClientIntegrationTest{
    public EasyMockRule rule = new EasyMockRule(this);
    @Autowired private EasemobClient client;

    @Before
    public void setup(){
        client.getToken(false);
    }

    @Test
    public void testCreateUser(){
        EasemobUser user = ShenCollections.get(client.createUser(
                new UserForm("testuser_0","testuser_0")),
                0);
        Assert.assertNotNull(user);
        System.out.println("User -> "+ GsonUtils.format(user));
    }



    @Test
    public void testCreateUsers(){
        List<UserForm> forms = Lists.newArrayList();
        for(int i=0;i<20;i++){
            forms.add(new UserForm("testuser_"+i,"password_"+i));
        }
        List<EasemobUser> users = client.createUser(forms.toArray(new UserForm[0]));
    }


    @Test
    public void testGetUser(){
        EasemobUser user = client.getUser("10568");
        Assert.assertNotNull(user);
    }

    @Test
    public void testJoinedRooms(){
        List<EasemobRoom> rooms = client.joinedRooms("10568");
        System.out.println(GsonUtils.format(rooms));
    }

    @Test
    public void testDeleteUser(){
        Assert.assertTrue(client.deleteUser("pig_test1"));
    }



    @Test
    public void testCreateChatRoom(){
        String roomId = client.createChatRoom("test room","pig_test1");
        Assert.assertNotNull(roomId);
        System.out.println("Room ID -> "+ roomId);
    }

    @Test
    public void testCreateChatRoomUserNotExists(){
        String roomId = client.createChatRoom("test room","notExists");
        Assert.assertNotNull(roomId);
        System.out.println("Room ID -> "+ roomId);
    }

    @Test
    public void testGetChatRoomInfo(){
        EasemobRoom room= client.getChatRoomInfo("20006560661506");
        Assert.assertNotNull(room);
        System.out.println("Room Info -> "+ GsonUtils.format(room));
    }

    @Test
    public void testDeleteChatRoom(){
        Assert.assertTrue(client.deleteChatRoom("20972840222721"));
    }

    @Test
    public void testDeleteChatRoomNotExists(){
        Assert.assertFalse(client.deleteChatRoom("aaaaa"));
    }

    @Test
    public void testGetUserStatus(){
        String status = client.getUserStatus("19131");
        System.out.println(status);
    }
    @Test
    public  void sendEasemobMessage(){
        String msg="测试";

        client.sendSysMessageToUsers(msg,new Long[]{10914l},null,"admin");
    }
    @Test
    public  void getEasemobRoom(){
        EasemobRoom chatRoomInfo = client.getChatRoomInfo("23168141033476");
        System.out.println(chatRoomInfo.currentMemberCount);

    }


}
