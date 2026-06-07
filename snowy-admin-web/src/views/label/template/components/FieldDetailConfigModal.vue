<template>
	<!-- 字段详细配置弹窗组件 -->
	<a-modal
		v-model:open="modalVisible"
		title="字段配置"
		width="800px"
		:confirm-loading="loading"
		@ok="handleSave"
		@cancel="handleCancel"
	>
		<a-form
			:model="formData"
			:rules="formRules"
			:label-col="{ span: 3 }"
			:wrapper-col="{ span: 21 }"
			ref="formRef"
			layout="horizontal"
		>
			<!-- 基础信息 -->
			<BasicInfoPanel :config="formData" />

			<!-- 通用配置 -->
			<GeneralConfigPanel :config="formData" />

			<!-- 默认值设置 -->
			<DefaultValueConfigPanel :config="formData" />

			<!-- 时间控件配置 -->
			<DateConfigSection v-if="['DATE', 'DATE_RANGE'].includes(formData.inputType)" :config="formData" />

			<!-- 下拉选择/复选框/单选框配置 -->
			<SelectConfigPanel
				v-if="['SELECT', 'RADIO', 'CHECKBOX'].includes(formData.inputType)"
				:config="formData"
				:dict-type-options="dictTypeOptions"
			/>

			<!-- 二维码配置 -->
			<QrCodeConfigPanel
				v-if="formData.inputType === 'QRCODE'"
				:config="formData.qrCodeConfig"
				:available-field-options="availableFieldOptions"
			/>

			<!-- 动态条码配置 -->
			<DynamicBarcodeConfigPanel
				v-if="formData.inputType === 'DYNAMIC_BARCODE'"
				:config="formData.dynamicBarcodeConfig"
				:code-rule-options="codeRuleOptions"
				:available-field-options="availableFieldOptions"
			/>

			<!-- 数字输入配置 -->
			<NumberConfigPanel v-if="formData.inputType === 'NUMBER'" :config="formData.numberConfig" />

			<!-- 文本输入配置 -->
			<InputConfigPanel v-if="formData.inputType === 'INPUT'" :config="formData.inputConfig" />

			<!-- 文本域配置 -->
			<TextareaConfigPanel v-if="formData.inputType === 'TEXTAREA'" :config="formData.textareaConfig" />
		</a-form>
	</a-modal>
</template>

<script setup name="FieldDetailConfigModal">
	import { ref, computed, watch } from 'vue'
	import { message } from 'ant-design-vue'
	import BasicInfoPanel from './BasicInfoPanel.vue'
	import GeneralConfigPanel from './GeneralConfigPanel.vue'
	import DateConfigSection from './DateConfigSection.vue'
	import QrCodeConfigPanel from './QrCodeConfigPanel.vue'
	import DynamicBarcodeConfigPanel from './DynamicBarcodeConfigPanel.vue'
	import NumberConfigPanel from './NumberConfigPanel.vue'
	import InputConfigPanel from './InputConfigPanel.vue'
	import TextareaConfigPanel from './TextareaConfigPanel.vue'
	import DefaultValueConfigPanel from './DefaultValueConfigPanel.vue'
	import SelectConfigPanel from './SelectConfigPanel.vue'
	import { useFieldConfig } from '@/composables/useFieldConfig'

	// 组件属性
	const props = defineProps({
		// 弹窗显示状态
		open: {
			type: Boolean,
			default: false
		},
		// 字段数据
		fieldData: {
			type: Object,
			default: () => ({})
		},
		// 模板信息
		templateInfo: {
			type: Object,
			default: () => ({})
		},
		// 字段作用域
		fieldScope: {
			type: String,
			default: 'MAIN'
		}
	})

	// 组件事件
	const emit = defineEmits(['update:open', 'save'])

	// 使用字段配置组合式函数
	const {
		// 响应式数据
		loading: fieldConfigLoading,
		dictTypes,
		codeRuleOptions,
		availableFieldOptions,
		// 方法
		loadDictTypes,
		loadCodeRules,
		loadAvailableFields
	} = useFieldConfig()

	// 响应式数据
	const loading = ref(false)
	const formRef = ref()
	const formData = ref({})

	// 字典类型选项（用于字典数据源选择）
	const dictTypeOptions = computed(() => {
		return dictTypes.value.map((dict) => ({
			label: dict.dictTypeName,
			value: dict.dictTypeCode
		}))
	})

	// 表单验证规则
	const formRules = {
		// 基础字段验证在保存时进行，这里主要验证配置项
		dictTypeCode: [
			{
				required: true,
				message: '请选择字典类型',
				trigger: 'blur',
				// 只有当数据来源为字典时才验证
				validator: (rule, value) => {
					if (formData.value.dataSource === 'dict' && !value) {
						return Promise.reject('请选择字典类型')
					}
					return Promise.resolve()
				}
			}
		],
		optionApiUrl: [
			{
				required: true,
				message: '请输入选项API地址',
				trigger: 'blur',
				// 只有当数据来源为API时才验证
				validator: (rule, value) => {
					if (formData.value.dataSource === 'api' && !value) {
						return Promise.reject('请输入选项API地址')
					}
					return Promise.resolve()
				}
			}
		],
		staticOptions: [
			{
				required: true,
				message: '请输入静态选项数据',
				trigger: 'blur',
				// 只有当数据来源为静态数据时才验证
				validator: (rule, value) => {
					if (formData.value.dataSource === 'static' && !value) {
						return Promise.reject('请输入静态选项数据')
					}
					return Promise.resolve()
				}
			}
		]
	}

	// 计算属性：弹窗显示状态
	const modalVisible = computed({
		get: () => props.open,
		set: (value) => emit('update:open', value)
	})

	// 初始化表单数据
	const initFormData = () => {
		// 如果没有字段数据，初始化一个空的表单结构
		// 注意：新增字段有tempId但没有id，也应该使用传入的字段数据
		if (!props.fieldData) {
			formData.value = {
				id: '',
				fieldKey: '',
				title: '',
				inputType: 'INPUT',
				isRequired: 0,
				templateId: props.templateInfo?.id || '',
				fieldScope: props.fieldScope || 'MAIN',
				placeholder: '',
				defaultValue: '',
				sortCode: 0,
				status: 'ENABLE',
				dictTypeCode: '',
				isMultiple: '0',
				dataSource: 'dict',
				dateFormat: '',
				optionApiUrl: '',
				selectedDataApiUrl: '',
				staticOptions: '',
				qrCodeConfig: {
					formatType: 'json',
					jsonDataType: 'object',
					selectedFields: [],
					separator: '|',
					itemValueSeparator: ',', // 添加默认的明细字段值分隔符
					detailRowSeparator: '#', // 添加默认的明细行分隔符
					detailRowPrefix: '', // 添加默认的行前缀
					detailRowNumber: false, // 是否启用行号
					endMarker: '', // 添加默认的结束标识符
					encodeMode: 'none', // 编码方式：none/base64/gzip_base64
					formatId: '', // ANSI格式标识，如：21
					startMarker: '' // 添加默认的开始标识符
				},
				// 动态条码配置
				dynamicBarcodeConfig: {
					codeRule: '',
					selectedFields: [],
					previewContent: ''
				},
				numberConfig: {
					min: null,
					max: null,
					step: 1,
					precision: 0
				}, // 文本输入配置
				inputConfig: {
					enablePadding: false,
					paddingDirection: 'left',
					totalLength: null,
					paddingChar: '0'
				},
				// 文本域配置
				textareaConfig: {
					rows: 4,
					maxLength: null,
					showCount: false
				},
				// 日期配置
				dateConfig: {
					displayFormat: 'YYYY-MM-DD',
					saveFormat: 'YYYY-MM-DD',
					inputFormats: ['YYYY-MM-DD', 'YYYY.MM.DD', 'YYYY/MM/DD'],
					precision: { year: 4, month: 2, day: 2 },
					separators: { dateSeparator: '-', rangeSeparator: ' - ' },
					display: { showWeekday: false },
					validation: { allowFuture: true, allowPast: true }
				}
			}
			return
		}

		// 深拷贝字段数据
		const data = JSON.parse(JSON.stringify(props.fieldData))

		// 解析optionsData中的配置
		let parsedOptions = {}
		if (data.optionsData) {
			try {
				parsedOptions = JSON.parse(data.optionsData)
			} catch (e) {
				// 解析字段配置失败
			}
		}

		// 提取配置数据
		const dictTypeCode = parsedOptions.dictTypeCode || data.dictTypeCode || ''
		const optionApiUrl = parsedOptions.optionApiUrl || data.optionApiUrl || ''
		const selectedDataApiUrl = parsedOptions.selectedDataApiUrl || data.selectedDataApiUrl || ''
		const staticOptions = parsedOptions.staticOptions || ''
		let defaultValue = parsedOptions.defaultValue !== undefined ? parsedOptions.defaultValue : ''

		// 根据现有数据判断数据来源
		let dataSource = 'dict' // 默认为字典数据源
		if (optionApiUrl || selectedDataApiUrl) {
			dataSource = 'api'
		} else if (staticOptions) {
			dataSource = 'static'
		} else if (dictTypeCode) {
			dataSource = 'dict'
		}

		// 特殊处理选择类控件的默认值
		if (['SELECT', 'RADIO', 'CHECKBOX'].includes(data.inputType)) {
			// 如果默认值是字符串且包含逗号，且是多选控件，则转换为数组
			if (
				typeof defaultValue === 'string' &&
				defaultValue.includes(',') &&
				(data.inputType === 'CHECKBOX' || (data.inputType === 'SELECT' && data.isMultiple === '1'))
			) {
				defaultValue = defaultValue.split(',').map((item) => item.trim())
			}
		}
		formData.value = {
			...data,
			// 确保必填字段有默认值
			fieldKey: data.fieldKey || '',
			title: data.title || '',
			inputType: data.inputType || 'INPUT',
			isRequired: data.isRequired !== undefined ? data.isRequired : 0,
			templateId: data.templateId || props.templateInfo?.id || '',
			fieldScope: data.fieldScope || props.fieldScope || '',
			// 状态字段映射：从optionsData中解析状态，如果没有则使用默认值
			status: parsedOptions.status || data.status || data.enableStatus || 'ENABLE',
			// 数据来源配置
			dataSource: parsedOptions.dataSource || dataSource,
			// 时间格式配置
			dateFormat: parsedOptions.dateFormat || '',
			// 默认值
			defaultValue: defaultValue,
			// 字典和API配置
			dictTypeCode,
			optionApiUrl,
			selectedDataApiUrl,
			staticOptions,
			// 二维码配置
			qrCodeConfig: {
				formatType: 'json',
				jsonDataType: 'object',
				selectedFields: [],
				separator: '|',
				itemValueSeparator: ',',
				detailRowSeparator: '#',
				detailRowPrefix: '',
				detailRowNumber: false,
				detailIncludeMainFields: false,
				startMarker: '',
				endMarker: '',
				fieldPrefixes: {},
				previewContent: '',
				encodeMode: 'none',
				detailLoopMode: 'join',
				...parsedOptions.qrCodeConfig
			},
			// 动态条码配置
			dynamicBarcodeConfig: {
				codeRule: '',
				selectedFields: [],
				previewContent: '',
				...parsedOptions.dynamicBarcodeConfig
			},
			// 数字输入配置
			numberConfig: {
				min: null,
				max: null,
				step: 1,
				precision: 0,
				...parsedOptions.numberConfig
			},
			// 文本输入配置
			inputConfig: {
				enablePadding: false,
				paddingDirection: 'left',
				totalLength: null,
				paddingChar: '0',
				...parsedOptions.inputConfig
			},
			// 文本域配置
			textareaConfig: {
				rows: 4,
				maxLength: null,
				showCount: false,
				...parsedOptions.textareaConfig
			},
			// 日期配置
			dateConfig: {
				displayFormat: 'YYYY-MM-DD',
				saveFormat: 'YYYY-MM-DD',
				inputFormats: ['YYYY-MM-DD', 'YYYY.MM.DD', 'YYYY/MM/DD'],
				precision: { year: 4, month: 2, day: 2 },
				separators: { dateSeparator: '-', rangeSeparator: ' - ' },
				display: { showWeekday: false },
				validation: { allowFuture: true, allowPast: true },
				...parsedOptions.dateConfig
			}
		}

		// 兼容旧数据：将旧版 enableGzip/enableBase64 迁移为 encodeMode
		const qrCfg = formData.value.qrCodeConfig
		if (qrCfg && (qrCfg.enableGzip !== undefined || qrCfg.enableBase64 !== undefined)) {
			qrCfg.encodeMode = qrCfg.enableGzip ? 'gzip_base64' : qrCfg.enableBase64 ? 'base64' : 'none'
			delete qrCfg.enableGzip
			delete qrCfg.enableBase64
		}
	}

	// 处理保存
	const handleSave = async () => {
		try {
			loading.value = true

			// 验证表单
			await formRef.value?.validate()

			// 验证基础必填字段
			if (!formData.value.fieldKey) {
				message.error('字段标识不能为空')
				loading.value = false
				return
			}
			if (!formData.value.title) {
				message.error('字段名称不能为空')
				loading.value = false
				return
			}
			if (!formData.value.inputType) {
				message.error('控件类型不能为空')
				loading.value = false
				return
			}
			if (formData.value.isRequired === undefined || formData.value.isRequired === null) {
				message.error('是否必填字段不能为空')
				loading.value = false
				return
			}
			if (!formData.value.templateId) {
				message.error('模板ID不能为空')
				loading.value = false
				return
			}

			// 构建配置数据
			const configData = {
				// 基本配置
				placeholder: formData.value.placeholder,
				defaultValue: formData.value.defaultValue,
				// 字段状态配置
				status: formData.value.status || 'ENABLE',

				// 时间控件配置
				dateFormat: formData.value.dateFormat,
				dateConfig: formData.value.dateConfig,

				// 选择类控件配置
				dataSource: formData.value.dataSource,
				dictTypeCode: formData.value.dataSource === 'dict' ? formData.value.dictTypeCode : '',
				optionApiUrl: formData.value.dataSource === 'api' ? formData.value.optionApiUrl : '',
				selectedDataApiUrl: formData.value.dataSource === 'api' ? formData.value.selectedDataApiUrl : '',
				staticOptions: formData.value.dataSource === 'static' ? formData.value.staticOptions : '',
				isMultiple: formData.value.isMultiple,

				// 二维码配置
				qrCodeConfig: {
					formatType: formData.value.qrCodeConfig.formatType,
					jsonDataType: formData.value.qrCodeConfig.jsonDataType,
					selectedFields: formData.value.qrCodeConfig.selectedFields,
					separator: formData.value.qrCodeConfig.separator,
					itemValueSeparator: formData.value.qrCodeConfig.itemValueSeparator,
					detailRowSeparator: formData.value.qrCodeConfig.detailRowSeparator,
					detailRowPrefix: formData.value.qrCodeConfig.detailRowPrefix,
					detailRowNumber: formData.value.qrCodeConfig.detailRowNumber,
					detailIncludeMainFields: formData.value.qrCodeConfig.detailIncludeMainFields,
					startMarker: formData.value.qrCodeConfig.startMarker,
					endMarker: formData.value.qrCodeConfig.endMarker,
					fieldPrefixes: formData.value.qrCodeConfig.fieldPrefixes,
					previewContent: formData.value.qrCodeConfig.previewContent,
					encodeMode: formData.value.qrCodeConfig.encodeMode,
					detailLoopMode: formData.value.qrCodeConfig.detailLoopMode
				},

				// 动态条码配置
				dynamicBarcodeConfig: {
					codeRule: formData.value.dynamicBarcodeConfig.codeRule,
					selectedFields: formData.value.dynamicBarcodeConfig.selectedFields,
					previewContent: formData.value.dynamicBarcodeConfig.previewContent
				},

				// 数字输入配置
				numberConfig: {
					min: formData.value.numberConfig.min,
					max: formData.value.numberConfig.max,
					step: formData.value.numberConfig.step,
					precision: formData.value.numberConfig.precision
				},

				// 文本输入配置
				inputConfig: {
					enablePadding: formData.value.inputConfig.enablePadding,
					paddingDirection: formData.value.inputConfig.paddingDirection,
					totalLength: formData.value.inputConfig.totalLength,
					paddingChar: formData.value.inputConfig.paddingChar
				},

				// 文本域配置
				textareaConfig: {
					rows: formData.value.textareaConfig.rows,
					maxLength: formData.value.textareaConfig.maxLength,
					showCount: formData.value.textareaConfig.showCount
				}
			}

			// 构建完整的字段数据，确保包含所有必填字段
			const fieldData = {
				// 基础必填字段
				id: formData.value.id,
				fieldKey: formData.value.fieldKey,
				title: formData.value.title,
				inputType: formData.value.inputType,
				isRequired: formData.value.isRequired,
				templateId: formData.value.templateId,
				fieldScope: formData.value.fieldScope,
				// 新增字段相关属性
				tempId: formData.value.tempId,
				isNew: formData.value.isNew,
				// 其他字段属性
				placeholder: formData.value.placeholder,
				sortCode: formData.value.sortCode,
				// 选择类控件的主字段属性
				dictTypeCode: formData.value.dataSource === 'dict' ? formData.value.dictTypeCode : '',
				isMultiple: formData.value.isMultiple,
				// 配置数据
				optionsData: JSON.stringify(configData)
			}

			// 发送保存事件
			emit('save', fieldData)
		} catch (error) {
			// 表单验证失败
			message.error('请检查表单数据')
		} finally {
			loading.value = false
		}
	}

	// 处理取消
	const handleCancel = () => {
		// 关闭弹窗，不重置表单数据，让父组件处理
		modalVisible.value = false
	}

	// 获取可用字段列表
	const loadAvailableFieldsForModal = async () => {
		try {
			// 检查是否有模板信息
			if (!props.templateInfo || !props.templateInfo.id) {
				// 模板信息不完整，无法获取字段列表
				return
			}

			// 防止重复加载
			if (fieldConfigLoading.value) {
				return
			}

			// 使用 useFieldConfig 提供的方法加载字段
			await loadAvailableFields(props.templateInfo.id, props.fieldScope || 'MAIN')
		} catch (error) {
			// 获取字段列表失败
			message.error('获取字段列表失败')
		}
	}

	// 加载字典类型列表
	const loadDictTypesForModal = async () => {
		try {
			// 使用 useFieldConfig 提供的方法加载字典类型
			await loadDictTypes()
		} catch (error) {
			// 获取字典类型列表失败
			message.error('获取字典类型列表失败')
		}
	}

	// 加载编码规则列表
	const loadCodeRulesForModal = async () => {
		try {
			// 使用 useFieldConfig 提供的方法加载编码规则
			await loadCodeRules()
		} catch (error) {
			// 获取编码规则列表失败
			message.error('获取编码规则列表失败')
		}
	}

	// 监听字段数据变化，重新初始化表单
	watch(
		() => props.fieldData,
		() => {
			if (props.open) {
				initFormData()
			}
		},
		{ deep: true }
	)

	// 监听弹窗显示状态，初始化表单数据
	watch(
		() => props.open,
		(open) => {
			if (open) {
				initFormData()
				loadAvailableFieldsForModal()
				loadCodeRulesForModal() // 加载编码规则列表
				loadDictTypesForModal() // 加载字典类型列表
			}
		}
	)

	// 监听模板信息和字段作用域变化，重新加载字段列表
	watch(
		() => [props.templateInfo, props.fieldScope],
		() => {
			if (props.open) {
				loadAvailableFieldsForModal()
				loadCodeRulesForModal() // 重新加载编码规则列表
			}
		},
		{ deep: true }
	)
</script>

<style scoped>
	.ml-2 {
		margin-left: 8px;
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

	.ant-form-item-explain {
		margin-top: 4px;
	}

	.ant-form-item-explain small {
		color: #999;
		font-size: 12px;
	}

	.field-help {
		margin-top: 8px;
		padding: 8px;
		background-color: #f0f9ff;
		border: 1px solid #e1f5fe;
		border-radius: 4px;
	}

	.field-hint {
		margin-top: 4px;
		font-size: 12px;
		color: #666;
		line-height: 1.4;
	}

	.field-table {
		border: 1px solid #e8e8e8;
		border-radius: 6px;
		overflow: hidden;
	}

	.field-table-header {
		display: grid;
		grid-template-columns: 20px 32px 1fr 180px;
		gap: 6px;
		align-items: center;
		padding: 8px 12px;
		font-size: 12px;
		font-weight: 500;
		color: #888;
		background: #fafafa;
		border-bottom: 1px solid #e8e8e8;
	}

	.field-table-header .field-table-prefix {
		text-align: left;
	}

	.field-table-row {
		display: grid;
		grid-template-columns: 20px 32px 1fr 180px;
		gap: 6px;
		align-items: center;
		padding: 6px 12px;
		transition: background-color 0.2s;
	}

	.field-table-row:not(:last-child) {
		border-bottom: 1px solid #f0f0f0;
	}

	.field-table-row:hover {
		background-color: #f6f8fa;
	}

	.field-table-row-active {
		background-color: #f0f5ff;
	}

	.field-table-row-active:hover {
		background-color: #e6f0ff;
	}

	.field-table-check {
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.field-table-name {
		font-size: 13px;
		color: #333;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.field-table-prefix {
		width: 100%;
	}

	.field-table-prefix-disabled {
		color: #d9d9d9;
		font-size: 14px;
		text-align: center;
	}

	.field-table-drag-handle {
		color: #ccc;
		font-size: 14px;
		line-height: 1;
		flex-shrink: 0;
		width: 20px;
		text-align: center;
		cursor: default;
		user-select: none;
	}

	.field-table-drag-handle-active {
		color: #bbb;
		cursor: grab;
	}

	.field-table-drag-handle-active:active {
		cursor: grabbing;
	}

	.field-table-row:hover .field-table-drag-handle-active {
		color: #666;
	}

	.field-table-row-dragging {
		opacity: 0.5;
		background-color: #e6f0ff !important;
	}

	.field-table-row-dragover {
		border-top: 2px solid var(--primary-color);
		background-color: #f0f5ff !important;
	}

	.field-table-empty {
		padding: 24px 0;
		text-align: center;
		color: #999;
		font-size: 13px;
	}

	.field-table-group {
		margin-bottom: 16px;
	}

	.field-table-group:last-child {
		margin-bottom: 0;
	}

	.field-table-group-label {
		font-size: 13px;
		font-weight: 500;
		color: #555;
		margin-bottom: 8px;
		padding-left: 4px;
		border-left: 3px solid var(--primary-color);
		line-height: 1.4;
	}

	.content-preview {
		padding: 12px;
		background-color: #f5f5f5;
		border: 1px solid #d9d9d9;
		border-radius: 4px;
		margin-top: 8px;
	}
</style>
