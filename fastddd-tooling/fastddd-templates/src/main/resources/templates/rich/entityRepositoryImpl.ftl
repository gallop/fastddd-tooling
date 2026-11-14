package ${infrastructurePackage}.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.gallop.fastddd.common.domain.AggregateRoot;
import ${domainPackage}.${entityName};
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ${comment!''}${msg.rich.repositoryImpl.suffix}
 *
 * <p>${msg.rich.repositoryImpl.implementsPrefix} {@link com.gallop.fastddd.crud.entity.EntityRepository}${msg.rich.repositoryImpl.allAbstractMethods}save /
 * saveAndPublishEvents / findById / findAll / findAllById / delete(E) / deleteAll(...) /
 * deleteAll() / existsById / count / refresh / flush${msg.rich.repositoryImpl.eventsPending} EventPublisher${msg.rich.repositoryImpl.integrationSuffix}
 * ${msg.rich.repositoryImpl.logEventsNote} CLAUDE.md${msg.rich.repositoryImpl.guideEnd}</p>
 *
 * @author ${author}
 * @date ${date}
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ${entityName}RepositoryImpl implements ${entityName}Repository {

    private final ${entityName}Mapper ${lowerEntityName}Mapper;
    private final ${entityName}BaseMapper ${lowerEntityName}BaseMapper;

    @Override
    public ${entityName} save(${entityName} entity) {
        ${entityName}PO po = ${lowerEntityName}Mapper.toPo(entity);
        if (po.getId() == null) {
            ${lowerEntityName}BaseMapper.insert(po);
        } else {
            ${lowerEntityName}BaseMapper.updateById(po);
        }
        return ${lowerEntityName}Mapper.toEntity(po);
    }

    @Override
    public ${entityName} saveAndPublishEvents(${entityName} entity) {
        ${entityName} saved = save(entity);
        // TODO EventPublisher ${msg.rich.repositoryImpl.todoPublishReady}CLAUDE.md${msg.rich.repositoryImpl.todoGuideEnd}
        if (entity instanceof AggregateRoot<?> root) {
            root.getDomainEvents().forEach(event -> log.info("${msg.rich.repositoryImpl.domainEventLabel}: {}", event));
            root.clearDomainEvents();
        }
        return saved;
    }

    @Override
    public Optional<${entityName}> findById(Long id) {
        ${entityName}PO po = ${lowerEntityName}BaseMapper.selectById(id);
        return po != null ? Optional.of(${lowerEntityName}Mapper.toEntity(po)) : Optional.empty();
    }

    @Override
    public List<${entityName}> findAll() {
        LambdaQueryWrapper<${entityName}PO> wrapper = new LambdaQueryWrapper<>();
        return ${lowerEntityName}BaseMapper.selectList(wrapper).stream()
                .map(${lowerEntityName}Mapper::toEntity)
                .toList();
    }

    @Override
    public List<${entityName}> findAllById(Iterable<Long> ids) {
        List<Long> idList = new ArrayList<>();
        ids.forEach(idList::add);
        if (idList.isEmpty()) {
            return Collections.emptyList();
        }
        return ${lowerEntityName}BaseMapper.selectBatchIds(idList).stream()
                .map(${lowerEntityName}Mapper::toEntity)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        ${lowerEntityName}BaseMapper.deleteById(id);
    }

    @Override
    public void delete(${entityName} entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteAll(Iterable<? extends ${entityName}> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        ${lowerEntityName}BaseMapper.delete(new LambdaQueryWrapper<>());
    }

    @Override
    public boolean existsById(Long id) {
        return ${lowerEntityName}BaseMapper.selectById(id) != null;
    }

    @Override
    public long count() {
        return ${lowerEntityName}BaseMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    public void refresh(${entityName} entity) {
        // TODO ${msg.rich.repositoryImpl.refreshTodo}
        findById(entity.getId());
    }

    @Override
    public void flush() {
        // ${msg.rich.repositoryImpl.flushNote}
    }
}
