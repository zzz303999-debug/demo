package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 配置 — 启用审计（@CreatedDate / @LastModifiedDate 自动填充）。
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
