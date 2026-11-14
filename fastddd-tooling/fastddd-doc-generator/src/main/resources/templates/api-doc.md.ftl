<#ftl encoding="UTF-8">
<#if controller??>
# ${controller.className!}

<#if controller.description??>
**${msg.doc.label.description}** ${controller.description}

</#if>
<#if controller.basePath??>
**${msg.doc.label.basePath}** `${controller.basePath}`

</#if>
**${msg.doc.label.package}** `${controller.packageName!}`

---

## ${msg.doc.heading.endpoints}

<#list controller.methods as method>
### ${method.httpMethod!} ${method.fullPath!}

<#if method.summary??>
**${method.summary!}**
</#if>

<#if (method.description!"")?has_content && method.description != (method.summary!"")>
${method.description}
</#if>

**${msg.doc.label.httpMethod}** `${method.httpMethod!}`

**${msg.doc.label.requestPath}** `${method.fullPath!}`

**${msg.doc.label.returnType}** `${method.returnType!"void"}`

<#if method.deprecated!false>
> **${msg.doc.label.notice}** ${msg.doc.text.deprecated}
</#if>

<#if method.parameters?? && (method.parameters?size > 0)>
#### ${msg.doc.heading.params}

<#list method.parameters as param>
**${param.name}** <#if param.required!false>*${msg.doc.required}* </#if>
- **${msg.doc.label.type}** `${param.type!}`
- **${msg.doc.label.location}** `${param.location.displayName!}`
<#if (param.description!"")?has_content>
- **${msg.doc.label.description}** `${param.description}`
</#if>
<#if (param.defaultValue!"")?has_content>
- **${msg.doc.label.defaultValue}** `${param.defaultValue}`
</#if>
<#if (param.example!"")?has_content>
- **${msg.doc.label.example}** `${param.example}`
</#if>

</#list>
</#if>

<#if method.requestBody??>
#### ${msg.doc.heading.requestBody}

**${msg.doc.label.type}** `${method.requestBody.typeName!}`

<#if (method.requestBody.description!"")?has_content>
**${msg.doc.label.description}** ${method.requestBody.description}
</#if>

<#if (method.requestBody.exampleJson!"")?has_content>
**${msg.doc.label.exampleJson}**
```json
${method.requestBody.exampleJson}
```
</#if>
</#if>

<#if method.responseBody??>
#### ${msg.doc.heading.responseBody}

**${msg.doc.label.type}** `${method.responseBody.typeName!}`

<#if (method.responseBody.description!"")?has_content>
**${msg.doc.label.description}** ${method.responseBody.description}
</#if>

<#if (method.responseBody.exampleJson!"")?has_content>
**${msg.doc.label.exampleJson}**
```json
${method.responseBody.exampleJson}
```
</#if>
</#if>

---

</#list>

**${msg.doc.label.generatedAt}** ${generatedAt!}
**${msg.doc.label.docVersion}** 1.0.0

</#if>
