package ${infrastructurePackage}.persistence;

import ${domainPackage}.${entityName};
import org.apache.ibatis.annotations.Mapper;

/**
 * ${entityName}${msg.rich.entityBaseMapper.desc}
 * ${msg.rich.entityBaseMapper.usage}
 *
 * @author ${author}
 * @date ${date}
 */
@Mapper
public interface ${entityName}BaseMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<${entityName}PO> {

    // ${msg.rich.entityBaseMapper.providedMethods}
    // - insert(entity): ${msg.rich.entityBaseMapper.insertDesc}
    // - deleteById(id): ${msg.rich.entityBaseMapper.deleteByIdDesc}
    // - updateById(entity): ${msg.rich.entityBaseMapper.updateByIdDesc}
    // - selectById(id): ${msg.rich.entityBaseMapper.selectByIdDesc}
    // - selectList(wrapper): ${msg.rich.entityBaseMapper.selectListDesc}
    // - selectPage(page, wrapper): ${msg.rich.entityBaseMapper.selectPageDesc}

    // ${msg.rich.entityBaseMapper.customSqlHint}
}
