version: '3.8'
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./docker/nginx/default.conf:/etc/nginx/conf.d/default.conf
    depends_on:
<#list subApps as app>
      - ${app.moduleName}
</#list>
    restart: unless-stopped

<#list subApps as app>
  ${app.moduleName}:
    build:
      context: .
      dockerfile: ${app.moduleName}/Dockerfile
    ports:
      - "${app.port}"
    environment:
      JWT_SECRET: ${r"${"}${app.channel}_JWT_SECRET:?${app.channel}_JWT_SECRET must be configured${r"}"}
<#if databaseType == "MYSQL">
      SPRING_DATASOURCE_URL: ${r"${"}DATASOURCE_URL:-jdbc:mysql://mysql:3306/${databaseName!"mydb"}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai${r"}"}
      SPRING_DATASOURCE_USERNAME: ${r"${"}MYSQL_USERNAME:-root${r"}"}
      SPRING_DATASOURCE_PASSWORD: ${r"${"}MYSQL_PASSWORD:?MYSQL_PASSWORD must be configured${r"}"}
<#elseif databaseType == "POSTGRESQL">
      SPRING_DATASOURCE_URL: ${r"${"}DATASOURCE_URL:-jdbc:postgresql://postgres:5432/${databaseName!"mydb"}?stringtype=unspecified${r"}"}
      SPRING_DATASOURCE_USERNAME: ${r"${"}POSTGRES_USERNAME:-postgres${r"}"}
      SPRING_DATASOURCE_PASSWORD: ${r"${"}POSTGRES_PASSWORD:?POSTGRES_PASSWORD must be configured${r"}"}
<#else>
      SPRING_DATASOURCE_URL: ${r"${"}DATASOURCE_URL:-jdbc:h2:mem:${databaseName!"mydb"}${r"}"}
      SPRING_DATASOURCE_USERNAME: ${r"${"}DB_USERNAME:-sa${r"}"}
      SPRING_DATASOURCE_PASSWORD: ${r"${"}DB_PASSWORD:${r"}"}
</#if>
    depends_on:
<#if databaseType == "MYSQL">
      - mysql
<#elseif databaseType == "POSTGRESQL">
      - postgres
</#if>
    restart: unless-stopped

</#list>
<#if databaseType == "MYSQL">
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${r"${"}MYSQL_PASSWORD:?MYSQL_PASSWORD must be configured${r"}"}
      MYSQL_DATABASE: ${databaseName!"mydb"}
    volumes:
      - mysql_data:/var/lib/mysql
    restart: unless-stopped

volumes:
  mysql_data:
<#elseif databaseType == "POSTGRESQL">
  postgres:
    image: postgres:16
    ports:
      - "5432:5432"
    environment:
      POSTGRES_PASSWORD: ${r"${"}POSTGRES_PASSWORD:?POSTGRES_PASSWORD must be configured${r"}"}
      POSTGRES_DB: ${databaseName!"mydb"}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
</#if>
