<#--
  类注释模板
  用于类的 Javadoc 注释

  参数：
  - className: 类名
  - description: 类描述
  - author: 作者
  - date: 日期
  - since: 版本号（可选）
  - deprecated: 是否已废弃（可选）

  使用方法：
  <#include "/common/class-comment.ftl"/>
-->
/**
 * ${className!""}${description?!" - " + description}
 *
 * @author ${author!"FastDDD Generator"}
 * @date ${date!""}
<#if since??>
 * @since ${since}
</#if>
<#if deprecated?? && deprecated>
 * @deprecated ${deprecated}
</#if>
 */
