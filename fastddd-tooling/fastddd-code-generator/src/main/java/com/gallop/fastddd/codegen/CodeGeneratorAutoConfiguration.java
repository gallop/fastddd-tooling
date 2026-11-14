package com.gallop.fastddd.codegen;

import com.gallop.fastddd.codegen.config.DatabaseConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 代码生成器自动配置类
 *
 * @author gallop
 * @date 2023-10-10
 */
@Configuration
@ComponentScan("com.gallop.fastddd.codegen")
@EnableConfigurationProperties(DatabaseConfig.class)
public class CodeGeneratorAutoConfiguration {
}