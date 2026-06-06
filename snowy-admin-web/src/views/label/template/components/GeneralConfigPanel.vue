<template>
	<!-- 通用配置 -->
	<div class="section">
		<div class="section-device"></div>
		<div class="section-title">通用配置</div>
	</div>

	<a-row :gutter="16">
		<a-col :span="12">
			<a-form-item label="占位符" :label-col="{ span: 6 }">
				<a-input v-model:value="localConfig.placeholder" placeholder="请输入占位符文本" allow-clear />
			</a-form-item>
		</a-col>
		<a-col :span="12">
			<a-form-item label="字段状态" :label-col="{ span: 6 }">
				<a-select v-model:value="localConfig.status" placeholder="请选择字段状态">
					<a-select-option value="ENABLE">启用</a-select-option>
					<a-select-option value="DISABLE">禁用</a-select-option>
				</a-select>
				<div class="field-hint">禁用的字段不会在打印记录表单中显示</div>
			</a-form-item>
		</a-col>
	</a-row>
</template>

<script setup name="GeneralConfigPanel">
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

	.ant-form-item-explain {
		margin-top: 4px;
	}

	.ant-form-item-explain small {
		color: #999;
		font-size: 12px;
	}

	.field-hint {
		margin-top: 4px;
		font-size: 12px;
		color: #666;
		line-height: 1.4;
	}
</style>
