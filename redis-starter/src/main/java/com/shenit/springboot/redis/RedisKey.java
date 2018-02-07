package com.shenit.springboot.redis;

import com.shenit.commons.utils.ShenStrings;

/**
 * Redis的Key管理工具类.
 */
public final class RedisKey {
    private static final String PATH_SEPERATOR = "/";

    /**
     * 组装路径
     * @param path
     * @return
     */
    public static String path(Object... path){
        return ShenStrings.joinWith(path,PATH_SEPERATOR);
    }
}
