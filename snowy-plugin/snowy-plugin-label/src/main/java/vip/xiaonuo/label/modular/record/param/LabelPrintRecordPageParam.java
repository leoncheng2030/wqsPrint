package vip.xiaonuo.label.modular.record.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签打印记录分页查询参数
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@Schema(description = "标签打印记录分页查询参数")
public class LabelPrintRecordPageParam {

    /** 当前页 */
    @Schema(description = "当前页")
    private Integer current;

    /** 每页条数 */
    @Schema(description = "每页条数")
    private Integer size;

    /** 排序字段 */
    @Schema(description = "排序字段")
    private String sortField;

    /** 排序方式 */
    @Schema(description = "排序方式")
    private String sortOrder;

    /** 关联的模板ID */
    @Schema(description = "关联的模板ID")
    private String templateName;

    /** 核心业务标识 (如订单号, 用于快速搜索) */
    @Schema(description = "核心业务标识 (如订单号, 用于快速搜索)")
    private String businessKey;

    /** 打印状态 (COMPLETED=已完成, FAILED=失败) */
    @Schema(description = "打印状态 (COMPLETED=已完成, FAILED=失败)")
    private String printStatus;
}
