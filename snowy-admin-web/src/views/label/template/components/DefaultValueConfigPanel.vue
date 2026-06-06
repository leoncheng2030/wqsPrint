<template>
	<!-- 默认值设置 -->
	<a-form-item label="默认值">
		<!-- 选择类控件的默认值设置 -->
		<template v-if="['SELECT', 'RADIO', 'CHECKBOX'].includes(localConfig.inputType)">
			<!-- 单选类型（SELECT单选、RADIO） -->
			<a-select
				v-if="['SELECT', 'RADIO'].includes(localConfig.inputType)"
				v-model:value="localConfig.defaultValue"
				:options="getFieldOptions(localConfig)"
				:placeholder="'请选择默认值'"
				allow-clear
				:show-search="true"
				:filter-option="filterOption"
			>
			</a-select>

			<!-- 多选类型（SELECT多选、CHECKBOX） -->
			<a-select
				v-else-if="
					localConfig.inputType === 'CHECKBOX' || (localConfig.inputType === 'SELECT' && localConfig.isMultiple === '1')
				"
				v-model:value="localConfig.defaultValue"
				:options="getFieldOptions(localConfig)"
				mode="multiple"
				:placeholder="'请选择默认值'"
				allow-clear
				:show-search="true"
				:filter-option="filterOption"
			>
			</a-select>
		</template>

		<!-- 日期类控件的默认值设置 -->
		<template v-else-if="['DATE'].includes(localConfig.inputType)">
			<a-date-picker
				v-model:value="localConfig.defaultValue"
				:format="getDateFormat(localConfig)"
				:placeholder="'请选择默认日期'"
				style="width: 100%"
			/>
		</template>

		<template v-else-if="['DATE_RANGE'].includes(localConfig.inputType)">
			<a-range-picker
				v-model:value="localConfig.defaultValue"
				:format="getDateFormat(localConfig)"
				:placeholder="['开始日期', '结束日期']"
				style="width: 100%"
			/>
		</template>

		<!-- 其他类型控件使用文本输入 -->
		<a-input v-else v-model:value="localConfig.defaultValue" :placeholder="'请输入默认值'" allow-clear />
	</a-form-item>
</template>

<script setup name="DefaultValueConfigPanel">
	import { ref, computed, watch } from 'vue'
	import tool from '@/utils/tool'
	import { getDateFormat } from '@/utils/dateUtils'

	const props = defineProps({
		config: {
			type: Object,
			default: () => ({})
		}
	})

	const localConfig = computed(() => props.config)

	// 字段选项缓存
	const fieldOptionsCache = ref({})

	// 获取字段选项数据
	const getFieldOptions = (field) => {
		// 对于选择类控件，需要返回选项数据
		if (['SELECT', 'RADIO', 'CHECKBOX'].includes(field.inputType)) {
			// 根据数据源类型返回相应的选项数据
			if (field.dataSource === 'dict' && field.dictTypeCode) {
				// 字典数据源
				const cachedOptions = fieldOptionsCache.value[field.fieldKey]
				if (cachedOptions && cachedOptions.length > 0) {
					return cachedOptions
				}
				return []
			} else if (field.dataSource === 'static' && field.staticOptions) {
				// 静态数据源
				try {
					const staticOptions =
						typeof field.staticOptions === 'string' ? JSON.parse(field.staticOptions) : field.staticOptions

					if (Array.isArray(staticOptions)) {
						return staticOptions
					} else if (typeof staticOptions === 'object' && staticOptions !== null) {
						return Object.entries(staticOptions).map(([value, label]) => ({
							label: String(label),
							value: String(value)
						}))
					}
				} catch (e) {
					return []
				}
			} else if (field.dataSource === 'api' && field.optionApiUrl) {
				return []
			}
		}
		return []
	}

	// 选项过滤函数
	const filterOption = (input, option) => {
		return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
	}

	// 为默认值选择加载字段选项
	const loadFieldOptionsForDefaultValue = async () => {
		try {
			if (!['SELECT', 'RADIO', 'CHECKBOX'].includes(localConfig.value.inputType)) {
				return
			}

			const field = localConfig.value

			if (field.dataSource === 'dict' && field.dictTypeCode) {
				const options = tool.dictList(field.dictTypeCode)
				if (options && options.length > 0) {
					fieldOptionsCache.value[field.fieldKey] = options
				}
			} else if (field.dataSource === 'static' && field.staticOptions) {
				try {
					const staticOptions =
						typeof field.staticOptions === 'string' ? JSON.parse(field.staticOptions) : field.staticOptions

					let options = []
					if (Array.isArray(staticOptions)) {
						options = staticOptions
					} else if (typeof staticOptions === 'object' && staticOptions !== null) {
						options = Object.entries(staticOptions).map(([value, label]) => ({
							label: String(label),
							value: String(value)
						}))
					}

					if (options.length > 0) {
						fieldOptionsCache.value[field.fieldKey] = options
					}
				} catch (e) {
					// 解析静态选项失败
				}
			}
		} catch (error) {
			// 加载字段选项失败
		}
	}

	// 监听数据源变化，更新字段选项缓存
	watch(
		() => [localConfig.value.dataSource, localConfig.value.dictTypeCode, localConfig.value.staticOptions],
		() => {
			loadFieldOptionsForDefaultValue()
		},
		{ deep: true, immediate: true }
	)
</script>
