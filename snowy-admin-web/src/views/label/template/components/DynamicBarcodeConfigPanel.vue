<template>
	<div class="section">
		<div class="section-device"></div>
		<div class="section-title">动态条码配置</div>
	</div>

	<a-form-item label="编码规则" name="codeRule">
		<a-select
			v-model:value="localConfig.codeRule"
			placeholder="请选择编码规则"
			:options="codeRuleOptions"
			@change="updateDynamicBarcodeContent"
			show-search
			:filter-option="filterCodeRuleOption"
		/>
		<div class="ant-form-item-explain">
			<small>选择预定义的编码规则来生成动态条码内容</small>
		</div>
	</a-form-item>

	<a-form-item label="关联字段">
		<a-select
			v-model:value="localConfig.selectedFields"
			mode="multiple"
			placeholder="请选择要包含在条码中的字段"
			:options="availableFieldOptions"
			@change="updateDynamicBarcodeContent"
			show-search
			allow-clear
		/>
		<div class="ant-form-item-explain">
			<small>选择的字段将用于替换编码规则中的字段占位符</small>
		</div>
	</a-form-item>

	<a-form-item v-if="localConfig.previewContent" label="内容预览">
		<div class="content-preview">
			<a-typography-paragraph code copyable>
				{{ localConfig.previewContent }}
			</a-typography-paragraph>
		</div>
	</a-form-item>
</template>

<script setup name="DynamicBarcodeConfigPanel">
	import { computed, onMounted, watch } from 'vue'
	import dayjs from 'dayjs'

	const props = defineProps({
		config: {
			type: Object,
			default: () => ({})
		},
		codeRuleOptions: {
			type: Array,
			default: () => []
		},
		availableFieldOptions: {
			type: Array,
			default: () => []
		}
	})

	const localConfig = computed(() => props.config)

	// 组件挂载时初始化内容预览
	onMounted(() => {
		updateDynamicBarcodeContent()
	})

	// 监听编码规则选项变化，重新更新预览
	watch(
		() => props.codeRuleOptions,
		() => {
			if (props.config.codeRule) {
				updateDynamicBarcodeContent()
			}
		},
		{ deep: true }
	)

	// 更新动态条码内容预览
	const updateDynamicBarcodeContent = () => {
		const config = props.config

		if (!config.codeRule) {
			config.previewContent = ''
			return
		}

		const selectedRule = props.codeRuleOptions.find((rule) => rule.value === config.codeRule)

		if (selectedRule) {
			let previewContent = ''

			try {
				let segments = selectedRule.segments

				if (typeof segments === 'string') {
					segments = JSON.parse(segments)
				}

				if (Array.isArray(segments) && segments.length > 0) {
					const contentParts = []

					segments.forEach((segment) => {
						switch (segment.type) {
							case 'fixed':
								contentParts.push(segment.value || '')
								break
							case 'field': {
								const fieldName = segment.fieldName || 'field'
								if (config.selectedFields && config.selectedFields.includes(fieldName)) {
									contentParts.push(`{${fieldName}}`)
								} else {
									contentParts.push(`{${fieldName}}`)
								}
								break
							}
							case 'date': {
								const dateFormat = segment.format || 'YYYY-MM-DD'
								const now = new Date()
								try {
									const formattedDate = dayjs(now).format(dateFormat)
									contentParts.push(formattedDate)
								} catch (error) {
									contentParts.push(`{日期:${dateFormat}}`)
								}
								break
							}
							case 'serial': {
								const length = segment.length || 3
								const startValue = segment.startValue || 1
								const serialFormat = String(startValue).padStart(length, '0')
								contentParts.push(`{序列号:${serialFormat}}`)
								break
							}
							case 'separator':
								contentParts.push(segment.separator || '')
								break
							default:
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

			config.previewContent = previewContent
		} else {
			config.previewContent = '未找到对应的编码规则'
		}
	}

	// 编码规则下拉框过滤函数
	const filterCodeRuleOption = (input, option) => {
		return option.label.toLowerCase().includes(input.toLowerCase())
	}
</script>

<style scoped>
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

	.content-preview {
		padding: 12px;
		background-color: #f5f5f5;
		border: 1px solid #d9d9d9;
		border-radius: 4px;
		margin-top: 8px;
	}
</style>
