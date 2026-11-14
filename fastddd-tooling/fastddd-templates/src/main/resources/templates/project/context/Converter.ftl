package ${contextPersistencePkg};

import ${contextDomainPkg}.${aggregateClassName};

import java.io.Serializable;

/**
 * ${aggregateClassName} ${msg.proj.context.converter.desc}
 *
 * @author ${author}
 * @date ${date}
 */
public final class ${aggregateClassName}Converter {

    private ${aggregateClassName}Converter() {
    }

    public static ${aggregateClassName}PO toPO(${aggregateClassName} ${aggregateVarName}) {
        ${aggregateClassName}PO po = new ${aggregateClassName}PO();
        po.setId(${aggregateVarName}.getId());
        po.setName(${aggregateVarName}.getName());
        po.setStatus(${aggregateVarName}.getStatus() != null
                ? ${aggregateVarName}.getStatus().getDbValue() : null);
        return po;
    }

    public static ${aggregateClassName} toDomain(${aggregateClassName}PO po) {
        // ${msg.proj.context.converter.restoreNote}
        return ${aggregateClassName}.restore(
                toLong(po.getId()),
                po.getName(),
                po.getStatus()
        );
    }

    private static Long toLong(Serializable id) {
        if (id == null) return null;
        if (id instanceof Number number) return number.longValue();
        return Long.valueOf(id.toString());
    }
}
