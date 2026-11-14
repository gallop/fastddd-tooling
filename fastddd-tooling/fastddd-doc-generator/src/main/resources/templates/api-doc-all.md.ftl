<#ftl encoding="UTF-8">
# ${apiDoc.projectName!"API Documentation"}

${apiDoc.description!""}

<#list controllers as controller>
<#include "api-doc.md.ftl">
</#list>
