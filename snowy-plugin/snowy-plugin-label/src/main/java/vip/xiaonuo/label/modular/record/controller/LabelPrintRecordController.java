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
package vip.xiaonuo.label.modular.record.controller;

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
import vip.xiaonuo.label.modular.record.entity.LabelPrintRecord;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordAddParam;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordEditParam;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordIdParam;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordPageParam;
import vip.xiaonuo.label.modular.record.service.LabelPrintRecordService;

import javax.validation.Valid;
import java.util.List;

/**
 * 打印记录控制器
 *
 * @author 
 * @date 2024/07/22 15:03
 **/
@Tag(name = "打印记录控制器")
@ApiSupport(author = "SNOWY_TEAM", order = 2)
@RestController
@Validated
public class LabelPrintRecordController {

    @Resource
    private LabelPrintRecordService labelPrintRecordService;

    /**
     * 获取打印记录分页
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 1)
    @Operation(summary = "获取打印记录分页")
    @SaCheckPermission("/label/record/page")
    @GetMapping("/label/record/page")
    public CommonResult<Page<LabelPrintRecord>> page(LabelPrintRecordPageParam labelPrintRecordPageParam) {
        return CommonResult.data(labelPrintRecordService.page(labelPrintRecordPageParam));
    }

    /**
     * 添加打印记录
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 2)
    @Operation(summary = "添加打印记录")
    @CommonLog("添加打印记录")
    @SaCheckPermission("/label/record/add")
    @PostMapping("/label/record/add")
    public CommonResult<String> add(@RequestBody @Valid LabelPrintRecordAddParam labelPrintRecordAddParam) {
        labelPrintRecordService.add(labelPrintRecordAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑打印记录
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 3)
    @Operation(summary = "编辑打印记录")
    @CommonLog("编辑打印记录")
    @SaCheckPermission("/label/record/edit")
    @PostMapping("/label/record/edit")
    public CommonResult<String> edit(@RequestBody @Valid LabelPrintRecordEditParam labelPrintRecordEditParam) {
        labelPrintRecordService.edit(labelPrintRecordEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除打印记录
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 4)
    @Operation(summary = "删除打印记录")
    @CommonLog("删除打印记录")
    @SaCheckPermission("/label/record/delete")
    @PostMapping("/label/record/delete")
    public CommonResult<String> delete(@RequestBody @Valid List<LabelPrintRecordIdParam> labelPrintRecordIdParamList) {
        labelPrintRecordService.delete(labelPrintRecordIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取打印记录详情
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    @ApiOperationSupport(order = 5)
    @Operation(summary = "获取打印记录详情")
    @SaCheckPermission("/label/record/detail")
    @GetMapping("/label/record/detail")
    public CommonResult<LabelPrintRecord> detail(@Valid LabelPrintRecordIdParam labelPrintRecordIdParam) {
        return CommonResult.data(labelPrintRecordService.detail(labelPrintRecordIdParam));
    }
}
