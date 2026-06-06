<template>
	<div class="section">
		<div class="section-device"></div>
		<div class="section-title">文本域配置</div>
	</div>

	<a-form-item label="行数">
		<a-input-number v-model:value="localConfig.rows" :min="2" :max="20" placeholder="文本域行数" style="width: 100%" />
	</a-form-item>

	<a-form-item label="最大长度">
		<a-input-number v-model:value="localConfig.maxLength" :min="1" placeholder="最大字符长度" style="width: 100%" />
	</a-form-item>

	<a-form-item label="显示字符计数">
		<a-switch v-model:checked="localConfig.showCount" />
		<span class="ml-2">{{ localConfig.showCount ? '显示' : '隐藏' }}字符计数</span>
	</a-form-item>
</template>

<script setup name="TextareaConfigPanel">
	import { computed } from 'vue'

	const props = defineProps({
		config: {
			type: Object,
			default: () => ({})
		}
	})

	const localConfig = computed(() => props.config)
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
</style>
