import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import fieldApi from '@/api/label/fieldApi'
import { inputTypeOptions, getInputTypeText, getInputTypeColor } from '@/config/inputTypes'

/**
 * 字段管理组合式函数
 */
export function useFields(templateId) {
	const dynamicFields = ref([])
	const loadingFields = ref(false)
	const fieldSearchText = ref('')



	/**
	 * 计算属性：过滤后的主字段
	 */
	const filteredMainFields = computed(() => {
		return dynamicFields.value.filter((field) => {
			const matchesScope = field.fieldScope === 'MAIN'
			const matchesSearch =
				!fieldSearchText.value ||
				field.title.toLowerCase().includes(fieldSearchText.value.toLowerCase()) ||
				field.fieldKey.toLowerCase().includes(fieldSearchText.value.toLowerCase())
			return matchesScope && matchesSearch
		})
	})

	/**
	 * 计算属性：过滤后的明细字段
	 */
	const filteredDetailFields = computed(() => {
		return dynamicFields.value.filter((field) => {
			const matchesScope = field.fieldScope === 'DETAIL'
			const matchesSearch =
				!fieldSearchText.value ||
				field.title.toLowerCase().includes(fieldSearchText.value.toLowerCase()) ||
				field.fieldKey.toLowerCase().includes(fieldSearchText.value.toLowerCase())
			return matchesScope && matchesSearch
		})
	})



	/**
	 * 加载动态字段数据
	 */
	const loadDynamicFields = async () => {
		if (!templateId) {
			return
		}

		loadingFields.value = true
		try {
			// 并行获取主字段和明细字段
			const [mainFieldsResponse, detailFieldsResponse] = await Promise.all([
				fieldApi.fieldPage({
					templateId: templateId,
					fieldScope: 'MAIN',
					current: 1,
					size: 1000
				}),
				fieldApi.fieldPage({
					templateId: templateId,
					fieldScope: 'DETAIL',
					current: 1,
					size: 1000
				})
			])

			// 合并字段数据
			const mainFields = mainFieldsResponse?.records || []
			const detailFields = detailFieldsResponse?.records || []

			dynamicFields.value = [...mainFields, ...detailFields]
		} catch (error) {
			message.error('加载动态字段失败: ' + error.message)
		} finally {
			loadingFields.value = false
		}
	}

	/**
	 * 复制字段属性名到剪贴板
	 */
	const copyFieldKey = async (field) => {
		const result = await copyToClipboard(field.fieldKey, `已复制字段属性名：${field.fieldKey}`)

		if (result.success) {
			message.success(result.message)
		} else {
			message.error(result.message)
		}
	}

	return {
		dynamicFields,
		loadingFields,
		fieldSearchText,
		filteredMainFields,
		filteredDetailFields,
		inputTypeOptions,
		getInputTypeText,
		getInputTypeColor,
		loadDynamicFields,
		copyFieldKey
	}
}
