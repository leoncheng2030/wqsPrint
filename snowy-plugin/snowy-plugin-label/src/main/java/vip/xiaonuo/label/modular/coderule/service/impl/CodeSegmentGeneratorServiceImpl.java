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
package vip.xiaonuo.label.modular.coderule.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vip.xiaonuo.label.modular.coderule.entity.WqsCodeRule;
import vip.xiaonuo.label.modular.coderule.service.CodeSegmentGeneratorService;
import vip.xiaonuo.label.modular.coderule.service.WqsCodeRuleService;
import vip.xiaonuo.common.exception.CommonException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 编码段生成器服务实现类
 *
 * @author jetox
 * @date 2025/07/23 20:00
 **/
@Slf4j
@Service
public class CodeSegmentGeneratorServiceImpl implements CodeSegmentGeneratorService {

    @Resource
    private WqsCodeRuleService wqsCodeRuleService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String SERIAL_KEY_PREFIX = "barcode:serial:";

    @Override
    public String generateCode(String ruleId, Map<String, Object> params) {
        WqsCodeRule codeRule = wqsCodeRuleService.queryEntity(ruleId);
        if (ObjectUtil.isEmpty(codeRule) || StrUtil.isEmpty(codeRule.getSegments())) {
            throw new CommonException("编码规则不存在或配置为空");
        }
        return generateCodeBySegments(codeRule.getSegments(), params, ruleId);
    }

    @Override
    public String generateCodePreview(String segments, Map<String, Object> params) {
        if (StrUtil.isEmpty(segments)) {
            throw new CommonException("编码段配置不能为空");
        }
        return generateCodeBySegments(segments, params, "preview");
    }

    /**
     * 根据编码段配置生成编码
     */
    private String generateCodeBySegments(String segments, Map<String, Object> params, String ruleId) {
        try {
            JSONArray segmentArray = JSONUtil.parseArray(segments);
            StringBuilder codeBuilder = new StringBuilder();

            for (int i = 0; i < segmentArray.size(); i++) {
                JSONObject segment = segmentArray.getJSONObject(i);
                String type = segment.getStr("type");
                String segmentValue = generateSegmentValue(segment, params, ruleId, i);
                codeBuilder.append(segmentValue);
            }

            return codeBuilder.toString();
        } catch (Exception e) {
            log.error("生成编码失败：", e);
            throw new CommonException("生成编码失败：{}", e.getMessage());
        }
    }

    /**
     * 生成单个编码段的值
     * 支持的编码段类型：fixed（固定值）、field（字段值）、date（日期格式）、serial（流水号）、separator（分隔符）
     */
    private String generateSegmentValue(JSONObject segment, Map<String, Object> params, String ruleId, int segmentIndex) {
        String type = segment.getStr("type");
        
        switch (type) {
            case "fixed":
                return generateFixedSegment(segment);
            case "field":
                return generateFieldSegment(segment, params);
            case "date":
                return generateDateSegment(segment);
            case "serial":
                return generateSerialSegment(segment, ruleId, segmentIndex);
            case "separator":
                return generateSeparatorSegment(segment);
            default:
                throw new CommonException("不支持的编码段类型：{}", type);
        }
    }

    /**
     * 生成固定值段
     */
    private String generateFixedSegment(JSONObject segment) {
        String value = segment.getStr("value");
        return StrUtil.isEmpty(value) ? "" : value;
    }

    /**
     * 生成字段值段
     */
    private String generateFieldSegment(JSONObject segment, Map<String, Object> params) {
        String fieldName = segment.getStr("fieldName");
        if (StrUtil.isEmpty(fieldName)) {
            return "";
        }
        
        Object fieldValue = params.get(fieldName);
        return fieldValue != null ? fieldValue.toString() : "";
    }

    /**
     * 生成日期格式段
     */
    private String generateDateSegment(JSONObject segment) {
        String format = segment.getStr("format");
        if (StrUtil.isEmpty(format)) {
            format = "YYYY";
        }
        
        Date now = new Date();
        switch (format) {
            case "YYYY":
                return DateUtil.format(now, "yyyy");
            case "MM":
                return DateUtil.format(now, "MM");
            case "DD":
                return DateUtil.format(now, "dd");
            case "YYYYMM":
                return DateUtil.format(now, "yyyyMM");
            case "YYYYMMDD":
                return DateUtil.format(now, "yyyyMMdd");
            default:
                return DateUtil.format(now, format);
        }
    }



    /**
     * 生成流水号段
     */
    private String generateSerialSegment(JSONObject segment, String ruleId, int segmentIndex) {
        int length = segment.getInt("length", 3);
        int startValue = segment.getInt("startValue", 1);
        String resetType = segment.getStr("resetType", "none");
        
        // 构建Redis键
        String serialKey = buildSerialKey(ruleId, segmentIndex, resetType);
        
        // 获取当前序号
        Long currentValue = stringRedisTemplate.opsForValue().increment(serialKey, 1);
        if (currentValue == null) {
            currentValue = 1L;
        }
        
        // 如果是第一次生成，设置起始值
        if (currentValue == 1 && startValue > 1) {
            stringRedisTemplate.opsForValue().set(serialKey, String.valueOf(startValue));
            currentValue = (long) startValue;
        }
        
        // 设置过期时间（根据重置类型）
        setSerialExpireTime(serialKey, resetType);
        
        // 格式化输出
        String format = "%0" + length + "d";
        return String.format(format, currentValue);
    }

    /**
     * 构建流水号Redis键
     */
    private String buildSerialKey(String ruleId, int segmentIndex, String resetType) {
        StringBuilder keyBuilder = new StringBuilder(SERIAL_KEY_PREFIX);
        keyBuilder.append(ruleId).append(":").append(segmentIndex);
        
        // 根据重置类型添加时间后缀
        LocalDate now = LocalDate.now();
        switch (resetType) {
            case "daily":
                keyBuilder.append(":").append(now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                break;
            case "monthly":
                keyBuilder.append(":").append(now.format(DateTimeFormatter.ofPattern("yyyyMM")));
                break;
            case "yearly":
                keyBuilder.append(":").append(now.getYear());
                break;
            case "none":
            default:
                // 不重置，不添加时间后缀
                break;
        }
        
        return keyBuilder.toString();
    }

    /**
     * 设置流水号过期时间
     */
    private void setSerialExpireTime(String serialKey, String resetType) {
        switch (resetType) {
            case "daily":
                // 设置到次日0点过期
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                long secondsUntilTomorrow = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    tomorrow.atStartOfDay()
                ).getSeconds();
                stringRedisTemplate.expire(serialKey, secondsUntilTomorrow, TimeUnit.SECONDS);
                break;
            case "monthly":
                // 设置到下月1号0点过期
                LocalDate nextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1);
                long secondsUntilNextMonth = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    nextMonth.atStartOfDay()
                ).getSeconds();
                stringRedisTemplate.expire(serialKey, secondsUntilNextMonth, TimeUnit.SECONDS);
                break;
            case "yearly":
                // 设置到明年1月1号0点过期
                LocalDate nextYear = LocalDate.now().plusYears(1).withDayOfYear(1);
                long secondsUntilNextYear = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    nextYear.atStartOfDay()
                ).getSeconds();
                stringRedisTemplate.expire(serialKey, secondsUntilNextYear, TimeUnit.SECONDS);
                break;
            case "none":
            default:
                // 不设置过期时间
                break;
        }
    }

    /**
     * 生成分隔符段
     */
    private String generateSeparatorSegment(JSONObject segment) {
        String separator = segment.getStr("separator");
        return StrUtil.isEmpty(separator) ? "" : separator;
    }

    @Override
    public void resetSerial(String ruleId, String serialKey) {
        // 删除指定的流水号键
        String fullKey = SERIAL_KEY_PREFIX + ruleId + ":" + serialKey;
        stringRedisTemplate.delete(fullKey);
        log.info("重置流水号成功，键：{}", fullKey);
    }
}