package com.fhb.common.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author lizhaozhong
 * @Date 9/10/20 9:39 AM
 * @Version 1.0
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.password:#{null}}")
    private String redisClusterPassword;
    @Value("${spring.redis.cluster.nodes}")
    private String redisClusterNodes;
    @Value("${spring.redis.cluster.max-redirects}")
    private int redisClusterMaxRedirects;
    @Value("${spring.redis-internal.password:}")
    private String redisInternalPassword;
    @Value("${spring.redis-internal.host:}")
    private String redisInternalHost;
    @Value("${spring.redis-internal.port:6379}")
    private int redisInternalPort;

    /**
     * 连接池配置信息
     */

    @Bean
    @ConfigurationProperties(prefix = "spring.redis.pool")
    public JedisPoolConfig jedisPoolConfig() {
        return new JedisPoolConfig();
    }

    @Bean
    public RedisClusterConfiguration getJedisCluster() {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        redisClusterConfiguration.setPassword(redisClusterPassword);
        redisClusterConfiguration.setMaxRedirects(redisClusterMaxRedirects);
        List<RedisNode> nodeList = new ArrayList<RedisNode>();
        String[] nodes = redisClusterNodes.split(",");
        for (String node : nodes) {
            String[] hp = node.split(":");
            nodeList.add(new RedisNode(hp[0], Integer.parseInt(hp[1])));
        }
        redisClusterConfiguration.setClusterNodes(nodeList);
        return redisClusterConfiguration;
    }

    @Bean
    public RedisStandaloneConfiguration getJedisInternal() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisInternalHost);
        configuration.setPort(redisInternalPort);
        configuration.setPassword(redisInternalPassword);
        return configuration;
    }

    /**
     * Jedis 连接
     *
     * @param jedisPoolConfig
     * @return
     */
    @Bean
    @Primary
    public JedisConnectionFactory getJedisConnectionFactory(RedisClusterConfiguration redisClusterConfiguration, JedisPoolConfig jedisPoolConfig) {
        return new JedisConnectionFactory(redisClusterConfiguration, jedisPoolConfig);
    }

    @Bean(name = "internalConnectionFactory")
    public JedisConnectionFactory getJedisInternalConnectionFactory(RedisStandaloneConfiguration redisStandaloneConfiguration) {
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    /**
     * 缓存管理器
     *
     * @param connectionFactory
     * @return
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean(name = "internal")
    public RedisTemplate<String, Object> redisInternalTemplate(@Qualifier("internalConnectionFactory") JedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}

