<template>
	<div class="date-config-panel">
		<!-- 快速预设选择 -->
		<a-form-item label="快速预设">
			<a-select 
				v-model:value="selectedPreset" 
				@change="onPresetChange"
				placeholder="选择预设格式"
				allowClear
			>
				<a-select-option 
					v-for="preset in datePresets" 
					:key="preset.key" 
					:value="preset.key"
				>
					{{ preset.label }}
				</a-select-option>
			</a-select>
		</a-form-item>

		<!-- 自定义配置 -->
		<a-divider>自定义配置</a-divider>

		<!-- 年份配置 -->
		<a-form-item label="年份显示">
			<a-radio-group v-model:value="config.precision.year" @change="updateFormat">
				<a-radio :value="2">两位 (25)</a-radio>
				<a-radio :value="4">四位 (2025)</a-radio>
			</a-radio-group>
		</a-form-item>

		<!-- 月份配置 -->
		<a-form-item label="月份显示">
			<a-radio-group v-model:value="config.precision.month" @change="updateFormat">
				<a-radio :value="1">不补零 (1, 2, 12)</a-radio>
				<a-radio :value="2">补零 (01, 02, 12)</a-radio>
			</a-radio-group>
		</a-form-item>

		<!-- 日期配置 -->
		<a-form-item label="日期显示">
			<a-radio-group v-model:value="config.precision.day" @change="updateFormat">
				<a-radio :value="1">不补零 (1, 2, 31)</a-radio>
				<a-radio :value="2">补零 (01, 02, 31)</a-radio>
			</a-radio-group>
		</a-form-item>

		<!-- 分隔符配置 -->
		<a-form-item label="日期分隔符">
			<a-select v-model:value="config.separators.dateSeparator" @change="updateFormat">
				<a-select-option value="-">横线 (-)</a-select-option>
				<a-select-option value=".">点号 (.)</a-select-option>
				<a-select-option value="/">斜线 (/)</a-select-option>
				<a-select-option value="">无分隔符</a-select-option>
			</a-select>
		</a-form-item>

		<!-- 日期范围分隔符配置 (仅日期范围字段显示) -->
		<a-form-item v-if="isDateRange" label="范围分隔符">
			<a-input 
				v-model:value="config.separators.rangeSeparator" 
				@change="updateFormat"
				placeholder="如: - 或 ~ 或 至"
			/>
		</a-form-item>

		<!-- 显示选项 -->
		<a-form-item label="显示选项">
			<a-space direction="vertical" style="width: 100%">
				<a-checkbox 
					v-model:checked="config.display.showWeekday" 
					@change="updateFormat"
				>
					显示星期
				</a-checkbox>
				<div v-if="config.display.showWeekday" style="margin-left: 24px;">
					<a-radio-group 
						v-model:value="config.display.weekdayFormat" 
						@change="updateFormat"
						size="small"
					>
						<a-radio value="short">周一</a-radio>
						<a-radio value="long">星期一</a-radio>
						<a-radio value="narrow">一</a-radio>
					</a-radio-group>
				</div>
			</a-space>
		</a-form-item>

		<!-- 验证配置 -->
		<a-form-item label="日期验证">
			<a-space direction="vertical" style="width: 100%">
				<a-checkbox 
					v-model:checked="config.validation.allowPast" 
					@change="updateConfig"
				>
					允许选择过去日期
				</a-checkbox>
				<a-checkbox 
					v-model:checked="config.validation.allowFuture" 
					@change="updateConfig"
				>
					允许选择未来日期
				</a-checkbox>
			</a-space>
		</a-form-item>

		<!-- 格式预览 -->
		<a-form-item label="格式预览">
			<div class="format-preview">
				<div class="preview-item">
					<span class="preview-label">格式字符串:</span>
					<code class="preview-code">{{ generatedFormat }}</code>
				</div>
				<div class="preview-item">
					<span class="preview-label">显示效果:</span>
					<span class="preview-text">{{ previewText }}</span>
				</div>
				<div v-if="isDateRange" class="preview-item">
					<span class="preview-label">范围效果:</span>
					<span class="preview-text">{{ rangePreviewText }}</span>
				</div>
			</div>
		</a-form-item>
	</div>
</template>

<script setup>
	import { ref, computed, watch, onMounted } from 'vue'
	import dayjs from 'dayjs'
	import 'dayjs/locale/zh-cn'
	import weekday from 'dayjs/plugin/weekday'
	import localeData from 'dayjs/plugin/localeData'
	import { usePerformanceOptimization } from '@/composables/usePerformanceOptimization'

	// 扩展dayjs插件
	dayjs.extend(weekday)
	dayjs.extend(localeData)
	dayjs.locale('zh-cn')

	// Props
	const props = defineProps({
		// 当前日期格式配置
		modelValue: {
			type: Object,
			default: () => ({})
		},
		// 是否为日期范围字段
		isDateRange: {
			type: Boolean,
			default: false
		}
	})

	// Emits
	const emit = defineEmits(['update:modelValue', 'change'])

	// 响应式数据
	const selectedPreset = ref('')

	// 默认配置
	const defaultConfig = {
		// 显示格式
		displayFormat: 'YYYY-MM-DD',
		// 存储格式
		saveFormat: 'YYYY-MM-DD',
		// 支持的输入格式
		inputFormats: ['YYYY-MM-DD', 'YYYY.MM.DD', 'YYYY/MM/DD'],
		// 显示位数控制
		precision: {
			year: 4,
			month: 2,
			day: 2,
			padZero: true
		},
		// 分隔符配置
		separators: {
			dateSeparator: '-',
			rangeSeparator: ' - ',
			timeSeparator: ':'
		},
		// 显示选项
		display: {
			showWeekday: false,
			weekdayFormat: 'short',
			showLunar: false,
			monthDisplay: 'numeric'
		},
		// 验证和约束
		validation: {
			minDate: null,
			maxDate: null,
			disabledDates: null,
			allowFuture: true,
			allowPast: true
		},
		// 国际化
		locale: 'zh-CN',
		timezone: 'Asia/Shanghai'
	}

	// 当前配置
	const config = ref({ ...defaultConfig })

	// 预设配置
	const datePresets = [
		{
			key: 'standard',
			label: '标准格式 (2025-01-05)',
			config: {
				precision: { year: 4, month: 2, day: 2 },
				separators: { dateSeparator: '-', rangeSeparator: ' - ' },
				display: { showWeekday: false }
			}
		},
		{
			key: 'compact',
			label: '紧凑格式 (25.1.5)',
			config: {
				precision: { year: 2, month: 1, day: 1 },
				separators: { dateSeparator: '.', rangeSeparator: '-' },
				display: { showWeekday: false }
			}
		},
		{
			key: 'chinese',
			label: '中文格式 (2025年01月05日)',
			config: {
				precision: { year: 4, month: 2, day: 2 },
				separators: { dateSeparator: '', rangeSeparator: ' 至 ' },
				display: { showWeekday: false },
				customFormat: 'YYYY年MM月DD日'
			}
		},
		{
			key: 'american',
			label: '美式格式 (01/05/2025)',
			config: {
				precision: { year: 4, month: 2, day: 2 },
				separators: { dateSeparator: '/', rangeSeparator: ' - ' },
				display: { showWeekday: false },
				customFormat: 'MM/DD/YYYY'
			}
		},
		{
			key: 'european',
			label: '欧式格式 (05.01.2025)',
			config: {
				precision: { year: 4, month: 2, day: 2 },
				separators: { dateSeparator: '.', rangeSeparator: ' - ' },
				display: { showWeekday: false },
				customFormat: 'DD.MM.YYYY'
			}
		},
		{
			key: 'weekday',
			label: '带星期格式 (周日 2025-01-05)',
			config: {
				precision: { year: 4, month: 2, day: 2 },
				separators: { dateSeparator: '-', rangeSeparator: ' - ' },
				display: { showWeekday: true, weekdayFormat: 'short' }
			}
		}
	]

	// 使用性能优化工具
	const { cachedComputed, debouncedComputed } = usePerformanceOptimization()

	// 缓存的生成格式字符串
	const generatedFormat = cachedComputed(
		() => {
			// 如果有自定义格式，直接使用
			if (config.value.customFormat) {
				return config.value.customFormat
			}

			// 根据精度配置生成格式
			const yearFormat = config.value.precision.year === 2 ? 'YY' : 'YYYY'
			const monthFormat = config.value.precision.month === 1 ? 'M' : 'MM'
			const dayFormat = config.value.precision.day === 1 ? 'D' : 'DD'
			const separator = config.value.separators.dateSeparator

			let format = `${yearFormat}${separator}${monthFormat}${separator}${dayFormat}`

			// 如果显示星期，添加星期格式
			if (config.value.display.showWeekday) {
				const weekdayFormat = getWeekdayFormat(config.value.display.weekdayFormat)
				format = `${weekdayFormat} ${format}`
			}

			return format
		},
		() => {
			const cfg = config.value
			return `format_${cfg.customFormat || ''}_${cfg.precision.year}_${cfg.precision.month}_${cfg.precision.day}_${cfg.separators.dateSeparator}_${cfg.display.showWeekday}_${cfg.display.weekdayFormat}`
		}
	)

	// 防抖的预览文本
	const previewText = debouncedComputed(() => {
		const now = dayjs()
		return now.format(generatedFormat.value)
	}, 100)

	// 缓存的日期范围预览文本
	const rangePreviewText = cachedComputed(
		() => {
			if (!props.isDateRange) return ''
			
			const start = dayjs()
			const end = dayjs().add(7, 'day')
			const separator = config.value.separators.rangeSeparator
			
			return `${start.format(generatedFormat.value)}${separator}${end.format(generatedFormat.value)}`
		},
		() => `range_${props.isDateRange}_${generatedFormat.value}_${config.value.separators.rangeSeparator}`
	)

	// 获取星期格式
	const getWeekdayFormat = (format) => {
		switch (format) {
			case 'short': return 'ddd' // 周一
			case 'long': return 'dddd' // 星期一
			case 'narrow': return 'dd' // 一
			default: return 'ddd'
		}
	}

	// 预设变化处理
	const onPresetChange = (presetKey) => {
		if (!presetKey) return
		
		const preset = datePresets.find(p => p.key === presetKey)
		if (preset) {
			// 合并预设配置到当前配置
			config.value = {
				...config.value,
				...preset.config,
				precision: { ...config.value.precision, ...preset.config.precision },
				separators: { ...config.value.separators, ...preset.config.separators },
				display: { ...config.value.display, ...preset.config.display }
			}
			updateConfig()
		}
	}

	// 更新格式
	const updateFormat = () => {
		// 清除预设选择
		selectedPreset.value = ''
		// 删除自定义格式，使用生成的格式
		delete config.value.customFormat
		updateConfig()
	}

	// 更新配置
	const updateConfig = () => {
		// 更新显示格式
		config.value.displayFormat = generatedFormat.value
		
		// 如果是日期范围，生成范围格式
		if (props.isDateRange) {
			const singleFormat = generatedFormat.value.replace(/^(ddd|dddd|dd)\s/, '') // 移除星期部分
			config.value.displayFormat = `${singleFormat}~${singleFormat}`
		}
		
		// 发送更新事件
		emit('update:modelValue', { ...config.value })
		emit('change', { ...config.value })
	}

	// 初始化配置
	const initConfig = () => {
		if (props.modelValue && Object.keys(props.modelValue).length > 0) {
			config.value = {
				...defaultConfig,
				...props.modelValue,
				precision: { ...defaultConfig.precision, ...props.modelValue.precision },
				separators: { ...defaultConfig.separators, ...props.modelValue.separators },
				display: { ...defaultConfig.display, ...props.modelValue.display },
				validation: { ...defaultConfig.validation, ...props.modelValue.validation }
			}
		} else {
			config.value = { ...defaultConfig }
		}
	}

	// 监听props变化
	watch(() => props.modelValue, () => {
		initConfig()
	}, { deep: true })

	// 组件挂载时初始化
	onMounted(() => {
		initConfig()
	})
</script>

<style scoped>
	.date-config-panel {
		padding: 16px;
	}

	.format-preview {
		padding: 12px;
		background-color: #f5f5f5;
		border: 1px solid #d9d9d9;
		border-radius: 6px;
	}

	.preview-item {
		display: flex;
		align-items: center;
		margin-bottom: 8px;
	}

	.preview-item:last-child {
		margin-bottom: 0;
	}

	.preview-label {
		min-width: 80px;
		font-weight: 500;
		color: #666;
	}

	.preview-code {
		padding: 2px 6px;
		background-color: #f0f0f0;
		border: 1px solid #d9d9d9;
		border-radius: 3px;
		font-family: 'Courier New', monospace;
		font-size: 12px;
		color: #d63384;
	}

	.preview-text {
		font-weight: 500;
		color: #1890ff;
	}
</style>