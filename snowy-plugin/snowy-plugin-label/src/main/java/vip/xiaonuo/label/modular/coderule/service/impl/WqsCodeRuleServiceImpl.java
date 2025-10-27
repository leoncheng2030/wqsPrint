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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.page.CommonPageRequest;
import vip.xiaonuo.label.modular.coderule.entity.WqsCodeRule;
import vip.xiaonuo.label.modular.coderule.mapper.WqsCodeRuleMapper;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRuleAddParam;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRuleEditParam;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRuleIdParam;
import vip.xiaonuo.label.modular.coderule.param.WqsCodeRulePageParam;
import vip.xiaonuo.label.modular.coderule.service.WqsCodeRuleService;

import vip.xiaonuo.common.util.CommonDownloadUtil;
import vip.xiaonuo.common.util.CommonResponseUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 编码规则表Service接口实现类
 *
 * @author jetox
 * @date  2025/07/23 19:41
 **/
@Service
public class WqsCodeRuleServiceImpl extends ServiceImpl<WqsCodeRuleMapper, WqsCodeRule> implements WqsCodeRuleService {

    @Override
    public Page<WqsCodeRule> page(WqsCodeRulePageParam wqsCodeRulePageParam) {
        QueryWrapper<WqsCodeRule> queryWrapper = new QueryWrapper<WqsCodeRule>().checkSqlInjection();
        if(ObjectUtil.isAllNotEmpty(wqsCodeRulePageParam.getSortField(), wqsCodeRulePageParam.getSortOrder())) {
            CommonSortOrderEnum.validate(wqsCodeRulePageParam.getSortOrder());
            queryWrapper.orderBy(true, wqsCodeRulePageParam.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(wqsCodeRulePageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(WqsCodeRule::getId);
        }
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(WqsCodeRuleAddParam wqsCodeRuleAddParam) {
        WqsCodeRule wqsCodeRule = BeanUtil.toBean(wqsCodeRuleAddParam, WqsCodeRule.class);
        this.save(wqsCodeRule);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(WqsCodeRuleEditParam wqsCodeRuleEditParam) {
        WqsCodeRule wqsCodeRule = this.queryEntity(wqsCodeRuleEditParam.getId());
        BeanUtil.copyProperties(wqsCodeRuleEditParam, wqsCodeRule);
        this.updateById(wqsCodeRule);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<WqsCodeRuleIdParam> wqsCodeRuleIdParamList) {
        // 执行删除
        this.removeByIds(CollStreamUtil.toList(wqsCodeRuleIdParamList, WqsCodeRuleIdParam::getId));
    }

    @Override
    public WqsCodeRule detail(WqsCodeRuleIdParam wqsCodeRuleIdParam) {
        return this.queryEntity(wqsCodeRuleIdParam.getId());
    }

    @Override
    public WqsCodeRule queryEntity(String id) {
        WqsCodeRule wqsCodeRule = this.getById(id);
        if(ObjectUtil.isEmpty(wqsCodeRule)) {
            throw new CommonException("编码规则表不存在，id值为：{}", id);
        }
        return wqsCodeRule;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<WqsCodeRuleEditParam> dataList = CollectionUtil.newArrayList();
         String fileName = "编码规则表导入模板_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), WqsCodeRuleEditParam.class).sheet("编码规则表").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 编码规则表导入模板下载失败：", e);
         CommonResponseUtil.renderError(response, "编码规则表导入模板下载失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JSONObject importData(MultipartFile file) {
        try {
            int successCount = 0;
            int errorCount = 0;
            JSONArray errorDetail = JSONUtil.createArray();
            // 创建临时文件
            File tempFile = FileUtil.writeBytes(file.getBytes(), FileUtil.file(FileUtil.getTmpDir() +
                    FileUtil.FILE_SEPARATOR + "wqsCodeRuleImportTemplate.xlsx"));
            // 读取excel
            List<WqsCodeRuleEditParam> wqsCodeRuleEditParamList =  EasyExcel.read(tempFile).head(WqsCodeRuleEditParam.class).sheet()
                    .headRowNumber(1).doReadSync();
            List<WqsCodeRule> allDataList = this.list();
            for (int i = 0; i < wqsCodeRuleEditParamList.size(); i++) {
                JSONObject jsonObject = this.doImport(allDataList, wqsCodeRuleEditParamList.get(i), i);
                if(jsonObject.getBool("success")) {
                    successCount += 1;
                } else {
                    errorCount += 1;
                    errorDetail.add(jsonObject);
                }
            }
            return JSONUtil.createObj()
                    .set("totalCount", wqsCodeRuleEditParamList.size())
                    .set("successCount", successCount)
                    .set("errorCount", errorCount)
                    .set("errorDetail", errorDetail);
        } catch (Exception e) {
            log.error(">>> 编码规则表导入失败：", e);
            throw new CommonException("编码规则表导入失败");
        }
    }

    public JSONObject doImport(List<WqsCodeRule> allDataList, WqsCodeRuleEditParam wqsCodeRuleEditParam, int i) {
        String id = wqsCodeRuleEditParam.getId();
        if(ObjectUtil.hasEmpty(id)) {
            return JSONUtil.createObj().set("index", i + 1).set("success", false).set("msg", "必填字段存在空值");
        } else {
            try {
                int index = CollStreamUtil.toList(allDataList, WqsCodeRule::getId).indexOf(wqsCodeRuleEditParam.getId());
                WqsCodeRule wqsCodeRule;
                boolean isAdd = false;
                if(index == -1) {
                    isAdd = true;
                    wqsCodeRule = new WqsCodeRule();
                } else {
                    wqsCodeRule = allDataList.get(index);
                }
                BeanUtil.copyProperties(wqsCodeRuleEditParam, wqsCodeRule);
                if(isAdd) {
                    allDataList.add(wqsCodeRule);
                } else {
                    allDataList.remove(index);
                    allDataList.add(index, wqsCodeRule);
                }
                this.saveOrUpdate(wqsCodeRule);
                return JSONUtil.createObj().set("success", true);
            } catch (Exception e) {
              log.error(">>> 数据导入异常：", e);
              return JSONUtil.createObj().set("success", false).set("index", i + 1).set("msg", "数据导入异常");
            }
        }
    }

    @Override
    public void exportData(List<WqsCodeRuleIdParam> wqsCodeRuleIdParamList, HttpServletResponse response) throws IOException {
       File tempFile = null;
       try {
         List<WqsCodeRuleEditParam> dataList;
         if(ObjectUtil.isNotEmpty(wqsCodeRuleIdParamList)) {
            List<String> idList = CollStreamUtil.toList(wqsCodeRuleIdParamList, WqsCodeRuleIdParam::getId);
            dataList = BeanUtil.copyToList(this.listByIds(idList), WqsCodeRuleEditParam.class);
         } else {
            dataList = BeanUtil.copyToList(this.list(), WqsCodeRuleEditParam.class);
         }
         String fileName = "编码规则表_" + DateUtil.format(DateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".xlsx";
         tempFile = FileUtil.file(FileUtil.getTmpDir() + FileUtil.FILE_SEPARATOR + fileName);
         EasyExcel.write(tempFile.getPath(), WqsCodeRuleEditParam.class).sheet("编码规则表").doWrite(dataList);
         CommonDownloadUtil.download(tempFile, response);
       } catch (Exception e) {
         log.error(">>> 编码规则表导出失败：", e);
         CommonResponseUtil.renderError(response, "编码规则表导出失败");
       } finally {
         FileUtil.del(tempFile);
       }
    }
}
