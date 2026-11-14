package ${domainPackage};

<#assign useLombok = useLombok!true>
<#assign useSwagger = useSwagger!false>
<#assign useAggregateRoot = useAggregateRoot!true>
<#assign useBaseEntity = false>

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

<#include "/common/imports.ftl"/>

/**
 * ${comment!''}${msg.rich.aggregate.root.suffix}
<#if table.aggregateName?? && table.aggregateName?has_content>
 * ${msg.rich.aggregate.root.aggregateNamePrefix}${table.aggregateName}
</#if>
<#if table.relationsSummary?? && table.relationsSummary?has_content>
 * ${msg.rich.aggregate.root.relationsSummaryPrefix}${table.relationsSummary}
</#if>
 *
 * @author ${author}
 * @date ${date}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
<#if useSwagger>
@Schema(description = "${comment!''}${msg.rich.aggregate.root.suffix}")
</#if>
public class ${entityName} extends AggregateRoot<Long> {

    private static final long serialVersionUID = 1L;

<#-- 定义基础字段，不需要在 Entity 中重复定义 -->
<#assign baseFields = ["id", "created_time", "updated_time", "created_by", "updated_by", "deleted", "version", "create_time", "update_time"]>
<#list table.columns as column>
    <#if !baseFields?seq_contains(column.columnName?lower_case)>
    /**
     * ${column.comment!column.fieldName}
     */
<#if useSwagger>
    @Schema(description = "${column.comment!column.fieldName}")
</#if>
    private ${column.javaType} ${column.fieldName};

    </#if>
</#list>
    /**
     * ${msg.rich.factory.create}${comment!''}
     *
     * @return ${msg.rich.factory.returnPrefix}${comment!''}${msg.common.instance.suffix}
     */
    public static ${entityName} create(<#assign firstParam = true><#list table.columns as column><#if !baseFields?seq_contains(column.columnName?lower_case)><#if !firstParam>, </#if>${column.javaType} ${column.fieldName}<#assign firstParam = false></#if></#list>) {
        ${entityName} ${lowerEntityName} = new ${entityName}();

<#list table.columns as column>
    <#if !baseFields?seq_contains(column.columnName?lower_case)>
        ${lowerEntityName}.${column.fieldName} = ${column.fieldName};
    </#if>
</#list>

        <#-- 发布领域事件 -->
        ${lowerEntityName}.addDomainEvent(new ${entityName}CreatedEvent(${lowerEntityName}.getId()));

        return ${lowerEntityName};
    }

<#list table.columns as column>
<#if column.javaType == "String" && !column.fieldName?contains("status") && !column.fieldName?contains("State")>
    /**
     * ${msg.rich.update.methodPrefix}${column.comment!column.fieldName}
     *
     * @param ${column.fieldName} ${column.comment!column.fieldName}
     */
    public void update${column.fieldName?cap_first}(${column.javaType} ${column.fieldName}) {
        this.${column.fieldName} = ${column.fieldName};
    }

</#if>
</#list>
    /**
     * ${msg.rich.isValid.desc}
     *
     * ${msg.rich.isValid.note}
     * TODO: ${msg.rich.isValid.todo}
     *
     * @return ${msg.rich.isValid.returnDefault}
     */
    public boolean isValid() {
        return true;
    }
}
