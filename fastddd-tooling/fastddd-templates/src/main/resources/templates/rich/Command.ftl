package ${applicationPackage}.command;

import com.gallop.fastddd.application.command.Command;
<#assign useLombok = useLombok!true>
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
 * ${msg.rich.factory.create}${entityName}${msg.rich.command.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Create${entityName}Command extends Command {

<#-- 定义基础字段，不需要在 Command 中包含 -->
<#assign baseFields = ["id", "created_time", "updated_time", "created_by", "updated_by", "deleted", "version", "create_time", "update_time"]>
<#list table.columns as column>
    <#if !baseFields?seq_contains(column.columnName?lower_case)>
    <#if column.javaType == "String">
    @NotBlank(message = "${column.comment!column.fieldName}${msg.common.validation.notBlank}")
    </#if>
    <#if column.javaType?contains("Integer") || column.javaType?contains("Long")>
    @NotNull(message = "${column.comment!column.fieldName}${msg.common.validation.notBlank}")
    </#if>
    private ${column.javaType} ${column.fieldName};

    </#if>
</#list>
}
