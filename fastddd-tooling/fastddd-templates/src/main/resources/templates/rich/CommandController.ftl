package ${applicationPackage}.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ${applicationPackage}.command.Create${entityName}Command;
import ${applicationPackage}.handler.Create${entityName}CommandHandler;
import ${domainPackage}.${entityName};
import com.gallop.fastddd.web.controller.CommandController;
import lombok.RequiredArgsConstructor;

/**
 * ${entityName}${msg.rich.commandController.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@RestController
@RequestMapping("/api/${entityName?lower_case}")
@RequiredArgsConstructor
public class ${entityName}CommandController extends CommandController<Create${entityName}Command, ${entityName}> {

    private final Create${entityName}CommandHandler createCommandHandler;

    @Override
    protected Create${entityName}CommandHandler getCommandHandler() {
        return createCommandHandler;
    }
}
