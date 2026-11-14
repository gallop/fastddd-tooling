package ${packageName};

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.gallop.fastddd.web.annotation.EnableApiResponseWrapper;

/**
 * ${artifactId} ${msg.proj.application.classDesc}
 *
 * @author ${author}
 * @date ${date}
 */
@SpringBootApplication
<#-- D-3/D-4（2026-09-13 运行期批）：Mapper 注册边界按通道三分。
     根因 1（D-3，纯 RICH）：RICH 产物把 MyBatis Mapper（@Mapper，org.apache.ibatis）、
     MapStruct 转换器（org.mapstruct.@Mapper(componentModel="spring")）与领域仓储接口
     （无注解，由 @Repository 实现类承载）混布在根包下——根包裸扫描会把后两者也注册成
     MyBatis 代理，与既有 Spring bean 同型冲突（双 bean，应用无法启动）。
     根因 2（D-4，混合）：逐表解析出 RICH 产物而项目级 ANEMIC 时，*BaseMapper（@Mapper）
     位于 <root>.infrastructure.persistence，不在贫血专属 mapperPackage 扫描范围内——
     单扫 mapperPackage 会漏注册，RepositoryImpl 注入失败（缺 bean，应用无法启动）。
     修复：纯 RICH → 根包扫描 + annotationClass 过滤（只注册真正的 MyBatis Mapper）；
     混合（richDependencies）→ 双 @MapperScan：mapperPackage 包扫描（贫血 Mapper 不带
     @Mapper，依赖包扫描）+ 根包注解过滤扫描（RICH *BaseMapper）；@MapperScan 自
     mybatis-spring 2.0.2 起 @Repeatable。纯贫血 → 维持 mapperPackage 包扫描，
     不得加 annotationClass（贫血 Mapper 均无 @Mapper，加过滤将全部失注册）。 -->
<#if modelType == "RICH">
@MapperScan(basePackages = "${packageName}", annotationClass = org.apache.ibatis.annotations.Mapper.class)
<#elseif (richDependencies!false)>
@MapperScan("${mapperPackage}")
@MapperScan(basePackages = "${packageName}", annotationClass = org.apache.ibatis.annotations.Mapper.class)
<#else>
@MapperScan("${mapperPackage}")
</#if>
@EnableApiResponseWrapper
public class ${className} {

    public static void main(String[] args) {
        SpringApplication.run(${className}.class, args);
    }
}
