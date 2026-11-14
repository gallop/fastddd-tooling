package com.gallop.fastddd.codegen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据库连接配置
 *
 * @author gallop
 * @date 2023-10-10
 */
@Data
@ConfigurationProperties(prefix = "fastddd.codegen.database")
public class DatabaseConfig {

    /**
     * 数据库URL
     */
    private String url;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 数据库驱动类名
     */
    private String driverClassName = "com.mysql.cj.jdbc.Driver";

    /**
     * 数据库目录名（对于 MySQL 是数据库名）
     */
    private String catalog;

    /**
     * 数据库模式名
     */
    private String schema;

    /**
     * 表名前缀，生成代码时会去除
     */
    private String tablePrefix;
}