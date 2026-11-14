package ${packageName}.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gallop.fastddd.model.po.BasePO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * ${msg.proj.security.sysrole.entityDesc}
 *
 * @author ${author}
 * @since ${date}
 */
@TableName("sys_role")
public class SysRole extends BasePO<Long> {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "${msg.proj.security.sysrole.roleNameLabel}${msg.common.validation.notBlank}")
    private String roleName;

    @NotBlank(message = "${msg.proj.security.sysrole.roleCodeLabel}${msg.common.validation.notBlank}")
    private String roleCode;

    private String description;

    @NotNull(message = "${msg.proj.security.sysrole.statusLabel}${msg.common.validation.notBlank}")
    private Integer status;

    private Integer sortOrder;

    private String remark;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
