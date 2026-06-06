<template>
	<!-- 基础信息 -->
	<div class="section">
		<div class="section-device"></div>
		<div class="section-title">基础信息</div>
	</div>

	<a-row :gutter="16">
		<a-col :span="12">
			<a-form-item label="字段名称" :label-col="{ span: 6 }">
				<a-input v-model:value="localConfig.title" disabled />
			</a-form-item>
		</a-col>
		<a-col :span="12">
			<a-form-item label="字段标识" :label-col="{ span: 6 }">
				<a-input v-model:value="localConfig.fieldKey" disabled />
			</a-form-item>
		</a-col>
	</a-row>

	<a-form-item label="控件类型">
		<a-tag :color="inputTypeColor">
			{{ inputTypeText }}
		</a-tag>
	</a-form-item>
</template>

<script setup name="BasicInfoPanel">
	import { computed } from 'vue'
	import { getInputTypeText, getInputTypeColor } from '@/config/inputTypes'

	const props = defineProps({
		config: {
			type: Object,
			default: () => ({})
		}
	})

	const localConfig = computed(() => props.config)
	const inputTypeText = computed(() => getInputTypeText(localConfig.value.inputType))
	const inputTypeColor = computed(() => getInputTypeColor(localConfig.value.inputType))
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
