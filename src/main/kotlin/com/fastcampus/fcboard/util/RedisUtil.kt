package com.fastcampus.fcboard.util

import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisUtil(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    fun setData(key: String, value: Any) {
        try {
            redisTemplate.opsForValue().set(key, value.toString())
        } catch (ex: RedisConnectionFailureException) {
            // Redis is unavailable; skip caching to avoid breaking core logic during tests or degraded runtime
        }
    }

    fun getData(key: String): Any? {
        return try {
            redisTemplate.opsForValue().get(key)
        } catch (ex: RedisConnectionFailureException) {
            null
        }
    }

    fun increment(key: String) {
        try {
            redisTemplate.opsForValue().increment(key)
        } catch (ex: RedisConnectionFailureException) {
            // Ignore when Redis is down
        }
    }

    fun getCount(key: String): Long? {
        return try {
            redisTemplate.opsForValue().get(key)?.toString()?.toLong()
        } catch (ex: RedisConnectionFailureException) {
            null
        }
    }

    fun getLikeCountKey(postId: Long): String {
        return "like:$postId"
    }
}
