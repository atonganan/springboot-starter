package net.gradle.springboot.easemob.requests;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import net.gradle.commons.utils.Payload;

import java.util.Set;

/**
 * 环信发送系统消息的请求
 * Created by jiangnan on 19/06/2017.
 */
public class EasemobMesasgeRequest {
    protected static final String FIELD_TYPE = "type";
    protected static final String FIELD_MSG = "msg";

    protected static final String MESSAGE_TYPE_TXT = "txt";
    protected static final String MESSAGE_TYPE_CMD = "cmd";

    @SerializedName("target_type")
    public MessageType type;
    public Set<String> target = Sets.newHashSet();
    @SerializedName("msg")
    public Payload message = new Payload();
    @SerializedName("from")
    public String fromAccount;
    public Payload ext;

    /**
     * 设置扩展信息
     * @param field
     * @param value
     */
    public void putExtend(String field, String value){
        if(ext == null) ext = new Payload();
        ext.put(field,value);
    }

    /**
     * 消息类型.
     */
    public enum MessageType{
        /**发送给用户*/
        users,
        /**发送给聊天组*/
        @SerializedName("chatgroups")
        groups,
        /**发送给聊天室*/
        @SerializedName("chatrooms")
        rooms;
    }

}
