package ${contextInterfacesPkg};

import ${contextApplicationPkg}.${aggregateClassName}Service;
import ${contextDomainPkg}.${aggregateClassName};
import ${contextDtoPkg}.${aggregateClassName}Response;
import com.gallop.fastddd.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ${aggregateClassName} ${msg.proj.context.controller.desc}
 *
 * @author ${author}
 * @date ${date}
 */
@Tag(name = "${aggregateClassName}s")
@RestController
@RequestMapping("/api/${aggregateVarName}s")
public class ${aggregateClassName}Controller {

    private final ${aggregateClassName}Service ${aggregateVarName}Service;

    public ${aggregateClassName}Controller(${aggregateClassName}Service ${aggregateVarName}Service) {
        this.${aggregateVarName}Service = ${aggregateVarName}Service;
    }

    @Operation(summary = "Create ${aggregateClassName}")
    @PostMapping
    public ResponseEntity<ApiResponse<${aggregateClassName}Response>> create(
            @RequestBody Create${aggregateClassName}Request request) {
        ${aggregateClassName} ${aggregateVarName} = ${aggregateVarName}Service.create(request.name());
        return ResponseEntity.ok(ApiResponse.success(${aggregateClassName}Response.from(${aggregateVarName})));
    }

    @Operation(summary = "Get ${aggregateClassName} by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<${aggregateClassName}Response>> getById(@PathVariable Long id) {
        ${aggregateClassName} ${aggregateVarName} = ${aggregateVarName}Service.findById(id);
        return ResponseEntity.ok(ApiResponse.success(${aggregateClassName}Response.from(${aggregateVarName})));
    }

    @Operation(summary = "List all ${aggregateClassName}s")
    @GetMapping
    public ResponseEntity<ApiResponse<List<${aggregateClassName}Response>>> findAll() {
        List<${aggregateClassName}Response> responses = ${aggregateVarName}Service.findAll().stream()
                .map(${aggregateClassName}Response::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * ${msg.proj.context.controller.createRequestDesc} (inner class for scaffold simplicity)
     */
    public record Create${aggregateClassName}Request(String name) {
    }
}
