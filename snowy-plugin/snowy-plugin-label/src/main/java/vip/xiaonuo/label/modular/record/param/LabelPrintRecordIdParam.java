package vip.xiaonuo.label.modular.record.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签打印记录Id参数
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@Schema(description = "标签打印记录Id参数")
public class LabelPrintRecordIdParam {

    /** 主键ID */
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id不能为空")
    private String id;
}
