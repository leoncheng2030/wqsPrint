package vip.xiaonuo.label.modular.template.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签打印模板分页查询参数
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@Schema(description = "标签打印模板分页查询参数")
public class LabelTemplatePageParam {

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

    /** 模板业务编码 */
    @Schema(description = "模板业务编码")
    private String code;

    /** 模板名称 */
    @Schema(description = "模板名称")
    private String name;

    /** 业务类型 */
    @Schema(description = "业务类型")
    private String type;

    /** 是否为明细模板 (1=是, 0=否) */
    @Schema(description = "是否为明细模板 (1=是, 0=否)")
    private String requiresDetails;

    /** 状态 (ENABLE=启用, DISABLE=禁用) */
    @Schema(description = "状态 (ENABLE=启用, DISABLE=禁用)")
    private String status;
}
