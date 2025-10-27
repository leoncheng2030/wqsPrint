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
package vip.xiaonuo.label.modular.field.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 动态字段排序参数
 *
 * @author 
 * @date 2024/07/22 15:03
 **/
@Getter
@Setter
public class LabelDynamicFieldSortParam {

    /** 排序数据列表 */
    @Schema(description = "排序数据列表")
    @NotNull(message = "排序数据列表不能为空")
    private List<SortItem> sortData;

    /**
     * 排序项
     */
    @Getter
    @Setter
    public static class SortItem {
        
        /** 字段ID */
        @Schema(description = "字段ID")
        @NotBlank(message = "字段ID不能为空")
        private String id;
        
        /** 排序码 */
        @Schema(description = "排序码")
        @NotNull(message = "排序码不能为空")
        private Integer sortCode;
    }
}