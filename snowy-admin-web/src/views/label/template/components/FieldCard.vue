<template>
	<div class="field-card" @click="$emit('copy-field', field)" :title="`点击复制字段属性名：${field.fieldKey}`">
		<div class="field-name">{{ field.title }}</div>
		<div class="field-meta">
			<span class="field-key">{{ field.fieldKey }}</span>
			<a-tag :color="getFieldTypeColor(field.inputType)" size="small">
				{{ getFieldTypeText(field.inputType) }}
			</a-tag>
		</div>
	</div>
</template>

<script setup>
	import { useFields } from '@/composables/useFields'

	const props = defineProps({
		field: {
			type: Object,
			required: true
		}
	})

	const emit = defineEmits(['copy-field'])

	// 获取字段工具函数
	const { getInputTypeText, getInputTypeColor } = useFields()

	// 获取字段类型文本
	const getFieldTypeText = (inputType) => {
		return getInputTypeText(inputType)
	}

	// 获取字段类型颜色
	const getFieldTypeColor = (inputType) => {
		return getInputTypeColor(inputType)
	}
</script>

<style scoped>
	.field-card {
		padding: 8px 10px;
		border: 1px solid #e8e8e8;
		border-radius: 4px;
		cursor: copy;
		transition: all 0.2s;
		background: #fafafa;
		min-height: 50px;
		display: flex;
		flex-direction: column;
		justify-content: center;
		position: relative;
	}

	.field-card::after {
		content: '📋';
		position: absolute;
		top: 4px;
		right: 4px;
		font-size: 10px;
		opacity: 0.5;
		transition: opacity 0.2s;
	}

	.field-card:hover {
		border-color: #1890ff;
		background: #f6ffed;
		transform: translateY(-1px);
		box-shadow: 0 2px 6px rgba(24, 144, 255, 0.15);
	}

	.field-card:hover::after {
		opacity: 1;
		color: #1890ff;
	}

	.field-name {
		font-weight: 500;
		color: #262626;
		font-size: 13px;
		line-height: 1.2;
		margin-bottom: 4px;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.field-meta {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 6px;
	}

	.field-key {
		color: #8c8c8c;
		font-size: 11px;
		font-family: 'Courier New', monospace;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		flex: 1;
		min-width: 0;
	}
</style>
