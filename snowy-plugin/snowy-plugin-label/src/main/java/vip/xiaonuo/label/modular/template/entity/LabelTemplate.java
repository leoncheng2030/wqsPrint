package vip.xiaonuo.label.modular.template.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import vip.xiaonuo.common.pojo.CommonEntity;

import java.util.Date;

/**
 * 标签打印模板实体
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@TableName("LABEL_TEMPLATE")
@Schema(description = "标签打印模板")
public class LabelTemplate extends CommonEntity {

    /** 主键ID */
    @Schema(description = "主键ID")
    private String id;

    /** 模板业务编码 (用户可读,唯一) */
    @Schema(description = "模板业务编码 (用户可读,唯一)")
    private String code;

    /** 模板名称 */
    @Schema(description = "模板名称")
    private String name;

    /** 业务类型 (如: MATERIAL_LABEL, SHIPPING_NOTE) */
    @Schema(description = "业务类型 (如: MATERIAL_LABEL, SHIPPING_NOTE)")
    private String type;

    /** 是否为明细模板 (1=是, 0=否) */
    @Schema(description = "是否为明细模板 (1=是, 0=否)")
    private String requiresDetails;

    /** sv-print模板配置JSON */
    @Schema(description = "sv-print模板配置JSON")
    private String templateContent;

    /** 状态 (ENABLE=启用, DISABLE=禁用) */
    @Schema(description = "状态 (ENABLE=启用, DISABLE=禁用)")
    private String status;


}
