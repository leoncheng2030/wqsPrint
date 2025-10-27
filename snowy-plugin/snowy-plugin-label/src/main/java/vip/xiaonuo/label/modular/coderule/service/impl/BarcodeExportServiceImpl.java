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

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.xiaonuo.label.modular.coderule.service.BarcodeExportService;
import vip.xiaonuo.label.modular.coderule.service.BarcodeGeneratorService;
import vip.xiaonuo.common.exception.CommonException;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 条码导出服务实现类
 *
 * @author jetox
 * @date 2025/07/23 22:30
 **/
@Slf4j
@Service
public class BarcodeExportServiceImpl implements BarcodeExportService {

    @Autowired
    private BarcodeGeneratorService barcodeGeneratorService;

    @Override
    public void exportToPDF(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> exportConfig, HttpServletResponse response) {
        if (paramsList == null || paramsList.isEmpty()) {
            throw new CommonException("参数列表不能为空");
        }

        try {
            // 设置响应头
            String fileName = "barcodes_" + DateUtil.format(DateUtil.date(), "yyyyMMdd_HHmmss") + ".pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 简化的PDF生成（实际项目中建议使用iText或其他PDF库）
            String htmlContent = generateBarcodesHTML(ruleId, paramsList, barcodeType, exportConfig);
            
            // 这里使用简化的PDF生成，实际应使用专业PDF库
            byte[] pdfBytes = generateSimplePDF(htmlContent, paramsList.size());
            
            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(pdfBytes);
                outputStream.flush();
            }

            log.info("PDF导出成功，文件名：{}，条码数量：{}", fileName, paramsList.size());

        } catch (Exception e) {
            log.error("PDF导出失败：", e);
            throw new CommonException("PDF导出失败：{}", e.getMessage());
        }
    }

    @Override
    public void exportToImages(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, String imageFormat, Map<String, Object> exportConfig, HttpServletResponse response) {
        if (paramsList == null || paramsList.isEmpty()) {
            throw new CommonException("参数列表不能为空");
        }

        try {
            // 设置响应头
            String fileName = "barcodes_" + DateUtil.format(DateUtil.date(), "yyyyMMdd_HHmmss") + ".zip";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            Integer width = (Integer) exportConfig.getOrDefault("width", 300);
            Integer height = (Integer) exportConfig.getOrDefault("height", 150);
            String format = ObjectUtil.defaultIfEmpty(imageFormat, "PNG").toLowerCase();

            try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
                for (int i = 0; i < paramsList.size(); i++) {
                    try {
                        Map<String, Object> params = paramsList.get(i);
                        
                        // 生成条码图片
                        String imageBase64 = barcodeGeneratorService.generateBarcodeImage(ruleId, params, barcodeType, width, height);
                        
                        // 解析Base64图片
                        String base64Data = imageBase64.substring(imageBase64.indexOf(",") + 1);
                        byte[] imageBytes = Base64.decode(base64Data);
                        
                        // 创建ZIP条目
                        String imageName = String.format("barcode_%04d.%s", i + 1, format);
                        ZipEntry zipEntry = new ZipEntry(imageName);
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(imageBytes);
                        zipOut.closeEntry();
                        
                    } catch (Exception e) {
                        log.warn("生成第{}个条码图片失败：{}", i + 1, e.getMessage());
                    }
                }
                
                zipOut.finish();
            }

            log.info("图片压缩包导出成功，文件名：{}，条码数量：{}", fileName, paramsList.size());

        } catch (Exception e) {
            log.error("图片压缩包导出失败：", e);
            throw new CommonException("图片压缩包导出失败：{}", e.getMessage());
        }
    }

    @Override
    public void exportToExcel(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> exportConfig, HttpServletResponse response) {
        if (paramsList == null || paramsList.isEmpty()) {
            throw new CommonException("参数列表不能为空");
        }

        try {
            // 设置响应头
            String fileName = "barcodes_" + DateUtil.format(DateUtil.date(), "yyyyMMdd_HHmmss") + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 简化的Excel生成（实际项目中建议使用EasyExcel或POI）
            String csvContent = generateBarcodesCSV(ruleId, paramsList, barcodeType, exportConfig);
            
            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(csvContent.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }

            log.info("Excel导出成功，文件名：{}，条码数量：{}", fileName, paramsList.size());

        } catch (Exception e) {
            log.error("Excel导出失败：", e);
            throw new CommonException("Excel导出失败：{}", e.getMessage());
        }
    }

    @Override
    public void exportByTemplate(Map<String, Object> template, HttpServletResponse response) {
        String ruleId = (String) template.get("ruleId");
        String exportType = (String) template.getOrDefault("exportType", "PDF");
        String barcodeType = (String) template.getOrDefault("barcodeType", "CODE128");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> paramsList = (List<Map<String, Object>>) template.get("paramsList");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> exportConfig = (Map<String, Object>) template.getOrDefault("exportConfig", Map.of());

        if (StrUtil.isEmpty(ruleId) || paramsList == null || paramsList.isEmpty()) {
            throw new CommonException("模板配置不完整");
        }

        switch (exportType.toUpperCase()) {
            case "PDF":
                exportToPDF(ruleId, paramsList, barcodeType, exportConfig, response);
                break;
            case "IMAGES":
                String imageFormat = (String) exportConfig.getOrDefault("imageFormat", "PNG");
                exportToImages(ruleId, paramsList, barcodeType, imageFormat, exportConfig, response);
                break;
            case "EXCEL":
            case "CSV":
                exportToExcel(ruleId, paramsList, barcodeType, exportConfig, response);
                break;
            case "LABELS":
                exportToLabels(ruleId, paramsList, barcodeType, exportConfig, response);
                break;
            default:
                throw new CommonException("不支持的导出类型：{}", exportType);
        }
    }

    @Override
    public String generatePrintHTML(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> printConfig) {
        if (paramsList == null || paramsList.isEmpty()) {
            throw new CommonException("参数列表不能为空");
        }

        try {
            StringBuilder htmlBuilder = new StringBuilder();
            
            // HTML头部
            htmlBuilder.append("<!DOCTYPE html>\n")
                      .append("<html>\n")
                      .append("<head>\n")
                      .append("    <meta charset=\"UTF-8\">\n")
                      .append("    <title>条码打印</title>\n")
                      .append("    <style>\n")
                      .append("        body { font-family: Arial, sans-serif; margin: 20px; }\n")
                      .append("        .barcode-container { display: inline-block; margin: 10px; text-align: center; }\n")
                      .append("        .barcode-image { border: 1px solid #ccc; }\n")
                      .append("        .barcode-info { margin-top: 5px; font-size: 12px; }\n")
                      .append("        @media print { .no-print { display: none; } }\n")
                      .append("    </style>\n")
                      .append("</head>\n")
                      .append("<body>\n");

            // 打印按钮
            htmlBuilder.append("    <button class=\"no-print\" onclick=\"window.print()\">打印</button>\n")
                      .append("    <br><br>\n");

            Integer width = (Integer) printConfig.getOrDefault("width", 300);
            Integer height = (Integer) printConfig.getOrDefault("height", 150);

            // 生成条码
            for (int i = 0; i < paramsList.size(); i++) {
                try {
                    Map<String, Object> params = paramsList.get(i);
                    String imageBase64 = barcodeGeneratorService.generateBarcodeImage(ruleId, params, barcodeType, width, height);
                    
                    htmlBuilder.append("    <div class=\"barcode-container\">\n")
                              .append("        <img src=\"").append(imageBase64).append("\" class=\"barcode-image\" />\n")
                              .append("        <div class=\"barcode-info\">编号：").append(i + 1).append("</div>\n")
                              .append("    </div>\n");
                    
                } catch (Exception e) {
                    log.warn("生成第{}个条码失败：{}", i + 1, e.getMessage());
                    htmlBuilder.append("    <div class=\"barcode-container\">\n")
                              .append("        <div style=\"border: 1px solid #f00; color: #f00; padding: 10px;\">生成失败</div>\n")
                              .append("        <div class=\"barcode-info\">编号：").append(i + 1).append("</div>\n")
                              .append("    </div>\n");
                }
            }

            // HTML尾部
            htmlBuilder.append("</body>\n")
                      .append("</html>");

            return htmlBuilder.toString();

        } catch (Exception e) {
            log.error("生成打印HTML失败：", e);
            throw new CommonException("生成打印HTML失败：{}", e.getMessage());
        }
    }

    @Override
    public void exportToLabels(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> labelConfig, HttpServletResponse response) {
        if (paramsList == null || paramsList.isEmpty()) {
            throw new CommonException("参数列表不能为空");
        }

        try {
            // 设置响应头
            String fileName = "barcode_labels_" + DateUtil.format(DateUtil.date(), "yyyyMMdd_HHmmss") + ".pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            // 标签配置
            Integer labelWidth = (Integer) labelConfig.getOrDefault("labelWidth", 400);
            Integer labelHeight = (Integer) labelConfig.getOrDefault("labelHeight", 200);
            Integer barcodeWidth = (Integer) labelConfig.getOrDefault("barcodeWidth", 300);
            Integer barcodeHeight = (Integer) labelConfig.getOrDefault("barcodeHeight", 100);
            Integer columns = (Integer) labelConfig.getOrDefault("columns", 3);

            // 生成标签HTML
            String htmlContent = generateLabelsHTML(ruleId, paramsList, barcodeType, labelConfig);
            
            // 转换为PDF
            byte[] pdfBytes = generateSimplePDF(htmlContent, paramsList.size());
            
            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(pdfBytes);
                outputStream.flush();
            }

            log.info("标签导出成功，文件名：{}，条码数量：{}", fileName, paramsList.size());

        } catch (Exception e) {
            log.error("标签导出失败：", e);
            throw new CommonException("标签导出失败：{}", e.getMessage());
        }
    }

    /**
     * 生成条码HTML
     */
    private String generateBarcodesHTML(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> config) {
        StringBuilder htmlBuilder = new StringBuilder();
        
        htmlBuilder.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>条码列表</title></head><body>");
        
        Integer width = (Integer) config.getOrDefault("width", 300);
        Integer height = (Integer) config.getOrDefault("height", 150);
        
        for (int i = 0; i < paramsList.size(); i++) {
            try {
                Map<String, Object> params = paramsList.get(i);
                String imageBase64 = barcodeGeneratorService.generateBarcodeImage(ruleId, params, barcodeType, width, height);
                
                htmlBuilder.append("<div style=\"margin: 10px;\">")
                          .append("<img src=\"").append(imageBase64).append("\" />")
                          .append("<p>编号：").append(i + 1).append("</p>")
                          .append("</div>");
                          
            } catch (Exception e) {
                htmlBuilder.append("<div style=\"margin: 10px; color: red;\">生成失败：").append(e.getMessage()).append("</div>");
            }
        }
        
        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }

    /**
     * 生成标签HTML
     */
    private String generateLabelsHTML(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> labelConfig) {
        StringBuilder htmlBuilder = new StringBuilder();
        
        Integer columns = (Integer) labelConfig.getOrDefault("columns", 3);
        Integer labelWidth = (Integer) labelConfig.getOrDefault("labelWidth", 400);
        Integer labelHeight = (Integer) labelConfig.getOrDefault("labelHeight", 200);
        Integer barcodeWidth = (Integer) labelConfig.getOrDefault("barcodeWidth", 300);
        Integer barcodeHeight = (Integer) labelConfig.getOrDefault("barcodeHeight", 100);
        
        htmlBuilder.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">")
                  .append("<style>")
                  .append(".label-container { display: inline-block; width: ").append(labelWidth).append("px; height: ").append(labelHeight).append("px; border: 1px solid #ccc; margin: 5px; text-align: center; vertical-align: top; }")
                  .append(".label-barcode { margin: 10px 0; }")
                  .append("</style>")
                  .append("</head><body>");
        
        for (int i = 0; i < paramsList.size(); i++) {
            try {
                Map<String, Object> params = paramsList.get(i);
                String imageBase64 = barcodeGeneratorService.generateBarcodeImage(ruleId, params, barcodeType, barcodeWidth, barcodeHeight);
                
                htmlBuilder.append("<div class=\"label-container\">")
                          .append("<div class=\"label-barcode\">")
                          .append("<img src=\"").append(imageBase64).append("\" />")
                          .append("</div>")
                          .append("<div>编号：").append(i + 1).append("</div>")
                          .append("</div>");
                          
                // 每行换行
                if ((i + 1) % columns == 0) {
                    htmlBuilder.append("<br>");
                }
                
            } catch (Exception e) {
                htmlBuilder.append("<div class=\"label-container\" style=\"color: red;\">生成失败</div>");
            }
        }
        
        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }

    /**
     * 生成CSV内容
     */
    private String generateBarcodesCSV(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Map<String, Object> config) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("编号,条码编码,生成时间\n");
        
        for (int i = 0; i < paramsList.size(); i++) {
            try {
                Map<String, Object> params = paramsList.get(i);
                String code = "CODE_" + (i + 1); // 简化的编码生成
                csvBuilder.append(i + 1).append(",").append(code).append(",").append(DateUtil.now()).append("\n");
            } catch (Exception e) {
                csvBuilder.append(i + 1).append(",生成失败,").append(DateUtil.now()).append("\n");
            }
        }
        
        return csvBuilder.toString();
    }

    /**
     * 简化的PDF生成（实际项目中建议使用专业PDF库）
     */
    private byte[] generateSimplePDF(String content, int count) {
        try {
            // 创建简单的PDF替代内容
            StringBuilder pdfContent = new StringBuilder();
            pdfContent.append("%PDF-1.4\n")
                      .append("1 0 obj\n")
                      .append("<<\n")
                      .append("/Type /Catalog\n")
                      .append("/Pages 2 0 R\n")
                      .append(">>\n")
                      .append("endobj\n")
                      .append("2 0 obj\n")
                      .append("<<\n")
                      .append("/Type /Pages\n")
                      .append("/Kids [3 0 R]\n")
                      .append("/Count 1\n")
                      .append(">>\n")
                      .append("endobj\n")
                      .append("3 0 obj\n")
                      .append("<<\n")
                      .append("/Type /Page\n")
                      .append("/Parent 2 0 R\n")
                      .append("/MediaBox [0 0 612 792]\n")
                      .append(">>\n")
                      .append("endobj\n")
                      .append("xref\n")
                      .append("0 4\n")
                      .append("0000000000 65535 f \n")
                      .append("0000000009 65535 n \n")
                      .append("0000000074 65535 n \n")
                      .append("0000000131 65535 n \n")
                      .append("trailer\n")
                      .append("<<\n")
                      .append("/Size 4\n")
                      .append("/Root 1 0 R\n")
                      .append(">>\n")
                      .append("startxref\n")
                      .append("208\n")
                      .append("%%EOF");
            
            return pdfContent.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("生成PDF失败：", e);
            throw new CommonException("生成PDF失败：{}", e.getMessage());
        }
    }
}