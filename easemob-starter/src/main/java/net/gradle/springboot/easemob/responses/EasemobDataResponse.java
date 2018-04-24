package net.gradle.springboot.easemob.responses;

import net.gradle.commons.utils.ShenCollections;

import java.util.List;
import java.util.Map;

/**
 * 一个通用的环信响应类型
 * Created by jiangnan on 18/06/2017.
 */
public class EasemobDataResponse<T> {
    public String action;
    public String application;
    public Map<String,Object> params;
    public String path;
    public String uri;
    public long timestamp;
    public int duration;
    public String organization;
    public String applicationName;
    public List<T> data;


    public T data(int i){
        return ShenCollections.get(data,i);
    }

    public T first(){
        return data(0);
    }

}
