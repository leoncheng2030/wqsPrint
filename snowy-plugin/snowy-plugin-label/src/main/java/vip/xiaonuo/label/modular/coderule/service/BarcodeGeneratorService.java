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

import java.util.Map;

/**
 * 条码生成器服务接口
 *
 * @author jetox
 * @date 2025/07/23 20:40
 **/
public interface BarcodeGeneratorService {

    /**
     * 根据编码规则生成条码图片
     *
     * @param ruleId 规则ID
     * @param params 参数集合
     * @param barcodeType 条码类型（如CODE128、QR等）
     * @param width 条码宽度
     * @param height 条码高度
     * @return Base64编码的图片数据
     */
    String generateBarcodeImage(String ruleId, Map<String, Object> params, String barcodeType, Integer width, Integer height);

    /**
     * 根据编码段配置生成条码预览图片
     *
     * @param segments 编码段配置JSON
     * @param params 参数集合
     * @param barcodeType 条码类型
     * @param width 条码宽度
     * @param height 条码高度
     * @return Base64编码的图片数据
     */
    String generateBarcodePreviewImage(String segments, Map<String, Object> params, String barcodeType, Integer width, Integer height);

    /**
     * 根据文本生成条码图片
     *
     * @param text 要编码的文本
     * @param barcodeType 条码类型
     * @param width 条码宽度
     * @param height 条码高度
     * @return Base64编码的图片数据
     */
    String generateBarcodeFromText(String text, String barcodeType, Integer width, Integer height);

    /**
     * 批量生成条码图片
     *
     * @param ruleId 规则ID
     * @param paramsList 参数列表
     * @param barcodeType 条码类型
     * @param width 条码宽度
     * @param height 条码高度
     * @return Base64编码的图片数据列表
     */
    java.util.List<String> generateBarcodeImages(String ruleId, java.util.List<Map<String, Object>> paramsList, String barcodeType, Integer width, Integer height);
}