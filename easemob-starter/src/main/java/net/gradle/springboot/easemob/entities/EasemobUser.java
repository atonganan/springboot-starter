package net.gradle.springboot.easemob.entities;

import com.google.gson.annotations.SerializedName;

/**
 * 环信用户数据实体
 * Created by jiangnan on 18/06/2017.
 */
public class EasemobUser {
    public String uuid;
    public String type;
    @SerializedName("created")
    public long createdAt;
    @SerializedName("modified")
    public long updatedAt;
    public String username;
    public boolean activated;
}
