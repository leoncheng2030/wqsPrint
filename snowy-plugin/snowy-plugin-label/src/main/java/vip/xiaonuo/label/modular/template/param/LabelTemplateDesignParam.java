package vip.xiaonuo.label.modular.template.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelTemplateDesignParam {
    /** 主键ID */
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id不能为空")
    private String id;

    /** 模板数据 */
    @Schema(description = "模板数据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "TEMPLATE_DATA不能为空")
    private String templateContent;
}
