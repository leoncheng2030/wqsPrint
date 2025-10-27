package vip.xiaonuo.label.modular.field.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签打印动态字段定义分页查询参数
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@Schema(description = "标签打印动态字段定义分页查询参数")
public class LabelDynamicFieldPageParam {

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

    /** 字段绑定键 (程序使用,唯一) */
    @Schema(description = "字段绑定键 (程序使用,唯一)")
    private String fieldKey;

    /** 字段显示标题 (UI使用) */
    @Schema(description = "字段显示标题 (UI使用)")
    private String title;

    /** 字段作用域 (用于分组) */
    @Schema(description = "字段作用域 (用于分组)")
    private String fieldScope;

    /** 输入控件类型 (如: INPUT, SELECT, DATE) */
    @Schema(description = "输入控件类型 (如: INPUT, SELECT, DATE)")
    private String inputType;

    /** 状态 (ENABLE=启用, DISABLE=禁用) */
    @Schema(description = "状态 (ENABLE=启用, DISABLE=禁用)")
    private String status;

    /** 关联模板ID */
    @Schema(description = "关联模板ID")
    private String templateId;
}
