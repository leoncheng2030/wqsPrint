import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import fieldApi from '@/api/label/fieldApi'
import dictApi from '@/api/dev/dictApi'
import wqsCodeRuleApi from '@/api/barcode/wqsCodeRuleApi'

/**
 * 字段配置组合式函�?
 * 统一处理字段验证、类型处理、选项数据加载等逻辑
 */
export function useFieldConfig() {
	// 响应式数�?
	const loading = ref(false)
	const dictTypes = ref([])
	const codeRuleOptions = ref([])
	const availableFieldOptions = ref([])

	// 字段类型配置
	const inputTypeOptions = [
		{ label: '单行文本', value: 'INPUT' },
		{ label: '多行文本', value: 'TEXTAREA' },
		{ label: '数字输入', value: 'NUMBER' },
		{ label: '下拉选择', value: 'SELECT' },
		{ label: '单选框', value: 'RADIO' },
		{ label: '复选框', value: 'CHECKBOX' },
		{ label: '日期选择', value: 'DATE' },
		{ label: '日期范围', value: 'DATE_RANGE' },
		{ label: '时间选择', value: 'TIME' },
		{ label: '开�?', value: 'SWITCH' },
		{ label: '二维�?', value: 'QRCODE' },
		{ label: '动态条�?', value: 'DYNAMIC_BARCODE' }
	]

	// 数据源类型选项
	const dataSourceOptions = [
		{ label: '字典数据', value: 'dict' },
		{ label: 'API接口', value: 'api' },
		{ label: '静态数�?', value: 'static' }
	]

	// 字段类型颜色映射
	const inputTypeColorMap = {
		INPUT: 'blue',
		TEXTAREA: 'cyan',
		NUMBER: 'green',
		SELECT: 'orange',
		RADIO: 'purple',
		CHECKBOX: 'magenta',
		DATE: 'volcano',
		DATE_RANGE: 'red',
		TIME: 'lime',
		SWITCH: 'geekblue',
		QRCODE: 'gold',
		DYNAMIC_BARCODE: 'pink'
	}

	// 计算属�?
	const getInputTypeText = computed(() => {
		return (type) => {
			const option = inputTypeOptions.find(item => item.value === type)
			return option ? option.label : type
		}
	})

	const getInputTypeColor = computed(() => {
		return (type) => inputTypeColorMap[type] || 'default'
	})

	// 字段验证规则
	const getFieldValidationRules = (fieldType) => {
		const baseRules = {
			fieldKey: [
				{ required: true, message: '请输入字段标�?', trigger: 'blur' },
				{ pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '字段标识必须以字母开头，只能包含字母、数字和下划�?', trigger: 'blur' }
			],
			title: [
				{ required: true, message: '请输入字段名�?', trigger: 'blur' },
				{ max: 50, message: '字段名称不能超过50个字�?', trigger: 'blur' }
			],
			inputType: [
				{ required: true, message: '请选择控件类型', trigger: 'change' }
			]
		}

		// 根据字段类型添加特定验证规则
		if (['SELECT', 'RADIO', 'CHECKBOX'].includes(fieldType)) {
			baseRules.dataSource = [
				{ required: true, message: '请选择数据�?', trigger: 'change' }
			]
		}

		return baseRules
	}

	// 字段标识生成函数
	const generateFieldKey = (title) => {
		if (!title) return ''
		
		// 移除特殊字符，保留中文、英文、数�?
		const cleanTitle = title.replace(/[^\u4e00-\u9fa5a-zA-Z0-9]/g, '')
		
		// 如果是纯英文，转换为小驼峰命�?
		if (/^[a-zA-Z0-9\s]+$/.test(cleanTitle)) {
			return cleanTitle
				.toLowerCase()
				.replace(/\s+(.)/g, (match, char) => char.toUpperCase())
				.replace(/^(.)/, (match, char) => char.toLowerCase())
		}
		
		// 如果包含中文，提取拼音首字母
		const pinyinMap = {
			'用户': 'user', '姓名': 'name', '年龄': 'age', '性别': 'gender',
			'电话': 'phone', '邮箱': 'email', '地址': 'address', '公司': 'company',
			'部门': 'department', '职位': 'position', '日期': 'date', '时间': 'time',
			'编号': 'code', '序号': 'serial', '状�?': 'status', '类型': 'type',
			'名称': 'name', '描述': 'description', '备注': 'remark', '金额': 'amount',
			'数量': 'quantity', '价格': 'price', '总计': 'total'
		}
		
		// 尝试匹配常用词汇
		for (const [chinese, english] of Object.entries(pinyinMap)) {
			if (cleanTitle.includes(chinese)) {
				return english
			}
		}
		
		// 默认生成规则：field + 时间戳后4�?
		return 'field' + Date.now().toString().slice(-4)
	}

	// 字段类型判断函数
	const isSelectField = (inputType) => {
		return ['SELECT', 'RADIO', 'CHECKBOX'].includes(inputType)
	}

	const isMultipleField = (inputType) => {
		return ['SELECT', 'CHECKBOX'].includes(inputType)
	}

	const isDateField = (inputType) => {
		return ['DATE', 'DATE_RANGE', 'TIME'].includes(inputType)
	}

	const isNumberField = (inputType) => {
		return inputType === 'NUMBER'
	}

	const isTextareaField = (inputType) => {
		return inputType === 'TEXTAREA'
	}

	const isQRCodeField = (inputType) => {
		return inputType === 'QRCODE'
	}

	const isDynamicBarcodeField = (inputType) => {
		return inputType === 'DYNAMIC_BARCODE'
	}

	// 加载字典类型列表
	const loadDictTypes = async () => {
		try {
			loading.value = true
			const res = await dictApi.dictTree()
			
			if (res && Array.isArray(res) && res.length > 0) {
				// 递归提取字典类型编码和名�?
				const extractDictTypes = (nodes) => {
					let types = []
					nodes.forEach((node) => {
						// 只提取有子节点的父级字典类型
						if (node.dictValue && node.dictLabel && node.children && node.children.length > 0) {
							types.push({
								label: `${node.dictLabel} (${node.dictValue})`,
								value: node.dictValue
							})
						}
						// 递归处理子节�?
						if (node.children && node.children.length > 0) {
							types = types.concat(extractDictTypes(node.children))
						}
					})
					return types
				}
				
				dictTypes.value = extractDictTypes(res)
			} else {
				dictTypes.value = []
			}
		} catch (error) {
			message.error('获取字典类型失败')
			dictTypes.value = []
		} finally {
			loading.value = false
		}
	}

	// 加载编码规则列表
	const loadCodeRules = async () => {
		try {
			loading.value = true
			const params = {
				current: 1,
				size: 1000
			}

			const res = await wqsCodeRuleApi.wqsCodeRulePage(params)

			if (res && res.records) {
				codeRuleOptions.value = res.records.map(rule => ({
					label: `${rule.ruleName} (${rule.ruleCode})`,
					value: rule.id,
					ruleCode: rule.ruleCode,
					ruleName: rule.ruleName,
					segments: rule.segments
				}))
			} else {
				codeRuleOptions.value = []
			}
		} catch (error) {
			message.error('获取编码规则列表失败')
			codeRuleOptions.value = []
		} finally {
			loading.value = false
		}
	}

	// 加载可用字段列表（用于二维码和动态条码配置）
	const loadAvailableFields = async (templateId, excludeTypes = ['QRCODE']) => {
		try {
			loading.value = true
			const params = {
				templateId: templateId,
				current: 1,
				size: 1000
			}

			const res = await fieldApi.fieldPage(params)
			if (res && res.records) {
				// 过滤掉指定类型的字段并按sortCode排序
				const filteredFields = res.records
					.filter((field) => !excludeTypes.includes(field.inputType))
					.sort((a, b) => {
						// 先按作用域排序（主字段在前）
						if (a.fieldScope !== b.fieldScope) {
							return a.fieldScope === 'MAIN' ? -1 : 1
						}
						// 同作用域内按sortCode排序
						return (parseInt(a.sortCode) || 0) - (parseInt(b.sortCode) || 0)
					})
					.map((field) => ({
						label: `${field.title} (${field.fieldKey})`,
						value: field.fieldKey,
						fieldScope: field.fieldScope,
						sortCode: field.sortCode
					}))
				
				// 按fieldKey去重，避免重复选项
				const uniqueFields = filteredFields.filter((field, index, self) => 
					index === self.findIndex(f => f.value === field.value)
				)
				
				availableFieldOptions.value = uniqueFields
			} else {
				availableFieldOptions.value = []
			}
		} catch (error) {
			message.error('获取字段列表失败')
			availableFieldOptions.value = []
		} finally {
			loading.value = false
		}
	}

	// 解析字段选项配置
	const parseFieldOptions = (optionsData) => {
		if (!optionsData) return {}
		
		try {
			if (typeof optionsData === 'string') {
				return JSON.parse(optionsData)
			}
			return optionsData
		} catch (error) {
			return {}
		}
	}

	// 格式化字段选项配置
	const formatFieldOptions = (config) => {
		try {
			return JSON.stringify(config, null, 2)
		} catch (error) {
			return '{}'
		}
	}

	// 验证字段配置
	const validateFieldConfig = (fieldData) => {
		const errors = []
		
		// 基础验证
		if (!fieldData.fieldKey) {
			errors.push('字段标识不能为空')
		} else if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(fieldData.fieldKey)) {
			errors.push('字段标识格式不正�?')
		}
		
		if (!fieldData.title) {
			errors.push('字段名称不能为空')
		}
		
		if (!fieldData.inputType) {
			errors.push('控件类型不能为空')
		}
		
		// 选择类控件验�?
		if (isSelectField(fieldData.inputType)) {
			if (!fieldData.dataSource) {
				errors.push('请选择数据�?')
			} else {
				if (fieldData.dataSource === 'dict' && !fieldData.dictTypeCode) {
					errors.push('请选择字典类型')
				}
				if (fieldData.dataSource === 'api' && !fieldData.optionApiUrl) {
					errors.push('请输入API接口地址')
				}
				if (fieldData.dataSource === 'static' && !fieldData.staticOptions) {
					errors.push('请配置静态选项')
				}
			}
		}
		
		// 二维码字段验�?
		if (isQRCodeField(fieldData.inputType)) {
			if (!fieldData.qrCodeConfig || !fieldData.qrCodeConfig.selectedFields || fieldData.qrCodeConfig.selectedFields.length === 0) {
				errors.push('请选择二维码包含的字段')
			}
		}
		
		// 动态条码字段验�?
		if (isDynamicBarcodeField(fieldData.inputType)) {
			if (!fieldData.dynamicBarcodeConfig || !fieldData.dynamicBarcodeConfig.codeRule) {
				errors.push('请选择编码规则')
			}
		}
		
		return {
			valid: errors.length === 0,
			errors
		}
	}

	// 字段类型变化处理
	const handleInputTypeChange = (fieldData) => {
		// 清空不相关的字段
		if (!isSelectField(fieldData.inputType)) {
			fieldData.dataSource = ''
			fieldData.dictTypeCode = ''
			fieldData.optionApiUrl = ''
			fieldData.selectedDataApiUrl = ''
			fieldData.staticOptions = ''
		}
		
		if (!isMultipleField(fieldData.inputType)) {
			fieldData.isMultiple = '0'
		}
		
		if (!isDateField(fieldData.inputType)) {
			fieldData.dateFormat = ''
			fieldData.dateConfig = {}
		}
		
		if (!isNumberField(fieldData.inputType)) {
			fieldData.numberConfig = {}
		}
		
		if (!isTextareaField(fieldData.inputType)) {
			fieldData.textareaConfig = {}
		}
		
		if (!isQRCodeField(fieldData.inputType)) {
			fieldData.qrCodeConfig = {}
		}
		
		if (!isDynamicBarcodeField(fieldData.inputType)) {
			fieldData.dynamicBarcodeConfig = {}
		}
	}

	return {
		// 响应式数�?
		loading,
		dictTypes,
		codeRuleOptions,
		availableFieldOptions,
		
		// 配置选项
		inputTypeOptions,
		dataSourceOptions,
		
		// 计算属�?
		getInputTypeText,
		getInputTypeColor,
		
		// 工具函数
		generateFieldKey,
		getFieldValidationRules,
		parseFieldOptions,
		formatFieldOptions,
		validateFieldConfig,
		handleInputTypeChange,
		
		// 类型判断函数
		isSelectField,
		isMultipleField,
		isDateField,
		isNumberField,
		isTextareaField,
		isQRCodeField,
		isDynamicBarcodeField,
		
		// 数据加载函数
		loadDictTypes,
		loadCodeRules,
		loadAvailableFields
	}
}