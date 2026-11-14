package ${contextDomainPkg};

import com.gallop.fastddd.common.domain.AggregateRoot;
import com.gallop.fastddd.common.exception.BusinessException;
<#if enableEvent?? && enableEvent>
import ${contextEventPkg}.${aggregateClassName}CreatedEvent;
</#if>
import java.util.Objects;

/**
 * ${aggregateClassName} ${msg.proj.context.aggregateRoot.suffix}
 *
 * TODO: ${msg.proj.context.aggregateRoot.todoLogic}
 *
 * @author ${author}
 * @date ${date}
 */
public class ${aggregateClassName} extends AggregateRoot<Long> {

    // TODO: ${msg.proj.context.aggregateRoot.todoFields}
    private String name;
    private ${aggregateClassName}Status status = ${aggregateClassName}Status.CREATED;

    ${aggregateClassName}() {
    }

    /**
     * ${msg.proj.context.aggregateRoot.createDesc} ${aggregateClassName}
     */
    public static ${aggregateClassName} create(String name) {
        Objects.requireNonNull(name, "name ${msg.proj.context.validation.notBlank}");

        ${aggregateClassName} ${aggregateVarName} = new ${aggregateClassName}();
        ${aggregateVarName}.name = name;
<#if enableEvent?? && enableEvent>
        // TODO: ${msg.proj.context.aggregateRoot.eventTodo}
        // ${msg.proj.context.aggregateRoot.eventAdvice}
        ${aggregateVarName}.addDomainEvent(new ${aggregateClassName}CreatedEvent(${aggregateVarName}.id));
</#if>
        return ${aggregateVarName};
    }

    /**
     * ${msg.proj.context.aggregateRoot.restoreDescPrefix} ${aggregateClassName}${msg.proj.context.aggregateRoot.restoreDescSuffix}
     *
     * ${msg.proj.context.aggregateRoot.restoreNoteA}
     * ${msg.proj.context.aggregateRoot.restoreNoteB}
     */
    public static ${aggregateClassName} restore(Long id, String name, String status) {
        ${aggregateClassName} ${aggregateVarName} = new ${aggregateClassName}();
        ${aggregateVarName}.id = id;
        ${aggregateVarName}.name = name;
        ${aggregateVarName}.status = status != null ? ${aggregateClassName}Status.fromDbValue(status) : null;
        return ${aggregateVarName};
    }

    /**
     * ${msg.proj.context.aggregateRoot.updateNameDesc}
     */
    public void updateName(String name) {
        Objects.requireNonNull(name, "name ${msg.proj.context.validation.notBlank}");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ${aggregateClassName}Status getStatus() {
        return status;
    }
}
