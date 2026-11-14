package com.gallop.fastddd.codegen.model;

import com.gallop.fastddd.codegen.config.GeneratorConfig;
import com.gallop.fastddd.codegen.constants.IdStrategy;
import com.gallop.fastddd.common.model.TableMetadata;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成模型
 *
 * @author gallop
 * @date 2023-10-10
 */
@Data
public class GeneratorModel {

    /**
     * 表元数据
     */
    private TableMetadata table;
    
    /**
     * 包名
     */
    private String packageName;
    
    /**
     * 模块名
     */
    private String moduleName;
    
    /**
     * 作者
     */
    private String author = "gallop";
    
    /**
     * 日期
     */
    private String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    
    /**
     * 是否使用Lombok
     */
    private boolean useLombok = true;
    
    /**
     * 是否使用Swagger
     */
    private boolean useSwagger = true;
    
    /**
     * 是否生成BaseEntity
     */
    private boolean generateBaseEntity = true;

    /**
     * 模型类型
     */
    private GeneratorConfig.ModelType modelType = GeneratorConfig.ModelType.ANEMIC;

    /**
     * 是否启用 CQRS
     */
    private boolean enableCQRS = false;

    /**
     * 主键生成策略
     * <p>用于指定生成Entity类的@TableId注解类型。</p>
     * <p>支持的值：AUTO、ASSIGN_ID、INPUT、NONE</p>
     *
     * @see IdStrategy
     * @since 1.0.1
     */
    private String idStrategy = IdStrategy.AUTO.getCode();

    /**
     * 自定义属性
     */
    private Map<String, Object> customProperties = new HashMap<>();
    
    /**
     * 添加自定义属性
     *
     * @param key   键
     * @param value 值
     */
    public void addCustomProperty(String key, Object value) {
        customProperties.put(key, value);
    }
}