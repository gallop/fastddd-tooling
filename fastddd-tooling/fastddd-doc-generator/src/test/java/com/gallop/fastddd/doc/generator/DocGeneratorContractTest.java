package com.gallop.fastddd.doc.generator;

import com.gallop.fastddd.doc.config.DocGeneratorProperties;
import com.gallop.fastddd.doc.config.FreeMarkerConfig;
import com.gallop.fastddd.doc.model.ApiController;
import com.gallop.fastddd.doc.model.ApiDoc;
import com.gallop.fastddd.doc.model.ApiMethod;
import com.gallop.fastddd.doc.model.ApiParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocGeneratorContractTest {
    @TempDir Path output;

    private ApiController controller(String name, boolean flags) {
        ApiController controller = new ApiController();
        controller.setClassName(name);
        controller.setDescription("中文接口");
        controller.setPackageName("example");
        ApiMethod method = new ApiMethod();
        method.setHttpMethod(ApiMethod.HttpMethod.GET);
        method.setFullPath("/example");
        method.setDescription("说明（没有 summary）");
        method.setDeprecated(flags);
        ApiParameter parameter = new ApiParameter();
        parameter.setName("query");
        parameter.setType("String");
        parameter.setLocation(ApiParameter.ParameterLocation.QUERY);
        parameter.setRequired(flags);
        method.addParameter(parameter);
        controller.addMethod(method);
        return controller;
    }

    @Test
    void falseFlagsMustNotBecomeDeprecatedOrRequiredAndUtf8IsPreserved() throws Exception {
        FreeMarkerConfig config = new FreeMarkerConfig();
        config.setLanguage("en");
        new DocGenerator(config.freemarkerConfiguration(), new DocGeneratorProperties())
                .generateController(controller("ExampleController", false), output.toString());
        String text = Files.readString(output.resolve("ExampleController.md"), StandardCharsets.UTF_8);
        assertThat(text).contains("中文接口", "说明（没有 summary）")
                .doesNotContain("*Required*", "*required*", "deprecated");
    }

    @Test
    void unsupportedFormatsFailBeforeCreatingOutputInsteadOfSilentlyWritingMarkdown() throws Exception {
        for (DocGeneratorProperties.DocFormat format : new DocGeneratorProperties.DocFormat[]{
                DocGeneratorProperties.DocFormat.HTML, DocGeneratorProperties.DocFormat.JSON}) {
            DocGeneratorProperties properties = new DocGeneratorProperties();
            properties.setFormat(format);
            DocGenerator generator = new DocGenerator(new FreeMarkerConfig().freemarkerConfiguration(), properties);
            Path dir = output.resolve(format.name());
            assertThatThrownBy(() -> generator.generate(new ApiDoc(), dir.toString()))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("MARKDOWN");
            assertThatThrownBy(() -> generator.generateController(controller("Test", false), dir.toString()))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("MARKDOWN");
            assertThat(Files.exists(dir)).isFalse();
        }
    }

    @Test
    void trueFlagsAndSeparateFileIndexArePreserved() throws Exception {
        FreeMarkerConfig config = new FreeMarkerConfig();
        config.setLanguage("en");
        ApiDoc doc = new ApiDoc();
        doc.setGeneratedAt(LocalDateTime.now());
        doc.addController(controller("ExampleController", true));
        new DocGenerator(config.freemarkerConfiguration(), new DocGeneratorProperties())
                .generate(doc, output.toString());
        assertThat(Files.readString(output.resolve("ExampleController.md")))
                .contains("*required*", "This endpoint is deprecated");
        assertThat(Files.readString(output.resolve("README.md")))
                .contains("](ExampleController.md)")
                .doesNotContain("](API-Documentation.md)");
    }

    @Test
    void singleFileIncludesAllControllersAndIndexDoesNotLinkMissingFiles() throws Exception {
        for (String language : new String[]{"zh", "en"}) {
            FreeMarkerConfig config = new FreeMarkerConfig();
            config.setLanguage(language);
            DocGeneratorProperties properties = new DocGeneratorProperties();
            properties.setSingleFile(true);
            ApiDoc doc = new ApiDoc();
            doc.setGeneratedAt(LocalDateTime.of(2026, 9, 13, 12, 0));
            doc.addController(controller("OneController", true));
            doc.addController(controller("TwoController", false));
            Path dir = output.resolve(language);
            new DocGenerator(config.freemarkerConfiguration(), properties).generate(doc, dir.toString());
            assertThat(Files.readString(dir.resolve("API-Documentation.md")))
                    .contains("OneController", "TwoController", "中文接口", "2026-09-13");
            assertThat(Files.readString(dir.resolve("README.md")))
                    .contains("](API-Documentation.md)")
                    .doesNotContain("](OneController.md)", "](TwoController.md)");
        }
    }
}
