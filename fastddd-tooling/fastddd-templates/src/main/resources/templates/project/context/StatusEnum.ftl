package ${contextDomainPkg};

import com.gallop.fastddd.common.exception.BusinessException;

import java.util.Arrays;
import java.util.Set;

/**
 * ${aggregateClassName} ${msg.proj.context.statusEnum.desc}
 *
 * TODO: ${msg.proj.context.statusEnum.todo}
 *
 * @author ${author}
 * @date ${date}
 */
public enum ${aggregateClassName}Status {

    CREATED("created"),
    ACTIVE("active"),
    CLOSED("closed");

    private static final Set<${aggregateClassName}Status> TRANSITIONABLE = Set.of(CREATED, ACTIVE);

    private final String dbValue;

    ${aggregateClassName}Status(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public boolean canTransition() {
        return TRANSITIONABLE.contains(this);
    }

    public boolean isClosed() {
        return this == CLOSED;
    }

    public static ${aggregateClassName}Status fromDbValue(String dbValue) {
        return Arrays.stream(values())
                .filter(s -> s.dbValue.equals(dbValue))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Unknown ${aggregateVarName} status: " + dbValue));
    }
}
