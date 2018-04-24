package net.gradle.springboot.easemob.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 环信聊天室数据实体
 * Created by jiangnan on 18/06/2017.
 */
public class EasemobRoom {
    @SerializedName("membersonly")
    public boolean membersOnly;
    @SerializedName("allowinvites")
    public boolean allowInvites;
    @SerializedName("public")
    public boolean toPublic;
    public String name;
    public String description;
    public List<Owner> affiliations;
    public String id;
    @SerializedName("maxusers")
    public int maxUsers;
    @SerializedName("affiliations_count")
    public int currentMemberCount;

    public static class Owner{
        public String owner;
    }
}
