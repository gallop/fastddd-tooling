package ${applicationPackage}.handler;

import org.springframework.stereotype.Component;

import ${applicationPackage}.query.Get${entityName}ByIdQuery;
import ${domainPackage}.${entityName};
import ${infrastructurePackage}.persistence.${entityName}Repository;
import com.gallop.fastddd.application.query.QueryHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ${msg.rich.repo.findByIdDesc}${comment!''}${msg.rich.queryHandler.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Get${entityName}ByIdQueryHandler implements QueryHandler<Get${entityName}ByIdQuery, ${entityName}> {

    private final ${entityName}Repository ${lowerEntityName}Repository;

    @Override
    public ${entityName} handle(Get${entityName}ByIdQuery query) {
        log.debug("${msg.rich.repo.findByIdDesc}${comment!''}: {}", query.getId());
        return ${lowerEntityName}Repository.findById(query.getId())
                .orElseThrow(() -> new RuntimeException("${comment!''}${msg.rich.queryHandler.notFound}: ID=" + query.getId()));
    }
}
