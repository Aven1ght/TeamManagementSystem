package ua.rud.teammanagementsystem.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class CacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final Logger log = LoggerFactory.getLogger(CacheService.class);
    public <T> T get(String key, Class<T> type){
        Object value = redisTemplate.opsForValue().get(key);
        if(value == null){
            return null;
        }
        return type.cast(value);
    }

    public void delete(String key){
        redisTemplate.delete(key);
    }

    public <T> void set(String key, T value, long ttl){
        redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(ttl));
    }

    public boolean exists(String key){
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
