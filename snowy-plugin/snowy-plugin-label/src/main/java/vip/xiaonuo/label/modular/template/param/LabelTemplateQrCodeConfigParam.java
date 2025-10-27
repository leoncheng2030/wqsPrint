package vip.xiaonuo.label.modular.template.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelTemplateQrCodeConfigParam {
    /** 主键ID */
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id不能为空")
    private String id;

    /** 二维码配置 */
    @Schema(description = "二维码配置")
    private String qrCodeConfig;
}
