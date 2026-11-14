server:
  port: 8080

spring:
  application:
    name: ${artifactId}

<#if (connectionPool!"HIKARI") == "HIKARI">
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1800000
<#elseif (connectionPool!"HIKARI") == "DRUID">
  datasource:
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
</#if>

# ${msg.proj.appProdYml.loggingNote}
logging:
  level:
    com.gallop.fastddd: INFO
    ${packageName}: INFO
    org.springframework.web: WARN
  file:
    name: logs/${artifactId}.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30

# ${msg.proj.appProdYml.springdocNote}
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
