package ${applicationPackage};

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ${domainPackage}.${entityName};
import ${infrastructurePackage}.persistence.${entityName}Repository;
import lombok.RequiredArgsConstructor;

/**
 * ${comment!''}${msg.rich.applicationService.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Service
@RequiredArgsConstructor
public class ${entityName}ApplicationService {

    private final ${entityName}Repository ${lowerEntityName}Repository;

    /**
     * ${msg.rich.factory.create}${comment!''}
     */
    @Transactional
    public ${entityName} create(${entityName} ${lowerEntityName}) {
        return ${lowerEntityName}Repository.save(${lowerEntityName});
    }

    /**
     * ${msg.rich.repo.findByIdDesc}
     */
    public Optional<${entityName}> getById(Long id) {
        return ${lowerEntityName}Repository.findById(id);
    }

    /**
     * ${msg.rich.repo.findAllDesc}
     */
    public List<${entityName}> findAll() {
        return ${lowerEntityName}Repository.findAll();
    }

    /**
     * ${msg.rich.update.methodPrefix}${comment!''}
     */
    @Transactional
    public ${entityName} update(${entityName} ${lowerEntityName}) {
        return ${lowerEntityName}Repository.save(${lowerEntityName});
    }

    /**
     * ${msg.rich.delete.prefix}${comment!''}
     */
    @Transactional
    public void delete(Long id) {
        ${lowerEntityName}Repository.deleteById(id);
    }
}
