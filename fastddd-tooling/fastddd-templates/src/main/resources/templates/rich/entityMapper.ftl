package ${infrastructurePackage}.persistence;

import ${domainPackage}.${entityName};
import org.mapstruct.Mapper;
import com.gallop.fastddd.crud.entity.EntityMapper;

/**
 * ${comment!''}${msg.rich.entityMapper.suffix}
 * ${msg.rich.entityMapper.usage}
 *
 * @author ${author}
 * @date ${date}
 */
@Mapper(componentModel = "spring")
public interface ${entityName}Mapper extends EntityMapper<${entityName}, ${entityName}PO> {

    /**
     * ${msg.rich.entityMapper.toEntityDesc}
     */
    @Override
    ${entityName} toEntity(${entityName}PO po);

    /**
     * ${msg.rich.entityMapper.toPoDesc}
     */
    @Override
    ${entityName}PO toPo(${entityName} entity);
}
