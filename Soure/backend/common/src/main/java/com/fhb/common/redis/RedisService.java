package com.fhb.common.redis;

import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author lizhaozhong
 * @Date 9/11/20 2:39 PM
 * @Version 1.0
 */
public interface RedisService {

    /**
     * 设置指定key的值
     *
     * @param key
     * @param value
     * @return
     */
    boolean set(String key, String value);

    /**
     * 设置hash元素单个字段的值
     *
     * @param key   key
     * @param field 字段名
     * @param value 值
     * @return 是否添加
     */
    Boolean hSet(String key, String field, String value);

    /**
     * 设置hash元素多个字段的值
     *
     * @param key   key
     * @param values 多个值
     * @return 是否成功
     */
    Boolean hMSet(String key, Map<String, String> values);

    /**
     * hash获取元素值
     *
     * @param key
     * @param field
     * @return
     */
    String hGet(String key, String field);

    /**
     * hash获取所有元素值
     * @param key hashKey
     * @return
     */
    Map<String, String> hGetAll(String key);

    /**
     * hash删除元素值
     *
     * @param key
     * @param field
     * @return
     */
    Long hDel(String key, String field);

    /**
     * 是否存在某个hash元素
     *
     * @param key
     * @param field
     * @return
     */
    Boolean hExist(String key, String field);

    /**
     * 获取list的hash元素
     *
     * @param key
     * @param field
     * @return
     */
    String hMGet(String key, List<String> field);


    /**
     * 当key不存在时设置key的值，带过期时间
     *
     * @param key
     * @param value
     * @param expireTime 过期时间，单位：毫秒
     * @return
     */
    boolean setNx(String key, String value, Long expireTime);

    /**
     * 设置指定key的值，带过期时间
     *
     * @param key
     * @param value
     * @param expireTime 过期时间，单位：毫秒
     * @return
     */
    boolean set(String key, String value, Long expireTime);

    /**
     * 设置byte数据
     * @param key
     * @param data
     * @return
     */
    boolean setByte(String key, byte[] data);

    /**
     * get获取数据
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * 获取byte数据
     * @param key
     * @return
     */
    byte[] getByte(String key);

    /**
     * 设置key的过期时间
     *
     * @param key
     * @param expire 单位：毫秒
     * @return
     */
    boolean expire(String key, long expire);

    /**
     * 设置有效时间
     * @param key
     * @param expire  时间
     * @param timeUnit  单位
     * @return
     */
    boolean expire(String key, long expire, TimeUnit timeUnit);

    /**
     * 移除key的值
     *
     * @param key
     * @return
     */
    boolean remove(String key);

    /**
     * 移除多个key的值
     *
     * @param key 需要移除的key信息
     * @return 删除结果
     */
    boolean remove(String... key);

    /**
     * 批量获取数据
     *
     * @param keys
     * @return
     */
    Map<String, String> mGet(List<String> keys);

    /**
     * zSet全部获取
     *
     * @param key   获取key下的所有内容
     * @param range 范围
     * @return 结果
     */
    Set<String> zGetAll(String key, Long range);

    /**
     * zSet插入
     *
     * @param key   key
     * @param value value
     * @param score score
     * @param range 范围
     */
    void zAdd(String key, String value, Long score, Long range);

    /**
     * zSet插入
     *
     * @param key   key
     * @param value value
     * @param score score
     */
    void zAdd(String key, String value, Long score);

    /**
     * 查分数
     *
     * @param key   主键
     * @param start 起始
     * @param end   结束
     * @return 返回
     */
    Set<ZSetOperations.TypedTuple<String>> zGetWithScore(String key, Long start, Long end);

    /**
     * zSet插入
     *
     * @param key   key
     * @param value value
     * @param score score
     */
    void zIncrement(String key, String value, Long score);

    /**
     * zSet插入返回最终值
     *
     * @param key   key
     * @param value value
     * @param score score
     * @return 结果
     */
    Double zIncrementScore(String key, String value, Double score);

    /**
     * zSet查询分数
     *
     * @param key   key
     * @param value value
     * @return 结果
     */
    Double zGet(String key, String value);

    /**
     * zSet删除分数
     *
     * @param key
     * @param value
     * @return
     */
    Long zRemove(String key, String value);

    /**
     * 删除zSet
     *
     * @param id id
     */
    void zDelete(String id);

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    long incr(String key, long delta);

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    long decr(String key, long delta);

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    long hIncr(String key, String item, long by);

    /**
     * 查询前缀
     *
     * @param prefix 前缀
     * @return keys
     */
    Set<String> getKeysWithPrefix(String prefix);

    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */
    long hDecr(String key, String item, long by);

    /**
     * 获取key剩余过期时间，单位：毫秒
     *
     * @param key
     * @return 剩余过期时间
     */
    long getExpire(String key);

    /**
     * 查询redis key是否存在
     *
     * @param key
     * @return  key是否存在
     */
    boolean exists(String key);

    /**
     * 添加集合
     * @param key
     * @param values
     * @return
     */
    Long addMembers(String key, List<String> values);

    /**
     * 是否存在
     * @param key
     * @param value
     * @return
     */
    boolean isMember(String key, String value);

    /**
     * 查询set的数量
     * @param key
     * @return
     */
    Long memberCount(String key);

    /**
     * 查询key对应所有内容
     * @param key
     * @return
     */
    Set<String> members(String key);

    /**
     * 添加集合元素
     * @param key
     * @param value
     * @return
     */
    Long addMember(String key,String value);

    /**
     * 删除集合元素
     * @param key
     * @param value
     * @return
     */
    Long removeMember(String key,String value);

    /**
     * 删除集合元素
     * @param key
     * @param values
     * @return
     */
    Long removeMembers(String key, Set<String> values);

    /**
     * list全部插入
     *
     * @param key key
     * @param list 值
     */
    void lAddAll(String key, List<String> list);

    /**
     * list读取
     *
     * @param key key
     * @param start 开始index
     * @param end 结束index
     * @return 结果
     */
    List<String> lGet(String key, Integer start, Integer end);

    /**
     * list读取
     *
     * @param key key
     * @return 结果
     */
    String lGet(String key);


    /**
     * 查看redis里面有什么
     *
     * @param key key
     * @param start 开始
     * @param end 结束
     * @return 结果
     */
    List<String> lRange(String key, long start, long end);

    /**
     * list插入
     *
     * @param key key
     * @param value 值
     */
    void lAdd(String key, String value);
}