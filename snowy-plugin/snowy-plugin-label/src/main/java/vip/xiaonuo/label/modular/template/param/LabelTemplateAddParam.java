package vip.xiaonuo.label.modular.template.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签打印模板新增参数
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@Schema(description = "标签打印模板新增参数")
public class LabelTemplateAddParam {

    /** 模板业务编码 */
    @Schema(description = "模板业务编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "CODE不能为空")
    private String code;

    /** 模板名称 */
    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "NAME不能为空")
    private String name;

    /** 业务类型 */
    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "TYPE不能为空")
    private String type;

    /** 是否为明细模板 (1=是, 0=否) */
    @Schema(description = "是否为明细模板 (1=是, 0=否)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "REQUIRES_DETAILS不能为空")
    private String requiresDetails;
}
