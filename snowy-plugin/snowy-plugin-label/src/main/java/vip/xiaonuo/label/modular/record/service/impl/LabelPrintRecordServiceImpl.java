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
package vip.xiaonuo.label.modular.record.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.page.CommonPageRequest;
import vip.xiaonuo.label.modular.record.entity.LabelPrintRecord;
import vip.xiaonuo.label.modular.record.mapper.LabelPrintRecordMapper;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordAddParam;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordEditParam;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordIdParam;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordPageParam;
import vip.xiaonuo.label.modular.record.service.LabelPrintRecordService;
import vip.xiaonuo.label.modular.template.service.LabelTemplateService;

import java.util.List;

/**
 * 打印记录Service接口实现类
 *
 * @author 
 * @date 2024/07/22 15:03
 **/
@Service
public class LabelPrintRecordServiceImpl extends ServiceImpl<LabelPrintRecordMapper, LabelPrintRecord> implements LabelPrintRecordService {
    @Resource
    private LabelTemplateService labelTemplateService;
    @Override
    public Page<LabelPrintRecord> page(LabelPrintRecordPageParam labelPrintRecordPageParam) {
        QueryWrapper<LabelPrintRecord> queryWrapper = new QueryWrapper<>();
        if (ObjectUtil.isNotEmpty(labelPrintRecordPageParam.getTemplateName())) {
            List<String> idsByName = labelTemplateService.getIdsByName(labelPrintRecordPageParam.getTemplateName());
            queryWrapper.in("TEMPLATE_ID", idsByName);
        }
        if (ObjectUtil.isNotEmpty(labelPrintRecordPageParam.getBusinessKey())) {
            queryWrapper.eq("BUSINESS_KEY", labelPrintRecordPageParam.getBusinessKey());
        }
        if (ObjectUtil.isNotEmpty(labelPrintRecordPageParam.getPrintStatus())) {
            queryWrapper.eq("PRINT_STATUS", labelPrintRecordPageParam.getPrintStatus());
        }
        queryWrapper.orderByDesc("CREATE_TIME");
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Override
    public void add(LabelPrintRecordAddParam labelPrintRecordAddParam) {
        LabelPrintRecord labelPrintRecord = BeanUtil.copyProperties(labelPrintRecordAddParam, LabelPrintRecord.class);
        this.save(labelPrintRecord);
    }

    @Override
    public void edit(LabelPrintRecordEditParam labelPrintRecordEditParam) {
        LabelPrintRecord labelPrintRecord = this.queryEntity(labelPrintRecordEditParam.getId());
        BeanUtil.copyProperties(labelPrintRecordEditParam, labelPrintRecord);
        this.updateById(labelPrintRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<LabelPrintRecordIdParam> labelPrintRecordIdParamList) {
        this.removeByIds(CollUtil.getFieldValues(labelPrintRecordIdParamList, "id"));
    }

    @Override
    public LabelPrintRecord detail(LabelPrintRecordIdParam labelPrintRecordIdParam) {
        return this.queryEntity(labelPrintRecordIdParam.getId());
    }

    public LabelPrintRecord queryEntity(String id) {
        LabelPrintRecord labelPrintRecord = this.getById(id);
        if (ObjectUtil.isEmpty(labelPrintRecord)) {
            // 此处可以抛出业务异常
        }
        return labelPrintRecord;
    }
}
