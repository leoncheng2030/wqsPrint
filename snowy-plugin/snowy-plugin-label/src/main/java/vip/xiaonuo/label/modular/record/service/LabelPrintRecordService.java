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
package vip.xiaonuo.label.modular.record.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.label.modular.record.entity.LabelPrintRecord;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordAddParam;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordEditParam;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordIdParam;
import vip.xiaonuo.label.modular.record.param.LabelPrintRecordPageParam;

import java.util.List;

/**
 * 打印记录Service接口
 *
 * @author 
 * @date 2024/07/22 15:03
 **/
public interface LabelPrintRecordService extends IService<LabelPrintRecord> {

    /**
     * 获取打印记录分页
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    Page<LabelPrintRecord> page(LabelPrintRecordPageParam labelPrintRecordPageParam);

    /**
     * 添加打印记录
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    void add(LabelPrintRecordAddParam labelPrintRecordAddParam);

    /**
     * 编辑打印记录
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    void edit(LabelPrintRecordEditParam labelPrintRecordEditParam);

    /**
     * 删除打印记录
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    void delete(List<LabelPrintRecordIdParam> labelPrintRecordIdParamList);

    /**
     * 获取打印记录详情
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    LabelPrintRecord detail(LabelPrintRecordIdParam labelPrintRecordIdParam);

    /**
     * 获取打印记录详情
     *
     * @author 
     * @date 2024/07/22 15:03
     */
    LabelPrintRecord queryEntity(String id);
}
