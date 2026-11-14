package ${applicationPackage}.command;

import com.gallop.fastddd.application.command.Command;
<#assign useLombok = useLombok!true>
<#assign useValidation = useValidation!true>

<#include "/common/imports.ftl"/>

/**
 * ${msg.rich.delete.prefix}${entityName}${msg.rich.command.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Delete${entityName}Command extends Command {

    /**
     * ${entityName} ID
     */
    @NotNull(message = "ID${msg.common.validation.notBlank}")
    private Long id;
}
