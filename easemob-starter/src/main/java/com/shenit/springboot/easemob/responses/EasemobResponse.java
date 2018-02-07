package com.shenit.springboot.easemob.responses;

import com.shenit.commons.utils.ShenCollections;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * 一个通用的环信响应类型
 * Created by jiangnan on 18/06/2017.
 */
public class EasemobResponse<T> {
    public String action;
    public String application;
    public Map<String,Object> params;
    public String path;
    public String uri;
    public List<T> entities;
    public long timestamp;
    public int duration;
    public String organization;
    public String applicationName;
    public Map<String,Object> data;

    public List<T> entities(){
        return entities;
    }

    public Object getData(String key){
        return MapUtils.getObject(data,key);
    }

    public T entity(int i){
        return ShenCollections.get(entities,i);
    }

    public T first(){
        return entity(0);
    }

}
