package com.gallop.fastddd.codegen.service;

import com.gallop.fastddd.codegen.config.DatabaseConfig;
import com.gallop.fastddd.codegen.config.GeneratorConfig;
import com.gallop.fastddd.codegen.constants.IdStrategy;
import com.gallop.fastddd.codegen.model.GeneratorModel;
import com.gallop.fastddd.common.model.TableMetadata;
import com.gallop.fastddd.codegen.util.NamingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成服务
 *
 * @author gallop
 * @date 2023-10-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private final DatabaseMetadataService databaseMetadataService;
    private final TemplateProcessorService templateProcessorService;
    private final DatabaseConfig databaseConfig;

    /**
     * 生成代码（贫血模型，向后兼容）
     *
     * @param tableName   表名
     * @param packageName 包名
     * @param moduleName  模块名
     * @return 生成的代码文件映射 (文件名 -> 文件内容)
     */
    @Deprecated
    public Map<String, String> generateCode(String tableName, String packageName, String moduleName, GeneratorConfig config) {
        // 获取表元数据
        TableMetadata tableMetadata = databaseMetadataService.getTable(tableName);
        if (tableMetadata == null) {
            throw new RuntimeException("表 " + tableName + " 不存在");
        }

        // 创建生成模型
        GeneratorModel model = createGeneratorModel(tableMetadata, packageName, moduleName, config);

        // 生成代码
        return generateCodeFromTemplates(model);
    }

    /**
     * 生成代码（重载方法，直接接受TableMetadata）
     *
     * @param tableMetadata 表元数据
     * @param packageName 包名
     * @param moduleName  模块名
     * @param config 生成配置
     * @return 生成的代码文件映射 (文件名 -> 文件内容)
     */
    public Map<String, String> generateCode(TableMetadata tableMetadata, String packageName, String moduleName, GeneratorConfig config) {
        if (tableMetadata == null) {
            throw new RuntimeException("表元数据不能为空");
        }

        // 创建生成模型
        GeneratorModel model = createGeneratorModel(tableMetadata, packageName, moduleName, config);

        // 生成代码
        return generateCodeFromTemplates(model);
    }

    /**
     * 创建生成模型
     *
     * @param tableMetadata 表元数据
     * @param packageName 包名
     * @param moduleName  模块名
     * @param config 生成配置
     * @return 生成模型
     */
    private GeneratorModel createGeneratorModel(TableMetadata tableMetadata, String packageName, String moduleName, GeneratorConfig config) {
        GeneratorModel model = new GeneratorModel();
        model.setTable(tableMetadata);
        model.setPackageName(packageName);
        model.setModuleName(moduleName);

        // 从配置中复制属性
        if (config != null) {
            model.setModelType(config.getModelType() != null ? config.getModelType() : GeneratorConfig.ModelType.ANEMIC);
            model.setEnableCQRS(config.isEnableCQRS());
            model.setUseLombok(config.isUseLombok());
            model.setUseSwagger(config.isUseSwagger());
            model.setIdStrategy(config.getIdStrategy());
            // GEN-002：写模式入自定义属性，经 variables 流入模板（默认 READ_ONLY）
            model.addCustomProperty("writeMode",
                    config.getWriteMode() != null ? config.getWriteMode() : "READ_ONLY");

            // 为充血模型设置包名
            if (config.getModelType() == GeneratorConfig.ModelType.RICH) {
                model.addCustomProperty("domainPackage", packageName + ".domain");
                model.addCustomProperty("applicationPackage", packageName + ".application");
                model.addCustomProperty("infrastructurePackage", packageName + ".infrastructure");
            } else {
                model.addCustomProperty("entityPackage", packageName + ".entity");
            }

            // 设置作者
            if (config.getAuthor() != null) {
                model.setAuthor(config.getAuthor());
            }
        }

        return model;
    }

    /**
     * 从模板生成代码
     *
     * @param model 生成模型
     * @return 生成的代码文件映射 (文件名 -> 文件内容)
     */
    private Map<String, String> generateCodeFromTemplates(GeneratorModel model) {
        Map<String, String> result = new HashMap<>();

        // 获取实体名称
        String entityName = model.getTable().getEntityName();
        String lowerEntityName = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);

        // 添加模板变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("model", model);
        variables.put("table", model.getTable());
        // 添加 tableName 和 tableComment 字段，因为模板使用 table.tableName 和 table.tableComment
        variables.put("tableName", model.getTable().getTableName());
        variables.put("tableComment", model.getTable().getTableComment());
        variables.put("comment", model.getTable().getTableComment());
        variables.put("entityName", entityName);
        variables.put("lowerEntityName", lowerEntityName);
        variables.put("packageName", model.getPackageName());
        variables.put("moduleName", model.getModuleName());
        variables.put("author", model.getAuthor());
        variables.put("date", model.getDate());
        variables.put("useLombok", model.isUseLombok());
        variables.put("useSwagger", model.isUseSwagger());
        variables.put("useMyBatisPlus", true);
        variables.put("useBasePO", true);
        variables.put("modelType", model.getModelType());
        variables.put("enableCQRS", model.isEnableCQRS());
        // 使用枚举获取默认值，避免魔法字符串
        variables.put("idStrategy", model.getIdStrategy() != null ? model.getIdStrategy() : IdStrategy.AUTO.getCode());

        // 根据模型类型设置包名
        String packageName = model.getPackageName();
        if (model.getModelType() == GeneratorConfig.ModelType.RICH) {
            variables.put("domainPackage", packageName + ".domain");
            variables.put("applicationPackage", packageName + ".application");
            variables.put("infrastructurePackage", packageName + ".infrastructure");
        } else {
            variables.put("entityPackage", packageName + ".entity");
            variables.put("mapperPackage", packageName + ".mapper");
            variables.put("servicePackage", packageName + ".service");
            variables.put("serviceImplPackage", packageName + ".service.impl");
            variables.put("controllerPackage", packageName + ".controller");
            // GEN-002：DTO 写请求体包（writeMode=DTO 时 CreateRequest/UpdateRequest 与 Controller 引用）
            variables.put("dtoPackage", packageName + ".dto");
        }

        variables.putAll(model.getCustomProperties());

        // 根据模型类型生成不同的代码
        if (model.getModelType() == GeneratorConfig.ModelType.RICH) {
            // 充血模型代码生成
            generateRichModelCode(result, variables, entityName);
        } else {
            // 贫血模型代码生成（原有逻辑）
            generateAnemicModelCode(result, variables, entityName);
        }

        return result;
    }

    /**
     * 生成贫血模型代码
     */
    private void generateAnemicModelCode(Map<String, String> result, Map<String, Object> variables, String entityName) {
        // 获取包名（用于路径构建）
        String packageName = (String) variables.get("packageName");

        // 生成实体类 - 放入 entity 包
        String entityCode = templateProcessorService.processTemplate("anemic/Entity.ftl", variables);
        result.put("backend/src/main/java/" + packageName.replace('.', '/') + "/entity/" + entityName + ".java", entityCode);

        // 生成Mapper接口 - 放入 mapper 包
        String mapperCode = templateProcessorService.processTemplate("anemic/Mapper.ftl", variables);
        result.put("backend/src/main/java/" + packageName.replace('.', '/') + "/mapper/" + entityName + "Mapper.java", mapperCode);

        // 生成Service接口 - 放入 service 包
        String serviceCode = templateProcessorService.processTemplate("anemic/Service.ftl", variables);
        result.put("backend/src/main/java/" + packageName.replace('.', '/') + "/service/" + entityName + "Service.java", serviceCode);

        // 生成ServiceImpl实现类 - 放入 service.impl 包
        String serviceImplCode = templateProcessorService.processTemplate("anemic/ServiceImpl.ftl", variables);
        result.put("backend/src/main/java/" + packageName.replace('.', '/') + "/service/impl/" + entityName + "ServiceImpl.java", serviceImplCode);

        // 生成Controller - 放入 controller 包
        String controllerCode = templateProcessorService.processTemplate("anemic/Controller.ftl", variables);
        result.put("backend/src/main/java/" + packageName.replace('.', '/') + "/controller/" + entityName + "Controller.java", controllerCode);

        // GEN-002：writeMode=DTO 时生成 DTO 写请求体（Create/Update Request，带 @NotNull 校验），
        // 作为写端点请求体（非 PO），由 Controller.ftl 的写方法分支引用。
        String writeMode = (String) variables.getOrDefault("writeMode", "READ_ONLY");
        if ("DTO".equals(writeMode)) {
            String dtoPath = "backend/src/main/java/" + packageName.replace('.', '/') + "/dto/";
            String createRequestCode = templateProcessorService.processTemplate("anemic/CreateRequest.ftl", variables);
            result.put(dtoPath + "Create" + entityName + "Request.java", createRequestCode);

            String updateRequestCode = templateProcessorService.processTemplate("anemic/UpdateRequest.ftl", variables);
            result.put(dtoPath + "Update" + entityName + "Request.java", updateRequestCode);
        }
    }

    /**
     * 生成充血模型代码
     */
    private void generateRichModelCode(Map<String, String> result, Map<String, Object> variables, String entityName) {
        String domainPackage = (String) variables.get("domainPackage");
        String applicationPackage = (String) variables.get("applicationPackage");
        String infrastructurePackage = (String) variables.get("infrastructurePackage");

        // 1. 生成 PO（持久化对象）- 放入 infrastructure.persistence 包
        String poCode = templateProcessorService.processTemplate("rich/po.ftl", variables);
        result.put("backend/src/main/java/" + infrastructurePackage.replace('.', '/') + "/persistence/" + entityName + "PO.java", poCode);

        // 2. 生成聚合根实体（使用实际存在的大写文件名）- 放入 domain 包
        String aggregateRootCode = templateProcessorService.processTemplate("rich/AggregateRoot.ftl", variables);
        result.put("backend/src/main/java/" + domainPackage.replace('.', '/') + "/" + entityName + ".java", aggregateRootCode);

        // 2.1 生成领域创建事件 - 放入 domain 包（聚合根 create() 发布 ${entityName}CreatedEvent，须生成否则编译缺类）
        String createdEventCode = templateProcessorService.processTemplate("rich/DomainEvent.ftl", variables);
        result.put("backend/src/main/java/" + domainPackage.replace('.', '/') + "/" + entityName + "CreatedEvent.java", createdEventCode);

        // 3. 生成 EntityMapper - 放入 infrastructure.persistence 包（与 PO/Repository 同包，template 声明 .persistence）
        String mapperCode = templateProcessorService.processTemplate("rich/entityMapper.ftl", variables);
        result.put("backend/src/main/java/" + infrastructurePackage.replace('.', '/') + "/persistence/" + entityName + "Mapper.java", mapperCode);

        // 3.1 生成 MyBatis-Plus 基础 Mapper - 放入 infrastructure.persistence 包（RepositoryImpl 注入 ${entityName}BaseMapper，须生成否则编译缺类）
        String entityBaseMapperCode = templateProcessorService.processTemplate("rich/EntityBaseMapper.ftl", variables);
        result.put("backend/src/main/java/" + infrastructurePackage.replace('.', '/') + "/persistence/" + entityName + "BaseMapper.java", entityBaseMapperCode);

        // 4. 生成 EntityRepository 接口 - 放入 infrastructure.persistence 包
        String repositoryCode = templateProcessorService.processTemplate("rich/entityRepository.ftl", variables);
        result.put("backend/src/main/java/" + infrastructurePackage.replace('.', '/') + "/persistence/" + entityName + "Repository.java", repositoryCode);

        // 5. 生成 EntityRepositoryImpl 实现 - 放入 infrastructure.persistence 包
        String repositoryImplCode = templateProcessorService.processTemplate("rich/entityRepositoryImpl.ftl", variables);
        result.put("backend/src/main/java/" + infrastructurePackage.replace('.', '/') + "/persistence/" + entityName + "RepositoryImpl.java", repositoryImplCode);

        // 6. 生成应用服务 - 放入 application 包（applicationPackage 已含 .application，路径不再追加 /application/）
        String applicationServiceCode = templateProcessorService.processTemplate("rich/applicationService.ftl", variables);
        result.put("backend/src/main/java/" + applicationPackage.replace('.', '/') + "/" + entityName + "ApplicationService.java", applicationServiceCode);

        // 7. 如果启用 CQRS，生成 Command 和 Query
        if ((boolean) variables.get("enableCQRS")) {
            // 生成 Command - 放入 application.command 包
            String commandCode = templateProcessorService.processTemplate("rich/Command.ftl", variables);
            result.put("backend/src/main/java/" + applicationPackage.replace('.', '/') + "/command/" + "Create" + entityName + "Command.java", commandCode);

            // 生成 CommandHandler - 放入 application.handler 包
            String commandHandlerCode = templateProcessorService.processTemplate("rich/CommandHandler.ftl", variables);
            result.put("backend/src/main/java/" + applicationPackage.replace('.', '/') + "/handler/" + "Create" + entityName + "CommandHandler.java", commandHandlerCode);

            // 生成 Query - 放入 application.query 包
            String queryCode = templateProcessorService.processTemplate("rich/query.ftl", variables);
            result.put("backend/src/main/java/" + applicationPackage.replace('.', '/') + "/query/" + "Get" + entityName + "ByIdQuery.java", queryCode);

            // 生成 QueryHandler - 放入 application.handler 包
            String queryHandlerCode = templateProcessorService.processTemplate("rich/queryHandler.ftl", variables);
            result.put("backend/src/main/java/" + applicationPackage.replace('.', '/') + "/handler/" + "Get" + entityName + "ByIdQueryHandler.java", queryHandlerCode);
        }

        // 8/9. 生成 CommandController / QueryController —— 与 Command/Query 同门控：
        // 控制器引用 Create*Command / Get*ByIdQuery 及其 Handler（第 7 步 enableCQRS 才生成），
        // 关闭 CQRS 时无条件生成会产出引用缺失类的不可编译控制器（D-2c 同源）。
        if ((boolean) variables.get("enableCQRS")) {
            String commandControllerCode = templateProcessorService.processTemplate("rich/CommandController.ftl", variables);
            result.put("backend/src/main/java/" + applicationPackage.replace('.', '/') + "/controller/" + entityName + "CommandController.java", commandControllerCode);

            String queryControllerCode = templateProcessorService.processTemplate("rich/queryController.ftl", variables);
            result.put("backend/src/main/java/" + applicationPackage.replace('.', '/') + "/controller/" + entityName + "QueryController.java", queryControllerCode);
        }
    }
}
