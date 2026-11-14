package ${packageName}.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gallop.fastddd.model.po.BasePO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * ${msg.proj.security.syspermission.entityDesc}
 *
 * @author ${author}
 * @since ${date}
 */
@TableName("sys_permission")
public class SysPermission extends BasePO<Long> {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "${msg.proj.security.syspermission.permissionNameLabel}${msg.common.validation.notBlank}")
    private String permissionName;

    @NotBlank(message = "${msg.proj.security.syspermission.permissionCodeLabel}${msg.common.validation.notBlank}")
    private String permissionCode;

    @NotNull(message = "${msg.proj.security.syspermission.resourceTypeLabel}${msg.common.validation.notBlank}")
    private Integer resourceType;

    private String path;

    private String method;

    private Long parentId;

    @NotNull(message = "${msg.proj.security.syspermission.statusLabel}${msg.common.validation.notBlank}")
    private Integer status;

    private Integer sortOrder;

    private String remark;

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public Integer getResourceType() { return resourceType; }
    public void setResourceType(Integer resourceType) { this.resourceType = resourceType; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
