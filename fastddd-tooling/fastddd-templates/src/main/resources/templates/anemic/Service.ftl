package ${servicePackage};

import ${entityPackage}.${entityName};
import com.gallop.fastddd.crud.service.IService;
import java.util.List;
import java.util.Optional;

/**
 * ${comment!entityName}${msg.anemic.service.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
public interface ${entityName}Service extends IService<${entityName}, Long> {

    // ${msg.anemic.service.crud.header}
    // - save(T entity) - ${msg.anemic.service.crud.save}
    // - saveBatch(Collection<T>) - ${msg.anemic.service.crud.saveBatch}
    // - saveOrUpdate(T entity) - ${msg.anemic.service.crud.saveOrUpdate}
    // - removeById(Long) - ${msg.anemic.service.crud.removeById}
    // - removeByIds(Collection<Long>) - ${msg.anemic.service.crud.removeByIds}
    // - updateById(T entity) - ${msg.anemic.service.crud.updateById}
    // - updateBatchById(Collection<T>) - ${msg.anemic.service.crud.updateBatchById}
    // - getById(Long) - ${msg.anemic.service.crud.getById}
    // - listByIds(Collection<Long>) - ${msg.anemic.service.crud.listByIds}
    // - getOne(Wrapper) - ${msg.anemic.service.crud.getOne}
    // - list(Wrapper) - ${msg.anemic.service.crud.listByWrapper}
    // - list() - ${msg.anemic.service.crud.listAll}
    // - page(IPage) - ${msg.anemic.service.crud.page}
    // - count() - ${msg.anemic.service.crud.count}
    // - getByField(String, Object) - ${msg.anemic.service.crud.getByField}
    // - listByField(String, Object) - ${msg.anemic.service.crud.listByField}
    // - existsByField(String, Object) - ${msg.anemic.service.crud.existsByField}
    // - countByField(String, Object) - ${msg.anemic.service.crud.countByField}
}
