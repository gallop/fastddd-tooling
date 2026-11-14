package com.gallop.fastddd.doc.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * FastDoc自动配置类
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Configuration
@ComponentScan(basePackages = "com.gallop.fastddd.doc")
@EnableConfigurationProperties(DocGeneratorProperties.class)
public class FastDocAutoConfiguration {
}
