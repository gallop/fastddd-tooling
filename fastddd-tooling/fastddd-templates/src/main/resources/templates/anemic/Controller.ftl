package ${controllerPackage};

import ${servicePackage}.${table.entityName}Service;
import ${entityPackage}.${table.entityName};
import com.gallop.fastddd.web.controller.BaseReadOnlyController;
<#if writeMode?? && writeMode == "DTO">
import ${dtoPackage}.Create${table.entityName}Request;
import ${dtoPackage}.Update${table.entityName}Request;
import com.gallop.fastddd.web.response.ApiResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
</#if>

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ${comment!''}${msg.anemic.controller.suffix}
 *
 * <p>${msg.anemic.controller.readonly.note1}
 * ${msg.anemic.controller.readonly.note2}
 * ${msg.anemic.controller.readonly.note3}
 *
 * <#if writeMode?? && writeMode == "DTO"><p>${msg.anemic.controller.writeMode.note1}
 * ${msg.anemic.controller.writeMode.reqPrefix}{@code Create${table.entityName}Request}/{@code Update${table.entityName}Request}${msg.anemic.controller.writeMode.reqSuffix}
 * ${msg.anemic.controller.writeMode.note2}
 * ${msg.anemic.controller.writeMode.note3}</p></#if>
 *
 * <h3>${msg.anemic.controller.extpt.title}</h3>
 * <p>${msg.anemic.controller.extpt.note1}
 * ${msg.anemic.controller.extpt.note2}
 * ${msg.anemic.controller.extpt.note3}
 * ${msg.anemic.controller.extpt.note4}
 * ${msg.anemic.controller.extpt.note5}</p>
 * <ul>
 *   <li>${msg.anemic.controller.extpt.classAuth}</li>
 *   <li>${msg.anemic.controller.extpt.methodAuthPrefix}{@code // @@PreAuthorize("hasAuthority('${lowerEntityName}:write')")}</li>
 *   <li>${msg.anemic.controller.extpt.dataAuth}
 *       {@code // @@DataPermission(dimension = "${tableName}", keyField = "id")}
 *       ${msg.anemic.controller.extpt.dataAuthHint}</li>
 * </ul>
 *
 * @author ${author}
 * @date ${date}
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html">Spring Security Method Security</a>
 */
// @@PreAuthorize("hasRole('ADMIN')")            // GEN-002-EXTPT: ${msg.anemic.controller.extpt.classAuthHint}
@RestController
@RequestMapping("/api/${table.entityName?lower_case}")
public class ${table.entityName}Controller extends BaseReadOnlyController<${table.entityName}Service, ${table.entityName}, Long> {

    // ${msg.anemic.controller.readonly.ifaceComment}
    // - GET /api/${table.entityName?lower_case}/{id}  ${msg.anemic.controller.readonly.getByIdDesc}
    // - GET /api/${table.entityName?lower_case}       ${msg.anemic.controller.readonly.listDesc}
    // - GET /api/${table.entityName?lower_case}/page  ${msg.anemic.controller.readonly.pageDesc}
    // GEN-002-EXTPT: ${msg.anemic.controller.readonly.queryAuthHint}
<#if writeMode?? && writeMode == "DTO">

    // @@DataPermission(dimension = "${tableName}", keyField = "id")
    // GEN-002-EXTPT: ${msg.anemic.controller.extpt.dataPermHint}

    /**
     * ${msg.anemic.controller.create.methodPrefix}${comment!''}${msg.anemic.controller.descSuffix}
     *
     * @param request ${msg.anemic.controller.create.paramRequest}
     * @return ${msg.anemic.controller.create.returnDesc}
     */
    // @@PreAuthorize("hasAuthority('${lowerEntityName}:create')")  // GEN-002-EXTPT
    @PostMapping
    public ResponseEntity<ApiResponse<${table.entityName}>> create(@Valid @RequestBody Create${table.entityName}Request request) {
        ${table.entityName} entity = new ${table.entityName}();
        BeanUtils.copyProperties(request, entity);
        service.save(entity);
        return ResponseEntity.ok(ApiResponse.success(entity));
    }

    // @@DataPermission(dimension = "${tableName}", keyField = "id")
    /**
     * ${msg.anemic.controller.update.methodPrefix}${comment!''}${msg.anemic.controller.descSuffix}
     *
     * @param id      ${msg.anemic.controller.update.paramId}
     * @param request ${msg.anemic.controller.update.paramRequest}
     * @return ${msg.anemic.controller.update.returnDesc}
     */
    // @@PreAuthorize("hasAuthority('${lowerEntityName}:update')")  // GEN-002-EXTPT
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<${table.entityName}>> update(@PathVariable("id") String id,
                                                                   @Valid @RequestBody Update${table.entityName}Request request) {
        ${table.entityName} entity = new ${table.entityName}();
        BeanUtils.copyProperties(request, entity);
        entity.setId(parseId(id));
        service.updateById(entity);
        return ResponseEntity.ok(ApiResponse.success(entity));
    }

    /**
     * ${msg.anemic.controller.delete.methodPrefix}${comment!''}${msg.anemic.controller.descSuffix}
     *
     * @param id ${msg.anemic.controller.update.paramId}
     * @return ${msg.anemic.controller.delete.returnDesc}
     */
    // @@PreAuthorize("hasAuthority('${lowerEntityName}:delete')")  // GEN-002-EXTPT
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") String id) {
        service.removeById(parseId(id));
        return ResponseEntity.ok(ApiResponse.success());
    }
</#if>
}
