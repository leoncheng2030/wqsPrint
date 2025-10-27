import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/barcode/coderule/` + url, ...arg)

/**
 * 编码规则表Api接口管理器
 *
 * @author jetox
 * @date  2025/07/23 19:41
 **/
export default {
	// 获取编码规则表分页
	wqsCodeRulePage(data) {
		return request('page', data, 'get')
	},
	// 提交编码规则表表单 edit为true时为编辑，默认为新增
	wqsCodeRuleSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除编码规则表
	wqsCodeRuleDelete(data) {
		return request('delete', data)
	},
	// 获取编码规则表详情
	wqsCodeRuleDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载编码规则表导入模板
    wqsCodeRuleDownloadTemplate(data) {
        return request('downloadImportTemplate', data, 'get', {
            responseType: 'blob'
        })
    },
    // 导入编码规则表
    wqsCodeRuleImport(data) {
        return request('importData', data)
    },
    // 导出编码规则表
    wqsCodeRuleExport(data) {
        return request('exportData', data, 'post', {
            responseType: 'blob'
        })
    },
    // 根据规则生成编码
    generateCode(ruleId, params) {
        return request(`generateCode?ruleId=${ruleId}`, params)
    },
    // 生成编码预览
    generatePreview(segments, params) {
        return request(`generatePreview?segments=${encodeURIComponent(segments)}`, params)
    },
    // 重置流水号
    resetSerial(ruleId, serialKey) {
        return request(`resetSerial?ruleId=${ruleId}&serialKey=${serialKey}`, {})
    },
    // 生成条码图片
    generateBarcodeImage(ruleId, params, barcodeType = 'CODE128', width = 300, height = 150) {
        return request(`generateBarcodeImage?ruleId=${ruleId}&barcodeType=${barcodeType}&width=${width}&height=${height}`, params)
    },
    // 生成条码预览图片
    generateBarcodePreview(segments, params, barcodeType = 'CODE128', width = 300, height = 150) {
        return request(`generateBarcodePreview?segments=${encodeURIComponent(segments)}&barcodeType=${barcodeType}&width=${width}&height=${height}`, params)
    },
    // 根据文本生成条码图片
    generateBarcodeFromText(text, barcodeType = 'CODE128', width = 300, height = 150) {
        return request(`generateBarcodeFromText?text=${encodeURIComponent(text)}&barcodeType=${barcodeType}&width=${width}&height=${height}`, {})
    },
    // 批量生成条码图片  
    generateBarcodeImages(ruleId, paramsList, barcodeType = 'CODE128', width = 300, height = 150) {
        return request(`generateBarcodeImages?ruleId=${ruleId}&barcodeType=${barcodeType}&width=${width}&height=${height}`, paramsList)
    }
}
