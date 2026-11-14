package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class MenuSeedTemplateTest {
    @Test
    void parentIsInsertedBeforeChildrenWithoutValuesSubqueriesInBothLanguages() throws Exception {
        for (String language : new String[]{"zh", "en"}) {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
            cfg.setClassForTemplateLoading(TemplateContext.class, "/templates");
            cfg.setDefaultEncoding("UTF-8");
            TemplateMessages.apply(cfg, language);
            String sql = new TemplateRenderer(cfg).render("project/init-data.sql.ftl", Map.of(
                    "date", "2026-09-13", "artifactId", "demo", "databaseDisplayName", "MySQL",
                    "databaseType", "MYSQL"));
            assertThat(sql).doesNotContain("(SELECT id FROM sys_menu")
                    .contains("FROM sys_menu parent WHERE parent.menu_code = 'system'");
            assertThat(sql.indexOf("VALUES\n('system',")).isLessThan(sql.indexOf("SELECT 'system:user'"));
            for (String child : new String[]{"user", "role", "permission", "menu"}) {
                assertThat(sql).contains("SELECT 'system:" + child + "'");
            }
        }
    }
}
