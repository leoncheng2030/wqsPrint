<template>
	<div class="section">
		<div class="section-device"></div>
		<div class="section-title">数字配置</div>
	</div>

	<a-form-item label="最小值">
		<a-input-number v-model:value="localConfig.min" placeholder="最小值" style="width: 100%" />
	</a-form-item>

	<a-form-item label="最大值">
		<a-input-number v-model:value="localConfig.max" placeholder="最大值" style="width: 100%" />
	</a-form-item>

	<a-form-item label="步长">
		<a-input-number v-model:value="localConfig.step" :min="0.01" placeholder="步长" style="width: 100%" />
	</a-form-item>

	<a-form-item label="小数位数">
		<a-input-number
			v-model:value="localConfig.precision"
			:min="0"
			:max="10"
			placeholder="小数位数"
			style="width: 100%"
		/>
	</a-form-item>
</template>

<script setup name="NumberConfigPanel">
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
