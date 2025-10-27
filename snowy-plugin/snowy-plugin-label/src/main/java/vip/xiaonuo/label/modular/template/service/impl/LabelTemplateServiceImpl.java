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
package vip.xiaonuo.label.modular.template.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.page.CommonPageRequest;
import vip.xiaonuo.label.modular.template.entity.LabelTemplate;
import vip.xiaonuo.label.modular.template.mapper.LabelTemplateMapper;
import vip.xiaonuo.label.modular.template.param.*;
import vip.xiaonuo.label.modular.template.service.LabelTemplateService;

import java.util.List;

/**
 * 标签模板Service接口实现类
 *
 * @author 
 * @date 2024/07/22 15:03
 **/
@Service
public class LabelTemplateServiceImpl extends ServiceImpl<LabelTemplateMapper, LabelTemplate> implements LabelTemplateService {

    @Override
    public Page<LabelTemplate> page(LabelTemplatePageParam labelTemplatePageParam) {
        QueryWrapper<LabelTemplate> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(labelTemplatePageParam.getType())){
            queryWrapper.lambda().eq(LabelTemplate::getType, labelTemplatePageParam.getType());
        }
        if (ObjectUtil.isNotEmpty(labelTemplatePageParam.getCode())){
            queryWrapper.lambda().like(LabelTemplate::getCode, labelTemplatePageParam.getCode());
        }
        if (ObjectUtil.isNotEmpty(labelTemplatePageParam.getStatus())){
            queryWrapper.lambda().eq(LabelTemplate::getStatus, labelTemplatePageParam.getStatus());
        }
        if (ObjectUtil.isNotEmpty(labelTemplatePageParam.getRequiresDetails())){
            queryWrapper.lambda().eq(LabelTemplate::getRequiresDetails, labelTemplatePageParam.getRequiresDetails());
        }
        queryWrapper.orderByDesc("CREATE_TIME");
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Override
    public void add(LabelTemplateAddParam labelTemplateAddParam) {
        LabelTemplate labelTemplate = BeanUtil.copyProperties(labelTemplateAddParam, LabelTemplate.class);
        this.save(labelTemplate);
    }

    @Override
    public void edit(LabelTemplateEditParam labelTemplateEditParam) {
        LabelTemplate labelTemplate = this.queryEntity(labelTemplateEditParam.getId());
        BeanUtil.copyProperties(labelTemplateEditParam, labelTemplate);
        this.updateById(labelTemplate);
    }

    @Override
    public List<String> getIdsByName(String templateName) {
        QueryWrapper<LabelTemplate> queryWrapper = new QueryWrapper<LabelTemplate>();
        queryWrapper.lambda().like(LabelTemplate::getName, templateName);
        return this.list(queryWrapper).stream().map(LabelTemplate::getId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<LabelTemplateIdParam> labelTemplateIdParamList) {
        this.removeByIds(CollUtil.getFieldValues(labelTemplateIdParamList, "id"));
    }


    @Override
    public LabelTemplate detail(LabelTemplateIdParam labelTemplateIdParam) {
        return this.queryEntity(labelTemplateIdParam.getId());
    }

    @Override
    public void design(LabelTemplateDesignParam labelTemplateDesignParam) {
        LabelTemplate labelTemplate = this.queryEntity(labelTemplateDesignParam.getId());
        labelTemplate.setTemplateContent(labelTemplateDesignParam.getTemplateContent());
        this.updateById(labelTemplate);
    }

    private LabelTemplate queryEntity(String id) {
        LabelTemplate labelTemplate = this.getById(id);
        if (ObjectUtil.isEmpty(labelTemplate)) {
            // 此处可以抛出业务异常
        }
        return labelTemplate;
    }
}
