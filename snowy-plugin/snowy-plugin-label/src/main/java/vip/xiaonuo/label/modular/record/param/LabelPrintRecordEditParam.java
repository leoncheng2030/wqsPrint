/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vip.xiaonuo.label.modular.record.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 打印记录编辑参数
 *
 * @author 
 * @date 2024/07/22 15:03
 **/
@Data
@Schema(description = "打印记录编辑参数")
public class LabelPrintRecordEditParam {

    /** 主键ID */
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "id不能为空")
    private String id;

    /** 关联的模板ID */
    @Schema(description = "关联的模板ID")
    private String templateId;

    /** 打印的数据快照 (JSON格式) */
    @Schema(description = "打印的数据快照 (JSON格式)")
    private String printData;

    /** 打印次数 */
    @Schema(description = "打印次数")
    private Integer printCount;

    /** 打印状态 */
    @Schema(description = "打印状态")
    private String printStatus;

    /** 核心业务标识 (如订单号, 用于快速搜索) */
    @Schema(description = "核心业务标识 (如订单号, 用于快速搜索)")
    private String businessKey;

    /** 备注信息 */
    @Schema(description = "备注信息")
    private String remark;
}
