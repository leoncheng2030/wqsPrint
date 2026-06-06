<template>
	<div class="section">
		<div class="section-device"></div>
		<div class="section-title">文本输入配置</div>
	</div>

	<a-form-item label="启用补零">
		<a-switch v-model:checked="localConfig.enablePadding" />
		<span class="ml-2">{{ localConfig.enablePadding ? '启用' : '禁用' }}补零功能</span>
	</a-form-item>

	<template v-if="localConfig.enablePadding">
		<a-form-item label="补零方向">
			<a-select v-model:value="localConfig.paddingDirection" placeholder="请选择补零方向" style="width: 100%">
				<a-select-option value="left">前补零</a-select-option>
				<a-select-option value="right">后补零</a-select-option>
			</a-select>
			<div class="ant-form-item-explain">
				<small>选择在字符串前面还是后面补零</small>
			</div>
		</a-form-item>

		<a-form-item label="字符总长度">
			<a-input-number
				v-model:value="localConfig.totalLength"
				:min="1"
				:max="50"
				placeholder="字符总长度"
				style="width: 100%"
			/>
			<div class="ant-form-item-explain">
				<small>设置补零后的字符串总长度</small>
			</div>
		</a-form-item>

		<a-form-item label="补零字符">
			<a-input
				v-model:value="localConfig.paddingChar"
				placeholder="补零字符（默认为0）"
				maxlength="1"
				style="width: 100%"
			/>
			<div class="ant-form-item-explain">
				<small>用于补位的字符，默认为数字0</small>
			</div>
		</a-form-item>

		<a-form-item v-if="localConfig.totalLength && localConfig.paddingDirection" label="效果预览">
			<div class="content-preview">
				<a-typography-paragraph code>
					{{ getPaddingPreview() }}
				</a-typography-paragraph>
			</div>
		</a-form-item>
	</template>
</template>

<script setup name="InputConfigPanel">
	import { computed } from 'vue'

	const props = defineProps({
		config: {
			type: Object,
			default: () => ({})
		}
	})

	const localConfig = computed(() => props.config)

	// 获取补零效果预览
	const getPaddingPreview = () => {
		const config = props.config
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

	.content-preview {
		padding: 12px;
		background-color: #f5f5f5;
		border: 1px solid #d9d9d9;
		border-radius: 4px;
		margin-top: 8px;
	}
</style>
