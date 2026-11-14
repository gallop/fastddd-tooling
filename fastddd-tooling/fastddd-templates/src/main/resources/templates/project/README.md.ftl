# ${artifactId}

FastDDD Application - ${modelType} Model

## ${msg.proj.readme.projectInfo}

- **GroupId**: ${groupId}
- **ArtifactId**: ${artifactId}
- **Version**: ${version}
- **Package**: ${packageName}
- **Model Type**: ${modelType}
- **Database**: ${databaseDisplayName}
- **Java Version**: ${javaVersion}
- **FastDDD Version**: ${fastdddVersion}
- **FastDDD Tooling Version**: ${fastdddToolingVersion}

## ${msg.proj.readme.quickStart}

### ${msg.proj.readme.compile}

```bash
mvn clean compile
```

### ${msg.proj.readme.run}

```bash
mvn spring-boot:run
```

### ${msg.proj.readme.access}

${msg.proj.readme.accessHint}`http://localhost:8080`

## ${msg.proj.readme.techStack}

- **Spring Boot**: ${springBootVersion}
- **FastDDD**: ${fastdddVersion}
- **Java**: ${javaVersion}
- **${msg.proj.readme.databaseLabel}**: ${databaseDisplayName}

<#if modules?has_content>
## ${msg.proj.readme.enabledModules}

<#list modules as module>
- ${module?cap_first}
</#list>
</#if>

## ${msg.proj.readme.devGuide}

<#if modelType == "ANEMIC">
### ${msg.proj.readme.anemicDev}

${msg.proj.readme.anemicDesc}

1. **${msg.proj.readme.layerEntity}**${msg.proj.readme.layerColon}`${entityPackage}`
2. **${msg.proj.readme.layerService}**${msg.proj.readme.layerColon}`${servicePackage}`
3. **${msg.proj.readme.layerController}**${msg.proj.readme.layerColon}`${controllerPackage}`
4. **${msg.proj.readme.layerMapper}**${msg.proj.readme.layerColon}`${mapperPackage}`

```bash
${msg.proj.readme.mavenPluginComment}
mvn fastddd-maven-plugin:${r"${fastddd-tooling.version}"}:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/${artifactId} \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=${packageName}
```
<#else>
### ${msg.proj.readme.richDev}

${msg.proj.readme.richDesc}

1. **${msg.proj.readme.layerDomain}**${msg.proj.readme.layerColon}`${domainPackage}`
2. **${msg.proj.readme.layerApplication}**${msg.proj.readme.layerColon}`${applicationPackage}`
3. **${msg.proj.readme.layerInfrastructure}**${msg.proj.readme.layerColon}`${infrastructurePackage}`
4. **${msg.proj.readme.layerInterfaces}**${msg.proj.readme.layerColon}`${interfacesPackage}`

```bash
${msg.proj.readme.mavenPluginComment}
mvn fastddd-maven-plugin:${r"${fastddd-tooling.version}"}:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/${artifactId} \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=${packageName} \
  -Dcodegen.modelType=RICH
```
</#if>

## ${msg.proj.readme.config}

### ${msg.proj.readme.idStrategyTitle}

${msg.proj.readme.idStrategyCurrent}**${idStrategy!"AUTO"}**

${msg.proj.readme.idTableHeader}
|------|------|---------|
| `AUTO` | ${msg.proj.readme.id.autoDesc} | ${msg.proj.readme.id.autoUse} |
| `ASSIGN_ID` | ${msg.proj.readme.id.assignDesc} | ${msg.proj.readme.id.assignUse} |
| `INPUT` | ${msg.proj.readme.id.inputDesc} | ${msg.proj.readme.id.inputUse} |
| `NONE` | ${msg.proj.readme.id.noneDesc} | ${msg.proj.readme.id.noneUse} |

#### ${msg.proj.readme.idChange}

**${msg.proj.readme.idGlobal}**

${msg.proj.readme.idModifyYml}
```yaml
mybatis-plus:
  global-config:
    db-config:
      id-type: assign_id  ${msg.proj.readme.idYamlComment}
```

**${msg.proj.readme.idPerTable}**

${msg.proj.readme.idModifyTableId}
```java
// ${msg.proj.readme.id.autoDesc}
@TableId(type = IdType.AUTO)
private Long id;

// ${msg.proj.readme.id.assignDesc}
@TableId(type = IdType.ASSIGN_ID)
private Long id;

// ${msg.proj.readme.id.inputDesc}
@TableId(type = IdType.INPUT)
private Long id;
```

### ${msg.proj.readme.dbConfig}

${msg.proj.readme.dbEditHint}

<#if databaseType == "MYSQL">
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/${artifactId}
    username: your_username
    password: your_password
```
<#elseif databaseType == "POSTGRESQL">
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${artifactId}
    username: your_username
    password: your_password
```
<#else>
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
```
</#if>

## ${msg.proj.readme.docs}

- ${msg.proj.readme.docUsage}
- [CLAUDE.md](./CLAUDE.md)
- [${msg.proj.readme.docApi}](http://localhost:8080/doc.html)

## ${msg.proj.readme.license}

Copyright © ${year} FastDDD Team

---

**${msg.proj.readme.genTime}**: ${date!""}
**${msg.proj.readme.genTool}**: FastDDD Initializr v${fastdddToolingVersion}
