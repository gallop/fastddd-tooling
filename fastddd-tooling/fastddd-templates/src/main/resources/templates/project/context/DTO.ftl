package ${contextDtoPkg};

import ${contextDomainPkg}.${aggregateClassName};

/**
 * ${aggregateClassName} ${msg.proj.context.dto.desc}
 *
 * TODO: ${msg.proj.context.dto.todoA}
 * ${msg.proj.context.dto.todoB}
 *
 * @author ${author}
 * @date ${date}
 */
public record ${aggregateClassName}Response(
        Long id,
        String name,
        String status
) {
    public static ${aggregateClassName}Response from(${aggregateClassName} ${aggregateVarName}) {
        return new ${aggregateClassName}Response(
                ${aggregateVarName}.getId(),
                ${aggregateVarName}.getName(),
                ${aggregateVarName}.getStatus() != null
                        ? ${aggregateVarName}.getStatus().getDbValue() : null
        );
    }
}
