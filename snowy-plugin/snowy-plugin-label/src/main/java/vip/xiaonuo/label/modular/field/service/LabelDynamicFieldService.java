package vip.xiaonuo.label.modular.field.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.label.modular.field.entity.LabelDynamicField;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldAddParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldEditParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldIdParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldPageParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldSortParam;

import java.util.List;

/**
 * 标签打印动态字段定义Service接口
 *
 * @author Gemini
 * @date 2025-07-20
 */
public interface LabelDynamicFieldService extends IService<LabelDynamicField> {

    /**
     * 分页查询
     *
     * @param pageParam
     * @return
     */
    Page<LabelDynamicField> page(LabelDynamicFieldPageParam pageParam);

    /**
     * 新增
     *
     * @param addParam
     */
    void add(LabelDynamicFieldAddParam addParam);

    /**
     * 编辑
     *
     * @param editParam
     */
    void edit(LabelDynamicFieldEditParam editParam);

    /**
     * 删除
     *
     * @param idParamList
     */
    void delete(List<LabelDynamicFieldIdParam> idParamList);

    /**
     * 获取详情
     *
     * @param idParam
     * @return
     */
    LabelDynamicField detail(LabelDynamicFieldIdParam idParam);
    
    /**
     * 更新字段排序
     *
     * @param sortParam 排序参数
     */
    void sort(LabelDynamicFieldSortParam sortParam);
}
