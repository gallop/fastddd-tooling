server:
  port: ${subApp.port}

spring:
  application:
    name: ${subApp.moduleName}
  profiles:
    active: dev
<#if datasourceUrl?? && datasourceUrl != "">
  datasource:
<#if (connectionPool!"HIKARI") == "DRUID">
    type: com.alibaba.druid.pool.DruidDataSource
<#else>
    type: com.zaxxer.hikari.HikariDataSource
</#if>
    driver-class-name: ${databaseDriver!"com.mysql.cj.jdbc.Driver"}
    url: ${datasourceUrl}
    username: ${datasourceUsername!"root"}
    password: ${datasourcePassword!""}
<#elseif databaseType == "MYSQL">
  datasource:
<#if (connectionPool!"HIKARI") == "DRUID">
    type: com.alibaba.druid.pool.DruidDataSource
<#else>
    type: com.zaxxer.hikari.HikariDataSource
</#if>
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/${databaseName!"mydb"}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
<#elseif databaseType == "POSTGRESQL">
  datasource:
<#if (connectionPool!"HIKARI") == "DRUID">
    type: com.alibaba.druid.pool.DruidDataSource
<#else>
    type: com.zaxxer.hikari.HikariDataSource
</#if>
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/${databaseName!"mydb"}
    username: postgres
    password: your_password
<#else>
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password:
    h2:
      console:
        enabled: true
</#if>

# MyBatis Plus
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: ${basePackage}.common
  global-config:
    db-config:
      id-type: ${r"${mybatis-plus.id-type:" + (idStrategy!"ASSIGN_ID")?lower_case + "}"}
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# OpenAPI
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui.html

# FastDDD Security
fastddd:
  security:
    jwt:
      enabled: true
      channel: ${subApp.channel}
      # Required at deployment time; generated source never contains a JWT fallback secret.
      secret: ${r"${JWT_SECRET}"}
      header-name: Authorization
      expiration: ${r"${JWT_EXPIRATION:8h}"}
    http:
      permit-all-paths:
        - /api/auth/**
        - /actuator/health
    cors:
      enabled: true
      allowed-origins:
        - ${r"${CORS_ORIGINS:"}${subApp.corsOrigins}${r"}"}
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
        - OPTIONS
      allowed-headers: "*"
      max-age: 3600s
<#if subApp.wechatMiniAppId?? && subApp.wechatMiniAppId != "">
    wechat-mini:
      app-id: ${r"${WECHAT_APP_ID:"}${subApp.wechatMiniAppId}${r"}"}
      secret: ${r"${WECHAT_SECRET:"}${subApp.wechatMiniSecret!""}${r"}"}
</#if>

# Logging
logging:
  level:
    com.gallop.fastddd: DEBUG
    ${subApp.packageName}: DEBUG
