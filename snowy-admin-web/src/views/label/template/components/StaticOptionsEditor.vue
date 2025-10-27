<template>
	<div class="static-options-editor">
		<!-- 选项列表 -->
		<div class="options-list">
			<div v-if="options.length === 0" class="empty-state">
				<a-empty description="暂无选项数据" />
			</div>
			<div v-else>
				<div v-for="(option, index) in options" :key="index" class="option-item">
					<div class="option-content">
						<div class="option-fields">
							<a-input
								v-model:value="option.value"
								placeholder="选项值"
								class="option-value"
								@change="handleOptionChange"
							/>
							<a-input
								v-model:value="option.label"
								placeholder="显示文本"
								class="option-label"
								@change="handleOptionChange"
							/>
						</div>
						<div class="option-actions">
							<a-button
								type="text"
								size="small"
								danger
								@click="removeOption(index)"
								:disabled="options.length <= 1"
							>
								<template #icon>
									<DeleteOutlined />
								</template>
							</a-button>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 添加按钮 -->
		<div class="add-option">
			<a-button type="dashed" block @click="addOption">
				<template #icon>
					<PlusOutlined />
				</template>
				添加选项
			</a-button>
		</div>

		<!-- JSON预览（可选） -->
		<div v-if="showJsonPreview" class="json-preview">
			<a-divider>JSON预览</a-divider>
			<a-textarea
				:value="jsonValue"
				:rows="4"
				readonly
				class="json-textarea"
			/>
		</div>
	</div>
</template>

<script setup>
	import { ref, computed, watch, onMounted } from 'vue'
	import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'
	import { message } from 'ant-design-vue'

	// 组件属性
	const props = defineProps({
		// 当前值（JSON字符串格式）
		modelValue: {
			type: String,
			default: ''
		},
		// 是否显示JSON预览
		showJsonPreview: {
			type: Boolean,
			default: false
		},
		// 最小选项数量
		minOptions: {
			type: Number,
			default: 1
		}
	})

	// 组件事件
	const emit = defineEmits(['update:modelValue', 'change'])

	// 响应式数据
	const options = ref([])

	// 计算属性：生成JSON值
	const jsonValue = computed(() => {
		if (options.value.length === 0) {
			return '{}'
		}

		// 将选项数组转换为对象格式 {value: label}
		const optionsObj = {}
		options.value.forEach(option => {
			if (option.value && option.label) {
				optionsObj[option.value] = option.label
			}
		})

		return JSON.stringify(optionsObj, null, 2)
	})

	// 解析JSON字符串为选项数组
	const parseJsonToOptions = (jsonStr) => {
		if (!jsonStr || jsonStr.trim() === '') {
			return [{ value: '', label: '' }]
		}

		try {
			const parsed = JSON.parse(jsonStr)
			if (typeof parsed === 'object' && parsed !== null) {
				const optionsArray = Object.entries(parsed).map(([value, label]) => ({
					value: String(value),
					label: String(label)
				}))
				return optionsArray.length > 0 ? optionsArray : [{ value: '', label: '' }]
			}
		} catch (error) {
			message.warning('JSON格式不正确，已重置为空选项')
		}

		return [{ value: '', label: '' }]
	}

	// 添加选项
	const addOption = () => {
		options.value.push({ value: '', label: '' })
		handleOptionChange()
	}

	// 删除选项
	const removeOption = (index) => {
		if (options.value.length <= props.minOptions) {
			message.warning(`至少需要保留${props.minOptions}个选项`)
			return
		}
		options.value.splice(index, 1)
		handleOptionChange()
	}

	// 处理选项变化
	const handleOptionChange = () => {
		// 发送更新事件
		emit('update:modelValue', jsonValue.value)
		emit('change', jsonValue.value)
	}

	// 监听外部值变化
	watch(
		() => props.modelValue,
		(newValue) => {
			options.value = parseJsonToOptions(newValue)
		},
		{ immediate: true }
	)

	// 组件挂载时初始化
	onMounted(() => {
		if (!props.modelValue) {
			options.value = [{ value: '', label: '' }]
		}
	})
</script>

<style scoped>
	.static-options-editor {
		width: 100%;
	}

	.options-list {
		margin-bottom: 16px;
	}

	.option-item {
		margin-bottom: 8px;
		padding: 12px;
		border: 1px solid #d9d9d9;
		border-radius: 6px;
		background-color: #fafafa;
	}

	.option-content {
		display: flex;
		align-items: center;
		gap: 8px;
	}

	.option-fields {
		flex: 1;
		display: flex;
		gap: 8px;
	}

	.option-value,
	.option-label {
		flex: 1;
	}

	.option-actions {
		flex-shrink: 0;
	}

	.add-option {
		margin-bottom: 16px;
	}

	.json-preview {
		margin-top: 16px;
	}

	.json-textarea {
		font-family: 'Courier New', monospace;
		font-size: 12px;
		background-color: #f5f5f5;
	}

	.empty-state {
		padding: 20px;
		text-align: center;
		border: 1px dashed #d9d9d9;
		border-radius: 6px;
		background-color: #fafafa;
	}
</style>