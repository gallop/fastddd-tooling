package ${applicationPackage}.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ${applicationPackage}.query.Get${entityName}ByIdQuery;
import ${applicationPackage}.handler.Get${entityName}ByIdQueryHandler;
import ${domainPackage}.${entityName};
import com.gallop.fastddd.web.controller.QueryController;
import com.gallop.fastddd.application.query.QueryHandler;
import lombok.RequiredArgsConstructor;

/**
 * ${comment!''}${msg.rich.queryController.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@RestController
@RequestMapping("/api/${entityName?lower_case}")
@RequiredArgsConstructor
public class ${entityName}QueryController extends QueryController<Get${entityName}ByIdQuery, ${entityName}> {

    private final Get${entityName}ByIdQueryHandler get${entityName}ByIdQueryHandler;

    @Override
    protected QueryHandler<Get${entityName}ByIdQuery, ${entityName}> getQueryHandler() {
        return get${entityName}ByIdQueryHandler;
    }
}
