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
package vip.xiaonuo.label.modular.field.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.label.modular.field.entity.LabelDynamicField;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldAddParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldEditParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldIdParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldPageParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldSortParam;
import vip.xiaonuo.label.modular.field.service.LabelDynamicFieldService;

import javax.validation.Valid;
import java.util.List;

/**
 * 动态字段控制器
 *
 * @author 
 * @date 2024/07/22 15:03
 **/
@Tag(name = "动态字段控制器")
@ApiSupport(author = "SNOWY_TEAM", order = 3)
@RestController
@Validated
public class LabelDynamicFieldController {

    @Resource
    private LabelDynamicFieldService labelDynamicFieldService;

    /**
     * 获取动态字段分页
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取动态字段分页")
    @SaCheckPermission("/label/field/page")
    @GetMapping("/label/field/page")
    public CommonResult<Page<LabelDynamicField>> page(LabelDynamicFieldPageParam labelDynamicFieldPageParam) {
        return CommonResult.data(labelDynamicFieldService.page(labelDynamicFieldPageParam));
    }

    /**
     * 添加动态字段
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 2)
    @Operation(summary = "添加动态字段")
    @CommonLog("添加动态字段")
    @SaCheckPermission("/label/field/add")
    @PostMapping("/label/field/add")
    public CommonResult<String> add(@RequestBody @Valid LabelDynamicFieldAddParam labelDynamicFieldAddParam) {
        labelDynamicFieldService.add(labelDynamicFieldAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑动态字段
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 3)
    @Operation(summary = "编辑动态字段")
    @CommonLog("编辑动态字段")
    @SaCheckPermission("/label/field/edit")
    @PostMapping("/label/field/edit")
    public CommonResult<String> edit(@RequestBody @Valid LabelDynamicFieldEditParam labelDynamicFieldEditParam) {
        labelDynamicFieldService.edit(labelDynamicFieldEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除动态字段
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 4)
    @Operation(summary = "删除动态字段")
    @CommonLog("删除动态字段")
    @SaCheckPermission("/label/field/delete")
    @PostMapping("/label/field/delete")
    public CommonResult<String> delete(@RequestBody @Valid List<LabelDynamicFieldIdParam> labelDynamicFieldIdParamList) {
        labelDynamicFieldService.delete(labelDynamicFieldIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取动态字段详情
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 5)
    @Operation(summary = "获取动态字段详情")
    @SaCheckPermission("/label/field/detail")
    @GetMapping("/label/field/detail")
    public CommonResult<LabelDynamicField> detail(@Valid LabelDynamicFieldIdParam labelDynamicFieldIdParam) {
        return CommonResult.data(labelDynamicFieldService.detail(labelDynamicFieldIdParam));
    }
    
    /**
     * 更新动态字段排序
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 6)
    @Operation(summary = "更新动态字段排序")
    @CommonLog("更新动态字段排序")
    @SaCheckPermission("/label/field/sort")
    @PostMapping("/label/field/sort")
    public CommonResult<String> sort(@RequestBody @Valid LabelDynamicFieldSortParam labelDynamicFieldSortParam) {
        labelDynamicFieldService.sort(labelDynamicFieldSortParam);
        return CommonResult.ok();
    }
}
