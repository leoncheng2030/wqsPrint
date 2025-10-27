package vip.xiaonuo.label.modular.record.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签打印记录新增参数
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@Schema(description = "标签打印记录新增参数")
public class LabelPrintRecordAddParam {

    /** 关联的模板ID */
    @Schema(description = "关联的模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "templateId不能为空")
    private String templateId;

    /** 打印的数据快照 (JSON格式) */
    @Schema(description = "打印的数据快照 (JSON格式)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "printData不能为空")
    private String printData;

    /** 核心业务标识 (如订单号, 用于快速搜索) */
    @Schema(description = "核心业务标识 (如订单号, 用于快速搜索)")
    private String businessKey;
}
