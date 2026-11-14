package ${entityPackage};

<#assign useLombok = useLombok!true>
<#assign useSwagger = useSwagger!false>
<#assign useBasePO = useBasePO!true>
<#assign useMyBatisPlus = useMyBatisPlus!true>
<#assign idStrategy = idStrategy!"AUTO">

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

<#-- 导入必要的类 -->
<#if useJavaTime>
import java.time.LocalDateTime;
</#if>
<#if useBigDecimal>
import java.math.BigDecimal;
</#if>
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
<#if useLombok>
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
</#if>
<#if useSwagger>
import io.swagger.v3.oas.annotations.media.Schema;
</#if>
import com.gallop.fastddd.model.po.BasePO;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * ${comment!entityName}${msg.anemic.entity.suffix}
<#if table.dddRole?? && table.dddRole?has_content>
 * ${msg.anemic.entity.dddRolePrefix}${table.dddRole}
</#if>
 *
 * @author ${author}
 * @date ${date}
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
<#if useLombok>
@Builder
</#if>
@TableName("${tableName}")
<#if useSwagger>
@Schema(description = "${comment!entityName}")
</#if>
public class ${entityName} extends BasePO<Long> {

<#-- 定义 BasePO 中的基础字段，这些字段不需要在 Entity 中重复定义 -->
<#assign basePOFields = ["id", "create_time", "update_time", "create_user", "update_user", "deleted", "version"]>
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
