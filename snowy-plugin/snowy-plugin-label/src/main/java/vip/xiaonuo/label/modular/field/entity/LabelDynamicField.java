package vip.xiaonuo.label.modular.field.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import vip.xiaonuo.common.pojo.CommonEntity;


/**
 * 标签打印动态字段定义实体
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@TableName("LABEL_DYNAMIC_FIELD")
@Schema(description = "标签打印动态字段定义")
public class LabelDynamicField extends CommonEntity {

    /** 主键ID */
    @TableId("ID")
    @Schema(description = "主键ID")
    private String id;

    /** 关联模板ID */
    @TableField("TEMPLATE_ID")
    @Schema(description = "关联模板ID")
    private String templateId;

    /** 字段绑定键 (程序使用,唯一) */
    @TableField("FIELD_KEY")
    @Schema(description = "字段绑定键 (程序使用,唯一)")
    private String fieldKey;

    /** 字段显示标题 (UI使用) */
    @TableField("TITLE")
    @Schema(description = "字段显示标题 (UI使用)")
    private String title;

    /** 字段作用域 (用于分组) */
    @TableField("FIELD_SCOPE")
    @Schema(description = "字段作用域 (用于分组)")
    private String fieldScope;

    /** 输入控件类型 (如: INPUT, SELECT, DATE) */
    @TableField("INPUT_TYPE")
    @Schema(description = "输入控件类型 (如: INPUT, SELECT, DATE)")
    private String inputType;

    /** 选项来源: 关联系统字典编码 */
    @TableField("DICT_TYPE_CODE")
    @Schema(description = "选项来源: 关联系统字典编码")
    private String dictTypeCode;

    /** 选项来源: 静态JSON数据 */
    @TableField("OPTIONS_DATA")
    @Schema(description = "选项来源: 静态JSON数据")
    private String optionsData;

    /** 选项来源: API接口地址 */
    @TableField("OPTION_API_URL")
    @Schema(description = "选项来源: API接口地址")
    private String optionApiUrl;

    /** 已选数据回显API地址 */
    @TableField("SELECTED_DATA_API_URL")
    @Schema(description = "已选数据回显API地址")
    private String selectedDataApiUrl;

    /** 是否支持多选 (1=是, 0=否) */
    @TableField("IS_MULTIPLE")
    @Schema(description = "是否支持多选 (1=是, 0=否)")
    private String isMultiple;

    /** 是否必填 (1=必填, 0=非必填) */
    @TableField("IS_REQUIRED")
    @Schema(description = "是否必填 (1=必填, 0=非必填)")
    private String isRequired;

    /** 验证提示 */
    @TableField("PLACEHOLDER")
    @Schema(description = "验证提示")
    private String placeholder;

    /** 状态 (ENABLE=启用, DISABLE=禁用) */
    @TableField("STATUS")
    @Schema(description = "状态 (ENABLE=启用, DISABLE=禁用)")
    private String status;
    
    /** 排序码 */
    @TableField("SORT_CODE")
    @Schema(description = "排序码")
    private Integer sortCode;

}
