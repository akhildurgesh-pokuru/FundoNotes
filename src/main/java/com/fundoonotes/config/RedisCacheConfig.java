package com.fundoonotes.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching // UC11: Activates @Cacheable and @CacheEvict.
public class RedisCacheConfig {
}
