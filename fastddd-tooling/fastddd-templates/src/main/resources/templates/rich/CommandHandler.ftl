package ${applicationPackage}.handler;

import org.springframework.stereotype.Component;

import ${applicationPackage}.command.Create${entityName}Command;
import ${domainPackage}.${entityName};
import ${infrastructurePackage}.persistence.${entityName}Repository;
import com.gallop.fastddd.application.command.CommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ${msg.rich.factory.create}${entityName}${msg.rich.commandHandler.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Create${entityName}CommandHandler implements CommandHandler<Create${entityName}Command, ${entityName}> {

    private final ${entityName}Repository ${lowerEntityName}Repository;

    @Override
    public ${entityName} handle(Create${entityName}Command command) {
        log.debug("${msg.rich.factory.create}${comment!''}: {}", command);

        // ${msg.rich.factory.create}${msg.rich.aggregate.root.suffix}
        ${entityName} ${lowerEntityName} = ${entityName}.create(
<#-- 定义基础字段，不需要在创建时传入 -->
<#assign baseFields = ["id", "created_time", "updated_time", "created_by", "updated_by", "deleted", "version", "create_time", "update_time"]>
<#assign firstField = true>
<#list table.columns as column>
    <#if !baseFields?seq_contains(column.columnName?lower_case)>
        <#if !firstField>, </#if>command.get${column.fieldName?cap_first}()
        <#assign firstField = false>
    </#if>
</#list>
        );

        // ${msg.rich.repo.saveDesc}
        return ${lowerEntityName}Repository.save(${lowerEntityName});
    }
}
