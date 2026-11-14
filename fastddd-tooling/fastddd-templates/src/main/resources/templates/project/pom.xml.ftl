<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>${groupId}</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>${version}</version>
    <packaging>jar</packaging>

    <name>${artifactId}</name>
    <description>${projectDescription!"FastDDD Application - " + modelTypeDisplayName + " Model"}</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>${springBootVersion}</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>${javaVersion}</java.version>
        <!-- ${msg.proj.pom.versionAxesNote1}
             ${msg.proj.pom.versionAxesNote2} -->
        <fastddd.version>${fastdddVersion}</fastddd.version>
        <fastddd-tooling.version>${fastdddToolingVersion}</fastddd-tooling.version>
        <!-- ${msg.proj.pom.tomcatSecurityNote} -->
        <!-- ${msg.proj.pom.tomcatImportNote1}
             ${msg.proj.pom.tomcatImportNote2} -->
        <tomcat.version>10.1.59</tomcat.version>
        <maven.compiler.release>${javaVersion}</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <springdoc.version>2.8.17</springdoc.version>
        <knife4j.version>4.5.0</knife4j.version>
<#-- richDependencies：逐表通道可能产出 RICH 产物（分析器/AI/手动覆盖逐表判定）时由生成通道注入，
     与项目级 modelType 无关——RICH 产物（聚合根/EntityMapper/RepositoryImpl/CQRS 四件套）编译需要
     fast-ddd-application 与 MapStruct（含注解处理器），仅凭 modelType=="RICH" 门控会漏配（D-2c）。
     默认 false 兼容旧上下文（不传该变量的调用方维持原 modelType 单门控行为）。 -->
<#if (richDependencies!false) || modelType == "RICH">
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
</#if>
<#if enableEvent?? && enableEvent>
        <rocketmq.version>5.1.4</rocketmq.version>
</#if>
<#if (connectionPool!"HIKARI") == "DRUID">
        <!-- ${msg.proj.pom.druidVersionNote} -->
        <druid.version>1.2.28</druid.version>
</#if>
    </properties>

    <dependencies>
        <!-- ${msg.proj.pom.depModelNote} -->
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-model</artifactId>
            <version>${fastdddVersion}</version>
        </dependency>

        <!-- ${msg.proj.pom.depWebNote} -->
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-web</artifactId>
            <version>${fastdddVersion}</version>
        </dependency>

        <!-- ${msg.proj.pom.depCrudNote} -->
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-crud</artifactId>
            <version>${fastdddVersion}</version>
        </dependency>

<#if (richDependencies!false) || modelType == "RICH">
        <!-- ${msg.proj.pom.depApplicationNote} -->
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-application</artifactId>
            <version>${fastdddVersion}</version>
        </dependency>

        <!-- ${msg.proj.pom.depMapstructNote} -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${r"${mapstruct.version}"}</version>
        </dependency>
</#if>

<#if modules??>
<#list modules as module>
<#if module == "cache">
        <!-- FastDDD Cache -->
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-cache</artifactId>
            <version>${fastdddVersion}</version>
        </dependency>
<#elseif module == "event">
        <!-- FastDDD Event -->
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-event</artifactId>
            <version>${fastdddVersion}</version>
        </dependency>
<#elseif module == "search">
        <!-- FastDDD Search -->
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-search</artifactId>
            <version>${fastdddVersion}</version>
        </dependency>
<#elseif module == "monitor">
        <!-- FastDDD Monitor -->
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-monitor</artifactId>
            <version>${fastdddVersion}</version>
        </dependency>
<#elseif module == "security">
        <!-- FastDDD Security (JWT + CORS + AuditLog) -->
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-security-starter</artifactId>
            <version>${fastdddVersion}</version>
        </dependency>
</#if>
</#list>
</#if>

        <!-- Database Driver -->
<#if databaseType == "MYSQL">
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
<#elseif databaseType == "POSTGRESQL">
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
<#else>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
</#if>

<#if databaseType?? && databaseType != "NONE">
<#if (connectionPool!"HIKARI") == "DRUID">
        <!-- ${msg.proj.pom.druidPoolNote1}
             ${msg.proj.pom.druidPoolNote2} -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-3-starter</artifactId>
            <version>${r"${druid.version}"}</version>
        </dependency>
<#else>
        <!-- ${msg.proj.pom.hikariPoolNote1}
             ${msg.proj.pom.hikariPoolNote2}
             ${msg.proj.pom.hikariPoolNote3} -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
</#if>
</#if>

        <!-- ${msg.proj.pom.depLombokNote} -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- ${msg.proj.pom.depSpringdocNote} -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${r"${springdoc.version}"}</version>
        </dependency>

        <!-- ${msg.proj.pom.depKnife4jNote} -->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
            <version>${r"${knife4j.version}"}</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>${javaVersion}</release>
                    <encoding>UTF-8</encoding>
<#if (richDependencies!false) || modelType == "RICH">
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${r"${mapstruct.version}"}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <!-- ${msg.proj.pom.processorPathNote1}
                                 ${msg.proj.pom.processorPathNote2} -->
                            <version>${r"${lombok.version}"}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>${r"${lombok-mapstruct-binding.version}"}</version>
                        </path>
                    </annotationProcessorPaths>
</#if>
                </configuration>
            </plugin>

            <!-- ${msg.proj.pom.mavenPluginNote} -->
            <plugin>
                <groupId>io.github.gallop.fastddd</groupId>
                <artifactId>fastddd-maven-plugin</artifactId>
                <version>${r"${fastddd-tooling.version}"}</version>
                <configuration>
                    <basePackage>${packageName}</basePackage>
<#if modelType == "RICH">
                    <modelType>RICH</modelType>
                    <enableCQRS>true</enableCQRS>
</#if>
                </configuration>
            </plugin>

            <#-- 可执行打包（D-3 批，2026-09-13）：生成 Dockerfile 以 java -jar 运行 target/*.jar、
                 README 提供 mvn spring-boot:run——此前 pom 未声明本插件，package 产物为无 Main-Class
                 的平装 jar，两处文档承诺均无法兑现。repackage 产出可执行 fat jar（原平装 jar 保留为
                 *.jar.original）。版本不声明：继承 parent（spring-boot-starter-parent）pluginManagement，
                 不引入独立第三方版本。 -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>

        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
                <includes>
                    <include>application.yml</include>
                    <include>application-*.yml</include>
                </includes>
            </resource>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>false</filtering>
                <excludes>
                    <exclude>application.yml</exclude>
                    <exclude>application-*.yml</exclude>
                </excludes>
            </resource>
        </resources>
    </build>

    <profiles>
        <!-- ${msg.proj.pom.profileDevNote} -->
        <profile>
            <id>dev</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <env>dev</env>
            </properties>
        </profile>

        <!-- ${msg.proj.pom.profileTestNote} -->
        <profile>
            <id>test</id>
            <properties>
                <env>test</env>
            </properties>
        </profile>

        <!-- ${msg.proj.pom.profileProdNote} -->
        <profile>
            <id>prod</id>
            <properties>
                <env>prod</env>
            </properties>
        </profile>
    </profiles>
</project>
