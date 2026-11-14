package ${infrastructurePackage}.persistence;

<#assign useLombok = useLombok!true>
<#assign useBasePO = useBasePO!true>
<#assign useMyBatisPlus = useMyBatisPlus!true>

<#-- 自动检测是否需要 java.time / java.math 导入（依据实际列类型，缺一即编译失败） -->
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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
<#if useLombok>
import lombok.Data;
import lombok.EqualsAndHashCode;
</#if>

<#include "/common/imports.ftl"/>

/**
 * ${comment!''}${msg.rich.po.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("${tableName}")
public class ${entityName}PO extends BasePO<Long> {

    private static final long serialVersionUID = 1L;

<#-- 定义基础字段，不需要在 PO 中重复定义 -->
<#assign baseFields = ["id", "created_time", "updated_time", "created_by", "updated_by", "create_user", "update_user", "deleted", "version", "create_time", "update_time"]>
<#list table.columns as column>
    <#if !baseFields?seq_contains(column.columnName?lower_case)>
    /**
     * ${column.comment!column.fieldName}
     */
    @TableField("${column.columnName}")
    private ${column.javaType} ${column.fieldName};

    </#if>
</#list>
}
