package ${dtoPackage};

<#assign useLombok = useLombok!true>
<#assign useSwagger = useSwagger!false>
<#assign useValidation = useValidation!true>

<#-- 自动检测是否需要 java.time / java.math 导入 -->
<#assign useJavaTime = false>
<#assign useBigDecimal = false>
<#list table.columns as column>
    <#if column.javaType?contains('LocalDateTime') || column.javaType?contains('LocalDate') || column.javaType?contains('LocalTime')>
        <#assign useJavaTime = true>
    </#if>
    <#if column.javaType?contains('BigDecimal')>
        <#assign useBigDecimal = true>
    </#if>
</#list>

<#include "/common/imports.ftl"/>

/**
 * ${msg.anemic.request.updatePrefix}${comment!''}${msg.anemic.request.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
<#if useSwagger>
@Schema(description = "${msg.anemic.request.updatePrefix}${comment!''}${msg.anemic.request.suffix}")
</#if>
public class Update${table.entityName}Request {

    /**
     * ID
     */
    <#if useValidation>
    @NotNull(message = "ID${msg.common.validation.notBlank}")
    </#if>
    <#if useSwagger>
    @Schema(description = "ID", required = true)
    </#if>
    private Long id;

<#-- 定义 BasePO 中的基础字段，更新时不需要传入 -->
<#assign basePOFields = ["id", "create_time", "update_time", "created_by", "updated_by", "deleted", "version"]>
<#list table.columns as column>
    <#if !basePOFields?seq_contains(column.columnName?lower_case)>
    /**
     * ${column.comment!column.fieldName}
     */
    <#if useSwagger>
    @Schema(description = "${column.comment!column.fieldName}")
    </#if>
    private ${column.javaType} ${column.fieldName};

    </#if>
</#list>
}
