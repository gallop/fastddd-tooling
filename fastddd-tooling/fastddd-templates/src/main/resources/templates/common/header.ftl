<#--
  文件头注释模板
  用于生成的 Java 文件顶部

  使用方法：
  <#include "/common/header.ftl"/>
-->
<#if author??>
/**
 * ${description!""}
 *
 * @author ${author}
 * @date ${date!""}
<#if since??>
 * @since ${since}
</#if>
 */
</#if>
