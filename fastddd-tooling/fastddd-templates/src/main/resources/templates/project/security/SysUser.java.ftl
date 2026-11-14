package ${packageName}.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gallop.fastddd.model.po.BasePO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * ${msg.proj.security.sysuser.entityDesc}
 *
 * @author ${author}
 * @since ${date}
 */
@TableName("sys_user")
public class SysUser extends BasePO<Long> {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "${msg.proj.security.sysuser.usernameLabel}${msg.common.validation.notBlank}")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "${msg.proj.security.sysuser.usernamePattern}")
    private String username;

    @NotBlank(message = "${msg.proj.security.sysuser.passwordLabel}${msg.common.validation.notBlank}")
    @JsonIgnore
    private String password;

    @Email(message = "${msg.proj.security.sysuser.emailLabel}${msg.common.validation.patternInvalid}")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "${msg.proj.security.sysuser.phoneLabel}${msg.common.validation.patternInvalid}")
    private String phone;

    private String realName;

    private String avatar;

    private Integer status;

    private Long deptId;

    private Integer userType;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private String remark;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Integer getUserType() { return userType; }
    public void setUserType(Integer userType) { this.userType = userType; }
    public LocalDateTime getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(LocalDateTime lastLoginTime) { this.lastLoginTime = lastLoginTime; }
    public String getLastLoginIp() { return lastLoginIp; }
    public void setLastLoginIp(String lastLoginIp) { this.lastLoginIp = lastLoginIp; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
