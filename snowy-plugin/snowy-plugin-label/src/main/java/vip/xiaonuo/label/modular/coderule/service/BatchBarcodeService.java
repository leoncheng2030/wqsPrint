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

import java.util.List;
import java.util.Map;

/**
 * 批量条码生成服务接口
 *
 * @author jetox
 * @date 2025/07/23 22:00
 **/
public interface BatchBarcodeService {

    /**
     * 批量生成条码
     *
     * @param ruleId 规则ID
     * @param paramsList 参数列表
     * @param barcodeType 条码类型
     * @param width 条码宽度
     * @param height 条码高度
     * @return 批量生成结果
     */
    Map<String, Object> batchGenerateBarcodes(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Integer width, Integer height);

    /**
     * 异步批量生成条码
     *
     * @param ruleId 规则ID
     * @param paramsList 参数列表
     * @param barcodeType 条码类型
     * @param width 条码宽度
     * @param height 条码高度
     * @return 任务ID
     */
    String asyncBatchGenerateBarcodes(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Integer width, Integer height);

    /**
     * 获取批量生成任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    Map<String, Object> getBatchTaskStatus(String taskId);

    /**
     * 根据数量批量生成条码（使用流水号）
     *
     * @param ruleId 规则ID
     * @param baseParams 基础参数
     * @param count 生成数量
     * @param barcodeType 条码类型
     * @param width 条码宽度
     * @param height 条码高度
     * @return 批量生成结果
     */
    Map<String, Object> batchGenerateByCount(String ruleId, Map<String, Object> baseParams, int count, String barcodeType, Integer width, Integer height);

    /**
     * 模板批量生成条码
     *
     * @param template 模板配置
     * @return 批量生成结果
     */
    Map<String, Object> batchGenerateByTemplate(Map<String, Object> template);

    /**
     * 取消批量生成任务
     *
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    boolean cancelBatchTask(String taskId);
}