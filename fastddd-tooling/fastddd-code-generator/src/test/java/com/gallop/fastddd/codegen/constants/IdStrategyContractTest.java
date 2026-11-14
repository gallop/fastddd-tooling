package com.gallop.fastddd.codegen.constants;

import com.baomidou.mybatisplus.annotation.IdType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ID-001 特征测试：锁定 {@link IdStrategy} 代码生成侧枚举的当前行为。
 *
 * <p>刻画现状（不改生产行为）：{@code IdStrategy.fromCode} 在 code 为 null/空白/未知时
 * <b>默认返回 {@link IdStrategy#AUTO}</b>，而框架运行期 {@code BasePO} 实际为
 * {@code ASSIGN_ID}——代码生成默认策略与框架基类策略存在偏离（见 ID-001 取证）。
 * {@link IdStrategy#NONE} 的 {@code idType} 为 {@code null}。
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class IdStrategyContractTest {

    @Nested
    @DisplayName("fromCode 默认值：null/空白/未知 → AUTO（与 BasePO 的 ASSIGN_ID 偏离）")
    class FromCodeDefault {

        @Test
        @DisplayName("null code → AUTO")
        void nullCode_returnsAuto() {
            assertThat(IdStrategy.fromCode(null)).isEqualTo(IdStrategy.AUTO);
        }

        @Test
        @DisplayName("空白 code → AUTO")
        void blankCode_returnsAuto() {
            assertThat(IdStrategy.fromCode("   ")).isEqualTo(IdStrategy.AUTO);
        }

        @Test
        @DisplayName("未知 code → AUTO")
        void unknownCode_returnsAuto() {
            assertThat(IdStrategy.fromCode("STRANGE")).isEqualTo(IdStrategy.AUTO);
        }

        @Test
        @DisplayName("显式 ASSIGN_ID code → ASSIGN_ID")
        void explicitAssignId() {
            assertThat(IdStrategy.fromCode("ASSIGN_ID")).isEqualTo(IdStrategy.ASSIGN_ID);
        }
    }

    @Nested
    @DisplayName("IdType 映射")
    class IdTypeMapping {

        @Test
        @DisplayName("AUTO → IdType.AUTO")
        void autoMaps() {
            assertThat(IdStrategy.AUTO.getIdType()).isEqualTo(IdType.AUTO);
        }

        @Test
        @DisplayName("ASSIGN_ID → IdType.ASSIGN_ID")
        void assignIdMaps() {
            assertThat(IdStrategy.ASSIGN_ID.getIdType()).isEqualTo(IdType.ASSIGN_ID);
        }

        @Test
        @DisplayName("INPUT → IdType.INPUT")
        void inputMaps() {
            assertThat(IdStrategy.INPUT.getIdType()).isEqualTo(IdType.INPUT);
        }

        @Test
        @DisplayName("NONE → null（交由全局配置）")
        void noneMapsToNull() {
            // 刻画：NONE 的 idType 为 null，生成 @TableId 无 type 属性
            assertThat(IdStrategy.NONE.getIdType()).isNull();
        }
    }

    @Nested
    @DisplayName("isValid 校验")
    class IsValid {

        @Test
        void validCodes() {
            assertThat(IdStrategy.isValid("AUTO")).isTrue();
            assertThat(IdStrategy.isValid("ASSIGN_ID")).isTrue();
            assertThat(IdStrategy.isValid("INPUT")).isTrue();
            assertThat(IdStrategy.isValid("NONE")).isTrue();
        }

        @Test
        void invalidCodes() {
            assertThat(IdStrategy.isValid(null)).isFalse();
            assertThat(IdStrategy.isValid("")).isFalse();
            assertThat(IdStrategy.isValid("auto_lower")).isFalse();
        }
    }
}
