package net.gradle.springboot.easemob.requests;

import net.gradle.commons.utils.Payload;

/**
 * 发送给用户的系统透传消息.
 * Created by jiangnan on 19/06/2017.
 */
public class SysActionRequest extends SysMessageRequest{
    private static final String ADMIN_ACCOUNT = "admin";
    public SysActionRequest(Long[] toUids, String message,Payload payload) {
        super(toUids,message,payload,null);
        this.message.put(FIELD_TYPE,MESSAGE_TYPE_CMD);  //改写消息类型为CMD，其他都一样
    }
}
