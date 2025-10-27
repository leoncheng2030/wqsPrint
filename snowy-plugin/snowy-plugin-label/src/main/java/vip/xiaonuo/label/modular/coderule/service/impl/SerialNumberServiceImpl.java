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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import vip.xiaonuo.label.modular.coderule.service.SerialNumberService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 流水号管理服务实现类
 *
 * @author jetox
 * @date 2025/07/23 21:20
 **/
@Slf4j
@Service
public class SerialNumberServiceImpl implements SerialNumberService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String SERIAL_KEY_PREFIX = "barcode:serial:";
    private static final String SERIAL_INFO_KEY_PREFIX = "barcode:serial:info:";

    // Lua脚本：原子性地获取并递增流水号
    private static final String INCR_SCRIPT = 
        "local current = redis.call('incr', KEYS[1]) " +
        "if current == 1 then " +
        "    redis.call('set', KEYS[1], ARGV[1]) " +
        "    return tonumber(ARGV[1]) " +
        "else " +
        "    return current " +
        "end";

    // Lua脚本：原子性地分配流水号段
    private static final String ALLOCATE_RANGE_SCRIPT =
        "local current = redis.call('get', KEYS[1]) " +
        "if not current then " +
        "    current = ARGV[1] " +
        "    redis.call('set', KEYS[1], current) " +
        "end " +
        "local start = tonumber(current) + 1 " +
        "local count = tonumber(ARGV[2]) " +
        "local newValue = start + count - 1 " +
        "redis.call('set', KEYS[1], newValue) " +
        "return {start, newValue}";

    @Override
    public Long getNextSerial(String ruleId, int segmentIndex, String resetType, int startValue) {
        String serialKey = buildSerialKey(ruleId, segmentIndex, resetType);
        
        try {
            // 使用Lua脚本保证原子性
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(INCR_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = stringRedisTemplate.execute(script, 
                Collections.singletonList(serialKey), 
                String.valueOf(startValue));
            
            if (result == null) {
                result = (long) startValue;
            }
            
            // 设置过期时间
            setSerialExpireTime(serialKey, resetType);
            
            // 记录流水号信息
            recordSerialInfo(ruleId, segmentIndex, resetType, result);
            
            log.debug("获取流水号成功，键：{}，值：{}", serialKey, result);
            return result;
            
        } catch (Exception e) {
            log.error("获取流水号失败，键：{}", serialKey, e);
            throw new RuntimeException("获取流水号失败", e);
        }
    }

    @Override
    public void resetSerial(String ruleId, int segmentIndex, String resetType) {
        String serialKey = buildSerialKey(ruleId, segmentIndex, resetType);
        
        try {
            stringRedisTemplate.delete(serialKey);
            
            // 删除对应的信息记录
            String infoKey = buildSerialInfoKey(ruleId, segmentIndex, resetType);
            stringRedisTemplate.delete(infoKey);
            
            log.info("重置流水号成功，键：{}", serialKey);
        } catch (Exception e) {
            log.error("重置流水号失败，键：{}", serialKey, e);
            throw new RuntimeException("重置流水号失败", e);
        }
    }

    @Override
    public void resetAllSerials(String ruleId) {
        String pattern = SERIAL_KEY_PREFIX + ruleId + ":*";
        
        try {
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (CollUtil.isNotEmpty(keys)) {
                stringRedisTemplate.delete(keys);
                log.info("批量重置流水号成功，规则ID：{}，重置数量：{}", ruleId, keys.size());
            }
            
            // 删除对应的信息记录
            String infoPattern = SERIAL_INFO_KEY_PREFIX + ruleId + ":*";
            Set<String> infoKeys = stringRedisTemplate.keys(infoPattern);
            if (CollUtil.isNotEmpty(infoKeys)) {
                stringRedisTemplate.delete(infoKeys);
            }
            
        } catch (Exception e) {
            log.error("批量重置流水号失败，规则ID：{}", ruleId, e);
            throw new RuntimeException("批量重置流水号失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getSerialStatus(String ruleId) {
        String pattern = SERIAL_KEY_PREFIX + ruleId + ":*";
        List<Map<String, Object>> statusList = new ArrayList<>();
        
        try {
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (CollUtil.isEmpty(keys)) {
                return statusList;
            }
            
            for (String key : keys) {
                Map<String, Object> status = new HashMap<>();
                
                // 解析键信息
                String[] parts = key.replace(SERIAL_KEY_PREFIX, "").split(":");
                status.put("ruleId", parts[0]);
                status.put("segmentIndex", Integer.parseInt(parts[1]));
                
                // 获取当前值
                String currentValue = stringRedisTemplate.opsForValue().get(key);
                status.put("currentValue", currentValue != null ? Long.parseLong(currentValue) : 0L);
                
                // 获取过期时间
                Long expire = stringRedisTemplate.getExpire(key);
                status.put("expireTime", expire);
                
                // 获取重置类型和其他信息
                String infoKey = key.replace(SERIAL_KEY_PREFIX, SERIAL_INFO_KEY_PREFIX);
                Map<Object, Object> infoMap = stringRedisTemplate.opsForHash().entries(infoKey);
                if (CollUtil.isNotEmpty(infoMap)) {
                    for (Map.Entry<Object, Object> entry : infoMap.entrySet()) {
                        status.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                }
                
                // 添加时间戳
                status.put("lastUpdateTime", System.currentTimeMillis());
                
                statusList.add(status);
            }
            
        } catch (Exception e) {
            log.error("获取流水号状态失败，规则ID：{}", ruleId, e);
            throw new RuntimeException("获取流水号状态失败", e);
        }
        
        return statusList;
    }

    @Override
    public void setSerialValue(String ruleId, int segmentIndex, String resetType, Long value) {
        String serialKey = buildSerialKey(ruleId, segmentIndex, resetType);
        
        try {
            stringRedisTemplate.opsForValue().set(serialKey, String.valueOf(value));
            
            // 设置过期时间
            setSerialExpireTime(serialKey, resetType);
            
            // 更新信息记录
            recordSerialInfo(ruleId, segmentIndex, resetType, value);
            
            log.info("设置流水号成功，键：{}，值：{}", serialKey, value);
        } catch (Exception e) {
            log.error("设置流水号失败，键：{}，值：{}", serialKey, value, e);
            throw new RuntimeException("设置流水号失败", e);
        }
    }

    @Override
    public Long[] allocateSerialRange(String ruleId, int segmentIndex, String resetType, int count) {
        String serialKey = buildSerialKey(ruleId, segmentIndex, resetType);
        
        try {
            // 使用Lua脚本原子性地分配范围
            DefaultRedisScript<List> script = new DefaultRedisScript<>();
            script.setScriptText(ALLOCATE_RANGE_SCRIPT);
            script.setResultType(List.class);
            
            @SuppressWarnings("unchecked")
            List<Long> result = stringRedisTemplate.execute(script,
                Collections.singletonList(serialKey),
                "0", String.valueOf(count));
            
            if (result != null && result.size() == 2) {
                Long start = result.get(0);
                Long end = result.get(1);
                
                // 设置过期时间
                setSerialExpireTime(serialKey, resetType);
                
                // 记录分配信息
                recordSerialInfo(ruleId, segmentIndex, resetType, end);
                
                log.info("分配流水号段成功，键：{}，范围：[{}, {}]", serialKey, start, end);
                return new Long[]{start, end};
            }
            
            throw new RuntimeException("分配流水号段失败，返回结果异常");
            
        } catch (Exception e) {
            log.error("分配流水号段失败，键：{}", serialKey, e);
            throw new RuntimeException("分配流水号段失败", e);
        }
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
     * 构建流水号信息键
     */
    private String buildSerialInfoKey(String ruleId, int segmentIndex, String resetType) {
        return buildSerialKey(ruleId, segmentIndex, resetType).replace(SERIAL_KEY_PREFIX, SERIAL_INFO_KEY_PREFIX);
    }

    /**
     * 设置流水号过期时间
     */
    private void setSerialExpireTime(String serialKey, String resetType) {
        try {
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
                    // 不设置过期时间，但设置一个很长的过期时间防止内存泄漏
                    stringRedisTemplate.expire(serialKey, 365, TimeUnit.DAYS);
                    break;
            }
        } catch (Exception e) {
            log.warn("设置流水号过期时间失败，键：{}", serialKey, e);
        }
    }

    /**
     * 记录流水号信息
     */
    private void recordSerialInfo(String ruleId, int segmentIndex, String resetType, Long currentValue) {
        try {
            String infoKey = buildSerialInfoKey(ruleId, segmentIndex, resetType);
            
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put("ruleId", ruleId);
            infoMap.put("segmentIndex", String.valueOf(segmentIndex));
            infoMap.put("resetType", resetType);
            infoMap.put("currentValue", String.valueOf(currentValue));
            infoMap.put("lastUpdateTime", String.valueOf(System.currentTimeMillis()));
            infoMap.put("lastUpdateDate", DateUtil.now());
            
            stringRedisTemplate.opsForHash().putAll(infoKey, infoMap);
            
            // 设置信息键的过期时间（比流水号键稍长一些）
            stringRedisTemplate.expire(infoKey, 366, TimeUnit.DAYS);
            
        } catch (Exception e) {
            log.warn("记录流水号信息失败", e);
        }
    }
}