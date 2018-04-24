package com.shenit.springboot.redis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.shenit.commons.utils.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Redis客户端支持.
 * 简化Redis的调用
 * Created by jiangnan on 27/07/2017.
 */
public class RedisClient {

    private static final String ABSOLUTE_PATH = "path";
    private static final String PUBLIC_SCHEMA = "public";

    private final String schema;
    private final String publicSchema;
    private RedisTemplate<String,Object> redis;

    public RedisClient( String namespace, String schema,RedisTemplate<String, Object> redis) {
        this.schema = ShenStrings.joinWith(namespace,schema, ShenStrings.COLON);
        this.publicSchema = ShenStrings.joinWith(ABSOLUTE_PATH, namespace, PUBLIC_SCHEMA, ShenStrings.COLON);
        this.redis = redis;
    }

    /**
     * 获取所有hash的值
     * @param key
     * @return
     */
    public Map<Object, Object> hgetall(String key) {
        return redis.opsForHash().entries(schemadPath(key));
    }

    /**
     * 获取所有hash值
     * @param key
     * @param valueSupplier
     * @param <V>
     * @return
     */
    public <V> Map<Object, V> hgetall(String key, Function<Object, V> valueSupplier) {
        return hgetall(key, null, valueSupplier);
    }

    /**
     * 获取所有hash的值
     * @param key
     * @return
     */
    public <K, V> Map<K, V> hgetall(String key, Function<Object, K> keySupplier, Function<Object, V> valueSupplier) {
        Map<Object,Object> values =  redis.opsForHash().entries(schemadPath(key));
        Map<K, V> result = Maps.newHashMap();
        for(Object k : values.keySet()){
            result.put(
                    keySupplier == null ? (K) k : keySupplier.apply(k),
                    valueSupplier.apply(values.get(k)));
        }
        return result;
    }


    /**
     * 批量批量添加值
     * @param key 值
     * @param entries 待插入的值
     */
    public void hmset(String key, Map<?, ?> entries) {
        redis.opsForHash().putAll(schemadPath(key),entries);
    }

    /**
     * 删除键
     * @param keys
     */
    public void del(String... keys) {
        if (ArrayUtils.isEmpty(keys)) return;

        redis.execute(new RedisCallback<Object>() {

            public Object doInRedis(RedisConnection connection) {
                final byte[][] rawKeys = new byte[keys.length][];

                int i = 0;
                for (String key : keys) rawKeys[i++] = schemadPath(key).getBytes();
                connection.del(rawKeys);
                return null;
            }
        }, true);
    }

    /**
     * 添加一个排序的set元素
     * @param key
     * @param value
     * @param score
     */
    public void zadd(String key, Object value, double score) {
        redis.opsForZSet().add(schemadPath(key), ShenStrings.str(value),score);
    }

    /**
     * 添加一个排序的set元素
     * @param key
     * @param keysScores
     */
    public void zadd(String key, Object... keysScores) {
        ZSetOperations<String,Object> zset = redis.opsForZSet();
        Map<Object,Object> scoreMap = ShenArrays.extract(keysScores);
        Set<ZSetOperations.TypedTuple<Object>> scores = Sets.newHashSet();
        scoreMap.forEach((k, v) -> {
            scores.add(new DefaultTypedTuple<>(
                    k,
                    ShenNumbers.toDouble(v, 0d)));
        });
        zset.add(schemadPath(key),scores);
    }

    /**
     * 移除一个排序set的元素
     * @param key
     * @param
     */
    public void zrem(String key, Object... hints) {
        redis.opsForZSet().remove(schemadPath(key),(Object[]) ShenStrings.strs(hints));
    }

    /**
     * 获取一个缓存的值并且更新其存活时间
     * @param key
     * @param clazz 返回类
     * @param expire
     * @param supplier
     * @param <T>
     * @return
     */
    public <T> T getSet(String key, Class<T> clazz, long expire, Supplier<T> supplier){
        key = schemadPath(key);
        ValueOperations<String,Object> ops = redis.opsForValue();
        T result = GsonUtils.parse(ShenStrings.str(ops.get(key)),clazz);
        return doGetSet(key,result,expire,supplier);
    }

    /**
     * 拿出来，如果不为空更新过期时间
     * @param key
     * @param type
     * @param expire
     * @param <T>
     * @return
     */
    public <T> T getAndKeepalive(String key, Class<T> type, long expire){
        key = schemadPath(key);
        ValueOperations<String,Object> ops = redis.opsForValue();
        String val = ShenStrings.str(ops.get(key));
        if(StringUtils.isEmpty(val)) return null;
        redis.expire(key,expire, TimeUnit.SECONDS);
        return GsonUtils.parse(val, type);
    }

    /**
     * 拿出来，如果不为空更新过期时间
     * @param key
     * @param type
     * @param expire
     * @param <T>
     * @return
     */
    public <T> T getWithDefault(String key, Class<T> type, long expire,Supplier<T> supplier){
        key = schemadPath(key);
        ValueOperations<String,Object> ops = redis.opsForValue();
        String val = ShenStrings.str(ops.get(key));
        if(StringUtils.isEmpty(val)) {
            val = ShenStrings.str(supplier.get());
            ops.set(key,val,expire,TimeUnit.SECONDS);
        }
        return GsonUtils.parse(val, type);
    }

    /**
     * 获取一个缓存的值.
     * 如果没有值，则尝试加载这个值，并且保存一定的有效期.
     * @param key
     * @param type 返回类
     * @param expire
     * @param supplier
     * @param <T>
     * @return
     */
    public <T> T getSet(String key, TypeToken<T> type, long expire, Supplier<T> supplier){
        key = schemadPath(key);
        ValueOperations<String,Object> ops = redis.opsForValue();
        T result = GsonUtils.parse(ShenStrings.str(ops.get(key)),type);
        return doGetSet(key,result,expire,supplier);
    }

    /**
     * 实际处理getAndSet的方法
     * @param key
     * @param result
     * @param expire
     * @param supplier
     * @param <T>
     * @return
     */
    private <T> T doGetSet(String key, T result, long expire, Supplier<T> supplier) {
        ValueOperations<String,Object> ops = redis.opsForValue();
        if(result == null) result = supplier.get();
        //仍然为空值，则删除这个值
        if(result == null) redis.delete(key);
            //否则刷新设置时间
        else ops.set(key,result,expire,TimeUnit.SECONDS);
        return result;
    }

    /**
     * 获取Long值
     * @param key
     * @param defaultVal
     * @return
     */
    public Long getLong(String key, Long defaultVal) {
        String data = (String) redis.opsForValue().get(schemadPath(key));
        return ShenNumbers.toLong(data,defaultVal);
    }

    /**
     * 过期时间
     * @param key
     * @param amount
     * @param unit
     */
    public void expire(String key, int amount, TimeUnit unit) {
        redis.expire(schemadPath(key),amount,unit);
    }

    /**
     * 设置值
     * @param key
     * @param data
     * @param seconds
     */
    public void set(String key, String data, Long seconds) {
        redis.executePipelined(new RedisCallback<Void>(){

            @Override
            public Void doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] keyBytes = schemadPath(key).getBytes();
                connection.set(keyBytes,data.getBytes(StandardCharsets.UTF_8));
                if(ShenValidates.gt(seconds,0))
                    connection.expire(keyBytes,seconds);
                return null;
            }
        });
    }

    /**
     * 获取指定Key，并且转换成Map
     * @param key
     * @return
     */
    public Map<String,Object> getJson(String key) {
        String json = ShenStrings.str(redis.opsForValue().get(schemadPath((key))));
        return StringUtils.isEmpty(json) ? null : GsonUtils.map(json);
    }

    /**
     * 获取指定的值，并且转换为指定类型
     * @param key
     * @param type
     * @param <T>
     * @return
     */
    public <T> T getObject(String key, Class<T> type) {
        String json  = ShenStrings.str(redis.opsForValue().get(schemadPath(key)));
        return StringUtils.isNotEmpty(json) ? GsonUtils.parse(json,type) : null;
    }

    /**
     * Set add command
     * @param key
     * @param vals
     */
    public Long sadd(String key, Object... vals) {
        return redis.opsForSet().add(schemadPath(key),vals);
    }

    /**
     * Set remove command
     * @param key
     * @param vals
     */
    public void srem(String key, Object... vals) {
        redis.opsForSet().remove(schemadPath(key),vals);
    }

    /**
     * 按照分数由小到大获取顺序序列元素
     * @param key
     * @param start
     * @param end
     */
    public Set<Object> zrange(String key, int start, int end) {
        return redis.opsForZSet().range(schemadPath(key),start,end);
    }

    /**
     * 按照分数由小到大获取顺序序列元素
     * @param path
     * @param start
     * @param end
     */
    public Set<Object> zrevrange(String path, int start, int end) {
        return redis.opsForZSet().reverseRange(schemadPath(path), start, end);
    }

    public Set<ZSetOperations.TypedTuple<Object>> zrevrangeWithScore(String path, int start, int end) {
        return redis.opsForZSet().reverseRangeWithScores(schemadPath(path), start, end);
    }

    public Long zrevrank(String path, Object value) {
        return redis.opsForZSet().reverseRank(schemadPath(path), value);
    }


    /**
     * 获取有序集合的成员的得分
     */
    public Double zscore(String path, Long key) {
        return redis.opsForZSet().score(schemadPath(path), key);
    }

    public void pipeline(RedisCallback<?> callback) {
        redis.executePipelined(callback);
    }


    public void pipeline(SessionCallback<?> callback) {
        redis.executePipelined(callback);
    }


    /**
     * 递增1
     * @param key
     * @param delta
     * @return
     */
    public long incr(String key, long delta) {
        return redis.opsForValue().increment(schemadPath(key),delta);
    }

    public String getString(String key) {
        return ShenStrings.str(redis.opsForValue().get(schemadPath(key)));
    }


    /**
     * 获取整型树枝
     * @param key
     * @return
     */
    public Integer getInteger(String key,Integer defaultValue) {
        return ShenNumbers.toInteger(redis.opsForValue().get(schemadPath(key)),defaultValue);
    }


    /**
     * 获取整型树枝
     * @param key
     * @return
     */
    public Integer getInteger(String key) {
        return getInteger(key,null);
    }

    /**
     * 获取double值
     * @param key
     * @param defaultValue
     * @return
     */
    public Double getDouble(String key, Double defaultValue) {
        return ShenNumbers.toDouble(redis.opsForValue().get(schemadPath(key)),defaultValue);
    }

    /**
     * 设置位数
     * @param key 键
     * @param pos 设置位置
     * @return 如果有变化,返回true; 否则返回false
     */
    public boolean setbit(String key, long pos) {
        return setbit(key,pos,true);
    }

    /**
     * 设置位数
     * @param key 键
     * @param pos 设置位置
     * @param on 是否为1
     * @return 如果有变化,返回true; 否则返回false
     */
    public boolean setbit(String key, long pos,boolean on) {
        if(pos < 0) return false;
        return redis.opsForValue().setBit(schemadPath(key),pos,on);
    }

    /**
     * 获取keys对应的所有
     * @param holder
     * @param keys
     * @param <V>
     * @return
     */
    public <K,V> List<V> hmGetSetAll(String holder, Collection<K> keys, Supplier<Map<K,V>> supplier) {
        holder = schemadPath(holder);
        HashOperations<String,K,V> hash = redis.opsForHash();
        List<V> result = hash.multiGet(holder,keys);
        if(result == null){
            Map<K,V> map = supplier.get();
            if(MapUtils.isEmpty(map)) return Collections.EMPTY_LIST;
            hash.putAll(holder,map);
            //自己过滤内容
            result = ShenMaps.getAll(map,keys);
        }
        return result;
    }

    /**
     * 获取keys对应的所有
     * @param holder
     * @param keys
     * @param <V>
     * @return
     */
    public <K,V> Map<K,V> hmGetSetAllEntries(String holder, Collection<K> keys, Supplier<Map<K,V>> supplier, Type keyType, Type valType) {
        holder = schemadPath(holder);
        HashOperations<String,K,V> hash = redis.opsForHash();
        Map<K,V> result = hash.entries(holder);
        if(MapUtils.isEmpty(result)){
            Map<K,V> map = supplier.get();
            if(MapUtils.isEmpty(map)) return Collections.EMPTY_MAP;
            hash.putAll(holder,map);
            //自己过滤内容
            result = ShenMaps.entries(map,keys);
        }else{
            //对于redis，只能保存字符串值，并且不会自动反序列化
            Map<K,V> converted = Maps.newHashMap();
            for(K key : keys){
                //因为redis反序列化默认用string做key.
                String skey = key.toString();
                if(!result.containsKey(skey)) continue;
                V val = result.get(skey);
                converted.put(GsonUtils.parse(ShenStrings.str(skey),keyType), GsonUtils.parse(ShenStrings.str(val),valType));
            }
            result = converted;
        }
        return result;
    }

    /**
     * 检查列表的长度
     * @param path
     * @return
     */
    public long llen(String path) {
        return redis.opsForList().size(schemadPath(path));
    }


    /**
     * 往列表尾插入多个值.
     * param path 路径
     * @param vals 值
     * @return
     */
    public long rpush(String path, Object... vals) {
        return redis.opsForList().rightPushAll(schemadPath(path),vals);
    }


    /**
     * 把路径前面带上schema前缀
     * @param key
     * @return
     */
    private String schemadPath(String key) {
        return key.startsWith("path:") ? key.substring(5) : (StringUtils.isEmpty(schema) ? key : ShenStrings.joinWith(schema ,key, ShenStrings.COLON));
    }

    /**
     * 把路径前面带上publicSchema前缀
     *
     * @param key
     * @return
     */
    public String publicPath(String key) {
        return StringUtils.isEmpty(schema) ? key : ShenStrings.joinWith(publicSchema, key, ShenStrings.COLON);
    }

    /**
     * 将路径标志为绝对路径，不加schema
     *
     * @param key
     * @return
     */
    public static String absolutePath(String key) {
        return ShenStrings.joinWith(ABSOLUTE_PATH, key, ShenStrings.COLON);
    }


    /**
     * 获取一个范围的列表值.
     * @param path 路径
     * @param start 开始位置
     * @param end 数量
     * @return
     */
    public <T> List<T> lrange(String path, int start, int end,Class<T> type) {
        List<Object> list = redis.opsForList().range(schemadPath(path),start,end);
        List<T> results = Lists.newArrayList();
        for(Object row : list) results.add(GsonUtils.parse(ShenStrings.str(row),type));
        return results;

    }

    /**
     * 左删除
     * @param path
     * @param start
     * @param end
     */
    public void ltrim(String path, int start, int end) {
        redis.opsForList().trim(schemadPath(path),start, end);
    }

    /**
     * 针对Hash的获取和设置操作. 首先尝试获取值，如果不存在则设置提供的值
     * @param path
     * @param key
     * @param val
     * @return
     */
    public String hGetSetIfAbsent(String path, String key, String val) {
        path = schemadPath(path);
        String exists = (String) redis.opsForHash().get(path,key);
        if(StringUtils.isEmpty(exists)) {
            redis.opsForHash().put(path,key,val);
            exists = val;
        }
        return exists;
    }

    /**
     * 针对Hash的设置操作.
     *
     * @param path
     * @param key
     * @param val
     * @return
     */
    public void hset(String path, String key, Object val) {
        path = schemadPath(path);
        redis.opsForHash().put(path, key, val);
    }


    /**
     * Hash删除
     * @param path
     * @param keys
     * @return
     */
    public Long hdel(String path, Object... keys) {
        if(keys.length == 0) return 0l;
        return redis.opsForHash().delete(schemadPath(path),keys);
    }

    /**
     * 左插入列表
     * @param path
     * @param vals
     */
    public Long lpush(String path, Object... vals) {
        return redis.opsForList().leftPushAll(schemadPath(path),vals);
    }

    /**
     * 获取bit值
     * @param path
     * @param offset
     * @return
     */
    public Boolean getbit(String path, Long offset) {
        return redis.opsForValue().getBit(schemadPath(path), offset);
    }

    /**
     * rediskey的存活时间
     * @param path
     * @return
     */
    public Long ttl(String path) {
        return redis.getExpire(schemadPath(path));
    }

    /**
     * Hash get
     * @param path
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T hget(String path, String key,Class<T> clazz) {
        String val = ShenStrings.str(redis.opsForHash().get(schemadPath(path),key));
        return GsonUtils.parse(val,clazz);
    }

    /**
     * 针对Hash的获取和设置操作. 首先尝试获取值，如果不存在则设置提供的值
     *
     * @param path
     * @param key
     * @param val
     * @return
     */
    public <T> T hGetSetIfAbsent(String path, String key, T val) {
        path = schemadPath(path);
        String exists = ShenStrings.str(redis.opsForHash().get(path, key));
        if (StringUtils.isEmpty(exists)) {
            System.out.println("hGetSetIfAbsent [" + exists + "]");
            redis.opsForHash().put(path, key, val);
            return val;
        }
        return (T) GsonUtils.parse(exists, val.getClass());
    }

    /**
     * 获取有序集合的成员数
     */
    public Long zcard(String path) {
        return redis.opsForZSet().zCard(schemadPath(path));
    }

    /**
     * key 重命名
     */
    public void rename(String key, String newKey) {
        redis.rename(schemadPath(key), schemadPath(newKey));
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        return redis.hasKey(schemadPath(key));
    }
}
