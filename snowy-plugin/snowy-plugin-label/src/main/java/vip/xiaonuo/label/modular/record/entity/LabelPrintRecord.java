package vip.xiaonuo.label.modular.record.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import vip.xiaonuo.common.pojo.CommonEntity;
import vip.xiaonuo.label.modular.template.entity.LabelTemplate;

import java.util.Date;

/**
 * 标签打印记录实体
 *
 * @author Gemini
 * @date 2025-07-20
 */
@Data
@TableName("LABEL_PRINT_RECORD")
@Schema(description = "标签打印记录")
public class LabelPrintRecord extends CommonEntity {

    /** 主键ID */
    @Schema(description = "主键ID")
    private String id;

    /** 关联的模板ID */
    @Schema(description = "关联的模板ID")
    @Trans(type = TransType.SIMPLE, target = LabelTemplate.class, fields = "name", alias = "template")
    private String templateId;

    /** 打印的数据快照 (JSON格式) */
    @Schema(description = "打印的数据快照 (JSON格式)")
    private String printData;

    /** 核心业务标识 (如订单号, 用于快速搜索) */
    @Schema(description = "核心业务标识 (如订单号, 用于快速搜索)")
    private String businessKey;

    /** 打印状态 (COMPLETED=已完成, FAILED=失败) */
    @Schema(description = "打印状态 (COMPLETED=已完成, FAILED=失败)")
    private String printStatus;

    /** 打印次数 */
    @Schema(description = "打印次数")
    private Integer printCount;

}
