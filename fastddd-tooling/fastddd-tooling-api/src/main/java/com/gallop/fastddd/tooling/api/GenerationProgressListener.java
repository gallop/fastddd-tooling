package com.gallop.fastddd.tooling.api;

/**
 * 生成进度回调契约（SPLIT-003）。
 *
 * <p>批量生成场景下，消费者实现此接口接收逐表进度通知（用于 Studio UI 进度反馈、插件日志等），
 * 而无需轮询或侵入生成器实现。默认方法为空，消费方按需覆写关心的回调。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public interface GenerationProgressListener {

    /** 单表生成开始。 */
    default void onTableStarted(String tableName) { }

    /** 单表生成成功。 */
    default void onTableCompleted(String tableName, GenerationResult result) { }

    /** 单表生成失败。 */
    default void onTableFailed(String tableName, Throwable error) { }
}
