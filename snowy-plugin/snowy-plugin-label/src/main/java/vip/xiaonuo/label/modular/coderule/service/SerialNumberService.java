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
 * 流水号管理服务接口
 *
 * @author jetox
 * @date 2025/07/23 21:20
 **/
public interface SerialNumberService {

    /**
     * 获取下一个流水号
     *
     * @param ruleId 规则ID
     * @param segmentIndex 编码段索引
     * @param resetType 重置类型
     * @param startValue 起始值
     * @return 下一个流水号
     */
    Long getNextSerial(String ruleId, int segmentIndex, String resetType, int startValue);

    /**
     * 重置指定的流水号
     *
     * @param ruleId 规则ID
     * @param segmentIndex 编码段索引
     * @param resetType 重置类型
     */
    void resetSerial(String ruleId, int segmentIndex, String resetType);

    /**
     * 批量重置流水号
     *
     * @param ruleId 规则ID
     */
    void resetAllSerials(String ruleId);

    /**
     * 获取流水号状态信息
     *
     * @param ruleId 规则ID
     * @return 流水号状态列表
     */
    List<Map<String, Object>> getSerialStatus(String ruleId);

    /**
     * 设置流水号值
     *
     * @param ruleId 规则ID
     * @param segmentIndex 编码段索引
     * @param resetType 重置类型
     * @param value 要设置的值
     */
    void setSerialValue(String ruleId, int segmentIndex, String resetType, Long value);

    /**
     * 预分配流水号段
     *
     * @param ruleId 规则ID
     * @param segmentIndex 编码段索引
     * @param resetType 重置类型
     * @param count 预分配数量
     * @return 预分配的流水号范围 [start, end]
     */
    Long[] allocateSerialRange(String ruleId, int segmentIndex, String resetType, int count);
}