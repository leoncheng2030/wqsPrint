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

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.label.modular.coderule.entity.WqsCodeRule;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRuleAddParam;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRuleEditParam;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRuleIdParam;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRulePageParam;
import java.io.IOException;
import java.util.List;

/**
 * 编码规则表Service接口
 *
 * @author jetox
 * @date  2025/07/23 19:41
 **/
public interface WqsCodeRuleService extends IService<WqsCodeRule> {

    /**
     * 获取编码规则表分页
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    Page<WqsCodeRule> page(WqsCodeRulePageParam wqsCodeRulePageParam);

    /**
     * 添加编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    void add(WqsCodeRuleAddParam wqsCodeRuleAddParam);

    /**
     * 编辑编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    void edit(WqsCodeRuleEditParam wqsCodeRuleEditParam);

    /**
     * 删除编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    void delete(List<WqsCodeRuleIdParam> wqsCodeRuleIdParamList);

    /**
     * 获取编码规则表详情
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    WqsCodeRule detail(WqsCodeRuleIdParam wqsCodeRuleIdParam);

    /**
     * 获取编码规则表详情
     *
     * @author jetox
     * @date  2025/07/23 19:41
     **/
    WqsCodeRule queryEntity(String id);

    /**
     * 下载编码规则表导入模板
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    void downloadImportTemplate(HttpServletResponse response) throws IOException;

    /**
     * 导入编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     **/
    JSONObject importData(MultipartFile file);

    /**
     * 导出编码规则表
     *
     * @author jetox
     * @date  2025/07/23 19:41
     */
    void exportData(List<WqsCodeRuleIdParam> wqsCodeRuleIdParamList, HttpServletResponse response) throws IOException;
}
