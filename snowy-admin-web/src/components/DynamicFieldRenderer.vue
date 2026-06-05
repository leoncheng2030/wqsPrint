<template>
	<div class="dynamic-field-renderer" :class="{ 'compact-mode': compact }">
		<!-- 文本输入 -->
		<a-input
			v-if="field.inputType === 'INPUT'"
			:value="modelValue"
			:placeholder="compact ? field.title : '请输入' + field.title"
			:size="compact ? 'small' : 'middle'"
			@update:value="handleValueChange"
		/>

		<!-- 多行文本 -->
		<a-textarea
			v-else-if="field.inputType === 'TEXTAREA'"
			:value="modelValue"
			:placeholder="compact ? field.title : '请输入' + field.title"
			:rows="compact ? 2 : 3"
			:size="compact ? 'small' : 'middle'"
			@update:value="handleValueChange"
		/>

		<!-- 数字输入 -->
		<a-input-number
			v-else-if="field.inputType === 'NUMBER'"
			:value="modelValue"
			:precision="numberPrecision"
			:placeholder="compact ? field.title : '请输入' + field.title"
			:size="compact ? 'small' : 'middle'"
			class="xn-wd"
			@update:value="handleValueChange"
		/>

		<!-- 日期选择 -->
		<a-date-picker
			v-else-if="field.inputType === 'DATE'"
			:value="dateValue"
			:placeholder="compact ? field.title : '请选择' + field.title"
			:format="getDateFormat(field)"
			:value-format="getDateFormat(field)"
			:show-time="isDateTimeFormat(field)"
			:size="compact ? 'small' : 'middle'"
			class="xn-wd"
			@update:value="handleValueChange"
		/>

		<!-- 日期范围选择 -->
		<a-range-picker
			v-else-if="field.inputType === 'DATE_RANGE'"
			:value="dateRangeValue"
			:placeholder="compact ? [field.title + '开始', field.title + '结束'] : ['开始' + field.title, '结束' + field.title]"
			:format="getDateRangeFormat(field)"
			:value-format="getDateRangeFormat(field)"
			:show-time="isDateTimeFormat(field)"
			:size="compact ? 'small' : 'middle'"
			class="xn-wd"
			@update:value="handleValueChange"
		/>

		<!-- 下拉选择 -->
		<a-select
			v-else-if="field.inputType === 'SELECT'"
			:value="modelValue"
			:placeholder="compact ? field.title : '请选择' + field.title"
			:options="options"
			:mode="field.isMultiple === '1' ? 'multiple' : undefined"
			:size="compact ? 'small' : 'middle'"
			style="width: 100%;"
			allow-clear
			@update:value="handleValueChange"
		/>

		<!-- 单选框 -->
		<a-radio-group
			v-else-if="field.inputType === 'RADIO'"
			:value="modelValue"
			:options="options"
			:size="compact ? 'small' : 'middle'"
			@update:value="handleValueChange"
		/>

		<!-- 复选框 -->
		<a-checkbox-group
			v-else-if="field.inputType === 'CHECKBOX'"
			:value="modelValue"
			:options="options"
			:size="compact ? 'small' : 'middle'"
			@update:value="handleValueChange"
		/>

		<!-- 动态条码 -->
		<div v-else-if="field.inputType === 'DYNAMIC_BARCODE'" class="dynamic-barcode-field">
			<a-input
				:value="modelValue"
				placeholder="根据编码规则自动生成"
				:size="compact ? 'small' : 'middle'"
				readonly
				style="background-color: #f5f5f5;"
			/>
		</div>

		<!-- 二维码显示 -->
		<div v-else-if="field.inputType === 'QRCODE'" class="qr-code-display">
			<a-qrcode v-if="modelValue" :value="modelValue" :size="compact ? 80 : 120" />
			<div v-else class="qr-placeholder">
				<span style="color: #999;">{{ compact ? '无二维码内容' : '二维码内容为空' }}</span>
			</div>
		</div>

		<!-- 默认文本输入 -->
		<a-input
			v-else
			:value="modelValue"
			:placeholder="compact ? field.title : '请输入' + field.title"
			:size="compact ? 'small' : 'middle'"
			@update:value="handleValueChange"
		/>
	</div>
</template>

<script setup name="DynamicFieldRenderer">
	import { computed, ref, watch } from 'vue'
	import { message } from 'ant-design-vue'
	import dayjs from 'dayjs'
	import { getDateFormat, getDateRangeFormat, isDateTimeFormat } from '@/utils/dateUtils'

	/**
	 * 动态字段渲染器组件
	 * 根据字段配置动态渲染不同类型的表单控件
	 * 支持文本、数字、日期、选择等多种输入类型
	 * 提供统一的数据绑定和事件处理机制
	 */

	// 组件属性定义
	const props = defineProps({
		// 字段配置对象
		field: {
			type: Object,
			required: true,
			validator: (field) => {
				// 验证必要的字段属性
				return field && field.fieldKey && field.title && field.inputType
			}
		},
		// 字段值
		modelValue: {
			type: [String, Number, Array, Object],
			default: ''
		},
		// 字段选项数据
		options: {
			type: Array,
			default: () => []
		},
		// 紧凑模式（用于表格中显示）
		compact: {
			type: Boolean,
			default: false
		}
	})
	// 事件定义 - 修正为 update:modelValue
	const emit = defineEmits(['update:modelValue'])

	// 处理值变化 - 触发正确的事件名称
	const handleValueChange = (value) => {
		// 对日期范围类型进行特殊处理
		if (props.field.inputType === 'DATE_RANGE' && Array.isArray(value) && value.length === 2) {
			// 将数组格式 ["2025-07-21", "2025-08-30"] 转换为字符串格式 "2025-07-21-2025-08-30"
			const formattedValue = value.join('-')
			// 触发父组件的值更新事件，使用转换后的格式
			emit('update:modelValue', formattedValue)
		} else {
			// 其他类型直接传递原值
			emit('update:modelValue', value)
		}
	}

	/**
	 * 计算属性：处理日期值，转为 dayjs 对象确保日期控件正确显示
	 */
	const dateValue = computed(() => {
		if (props.field.inputType === 'DATE') {
			if (!props.modelValue || props.modelValue === '') {
				// 如果为空，使用当前日期
				return dayjs()
			}
			// 处理 Excel 序列号日期数字（兜底保护）
			if (typeof props.modelValue === 'number' && props.modelValue > 0) {
				const date = dayjs('1899-12-30').add(props.modelValue, 'day')
				if (date.isValid()) return date
			}
			// 将字符串转换为 dayjs 对象，确保日期控件正确回显
			const format = getDateFormat(props.field)
			const parsed = dayjs(props.modelValue, format)
			return parsed.isValid() ? parsed : dayjs(props.modelValue)
		}
		return props.modelValue
	})

	// 日期格式工具函数已统一移至 dateUtils.js 管理

	// 获取数字精度配置
	const numberPrecision = computed(() => {
		if (props.field.inputType !== 'NUMBER' || !props.field.optionsData) return undefined
		try {
			const options = typeof props.field.optionsData === 'string'
				? JSON.parse(props.field.optionsData)
				: props.field.optionsData
			if (options.numberConfig && options.numberConfig.precision !== undefined && options.numberConfig.precision >= 0) {
				return options.numberConfig.precision
			}
		} catch (e) {
			// 解析失败，返回 undefined
		}
		return undefined
	})

	// 计算属性：处理日期范围值的格式转换
	const dateRangeValue = computed(() => {
		// 如果是日期范围类型且值为空，则默认为当前日期到未来一周
		if (props.field.inputType === 'DATE_RANGE' && (!props.modelValue || props.modelValue === '')) {
			return [dayjs(), dayjs().add(7, 'day')]
		}
		
		// 如果是日期范围类型且值为字符串格式
		if (props.field.inputType === 'DATE_RANGE' && typeof props.modelValue === 'string' && props.modelValue) {

			// 获取日期格式配置
			const dateFormat = getDateRangeFormat(props.field)
			const rangeSeparator = props.field.dateConfig?.rangeSeparator || '-'

			// 定义解析日期字符串为 dayjs 对象的函数
			const parseDateString = (dateStr) => {
				try {
					// 尝试使用配置的输入格式解析
					if (props.field.dateConfig?.inputFormats) {
						for (const format of props.field.dateConfig.inputFormats) {
						const parsed = dayjs(dateStr, format, true)
						if (parsed.isValid()) {
							return parsed
						}
						}
					}

					// 尝试使用常见格式解析
					const commonFormats = ['YYYY.MM.DD', 'YYYY-MM-DD', 'YYYY/MM/DD']
					for (const format of commonFormats) {
					const parsed = dayjs(dateStr, format, true)
					if (parsed.isValid()) {
						return parsed
					}
					}

					// 最后尝试自动解析
				const parsed = dayjs(dateStr)
				if (parsed.isValid()) {
					return parsed
				}

				return null
			} catch (error) {
				return null
				}
			}

			let startDateStr = null
			let endDateStr = null

			// 首先尝试使用配置的范围分隔符分割
			if (props.modelValue.includes(rangeSeparator)) {
				const parts = props.modelValue.split(rangeSeparator)
				if (parts.length === 2) {
					startDateStr = parts[0].trim()
					endDateStr = parts[1].trim()
				}
			}

			// 尝试使用常见的范围分隔符分割
			if (!startDateStr && props.modelValue.includes('~')) {
				const parts = props.modelValue.split('~')
				if (parts.length === 2) {
					startDateStr = parts[0].trim()
					endDateStr = parts[1].trim()
				}
			}

			// 处理标准格式 YYYY-MM-DD-YYYY-MM-DD
			if (!startDateStr && props.modelValue.match(/^\d{4}-\d{2}-\d{2}-\d{4}-\d{2}-\d{2}$/)) {
				// 标准格式：按照固定位置分割
				startDateStr = props.modelValue.substring(0, 10) // 前10个字符：YYYY-MM-DD
				endDateStr = props.modelValue.substring(11)      // 从第11个字符开始：YYYY-MM-DD
			}

			// 处理其他格式：根据日期格式长度智能分割
			if (!startDateStr && props.modelValue.includes('-')) {
				const formatLength = dateFormat.length
				if (props.modelValue.length >= formatLength * 2 + 1) {
					startDateStr = props.modelValue.substring(0, formatLength)
					endDateStr = props.modelValue.substring(formatLength + 1)
				} else {
					// 如果上述方法都失败，尝试简单的分割（向后兼容）
					const parts = props.modelValue.split('-')
					if (parts.length >= 6) {
						// 重新组合日期：前3部分为开始日期，后3部分为结束日期
						startDateStr = parts.slice(0, 3).join('-')
						endDateStr = parts.slice(3, 6).join('-')
					}
				}
			}

			// 如果成功分割，将字符串转换为 dayjs 对象
			if (startDateStr && endDateStr) {
				const startDate = parseDateString(startDateStr)
				const endDate = parseDateString(endDateStr)

				if (startDate && endDate) {
					return [startDate, endDate]
				} else {
					return [startDateStr, endDateStr]
				}
			}
		}

		// 如果已经是数组格式或其他情况，直接返回
		return props.modelValue
	})
</script>

<style scoped lang="less">
	.dynamic-field-renderer {
		// 组件样式
		.xn-wd {
			width: 100%;
		}

		// 紧凑模式样式
		&.compact-mode {
			.ant-input,
			.ant-input-number,
			.ant-select,
			.ant-date-picker,
			.ant-range-picker {
				border-radius: 4px;
			}

			.ant-radio-group,
			.ant-checkbox-group {
				font-size: 12px;

				.ant-radio,
				.ant-checkbox {
					margin-right: 8px;
				}
			}
		}
	}
</style>