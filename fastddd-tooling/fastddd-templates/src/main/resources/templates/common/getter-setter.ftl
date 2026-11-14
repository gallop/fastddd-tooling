<#--
  Getter/Setter 方法模板
  用于不使用 Lombok 时生成 getter/setter

  参数：
  - fieldName: 字段名
  - fieldType: 字段类型

  使用方法：
  <#include "/common/getter-setter.ftl"/>
-->
<#macro getterSetter fieldName fieldType>
    public ${fieldType} get${fieldName?cap_first}() {
        return this.${fieldName};
    }

    public void set${fieldName?cap_first}(${fieldType} ${fieldName}) {
        this.${fieldName} = ${fieldName};
    }
</#macro>

<#--
  使用示例：

  <@getterSetter fieldName="userName" fieldType="String" />
-->
