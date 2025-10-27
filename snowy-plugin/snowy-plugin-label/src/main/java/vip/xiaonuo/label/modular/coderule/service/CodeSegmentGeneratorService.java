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
 * 编码段生成器服务接口
 *
 * @author jetox
 * @date 2025/07/23 20:00
 **/
public interface CodeSegmentGeneratorService {

    /**
     * 根据编码规则生成编码
     *
     * @param ruleId 规则ID
     * @param params 参数集合
     * @return 生成的编码
     */
    String generateCode(String ruleId, Map<String, Object> params);

    /**
     * 根据编码规则生成编码预览
     *
     * @param segments 编码段配置
     * @param params 参数集合
     * @return 生成的编码预览
     */
    String generateCodePreview(String segments, Map<String, Object> params);

    /**
     * 重置流水号
     *
     * @param ruleId 规则ID
     * @param serialKey 流水号键
     */
    void resetSerial(String ruleId, String serialKey);
}