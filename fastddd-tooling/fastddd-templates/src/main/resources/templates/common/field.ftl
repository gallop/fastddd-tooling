<#--
  字段定义模板
  用于实体类的字段定义

  参数：
  - fieldName: 字段名（camelCase）
  - fieldType: 字段类型
  - fieldComment: 字段注释
  - isRequired: 是否必需（默认 false）
  - maxLength: 最大长度（可选）
  - pattern: 正则表达式（可选）

  使用方法：
  <#include "/common/field.ftl"/>
-->
<#macro fieldDef fieldName fieldType fieldComment="" isRequired=false maxLength=0 pattern="">
    <#if fieldComment??>
    /**
     * ${fieldComment}
    <#if isRequired>
     * (required)
    </#if>
    <#if maxLength?gt 0>
     * (max length: ${maxLength})
    </#if>
    <#if pattern??>
     * (pattern: ${pattern})
    </#if>
     */
    </#if>
    <#if useSwagger?? && useSwagger>
    @ApiModelProperty(value = "${fieldComment!"Field"}"<#if isRequired>, required = true</#if>)
    </#if>
    <#if useValidation?? && useValidation && isRequired>
    @NotNull(message = "${fieldComment!"Field"}${msg.common.validation.notBlank}")
    </#if>
    <#if useValidation?? && useValidation && (fieldType?contains("String")) && maxLength?gt 0>
    @Size(max = ${maxLength}, message = "${fieldComment!"Field"}${msg.common.validation.sizePrefix}${maxLength}")
    </#if>
    <#if useValidation?? && useValidation && pattern??>
    @Pattern(regexp = "${pattern}", message = "${fieldComment!"Field"}${msg.common.validation.patternInvalid}")
    </#if>
    private ${fieldType} ${fieldName};
</#macro>

<#--
  使用示例：

  <@fieldDef fieldName="userName" fieldType="String" fieldComment="用户名" isRequired=true maxLength=50 />
  <@fieldDef fieldName="age" fieldType="Integer" fieldComment="年龄" />
-->
