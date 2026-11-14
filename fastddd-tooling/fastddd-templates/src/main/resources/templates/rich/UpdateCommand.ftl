package ${applicationPackage}.command;

import com.gallop.fastddd.application.command.Command;
<#assign useLombok = useLombok!true>
<#assign useValidation = useValidation!true>

<#-- 自动检测是否需要 java.time 导入 -->
<#assign useJavaTime = false>
<#list table.columns as column>
    <#if column.javaType?contains('LocalDateTime') || column.javaType?contains('LocalDate') || column.javaType?contains('LocalTime')>
        <#assign useJavaTime = true>
        <#break>
    </#if>
</#list>

<#include "/common/imports.ftl"/>

/**
 * ${msg.rich.update.methodPrefix}${entityName}${msg.rich.command.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Update${entityName}Command extends Command {

    /**
     * ${entityName} ID
     */
    @NotNull(message = "ID${msg.common.validation.notBlank}")
    private Long id;

<#-- 定义基础字段，更新时不需要包含 -->
<#assign baseFields = ["id", "created_time", "updated_time", "created_by", "updated_by", "deleted", "version", "create_time", "update_time"]>
<#list table.columns as column>
    <#if !baseFields?seq_contains(column.columnName?lower_case)>
    /**
     * ${column.comment!column.fieldName}
     */
    private ${column.javaType} ${column.fieldName};

    </#if>
</#list>
}
