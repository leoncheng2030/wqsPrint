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
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vip.xiaonuo.label.modular.coderule.service.BatchBarcodeService;
import vip.xiaonuo.label.modular.coderule.service.BarcodeGeneratorService;
import vip.xiaonuo.label.modular.coderule.service.SerialNumberService;
import vip.xiaonuo.common.exception.CommonException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批量条码生成服务实现类
 *
 * @author jetox
 * @date 2025/07/23 22:00
 **/
@Slf4j
@Service
public class BatchBarcodeServiceImpl implements BatchBarcodeService {

    @Autowired
    private BarcodeGeneratorService barcodeGeneratorService;

    @Autowired
    private SerialNumberService serialNumberService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 内存中的任务状态缓存（生产环境建议使用Redis）
    private final Map<String, Map<String, Object>> taskStatusCache = new ConcurrentHashMap<>();

    private static final String BATCH_TASK_KEY_PREFIX = "barcode:batch:task:";
    private static final int MAX_BATCH_SIZE = 1000; // 最大批次大小
    private static final int BATCH_CHUNK_SIZE = 50;  // 分块处理大小

    @Override
    public Map<String, Object> batchGenerateBarcodes(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Integer width, Integer height) {
        if (paramsList == null || paramsList.isEmpty()) {
            throw new CommonException("参数列表不能为空");
        }

        if (paramsList.size() > MAX_BATCH_SIZE) {
            throw new CommonException("批量生成数量不能超过{}个", MAX_BATCH_SIZE);
        }

        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        List<String> successImages = new ArrayList<>();
        List<Map<String, Object>> errorItems = new ArrayList<>();

        try {
            for (int i = 0; i < paramsList.size(); i++) {
                try {
                    Map<String, Object> params = paramsList.get(i);
                    String imageBase64 = barcodeGeneratorService.generateBarcodeImage(ruleId, params, barcodeType, width, height);
                    successImages.add(imageBase64);
                } catch (Exception e) {
                    Map<String, Object> errorItem = new HashMap<>();
                    errorItem.put("index", i);
                    errorItem.put("params", paramsList.get(i));
                    errorItem.put("error", e.getMessage());
                    errorItems.add(errorItem);
                    
                    log.warn("批量生成条码时第{}项失败：{}", i, e.getMessage());
                }
            }

            result.put("success", true);
            result.put("totalCount", paramsList.size());
            result.put("successCount", successImages.size());
            result.put("errorCount", errorItems.size());
            result.put("images", successImages);
            result.put("errors", errorItems);
            result.put("processingTime", System.currentTimeMillis() - startTime);
            result.put("timestamp", DateUtil.now());

        } catch (Exception e) {
            log.error("批量生成条码失败：", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("processingTime", System.currentTimeMillis() - startTime);
        }

        return result;
    }

    @Override
    public String asyncBatchGenerateBarcodes(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Integer width, Integer height) {
        if (paramsList == null || paramsList.isEmpty()) {
            throw new CommonException("参数列表不能为空");
        }

        if (paramsList.size() > MAX_BATCH_SIZE) {
            throw new CommonException("批量生成数量不能超过{}个", MAX_BATCH_SIZE);
        }

        // 生成任务ID
        String taskId = "batch_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        
        // 初始化任务状态
        Map<String, Object> taskStatus = new HashMap<>();
        taskStatus.put("taskId", taskId);
        taskStatus.put("status", "PROCESSING");
        taskStatus.put("totalCount", paramsList.size());
        taskStatus.put("processedCount", 0);
        taskStatus.put("successCount", 0);
        taskStatus.put("errorCount", 0);
        taskStatus.put("startTime", System.currentTimeMillis());
        taskStatus.put("ruleId", ruleId);
        taskStatus.put("barcodeType", barcodeType);
        taskStatus.put("width", width);
        taskStatus.put("height", height);
        taskStatus.put("progress", 0.0);

        taskStatusCache.put(taskId, taskStatus);

        // 异步处理
        processAsyncBatchGeneration(taskId, ruleId, paramsList, barcodeType, width, height);

        return taskId;
    }

    @Async
    private void processAsyncBatchGeneration(String taskId, String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Integer width, Integer height) {
        Map<String, Object> taskStatus = taskStatusCache.get(taskId);
        if (taskStatus == null) {
            return;
        }

        List<String> successImages = new ArrayList<>();
        List<Map<String, Object>> errorItems = new ArrayList<>();
        AtomicInteger processedCount = new AtomicInteger(0);

        try {
            // 分块处理
            for (int start = 0; start < paramsList.size(); start += BATCH_CHUNK_SIZE) {
                int end = Math.min(start + BATCH_CHUNK_SIZE, paramsList.size());
                List<Map<String, Object>> chunk = paramsList.subList(start, end);

                for (int i = 0; i < chunk.size(); i++) {
                    int globalIndex = start + i;
                    try {
                        Map<String, Object> params = chunk.get(i);
                        String imageBase64 = barcodeGeneratorService.generateBarcodeImage(ruleId, params, barcodeType, width, height);
                        successImages.add(imageBase64);
                        
                        // 更新成功计数
                        taskStatus.put("successCount", (Integer)taskStatus.get("successCount") + 1);
                    } catch (Exception e) {
                        Map<String, Object> errorItem = new HashMap<>();
                        errorItem.put("index", globalIndex);
                        errorItem.put("params", chunk.get(i));
                        errorItem.put("error", e.getMessage());
                        errorItems.add(errorItem);
                        
                        // 更新错误计数
                        taskStatus.put("errorCount", (Integer)taskStatus.get("errorCount") + 1);
                        
                        log.warn("异步批量生成条码时第{}项失败：{}", globalIndex, e.getMessage());
                    }

                    // 更新处理进度
                    int processed = processedCount.incrementAndGet();
                    taskStatus.put("processedCount", processed);
                    taskStatus.put("progress", (double)processed / paramsList.size() * 100);
                }

                // 每处理一个块后短暂休息，避免占用过多资源
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // 完成处理
            taskStatus.put("status", "COMPLETED");
            taskStatus.put("endTime", System.currentTimeMillis());
            taskStatus.put("processingTime", (Long)taskStatus.get("endTime") - (Long)taskStatus.get("startTime"));
            taskStatus.put("images", successImages);
            taskStatus.put("errors", errorItems);
            
            // 将结果缓存到Redis（可选）
            cacheTaskResult(taskId, taskStatus);

        } catch (Exception e) {
            log.error("异步批量生成条码失败，任务ID：{}", taskId, e);
            taskStatus.put("status", "FAILED");
            taskStatus.put("error", e.getMessage());
            taskStatus.put("endTime", System.currentTimeMillis());
        }
    }

    @Override
    public Map<String, Object> getBatchTaskStatus(String taskId) {
        if (StrUtil.isEmpty(taskId)) {
            throw new CommonException("任务ID不能为空");
        }

        Map<String, Object> taskStatus = taskStatusCache.get(taskId);
        if (taskStatus == null) {
            // 尝试从Redis获取
            taskStatus = getCachedTaskResult(taskId);
            if (taskStatus == null) {
                throw new CommonException("任务不存在或已过期，任务ID：{}", taskId);
            }
        }

        return new HashMap<>(taskStatus);
    }

    @Override
    public Map<String, Object> batchGenerateByCount(String ruleId, Map<String, Object> baseParams, int count, String barcodeType, Integer width, Integer height) {
        if (count <= 0 || count > MAX_BATCH_SIZE) {
            throw new CommonException("生成数量必须在1-{}之间", MAX_BATCH_SIZE);
        }

        List<Map<String, Object>> paramsList = new ArrayList<>();
        
        // 为每个条码创建参数（主要是为了触发流水号递增）
        for (int i = 0; i < count; i++) {
            Map<String, Object> params = new HashMap<>(baseParams);
            params.put("_batchIndex", i + 1); // 添加批次索引
            paramsList.add(params);
        }

        return batchGenerateBarcodes(ruleId, paramsList, barcodeType, width, height);
    }

    @Override
    public Map<String, Object> batchGenerateByTemplate(Map<String, Object> template) {
        String ruleId = (String) template.get("ruleId");
        String barcodeType = (String) template.getOrDefault("barcodeType", "CODE128");
        Integer width = (Integer) template.getOrDefault("width", 300);
        Integer height = (Integer) template.getOrDefault("height", 150);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> paramsList = (List<Map<String, Object>>) template.get("paramsList");
        
        if (StrUtil.isEmpty(ruleId)) {
            throw new CommonException("模板中必须指定规则ID");
        }
        
        if (paramsList == null || paramsList.isEmpty()) {
            throw new CommonException("模板中必须指定参数列表");
        }

        return batchGenerateBarcodes(ruleId, paramsList, barcodeType, width, height);
    }

    @Override
    public boolean cancelBatchTask(String taskId) {
        if (StrUtil.isEmpty(taskId)) {
            return false;
        }

        Map<String, Object> taskStatus = taskStatusCache.get(taskId);
        if (taskStatus != null && "PROCESSING".equals(taskStatus.get("status"))) {
            taskStatus.put("status", "CANCELLED");
            taskStatus.put("endTime", System.currentTimeMillis());
            
            log.info("批量生成任务已取消，任务ID：{}", taskId);
            return true;
        }

        return false;
    }

    /**
     * 缓存任务结果到Redis
     */
    private void cacheTaskResult(String taskId, Map<String, Object> taskStatus) {
        try {
            String cacheKey = BATCH_TASK_KEY_PREFIX + taskId;
            
            // 序列化任务状态（简化版本，生产环境建议使用JSON）
            Map<String, String> cacheData = new HashMap<>();
            taskStatus.forEach((key, value) -> {
                if (!(value instanceof List)) { // 跳过复杂对象
                    cacheData.put(key, String.valueOf(value));
                }
            });
            
            stringRedisTemplate.opsForHash().putAll(cacheKey, cacheData);
            stringRedisTemplate.expire(cacheKey, 1, TimeUnit.HOURS); // 缓存1小时
            
        } catch (Exception e) {
            log.warn("缓存任务结果失败，任务ID：{}", taskId, e);
        }
    }

    /**
     * 从Redis获取缓存的任务结果
     */
    private Map<String, Object> getCachedTaskResult(String taskId) {
        try {
            String cacheKey = BATCH_TASK_KEY_PREFIX + taskId;
            Map<Object, Object> cacheData = stringRedisTemplate.opsForHash().entries(cacheKey);
            
            if (cacheData.isEmpty()) {
                return null;
            }
            
            Map<String, Object> taskStatus = new HashMap<>();
            cacheData.forEach((key, value) -> taskStatus.put(String.valueOf(key), value));
            
            return taskStatus;
            
        } catch (Exception e) {
            log.warn("获取缓存任务结果失败，任务ID：{}", taskId, e);
            return null;
        }
    }
}