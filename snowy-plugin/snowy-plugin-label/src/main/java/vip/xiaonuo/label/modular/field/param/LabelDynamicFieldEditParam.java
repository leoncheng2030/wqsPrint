package vip.xiaonuo.label.modular.field.param;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签打印动态字段定义编辑参数
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@Schema(description = "标签打印动态字段定义编辑参数")
public class LabelDynamicFieldEditParam {

    /** 主键ID */
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id不能为空")
    private String id;

    /** 关联模板ID */
    @Schema(description = "关联模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "templateId不能为空")
    private String templateId;

    /** 字段绑定键 (程序使用,唯一) */
    @Schema(description = "字段绑定键 (程序使用,唯一)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "fieldKey不能为空")
    private String fieldKey;

    /** 字段显示标题 (UI使用) */
    @Schema(description = "字段显示标题 (UI使用)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "title不能为空")
    private String title;

    /** 字段作用域 (用于分组) */
    @Schema(description = "字段作用域 (用于分组)")
    private String fieldScope;

    /** 输入控件类型 (如: INPUT, SELECT, DATE) */
    @Schema(description = "输入控件类型 (如: INPUT, SELECT, DATE)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "inputType不能为空")
    private String inputType;

    /** 选项来源: 关联系统字典编码 */
    @Schema(description = "选项来源: 关联系统字典编码")
    private String dictTypeCode;

    /** 选项来源: 静态JSON数据 */
    @Schema(description = "选项来源: 静态JSON数据")
    private String optionsData;

    /** 选项来源: API接口地址 */
    @Schema(description = "选项来源: API接口地址")
    private String optionApiUrl;

    /** 已选数据回显API地址 */
    @Schema(description = "已选数据回显API地址")
    private String selectedDataApiUrl;

    /** 是否支持多选 (1=是, 0=否) */
    @Schema(description = "是否支持多选 (1=是, 0=否)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "isMultiple不能为空")
    private String isMultiple;

    /** 是否必填 */
    @Schema(description = "是否必填", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "isRequired不能为空")
    private String isRequired;

    /** 验证提示 */
    @TableField("PLACEHOLDER")
    @Schema(description = "验证提示")
    private String placeholder;
    
    /** 排序码 */
    @Schema(description = "排序码")
    private Integer sortCode;
}
