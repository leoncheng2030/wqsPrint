<template>
	<!-- 字段详细配置弹窗组件 -->
	<a-modal v-model:open="modalVisible" title="字段配置" width="800px" :confirm-loading="loading" @ok="handleSave"
		@cancel="handleCancel">
		<a-form :model="formData" :rules="formRules" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }" ref="formRef"
			layout="horizontal">
			<!-- 基本信息 -->
			<div class="section">
				<div class="section-device"></div>
				<div class="section-title">
					基础信息
				</div>
			</div>

			<a-form-item label="字段名称">
				<a-input v-model:value="formData.title" disabled />
			</a-form-item>

			<a-form-item label="字段标识">
				<a-input v-model:value="formData.fieldKey" disabled />
			</a-form-item>

			<a-form-item label="控件类型">
				<a-tag :color="getInputTypeColor(formData.inputType)">
					{{ getInputTypeText(formData.inputType) }}
				</a-tag>
			</a-form-item>

			<!-- 通用配置 -->
			<div class="section">
				<div class="section-device"></div>
				<div class="section-title">
					通用配置
				</div>
			</div>

			<a-form-item label="占位符">
				<a-input v-model:value="formData.placeholder" placeholder="请输入占位符文本" allow-clear />
			</a-form-item>

			<a-form-item label="字段状态">
				<a-select v-model:value="formData.status" placeholder="请选择字段状态">
					<a-select-option value="ENABLE">启用</a-select-option>
					<a-select-option value="DISABLE">禁用</a-select-option>
				</a-select>
				<div class="field-hint">禁用的字段不会在打印记录表单中显示</div>
			</a-form-item>

			<!-- 默认值设置 -->
			<a-form-item label="默认值">
				<!-- 选择类控件的默认值设置 -->
				<template v-if="['SELECT', 'RADIO', 'CHECKBOX'].includes(formData.inputType)">
					<!-- 单选类型（SELECT单选、RADIO） -->
					<a-select v-if="['SELECT', 'RADIO'].includes(formData.inputType)"
						v-model:value="formData.defaultValue" :options="getFieldOptions(formData)"
						:placeholder="'请选择默认值'" allow-clear :show-search="true" :filter-option="filterOption">
					</a-select>

					<!-- 多选类型（SELECT多选、CHECKBOX） -->
					<a-select
						v-else-if="formData.inputType === 'CHECKBOX' || (formData.inputType === 'SELECT' && formData.isMultiple === '1')"
						v-model:value="formData.defaultValue" :options="getFieldOptions(formData)" mode="multiple"
						:placeholder="'请选择默认值'" allow-clear :show-search="true" :filter-option="filterOption">
					</a-select>
				</template>

				<!-- 日期类控件的默认值设置 -->
				<template v-else-if="['DATE'].includes(formData.inputType)">
					<a-date-picker v-model:value="formData.defaultValue" :format="getDateFormat(formData)"
						:placeholder="'请选择默认日期'" style="width: 100%" />
				</template>

				<template v-else-if="['DATE_RANGE'].includes(formData.inputType)">
					<a-range-picker v-model:value="formData.defaultValue" :format="getDateFormat(formData)"
						:placeholder="['开始日期', '结束日期']" style="width: 100%" />
				</template>

				<!-- 其他类型控件使用文本输入 -->
				<a-input v-else v-model:value="formData.defaultValue" :placeholder="'请输入默认值'" allow-clear />
			</a-form-item>

			<!-- 时间控件配置 -->
			<template v-if="['DATE', 'DATE_RANGE'].includes(formData.inputType)">
				<div class="section">
					<div class="section-device"></div>
					<div class="section-title">
						时间配置
					</div>
				</div>

				<!-- 使用新的日期配置面板 -->
				<DateConfigPanel v-model="formData.dateConfig" :is-date-range="formData.inputType === 'DATE_RANGE'"
					@change="onDateConfigChange" />

				<!-- 保持向后兼容的简单格式选择 -->
				<a-form-item label="兼容格式" name="dateFormat">
					<a-select v-model:value="formData.dateFormat" placeholder="选择兼容格式（可选）" :options="dateFormatOptions"
						allow-clear>
						<template #suffixIcon>
							<InfoCircleOutlined style="color: #999" />
						</template>
					</a-select>
					<div class="ant-form-item-explain">
						<small>此选项用于向后兼容，建议使用上方的高级配置</small>
					</div>
				</a-form-item>
			</template>

			<!-- 下拉选择/复选框/单选框配置 -->
			<template v-if="['SELECT', 'RADIO', 'CHECKBOX'].includes(formData.inputType)">
				<div class="section">
					<div class="section-device"></div>
					<div class="section-title">
						选项配置
					</div>
				</div>

				<a-form-item label="数据来源" name="dataSource">
					<a-select v-model:value="formData.dataSource" placeholder="请选择数据来源" :options="dataSourceOptions"
						@change="onDataSourceChange" />
					<div class="ant-form-item-explain">
						<small>选择选项数据的来源方式</small>
					</div>
				</a-form-item>

				<!-- 字典数据源配置 -->
				<template v-if="formData.dataSource === 'dict'">
					<a-form-item label="字典编码" name="dictTypeCode">
						<a-select v-model:value="formData.dictTypeCode" placeholder="请选择字典类型" allow-clear show-search
							:filter-option="filterDictTypeOption" :options="dictTypeOptions" />
						<div class="ant-form-item-explain">
							<small>从系统字典中选择字典类型获取选项数据</small>
						</div>
					</a-form-item>
				</template>

				<!-- API数据源配置 -->
				<template v-if="formData.dataSource === 'api'">
					<a-form-item label="选项API地址" name="optionApiUrl">
						<a-input v-model:value="formData.optionApiUrl" placeholder="请输入获取选项数据的API地址" allow-clear />
						<div class="ant-form-item-explain">
							<small>用于动态获取选项数据的接口地址</small>
						</div>
					</a-form-item>

					<a-form-item label="已选数据API" name="selectedDataApiUrl">
						<a-input v-model:value="formData.selectedDataApiUrl" placeholder="请输入获取已选数据的API地址"
							allow-clear />
						<div class="ant-form-item-explain">
							<small>用于回显已选择数据的接口地址</small>
						</div>
					</a-form-item>
				</template>

				<!-- 静态数据源配置 -->
				<template v-if="formData.dataSource === 'static'">
					<a-form-item label="静态选项" name="staticOptions">
						<StaticOptionsEditor v-model="formData.staticOptions" :show-json-preview="false"
							:min-options="1" @change="handleStaticOptionsChange" />
						<div class="ant-form-item-explain">
							<small>配置静态选项的值和显示文本</small>
						</div>
					</a-form-item>
				</template>

				<!-- 多选支持（仅SELECT和CHECKBOX） -->
				<a-form-item v-if="['SELECT', 'CHECKBOX'].includes(formData.inputType)" label="多选支持">
					<a-switch v-model:checked="formData.isMultiple" checked-value="1" un-checked-value="0" />
					<span class="ml-2">{{ formData.isMultiple === '1' ? '支持多选' : '单选模式' }}</span>
				</a-form-item>
			</template>

			<!-- 二维码配置 -->
			<template v-if="formData.inputType === 'QRCODE'">
				<div class="section">
					<div class="section-device"></div>
					<div class="section-title">
						二维码配置
					</div>
				</div>

				<a-form-item label="二维码格式" name="formatType">
					<a-select v-model:value="formData.qrCodeConfig.formatType" placeholder="请选择二维码格式类型"
						@change="onFormatTypeChange">
						<a-select-option value="json">JSON格式</a-select-option>
						<a-select-option value="custom">自定义分隔符</a-select-option>
					</a-select>
					<div class="ant-form-item-explain">
						<small>选择二维码内容的格式类型</small>
					</div>
				</a-form-item>

				<a-form-item v-if="formData.qrCodeConfig.formatType === 'json'" label="JSON数据类型" name="jsonDataType">
					<a-select v-model:value="formData.qrCodeConfig.jsonDataType" placeholder="请选择JSON数据类型"
						@change="updateQrContent">
						<a-select-option value="object">对象类型</a-select-option>
						<a-select-option value="array">数组类型</a-select-option>
					</a-select>
					<div class="ant-form-item-explain">
						<small>对象类型：{"字段名":"字段值"}，数组类型：["字段값1","字段값2"]</small>
					</div>
				</a-form-item>

				<a-form-item v-if="formData.qrCodeConfig.formatType === 'custom'" label="分隔符配置" name="separator">
					<a-input v-model:value="formData.qrCodeConfig.separator" placeholder="请输入分隔符，如：| 或 , 或 ; 等"
						@change="updateQrContent" allow-clear />
					<div class="ant-form-item-explain">
						<small>用于分隔不同字段值的字符</small>
					</div>
				</a-form-item>

				<!-- 明细字段值分隔符配置 -->
				<a-form-item label="明细值分隔符" name="itemValueSeparator">
					<a-input v-model:value="formData.qrCodeConfig.itemValueSeparator" placeholder="请输入明细字段值分隔符，如：, 或 ; 或 | 等"
						@change="updateQrContent" allow-clear />
					<div class="ant-form-item-explain">
						<small>用于分隔明细字段中多个值的字符，默认为逗号(,)</small>
					</div>
				</a-form-item>

				<!-- 明细行分隔符配置 -->
				<a-form-item label="明细行分隔符" name="detailRowSeparator">
					<a-input v-model:value="formData.qrCodeConfig.detailRowSeparator" placeholder="请输入明细行分隔符，如：# 或 @ 或 $ 等"
						@change="updateQrContent" allow-clear />
					<div class="ant-form-item-explain">
						<small>用于分隔不同明细行的字符，默认为井号(#)</small>
					</div>
				</a-form-item>

				<!-- 结束标识符配置 -->
				<a-form-item label="结束标识符" name="endMarker">
					<a-input v-model:value="formData.qrCodeConfig.endMarker" placeholder="请输入结束标识符，如：END 或 ## 或 EOF 等"
						@change="updateQrContent" allow-clear />
					<div class="ant-form-item-explain">
						<small>添加在二维码内容最后的结束标识符，可选配置</small>
					</div>
				</a-form-item>

				<a-form-item label="选择字段" name="selectedFields">
					<a-select v-model:value="formData.qrCodeConfig.selectedFields" mode="multiple"
						placeholder="请选择要包含在二维码中的字段" :options="availableFieldOptions" @change="updateQrContent" />
					<div class="ant-form-item-explain">
						<small>选择的字段将按照所选格式类型生成二维码内容</small>
					</div>
				</a-form-item>

				<!-- 明细字段循环模式配置 -->
				<a-form-item v-if="hasDetailFields" label="循环模式" name="detailLoopMode">
					<a-select v-model:value="formData.qrCodeConfig.detailLoopMode" placeholder="请选择明细字段的循环处理模式"
						@change="updateQrContent">
						<a-select-option value="join">连接模式（所有明细值用分隔符连接）</a-select-option>
						<a-select-option value="loop">循环模式（为每个明细项生成完整内容）</a-select-option>
						<a-select-option value="first">首项模式（只使用第一个明细项）</a-select-option>
					</a-select>
					<div class="ant-form-item-explain">
						<small>当二维码包含明细字段时的处理方式</small>
					</div>
				</a-form-item>

				<a-form-item v-if="formData.qrCodeConfig.previewContent" label="内容预览">
					<div class="content-preview">
						<a-typography-paragraph code copyable>
							{{ formData.qrCodeConfig.previewContent }}
						</a-typography-paragraph>
					</div>
				</a-form-item>
			</template>

			<!-- 动态条码配置 -->
			<template v-if="formData.inputType === 'DYNAMIC_BARCODE'">
				<div class="section">
					<div class="section-device"></div>
					<div class="section-title">
						动态条码配置
					</div>
				</div>
				<a-form-item label="编码规则" name="codeRule">
					<a-select v-model:value="formData.dynamicBarcodeConfig.codeRule" placeholder="请选择编码规则"
						:options="codeRuleOptions" @change="updateDynamicBarcodeContent" show-search
						:filter-option="filterCodeRuleOption" />
					<div class="ant-form-item-explain">
						<small>选择预定义的编码规则来生成动态条码内容</small>
					</div>
				</a-form-item>

				<a-form-item label="关联字段">
					<a-select v-model:value="formData.dynamicBarcodeConfig.selectedFields" mode="multiple"
						placeholder="请选择要包含在条码中的字段" :options="availableFieldOptions"
						@change="updateDynamicBarcodeContent" show-search allow-clear />
					<div class="ant-form-item-explain">
						<small>选择的字段将用于替换编码规则中的字段占位符</small>
					</div>
				</a-form-item>

				<a-form-item v-if="formData.dynamicBarcodeConfig.previewContent" label="内容预览">
					<div class="content-preview">
						<a-typography-paragraph code copyable>
							{{ formData.dynamicBarcodeConfig.previewContent }}
						</a-typography-paragraph>
					</div>
				</a-form-item>
			</template>

			<!-- 数字输入配置 -->
			<template v-if="formData.inputType === 'NUMBER'">
				<div class="section">
					<div class="section-device"></div>
					<div class="section-title">
						数字配置
					</div>
				</div>
				<a-form-item label="最小值">
					<a-input-number v-model:value="formData.numberConfig.min" placeholder="最小值" style="width: 100%" />
				</a-form-item>

				<a-form-item label="最大值">
					<a-input-number v-model:value="formData.numberConfig.max" placeholder="最大值" style="width: 100%" />
				</a-form-item>

				<a-form-item label="步长">
					<a-input-number v-model:value="formData.numberConfig.step" :min="0.01" placeholder="步长"
						style="width: 100%" />
				</a-form-item>

				<a-form-item label="小数位数">
					<a-input-number v-model:value="formData.numberConfig.precision" :min="0" :max="10"
						placeholder="小数位数" style="width: 100%" />
				</a-form-item>
			</template>

			<!-- 文本输入配置 -->
			<template v-if="formData.inputType === 'INPUT'">
				<div class="section">
					<div class="section-device"></div>
					<div class="section-title">
						文本输入配置
					</div>
				</div>

				<a-form-item label="启用补零">
					<a-switch v-model:checked="formData.inputConfig.enablePadding" />
					<span class="ml-2">{{ formData.inputConfig.enablePadding ? '启用' : '禁用' }}补零功能</span>
				</a-form-item>

				<template v-if="formData.inputConfig.enablePadding">
					<a-form-item label="补零方向">
						<a-select v-model:value="formData.inputConfig.paddingDirection" placeholder="请选择补零方向" style="width: 100%">
							<a-select-option value="left">前补零</a-select-option>
							<a-select-option value="right">后补零</a-select-option>
						</a-select>
						<div class="ant-form-item-explain">
							<small>选择在字符串前面还是后面补零</small>
						</div>
					</a-form-item>

					<a-form-item label="字符总长度">
						<a-input-number v-model:value="formData.inputConfig.totalLength" :min="1" :max="50" placeholder="字符总长度"
							style="width: 100%" />
						<div class="ant-form-item-explain">
							<small>设置补零后的字符串总长度</small>
						</div>
					</a-form-item>

					<a-form-item label="补零字符">
						<a-input v-model:value="formData.inputConfig.paddingChar" placeholder="补零字符（默认为0）" maxlength="1"
							style="width: 100%" />
						<div class="ant-form-item-explain">
							<small>用于补位的字符，默认为数字0</small>
						</div>
					</a-form-item>

					<a-form-item v-if="formData.inputConfig.totalLength && formData.inputConfig.paddingDirection" label="效果预览">
						<div class="content-preview">
							<a-typography-paragraph code>
								{{ getPaddingPreview() }}
							</a-typography-paragraph>
						</div>
					</a-form-item>
				</template>
			</template>

			<!-- 文本域配置 -->
			<template v-if="formData.inputType === 'TEXTAREA'">
				<div class="section">
					<div class="section-device"></div>
					<div class="section-title">
						文本域配置
					</div>
				</div>
				<a-form-item label="行数">
					<a-input-number v-model:value="formData.textareaConfig.rows" :min="2" :max="20" placeholder="文本域行数"
						style="width: 100%" />
				</a-form-item>

				<a-form-item label="最大长度">
					<a-input-number v-model:value="formData.textareaConfig.maxLength" :min="1" placeholder="最大字符长度"
						style="width: 100%" />
				</a-form-item>

				<a-form-item label="显示字符计数">
					<a-switch v-model:checked="formData.textareaConfig.showCount" />
					<span class="ml-2">{{ formData.textareaConfig.showCount ? '显示' : '隐藏' }}字符计数</span>
				</a-form-item>
			</template>
		</a-form>
	</a-modal>
</template>

<script setup name="FieldDetailConfigModal">
import { ref, computed, watch } from 'vue'
import { Layout, message } from 'ant-design-vue'
import { InfoCircleOutlined } from '@ant-design/icons-vue'
import fieldApi from '@/api/label/fieldApi'
import wqsCodeRuleApi from '@/api/barcode/wqsCodeRuleApi'
import DateConfigPanel from './DateConfigPanel.vue'
import StaticOptionsEditor from './StaticOptionsEditor.vue'
import DynamicFieldRenderer from '@/components/DynamicFieldRenderer.vue'
import dictApi from '@/api/dev/dictApi'
import tool from '@/utils/tool'
import { inputTypeOptions, getInputTypeText, getInputTypeColor } from '@/config/inputTypes'
import { useFieldConfig } from '@/composables/useFieldConfig'
import dayjs from 'dayjs'

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
	// 计算属性
	getInputTypeText: getFieldInputTypeText,
	getInputTypeColor: getFieldInputTypeColor,
	// 方法
	loadDictTypes,
	loadCodeRules,
	loadAvailableFields,
	validateFieldConfig,
	processFieldOptions,
	formatFieldValue
} = useFieldConfig()

// 响应式数据
const loading = ref(false)
const formRef = ref()
const formData = ref({})

// 字典类型选项（用于字典数据源选择）
const dictTypeOptions = computed(() => {
	return dictTypes.value.map(dict => ({
		label: dict.dictTypeName,
		value: dict.dictTypeCode
	}))
})

// 检查是否包含明细字段
const hasDetailFields = computed(() => {
	if (!formData.value.qrCodeConfig?.selectedFields) return false
	// 检查选中的字段中是否包含明细字段
	// 这里需要根据实际的字段类型判断，暂时返回true以便测试
	return formData.value.qrCodeConfig.selectedFields.length > 0
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

// 二维码格式类型变化处理
const onFormatTypeChange = (value) => {
	if (value === 'json') {
		formData.value.qrCodeConfig.separator = ''
	} else if (value === 'custom') {
		formData.value.qrCodeConfig.separator = '|'
	}
	updateQrContent()
}

// 更新二维码内容预览
const updateQrContent = () => {
	const config = formData.value.qrCodeConfig

	// 检查是否有选中的字段
	if (!config.selectedFields || config.selectedFields.length === 0) {
		config.previewContent = ''
		return
	}

	if (config.formatType === 'json') {
		// JSON格式 - 根据数据类型生成不同格式
		if (config.jsonDataType === 'array') {
			// 数组类型：[{"字段名1":"字段值1", "字段名2":"字段值2"}]
			const jsonObj = {}
			config.selectedFields.forEach((fieldKey) => {
				jsonObj[fieldKey] = fieldKey
			})
			const jsonArray = [jsonObj]
			config.previewContent = JSON.stringify(jsonArray, null, 2)
		} else {
			// 对象类型：{"字段名": "字段值"}
			const jsonObj = {}
			config.selectedFields.forEach((fieldKey) => {
				jsonObj[fieldKey] = fieldKey
			})
			config.previewContent = JSON.stringify(jsonObj, null, 2)
		}
	} else if (config.formatType === 'custom') {
		// 自定义分隔符格式 - 直接使用fieldKey拼接
		const separator = config.separator || '|'
		config.previewContent = config.selectedFields.join(separator)
	}

	// 在预览内容最后添加结束标识符（如果配置了的话）
	if (config.endMarker && config.endMarker.trim() !== '') {
		if (config.previewContent && config.previewContent.trim() !== '') {
			config.previewContent = config.previewContent + config.endMarker
		} else {
			config.previewContent = config.endMarker
		}
	}
}

// 更新动态条码内容预览
const updateDynamicBarcodeContent = () => {
	const config = formData.value.dynamicBarcodeConfig

	if (!config.codeRule) {
		config.previewContent = ''
		return
	}

	// 根据选中的编码规则ID查找对应的规则信息
	const selectedRule = codeRuleOptions.value.find((rule) => rule.value === config.codeRule)

	if (selectedRule) {
		// 解析编码规则的片段配置，生成动态条码内容格式
		let previewContent = ''

		try {
			// 解析片段配置（如果是字符串则解析为JSON）
			let segments = selectedRule.segments

			if (typeof segments === 'string') {
				segments = JSON.parse(segments)
			}

			if (Array.isArray(segments) && segments.length > 0) {
				// 根据片段配置生成预览内容
				const contentParts = []

				segments.forEach((segment, index) => {
					switch (segment.type) {
						case 'fixed':
							// 固定值片段
							contentParts.push(segment.value || '')
							break
						case 'field':
							// 字段片段 - 检查是否在selectedFields中
							const fieldName = segment.fieldName || 'field'
							if (config.selectedFields && config.selectedFields.includes(fieldName)) {
								// 如果字段被选中，显示字段名
								contentParts.push(`{${fieldName}}`)
							} else {
								// 如果字段未被选中，显示占位符
								contentParts.push(`{${fieldName}}`)
							}
							break
						case 'date':
							// 日期片段 - 显示日期格式预览
							const dateFormat = segment.format || 'YYYY-MM-DD'
							const now = new Date()
							try {
								// 使用dayjs格式化当前日期作为预览
								const formattedDate = dayjs(now).format(dateFormat)
								contentParts.push(formattedDate)
							} catch (error) {
								// 如果格式化失败，显示格式字符串
								contentParts.push(`{日期:${dateFormat}}`)
							}
							break
						case 'serial':
							// 序列号片段 - 显示序列号格式
							const length = segment.length || 3
							const startValue = segment.startValue || 1
							const serialFormat = String(startValue).padStart(length, '0')
							contentParts.push(`{序列号:${serialFormat}}`)
							break
						case 'separator':
							// 分隔符片段
							contentParts.push(segment.separator || '')
							break
						default:
							// 未知类型，显示原始值
							contentParts.push(segment.value || '')
					}
				})

				previewContent = contentParts.join('')
			} else {
				previewContent = '编码规则片段配置为空'
			}
		} catch (error) {
			previewContent = '编码规则片段解析失败'
		}

		// 设置预览内容
		config.previewContent = previewContent
	} else {
		config.previewContent = '未找到对应的编码规则'
	}
}

// 计算属性：弹窗显示状态
const modalVisible = computed({
	get: () => props.open,
	set: (value) => emit('update:open', value)
})

// 时间格式选项
const dateFormatOptions = [
	{ label: '日期 (YYYY.MM.DD)', value: 'YYYY.MM.DD' },
	{ label: '日期 (YYYY-MM-DD)', value: 'YYYY-MM-DD' },
	{ label: '日期时间 (YYYY-MM-DD HH:mm:ss)', value: 'YYYY-MM-DD HH:mm:ss' },
	{ label: '日期时间 (YYYY-MM-DD HH:mm)', value: 'YYYY-MM-DD HH:mm' },
	{ label: '年月 (YYYY-MM)', value: 'YYYY-MM' },
	{ label: '时间 (HH:mm:ss)', value: 'HH:mm:ss' },
	{ label: '时间 (HH:mm)', value: 'HH:mm' },
	{ label: '日期范围 (YYYY-MM-DD ~ YYYY-MM-DD)', value: 'YYYY-MM-DD~YYYY-MM-DD' },
	{
		label: '日期时间范围 (YYYY-MM-DD HH:mm:ss ~ YYYY-MM-DD HH:mm:ss)',
		value: 'YYYY-MM-DD HH:mm:ss~YYYY-MM-DD HH:mm:ss'
	}
]

// 数据来源选项
const dataSourceOptions = [
	{ label: '字典数据', value: 'dict' },
	{ label: 'API接口', value: 'api' },
	{ label: '静态数据', value: 'static' }
]

// 数据来源变化处理
const onDataSourceChange = (value) => {
	// 清空其他数据源的配置
	if (value !== 'dict') {
		formData.value.dictTypeCode = ''
	}
	if (value !== 'api') {
		formData.value.optionApiUrl = ''
		formData.value.selectedDataApiUrl = ''
	}
	if (value !== 'static') {
		formData.value.staticOptions = ''
	}
}

// 日期配置变化处理
const onDateConfigChange = (config) => {
	// 更新日期配置
	formData.value.dateConfig = { ...config }

	// 如果有生成的显示格式，同步到兼容格式字段
	if (config.displayFormat) {
		formData.value.dateFormat = config.displayFormat
	}

	// 日期配置已更新
}

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
				itemValueSeparator: ',',  // 添加默认的明细字段值分隔符
				detailRowSeparator: '#',  // 添加默认的明细行分隔符
				endMarker: '',  // 添加默认的结束标识符
				previewContent: '',
				useCodeRule: false,
				codeRule: ''
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
		if (typeof defaultValue === 'string' &&
			defaultValue.includes(',') &&
			(data.inputType === 'CHECKBOX' || (data.inputType === 'SELECT' && data.isMultiple === '1'))) {
			defaultValue = defaultValue.split(',').map(item => item.trim())
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
			endMarker: '',
			previewContent: '',
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

	// 如果是二维码控件，更新内容预览
	if (data.inputType === 'QRCODE') {
		updateQrContent()
	}
	// 如果是动态条码控件，更新内容预览
	if (data.inputType === 'DYNAMIC_BARCODE') {
		updateDynamicBarcodeContent()
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
				endMarker: formData.value.qrCodeConfig.endMarker,
				previewContent: formData.value.qrCodeConfig.previewContent
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

		// 编码规则加载完成后，如果当前是动态条码类型，重新更新动态条码内容
		if (formData.value.inputType === 'DYNAMIC_BARCODE' && formData.value.dynamicBarcodeConfig.codeRule) {
			// 编码规则加载完成，重新更新动态条码内容
			updateDynamicBarcodeContent()
		}
	} catch (error) {
		// 获取编码规则列表失败
		message.error('获取编码规则列表失败')
	}
}

// 编码规则下拉框过滤函数
const filterCodeRuleOption = (input, option) => {
	return option.label.toLowerCase().includes(input.toLowerCase())
}

// 字典类型下拉框过滤函数
const filterDictTypeOption = (input, option) => {
	return option.label.toLowerCase().includes(input.toLowerCase())
}

// 获取补零效果预览
const getPaddingPreview = () => {
	const config = formData.value.inputConfig
	if (!config.enablePadding || !config.totalLength || !config.paddingDirection) {
		return '请完善补零配置'
	}

	const sampleText = '123'
	const paddingChar = config.paddingChar || '0'
	const totalLength = config.totalLength

	if (sampleText.length >= totalLength) {
		return `示例: "${sampleText}" (无需补零，已达到或超过目标长度)`
	}

	let result = sampleText
	const paddingCount = totalLength - sampleText.length
	const padding = paddingChar.repeat(paddingCount)

	if (config.paddingDirection === 'left') {
		result = padding + sampleText
	} else {
		result = sampleText + padding
	}

	return `示例: "${sampleText}" → "${result}"`
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

			// 如果是编辑现有字段，加载字段选项以供默认值选择使用
			if (props.fieldData && props.fieldData.id) {
				loadFieldOptionsForDefaultValue()
			}
		}
	}
)

// 为默认值选择加载字段选项
const loadFieldOptionsForDefaultValue = async () => {
	try {
		// 只处理选择类控件
		if (!['SELECT', 'RADIO', 'CHECKBOX'].includes(formData.value.inputType)) {
			return
		}

		const field = formData.value

		// 根据数据源加载选项
		if (field.dataSource === 'dict' && field.dictTypeCode) {
			// 从字典获取选项
			const options = tool.dictList(field.dictTypeCode)
			if (options && options.length > 0) {
				fieldOptionsCache.value[field.fieldKey] = options
			}
		} else if (field.dataSource === 'static' && field.staticOptions) {
			// 从静态选项获取
			try {
				const staticOptions = typeof field.staticOptions === 'string'
					? JSON.parse(field.staticOptions)
					: field.staticOptions

				let options = []
				if (Array.isArray(staticOptions)) {
					options = staticOptions
				} else if (typeof staticOptions === 'object' && staticOptions !== null) {
					options = Object.entries(staticOptions).map(([value, label]) => ({
						label: String(label),
						value: String(value)
					}))
				}

				if (options.length > 0) {
					fieldOptionsCache.value[field.fieldKey] = options
				}
			} catch (e) {
				// 解析静态选项失败
			}
		}
	} catch (error) {
		// 加载字段选项失败
	}
}

// 监听数据源变化，更新字段选项缓存
watch(
	() => [formData.value.dataSource, formData.value.dictTypeCode, formData.value.staticOptions],
	() => {
		if (props.open) {
			loadFieldOptionsForDefaultValue()
		}
	},
	{ deep: true }
)

// 处理静态选项变化
const handleStaticOptionsChange = (options) => {
	formData.value.staticOptions = options
	// 静态选项已更新
}

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

// 获取字段选项数据
const getFieldOptions = (field) => {
	// 对于选择类控件，需要返回选项数据
	if (['SELECT', 'RADIO', 'CHECKBOX'].includes(field.inputType)) {
		// 根据数据源类型返回相应的选项数据
		if (field.dataSource === 'dict' && field.dictTypeCode) {
			// 字典数据源
			// 这里应该返回实际的字典项，而不是字典类型
			// 由于在模态框中可能没有加载具体的字典项，我们使用缓存的选项
			const cachedOptions = fieldOptionsCache.value[field.fieldKey]
			if (cachedOptions && cachedOptions.length > 0) {
				return cachedOptions
			}
			// 如果没有缓存，则返回空数组
			return []
		} else if (field.dataSource === 'static' && field.staticOptions) {
			// 静态数据源
			try {
				const staticOptions = typeof field.staticOptions === 'string'
					? JSON.parse(field.staticOptions)
					: field.staticOptions

				// 如果是数组格式，直接返回
				if (Array.isArray(staticOptions)) {
					return staticOptions
				}
				// 如果是键值对对象，转换为选项数组
				else if (typeof staticOptions === 'object' && staticOptions !== null) {
					return Object.entries(staticOptions).map(([value, label]) => ({
						label: String(label),
						value: String(value)
					}))
				}
			} catch (e) {
				// 解析静态选项失败
				return []
			}
		} else if (field.dataSource === 'api' && field.optionApiUrl) {
			// API数据源 - 这里暂时返回空数组，因为实际选项需要通过API获取
			return []
		}
	}
	// 其他类型控件返回空数组
	return []
}

// 选项过滤函数
const filterOption = (input, option) => {
	return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
}

// 获取日期格式
const getDateFormat = (field) => {
	if (field.dateConfig && field.dateConfig.displayFormat) {
		return field.dateConfig.displayFormat
	} else if (field.dateFormat) {
		return field.dateFormat
	} else {
		return 'YYYY-MM-DD'
	}
}

// 字段选项缓存
const fieldOptionsCache = ref({})

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

.content-preview {
	padding: 12px;
	background-color: #f5f5f5;
	border: 1px solid #d9d9d9;
	border-radius: 4px;
	margin-top: 8px;
}
</style>
