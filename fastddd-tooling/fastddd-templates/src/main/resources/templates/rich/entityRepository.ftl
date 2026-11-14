package ${infrastructurePackage}.persistence;

import java.util.List;
import java.util.Optional;

import ${domainPackage}.${entityName};
import com.gallop.fastddd.crud.entity.EntityRepository;

/**
 * ${comment!''}${msg.rich.repository.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
public interface ${entityName}Repository extends EntityRepository<${entityName}, Long> {

    /**
     * ${msg.rich.repo.findByIdDesc}
     */
    @Override
    Optional<${entityName}> findById(Long id);

    /**
     * ${msg.rich.repo.findAllDesc}
     */
    List<${entityName}> findAll();

    /**
     * ${msg.rich.repo.saveDesc}
     */
    @Override
    ${entityName} save(${entityName} entity);

    /**
     * ${msg.rich.repo.deleteByIdDesc}
     */
    void deleteById(Long id);
}
