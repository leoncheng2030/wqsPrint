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
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.xiaonuo.label.modular.coderule.service.BarcodeGeneratorService;
import vip.xiaonuo.label.modular.coderule.service.CodeSegmentGeneratorService;
import vip.xiaonuo.common.exception.CommonException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 条码生成器服务实现类
 *
 * @author jetox
 * @date 2025/07/23 20:40
 **/
@Slf4j
@Service
public class BarcodeGeneratorServiceImpl implements BarcodeGeneratorService {

    @Autowired
    private CodeSegmentGeneratorService codeSegmentGeneratorService;

    // 默认条码尺寸
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 150;
    
    // 支持的条码类型
    private static final String BARCODE_TYPE_CODE128 = "CODE128";
    private static final String BARCODE_TYPE_QR = "QR";
    private static final String BARCODE_TYPE_CODE39 = "CODE39";

    @Override
    public String generateBarcodeImage(String ruleId, Map<String, Object> params, String barcodeType, Integer width, Integer height) {
        try {
            // 生成编码文本
            String codeText = codeSegmentGeneratorService.generateCode(ruleId, params);
            
            // 生成条码图片
            return generateBarcodeFromText(codeText, barcodeType, width, height);
        } catch (Exception e) {
            log.error("生成条码图片失败：", e);
            throw new CommonException("生成条码图片失败：{}", e.getMessage());
        }
    }

    @Override
    public String generateBarcodePreviewImage(String segments, Map<String, Object> params, String barcodeType, Integer width, Integer height) {
        try {
            // 生成编码文本
            String codeText = codeSegmentGeneratorService.generateCodePreview(segments, params);
            
            // 生成条码图片
            return generateBarcodeFromText(codeText, barcodeType, width, height);
        } catch (Exception e) {
            log.error("生成条码预览图片失败：", e);
            throw new CommonException("生成条码预览图片失败：{}", e.getMessage());
        }
    }

    @Override
    public String generateBarcodeFromText(String text, String barcodeType, Integer width, Integer height) {
        if (StrUtil.isEmpty(text)) {
            throw new CommonException("要编码的文本不能为空");
        }

        try {
            // 设置默认尺寸
            int imgWidth = ObjectUtil.defaultIfNull(width, DEFAULT_WIDTH);
            int imgHeight = ObjectUtil.defaultIfNull(height, DEFAULT_HEIGHT);
            String type = ObjectUtil.defaultIfEmpty(barcodeType, BARCODE_TYPE_CODE128);

            BufferedImage barcodeImage;
            
            switch (type.toUpperCase()) {
                case BARCODE_TYPE_CODE128:
                    barcodeImage = generateCode128Barcode(text, imgWidth, imgHeight);
                    break;
                case BARCODE_TYPE_QR:
                    barcodeImage = generateQRCodeBarcode(text, imgWidth, imgHeight);
                    break;
                case BARCODE_TYPE_CODE39:
                    barcodeImage = generateCode39Barcode(text, imgWidth, imgHeight);
                    break;
                default:
                    throw new CommonException("不支持的条码类型：{}", type);
            }

            // 转换为Base64
            return imageToBase64(barcodeImage);
        } catch (Exception e) {
            log.error("根据文本生成条码失败：", e);
            throw new CommonException("根据文本生成条码失败：{}", e.getMessage());
        }
    }

    @Override
    public List<String> generateBarcodeImages(String ruleId, List<Map<String, Object>> paramsList, String barcodeType, Integer width, Integer height) {
        List<String> results = new ArrayList<>();
        
        for (Map<String, Object> params : paramsList) {
            try {
                String barcodeImage = generateBarcodeImage(ruleId, params, barcodeType, width, height);
                results.add(barcodeImage);
            } catch (Exception e) {
                log.error("批量生成条码时出错：", e);
                // 添加空值或错误标识，保持列表长度一致
                results.add("");
            }
        }
        
        return results;
    }

    /**
     * 生成Code128条码图片
     * 注：这里使用简化的文本渲染实现，实际项目中建议使用专业的条码库如ZXing
     */
    private BufferedImage generateCode128Barcode(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置背景为白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // 设置前景为黑色
        g2d.setColor(Color.BLACK);
        
        // 简化的条码渲染：绘制垂直线条
        int barWidth = Math.max(1, width / (text.length() * 8)); // 每个字符8个条
        int barHeight = height * 2 / 3; // 条码高度占2/3
        int startY = (height - barHeight) / 2;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 根据字符ASCII值生成模式
            int pattern = c % 256;
            
            for (int j = 0; j < 8; j++) {
                if ((pattern & (1 << j)) != 0) {
                    int x = i * 8 * barWidth + j * barWidth;
                    g2d.fillRect(x, startY, barWidth, barHeight);
                }
            }
        }
        
        // 绘制文本
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = (width - textWidth) / 2;
        int textY = startY + barHeight + 15;
        g2d.drawString(text, textX, textY);
        
        g2d.dispose();
        return image;
    }

    /**
     * 生成QR码图片
     * 注：这里使用简化的方块渲染实现，实际项目中建议使用专业的二维码库
     */
    private BufferedImage generateQRCodeBarcode(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置背景为白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // 设置前景为黑色
        g2d.setColor(Color.BLACK);
        
        // 简化的QR码渲染：生成伪随机方块模式
        int qrSize = Math.min(width, height) * 3 / 4;
        int startX = (width - qrSize) / 2;
        int startY = (height - qrSize) / 2;
        int moduleSize = qrSize / 25; // 25x25的模块
        
        // 基于文本内容生成确定性的模式
        boolean[][] pattern = generateQRPattern(text, 25);
        
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                if (pattern[i][j]) {
                    int x = startX + j * moduleSize;
                    int y = startY + i * moduleSize;
                    g2d.fillRect(x, y, moduleSize, moduleSize);
                }
            }
        }
        
        g2d.dispose();
        return image;
    }

    /**
     * 生成Code39条码图片
     * 注：这里使用简化实现，实际项目中建议使用专业的条码库
     */
    private BufferedImage generateCode39Barcode(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置背景为白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // 设置前景为黑色
        g2d.setColor(Color.BLACK);
        
        // 简化的Code39渲染
        int barWidth = Math.max(1, width / (text.length() * 10)); // 每个字符10个条
        int barHeight = height * 2 / 3;
        int startY = (height - barHeight) / 2;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Code39的简化模式
            int pattern = getCode39Pattern(c);
            
            for (int j = 0; j < 9; j++) { // Code39每个字符9个条
                if ((pattern & (1 << j)) != 0) {
                    int x = i * 10 * barWidth + j * barWidth;
                    int currentBarWidth = ((j % 3) == 0) ? barWidth * 3 : barWidth; // 宽窄条交替
                    g2d.fillRect(x, startY, currentBarWidth, barHeight);
                }
            }
        }
        
        // 绘制文本
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = (width - textWidth) / 2;
        int textY = startY + barHeight + 15;
        g2d.drawString(text, textX, textY);
        
        g2d.dispose();
        return image;
    }

    /**
     * 生成QR码模式
     */
    private boolean[][] generateQRPattern(String text, int size) {
        boolean[][] pattern = new boolean[size][size];
        
        // 基于文本内容生成确定性的随机种子
        int seed = text.hashCode();
        java.util.Random random = new java.util.Random(seed);
        
        // 填充模式
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // 添加定位点（左上、右上、左下角）
                if ((i < 7 && j < 7) || (i < 7 && j >= size - 7) || (i >= size - 7 && j < 7)) {
                    pattern[i][j] = (i == 0 || i == 6 || j == 0 || j == 6 || 
                                   (i >= 2 && i <= 4 && j >= 2 && j <= 4));
                } else {
                    // 其他区域随机填充
                    pattern[i][j] = random.nextDouble() > 0.5;
                }
            }
        }
        
        return pattern;
    }

    /**
     * 获取Code39字符模式
     */
    private int getCode39Pattern(char c) {
        // 简化的Code39字符模式映射
        switch (Character.toUpperCase(c)) {
            case '0': return 0b101001101;
            case '1': return 0b110100101;
            case '2': return 0b101100101;
            case '3': return 0b110110100;
            case '4': return 0b101001101;
            case '5': return 0b110100110;
            case '6': return 0b101100110;
            case '7': return 0b101001011;
            case '8': return 0b110100101;
            case '9': return 0b101100101;
            case 'A': return 0b110101001;
            case 'B': return 0b101101001;
            case 'C': return 0b110110100;
            default: return 0b101001101; // 默认模式
        }
    }

    /**
     * 将BufferedImage转换为Base64字符串
     */
    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        baos.flush();
        byte[] imageBytes = baos.toByteArray();
        baos.close();
        
        return "data:image/png;base64," + Base64.encode(imageBytes);
    }
}