package ${domainPackage};

<#assign useLombok = useLombok!true>

<#include "/common/imports.ftl"/>

/**
 * ${comment!''}${msg.rich.valueObject.suffix}
 * ${msg.rich.valueObject.desc}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ${entityName}ValueObject {

<#list table.columns as column>
    /**
     * ${column.comment!column.fieldName}
     */
    private final ${column.javaType} ${column.fieldName};

</#list>

    /**
     * ${msg.rich.valueObject.sameValueAsDesc}
     *
     * @param other ${msg.rich.valueObject.otherParam}
     * @return ${msg.rich.valueObject.sameValueAsReturn}
     */
    public boolean sameValueAs(${entityName}ValueObject other) {
        if (this == other) return true;
        if (other == null) return false;
<#list table.columns as column>
        if (${column.fieldName} != null ? !${column.fieldName}.equals(other.${column.fieldName}) : other.${column.fieldName} != null) return false;
</#list>
        return true;
    }
}
