<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>${groupId}</groupId>
        <artifactId>${artifactId}-common</artifactId>
        <version>${version}</version>
    </parent>

    <artifactId>common-${domainName}</artifactId>

    <name>common-${domainName}</name>
    <description>${domainName?cap_first} domain module</description>

    <dependencies>
        <dependency>
            <groupId>${groupId}</groupId>
            <artifactId>common-shared</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-crud</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
