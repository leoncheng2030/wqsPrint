<template>
	<xn-form-container :title="getFormTitle()" width="80%" :visible="visible" :destroy-on-close="true"
		:body-style="{ 'padding-top': '10px' }" @close="onClose">
		<div class="section">
			<div class="section-device"></div>
			<div class="section-title">
				基础数据
			</div>
		</div>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="horizontal">
			<a-row :gutter="16">
				<a-col :span="12">
					<a-form-item label="业务标识:" name="businessKey">
						<a-input v-model:value="formData.businessKey" placeholder="请输入核心业务标:" allow-clear
							class="xn-wd" />
					</a-form-item>
				</a-col>
				<a-col :span="12">
					<a-form-item label="业务类型:" name="businessType">
						<a-select v-model:value="formData.businessType" placeholder="请选择业务类型" allow-clear
							:options="businessTypeOptions" @change="onBusinessTypeChange">
						</a-select>
					</a-form-item>
				</a-col>
				<a-col :span="12">
					<a-form-item label="关联模板:" name="templateId">
						<a-select v-model:value="formData.templateId" placeholder="请先选择业务类型" allow-clear
							:options="templateOptions" :disabled="!formData.businessType" @change="onTemplateChange">
						</a-select>
					</a-form-item>
				</a-col>
				<a-col :span="12">
					<a-form-item label="打印份数:" name="printCount">
						<a-input-number v-model:value="formData.printCount" placeholder="请输入打印份:" :min="0" :max="999"
							class="xn-wd" />
					</a-form-item>
				</a-col>
			</a-row>

			<!-- 流水号连续生成设置区: -->
			<div v-if="showSerialNumberRange" class="serial-number-range-section">
				<a-divider orientation="left">流水号连续生成设:</a-divider>
				<a-row :gutter="16">
					<a-col :span="8">
						<a-form-item label="流水号字段段：">
							<a-select v-model:value="serialNumberRangeConfig.selectedField" placeholder="请选择流水号字段:"
								:options="serialNumberFieldOptions" allow-clear />
						</a-form-item>
					</a-col>
					<a-col :span="8">
						<a-form-item label="起始值：" name="serialStartValue">
							<a-input-number v-model:value="serialNumberRangeConfig.startValue" placeholder="请输入起始:"
								:min="1" :max="999999" class="xn-wd" />
						</a-form-item>
					</a-col>
					<a-col :span="8">
						<a-form-item label="结束值：" name="serialEndValue">
							<a-input-number v-model:value="serialNumberRangeConfig.endValue" placeholder="请输入结束:"
								:min="1" :max="999999" class="xn-wd" />
						</a-form-item>
					</a-col>
				</a-row>
				<!-- 只在会生成多页时显示提示信息 -->
				<template v-if="willGenerateMultiplePages">
					<a-row>
						<a-col :span="24">
							<a-alert :message="`将生成包: ${calculatePageCount} 页连续流水号的打印数据`" type="info" show-icon
								style="margin-bottom: 16px;" />
						</a-col>
					</a-row>
				</template>
			</div>

			<!-- 主字段段区: -->
			<div v-if="mainFields.length > 0" class="main-fields-section">
				<div class="section">
					<div class="section-device"></div>
					<div class="section-title">
						主字段:
					</div>
				</div>
				<a-row :gutter="16">
					<a-col v-for="field in mainFields" :key="field.id || field.fieldKey" :span="getFieldSpan(field)">
						<a-form-item :label="field.title + ':'" :name="['dynamicFieldData', field.fieldKey]"
							:rules="getDynamicFieldRules(field)">
							<DynamicFieldRenderer :field="field" v-model="formData.dynamicFieldData[field.fieldKey]"
								:options="getFieldOptions(field)" />
						</a-form-item>
					</a-col>
				</a-row>
			</div>

			<!-- 明细字段段区域 - 可编辑表: -->
			<div v-if="detailFields.length > 0" class="detail-fields-section">
				<div class="section">
					<div class="section-device"></div>
					<div class="section-title">
						数据明细
					</div>
					<div class="actions">
						<a-space>
							<a-button type="primary" @click="exportDetailTemplate">
								<template #icon>
									<DownloadOutlined />
								</template>
								导出模板
							</a-button>
							<a-button type="default" @click="importDetailData">
								<template #icon>
									<UploadOutlined />
								</template>
								导入数据
							</a-button>
						</a-space>
					</div>
				</div>
				<a-table :columns="detailTableColumns" :data-source="detailTableData" :pagination="false" size="small"
					bordered :scroll="{ x: 300 }" class="editable-table">
					<template #bodyCell="{ column, record, index }">
						<template v-if="column.editable">
							<DynamicFieldRenderer :field="column.field" v-model="record[column.dataIndex]"
								:options="getFieldOptions(column.field)" :compact="true" />
						</template>
						<template v-else-if="column.dataIndex === 'operation'">
							<a-space>
								<a-button type="link" size="small" @click="addDetailRow(index)" title="在此行后插入">
									<template #icon>
										<PlusOutlined />
									</template>
								</a-button>
								<a-button type="link" size="small" danger @click="removeDetailRow(index)"
									:disabled="detailTableData.length <= 1" title="删除此行">
									<template #icon>
										<MinusOutlined />
									</template>
								</a-button>
								<a-button type="link" size="small" @click="copyDetailRow(index)" title="复制此行">
									<template #icon>
										<CopyOutlined />
									</template>
								</a-button>
							</a-space>
						</template>
					</template>
				</a-table>
				<div class="detail-table-actions">
					<a-button type="dashed" @click="addDetailRow()" block>
						<template #icon>
							<PlusOutlined />
						</template>
						添加明细:
					</a-button>
				</div>
			</div>
		</a-form>
		<template #footer>
			<a-button class="xn-mr8" @click="onClose">关闭</a-button>
			<a-button type="primary" :loading="formLoading" @click="onSubmit">保存</a-button>
		</template>
	</xn-form-container>
</template>

<script setup name="labelRecord">
import { message } from 'ant-design-vue'
import { PlusOutlined, MinusOutlined, CopyOutlined, DownloadOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { nextTick } from 'vue'
import recordApi from '@/api/label/recordApi'
import templateApi from '@/api/label/templateApi'
import fieldApi from '@/api/label/fieldApi'
import wqsCodeRuleApi from '@/api/barcode/wqsCodeRuleApi'
import DynamicFieldRenderer from '@/components/DynamicFieldRenderer.vue'
import { required } from '@/utils/formRules'
import tool from '@/utils/tool'
import dayjs from 'dayjs'
import { useFieldManager } from '@/composables/useFieldManager'
import { useApiOptimization } from '@/composables/useApiOptimization'
import { usePerformanceOptimization } from '@/composables/usePerformanceOptimization'

// 默认是关闭状:
const visible = ref(false)
const formRef = ref()
const emit = defineEmits({ successful: null })
const formLoading = ref(false)
const isCopyMode = ref(false)

// 表单数据
const formData = ref({})

// 模板选项
const templateOptions = ref([])

// 动态字段段列:
const dynamicFields = ref([])

// 字段段选项缓存
const fieldOptionsCache = ref({})

// 编码规则选项（用于动态条码编码规则选择:
const codeRuleOptions = ref([])
// 编码规则是否已加载完成的标志
const codeRulesLoaded = ref(false)

// 引入性能优化工具
const { cachedComputed } = usePerformanceOptimization()

// 流水号字段段相关状:
const serialNumberFields = ref([])
const showSerialNumberRange = ref(false)
const serialNumberRangeConfig = ref({
	startValue: 1,
	endValue: 1,
	selectedField: null
})

// 使用优化的组合式函数
const { withDeduplication, handleError: handleApiError } = useApiOptimization()
const {
	loadTemplateFields: loadFieldsOptimized,
	getFieldOptions,
	setFieldDefaultValue: setFieldDefaultValueOptimized,
	getFieldRules: getDynamicFieldRules,
	clearCache: clearFieldCache
} = useFieldManager()

// 计算属性 - 简化缓存逻辑
const filteredDynamicFields = computed(() => {
	return dynamicFields.value.filter(field => field.fieldKey !== 'qrContent')
})

const serialNumberFieldOptions = computed(() => {
	return serialNumberFields.value.map(field => ({
		label: field.fieldLabel,
		value: field.fieldKey
	}))
})

// 页数计算（移除防抖以确保编辑模式下实时响应）
const calculatePageCount = computed(() => {
	const start = serialNumberRangeConfig.value.startValue || 1
	const end = serialNumberRangeConfig.value.endValue || 1
	return Math.max(0, end - start + 1)
})

// 是否会生成多页（用于控制提示信息和预览按钮的显示:
const willGenerateMultiplePages = computed(() => {
	return calculatePageCount.value > 1
})

const mainFields = computed(() => {
	return filteredDynamicFields.value.filter(field => field.fieldScope === 'MAIN')
})

const detailFields = computed(() => {
	return filteredDynamicFields.value.filter(field => field.fieldScope === 'DETAIL')
})

// 明细表格数据
const detailTableData = ref([])

// 明细表格列配:
const detailTableColumns = computed(() => {
	const columns = detailFields.value.map((field) => ({
		title: field.title,
		dataIndex: field.fieldKey,
		key: field.fieldKey,
		width: getColumnWidth(field),
		editable: true,
		field: field
	}))

	// 添加操作:
	columns.push({
		title: '操作',
		dataIndex: 'operation',
		key: 'operation',
		width: 100,
		fixed: 'right'
	})

	return columns
})

// 获取列宽:
const getColumnWidth = (field) => {
	switch (field.inputType) {
		case 'TEXTAREA':
			return 200
		case 'SELECT':
			return 150
		case 'DATE':
			return 120
		default:
			return 120
	}
}

// 添加明细:
const addDetailRow = (index) => {
	const newRow = { key: Date.now() }
	// 初始化明细字段段默认:
	detailFields.value.forEach((field) => {
		newRow[field.fieldKey] = setFieldDefaultValue(field)
	})

	if (typeof index === 'number') {
		// 在指定位置后插入
		detailTableData.value.splice(index + 1, 0, newRow)
	} else {
		// 在末尾添:
		detailTableData.value.push(newRow)
	}
}

// 删除明细:
const removeDetailRow = (index) => {
	if (detailTableData.value.length > 1) {
		detailTableData.value.splice(index, 1)
	}
}

// 复制明细:
const copyDetailRow = (index) => {
	// 深拷贝当前行数据
	const currentRow = detailTableData.value[index]
	const newRow = JSON.parse(JSON.stringify(currentRow))

	// 生成新的key，避免key重复
	newRow.key = Date.now()

	// 在当前行后插入复制的:
	detailTableData.value.splice(index + 1, 0, newRow)
}

// 初始化明细表格数:
const initDetailTableData = () => {
	if (detailFields.value.length > 0) {
		// 如果有明细字段段但没有数据，添加一行默认数:
		if (detailTableData.value.length === 0) {
			addDetailRow()
		}
	} else {
		// 如果没有明细字段段，清空表格数:
		detailTableData.value = []
	}
}

// 同步明细表格数据到表单数:
const syncDetailTableDataToForm = () => {
	if (detailFields.value.length > 0 && detailTableData.value.length > 0) {
		// 明细数据不存储到dynamicFieldData中，而是直接存储到printData的items字段段
		// 这里只处理主字段段数据到dynamicFieldData
		mainFields.value.forEach((field) => {
			// 主字段段数据保持原有逻辑，存储到dynamicFieldData
			if (formData.value.dynamicFieldData[field.fieldKey] === undefined) {
				formData.value.dynamicFieldData[field.fieldKey] = setFieldDefaultValue(field)
			}
		})

		// 明细数据将在onSubmit中直接处理到printData.items
	}
}

// 从表单数据加载明细表格数:
const loadDetailTableDataFromForm = () => {
	if (detailFields.value.length > 0) {
		// 从printData.items中加载明细数:
		const printDataStr = formData.value.printData
		let items = []

		if (printDataStr) {
			try {
				const printData = typeof printDataStr === 'string' ? JSON.parse(printDataStr) : printDataStr

				// 判断是否为多页数据（数组格式:
				let firstPageData = printData
				if (Array.isArray(printData) && printData.length > 0) {
					// 如果是数组，取第一页的数据进行回显
					firstPageData = printData[0]
				}

				items = firstPageData.items || []
			} catch (e) {
				// 解析失败，使用空数组
			}
		}

		// 重新构建表格数据
		detailTableData.value = []
		if (items.length > 0) {
			items.forEach((item, index) => {
				const row = { key: Date.now() + index }
				// 加载明细字段段数据
				detailFields.value.forEach((field) => {
					let fieldValue = item[field.fieldKey] || ''
					// 如果是日期字段段，需要转换为标准格式用于显示
					if ((field.inputType === 'DATE' || field.inputType === 'DATE_RANGE') && fieldValue) {
						fieldValue = formatFieldValueForDisplay(field, fieldValue)
					}
					row[field.fieldKey] = fieldValue
				})
				// 如果明细条目中有qrContent，也要加:
				if (item.qrContent) {
					row.qrContent = item.qrContent
				}
				detailTableData.value.push(row)
			})
		} else {
			// 如果没有数据，添加一行默认数据，并确保应用字段段默认:
			initDetailTableData()
		}
	}
}
// 工具函数：解析二维码配置
const parseQrConfig = (qrCodeConfig) => {
	try {
		return typeof qrCodeConfig === 'string' ? JSON.parse(qrCodeConfig) : qrCodeConfig
	} catch (e) {
		return null
	}
}

// 工具函数：获取日期格:
const getDateFormat = (field) => {
	// 优先使用新的dateConfig配置
	if (field.dateConfig && field.dateConfig.displayFormat) {
		return field.dateConfig.displayFormat
	}

	// 向后兼容：使用字段段配置中的时间格:
	if (field.dateFormat) {
		return field.dateFormat
	}

	// 如果没有配置时间格式，则通过 placeholder 来判断（向后兼容:
	if (field.placeholder) {
		// 检查是否包含常见的时间格式关键:
		if (field.placeholder.includes('YYYY-MM-DD HH:mm:ss')) {
			return 'YYYY-MM-DD HH:mm:ss'
		} else if (field.placeholder.includes('YYYY-MM-DD HH:mm')) {
			return 'YYYY-MM-DD HH:mm'
		} else if (field.placeholder.includes('YYYY-MM-DD')) {
			return 'YYYY-MM-DD'
		} else if (field.placeholder.includes('YYYY/MM/DD')) {
			return 'YYYY/MM/DD'
		} else if (field.placeholder.includes('MM-DD')) {
			return 'MM-DD'
		}
	}
	// 默认日期格式
	return 'YYYY-MM-DD'
}

// 工具函数：获取日期范围格:
const getDateRangeFormat = (field) => {
	// 优先使用新的dateConfig配置
	if (field.dateConfig && field.dateConfig.displayFormat) {
		// 如果是范围格式（包含~或其他分隔符），提取单个日期格式
		const displayFormat = field.dateConfig.displayFormat
		if (displayFormat.includes('~')) {
			// 例如:'YYYY.MM.DD~YYYY.MM.DD' -> 'YYYY.MM.DD'
			return displayFormat.split('~')[0].trim()
		} else if (displayFormat.includes(' - ')) {
			// 例如:'YYYY.MM.DD - YYYY.MM.DD' -> 'YYYY.MM.DD'
			return displayFormat.split(' - ')[0].trim()
		} else if (displayFormat.includes('-') && displayFormat.match(/\d{4}.*-.*\d{4}/)) {
			// 处理可能的日期范围格式，但要避免误判单个日期中的连字段:
			const parts = displayFormat.split('-')
			if (parts.length > 3) {
				// 如果分割后超:3部分，可能是范围格式，取前半部分
				const halfLength = Math.floor(parts.length / 2)
				return parts.slice(0, halfLength).join('-')
			}
		}
		// 如果不是范围格式，直接使:
		return displayFormat
	}

	// 向后兼容：使用字段段配置中的时间格:
	if (field.dateFormat) {
		// 如果是范围格式（包含~），提取单个日期格式
		if (field.dateFormat.includes('~')) {
			// 例如:'YYYY-MM-DD~YYYY-MM-DD' -> 'YYYY-MM-DD'
			return field.dateFormat.split('~')[0].trim()
		} else if (field.dateFormat.includes(' - ')) {
			// 例如:'YYYY-MM-DD - YYYY-MM-DD' -> 'YYYY-MM-DD'
			return field.dateFormat.split(' - ')[0].trim()
		}
		// 如果不是范围格式，直接使:
		return field.dateFormat
	}

	// 如果没有配置时间格式，则通过 placeholder 来判断（向后兼容:
	if (field.placeholder) {
		// 检查是否包含常见的时间格式关键:
		if (field.placeholder.includes('YYYY-MM-DD HH:mm:ss')) {
			return 'YYYY-MM-DD HH:mm:ss'
		} else if (field.placeholder.includes('YYYY-MM-DD HH:mm')) {
			return 'YYYY-MM-DD HH:mm'
		} else if (field.placeholder.includes('YYYY-MM-DD')) {
			return 'YYYY-MM-DD'
		} else if (field.placeholder.includes('YYYY/MM/DD')) {
			return 'YYYY/MM/DD'
		} else if (field.placeholder.includes('MM-DD')) {
			return 'MM-DD'
		}
	}
	// 默认日期格式
	return 'YYYY-MM-DD'
}

// 工具函数：设置字段段默认:
const setFieldDefaultValue = (field) => {
	// 检查字段段是否有配置默认:
	if (field.optionsData) {
		try {
			const options = JSON.parse(field.optionsData)
			if (options.defaultValue !== undefined) {
				// 根据字段段类型处理默认:
				if (field.inputType === 'CHECKBOX' || (field.inputType === 'SELECT' && field.isMultiple === '1')) {
					// 多选类型字段段，确保默认值是数组
					if (Array.isArray(options.defaultValue)) {
						return options.defaultValue
					} else if (typeof options.defaultValue === 'string' && options.defaultValue) {
						// 如果是逗号分隔的字段符串，转换为数组
						return options.defaultValue.split(',').map(item => item.trim())
					} else if (typeof options.defaultValue === 'string' && options.defaultValue === '') {
						// 空字段符串表示没有默认:
						return []
					} else {
						// 其他情况返回空数:
						return []
					}
				} else {
					// 单值类型字段段直接返回默认:
					return options.defaultValue
				}
			}
		} catch (e) {
			// 解析失败，使用默认逻辑
		}
	}

	// 如果没有配置默认值，根据字段段类型使用默认逻辑
	if (field.inputType === 'CHECKBOX' || (field.inputType === 'SELECT' && field.isMultiple === '1')) {
		return []
	} else if (field.inputType === 'DATE') {
		// 日期字段段默认使用当前日期
		const format = getDateFormat(field)
		return dayjs().format(format)
	} else if (field.inputType === 'DATE_RANGE') {
		// 日期范围字段段默认使用当前日期到未来一:
		const format = getDateRangeFormat(field)
		const startDate = dayjs().format(format)
		const endDate = dayjs().add(7, 'day').format(format)
		return `${startDate}-${endDate}`
	} else {
		return ''
	}
}

// 工具函数：清空动态字段段数:
const clearDynamicFieldsData = () => {
	formData.value.dynamicFieldData = {}
	dynamicFields.value = []
	fieldOptionsCache.value = {}
}

// 工具函数：强制清理所有缓存和数据
const clearAllCacheAndData = async () => {
	// 清空动态字段数据和缓存
	clearDynamicFieldsData()

	// 清空二维码配置
	formData.value.qrCodeConfig = ''

	// 清空明细表格数据
	detailTableData.value = []

	// 清空流水号相关配置
	serialNumberFields.value = []
	showSerialNumberRange.value = false
	serialNumberRangeConfig.value = {
		startValue: 1,
		endValue: 1,
		selectedField: null
	}

	// 清空性能优化缓存（如果有的话）
	if (typeof cachedComputed.clearCache === 'function') {
		cachedComputed.clearCache()
	}

	// 强制触发响应式更新
	await nextTick()

	console.log('已清理所有缓存和数据')
}



// 常量定义
const FIELD_STATUS = {
	ENABLE: 'ENABLE',
	DISABLE: 'DISABLE'
}

const FIELD_SCOPE = {
	MAIN: 'MAIN'
}

const INPUT_TYPES = {
	CHECKBOX: 'CHECKBOX',
	SELECT: 'SELECT',
	RADIO: 'RADIO',
	DATE: 'DATE',
	TEXTAREA: 'TEXTAREA'
}
// 获取表单标题
const getFormTitle = () => {
	if (isCopyMode.value) {
		return '复制打印记录'
	}
	return formData.value.id ? '编辑打印记录' : '新增打印记录'
}

// 打开抽屉
const onOpen = async (record, isCopy = false) => {
	visible.value = true
	isCopyMode.value = isCopy

	// 先清理所有缓存和数据，确保干净的状态
	await clearAllCacheAndData()

	// 初始化表单数:
	formData.value = {
		printStatus: 'COMPLETED', // 默认已完成状:
		printCount: 1, // 默认打印1:
		dynamicFieldData: {} // 初始化动态字段段数据对:
	}

	// 如果是编辑模式，加载数据
	if (record) {
		if (isCopy) {
			// 复制模式：直接使用传入的数据，不查询详情
			formData.value = Object.assign(formData.value, record)
			// 如果有模板ID，加载模板相关信:
			if (record.templateId) {
				templateApi.templateDetail({ id: record.templateId })
					.then((templateData) => {
						if (templateData) {
							// 设置业务类型
							formData.value.businessType = templateData.type
							// 加载该业务类型下的所有模板
							loadTemplatesByType(templateData.type)
							// 加载模板关联字段段
							loadTemplateFields(record.templateId)
								.then(() => {
									// 解析复制的打印数据
									if (record.printData) {
										try {
											const printData = JSON.parse(record.printData)
											// 提取主字段段数据
											const dynamicFieldDataFromPrint = {}
											Object.keys(printData).forEach((key) => {
												if (key !== 'qrContent' && key !== 'items') {
													dynamicFieldDataFromPrint[key] = printData[key]
												}
											})
											formData.value.dynamicFieldData = {
												...formData.value.dynamicFieldData,
												...dynamicFieldDataFromPrint
											}
											// 加载明细数据
											if (printData.items && Array.isArray(printData.items)) {
												detailTableData.value = printData.items.map((item, index) => ({
													key: Date.now() + index,
													...item
												}))
											} else {
												initDetailTableData()
											}
										} catch (e) {
											console.error('解析打印数据失败:', e)
											initDetailTableData()
										}
									} else {
										initDetailTableData()
									}
								})
								.catch((error) => {
									message.error('加载模板字段失败，请检查模板配置')
									console.error('加载模板字段失败:', error)
								})
						} else {
							message.error('模板数据不存在或已被删除')
						}
					})
					.catch((error) => {
						message.error('加载模板信息失败，请检查模板是否存在')
						console.error('加载模板信息失败:', error)
					})
			}
		} else {
			// 编辑模式：查询详:
			convertFormData(record)
		}
	}
}

// 关闭抽屉
const onClose = async () => {
  visible.value = false
  isCopyMode.value = false
  formData.value = {}
  templateOptions.value = []

  // 使用统一的缓存清理方法
  await clearAllCacheAndData()
}

// 业务类型变化事件
const onBusinessTypeChange = async (businessType) => {
	// 清空模板选择
	formData.value.templateId = undefined
	templateOptions.value = []
	// 强制清理所有相关缓存和数据
	await clearAllCacheAndData()

	if (businessType) {
		// 根据业务类型加载对应的模:
		loadTemplatesByType(businessType)
	}
}

// 根据业务类型加载模板选项
const loadTemplatesByType = (businessType) => {
	// 获取指定业务类型的启用模板
	templateApi
		.templatePage({
			type: businessType,
			status: 'ENABLE',
			size: 1000
		})
		.then((res) => {
			if (res && res.records) {
				templateOptions.value = res.records.map((item) => ({
					label: `${item.name} (${item.code})`,
					value: item.id
				}))
			} else {
				templateOptions.value = []
			}
		})
		.catch((error) => {
			handleApiError(error, '加载模板列表')
			templateOptions.value = []
		})
}

// 模板变化事件
const onTemplateChange = async (templateId) => {
	// 强制清理所有相关缓存和数据
	await clearAllCacheAndData()

	if (templateId) {
		try {
			// 根据选择的模板加载关联字段段（会自动提取二维码配置:
			await loadTemplateFields(templateId)
			// 初始化明细表格数:
			initDetailTableData()
			// 如果是编辑模式，加载明细表格数据
			if (formData.value.id) {
				loadDetailTableDataFromForm()
			}
		} catch (error) {
			handleApiError(error, '加载模板信息')
		}
	}
}

// 加载模板关联的字段:
const loadTemplateFields = async (templateId, preserveExistingData = false) => {
	try {
		// 根据模板ID获取关联字段段
		const params = {
			templateId: templateId,
			status: FIELD_STATUS.ENABLE, // 只加载启用的字段段
			size: 1000 // 获取所有字段:
		}

		const res = await fieldApi.fieldPage(params)

		if (!res) {
			throw new Error('字段API返回空结果')
		}
		if (res && res.records) {
			// 检查是否有禁用字段
			const disabledFields = res.records.filter(f => f.status === 'DISABLE')
			if (disabledFields.length > 0) {
				console.warn('⚠️ 检测到禁用字段，但仍然被返回！可能后端API不支持状态过滤')
			}
			// 解析字段状态并过滤
			const processedRecords = res.records.map(field => {
				let status = field.status || 'ENABLE' // 默认状态

				// 从optionsData中解析状态配置
				if (field.optionsData) {
					try {
						const options = JSON.parse(field.optionsData)
						if (options.status) {
							status = options.status
						}
					} catch (e) {
						console.warn(`解析字段 "${field.title}" 配置失败:`, e)
					}
				}
				return {
					...field,
					status
				}

				return { ...field, status: status }
			})

			// 前端过滤：只保留启用的字段
			const enabledRecords = processedRecords.filter(field => field.status === 'ENABLE')

			// 按字段段作用域和排序号排序，并解析时间格式配置
			const fields = enabledRecords
				.map((field) => {
					let dateFormat = '' // 默认为空
					let dateConfig = null // 新的日期配置
					let dynamicBarcodeConfig = null // 动态条码配:
					let inputConfig = null // 文本输入配置

					// 如果是DATE或DATE_RANGE类型，尝试从optionsData中解析时间格式配:
					if (['DATE', 'DATE_RANGE'].includes(field.inputType) && field.optionsData) {
						try {
							const options = JSON.parse(field.optionsData)

							// 优先使用新的dateConfig配置
							if (options.dateConfig) {
								dateConfig = options.dateConfig
								// 从dateConfig中提取displayFormat作为dateFormat
								if (dateConfig.displayFormat) {
									dateFormat = dateConfig.displayFormat
								}
							}
							// 向后兼容：如果没有dateConfig，使用旧的dateFormat
							else if (options.dateFormat) {
								dateFormat = options.dateFormat
							}
						} catch (e) {
							// 解析时间格式配置失败
						}
					}

					// 如果是DYNAMIC_BARCODE类型，尝试从optionsData中解析动态条码配:
					if (field.inputType === 'DYNAMIC_BARCODE' && field.optionsData) {
						try {
							const options = JSON.parse(field.optionsData)
							if (options.dynamicBarcodeConfig) {
								dynamicBarcodeConfig = typeof options.dynamicBarcodeConfig === 'string'
									? JSON.parse(options.dynamicBarcodeConfig)
									: options.dynamicBarcodeConfig

							}
						} catch (e) {
							// 解析动态条码配置失:
						}
				}

				// 如果是INPUT类型，尝试从optionsData中解析文本输入配置:
				if (field.inputType === 'INPUT' && field.optionsData) {
					try {
						const options = JSON.parse(field.optionsData)
						if (options.inputConfig) {
							inputConfig = typeof options.inputConfig === 'string'
								? JSON.parse(options.inputConfig)
								: options.inputConfig
						}
					} catch (e) {
						// 解析文本输入配置失败
					}
				}

				return {
					...field,
					dateFormat: dateFormat, // 添加解析后的时间格式
					dateConfig: dateConfig, // 添加解析后的日期配置
					dynamicBarcodeConfig: dynamicBarcodeConfig, // 添加解析后的动态条码配置
					inputConfig: inputConfig // 添加解析后的文本输入配置
				}
			})

			dynamicFields.value = fields

			// 初始化动态字段段数:
			if (
				!preserveExistingData &&
				(!formData.value.dynamicFieldData || Object.keys(formData.value.dynamicFieldData).length === 0)
			) {
				// 新建模式：初始化所有字段段默认:
				const dynamicFieldData = {}
				fields.forEach((field) => {
					dynamicFieldData[field.fieldKey] = setFieldDefaultValue(field)
				})
				formData.value.dynamicFieldData = dynamicFieldData
			} else if (preserveExistingData) {
				// 编辑模式：保留现有数据，只为新字段段设置默认:
				const existingData = formData.value.dynamicFieldData || {}
				fields.forEach((field) => {
					// 如果字段段在现有数据中不存在，则设置默认:
					if (!(field.fieldKey in existingData)) {
						existingData[field.fieldKey] = setFieldDefaultValue(field)
					}
				})
				formData.value.dynamicFieldData = existingData
			}

			// 预加载字段段选项
			await loadFieldOptions(fields)

			// 先加载编码规则列表，确保在提取二维码配置之前编码规则已经可用
			await loadCodeRules()

			// 字段段加载完成后，从字段段配置中提取二维码配:
			extractQrCodeConfigFromFields()

			// 编码规则加载完成后，立即更新动态条码字段内容
			await nextTick(() => {
				// 确保编码规则已加载后再更新动态条码字段
				if (codeRulesLoaded.value) {
					updateAllDynamicBarcodeFields()
				}

				// 检测流水号字段段
				serialNumberFields.value = detectSerialNumberFields()
				showSerialNumberRange.value = serialNumberFields.value.length > 0

				// 重置流水号范围配:
				if (serialNumberFields.value.length > 0) {
					serialNumberRangeConfig.value = {
						startValue: 1,
						endValue: 1,
						selectedField: serialNumberFields.value[0].fieldKey
					}
				}
			})

			// 加载到字段:
		}
	} catch (error) {
		// 添加更详细的错误日志
		console.error('加载模板字段详细错误:', {
			error: error,
			templateId: templateId,
			params: {
				templateId: templateId,
				status: FIELD_STATUS.ENABLE,
				size: 1000
			}
		})
		handleApiError(error, '加载模板字段')
		throw error // 抛出错误以便上层处理
	}
}

// 加载字段选项数据
const loadFieldOptions = async (fields) => {
	for (const field of fields) {
		// 处理选择类型字段的选项
		if (['SELECT', 'RADIO', 'CHECKBOX'].includes(field.inputType)) {
			await loadSingleFieldOptions(field)
		}
		// 处理 DATE 类型字段的时间格式配置
		if (field.inputType === 'DATE') {
			// 确保时间格式配置已经在 loadTemplateFields 中解析
		}
	}
}

// 加载单个字段的选项数据
const loadSingleFieldOptions = async (field) => {
	try {
		// 解析字段配置中的选项数据
		let options = []

		if (field.optionsData) {
			try {
				const parsedConfig = JSON.parse(field.optionsData)

				// 检查是否有静态选项
				if (parsedConfig.staticOptions) {
					// 解析静态选项
					try {
						const staticOptions = typeof parsedConfig.staticOptions === 'string'
							? JSON.parse(parsedConfig.staticOptions)
							: parsedConfig.staticOptions

						if (Array.isArray(staticOptions)) {
							options = staticOptions.map(option => ({
								label: option.label || option.value,
								value: option.value
							}))
						}
					} catch (e) {
						console.warn(`解析字段 "${field.title}" 的静态选项失败:`, e)
					}
				}

				// 检查是否有字典类型
				if (parsedConfig.dictTypeCode && parsedConfig.dictTypeCode.trim()) {
					// 从字典获取选项
					const dictOptions = tool.dictList(parsedConfig.dictTypeCode)
					if (dictOptions && dictOptions.length > 0) {
						options = dictOptions
					}
				}

				// TODO: 支持 API 选项加载
				// if (parsedConfig.optionApiUrl) {
				//     // 从API加载选项
				// }

			} catch (e) {
				console.warn(`解析字段 "${field.title}" 的配置失败:`, e)
			}
		}

		// 从字段本身的字典类型获取选项（向后兼容）
		if (options.length === 0 && field.dictTypeCode && field.dictTypeCode.trim()) {
			const dictOptions = tool.dictList(field.dictTypeCode)
			if (dictOptions && dictOptions.length > 0) {
				options = dictOptions
			}
		}

		// 缓存字段选项
		fieldOptionsCache.value[field.fieldKey] = options

	} catch (error) {
		console.error(`加载字段 "${field.title}" 选项失败:`, error)
		// 设置空选项
		fieldOptionsCache.value[field.fieldKey] = []
	}
}



// 获取字段段布局跨度
const getFieldSpan = (field) => {
	// 多行文本占用更大空间
	if (field.inputType === 'TEXTAREA') {
		return 24
	}
	// 其他字段段默认占用一半空:
	return 12
}



// 提取二维码配置 - 简化版
const extractQrCodeConfigFromFields = () => {
	try {
		const qrCodeFields = dynamicFields.value.filter(field => field.inputType === 'QRCODE')

		let mainQrConfig = null
		let detailQrConfig = null

		for (const qrCodeField of qrCodeFields) {
			if (qrCodeField.optionsData) {
				const fieldConfig = typeof qrCodeField.optionsData === 'string'
					? JSON.parse(qrCodeField.optionsData)
					: qrCodeField.optionsData

				if (fieldConfig.qrCodeConfig) {
					const currentQrConfig = typeof fieldConfig.qrCodeConfig === 'string'
						? JSON.parse(fieldConfig.qrCodeConfig)
						: fieldConfig.qrCodeConfig

					if (qrCodeField.fieldScope === 'MAIN') {
						mainQrConfig = mainQrConfig || currentQrConfig
					} else if (qrCodeField.fieldScope === 'DETAIL') {
						detailQrConfig = detailQrConfig || currentQrConfig
					}
				}
			}
		}

		formData.value.mainQrCodeConfig = mainQrConfig ? JSON.stringify(mainQrConfig) : ''
		formData.value.detailQrCodeConfig = detailQrConfig ? JSON.stringify(detailQrConfig) : ''
		formData.value.qrCodeConfig = formData.value.mainQrCodeConfig || formData.value.detailQrCodeConfig || ''
	} catch (error) {
		formData.value.qrCodeConfig = ''
		formData.value.mainQrCodeConfig = ''
		formData.value.detailQrCodeConfig = ''
	}
}

// 提交表单
const onSubmit = async () => {
	try {
		// 表单验证
		await formRef.value.validate()

		// 检查是否启用了流水号范围生成且会生成多:
		if (willGenerateMultiplePages.value &&
			serialNumberRangeConfig.value.selectedField) {
			await handleMultiPageSubmit()
		} else {
			await handleSinglePageSubmit()
		}
	} catch (error) {
		if (error.errorFields) {
			// 表单验证错误
			message.error('请检查表单输:')
		} else {
			message.error('提交失败: ' + (error.message || error))
		}
	}
}

// 单页提交处理（保持原有逻辑:
const handleSinglePageSubmit = async () => {


	formLoading.value = true

	try {
		// 合并明细表格数据到动态字段段数据中
		syncDetailTableDataToForm()

		// 准备提交数据
		const submitData = { ...formData.value }

		// 组装打印数据 - 只保留主字段段数据在顶级，明细数据存储到items:
		const printData = {
			// 明细数据存储到items字段段:
			items: []
			// qrCodeContent 会在后面根据二维码配置生成并添加
		}
		// 只添加主字段段数据到printData顶级
		if (mainFields.value.length > 0) {
			mainFields.value.forEach((field) => {
				const fieldValue = submitData.dynamicFieldData[field.fieldKey]
				// 确保所有字段段都被添加到printData中，即使值为:
				// 对日期字段段进行格式转:
				printData[field.fieldKey] = formatFieldValueForSave(field, fieldValue || '')
			})
		}

		// 处理明细表格数据，存储到printData.items:
		if (detailFields.value.length > 0 && detailTableData.value.length > 0) {
			printData.items = detailTableData.value.map((row) => {
				const item = {}
				detailFields.value.forEach((field) => {
					const fieldValue = row[field.fieldKey] || ''
					// 对日期字段段进行格式转:
					item[field.fieldKey] = formatFieldValueForSave(field, fieldValue)
				})
				return item
			})

		}

		// 根据二维码配置信息生成二维码内容 - 修改为使用分离的配置
		// 处理主字段段二维码
		if (submitData.mainQrCodeConfig) {
			const mainQrConfig = parseQrConfig(submitData.mainQrCodeConfig)
			if (mainQrConfig && mainQrConfig.selectedFields && mainQrConfig.selectedFields.length > 0) {
				// 验证选择的字段段都是主字段段
				const validMainFields = mainQrConfig.selectedFields.filter((fieldKey) =>
					mainFields.value.some((field) => field.fieldKey === fieldKey)
				)

				// 使用原始的字段段选择列表而不是过滤后的列表，以支持明细字段:
				const validMainQrConfig = { ...mainQrConfig }
				// 使用已经格式化的printData而不是原始的submitData.dynamicFieldData
				// 为了支持在主字段段二维码中使用明细字段段，创建一个包含所有数据的对象
				const allDataForQrCode = {
					...printData,
					items: printData.items
				}
				printData.qrContent = generateQrCodeContent(validMainQrConfig, allDataForQrCode)
			}
		}

		// 处理明细字段段二维:
		if (submitData.detailQrCodeConfig && detailFields.value.length > 0 && printData.items.length > 0) {
			const detailQrConfig = parseQrConfig(submitData.detailQrCodeConfig)
			if (detailQrConfig && detailQrConfig.selectedFields && detailQrConfig.selectedFields.length > 0) {
				// 验证选择的字段段都是明细字段:
				const validDetailFields = detailQrConfig.selectedFields.filter((fieldKey) =>
					detailFields.value.some((field) => field.fieldKey === fieldKey)
				)

				if (validDetailFields.length > 0) {
					const validDetailQrConfig = { ...detailQrConfig, selectedFields: validDetailFields }
					printData.items = printData.items.map((item) => {
						return {
							...item,
							qrContent: generateQrCodeContent(validDetailQrConfig, item)
						}
					})

				}
			}
		}
		// 如果原来有打印数据，尝试合并非字段段相关的数据
		if (submitData.printData) {
			try {
				const existingPrintData = JSON.parse(submitData.printData)
				// 检查是否为数组格式（多页数据）
				if (Array.isArray(existingPrintData) && existingPrintData.length > 0) {
					// 如果是数组，只取第一页的数据进行合并
					const firstPageData = existingPrintData[0]
					Object.keys(firstPageData).forEach((key) => {
						// 跳过字段段数据和特殊字段段，只保留其他配置信:
						if (
							key !== 'qrContent' &&
							key !== 'items' &&
							key !== '_pageIndex' &&
							key !== '_pageCount' &&
							!mainFields.value.some((field) => field.fieldKey === key) &&
							!detailFields.value.some((field) => field.fieldKey === key)
						) {
							printData[key] = firstPageData[key]
						}
					})
				} else {
					// 如果不是数组，直接处理
					Object.keys(existingPrintData).forEach((key) => {
						// 跳过字段段数据和特殊字段段，只保留其他配置信:
						if (
							key !== 'qrContent' &&
							key !== 'items' &&
							key !== '_pageIndex' &&
							key !== '_pageCount' &&
							!mainFields.value.some((field) => field.fieldKey === key) &&
							!detailFields.value.some((field) => field.fieldKey === key)
						) {
							printData[key] = existingPrintData[key]
						}
					})
				}
			} catch (e) {
				// 原打印数据格式错误，使用新数:
			}
		}

		// 根据打印份数生成多份printData数据
		const printCount = submitData.printCount || 1
		const printDataList = []

		for (let i = 0; i < printCount; i++) {
			// 创建printData的深拷贝，避免引用问题
			const printDataCopy = JSON.parse(JSON.stringify(printData))
			printDataCopy._pageIndex = i + 1
			printDataCopy._pageCount = printCount
			printDataList.push(printDataCopy)
		}

		// 设置打印数据（始终存储为数组格式，保持数据结构一致性）
		submitData.printData = JSON.stringify(printDataList)

		// 移除不需要提交的字段段 - 更新删除列表
		delete submitData.businessType
		delete submitData.dynamicFieldData
		delete submitData.qrCodeConfig // 删除原有配置信息
		delete submitData.mainQrCodeConfig // 删除主字段段配置信:
		delete submitData.detailQrCodeConfig // 删除明细字段段配置信息

		await recordApi.recordSubmitForm(submitData, submitData.id)

		onClose()
		emit('successful')
		message.success(formData.value.id ? '编辑成功' : '新增成功')

	} catch (error) {
		handleApiError(error, '保存打印记录')
	} finally {
		formLoading.value = false
	}
}

// 多页提交处理
const handleMultiPageSubmit = async () => {
	formLoading.value = true

	try {
		// 生成多页数据
		const multiPageData = await generateMultiPageData()



		// 合并明细表格数据到动态字段段数据中
		syncDetailTableDataToForm()

		// 准备提交数据
		const submitData = { ...formData.value }

		// 组装多页打印数据数组
		const printDataList = []
		const printCount = submitData.printCount || 1

		multiPageData.forEach((pageData, index) => {
			// 为每个流水号生成指定份数的打印数据
			for (let copyIndex = 0; copyIndex < printCount; copyIndex++) {
				const printData = {
					items: []
				}

				// 添加主字段数据到printData顶级
				if (mainFields.value.length > 0) {
					mainFields.value.forEach((field) => {
						const fieldValue = pageData.dynamicFieldData[field.fieldKey]
						// 确保所有字段都被添加到printData中，即使值为空
						printData[field.fieldKey] = formatFieldValueForSave(field, fieldValue || '')
					})
				}

				// 处理明细表格数据
				if (detailFields.value.length > 0 && detailTableData.value.length > 0) {
					printData.items = detailTableData.value.map((row) => {
						const item = {}
						detailFields.value.forEach((field) => {
							const fieldValue = row[field.fieldKey] || ''
							item[field.fieldKey] = formatFieldValueForSave(field, fieldValue)
						})
						return item
					})
				}

				// 根据二维码配置信息生成二维码内容 - 修改为使用分离的配置
				// 处理主字段二维码
				if (submitData.mainQrCodeConfig) {
					const mainQrConfig = parseQrConfig(submitData.mainQrCodeConfig)
					console.log('主字段二维码配置:', mainQrConfig)
					if (mainQrConfig && mainQrConfig.selectedFields && mainQrConfig.selectedFields.length > 0) {
						// 验证选择的字段都是主字段
						const validMainFields = mainQrConfig.selectedFields.filter((fieldKey) =>
							mainFields.value.some((field) => field.fieldKey === fieldKey)
						)
						console.log('有效的主字段:', validMainFields)
						console.log('原始选择的字段:', mainQrConfig.selectedFields)

						// 使用原始的字段选择列表而不是过滤后的列表，以支持明细字段
						const validMainQrConfig = { ...mainQrConfig }
						console.log('使用原始配置:', validMainQrConfig)
						console.log('使用原始配置的selectedFields:', validMainQrConfig.selectedFields)
						
						// 创建用于生成二维码的数据对象，避免重复嵌套
						const qrCodeData = {}
						
						// 添加主字段数据
						mainFields.value.forEach((field) => {
							qrCodeData[field.fieldKey] = printData[field.fieldKey]
						})
						
						// 如果二维码配置中包含明细字段，添加明细数据
						const hasDetailFields = mainQrConfig.selectedFields.some((fieldKey) =>
							detailFields.value.some((field) => field.fieldKey === fieldKey)
						)
						
						if (hasDetailFields && printData.items && printData.items.length > 0) {
							// 只添加必要的明细字段，避免重复嵌套
							qrCodeData.items = printData.items.map(item => {
								const cleanItem = {}
								detailFields.value.forEach(field => {
									if (mainQrConfig.selectedFields.includes(field.fieldKey)) {
										cleanItem[field.fieldKey] = item[field.fieldKey]
									}
								})
								return cleanItem
							})
						}
						
						console.log('用于生成主字段二维码的数据:', qrCodeData)
						printData.qrContent = generateQrCodeContent(validMainQrConfig, qrCodeData)
						console.log('生成的主字段二维码内容:', printData.qrContent)
					}
				}

				// 处理明细字段二维码
				if (submitData.detailQrCodeConfig && detailFields.value.length > 0 && printData.items.length > 0) {
					const detailQrConfig = parseQrConfig(submitData.detailQrCodeConfig)
					if (detailQrConfig && detailQrConfig.selectedFields && detailQrConfig.selectedFields.length > 0) {
						const validDetailFields = detailQrConfig.selectedFields.filter((fieldKey) =>
							detailFields.value.some((field) => field.fieldKey === fieldKey)
						)

						if (validDetailFields.length > 0) {
							const validDetailQrConfig = { ...detailQrConfig, selectedFields: validDetailFields }
							printData.items = printData.items.map((item) => {
								return {
									...item,
									qrContent: generateQrCodeContent(validDetailQrConfig, item)
								}
							})

						}
					}
				}

				// 添加页面标识信息
				printData.serialNumber = pageData._serialNumber
				printData.labelIndex = pageData._pageIndex
				printData.totalLabels = pageData._totalPages
				// 添加打印份数信息
				printData._pageIndex = copyIndex + 1
				printData._pageCount = printCount

				printDataList.push(printData)
			}
		})

		// 设置多页打印数据（JSON数组格式:
		submitData.printData = JSON.stringify(printDataList)

		// 移除不需要提交的字段段 - 更新删除列表
		delete submitData.businessType
		delete submitData.dynamicFieldData
		delete submitData.qrCodeConfig // 删除原有配置信息
		delete submitData.mainQrCodeConfig // 删除主字段段配置信:
		delete submitData.detailQrCodeConfig // 删除明细字段段配置信息



		// 保存单条记录，包含多页打印数:
		await recordApi.recordSubmitForm(submitData, submitData.id)


		message.success(`成功创建包含 ${multiPageData.length} 页的连续打印记录`)

		onClose()
		emit('successful')

	} catch (error) {

		message.error('多页数据生成失败: ' + (error.message || error))
	} finally {
		formLoading.value = false
	}
}

// 新增：根据字段段配置格式化字段段值用于保:
const formatFieldValueForSave = (field, value) => {
	if (!field) {
		return value
	}

	// 处理日期字段段
	if (field.inputType === 'DATE') {
		// 如果日期字段段值为空，使用当前日期作为默认:
		if (!value || (typeof value === 'string' && value.trim() === '')) {
			const format = getDateFormat(field)
			value = dayjs().format(format)
		}
		// 如果字段段有自定义日期格式，且值不为空
		if (typeof value === 'string' && value.trim() !== '') {
			try {


				let parsedDate
				let targetFormat = 'YYYY-MM-DD' // 默认保存格式

				// 优先使用新的dateConfig配置
				if (field.dateConfig) {
					const { displayFormat, saveFormat, inputFormat } = field.dateConfig

					// 确定输入格式（优先使用inputFormat，其次displayFormat:
					const parseFormat = inputFormat || displayFormat
					// 确定保存格式（默认为YYYY-MM-DD:
					targetFormat = saveFormat || 'YYYY-MM-DD'

					// 如果是点号分隔的格式，先转换为标准格:
					if (parseFormat && parseFormat.includes('.')) {
						const standardValue = value.replace(/\./g, '-')
						parsedDate = dayjs(standardValue)
					} else if (parseFormat) {
						// 使用指定格式解析
						parsedDate = dayjs(value, parseFormat)
					} else {
						// 尝试自动解析
						parsedDate = dayjs(value)
					}
				}
				// 向后兼容：使用旧的dateFormat配置
				else if (field.dateFormat) {
					targetFormat = field.dateFormat

					// 处理点号分隔的日期格式（如：2025.07.25:
					if (value.includes('.')) {

						// 将点号替换为横线，转换为标准格式
						const standardDate = value.replace(/\./g, '-')
						parsedDate = dayjs(standardDate)
					} else {
						// 标准格式或其他格:
						parsedDate = dayjs(value)
					}
				}

				if (parsedDate && parsedDate.isValid()) {
					// 转换为配置的格式
					const result = parsedDate.format(targetFormat)
					return result
				}
			} catch (e) {
				// 日期格式转换失败
			}
		}
	}

	// 处理日期范围字段段
	if (field.inputType === 'DATE_RANGE') {
		// 如果日期范围字段段值为空，使用当前日期到一周后作为默认:
		if (!value || (typeof value === 'string' && value.trim() === '')) {
			const format = getDateRangeFormat(field)
			value = dayjs().format(format) + '-' + dayjs().add(7, 'day').format(format)
		}
		// 如果字段段有自定义日期格式，且值不为空
		if (typeof value === 'string' && value.trim() !== '') {
			try {


				let singleFormat = 'YYYY-MM-DD' // 默认单个日期格式
				let separator = '-' // 默认分隔:

				// 优先使用新的dateConfig配置
				if (field.dateConfig) {
					const { displayFormat, saveFormat, inputFormat, rangeSeparator } = field.dateConfig

					// 确定输入格式和保存格:
					const parseFormat = inputFormat || displayFormat

					// 如果有displayFormat且包含范围分隔符，从中提取单个日期格式和分隔:
					if (displayFormat && (displayFormat.includes('~') || displayFormat.includes(' - '))) {
						if (displayFormat.includes('~')) {
							singleFormat = displayFormat.split('~')[0].trim()
							separator = '~'
						} else if (displayFormat.includes(' - ')) {
							singleFormat = displayFormat.split(' - ')[0].trim()
							separator = ' - '
						}
					} else {
						// 使用配置的保存格式，如果没有则使用默认格:
						singleFormat = saveFormat || 'YYYY-MM-DD'
						separator = rangeSeparator || '-'
					}



					// 检查是否为范围格式（包:'-'分隔符）
					if (value.includes('-')) {
						const [startDateStr, endDateStr] = value.split('-')

						let startDate, endDate

						// 如果是点号分隔的格式
						if (parseFormat && parseFormat.includes('.')) {
							const standardStartDate = startDateStr.replace(/\./g, '-')
							const standardEndDate = endDateStr.replace(/\./g, '-')

							startDate = dayjs(standardStartDate)
							endDate = dayjs(standardEndDate)
						} else if (parseFormat) {
							// 使用指定格式解析
							startDate = dayjs(startDateStr, parseFormat)
							endDate = dayjs(endDateStr, parseFormat)

						} else {
							// 尝试自动解析
							startDate = dayjs(startDateStr)
							endDate = dayjs(endDateStr)

						}

						if (startDate && endDate && startDate.isValid() && endDate.isValid()) {
							const formattedStart = startDate.format(singleFormat)
							const formattedEnd = endDate.format(singleFormat)
							const result = `${formattedStart}${separator}${formattedEnd}`
							return result
						}
					}
				}
				// 向后兼容：使用旧的dateFormat配置
				else if (field.dateFormat) {
					// 获取单个日期格式和分隔符（去掉范围标识符:
					if (field.dateFormat.includes('~')) {
						singleFormat = field.dateFormat.split('~')[0].trim()
						separator = '~'
					} else if (field.dateFormat.includes(' - ')) {
						singleFormat = field.dateFormat.split(' - ')[0].trim()
						separator = ' - '
					} else {
						singleFormat = field.dateFormat
						separator = '-' // 默认分隔:
					}



					let startDate, endDate

					// 处理标准格式的日期范围（YYYY-MM-DD-YYYY-MM-DD:
					if (value.includes('-') && !value.includes('.')) {
						const parts = value.split('-')
						if (parts.length >= 6) {
							// 重新组合日期：前3部分为开始日期，:3部分为结束日:
							const startDateStr = parts.slice(0, 3).join('-')
							const endDateStr = parts.slice(3, 6).join('-')

							startDate = dayjs(startDateStr)
							endDate = dayjs(endDateStr)

						}
					}
					// 处理点号分隔的日期范围格式（如：2025.07.25-2025.08.31:
					else if (value.includes('.') && value.includes('-')) {

						// 按照 "-" 分割成两个日期部:
						const dateRangeParts = value.split('-')
						if (dateRangeParts.length === 2) {
							const startDateStr = dateRangeParts[0].trim()
							const endDateStr = dateRangeParts[1].trim()

							// 将点号替换为横线，转换为标准格式
							const standardStartDate = startDateStr.replace(/\./g, '-')
							const standardEndDate = endDateStr.replace(/\./g, '-')

							startDate = dayjs(standardStartDate)
							endDate = dayjs(standardEndDate)

						}
					}

					// 如果成功解析了日期，则转换为目标格式
					if (startDate && endDate && startDate.isValid() && endDate.isValid()) {
						// 转换为配置的格式并重新组:
						const formattedStart = startDate.format(singleFormat)
						const formattedEnd = endDate.format(singleFormat)
						const result = `${formattedStart}${separator}${formattedEnd}`
						return result
					}
				}
			} catch (e) {
				// 日期范围格式转换失败
			}
		}
	}

	// 处理文本输入字段段的补零配:
	if (field.inputType === 'INPUT' && field.inputConfig) {
		// 支持新的配置格式（enablePadding, totalLength等）
		if (field.inputConfig.enablePadding) {
			const { totalLength, paddingDirection, paddingChar } = field.inputConfig

			if (totalLength && totalLength > 0) {
				// 处理空值：如果值为空或undefined，转换为空字段符串
				const stringValue = String(value || '')
				const padCharacter = paddingChar || '0'

				if (stringValue.length < totalLength) {
					let result
					if (paddingDirection === 'suffix' || paddingDirection === 'right') {
						// 后补:
						result = stringValue.padEnd(totalLength, padCharacter)
					} else {
						// 前补零（默认:
						result = stringValue.padStart(totalLength, padCharacter)
					}
					return result
				}
			}
		}
		// 支持旧的配置格式（padZero, padLength等）
		else if (field.inputConfig.padZero) {
			const { padLength, padDirection, padChar } = field.inputConfig

			if (padLength && padLength > 0) {
				// 处理空值：如果值为空或undefined，转换为空字段符串
				const stringValue = String(value || '')
				const paddingChar = padChar || '0'

				if (stringValue.length < padLength) {
					if (padDirection === 'suffix' || padDirection === 'right') {
						// 后补:
						return stringValue.padEnd(padLength, paddingChar)
					} else {
						// 前补零（默认:
						return stringValue.padStart(padLength, paddingChar)
					}
				}
			}
		}
	}

	// 其他字段段类型直接返回原:
	return value
}

// 新增：根据字段段配置将保存的自定义格式日期转换回标准格式用于显:
const formatFieldValueForDisplay = (field, value) => {
	if (!value || !field) {
		return value
	}

	// 处理日期字段段
	if (field.inputType === 'DATE') {
		// 如果字段段有日期配置，且值不为空
		if (typeof value === 'string' && value.trim() !== '') {
			try {
				// 优先使用新的dateConfig配置
				if (field.dateConfig) {
					const { displayFormat, saveFormat } = field.dateConfig

					// 确定保存格式（默认为YYYY-MM-DD）和显示格式
					const parseFormat = saveFormat || 'YYYY-MM-DD'
					const targetFormat = displayFormat || 'YYYY-MM-DD'



					// 解析保存的日期:
					const date = dayjs(value, parseFormat)
					if (date.isValid()) {
						// 转换为显示格:
						const result = date.format(targetFormat)
						return result
					}
				}
				// 向后兼容：使用旧的dateFormat配置
				else if (field.dateFormat) {

					// 首先尝试按照自定义格式解析日:
					const parsedDate = dayjs(value, field.dateFormat)
					if (parsedDate.isValid()) {
						// 转换为标准格: YYYY-MM-DD 用于显示
						const result = parsedDate.format('YYYY-MM-DD')
						return result
					} else {
						// 如果按自定义格式解析失败，尝试处理点号分隔格:
						if (value.includes('.')) {
							// 将点号替换为横线，转换为标准格式
							const standardDate = value.replace(/\./g, '-')
							const dotParsed = dayjs(standardDate)
							if (dotParsed.isValid()) {
								const result = dotParsed.format('YYYY-MM-DD')
								return result
							}
						} else {
							// 尝试直接解析（可能已经是标准格式:
							const directParsed = dayjs(value)
							if (directParsed.isValid()) {
								const result = directParsed.format('YYYY-MM-DD')
								return result
							}
						}
					}
				}
			} catch (e) {
				// 日期显示格式转换失败
			}
		}
	}

	// 处理日期范围字段段
	if (field.inputType === 'DATE_RANGE') {
		// 如果字段段有日期配置，且值不为空
		if (typeof value === 'string' && value.trim() !== '') {
			try {
				// 优先使用新的dateConfig配置
				if (field.dateConfig) {
					const { displayFormat, saveFormat, rangeSeparator } = field.dateConfig

					// 确定保存格式和显示格:
					const parseFormat = saveFormat || 'YYYY-MM-DD'
					const targetFormat = displayFormat || 'YYYY-MM-DD'
					const separator = rangeSeparator || '-'

					// 智能识别日期范围分隔符并正确分割
					let startDateStr = ''
					let endDateStr = ''

					// 对于标准格式 YYYY-MM-DD-YYYY-MM-DD，需要特殊处:
					if (parseFormat === 'YYYY-MM-DD' && value.match(/^\d{4}-\d{2}-\d{2}-\d{4}-\d{2}-\d{2}$/)) {
						// 标准格式：按照固定位置分:
						startDateStr = value.substring(0, 10) // :10个字段符：YYYY-MM-DD
						endDateStr = value.substring(11)      // 从第11个字段符开始：YYYY-MM-DD

					} else {
						// 其他格式：尝试智能分:
						// 首先尝试使用显示分隔符分:
						if (value.includes(separator) && separator !== '-') {
							[startDateStr, endDateStr] = value.split(separator)
						} else {
							// 如果没有显示分隔符，尝试按照日期格式长度分割
							const formatLength = parseFormat.length
							if (value.length >= formatLength * 2 + 1) {
								startDateStr = value.substring(0, formatLength)
								endDateStr = value.substring(formatLength + 1)
							}
						}
					}

					if (startDateStr && endDateStr) {
						// 解析保存的日期:
						const startDate = dayjs(startDateStr, parseFormat)
						const endDate = dayjs(endDateStr, parseFormat)

						if (startDate.isValid() && endDate.isValid()) {
							// 从显示格式中提取单个日期格式（去掉范围分隔符:
							let singleDateFormat = targetFormat
							// 检查是否包含范围分隔符，如果包含则提取单个日期格式
							if (targetFormat.includes('~')) {
								singleDateFormat = targetFormat.split('~')[0].trim()
							} else if (targetFormat.includes(' - ')) {
								singleDateFormat = targetFormat.split(' - ')[0].trim()
							} else if (targetFormat.includes('-') && !targetFormat.match(/^[YMD-]+$/)) {
								// 如果包含'-'但不是纯日期格式（如YYYY-MM-DD），则可能是范围格式
								const parts = targetFormat.split('-')
								if (parts.length > 3) {
									// 可能: YYYY.MM.DD-YYYY.MM.DD 这样的格式，取前半部:
									singleDateFormat = parts.slice(0, Math.ceil(parts.length / 2)).join('-')
								}
							}

							// 转换为显示格式（使用单个日期格式:
							const formattedStart = startDate.format(singleDateFormat)
							const formattedEnd = endDate.format(singleDateFormat)
							// 使用显示分隔符连接日:
							const displaySeparator = rangeSeparator || '~'
							const result = `${formattedStart}${displaySeparator}${formattedEnd}`
							return result
						} else {

						}
					}
				}
				// 向后兼容：使用旧的dateFormat配置
				else if (field.dateFormat) {


					// 获取单个日期格式（去掉范围标识符:
					let singleFormat = field.dateFormat
					if (singleFormat.includes('~')) {
						singleFormat = singleFormat.split('~')[0].trim()
					}
					// 先尝试按照标准格式解析（YYYY-MM-DD-YYYY-MM-DD:
					if (value.includes('-')) {
						const standardParts = value.split('-')
						if (standardParts.length === 6) {
							// 标准格式：YYYY-MM-DD-YYYY-MM-DD
							const startDateStr = standardParts.slice(0, 3).join('-')
							const endDateStr = standardParts.slice(3, 6).join('-')

							const startDate = dayjs(startDateStr)
							const endDate = dayjs(endDateStr)

							if (startDate.isValid() && endDate.isValid()) {
								// 已经是标准格式，直接返回

								return value
							}
						}

						// 如果不是标准格式，尝试按照自定义格式解析
						// 例如:"07-21-08-30" (MM-DD格式) 需要转换为 "2025-07-21-2025-08-30"
						const parts = value.split('-')

						// 根据自定义格式的长度来判断如何分:
						const formatParts = singleFormat.split(/[-.]/).length

						if (parts.length >= formatParts * 2) {
							// 分割开始和结束日期
							const startParts = parts.slice(0, formatParts)
							const endParts = parts.slice(formatParts, formatParts * 2)

							const startDateStr = startParts.join('-')
							const endDateStr = endParts.join('-')

							// 按照自定义格式解析日:
							const startDate = dayjs(startDateStr, singleFormat)
							const endDate = dayjs(endDateStr, singleFormat)

							if (startDate.isValid() && endDate.isValid()) {
								// 转换为标准格式并重新组合
								const formattedStart = startDate.format('YYYY-MM-DD')
								const formattedEnd = endDate.format('YYYY-MM-DD')

								return `${formattedStart}-${formattedEnd}`
							}
						}
					}

					// 处理点号分隔的日期范围格式（如：2025.07.25-2025.08.31:
					if (value.includes('.') && value.includes('-')) {


						// 按照 "-" 分割成两个日期部:
						const dateRangeParts = value.split('-')
						if (dateRangeParts.length === 2) {
							const startDateStr = dateRangeParts[0].trim()
							const endDateStr = dateRangeParts[1].trim()

							// 将点号替换为横线，转换为标准格式
							const standardStartDate = startDateStr.replace(/\./g, '-')
							const standardEndDate = endDateStr.replace(/\./g, '-')

							// 验证日期有效:
							const startDate = dayjs(standardStartDate)
							const endDate = dayjs(standardEndDate)

							if (startDate.isValid() && endDate.isValid()) {
								// 转换为标准格:
								const formattedStart = startDate.format('YYYY-MM-DD')
								const formattedEnd = endDate.format('YYYY-MM-DD')

								return `${formattedStart}-${formattedEnd}`
							}
						}
					}
				}

			} catch (e) {
				// 日期范围显示格式转换失败
			}
		}
	}

	// 其他字段段类型直接返回原:
	return value
}

// 新增：根据二维码配置和动态字段段数据生成二维码内容
// const generateQrCodeContent = (qrConfig, dynamicFieldData) => {
// 	if (!qrConfig || !qrConfig.formatType) {
// 		return ''
// 	}
//
// 	try {
// 		let content = ''
//
// 		// 将格式类型转换为大写以保证兼容:
// 		const formatType = qrConfig.formatType.toUpperCase()
//
// 		// 根据字段的sortCode重新排序selectedFields，确保生成顺序正确
// 		let sortedSelectedFields = qrConfig.selectedFields || []
// 		if (sortedSelectedFields.length > 0 && dynamicFields.value && dynamicFields.value.length > 0) {
// 			// 创建字段映射表，包含sortCode和fieldScope信息
// 			const fieldMap = new Map()
// 			dynamicFields.value.forEach(field => {
// 				fieldMap.set(field.fieldKey, {
// 					sortCode: parseInt(field.sortCode) || 0,
// 					fieldScope: field.fieldScope
// 				})
// 			})
//
// 			// 按照字段的sortCode和fieldScope重新排序selectedFields
// 			sortedSelectedFields = [...sortedSelectedFields].sort((a, b) => {
// 				const fieldA = fieldMap.get(a)
// 				const fieldB = fieldMap.get(b)
//
// 				if (!fieldA || !fieldB) return 0
//
// 				// 先按作用域排序（主字段在前）
// 				if (fieldA.fieldScope !== fieldB.fieldScope) {
// 					return fieldA.fieldScope === 'MAIN' ? -1 : 1
// 				}
//
// 				// 同作用域内按sortCode排序
// 				return fieldA.sortCode - fieldB.sortCode
// 			})
// 		}
//
// 		// 根据格式类型生成内容
// 		switch (formatType) {
// 			case 'CUSTOM':
// 				// 自定义格式：使用分隔符连接选定字段段
// 				if (sortedSelectedFields.length > 0) {
// 					// 使用配置中的separator，如果没有则使用默认:'|'
// 					const separator = qrConfig.separator || qrConfig.customSeparator || '|'
// 					// 使用明细行分隔符，如果没有配置则使用默认的井号
// 					const detailRowSeparator = qrConfig.detailRowSeparator || '#'
//
// 					// 判断是否有明细字段
// 					const hasDetailFields = sortedSelectedFields.some(fieldKey =>
// 						dynamicFieldData.items && dynamicFieldData.items.length > 0 &&
// 						dynamicFieldData.items[0].hasOwnProperty(fieldKey)
// 					)
//
// 					if (hasDetailFields && dynamicFieldData.items && dynamicFieldData.items.length > 0) {
// 						// 如果有明细字段，按行循环处理，每行按配置顺序排列字段
// 						const detailParts = []
//
// 						dynamicFieldData.items.forEach((item) => {
// 							const rowValues = []
// 							// 按照配置的字段顺序处理每一行
// 							sortedSelectedFields.forEach((fieldKey) => {
// 								// 首先检查是否为明细字段（在明细数据中存在且值会变化）
// 								if (item.hasOwnProperty(fieldKey)) {
// 									// 明细字段：使用当前行数据
// 									rowValues.push(item[fieldKey] || '')
// 								} else if (dynamicFieldData[fieldKey] !== undefined) {
// 									// 主字段：使用主表数据
// 									rowValues.push(dynamicFieldData[fieldKey] || '')
// 								} else {
// 									// 字段不存在，添加空值
// 									rowValues.push('')
// 								}
// 							})
// 							detailParts.push(rowValues.join(separator))
// 						})
//
// 					// 使用明细行分隔符连接各行
// 					content = detailParts.join(detailRowSeparator)
// 					console.log('QR Code Content (with details):', content)
// 					} else {
// 						// 如果没有明细字段，只处理主字段
// 						const values = []
// 						sortedSelectedFields.forEach((fieldKey) => {
// 							if (dynamicFieldData[fieldKey] !== undefined) {
// 								values.push(dynamicFieldData[fieldKey] || '')
// 							}
// 						})
// 						content = values.join(separator)
// 						console.log('QR Code Content (main only):', content)
// 					}
// 				}
// 				break
//
// 			case 'JSON':
// 				// JSON格式：根据数据类型生成不同格:
// 				if (sortedSelectedFields.length > 0) {
// 					const jsonData = {}
// 					sortedSelectedFields.forEach((fieldKey) => {
// 						if (dynamicFieldData[fieldKey] !== undefined) {
// 							jsonData[fieldKey] = dynamicFieldData[fieldKey] || ''
// 						} else if (dynamicFieldData.items && dynamicFieldData.items.length > 0) {
// 							// 如果是明细字段段，从items中获取所有条目的值并连接
// 							const itemValues = dynamicFieldData.items.map(item =>
// 								item[fieldKey] !== undefined ? item[fieldKey] : ''
// 							)
// 							// 使用配置中的明细值分隔符，如果没有则使用默认:','
// 							const itemValueSeparator = qrConfig.itemValueSeparator || ','
// 							// 返回所有明细项的值，用指定分隔符连接
// 							jsonData[fieldKey] = itemValues.filter(val => val !== '').join(itemValueSeparator)
// 						} else {
// 							jsonData[fieldKey] = ''
// 						}
// 					})
//
// 					// 根据jsonDataType决定输出格式
// 					if (qrConfig.jsonDataType === 'array') {
// 						// 数组类型：[{"字段段:1":"字段段:1", "字段段:2":"字段段:2"}]
// 						content = JSON.stringify([jsonData])
// 					} else {
// 						// 对象类型：{"字段段:1":"字段段:1", "字段段:2":"字段段:2"}
// 						content = JSON.stringify(jsonData)
// 					}
// 				}
// 				break
//
// 			case 'URL':
// 				// URL格式：将选定字段段作为查询参数
// 				if (sortedSelectedFields.length > 0) {
// 					const baseUrl = qrConfig.baseUrl || ''
// 					const params = new URLSearchParams()
// 					sortedSelectedFields.forEach((fieldKey) => {
// 						let value = ''
// 						if (dynamicFieldData[fieldKey] !== undefined) {
// 							value = dynamicFieldData[fieldKey] || ''
// 						} else if (dynamicFieldData.items && dynamicFieldData.items.length > 0) {
// 							// 如果是明细字段段，从items中获取所有条目的值并连接
// 							const itemValues = dynamicFieldData.items.map(item =>
// 								item[fieldKey] !== undefined ? item[fieldKey] : ''
// 							)
// 							// 使用配置中的明细值分隔符，如果没有则使用默认:','
// 							const itemValueSeparator = qrConfig.itemValueSeparator || ','
// 							// 返回所有明细项的值，用指定分隔符连接
// 							value = itemValues.filter(val => val !== '').join(itemValueSeparator)
// 						}
// 						if (value) {
// 							params.append(fieldKey, value)
// 						}
// 					})
// 					content = baseUrl + (params.toString() ? '?' + params.toString() : '')
// 				}
// 				break
//
// 			default:
// 				break
// 		}
//
// 		// 在内容最后添加结束标识符（如果配置了的话）
// 		if (qrConfig.endMarker && qrConfig.endMarker.trim() !== '') {
// 			if (content && content.trim() !== '') {
// 				content = content + qrConfig.endMarker
// 			} else {
// 				content = qrConfig.endMarker
// 			}
// 		}
//
// 		return content
// 	} catch (error) {
// 		return ''
// 	}
// }

// 根据二维码配置和动态字段段数据生成二维码内容
const generateQrCodeContent = (qrConfig, dynamicFieldData) => {
	if (!qrConfig || !qrConfig.formatType) {
		return ''
	}

	try {
		let content = ''

		// 将格式类型转换为大写以保证兼容:
		const formatType = qrConfig.formatType.toUpperCase()

		// 使用用户配置的字段顺序，而不是根据sortCode重新排序
		// 原代码中根据sortCode重新排序selectedFields的逻辑是错误的，应保持用户选择的字段顺序
		const selectedFields = qrConfig.selectedFields || []

		// 根据格式类型生成内容
		switch (formatType) {
			case 'CUSTOM':
				// 自定义格式：使用分隔符连接选定字段段
				if (selectedFields.length > 0) {
					// 使用配置中的separator，如果没有则使用默认:'|'
					const separator = qrConfig.separator || qrConfig.customSeparator || '|'
					// 使用明细行分隔符，如果没有配置则使用默认的井号
					const detailRowSeparator = qrConfig.detailRowSeparator || '#'

					// 判断是否有明细字段
					const hasDetailFields = selectedFields.some(fieldKey =>
						dynamicFieldData.items && dynamicFieldData.items.length > 0 &&
						dynamicFieldData.items[0].hasOwnProperty(fieldKey)
					)

					if (hasDetailFields && dynamicFieldData.items && dynamicFieldData.items.length > 0) {
						// 如果有明细字段，按行循环处理，每行按配置顺序排列字段
						const detailParts = []

						dynamicFieldData.items.forEach((item) => {
							const rowValues = []
							// 按照配置的字段顺序处理每一行
							selectedFields.forEach((fieldKey) => {
								// 首先检查是否为明细字段（在明细数据中存在且值会变化）
								if (item.hasOwnProperty(fieldKey)) {
									// 明细字段：使用当前行数据
									rowValues.push(item[fieldKey] || '')
								} else if (dynamicFieldData[fieldKey] !== undefined) {
									// 主字段：使用主表数据
									rowValues.push(dynamicFieldData[fieldKey] || '')
								} else {
									// 字段不存在，添加空值
									rowValues.push('')
								}
							})
							detailParts.push(rowValues.join(separator))
						})

					// 使用明细行分隔符连接各行
					content = detailParts.join(detailRowSeparator)
					console.log('QR Code Content (with details):', content)
					} else {
						// 如果没有明细字段，只处理主字段
						const values = []
						// 按照用户选择的字段顺序处理
						selectedFields.forEach((fieldKey) => {
							if (dynamicFieldData[fieldKey] !== undefined) {
								values.push(dynamicFieldData[fieldKey] || '')
							}
						})
						content = values.join(separator)
						console.log('QR Code Content (main only):', content)
					}
				}
				break

			case 'JSON':
				// JSON格式：根据数据类型生成不同格:
				if (selectedFields.length > 0) {
					const jsonData = {}
					// 按照用户选择的字段顺序处理
					selectedFields.forEach((fieldKey) => {
						if (dynamicFieldData[fieldKey] !== undefined) {
							jsonData[fieldKey] = dynamicFieldData[fieldKey] || ''
						} else if (dynamicFieldData.items && dynamicFieldData.items.length > 0) {
							// 如果是明细字段段，从items中获取所有条目的值并连接
							const itemValues = dynamicFieldData.items.map(item =>
								item[fieldKey] !== undefined ? item[fieldKey] : ''
							)
							// 使用配置中的明细值分隔符，如果没有则使用默认:','
							const itemValueSeparator = qrConfig.itemValueSeparator || ','
							// 返回所有明细项的值，用指定分隔符连接
							jsonData[fieldKey] = itemValues.filter(val => val !== '').join(itemValueSeparator)
						} else {
							jsonData[fieldKey] = ''
						}
					})

					// 根据jsonDataType决定输出格式
					if (qrConfig.jsonDataType === 'array') {
						// 数组类型：[{"字段段:1":"字段段:1", "字段段:2":"字段段:2"}]
						content = JSON.stringify([jsonData])
					} else {
						// 对象类型：{"字段段:1":"字段段:1", "字段段:2":"字段段:2"}
						content = JSON.stringify(jsonData)
					}
				}
				break

			case 'URL':
				// URL格式：将选定字段段作为查询参数
				if (selectedFields.length > 0) {
					const baseUrl = qrConfig.baseUrl || ''
					const params = new URLSearchParams()
					// 按照用户选择的字段顺序处理
					selectedFields.forEach((fieldKey) => {
						let value = ''
						if (dynamicFieldData[fieldKey] !== undefined) {
							value = dynamicFieldData[fieldKey] || ''
						} else if (dynamicFieldData.items && dynamicFieldData.items.length > 0) {
							// 如果是明细字段段，从items中获取所有条目的值并连接
							const itemValues = dynamicFieldData.items.map(item =>
								item[fieldKey] !== undefined ? item[fieldKey] : ''
							)
							// 使用配置中的明细值分隔符，如果没有则使用默认:','
							const itemValueSeparator = qrConfig.itemValueSeparator || ','
							// 返回所有明细项的值，用指定分隔符连接
							value = itemValues.filter(val => val !== '').join(itemValueSeparator)
						}
						if (value) {
							params.append(fieldKey, value)
						}
					})
					content = baseUrl + (params.toString() ? '?' + params.toString() : '')
				}
				break

			default:
				break
		}

		// 在内容最后添加结束标识符（如果配置了的话）
		if (qrConfig.endMarker && qrConfig.endMarker.trim() !== '') {
			if (content && content.trim() !== '') {
				content = content + qrConfig.endMarker
			} else {
				content = qrConfig.endMarker
			}
		}

		return content
	} catch (error) {
		return ''
	}
}

// convertFormData 方法中的数据回显部分
const convertFormData = (record) => {
	const param = {
		id: record.id
	}
	// 查询详情
	recordApi.recordDetail(param)
		.then((data) => {
			if (data) {
				formData.value = Object.assign(formData.value, data)
				// 如果有模板ID，加载模板相关信息
				if (data.templateId) {
					templateApi.templateDetail({ id: data.templateId })
						.then((templateData) => {
							if (templateData) {
								// 设置业务类型
								formData.value.businessType = templateData.type
								// 加载该业务类型下的所有模板
								loadTemplatesByType(templateData.type)

								// 如果有打印数据，从中提取主字段数据
								let dynamicFieldDataFromPrint = {}
								if (data.printData) {
									try {
										const printData = JSON.parse(data.printData)

										// 判断是否为多页数据（数组格式）
										let firstPageData = printData
										if (Array.isArray(printData) && printData.length > 0) {
											// 如果是数组，取第一页的数据进行回显
											firstPageData = printData[0]
										}

										// 从printData中提取主字段数据（排除qrContent和items等特殊字段）
										Object.keys(firstPageData).forEach((key) => {
											// 排除特殊字段，其余都视为主字段数据
											if (key !== 'qrContent' && key !== 'items' && key !== '_pageIndex' && key !== '_pageCount') {
												dynamicFieldDataFromPrint[key] = firstPageData[key]
											}
										})
									} catch (e) {
										console.error('解析打印数据失败:', e)
										// 打印数据不是有效的JSON格式
									}
								}

								// 加载模板关联字段，并保留现有数据（会自动提取二维码配置）
								loadTemplateFields(formData.value.templateId, true)
									.then(() => {
										// 字段加载完成后，设置主字段数据
										formData.value.dynamicFieldData = {
											...formData.value.dynamicFieldData,
											...dynamicFieldDataFromPrint
										}

										// 字段加载完成后，对日期字段进行格式转换以便正确显示
										if (mainFields.value && mainFields.value.length > 0) {
											mainFields.value.forEach(field => {
												if ((field.inputType === 'DATE' || field.inputType === 'DATE_RANGE') && formData.value.dynamicFieldData[field.fieldKey]) {
													// 将保存的自定义格式日期转换为标准格式用于显示
													const originalValue = formData.value.dynamicFieldData[field.fieldKey]
													const displayValue = formatFieldValueForDisplay(field, originalValue)
													formData.value.dynamicFieldData[field.fieldKey] = displayValue
												}
											})
										}

										// 字段加载完成后，加载明细表格数据
										loadDetailTableDataFromForm()
									})
									.catch((error) => {
										message.error('加载模板字段失败，请检查模板配置')
										console.error('编辑模式加载模板字段失败:', error)
									})
							} else {
								message.error('模板数据不存在或已被删除')
							}
						})
						.catch((error) => {
							message.error('加载模板信息失败，请检查模板是否存在')
							console.error('编辑模式加载模板信息失败:', error)
						})
				} else {
					message.warning('没有关联的模板信息')
				}
			} else {
				message.error('打印记录数据不存在或已被删除')
			}
		})
		.catch((error) => {
			message.error('加载打印记录详情失败')
			console.error('加载打印记录详情失败:', error)
		})
}

// 表单验证规则
const formRules = {
	businessType: [required('请选择业务类型')],
	templateId: [required('请选择关联模板')],
	printCount: [required('请输入打印次:')],
	businessKey: [required('请输入业务标:')]
}

// 业务类型选项
const businessTypeOptions = tool.dictList('BUSINESS_TYPE')

// 获取编码规则列表
const loadCodeRules = async () => {
	try {
		// 获取编码规则分页数据
		const params = {
			current: 1,
			size: 1000 // 获取所有编码规:
		}

		const res = await wqsCodeRuleApi.wqsCodeRulePage(params)

		if (res && res.records) {
			// 将编码规则转换为下拉选项格式
			codeRuleOptions.value = res.records.map(rule => ({
				label: `${rule.ruleName} (${rule.ruleCode})`,
				value: rule.id, // 使用规则ID作为:
				ruleCode: rule.ruleCode, // 保存规则编码用于预览
				ruleName: rule.ruleName, // 保存规则名称
				segments: rule.segments // 保存规则片段用于预览
			}))

		} else {
			codeRuleOptions.value = []

		}
		// 设置编码规则加载完成标志
		codeRulesLoaded.value = true

	} catch (error) {

		message.error('获取编码规则列表失败')
		codeRuleOptions.value = []
		// 即使失败也要设置加载完成标志
		codeRulesLoaded.value = true
	}
}

// 流水号字段段检测函:
const detectSerialNumberFields = () => {
	const serialFields = []

	// 检查主字段段中的流水号字段:
	if (mainFields.value && mainFields.value.length > 0) {
		mainFields.value.forEach(field => {
			// 检查字段段是否配置了流水:
			if (field.serialNumberConfig &&
				field.serialNumberConfig.enabled === true) {

				serialFields.push({
					fieldKey: field.fieldKey,
					fieldLabel: field.fieldLabel,
					config: field.serialNumberConfig,
					scope: 'MAIN'
				})
			}

			// 检查动态条码字段段中是否包含流水号段
			if (field.inputType === 'DYNAMIC_BARCODE' &&
				field.dynamicBarcodeConfig) {
				const hasSerialSegment = checkBarcodeHasSerialSegment(field.dynamicBarcodeConfig)
				if (hasSerialSegment) {

					serialFields.push({
						fieldKey: field.fieldKey,
						fieldLabel: field.fieldLabel,
						config: hasSerialSegment,
						scope: 'MAIN',
						type: 'BARCODE'
					})
				}
			}
		})
	}


	return serialFields
}

// 检查条码配置中是否包含流水号段
const checkBarcodeHasSerialSegment = (barcodeConfig) => {


	if (!barcodeConfig || !barcodeConfig.codeRule) {

		return false
	}

	const codeRule = codeRuleOptions.value.find(rule =>
		String(rule.value) === String(barcodeConfig.codeRule))



	if (!codeRule || !codeRule.segments) {

		return false
	}

	let segments = codeRule.segments
	if (typeof segments === 'string') {
		segments = JSON.parse(segments)
	}



	// 查找流水号段，并返回相关配置
	const serialSegment = segments.find((segment, index) => {

		if (segment.type === 'serial') {

			return true
		}
		return false
	})

	if (serialSegment && serialSegment.type === 'serial') {
		const result = {
			...serialSegment,
			segmentIndex: segments.findIndex(s => s.type === 'serial'),
			ruleId: codeRule.value
		}

		return result
	}


	return false
}

// 格式化流水号
const formatSerialNumber = (serialNumber, fieldConfig) => {
	if (fieldConfig.config && fieldConfig.config.length) {
		return String(serialNumber).padStart(fieldConfig.config.length, '0')
	}
	return String(serialNumber)
}

// 生成带流水号的条码内:
const generateDynamicBarcodeContentWithSerial = (fieldConfig, dynamicFieldData, serialNumber) => {
	try {
		// 查找对应的编码规:
		const codeRule = codeRuleOptions.value.find(rule =>
			String(rule.value) === String(fieldConfig.config.ruleId))

		if (!codeRule) {

			return ''
		}

		// 解析片段配置
		let segments = codeRule.segments
		if (typeof segments === 'string') {
			segments = JSON.parse(segments)
		}

		if (!Array.isArray(segments) || segments.length === 0) {
			return ''
		}

		// 根据片段配置生成内容
		const contentParts = []
		segments.forEach((segment, index) => {
			switch (segment.type) {
				case 'fixed':
					// 固定值片:
					contentParts.push(segment.value || '')
					break
				case 'field':
					// 字段段片段 - 从动态数据中获取:
					const fieldName = segment.fieldName
					const fieldValue = dynamicFieldData[fieldName] || ''
					contentParts.push(fieldValue)
					break
				case 'serial':
					// 序列号片: - 使用传入的序列号
					const length = segment.length || 3
					const formattedSerial = String(serialNumber).padStart(length, '0')
					contentParts.push(formattedSerial)
					break
				case 'separator':
					// 分隔符片:
					contentParts.push(segment.separator || '')
					break
				default:
					// 未知类型，显示原始:
					contentParts.push(segment.value || '')
			}
		})

		return contentParts.join('')
	} catch (error) {
		return ''
	}
}



// 新增：生成多页数据的函数
const generateMultiPageData = async () => {
	try {
		const startValue = serialNumberRangeConfig.value.startValue || 1
		const endValue = serialNumberRangeConfig.value.endValue || 1
		const selectedField = serialNumberRangeConfig.value.selectedField

		if (!selectedField) {
			throw new Error('未选择流水号字段')
		}

		const fieldConfig = serialNumberFields.value.find(f => f.fieldKey === selectedField)
		if (!fieldConfig) {
			throw new Error('未找到选定的流水号字段配置')
		}

		const pageCount = Math.max(0, endValue - startValue + 1)

		if (pageCount > 1000) {
			throw new Error('单次生成的页数不能超过1000')
		}

		// 构建多页数据
		const multiPageData = []
		for (let i = 0; i < pageCount; i++) {
			const currentSerial = startValue + i
			
			// 深拷贝dynamicFieldData以避免引用问题
			const dynamicFieldDataCopy = JSON.parse(JSON.stringify(formData.value.dynamicFieldData || {}))
			
			// 创建页面数据，确保没有重复嵌套
			const pageData = {
				// 只保留必要的表单字段，避免嵌套对象
				businessKey: formData.value.businessKey,
				templateId: formData.value.templateId,
				// 为每页生成唯一ID（设为null表示新记录）
				id: null,
				// 使用深拷贝的dynamicFieldData
				dynamicFieldData: dynamicFieldDataCopy,
				// 添加页面标识
				_pageIndex: i + 1,
				_totalPages: pageCount,
				_serialNumber: currentSerial
			}

			// 根据字段类型设置流水号
			if (fieldConfig.type === 'BARCODE') {
				// 条码字段：重新生成包含流水号的条码内容
				pageData.dynamicFieldData[fieldConfig.fieldKey] =
					generateDynamicBarcodeContentWithSerial(fieldConfig, pageData.dynamicFieldData, currentSerial)
			} else {
				// 普通流水号字段：直接设置格式化后的流水号
				pageData.dynamicFieldData[fieldConfig.fieldKey] =
					formatSerialNumber(currentSerial, fieldConfig)
			}

			// 确保没有重复嵌套的items数组
			if (pageData.items && Array.isArray(pageData.items)) {
				// 清理items数组中的重复嵌套
				pageData.items = pageData.items.map(item => {
					if (item && typeof item === 'object' && item.items) {
						// 如果item有items属性，移除它以防止嵌套
						const { items, ...cleanedItem } = item
						return cleanedItem
					}
					return item
				})
			}

			multiPageData.push(pageData)
		}

		return multiPageData
	} catch (error) {
		throw error
	}
}

// 导出明细数据模板
const exportDetailTemplate = () => {
	if (!detailFields.value || detailFields.value.length === 0) {
		message.warning('当前没有明细字段段可导:')
		return
	}

	try {
		// 动态导入xlsx:
		import('xlsx').then(XLSX => {
			// 创建表头
			const headers = detailFields.value.map(field => field.title)

			// 添加示例:
			const exampleRow = detailFields.value.map(field => {
				switch (field.inputType) {
					case 'DATE':
						return '2025-01-01'
					case 'SELECT':
						return field.optionsData ? '选项1' : ''
					case 'NUMBER':
						return '123'
					default:
						return '示例数据'
				}
			})

			// 组装数据
			const data = [headers, exampleRow]

			// 创建工作:
			const ws = XLSX.utils.aoa_to_sheet(data)
			const wb = XLSX.utils.book_new()
			XLSX.utils.book_append_sheet(wb, ws, '明细模板')

			// 获取业务标识名称
			const businessKeyName = formData.value.businessKey || '明细模板'

			// 导出Excel文件
			XLSX.writeFile(wb, `${businessKeyName}明细模板.xlsx`)

			message.success('模板导出成功')
		}).catch(error => {
			console.error('导入xlsx库失::', error)
			message.error('模板导出失败: xlsx库加载失:')
		})
	} catch (error) {
		message.error('模板导出失败: ' + (error.message || error))
	}
}

// 导入明细数据
const importDetailData = () => {
	if (!detailFields.value || detailFields.value.length === 0) {
		message.warning('当前没有明细字段段可供导入')
		return
	}

	try {
		// 创建文件选择:
		const fileInput = document.createElement('input')
		fileInput.type = 'file'
		fileInput.accept = '.xlsx, .xls'
		fileInput.onchange = (event) => {
			const file = event.target.files[0]
			if (!file) return

			// 动态导入xlsx:
			import('xlsx').then(XLSX => {
				const reader = new FileReader()
				reader.onload = (e) => {
					try {
						const data = e.target.result
						let rows = []

						if (file.name.endsWith('.xlsx') || file.name.endsWith('.xls')) {
							// 处理Excel文件
							const workbook = XLSX.read(data, { type: 'array' })
							const sheetName = workbook.SheetNames[0]
							const worksheet = workbook.Sheets[sheetName]
							rows = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
						} else {
							message.warning('请选择Excel文件(.xlsx:.xls格式)')
							return
						}

						if (rows.length < 1) {
							message.warning('文件中没有数:')
							return
						}

						// 处理数据
						processImportedData(rows)
					} catch (error) {
						message.error('文件处理失败: ' + (error.message || error))
					}
				}

				// 读取Excel文件
				reader.readAsArrayBuffer(file)
			}).catch(error => {
				console.error('导入xlsx库失::', error)
				message.error('导入失败: xlsx库加载失:')
			})
		}
		fileInput.click()
	} catch (error) {
		message.error('导入失败: ' + (error.message || error))
	}
}

// 注意：parseCSV函数已移除，现在使用xlsx库处理Excel文件

// 处理导入的数据
const processImportedData = (rows) => {
	if (rows.length < 1) return

	try {
		// 获取表头（第一行）
		const headers = rows[0]

		// 验证表头是否匹配
		const fieldKeys = detailFields.value.map(field => field.title)
		const missingFields = fieldKeys.filter(key => !headers.includes(key))

		if (missingFields.length > 0) {
			message.warning(`模板不匹配，缺少字段: ${missingFields.join(', ')}`)
			return
		}

		// 转换数据行，过滤空行
		const dataRows = rows.slice(1)
		const newDetailTableData = []

		for (let i = 0; i < dataRows.length; i++) {
			const row = dataRows[i]

			// 检查是否为空行：如果行中所有值都为空或未定义，则跳过该行
			const isEmptyRow = !row || row.length === 0 || row.every(cell => {
				// 判断单元格是否为空
				if (cell === null || cell === undefined) return true
				if (typeof cell === 'string' && cell.trim() === '') return true
				return false
			})

			// 如果是空行，跳过处理
			if (isEmptyRow) {
				console.log(`跳过第 ${i + 2} 行（空行）`) // +2 因为Excel行号从1开始，且跳过了表头
				continue
			}

			const rowData = { key: Date.now() + i }

			// 根据表头映射数据
			for (let j = 0; j < headers.length; j++) {
				const header = headers[j]
				const field = detailFields.value.find(f => f.title === header)

				if (field) {
					// 处理特殊字段类型
					let value = row[j] || ''

					// 处理复选框类型
					if (field.inputType === 'CHECKBOX') {
						value = value ? value.split(',').map(v => v.trim()) : []
					}

					rowData[field.fieldKey] = value
				}
			}

			newDetailTableData.push(rowData)
		}

		// 更新表格数据
		detailTableData.value = newDetailTableData

		const totalRows = dataRows.length
		const validRows = newDetailTableData.length
		const skippedRows = totalRows - validRows

		// 提供更详细的导入反馈
		if (skippedRows > 0) {
			message.success(`成功导入 ${validRows} 行数据，跳过 ${skippedRows} 行空行`)
		} else {
			message.success(`成功导入 ${validRows} 行数据`)
		}
	} catch (error) {
		message.error('数据处理失败: ' + (error.message || error))
	}
}

// 新增：更新所有动态条码字段的内容
const updateAllDynamicBarcodeFields = () => {
	// 检查编码规则是否已加载完成
	if (!codeRulesLoaded.value) {
		return
	}

	// 更新主字段中的动态条码
	if (dynamicFields.value && dynamicFields.value.length > 0) {
		// 过滤出主字段
		const mainFields = dynamicFields.value.filter(field => field.fieldScope === FIELD_SCOPE.MAIN)

		mainFields.forEach(field => {
			if (field.inputType === 'DYNAMIC_BARCODE' && field.dynamicBarcodeConfig) {
				const content = generateDynamicBarcodeContent(field.dynamicBarcodeConfig, formData.value.dynamicFieldData)

				if (content !== formData.value.dynamicFieldData[field.fieldKey]) {
					formData.value.dynamicFieldData[field.fieldKey] = content
				}
			}
		})
	}

	// 更新明细字段中的动态条码
	if (dynamicFields.value && dynamicFields.value.length > 0 && formData.value.detailTableData) {
		// 过滤出明细字段
		const detailFields = dynamicFields.value.filter(field => field.fieldScope === FIELD_SCOPE.DETAIL)
		detailFields.forEach(field => {
			if (field.inputType === 'DYNAMIC_BARCODE' && field.dynamicBarcodeConfig) {
				formData.value.detailTableData.forEach((row, rowIndex) => {
					const content = generateDynamicBarcodeContent(field.dynamicBarcodeConfig, {
						...formData.value.dynamicFieldData, // 主字段数据
						...row // 当前行数据
					})
					if (content !== row[field.fieldKey]) {
						row[field.fieldKey] = content
					}
				})
			}
		})
	}
}

// 生成动态条码内容
const generateDynamicBarcodeContent = (barcodeConfig, fieldData) => {
	try {
		if (!barcodeConfig || !barcodeConfig.codeRule) {
			return ''
		}

		// 查找对应的编码规则
		const codeRule = codeRuleOptions.value.find(rule =>
			String(rule.value) === String(barcodeConfig.codeRule)
		)

		if (!codeRule || !codeRule.segments) {
			return ''
		}

		let segments = codeRule.segments
		if (typeof segments === 'string') {
			segments = JSON.parse(segments)
		}

		if (!Array.isArray(segments)) {
			return ''
		}

		// 根据片段生成条码内容
		let result = ''
		for (const segment of segments) {
			// 将片段类型转换为大写进行比较，兼容大小写格式
			const segmentType = (segment.type || '').toUpperCase()

			if (segmentType === 'FIXED') {
				// 固定文本
				result += segment.value || ''
			} else if (segmentType === 'DATE') {
				// 日期格式
				const format = segment.format || segment.dateFormat || 'YYYYMMDD'

				// 处理特殊格式
				if (format === 'MM_ABC') {
					// 10/11/12月用A/B/C表示
					const month = dayjs().month() + 1 // dayjs的月份从0开始
					if (month === 10) {
						result += 'A'
					} else if (month === 11) {
						result += 'B'
					} else if (month === 12) {
						result += 'C'
					} else {
						result += String(month).padStart(2, '0')
					}
				} else if (format === 'YYYYMM_ABC') {
					// YYYYMM格式，但10/11/12月用A/B/C表示
					const year = dayjs().year()
					const month = dayjs().month() + 1
					if (month === 10) {
						result += year + 'A'
					} else if (month === 11) {
						result += year + 'B'
					} else if (month === 12) {
						result += year + 'C'
					} else {
						result += year + String(month).padStart(2, '0')
					}
				} else if (format === 'YYYYMMDDHHmmss') {
					// YYYYMMDDHHmmss格式
					result += dayjs().format(format)
				} else {
					// 标准格式
					result += dayjs().format(format)
				}
			} else if (segmentType === 'SERIAL') {
				// 流水号，这里使用默认值
				const length = segment.length || 4
				const defaultValue = segment.startValue || 1
				result += String(defaultValue).padStart(length, '0')
			} else if (segmentType === 'FIELD') {
				// 动态字段
				const fieldKey = segment.fieldName || segment.fieldKey  // 兼容fieldName和fieldKey
				if (fieldKey && fieldData && fieldData[fieldKey]) {
					result += String(fieldData[fieldKey])
				} else {
					// 如果字段值为空，使用默认值
					result += segment.defaultValue || ''
				}
			} else {
				// 添加调试信息，帮助识别未处理的片段类型
				console.warn('未识别的片段类型:', segment.type, segment)
			}
		}

		return result
	} catch (error) {
		console.error('生成动态条码内容失败:', error)
		return ''
	}
}

// 监听动态字段段数据变化，自动更新动态条码内:
watch(
	() => formData.value.dynamicFieldData,
	() => {
		// 延迟执行，确保数据更新完:
		nextTick(() => {
			updateAllDynamicBarcodeFields()
		})
	},
	{ deep: true }
)

// 监听明细表格数据变化，自动更新动态条码内:
watch(
	() => formData.value.detailTableData,
	() => {
		// 延迟执行，确保数据更新完:
		nextTick(() => {
			updateAllDynamicBarcodeFields()
		})
	},
	{ deep: true }
)

// 监听编码规则加载状态，加载完成后更新动态条码字段
watch(
	codeRulesLoaded,
	(newValue) => {
		if (newValue && dynamicFields.value.length > 0) {
			nextTick(() => {
				updateAllDynamicBarcodeFields()
			})
		}
	}
)

// 调用这个函数将子组件的一些数据和方法暴露出去
defineExpose({
	onOpen
})
</script>

<style scoped lang="less">
.main-fields-section,
.detail-fields-section,
.serial-number-range-section {
	margin-top: 16px;

	.ant-divider {
		margin: 16px 0;
		font-weight: 500;
		color: #1890ff;
	}
}

.section {
	margin-bottom: 16px;
	display: flex;
	justify-items: center;
	align-items: center;
}

.section-device {
	width: 5px;
	height: 20px;
	margin-right: 10px;
	background: var(--primary-color);
	border-radius: 5px;
}

.section-title {
	font-size: 16px;
	font-weight: 500;
	color: #333;
	flex: 1;
}

.serial-number-range-section {
	background: #f8f9fa;
	padding: 16px;
	border-radius: 6px;
	border: 1px solid #e9ecef;

	.ant-divider {
		color: #52c41a;
	}
}

/* 明细表格样式 */
.editable-table {
	margin-bottom: 16px;

	:deep(.ant-table-cell) {
		padding: 8px 12px;
	}

	:deep(.ant-input),
	:deep(.ant-select),
	:deep(.ant-date-picker) {
		border: none;
		box-shadow: none;
		background: transparent;
	}

	:deep(.ant-input:focus),
	:deep(.ant-select:focus),
	:deep(.ant-date-picker:focus) {
		border-color: #1890ff;
		box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
	}
}

.detail-table-actions {
	margin-top: 8px;
}

.detail-table-actions-top {
	margin-bottom: 12px;

	.ant-btn {
		margin-right: 8px;
	}
}
</style>
