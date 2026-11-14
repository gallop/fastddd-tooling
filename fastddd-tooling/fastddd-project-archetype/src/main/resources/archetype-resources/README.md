# ${artifactId}

FastDDD Application - ${modelType} Model

## 项目信息

- **GroupId**: ${groupId}
- **ArtifactId**: ${artifactId}
- **Package**: ${package}
- **Model Type**: ${modelType}

## 快速开始

### 编译项目

\`\`\`bash
mvn clean compile
\`\`\`

### 运行项目

\`\`\`bash
mvn spring-boot:run
\`\`\`

### 生成代码

\`\`\`bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:${fastddd-tooling.version}:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/your_db \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=${package}
\`\`\`

## 技术栈

- Spring Boot ${springBootVersion}
- FastDDD ${fastddd.version}
- FastDDD Tooling ${fastddd-tooling.version}
- Java 21
#if(${databaseType} == "mysql")
- MySQL
#elseif(${databaseType} == "postgresql")
- PostgreSQL
#else
- H2 Database
#end

## 文档

- FastDDD 使用指南：参见所用 Framework Community 版本的发布说明
- [包结构指南](./docs/PACKAGE_GUIDE.md)

## License

Copyright © 2025
