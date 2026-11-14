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

    <artifactId>common-shared</artifactId>

    <name>common-shared</name>
    <description>Cross-domain utilities, base classes, and shared infrastructure</description>

    <dependencies>
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-common</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fast-ddd-model</artifactId>
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
