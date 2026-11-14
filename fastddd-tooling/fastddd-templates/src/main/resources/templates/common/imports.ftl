<#--
  通用导入模板
  根据配置动态生成导入语句

  使用方法：
  <#include "/common/imports.ftl"/>
-->
<#-- Java 标准库 -->
<#if useJavaTime?? && useJavaTime>
import java.time.*;
import java.time.format.DateTimeFormatter;
</#if>
<#if useBigDecimal?? && useBigDecimal>
import java.math.BigDecimal;
</#if>
<#if useList?? && useList>
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
</#if>
<#if useStream?? && useStream>
import java.util.stream.Collectors;
</#if>
<#if useOptional?? && useOptional>
import java.util.Optional;
</#if>

<#-- Lombok -->
<#if useLombok?? && useLombok>
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.RequiredArgsConstructor;
</#if>

<#-- OpenAPI 3 (Swagger) -->
<#if useSwagger?? && useSwagger>
import io.swagger.v3.oas.annotations.media.Schema;
</#if>

<#-- FastDDD 基础类 -->
<#if useBaseEntity?? && useBaseEntity>
import com.gallop.fastddd.model.BaseEntity;
</#if>
<#if useBasePO?? && useBasePO>
import com.gallop.fastddd.model.po.BasePO;
</#if>
<#if useAggregateRoot?? && useAggregateRoot>
import com.gallop.fastddd.common.domain.AggregateRoot;
import com.gallop.fastddd.common.domain.DomainEvent;
</#if>

<#-- MyBatis Plus -->
<#if useMyBatisPlus?? && useMyBatisPlus>
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
</#if>

<#-- Jakarta Validation -->
<#if useValidation?? && useValidation>
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
</#if>

<#-- Spring Web -->
<#if useRestController?? && useRestController>
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ModelAttribute;
</#if>
<#if useRequestMapping?? && useRequestMapping>
import org.springframework.web.bind.annotation.RequestMapping;
</#if>
<#if useRequestBody?? && useRequestBody>
import org.springframework.web.bind.annotation.RequestBody;
</#if>
<#if usePathVariable?? && usePathVariable>
import org.springframework.web.bind.annotation.PathVariable;
</#if>
<#if useRequestParam?? && useRequestParam>
import org.springframework.web.bind.annotation.RequestParam;
</#if>
<#if useAutowired?? && useAutowired>
import org.springframework.beans.factory.annotation.Autowired;
</#if>
<#if useService?? && useService>
import org.springframework.stereotype.Service;
</#if>
<#if useComponent?? && useComponent>
import org.springframework.stereotype.Component;
</#if>

<#-- FastDDD Web -->
<#if useApiResponse?? && useApiResponse>
import com.gallop.fastddd.common.web.response.ApiResponse;
</#if>
<#if useBaseController?? && useBaseController>
import com.gallop.fastddd.web.controller.BaseController;
</#if>

<#-- FastDDD Application -->
<#if useCommand?? && useCommand>
import com.gallop.fastddd.application.command.Command;
</#if>
<#if useQuery?? && useQuery>
import com.gallop.fastddd.application.query.Query;
</#if>
<#if useCommandHandler?? && useCommandHandler>
import com.gallop.fastddd.application.command.CommandHandler;
</#if>
<#if useQueryHandler?? && useQueryHandler>
import com.gallop.fastddd.application.query.QueryHandler;
</#if>
<#if useCommandController?? && useCommandController>
import com.gallop.fastddd.web.controller.CommandController;
</#if>
<#if useQueryController?? && useQueryController>
import com.gallop.fastddd.web.controller.QueryController;
</#if>
