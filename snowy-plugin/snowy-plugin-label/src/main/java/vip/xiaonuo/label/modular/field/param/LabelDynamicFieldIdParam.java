package vip.xiaonuo.label.modular.field.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签打印动态字段定义Id参数
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@Schema(description = "标签打印动态字段定义Id参数")
public class LabelDynamicFieldIdParam {

    /** 主键ID */
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id不能为空")
    private String id;
}
