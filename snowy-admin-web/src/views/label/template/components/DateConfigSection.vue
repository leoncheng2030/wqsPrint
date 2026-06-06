<template>
	<div class="section">
		<div class="section-device"></div>
		<div class="section-title">时间配置</div>
	</div>

	<!-- 使用新的日期配置面板 -->
	<DateConfigPanel
		v-model="localConfig.dateConfig"
		:is-date-range="localConfig.inputType === 'DATE_RANGE'"
		@change="onDateConfigChange"
	/>

	<!-- 保持向后兼容的简单格式选择 -->
	<a-form-item label="兼容格式" name="dateFormat">
		<a-select
			v-model:value="localConfig.dateFormat"
			placeholder="选择兼容格式（可选）"
			:options="dateFormatOptions"
			allow-clear
		>
			<template #suffixIcon>
				<InfoCircleOutlined style="color: #999" />
			</template>
		</a-select>
		<div class="ant-form-item-explain">
			<small>此选项用于向后兼容，建议使用上方的高级配置</small>
		</div>
	</a-form-item>
</template>

<script setup name="DateConfigSection">
	import { computed } from 'vue'
	import { InfoCircleOutlined } from '@ant-design/icons-vue'
	import DateConfigPanel from './DateConfigPanel.vue'

	const props = defineProps({
		config: {
			type: Object,
			default: () => ({})
		}
	})

	const localConfig = computed(() => props.config)

	// 时间格式选项
	const dateFormatOptions = [
		{ label: '日期 (YYYY.MM.DD)', value: 'YYYY.MM.DD' },
		{ label: '日期 (YYYY-MM-DD)', value: 'YYYY-MM-DD' },
		{ label: '日期时间 (YYYY-MM-DD HH:mm:ss)', value: 'YYYY-MM-DD HH:mm:ss' },
		{ label: '日期时间 (YYYY-MM-DD HH:mm)', value: 'YYYY-MM-DD HH:mm' },
		{ label: '年月 (YYYY-MM)', value: 'YYYY-MM' },
		{ label: '时间 (HH:mm:ss)', value: 'HH:mm:ss' },
		{ label: '时间 (HH:mm)', value: 'HH:mm' },
		{ label: '日期范围 (YYYY-MM-DD ~ YYYY-MM-DD)', value: 'YYYY-MM-DD~YYYY-MM-DD' },
		{
			label: '日期时间范围 (YYYY-MM-DD HH:mm:ss ~ YYYY-MM-DD HH:mm:ss)',
			value: 'YYYY-MM-DD HH:mm:ss~YYYY-MM-DD HH:mm:ss'
		}
	]

	// 日期配置变化处理
	const onDateConfigChange = (config) => {
		// 更新日期配置
		// eslint-disable-next-line vue/no-mutating-props
		props.config.dateConfig = { ...config }

		// 如果有生成的显示格式，同步到兼容格式字段
		if (config.displayFormat) {
			// eslint-disable-next-line vue/no-mutating-props
			props.config.dateFormat = config.displayFormat
		}
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
</style>
