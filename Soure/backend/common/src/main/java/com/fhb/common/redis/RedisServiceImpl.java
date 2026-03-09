package com.fhb.common.redis;

import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author lizhaozhong
 * @Date 9/11/20 2:40 PM
 * @Version 1.0
 */
@Service("redisService")
public class RedisServiceImpl implements RedisService {

    @Value("${spring.redis.enable:true}")
    private Boolean redisEnable;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean set(final String key, final String value) {
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            connection.set(serializer.serialize(key), serializer.serialize(value));
            return true;
        });
    }

    @Override
    public Boolean hSet(String key, String field, String value) {
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            connection.hSet(serializer.serialize(key), serializer.serialize(field), serializer.serialize(value));
            return true;
        });
    }

    @Override
    public Boolean hMSet(String key, Map<String, String> values) {
        if (StringUtils.isBlank(key) || ObjectUtils.isEmpty(values)) {
            return false;
        }
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Map<byte[], byte[]> serializeMap = new HashMap<>();
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    serializeMap.put(serializer.serialize(entry.getKey()), serializer.serialize(entry.getValue()));
                }
                connection.hMSet(serializer.serialize(key), serializeMap);
                return true;
            }
        });
    }

    @Override
    public String hGet(String key, String field) {
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] value = connection.hGet(serializer.serialize(key), serializer.serialize(field));
                return serializer.deserialize(value);
            }
        });
    }

    @Override
    public Map<String, String> hGetAll(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return redisTemplate.execute(new RedisCallback<Map<String, String>>() {
            @Override
            public Map<String, String> doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                Map<byte[], byte[]> map = connection.hGetAll(serializer.serialize(key));
                if (ObjectUtils.isEmpty(map)) {
                    return Collections.emptyMap();
                }
                Map<String, String> resultMap = new HashMap<>();
                for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
                    resultMap.put(serializer.deserialize(entry.getKey()), serializer.deserialize(entry.getValue()));
                }
                return resultMap;
            }
        });
    }

    @Override
    public Long hDel(String key, String field) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                return connection.hDel(serializer.serialize(key), serializer.serialize(field));
            }
        });
    }

    @Override
    public void lAddAll(String key, List<String> list) {
        redisTemplate.opsForList().leftPushAll(key, list);
    }

    @Override
    public List<String> lGet(String key, Integer start, Integer end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    @Override
    public String lGet(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    @Override
    public List<String> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    @Override
    public void lAdd(String key, String value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public Boolean hExist(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    @Override
    public String hMGet(String key, List<String> field) {
        byte[][] fields = new byte[field.size()][];
        RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
        for (int i = 0; i < field.size(); i++) {
            fields[i] = serializer.serialize(field.get(i));
        }
        return redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                List<byte[]> value = connection.hMGet(serializer.serialize(key),
                        fields);
                StringBuilder sb = new StringBuilder();
                for (byte[] b : value) {
                    if (sb.length() != 0) {
                        sb.append("@@");
                    }
                    sb.append(serializer.deserialize(b));
                }
                return sb.toString();
            }
        });
    }

    @Override
    public boolean setNx(String key, String value, Long expireTime) {
        if (!redisEnable) {
            return true;
        }
        boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            return connection.set(serializer.serialize(key), serializer.serialize(value), Expiration.from(expireTime, TimeUnit.MILLISECONDS),
                    RedisStringCommands.SetOption.SET_IF_ABSENT);
        });
        return result;
    }

    @Override
    public boolean set(final String key, final String value, Long expireTime) {
        boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            connection.set(serializer.serialize(key), serializer.serialize(value), Expiration.from(expireTime, TimeUnit.MILLISECONDS),
                    RedisStringCommands.SetOption.UPSERT);
            return true;
        });
        return result;
    }

    @Override
    public boolean setByte(String key, byte[] data) {
        if (StringUtils.isBlank(key) || data == null) {
            return false;
        }
        boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            connection.set(key.getBytes(), data);
            return true;
        });
        return result;
    }

    @Override
    public String get(final String key) {
        if (!redisEnable) {
            return "";
        }
        String result = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] value = connection.get(serializer.serialize(key));
                return serializer.deserialize(value);
            }
        });
        return result;
    }

    @Override
    public byte[] getByte(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        byte[] result = redisTemplate.execute((RedisConnection connection) -> {
            return connection.get(key.getBytes());
        });
        return result;
    }

    @Override
    public Map<String, String> mGet(List<String> keys) {
        Map<String, String> result = new HashMap<>();
        if (!redisEnable) {
            return result;
        }
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null) {
            return result;
        }
        int length = values.size();
        for (int i = 0; i < length; i++) {
            String key = keys.get(i);
            String value = values.get(i);
            if (StringUtils.isNotEmpty(value)) {
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public Set<String> zGetAll(String key, Long range) {
        return redisTemplate.opsForZSet().reverseRange(key, 0, range);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<String>> zGetWithScore(String key, Long start, Long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    @Override
    public void zAdd(String key, String value, Long score, Long range) {

        // 拿到存入Redis里数据的唯一分值
        Double s = redisTemplate.opsForZSet().score(key, value);
        //检索是否有旧记录  1.无则插入记录值  2.有则删除 再次插入
        if (!ObjectUtils.isEmpty(s)) {
            //删除旧的
            redisTemplate.opsForZSet().remove(key, value);
        }
        //加入新的记录，设置当前时间戳为分数score
        redisTemplate.opsForZSet().add(key, value, score);
        //获取总记录数
        Long aLong = redisTemplate.opsForZSet().zCard(key);
        if (!ObjectUtils.isEmpty(aLong) && aLong > range) {
            redisTemplate.opsForZSet().removeRange(key, 0, aLong - range - 1);
        }
    }

    @Override
    public void zAdd(String key, String value, Long score) {

        // 拿到存入Redis里数据的唯一分值
        Double s = redisTemplate.opsForZSet().score(key, value);
        //检索是否有旧记录  1.无则插入记录值  2.有则删除 再次插入
        if (!ObjectUtils.isEmpty(s)) {
            //删除旧的
            redisTemplate.opsForZSet().remove(key, value);
        }
        //加入新的记录，设置当前时间戳为分数score
        redisTemplate.opsForZSet().add(key, value, score);
    }

    @Override
    public void zIncrement(String key, String value, Long score) {
        redisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    @Override
    public Double zIncrementScore(String key, String value, Double score) {
        return redisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    @Override
    public Double zGet(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    @Override
    public Long zRemove(String key, String value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }

    @Override
    public void zDelete(String key) {
        Long aLong = redisTemplate.opsForZSet().zCard(key);
        if (ObjectUtils.isEmpty(aLong)) {
            return;
        }
        redisTemplate.opsForZSet().removeRange(key, 0, aLong - 1);
    }

    @Override
    public boolean expire(final String key, long expire) {
        return redisTemplate.expire(key, expire, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean expire(String key, long expire, TimeUnit timeUnit) {
        return redisTemplate.expire(key, expire, timeUnit);
    }

    @Override
    public boolean remove(final String key) {
        boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            connection.del(key.getBytes());
            return true;
        });
        return result;
    }

    @Override
    public boolean remove(String... key) {
        Long delete = redisTemplate.delete(CollUtil.toList(key));
        return null != delete && key.length == delete;
    }

    @Override
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    @Override
    public long hIncr(String key, String item, long by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    @Override
    public Set<String> getKeysWithPrefix(String prefix) {
        return redisTemplate.keys(prefix + "*");
    }

    @Override
    public long hDecr(String key, String item, long by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    @Override
    public long getExpire(String key) {
        if (StringUtils.isBlank(key)) {
            return -1;
        }
        return redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public Long addMembers(String key, List<String> values) {
        if (CollUtil.isEmpty(values)) {
            return 0L;
        }
        return this.redisTemplate.opsForSet().add(key, values.toArray(new String[0]));
    }

    @Override
    public boolean isMember(String key, String value) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return this.redisTemplate.opsForSet().isMember(key, value);
    }

    @Override
    public Long memberCount(String key) {
        if (StringUtils.isBlank(key)) {
            return 0L;
        }
        return this.redisTemplate.opsForSet().size(key);
    }

    @Override
    public Set<String> members(String key) {
        return this.redisTemplate.opsForSet().members(key);
    }

    @Override
    public Long addMember(String key, String value) {
        return this.redisTemplate.opsForSet().add(key, value);
    }

    @Override
    public Long removeMember(String key, String value) {
        return this.redisTemplate.opsForSet().remove(key, value);
    }

    @Override
    public Long removeMembers(String key, Set<String> values) {
        if (CollUtil.isEmpty(values)) {
            return 0L;
        }
        return this.redisTemplate.opsForSet().remove(key, values.toArray(new String[0]));
    }
}
