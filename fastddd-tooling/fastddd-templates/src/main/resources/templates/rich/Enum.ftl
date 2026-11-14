package ${domainPackage};

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ${comment!''}${msg.rich.enum.suffix}
 *
 * @author ${author}
 * @date ${date}
 */
@Getter
@AllArgsConstructor
public enum ${entityName}Status {

    /**
     * ${msg.rich.enum.status.pending}
     */
    PENDING(0, "${msg.rich.enum.status.pending}"),

    /**
     * ${msg.rich.enum.status.active}
     */
    ACTIVE(1, "${msg.rich.enum.status.active}"),

    /**
     * ${msg.rich.enum.status.disabled}
     */
    DISABLED(2, "${msg.rich.enum.status.disabled}"),

    /**
     * ${msg.rich.enum.status.deleted}
     */
    DELETED(9, "${msg.rich.enum.status.deleted}");

    private final Integer code;
    private final String desc;

    /**
     * ${msg.rich.enum.fromCodeDesc}
     *
     * @param code ${msg.rich.enum.codeParam}
     * @return ${msg.rich.enum.fromCodeReturn}
     */
    public static ${entityName}Status fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (${entityName}Status status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * ${msg.rich.enum.isValidCodeDesc}
     *
     * @param code ${msg.rich.enum.codeParam}
     * @return ${msg.rich.enum.isValidCodeReturn}
     */
    public static boolean isValidCode(Integer code) {
        return fromCode(code) != null;
    }
}
