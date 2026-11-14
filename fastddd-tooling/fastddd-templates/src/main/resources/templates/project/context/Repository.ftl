package ${contextDomainPkg};

import java.util.List;
import java.util.Optional;

/**
 * ${aggregateClassName} ${msg.proj.context.repository.desc}
 *
 * @author ${author}
 * @date ${date}
 */
public interface ${aggregateClassName}Repository {

    ${aggregateClassName} save(${aggregateClassName} ${aggregateVarName});

    Optional<${aggregateClassName}> findById(Long id);

    List<${aggregateClassName}> findAll();
}
