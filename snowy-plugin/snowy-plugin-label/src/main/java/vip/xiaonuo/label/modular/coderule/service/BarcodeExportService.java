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
package vip.xiaonuo.label.modular.coderule.service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 条码导出服务接口
 *
 * @author jetox
 * @date 2025/07/23 22:30
 **/
public interface BarcodeExportService {

    /**
     * 导出条码为PDF格式
     *
     * @param ruleId 规则ID
     * @param paramsList 参数列表
     * @param barcodeType 条码类型
     * @param exportConfig 导出配置
     * @param response HTTP响应
     */
    void exportToPDF(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> exportConfig, HttpServletResponse response);

    /**
     * 导出条码为图片压缩包
     *
     * @param ruleId 规则ID
     * @param paramsList 参数列表
     * @param barcodeType 条码类型
     * @param imageFormat 图片格式（PNG、JPEG等）
     * @param exportConfig 导出配置
     * @param response HTTP响应
     */
    void exportToImages(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, String imageFormat, Map<String, Object> exportConfig, HttpServletResponse response);

    /**
     * 导出条码为Excel格式（包含条码图片）
     *
     * @param ruleId 规则ID
     * @param paramsList 参数列表
     * @param barcodeType 条码类型
     * @param exportConfig 导出配置
     * @param response HTTP响应
     */
    void exportToExcel(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> exportConfig, HttpServletResponse response);

    /**
     * 根据模板导出条码
     *
     * @param template 导出模板配置
     * @param response HTTP响应
     */
    void exportByTemplate(Map<String, Object> template, HttpServletResponse response);

    /**
     * 生成条码打印页面HTML
     *
     * @param ruleId 规则ID
     * @param paramsList 参数列表
     * @param barcodeType 条码类型
     * @param printConfig 打印配置
     * @return HTML内容
     */
    String generatePrintHTML(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> printConfig);

    /**
     * 导出条码标签（支持自定义布局）
     *
     * @param ruleId 规则ID
     * @param paramsList 参数列表
     * @param barcodeType 条码类型
     * @param labelConfig 标签配置
     * @param response HTTP响应
     */
    void exportToLabels(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> labelConfig, HttpServletResponse response);
}