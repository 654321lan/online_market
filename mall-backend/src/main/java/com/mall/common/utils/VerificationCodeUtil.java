package com.mall.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class VerificationCodeUtil {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${verification.code.expire-minutes}")
    private long expireMinutes;

    @Value("${verification.code.max-attempts}")
    private int maxAttempts;

    @Value("${verification.code.lock-minutes}")
    private long lockMinutes;

    @Value("${verification.code.send-interval}")
    private long sendInterval;

    private static final String CODE_PREFIX = "verify_code:";
    private static final String ATTEMPT_PREFIX = "verify_attempt:";
    private static final String LOCK_PREFIX = "verify_lock:";
    private static final String SEND_INTERVAL_PREFIX = "send_interval:";

    public boolean sendCode(String identifier, String code) {
        String lockKey = LOCK_PREFIX + identifier;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            return false;
        }

        String intervalKey = SEND_INTERVAL_PREFIX + identifier;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(intervalKey))) {
            return false;
        }

        String codeKey = CODE_PREFIX + identifier;
        redisTemplate.opsForValue().set(codeKey, code, expireMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(intervalKey, "1", sendInterval, TimeUnit.SECONDS);
        
        return true;
    }

    public boolean verifyCode(String identifier, String inputCode) {
        String lockKey = LOCK_PREFIX + identifier;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            return false;
        }

        String codeKey = CODE_PREFIX + identifier;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            return false;
        }

        if (storedCode.equals(inputCode)) {
            redisTemplate.delete(codeKey);
            resetAttempts(identifier);
            return true;
        } else {
            incrementAttempts(identifier);
            return false;
        }
    }

    private void incrementAttempts(String identifier) {
        String attemptKey = ATTEMPT_PREFIX + identifier;
        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        int attempts = attemptsStr == null ? 1 : Integer.parseInt(attemptsStr) + 1;
        
        redisTemplate.opsForValue().set(attemptKey, String.valueOf(attempts), lockMinutes, TimeUnit.MINUTES);

        if (attempts >= maxAttempts) {
            String lockKey = LOCK_PREFIX + identifier;
            redisTemplate.opsForValue().set(lockKey, "1", lockMinutes, TimeUnit.MINUTES);
        }
    }

    private void resetAttempts(String identifier) {
        String attemptKey = ATTEMPT_PREFIX + identifier;
        redisTemplate.delete(attemptKey);
    }

    public boolean isLocked(String identifier) {
        String lockKey = LOCK_PREFIX + identifier;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    public long getRemainingLockTime(String identifier) {
        String lockKey = LOCK_PREFIX + identifier;
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
