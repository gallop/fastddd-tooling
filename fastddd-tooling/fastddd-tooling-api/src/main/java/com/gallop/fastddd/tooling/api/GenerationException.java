package com.gallop.fastddd.tooling.api;

/**
 * Tooling 生成过程中的语义错误（SPLIT-003）。
 *
 * <p>API 实现抛出此受检语义异常以取代生成器内部的具体运行期异常泄漏
 * （如 {@code RuntimeException}、模板引擎异常），保持 API 二进制面的异常契约稳定。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public class GenerationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GenerationException(String message) {
        super(message);
    }

    public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
