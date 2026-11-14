package ${serviceImplPackage};

import ${servicePackage}.${entityName}Service;
import ${entityPackage}.${entityName};
import ${mapperPackage}.${entityName}Mapper;
import com.gallop.fastddd.crud.service.impl.BaseServiceImpl;
import com.gallop.fastddd.crud.repository.BaseRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ${comment!entityName}Service${msg.anemic.serviceImpl.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Service
public class ${entityName}ServiceImpl extends BaseServiceImpl<${entityName}Mapper, ${entityName}, Long> implements ${entityName}Service {

    private static final Logger log = LoggerFactory.getLogger(${entityName}ServiceImpl.class);

    // ${msg.anemic.serviceImpl.note1}
    // ${msg.anemic.serviceImpl.note2}
}
