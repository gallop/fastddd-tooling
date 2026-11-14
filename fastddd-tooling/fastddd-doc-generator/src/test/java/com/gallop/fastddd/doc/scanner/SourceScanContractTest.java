package com.gallop.fastddd.doc.scanner;

import com.gallop.fastddd.doc.model.ApiController;
import com.gallop.fastddd.doc.model.ApiDoc;
import com.gallop.fastddd.doc.model.ApiMethod;
import com.gallop.fastddd.doc.model.ApiParameter;
import com.gallop.fastddd.doc.parser.AnnotationParser;
import com.gallop.fastddd.doc.parser.DescriptionExtractor;
import com.gallop.fastddd.doc.parser.JavadocParser;
import com.gallop.fastddd.doc.parser.TypeExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 源码扫描链路契约：真实控制器源文件 → JavaDocScanner → ApiDoc 模型。
 *
 * 回归背景：parseFile 曾用 parse(String) 解析，CompilationUnit 无 storage，
 * getPrimaryType() 恒为空，isRestController 恒为 false，扫描结果恒为 0 个控制器
 * —— 单元测试只覆盖模型层渲染，未覆盖"源文件能被扫出"这一最小事实，缺陷因此漏网。
 */
class SourceScanContractTest {

    @TempDir Path sourceRoot;

    private static final String CONTROLLER_SOURCE = """
            package com.example.scan.controller;

            import org.springframework.web.bind.annotation.*;

            /**
             * 用户管理接口
             */
            @RestController
            @RequestMapping("/api/users")
            public class UserController {

                /**
                 * 分页查询用户（可选过滤关键词）
                 *
                 * @param keyword 过滤关键词
                 * @return 用户分页列表
                 */
                @GetMapping
                public String list(@RequestParam(name = "keyword", required = false) String keyword) {
                    return "[]";
                }

                /**
                 * 查询单个用户详情
                 *
                 * @param id 用户ID
                 * @return 用户详情
                 * @deprecated 已由 /api/users/detail 取代
                 */
                @Deprecated
                @GetMapping("/{id}")
                public String get(@PathVariable Long id) {
                    return "{}";
                }
            }
            """;

    @Test
    void sourceScanFindsControllerMethodsAndBooleanFlags() throws Exception {
        Path packageDir = sourceRoot.resolve("com/example/scan/controller");
        Files.createDirectories(packageDir);
        Files.writeString(packageDir.resolve("UserController.java"), CONTROLLER_SOURCE, StandardCharsets.UTF_8);

        AnnotationParser annotationParser = new AnnotationParser(new TypeExtractor(),
                new DescriptionExtractor(new JavadocParser()));
        JavaDocScanner scanner = new JavaDocScanner(
                new DescriptionExtractor(new JavadocParser()),
                annotationParser,
                new TypeExtractor());

        ApiDoc apiDoc = scanner.scan("com.example.scan.controller", sourceRoot.toString());

        assertThat(apiDoc.getControllers()).hasSize(1);
        ApiController controller = apiDoc.getControllers().get(0);
        assertThat(controller.getClassName()).isEqualTo("UserController");
        assertThat(controller.getMethods()).hasSize(2);

        ApiMethod list = controller.getMethods().stream()
                .filter(m -> "list".equals(m.getMethodName()))
                .findFirst().orElseThrow();
        assertThat(list.getFullPath()).isEqualTo("/api/users");
        assertThat(list.isDeprecated()).isFalse();
        assertThat(list.getParameters()).isNotEmpty();
        ApiParameter keyword = list.getParameters().get(0);
        assertThat(keyword.getName()).isEqualTo("keyword");
        assertThat(keyword.isRequired()).isFalse();

        ApiMethod get = controller.getMethods().stream()
                .filter(m -> "get".equals(m.getMethodName()))
                .findFirst().orElseThrow();
        assertThat(get.getFullPath()).isEqualTo("/api/users/{id}");
        assertThat(get.isDeprecated()).isTrue();
    }
}
