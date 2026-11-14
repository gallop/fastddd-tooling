<#--
  ToString 方法模板
  用于生成 toString() 方法

  参数：
  - className: 类名
  - fields: 字段列表（用逗号分隔）

  使用方法：
  <#include "/common/tostring.ftl"/>
-->
<#macro toStringMethod className fields="">
    @Override
    public String toString() {
        <#if fields?has_content && fields?length gt 0>
        return "${className}{" +
                <#list fields?split(',') as field>
                "${field?trim}='" + ${field?trim} + '\'' +
                <#if field_has_next>
                + ", " +
                </#if>
                </#list>
                '}';
        <#else>
        return "${className}{}";
        </#if>
    }
</#macro>

<#--
  使用示例：

  <@toStringMethod className="User" fields="id,userName,email" />
-->
