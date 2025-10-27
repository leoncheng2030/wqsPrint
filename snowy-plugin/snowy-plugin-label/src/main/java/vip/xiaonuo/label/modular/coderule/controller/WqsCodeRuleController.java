/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Snowy源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://www.xiaonuo.vip
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队xiaonuobase@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Snowy商业授权许可，请在官网购买授权，地址为 https://www.xiaonuo.vip
 */
package vip.xiaonuo.label.modular.coderule.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.label.modular.coderule.entity.WqsCodeRule;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRuleAddParam;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRuleEditParam;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRuleIdParam;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRulePageParam;
import vip.xiaonuo.label.modular.coderule.service.WqsCodeRuleService;
import vip.xiaonuo.label.modular.coderule.service.CodeSegmentGeneratorService;
import vip.xiaonuo.label.modular.coderule.service.BarcodeGeneratorService;
import vip.xiaonuo.label.modular.coderule.service.SerialNumberService;
import vip.xiaonuo.label.modular.coderule.service.BatchBarcodeService;
import vip.xiaonuo.label.modular.coderule.service.BarcodeExportService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 编码规则表控制器
 *
 * @author jetox
 * @date  2025/07/23 19:41
 */
@Tag(name = "编码规则表控制器")
@RestController
@Validated
public class WqsCodeRuleController {

    @Resource
    private WqsCodeRuleService wqsCodeRuleService;

    @Resource
    private CodeSegmentGeneratorService codeSegmentGeneratorService;

    @Resource
    private BarcodeGeneratorService barcodeGeneratorService;

    @Resource
    private SerialNumberService serialNumberService;

    @Resource
    private BatchBarcodeService batchBarcodeService;

    @Resource
    private BarcodeExportService barcodeExportService;

    /**
     * 获取编码规则表分页
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    @Operation(summary = "获取编码规则表分页")
    @SaCheckPermission("/barcode/coderule/page")
    @GetMapping("/barcode/coderule/page")
    public CommonResult<Page<WqsCodeRule>> page(WqsCodeRulePageParam wqsCodeRulePageParam) {
        return CommonResult.data(wqsCodeRuleService.page(wqsCodeRulePageParam));
    }

    /**
     * 添加编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    @Operation(summary = "添加编码规则表")
    @CommonLog("添加编码规则表")
    @SaCheckPermission("/barcode/coderule/add")
    @PostMapping("/barcode/coderule/add")
    public CommonResult<String> add(@RequestBody @Valid WqsCodeRuleAddParam wqsCodeRuleAddParam) {
        wqsCodeRuleService.add(wqsCodeRuleAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    @Operation(summary = "编辑编码规则表")
    @CommonLog("编辑编码规则表")
    @SaCheckPermission("/barcode/coderule/edit")
    @PostMapping("/barcode/coderule/edit")
    public CommonResult<String> edit(@RequestBody @Valid WqsCodeRuleEditParam wqsCodeRuleEditParam) {
        wqsCodeRuleService.edit(wqsCodeRuleEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    @Operation(summary = "删除编码规则表")
    @CommonLog("删除编码规则表")
    @SaCheckPermission("/barcode/coderule/delete")
    @PostMapping("/barcode/coderule/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                                   List<WqsCodeRuleIdParam> wqsCodeRuleIdParamList) {
        wqsCodeRuleService.delete(wqsCodeRuleIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取编码规则表详情
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    @Operation(summary = "获取编码规则表详情")
    @SaCheckPermission("/barcode/coderule/detail")
    @GetMapping("/barcode/coderule/detail")
    public CommonResult<WqsCodeRule> detail(@Valid WqsCodeRuleIdParam wqsCodeRuleIdParam) {
        return CommonResult.data(wqsCodeRuleService.detail(wqsCodeRuleIdParam));
    }

    /**
     * 下载编码规则表导入模板
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    @Operation(summary = "下载编码规则表导入模板")
    @GetMapping(value = "/barcode/coderule/downloadImportTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        wqsCodeRuleService.downloadImportTemplate(response);
    }

    /**
     * 导入编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    @Operation(summary = "导入编码规则表")
    @CommonLog("导入编码规则表")
    @SaCheckPermission("/barcode/coderule/importData")
    @PostMapping("/barcode/coderule/importData")
    public CommonResult<JSONObject> importData(@RequestPart("file") MultipartFile file) {
        return CommonResult.data(wqsCodeRuleService.importData(file));
    }

    /**
     * 导出编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    @Operation(summary = "导出编码规则表")
    @SaCheckPermission("/barcode/coderule/exportData")
    @PostMapping(value = "/barcode/coderule/exportData", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportData(@RequestBody List<WqsCodeRuleIdParam> wqsCodeRuleIdParamList, HttpServletResponse response) throws IOException {
        wqsCodeRuleService.exportData(wqsCodeRuleIdParamList, response);
    }

    /**
     * 根据规则生成编码
     *
     * @author jetox
     * @date 2025/07/23 20:30
     */
    @Operation(summary = "根据规则生成编码")
    @SaCheckPermission("/barcode/coderule/generateCode")
    @PostMapping("/barcode/coderule/generateCode")
    public CommonResult<String> generateCode(@RequestParam String ruleId, @RequestBody Map<String, Object> params) {
        String code = codeSegmentGeneratorService.generateCode(ruleId, params);
        return CommonResult.data(code);
    }

    /**
     * 生成编码预览
     *
     * @author jetox
     * @date 2025/07/23 20:30
     */
    @Operation(summary = "生成编码预览")
    @PostMapping("/barcode/coderule/generatePreview")
    public CommonResult<String> generatePreview(@RequestParam String segments, @RequestBody Map<String, Object> params) {
        String code = codeSegmentGeneratorService.generateCodePreview(segments, params);
        return CommonResult.data(code);
    }

    /**
     * 重置流水号
     *
     * @author jetox
     * @date 2025/07/23 20:30
     */
    @Operation(summary = "重置流水号")
    @CommonLog("重置流水号")
    @SaCheckPermission("/barcode/coderule/resetSerial")
    @PostMapping("/barcode/coderule/resetSerial")
    public CommonResult<String> resetSerial(@RequestParam String ruleId, @RequestParam String serialKey) {
        codeSegmentGeneratorService.resetSerial(ruleId, serialKey);
        return CommonResult.ok();
    }

    /**
     * 生成条码图片
     *
     * @author jetox
     * @date 2025/07/23 21:00
     */
    @Operation(summary = "生成条码图片")
    @SaCheckPermission("/barcode/coderule/generateBarcodeImage")
    @PostMapping("/barcode/coderule/generateBarcodeImage")
    public CommonResult<String> generateBarcodeImage(@RequestParam String ruleId, 
                                                    @RequestBody Map<String, Object> params,
                                                    @RequestParam(defaultValue = "CODE128") String barcodeType,
                                                    @RequestParam(defaultValue = "300") Integer width,
                                                    @RequestParam(defaultValue = "150") Integer height) {
        String imageBase64 = barcodeGeneratorService.generateBarcodeImage(ruleId, params, barcodeType, width, height);
        return CommonResult.data(imageBase64);
    }

    /**
     * 生成条码预览图片
     *
     * @author jetox
     * @date 2025/07/23 21:00
     */
    @Operation(summary = "生成条码预览图片")
    @PostMapping("/barcode/coderule/generateBarcodePreview")
    public CommonResult<String> generateBarcodePreview(@RequestParam String segments,
                                                      @RequestBody Map<String, Object> params,
                                                      @RequestParam(defaultValue = "CODE128") String barcodeType,
                                                      @RequestParam(defaultValue = "300") Integer width,
                                                      @RequestParam(defaultValue = "150") Integer height) {
        String imageBase64 = barcodeGeneratorService.generateBarcodePreviewImage(segments, params, barcodeType, width, height);
        return CommonResult.data(imageBase64);
    }

    /**
     * 根据文本生成条码图片
     *
     * @author jetox
     * @date 2025/07/23 21:00
     */
    @Operation(summary = "根据文本生成条码图片")
    @PostMapping("/barcode/coderule/generateBarcodeFromText")
    public CommonResult<String> generateBarcodeFromText(@RequestParam String text,
                                                       @RequestParam(defaultValue = "CODE128") String barcodeType,
                                                       @RequestParam(defaultValue = "300") Integer width,
                                                       @RequestParam(defaultValue = "150") Integer height) {
        String imageBase64 = barcodeGeneratorService.generateBarcodeFromText(text, barcodeType, width, height);
        return CommonResult.data(imageBase64);
    }

    /**
     * 批量生成条码图片
     *
     * @author jetox
     * @date 2025/07/23 21:00
     */
    @Operation(summary = "批量生成条码图片")
    @SaCheckPermission("/barcode/coderule/generateBarcodeImages")
    @PostMapping("/barcode/coderule/generateBarcodeImages")
    public CommonResult<List<String>> generateBarcodeImages(@RequestParam String ruleId,
                                                           @RequestBody List<Map<String, Object>> paramsList,
                                                           @RequestParam(defaultValue = "CODE128") String barcodeType,
                                                           @RequestParam(defaultValue = "300") Integer width,
                                                           @RequestParam(defaultValue = "150") Integer height) {
        List<String> imageList = barcodeGeneratorService.generateBarcodeImages(ruleId, paramsList, barcodeType, width, height);
        return CommonResult.data(imageList);
    }

    /**
     * 获取流水号状态
     *
     * @author jetox
     * @date 2025/07/23 21:40
     */
    @Operation(summary = "获取流水号状态")
    @SaCheckPermission("/barcode/coderule/getSerialStatus")
    @GetMapping("/barcode/coderule/getSerialStatus")
    public CommonResult<List<Map<String, Object>>> getSerialStatus(@RequestParam String ruleId) {
        List<Map<String, Object>> statusList = serialNumberService.getSerialStatus(ruleId);
        return CommonResult.data(statusList);
    }

    /**
     * 设置流水号值
     *
     * @author jetox
     * @date 2025/07/23 21:40
     */
    @Operation(summary = "设置流水号值")
    @CommonLog("设置流水号值")
    @SaCheckPermission("/barcode/coderule/setSerialValue")
    @PostMapping("/barcode/coderule/setSerialValue")
    public CommonResult<String> setSerialValue(@RequestParam String ruleId,
                                              @RequestParam int segmentIndex,
                                              @RequestParam String resetType,
                                              @RequestParam Long value) {
        serialNumberService.setSerialValue(ruleId, segmentIndex, resetType, value);
        return CommonResult.ok();
    }

    /**
     * 重置指定流水号
     *
     * @author jetox
     * @date 2025/07/23 21:40
     */
    @Operation(summary = "重置指定流水号")
    @CommonLog("重置指定流水号")
    @SaCheckPermission("/barcode/coderule/resetSpecificSerial")
    @PostMapping("/barcode/coderule/resetSpecificSerial")
    public CommonResult<String> resetSpecificSerial(@RequestParam String ruleId,
                                                    @RequestParam int segmentIndex,
                                                    @RequestParam String resetType) {
        serialNumberService.resetSerial(ruleId, segmentIndex, resetType);
        return CommonResult.ok();
    }

    /**
     * 重置所有流水号
     *
     * @author jetox
     * @date 2025/07/23 21:40
     */
    @Operation(summary = "重置所有流水号")
    @CommonLog("重置所有流水号")
    @SaCheckPermission("/barcode/coderule/resetAllSerials")
    @PostMapping("/barcode/coderule/resetAllSerials")
    public CommonResult<String> resetAllSerials(@RequestParam String ruleId) {
        serialNumberService.resetAllSerials(ruleId);
        return CommonResult.ok();
    }

    /**
     * 预分配流水号段
     *
     * @author jetox
     * @date 2025/07/23 21:40
     */
    @Operation(summary = "预分配流水号段")
    @SaCheckPermission("/barcode/coderule/allocateSerialRange")
    @PostMapping("/barcode/coderule/allocateSerialRange")
    public CommonResult<Long[]> allocateSerialRange(@RequestParam String ruleId,
                                                    @RequestParam int segmentIndex,
                                                    @RequestParam String resetType,
                                                    @RequestParam int count) {
        Long[] range = serialNumberService.allocateSerialRange(ruleId, segmentIndex, resetType, count);
        return CommonResult.data(range);
    }

    /**
     * 批量生成条码
     *
     * @author jetox
     * @date 2025/07/23 22:20
     */
    @Operation(summary = "批量生成条码")
    @SaCheckPermission("/barcode/coderule/batchGenerateBarcodes")
    @PostMapping("/barcode/coderule/batchGenerateBarcodes")
    public CommonResult<Map<String, Object>> batchGenerateBarcodes(@RequestParam String ruleId,
                                                                  @RequestBody List<Map<String, Object>> paramsList,
                                                                  @RequestParam(defaultValue = "CODE128") String barcodeType,
                                                                  @RequestParam(defaultValue = "300") Integer width,
                                                                  @RequestParam(defaultValue = "150") Integer height) {
        Map<String, Object> result = batchBarcodeService.batchGenerateBarcodes(ruleId, paramsList, barcodeType, width, height);
        return CommonResult.data(result);
    }

    /**
     * 异步批量生成条码
     *
     * @author jetox
     * @date 2025/07/23 22:20
     */
    @Operation(summary = "异步批量生成条码")
    @SaCheckPermission("/barcode/coderule/asyncBatchGenerateBarcodes")
    @PostMapping("/barcode/coderule/asyncBatchGenerateBarcodes")
    public CommonResult<String> asyncBatchGenerateBarcodes(@RequestParam String ruleId,
                                                          @RequestBody List<Map<String, Object>> paramsList,
                                                          @RequestParam(defaultValue = "CODE128") String barcodeType,
                                                          @RequestParam(defaultValue = "300") Integer width,
                                                          @RequestParam(defaultValue = "150") Integer height) {
        String taskId = batchBarcodeService.asyncBatchGenerateBarcodes(ruleId, paramsList, barcodeType, width, height);
        return CommonResult.data(taskId);
    }

    /**
     * 获取批量生成任务状态
     *
     * @author jetox
     * @date 2025/07/23 22:20
     */
    @Operation(summary = "获取批量生成任务状态")
    @SaCheckPermission("/barcode/coderule/getBatchTaskStatus")
    @GetMapping("/barcode/coderule/getBatchTaskStatus")
    public CommonResult<Map<String, Object>> getBatchTaskStatus(@RequestParam String taskId) {
        Map<String, Object> status = batchBarcodeService.getBatchTaskStatus(taskId);
        return CommonResult.data(status);
    }

    /**
     * 根据数量批量生成条码
     *
     * @author jetox
     * @date 2025/07/23 22:20
     */
    @Operation(summary = "根据数量批量生成条码")
    @SaCheckPermission("/barcode/coderule/batchGenerateByCount")
    @PostMapping("/barcode/coderule/batchGenerateByCount")
    public CommonResult<Map<String, Object>> batchGenerateByCount(@RequestParam String ruleId,
                                                                 @RequestBody Map<String, Object> baseParams,
                                                                 @RequestParam int count,
                                                                 @RequestParam(defaultValue = "CODE128") String barcodeType,
                                                                 @RequestParam(defaultValue = "300") Integer width,
                                                                 @RequestParam(defaultValue = "150") Integer height) {
        Map<String, Object> result = batchBarcodeService.batchGenerateByCount(ruleId, baseParams, count, barcodeType, width, height);
        return CommonResult.data(result);
    }

    /**
     * 模板批量生成条码
     *
     * @author jetox
     * @date 2025/07/23 22:20
     */
    @Operation(summary = "模板批量生成条码")
    @SaCheckPermission("/barcode/coderule/batchGenerateByTemplate")
    @PostMapping("/barcode/coderule/batchGenerateByTemplate")
    public CommonResult<Map<String, Object>> batchGenerateByTemplate(@RequestBody Map<String, Object> template) {
        Map<String, Object> result = batchBarcodeService.batchGenerateByTemplate(template);
        return CommonResult.data(result);
    }

    /**
     * 取消批量生成任务
     *
     * @author jetox
     * @date 2025/07/23 22:20
     */
    @Operation(summary = "取消批量生成任务")
    @SaCheckPermission("/barcode/coderule/cancelBatchTask")
    @PostMapping("/barcode/coderule/cancelBatchTask")
    public CommonResult<Boolean> cancelBatchTask(@RequestParam String taskId) {
        boolean result = batchBarcodeService.cancelBatchTask(taskId);
        return CommonResult.data(result);
    }

    /**
     * 导出条码为PDF
     *
     * @author jetox
     * @date 2025/07/23 22:50
     */
    @Operation(summary = "导出条码为PDF")
    @SaCheckPermission("/barcode/coderule/exportToPDF")
    @PostMapping(value = "/barcode/coderule/exportToPDF", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportToPDF(@RequestParam String ruleId,
                           @RequestBody List<Map<String, Object>> paramsList,
                           @RequestParam(defaultValue = "CODE128") String barcodeType,
                           @RequestParam(required = false) Map<String, Object> exportConfig,
                           HttpServletResponse response) {
        if (exportConfig == null) {
            exportConfig = Map.of();
        }
        barcodeExportService.exportToPDF(ruleId, paramsList, barcodeType, exportConfig, response);
    }

    /**
     * 导出条码为图片压缩包
     *
     * @author jetox
     * @date 2025/07/23 22:50
     */
    @Operation(summary = "导出条码为图片压缩包")
    @SaCheckPermission("/barcode/coderule/exportToImages")
    @PostMapping(value = "/barcode/coderule/exportToImages", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportToImages(@RequestParam String ruleId,
                              @RequestBody List<Map<String, Object>> paramsList,
                              @RequestParam(defaultValue = "CODE128") String barcodeType,
                              @RequestParam(defaultValue = "PNG") String imageFormat,
                              @RequestParam(required = false) Map<String, Object> exportConfig,
                              HttpServletResponse response) {
        if (exportConfig == null) {
            exportConfig = Map.of();
        }
        barcodeExportService.exportToImages(ruleId, paramsList, barcodeType, imageFormat, exportConfig, response);
    }

    /**
     * 导出条码为Excel
     *
     * @author jetox
     * @date 2025/07/23 22:50
     */
    @Operation(summary = "导出条码为Excel")
    @SaCheckPermission("/barcode/coderule/exportToExcel")
    @PostMapping(value = "/barcode/coderule/exportToExcel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportToExcel(@RequestParam String ruleId,
                             @RequestBody List<Map<String, Object>> paramsList,
                             @RequestParam(defaultValue = "CODE128") String barcodeType,
                             @RequestParam(required = false) Map<String, Object> exportConfig,
                             HttpServletResponse response) {
        if (exportConfig == null) {
            exportConfig = Map.of();
        }
        barcodeExportService.exportToExcel(ruleId, paramsList, barcodeType, exportConfig, response);
    }

    /**
     * 导出条码标签
     *
     * @author jetox
     * @date 2025/07/23 22:50
     */
    @Operation(summary = "导出条码标签")
    @SaCheckPermission("/barcode/coderule/exportToLabels")
    @PostMapping(value = "/barcode/coderule/exportToLabels", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportToLabels(@RequestParam String ruleId,
                              @RequestBody List<Map<String, Object>> paramsList,
                              @RequestParam(defaultValue = "CODE128") String barcodeType,
                              @RequestParam(required = false) Map<String, Object> labelConfig,
                              HttpServletResponse response) {
        if (labelConfig == null) {
            labelConfig = Map.of();
        }
        barcodeExportService.exportToLabels(ruleId, paramsList, barcodeType, labelConfig, response);
    }

    /**
     * 根据模板导出条码
     *
     * @author jetox
     * @date 2025/07/23 22:50
     */
    @Operation(summary = "根据模板导出条码")
    @SaCheckPermission("/barcode/coderule/exportByTemplate")
    @PostMapping(value = "/barcode/coderule/exportByTemplate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportByTemplate(@RequestBody Map<String, Object> template, HttpServletResponse response) {
        barcodeExportService.exportByTemplate(template, response);
    }

    /**
     * 生成条码打印页面HTML
     *
     * @author jetox
     * @date 2025/07/23 22:50
     */
    @Operation(summary = "生成条码打印页面HTML")
    @SaCheckPermission("/barcode/coderule/generatePrintHTML")
    @PostMapping("/barcode/coderule/generatePrintHTML")
    public CommonResult<String> generatePrintHTML(@RequestParam String ruleId,
                                                 @RequestBody List<Map<String, Object>> paramsList,
                                                 @RequestParam(defaultValue = "CODE128") String barcodeType,
                                                 @RequestParam(required = false) Map<String, Object> printConfig) {
        if (printConfig == null) {
            printConfig = Map.of();
        }
        String htmlContent = barcodeExportService.generatePrintHTML(ruleId, paramsList, barcodeType, printConfig);
        return CommonResult.data(htmlContent);
    }
}
