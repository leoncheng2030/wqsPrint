<template>
	<div class="section">
		<div class="section-device"></div>
		<div class="section-title">选项配置</div>
	</div>

	<a-form-item label="数据来源" name="dataSource">
		<a-select
			v-model:value="localConfig.dataSource"
			placeholder="请选择数据来源"
			:options="dataSourceOptions"
			@change="onDataSourceChange"
		/>
		<div class="ant-form-item-explain">
			<small>选择选项数据的来源方式</small>
		</div>
	</a-form-item>

	<!-- 字典数据源配置 -->
	<template v-if="localConfig.dataSource === 'dict'">
		<a-form-item label="字典编码" name="dictTypeCode">
			<a-select
				v-model:value="localConfig.dictTypeCode"
				placeholder="请选择字典类型"
				allow-clear
				show-search
				:filter-option="filterDictTypeOption"
				:options="dictTypeOptions"
			/>
			<div class="ant-form-item-explain">
				<small>从系统字典中选择字典类型获取选项数据</small>
			</div>
		</a-form-item>
	</template>

	<!-- API数据源配置 -->
	<template v-if="localConfig.dataSource === 'api'">
		<a-form-item label="选项API地址" name="optionApiUrl">
			<a-input v-model:value="localConfig.optionApiUrl" placeholder="请输入获取选项数据的API地址" allow-clear />
			<div class="ant-form-item-explain">
				<small>用于动态获取选项数据的接口地址</small>
			</div>
		</a-form-item>

		<a-form-item label="已选数据API" name="selectedDataApiUrl">
			<a-input v-model:value="localConfig.selectedDataApiUrl" placeholder="请输入获取已选数据的API地址" allow-clear />
			<div class="ant-form-item-explain">
				<small>用于回显已选择数据的接口地址</small>
			</div>
		</a-form-item>
	</template>

	<!-- 静态数据源配置 -->
	<template v-if="localConfig.dataSource === 'static'">
		<a-form-item label="静态选项" name="staticOptions">
			<StaticOptionsEditor
				v-model="localConfig.staticOptions"
				:show-json-preview="false"
				:min-options="1"
				@change="handleStaticOptionsChange"
			/>
			<div class="ant-form-item-explain">
				<small>配置静态选项的值和显示文本</small>
			</div>
		</a-form-item>
	</template>

	<!-- 多选支持（仅SELECT和CHECKBOX） -->
	<a-form-item v-if="['SELECT', 'CHECKBOX'].includes(localConfig.inputType)" label="多选支持">
		<a-switch v-model:checked="localConfig.isMultiple" checked-value="1" un-checked-value="0" />
		<span class="ml-2">{{ localConfig.isMultiple === '1' ? '支持多选' : '单选模式' }}</span>
	</a-form-item>
</template>

<script setup name="SelectConfigPanel">
	import { computed } from 'vue'
	import StaticOptionsEditor from './StaticOptionsEditor.vue'

	const props = defineProps({
		config: {
			type: Object,
			default: () => ({})
		},
		dictTypeOptions: {
			type: Array,
			default: () => []
		}
	})

	const localConfig = computed(() => props.config)

	// 数据来源选项
	const dataSourceOptions = [
		{ label: '字典数据', value: 'dict' },
		{ label: 'API接口', value: 'api' },
		{ label: '静态数据', value: 'static' }
	]

	// 数据来源变化处理
	const onDataSourceChange = (value) => {
		if (value !== 'dict') {
			// eslint-disable-next-line vue/no-mutating-props
			props.config.dictTypeCode = ''
		}
		if (value !== 'api') {
			// eslint-disable-next-line vue/no-mutating-props
			props.config.optionApiUrl = ''
			// eslint-disable-next-line vue/no-mutating-props
			props.config.selectedDataApiUrl = ''
		}
		if (value !== 'static') {
			// eslint-disable-next-line vue/no-mutating-props
			props.config.staticOptions = ''
		}
	}

	// 字典类型下拉框过滤函数
	const filterDictTypeOption = (input, option) => {
		return option.label.toLowerCase().includes(input.toLowerCase())
	}

	// 处理静态选项变化
	const handleStaticOptionsChange = (options) => {
		// eslint-disable-next-line vue/no-mutating-props
		props.config.staticOptions = options
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
</style>
