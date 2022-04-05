package com.example.demo;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.session.RedisSessionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.RedisSessionRepository;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RedisSessionProperties.class)
@EnableSpringHttpSession
public class DemoSessionConfig {

    private final Duration sessionTimeout;

    private final RedisSessionProperties redisSessionProperties;

    private final RedisConnectionFactory redisConnectionFactory;

    public DemoSessionConfig(@Value("${spring.session.timeout:30m}") Duration sessionTimeout, RedisSessionProperties redisSessionProperties,
                             ObjectProvider<RedisConnectionFactory> redisConnectionFactory) {
        this.sessionTimeout = sessionTimeout;
        this.redisSessionProperties = redisSessionProperties;
        this.redisConnectionFactory = redisConnectionFactory.getObject();
    }

    @Bean(name = "sessionRedisOperations")
    public RedisOperations<String, Object> sessionRedisOperations() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean(name = "sessionRepository")
    public RedisSessionRepository sessionRepository(RedisOperations<String, Object> sessionRedisOperations) {
        RedisSessionRepository sessionRepository = new RedisSessionRepository(sessionRedisOperations);
        sessionRepository.setDefaultMaxInactiveInterval(sessionTimeout);
        sessionRepository.setRedisKeyNamespace(this.redisSessionProperties.getNamespace());
        sessionRepository.setFlushMode(this.redisSessionProperties.getFlushMode());
        sessionRepository.setSaveMode(this.redisSessionProperties.getSaveMode());
        return sessionRepository;
    }
}
