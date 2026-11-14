package ${applicationPackage}.query;

import com.gallop.fastddd.application.query.Query;
<#assign useLombok = useLombok!true>
<#assign useValidation = useValidation!true>

<#include "/common/imports.ftl"/>

/**
 * ${msg.rich.repo.findByIdDesc}${comment!''}
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@NoArgsConstructor
public class Get${entityName}ByIdQuery extends Query {

    /**
     * ID
     */
    @NotNull(message = "ID${msg.common.validation.notBlank}")
    private Long id;

    public String getIdentifier() {
        return "Get${entityName}ById-" + id;
    }

    @Override
    public String getQueryType() {
        return "QUERY";
    }
}
