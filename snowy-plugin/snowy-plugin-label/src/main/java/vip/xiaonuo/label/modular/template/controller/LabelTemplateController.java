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
package vip.xiaonuo.label.modular.template.controller;

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
import vip.xiaonuo.label.modular.template.entity.LabelTemplate;
import vip.xiaonuo.label.modular.template.param.*;
import vip.xiaonuo.label.modular.template.service.LabelTemplateService;

import javax.validation.Valid;
import java.util.List;

/**
 * 标签模板控制器
 *
 * @author 
 * @date 2024/07/22 15:03
 **/
@Tag(name = "标签模板控制器")
@ApiSupport(author = "SNOWY_TEAM", order = 1)
@RestController
@Validated
public class LabelTemplateController {

    @Resource
    private LabelTemplateService labelTemplateService;

    /**
     * 获取标签模板分页
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取标签模板分页")
    @GetMapping("/label/template/page")
    public CommonResult<Page<LabelTemplate>> page(LabelTemplatePageParam labelTemplatePageParam) {
        return CommonResult.data(labelTemplateService.page(labelTemplatePageParam));
    }

    /**
     * 添加标签模板
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 2)
    @Operation(summary = "添加标签模板")
    @CommonLog("添加标签模板")
    @SaCheckPermission("/label/template/add")
    @PostMapping("/label/template/add")
    public CommonResult<String> add(@RequestBody @Valid LabelTemplateAddParam labelTemplateAddParam) {
        labelTemplateService.add(labelTemplateAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑标签模板
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 3)
    @Operation(summary = "编辑标签模板")
    @CommonLog("编辑标签模板")
    @SaCheckPermission("/label/template/edit")
    @PostMapping("/label/template/edit")
    public CommonResult<String> edit(@RequestBody @Valid LabelTemplateEditParam labelTemplateEditParam) {
        labelTemplateService.edit(labelTemplateEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除标签模板
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 4)
    @Operation(summary = "删除标签模板")
    @CommonLog("删除标签模板")
    @SaCheckPermission("/label/template/delete")
    @PostMapping("/label/template/delete")
    public CommonResult<String> delete(@RequestBody @Valid List<LabelTemplateIdParam> labelTemplateIdParamList) {
        labelTemplateService.delete(labelTemplateIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取标签模板详情
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 5)
    @Operation(summary = "获取标签模板详情")
    @SaCheckPermission("/label/template/detail")
    @GetMapping("/label/template/detail")
    public CommonResult<LabelTemplate> detail(@Valid LabelTemplateIdParam labelTemplateIdParam) {
        return CommonResult.data(labelTemplateService.detail(labelTemplateIdParam));
    }

    /**
     * 处理标签模板设计
     *
     * @author
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 6)
    @Operation(summary = "处理标签模板设计")
    @CommonLog("处理标签模板设计")
    @SaCheckPermission("/label/template/design")
    @PostMapping("/label/template/design")
    public CommonResult<String> design(@RequestBody @Valid LabelTemplateDesignParam labelTemplateDesignParam) {
        labelTemplateService.design(labelTemplateDesignParam);
        return CommonResult.ok();
    }
}
