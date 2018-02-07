package com.shenit.springboot.easemob.forms;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public class UserForm {
    public final String username;
    public final String password;
    public String nickname;

    public UserForm(String username, String password){
        if(StringUtils.isAnyEmpty(username,password)) throw new IllegalArgumentException("User name and password should not be null");
        this.username = username;
        this.nickname = username;
        this.password = password;
    }
}
