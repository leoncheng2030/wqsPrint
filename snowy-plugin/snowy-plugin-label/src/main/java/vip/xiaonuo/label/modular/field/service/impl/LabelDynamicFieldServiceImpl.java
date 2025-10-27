/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vip.xiaonuo.label.modular.field.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.page.CommonPageRequest;
import vip.xiaonuo.label.modular.field.entity.LabelDynamicField;
import vip.xiaonuo.label.modular.field.mapper.LabelDynamicFieldMapper;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldAddParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldEditParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldIdParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldPageParam;
import vip.xiaonuo.label.modular.field.param.LabelDynamicFieldSortParam;
import vip.xiaonuo.label.modular.field.service.LabelDynamicFieldService;

import java.util.List;

/**
 * 动态字段Service接口实现类
 *
 * @author 
 * @date 2024/07/22 15:03
 **/
@Service
public class LabelDynamicFieldServiceImpl extends ServiceImpl<LabelDynamicFieldMapper, LabelDynamicField> implements LabelDynamicFieldService {

    @Override
    public Page<LabelDynamicField> page(LabelDynamicFieldPageParam labelDynamicFieldPageParam) {
        QueryWrapper<LabelDynamicField> queryWrapper = new QueryWrapper<>();
        
        // 按模板ID过滤
        if (ObjectUtil.isNotEmpty(labelDynamicFieldPageParam.getTemplateId())) {
            queryWrapper.eq("TEMPLATE_ID", labelDynamicFieldPageParam.getTemplateId());
        }
        
        // 其他查询条件
        if (ObjectUtil.isNotEmpty(labelDynamicFieldPageParam.getFieldKey())) {
            queryWrapper.like("FIELD_KEY", labelDynamicFieldPageParam.getFieldKey());
        }
        if (ObjectUtil.isNotEmpty(labelDynamicFieldPageParam.getTitle())) {
            queryWrapper.like("TITLE", labelDynamicFieldPageParam.getTitle());
        }
        if (ObjectUtil.isNotEmpty(labelDynamicFieldPageParam.getFieldScope())) {
            queryWrapper.eq("FIELD_SCOPE", labelDynamicFieldPageParam.getFieldScope());
        }
        if (ObjectUtil.isNotEmpty(labelDynamicFieldPageParam.getInputType())) {
            queryWrapper.eq("INPUT_TYPE", labelDynamicFieldPageParam.getInputType());
        }
        if (ObjectUtil.isNotEmpty(labelDynamicFieldPageParam.getStatus())) {
            queryWrapper.eq("STATUS", labelDynamicFieldPageParam.getStatus());
        }
        
        // 按排序码升序排序，如果排序码相同则按创建时间升序排序
        queryWrapper.orderByAsc("SORT_CODE");
        
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Override
    public void add(LabelDynamicFieldAddParam labelDynamicFieldAddParam) {
        // 检查在同一模板和字段范围内fieldKey是否已存在（排除已逻辑删除的记录）
        QueryWrapper<LabelDynamicField> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("TEMPLATE_ID", labelDynamicFieldAddParam.getTemplateId())
                   .eq("FIELD_SCOPE", labelDynamicFieldAddParam.getFieldScope())
                   .eq("FIELD_KEY", labelDynamicFieldAddParam.getFieldKey())
                   .eq("DELETE_FLAG", "NOT_DELETE"); // 排除已逻辑删除的记录，"NOT_DELETE"表示未删除
        
        if (this.count(checkWrapper) > 0) {
            throw new RuntimeException("在模板[" + labelDynamicFieldAddParam.getTemplateId() + "]的字段范围[" + labelDynamicFieldAddParam.getFieldScope() + "]中，字段键[" + labelDynamicFieldAddParam.getFieldKey() + "]已存在");
        }
        
        // 如果没有设置排序码，则获取当前最大排序码并加1
        if (labelDynamicFieldAddParam.getSortCode() == null) {
            // 查询同一模板和字段范围内的最大排序码
            QueryWrapper<LabelDynamicField> maxSortWrapper = new QueryWrapper<>();
            maxSortWrapper.eq("TEMPLATE_ID", labelDynamicFieldAddParam.getTemplateId())
                         .eq("FIELD_SCOPE", labelDynamicFieldAddParam.getFieldScope())
                         .eq("DELETE_FLAG", "NOT_DELETE") // 同样排除已逻辑删除的记录
                         .orderByDesc("SORT_CODE")
                         .last("LIMIT 1");
            
            LabelDynamicField maxSortField = this.getOne(maxSortWrapper);
            Integer maxSortCode = (maxSortField != null && maxSortField.getSortCode() != null) ? maxSortField.getSortCode() : 0;
            labelDynamicFieldAddParam.setSortCode(maxSortCode + 10); // 增加10，便于后续插入新字段
        }
        
        LabelDynamicField labelDynamicField = BeanUtil.copyProperties(labelDynamicFieldAddParam, LabelDynamicField.class);
        this.save(labelDynamicField);
    }

    @Override
    public void edit(LabelDynamicFieldEditParam labelDynamicFieldEditParam) {
        LabelDynamicField labelDynamicField = this.queryEntity(labelDynamicFieldEditParam.getId());
        
        // 检查在同一模板和字段范围内fieldKey是否已存在（排除当前记录和已逻辑删除的记录）
        QueryWrapper<LabelDynamicField> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("TEMPLATE_ID", labelDynamicFieldEditParam.getTemplateId())
                   .eq("FIELD_SCOPE", labelDynamicFieldEditParam.getFieldScope())
                   .eq("FIELD_KEY", labelDynamicFieldEditParam.getFieldKey())
                   .ne("ID", labelDynamicFieldEditParam.getId())
                   .eq("DELETE_FLAG", "NOT_DELETE"); // 排除已逻辑删除的记录，"NOT_DELETE"表示未删除
        
        if (this.count(checkWrapper) > 0) {
            throw new RuntimeException("在模板[" + labelDynamicFieldEditParam.getTemplateId() + "]的字段范围[" + labelDynamicFieldEditParam.getFieldScope() + "]中，字段键[" + labelDynamicFieldEditParam.getFieldKey() + "]已存在");
        }
        
        BeanUtil.copyProperties(labelDynamicFieldEditParam, labelDynamicField);
        this.updateById(labelDynamicField);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<LabelDynamicFieldIdParam> labelDynamicFieldIdParamList) {
        this.removeByIds(CollUtil.getFieldValues(labelDynamicFieldIdParamList, "id"));
    }

    @Override
    public LabelDynamicField detail(LabelDynamicFieldIdParam labelDynamicFieldIdParam) {
        return this.queryEntity(labelDynamicFieldIdParam.getId());
    }

    public LabelDynamicField queryEntity(String id) {
        LabelDynamicField labelDynamicField = this.getById(id);
        if (ObjectUtil.isEmpty(labelDynamicField)) {
            // 此处可以抛出业务异常
        }
        return labelDynamicField;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sort(LabelDynamicFieldSortParam sortParam) {
        // 获取排序数据列表
        List<LabelDynamicFieldSortParam.SortItem> sortDataList = sortParam.getSortData();
        
        // 批量更新排序码
        for (LabelDynamicFieldSortParam.SortItem sortItem : sortDataList) {
            // 更新字段的排序码
            LabelDynamicField field = this.getById(sortItem.getId());
            if (field != null) {
                field.setSortCode(sortItem.getSortCode());
                this.updateById(field);
            }
        }
    }
}