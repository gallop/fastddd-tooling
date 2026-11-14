package ${subApp.packageName};

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.gallop.fastddd.web.annotation.EnableApiResponseWrapper;

/**
 * ${subApp.label} application
 *
 * @author FastDDD
 */
@SpringBootApplication(scanBasePackages = {
    "${basePackage}.common",
    "${subApp.packageName}"
})
@MapperScan({
    "${basePackage}.common"
})
@EnableApiResponseWrapper
public class ${subApp.applicationName} {

    public static void main(String[] args) {
        SpringApplication.run(${subApp.applicationName}.class, args);
    }
}
