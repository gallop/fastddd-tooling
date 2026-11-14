package ${contextPersistencePkg};

import ${contextDomainPkg}.${aggregateClassName};
import ${contextDomainPkg}.${aggregateClassName}Repository;
import com.gallop.fastddd.common.exception.BusinessException;
<#if enableEvent?? && enableEvent>
import com.gallop.fastddd.event.EventPublisher;
</#if>
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ${aggregateClassName} ${msg.proj.context.repositoryImpl.desc}
 *
 * @author ${author}
 * @date ${date}
 */
@Repository
public class ${aggregateClassName}RepositoryImpl implements ${aggregateClassName}Repository {

    private final ${aggregateClassName}Mapper ${aggregateVarName}Mapper;
<#if enableEvent?? && enableEvent>
    private final EventPublisher eventPublisher;

    public ${aggregateClassName}RepositoryImpl(${aggregateClassName}Mapper ${aggregateVarName}Mapper,
                                               EventPublisher eventPublisher) {
        this.${aggregateVarName}Mapper = ${aggregateVarName}Mapper;
        this.eventPublisher = eventPublisher;
    }
<#else>
    public ${aggregateClassName}RepositoryImpl(${aggregateClassName}Mapper ${aggregateVarName}Mapper) {
        this.${aggregateVarName}Mapper = ${aggregateVarName}Mapper;
    }
</#if>

    @Override
    public ${aggregateClassName} save(${aggregateClassName} ${aggregateVarName}) {
        boolean isNew = ${aggregateVarName}.getId() == null;
        ${aggregateClassName}PO po = ${aggregateClassName}Converter.toPO(${aggregateVarName});
        if (isNew) {
            ${aggregateVarName}Mapper.insert(po);
            ${aggregateVarName}.setId(((Number) po.getId()).longValue());
        } else {
            int updatedRows = ${aggregateVarName}Mapper.updateById(po);
            if (updatedRows == 0) {
                throw new BusinessException("Concurrent update conflict for ${aggregateClassName}: " + ${aggregateVarName}.getId());
            }
        }
<#if enableEvent?? && enableEvent>
        publishDomainEvents(${aggregateVarName});
</#if>
        return ${aggregateVarName};
    }

    @Override
    public Optional<${aggregateClassName}> findById(Long id) {
        return Optional.ofNullable(${aggregateVarName}Mapper.selectById(id))
                .map(${aggregateClassName}Converter::toDomain);
    }

    @Override
    public List<${aggregateClassName}> findAll() {
        return ${aggregateVarName}Mapper.selectAll().stream()
                .map(${aggregateClassName}Converter::toDomain)
                .toList();
    }
<#if enableEvent?? && enableEvent>

    private void publishDomainEvents(${aggregateClassName} ${aggregateVarName}) {
        // ${msg.proj.context.repositoryImpl.domainEventsNote}
        if (${aggregateVarName}.getDomainEvents().isEmpty()) {
            return;
        }
        eventPublisher.publishEvents(${aggregateVarName}.getDomainEvents());
        ${aggregateVarName}.clearDomainEvents();
    }
</#if>
}
