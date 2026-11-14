package com.gallop.fastddd.doc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API文档描述注解
 * 用于为API端点、控制器、参数添加描述信息
 * 可以作为Javadoc的替代或补充
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Target({
        ElementType.TYPE,        // 类、接口（用于Controller）
        ElementType.METHOD,      // 方法（用于API端点）
        ElementType.PARAMETER,   // 参数（用于方法参数）
        ElementType.FIELD        // 字段（用于DTO字段）
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDescription {

    /**
     * 描述内容
     */
    String value();

    /**
     * 详细描述（可选）
     * 如果设置，value将作为简短描述，detail作为详细描述
     */
    String detail() default "";

    /**
     * 示例值（可选）
     * 用于参数或字段，提供示例值
     */
    String example() default "";

    /**
     * 是否已废弃（可选）
     */
    boolean deprecated() default false;
}
