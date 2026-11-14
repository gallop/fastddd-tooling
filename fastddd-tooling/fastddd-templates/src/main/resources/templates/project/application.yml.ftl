server:
  port: 8080

spring:
  application:
    name: ${artifactId}
  profiles:
    active: dev

<#if datasourceUrl?? && datasourceUrl != "">
  datasource:
<#if (connectionPool!"HIKARI") == "DRUID">
    type: com.alibaba.druid.pool.DruidDataSource
<#else>
    type: com.zaxxer.hikari.HikariDataSource
</#if>
    driver-class-name: ${databaseDriver}
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
    driver-class-name: ${databaseDriver}
    url: jdbc:mysql://localhost:3306/${databaseName}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
<#elseif databaseType == "POSTGRESQL">
  datasource:
<#if (connectionPool!"HIKARI") == "DRUID">
    type: com.alibaba.druid.pool.DruidDataSource
<#else>
    type: com.zaxxer.hikari.HikariDataSource
</#if>
    driver-class-name: ${databaseDriver}
    url: jdbc:postgresql://localhost:5432/${databaseName}
    username: postgres
    password: your_password
<#else>
  datasource:
    driver-class-name: ${databaseDriver}
    url: jdbc:h2:mem:testdb
    username: sa
    password:
    h2:
      console:
        enabled: true
</#if>

# ${msg.proj.appYml.mybatisPlusNote}
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  mapper-locations: classpath*:mapper/**/*.xml
  global-config:
    db-config:
      # ${msg.proj.appYml.idTypeNote}
      # ${msg.proj.appYml.idTypeOverrideNote}
      id-type: ${r"${mybatis-plus.id-type:" + (idStrategy!"ASSIGN_ID")?lower_case + "}"}
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# ${msg.proj.appYml.springdocNote}
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui.html

# ${msg.proj.appYml.fastdddNote}
fastddd:
<#if modules??>
<#if modules?seq_contains("security")>
  security:
    jwt:
      enabled: true
      secret: ${r"${JWT_SECRET}"}
      expiration: 24h
    cors:
      enabled: true
      allowed-origins:
        - http://localhost:3000
        - http://localhost:5173
      allowed-methods:
        - GET
        - POST
        - PUT
        - DELETE
      max-age: 3600s
</#if>
<#if modules?seq_contains("cache")>
  cache:
    local:
      enabled: true
      max-size: 1000
      expire-after-write: 30m
    redis:
      enabled: false
</#if>
<#-- 事件配置 fastddd.event.* 暂不生成：框架未提供 fastddd.event.* 绑定（事件模块仅绑定 rocketmq.*）。
     待 EVT（ADR-003 OutboxTransport SPI）决定补真实绑定后再恢复。 -->
<#if modules?seq_contains("monitor")>
  monitor:
    sql:
      <#-- 框架契约：SqlMonitoringProperties.slowQueryThreshold 为 long（毫秒），不接 Spring Duration 单位后缀 -->
      slow-query-threshold: 1000
</#if>
<#if modules?seq_contains("search")>
  search:
    enabled: true
    backend: sql
    sql:
      full-text-enabled: auto
    # ${msg.proj.appYml.searchBackendNote}
    # meilisearch:
    #   host: http://localhost:7700
    #   api-key: your-master-key
    #   timeout: 5s
    #   index-prefix: "fastddd_"
</#if>
</#if>

# ${msg.proj.appYml.loggingNote}
logging:
  level:
    com.gallop.fastddd: DEBUG
    ${packageName}: DEBUG
