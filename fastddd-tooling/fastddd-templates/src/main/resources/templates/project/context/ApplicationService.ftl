package ${contextApplicationPkg};

import ${contextDomainPkg}.${aggregateClassName};
import ${contextDomainPkg}.${aggregateClassName}Repository;
import com.gallop.fastddd.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ${aggregateClassName} ${msg.proj.context.applicationService.desc}
 *
 * @author ${author}
 * @date ${date}
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ${aggregateClassName}Service {

    private final ${aggregateClassName}Repository ${aggregateVarName}Repository;

    public ${aggregateClassName}Service(${aggregateClassName}Repository ${aggregateVarName}Repository) {
        this.${aggregateVarName}Repository = ${aggregateVarName}Repository;
    }

    public ${aggregateClassName} create(String name) {
        ${aggregateClassName} ${aggregateVarName} = ${aggregateClassName}.create(name);
        return ${aggregateVarName}Repository.save(${aggregateVarName});
    }

    @Transactional(readOnly = true)
    public ${aggregateClassName} findById(Long id) {
        return ${aggregateVarName}Repository.findById(id)
                .orElseThrow(() -> new BusinessException("${aggregateClassName} not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<${aggregateClassName}> findAll() {
        return ${aggregateVarName}Repository.findAll();
    }
}
