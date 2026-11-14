server:
  port: 8080

spring:
  application:
    name: ${artifactId}

<#if databaseType == "MYSQL">
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/${artifactId}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
<#elseif databaseType == "POSTGRESQL">
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/${artifactId}
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

# ${msg.proj.appDevYml.loggingNote}
logging:
  level:
    com.gallop.fastddd: DEBUG
    ${packageName}: DEBUG
    org.springframework.web: DEBUG
    org.mybatis: DEBUG

# ${msg.proj.appDevYml.springdocNote}
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
