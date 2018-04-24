package net.gradle.springboot.easemob.requests;

import net.gradle.commons.utils.Payload;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 发送给用户的系统消息请求.
 * Created by jiangnan on 19/06/2017.
 */
public class SysMessageRequest extends EasemobMesasgeRequest{
    public SysMessageRequest(Long[] toUids, String message,Payload payload,String from) {
        this.type = MessageType.users;
        if(StringUtils.isEmpty(message)) throw new IllegalArgumentException("no_message");
        this.target.addAll(Stream.of(toUids)
                .filter(Objects::nonNull)
                .map(i -> i.toString())
                .collect(Collectors.toList()));
        if(target.isEmpty()) throw new IllegalArgumentException("no_target");
        this.ext=payload;
        this.message.put(FIELD_TYPE,MESSAGE_TYPE_TXT);
        this.message.put(FIELD_MSG,message);

        this.fromAccount = from;
    }
}
