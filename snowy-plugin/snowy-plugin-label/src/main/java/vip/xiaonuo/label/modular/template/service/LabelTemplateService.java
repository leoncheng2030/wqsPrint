package vip.xiaonuo.label.modular.template.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.label.modular.template.entity.LabelTemplate;
import vip.xiaonuo.label.modular.template.param.*;

import java.util.List;

/**
 * 标签打印模板Service接口
 *
 * @author Gemini
 * @date 2025-07-20
 */
public interface LabelTemplateService extends IService<LabelTemplate> {

    /**
     * 分页查询
     *
     * @param pageParam
     * @return
     */
    Page<LabelTemplate> page(LabelTemplatePageParam pageParam);

    /**
     * 新增
     *
     * @param addParam
     */
    void add(LabelTemplateAddParam addParam);

    /**
     * 编辑
     *
     * @param editParam
     */
    void edit(LabelTemplateEditParam editParam);

    /**
     * 获取详情
     *
     * @param templateName
     * @return
     */
    List<String> getIdsByName(String templateName);

    /**
     * 删除
     *
     * @param idParamList
     */
    void delete(List<LabelTemplateIdParam> idParamList);

    /**
     * 获取详情
     *
     * @param idParam
     * @return
     */
    LabelTemplate detail(LabelTemplateIdParam idParam);

    void design(LabelTemplateDesignParam labelTemplateDesignParam);

}
