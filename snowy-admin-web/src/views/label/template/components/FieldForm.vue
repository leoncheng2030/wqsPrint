<template>
	<!-- 字段配置表单组件 - 支持主字段和明细字段 -->
	<div class="field-form-container">
		<!-- 操作按钮区域 -->
		<div class="xn-mb10">
			<a-space>
				<a-button type="primary" @click="addNewField">
					<template #icon><PlusOutlined /></template>
					新增{{ fieldTypeLabel }}
				</a-button>
				<a-button @click="batchDelete" :disabled="selectedRowKeys.length === 0">
					<template #icon><DeleteOutlined /></template>
					批量删除
				</a-button>
				<a-button @click="saveAllChanges" :loading="saveLoading" type="primary">
					<template #icon><SaveOutlined /></template>
					保存所有更改
				</a-button>
			</a-space>
		</div>

		<!-- 简化的可编辑表格 -->
		<a-table
			:key="tableKey"
			:columns="columns"
			:data-source="fieldList"
			:loading="loading"
			:row-key="record => record.id || record.tempId || `temp-${record.fieldKey || Math.random()}`"
			:row-selection="{
				selectedRowKeys: selectedRowKeys,
				onChange: onSelectChange
			}"
			:scroll="{ x: 900 }"
			:pagination="{
				current: pagination.current,
				pageSize: pagination.pageSize,
				total: pagination.total,
				showSizeChanger: true,
				showQuickJumper: true,
				showTotal: (total) => `共 ${total} 条数据`,
				onChange: onPageChange,
				onShowSizeChange: onPageSizeChange
			}"
			border
			size="small"
		>
			<template #bodyCell="{ column, record, index }">

				<!-- 字段名称 -->
				<template v-if="column.dataIndex === 'title'">
					<a-input
						v-if="record.editing"
						v-model:value="record.title"
						placeholder="请输入字段名称（仅支持汉字）"
						:status="getFieldStatus(record, 'title')"
						size="small"
						@input="onTitleChange(record)"
						@keypress="onTitleKeyPress"
						@paste="onTitlePaste"
					/>
					<span v-else>{{ record.title }}</span>
				</template>

				<!-- 字段标识 -->
				<template v-if="column.dataIndex === 'fieldKey'">
					<a-input
						v-if="record.editing"
						v-model:value="record.fieldKey"
						placeholder="请输入字段标识"
						:status="getFieldStatus(record, 'fieldKey')"
						:disabled="record.inputType === 'QRCODE'"
						size="small"
					/>
					<span v-else>{{ record.fieldKey }}</span>
				</template>
				<!-- 字段排序 -->
				<template v-if="column.dataIndex === 'sortCode'">
					<a-input
						v-if="record.editing"
						v-model:value="record.sortCode"
						placeholder="请输入字段名称"
						:status="getFieldStatus(record, 'sortCode')"
						size="small"
					/>
					<span v-else>{{ record.sortCode }}</span>
				</template>

				<!-- 控件类型 -->
				<template v-if="column.dataIndex === 'inputType'">
					<a-select
						v-if="record.editing"
						v-model:value="record.inputType"
						placeholder="请选择控件类型"
						:options="inputTypeOptions"
						:status="getFieldStatus(record, 'inputType')"
						size="small"
						style="width: 100%;"
						@change="onInputTypeChange(record)"
					/>
					<a-tag v-else :color="getInputTypeColor(record.inputType)">
						{{ getInputTypeText(record.inputType) }}
					</a-tag>
				</template>

				<!-- 必填配置 -->
				<template v-if="column.dataIndex === 'isRequired'">
					<a-switch
						v-if="record.editing"
						v-model:checked="record.isRequired"
						checked-value="1"
						un-checked-value="0"
						size="small"
					/>
					<a-tag v-else :color="record.isRequired === '1' ? 'red' : 'default'">
						{{ record.isRequired === '1' ? '必填' : '非必填' }}
					</a-tag>
				</template>

				<!-- 状态 -->
				<template v-if="column.dataIndex === 'status'">
					<a-select
						v-if="record.editing"
						v-model:value="record.status"
						:options="statusOptions"
						size="small"
					/>
					<a-tag v-else :color="record.status === 'ENABLE' ? 'green' : 'red'">
						{{ record.status === 'ENABLE' ? '启用' : '禁用' }}
					</a-tag>
				</template>

				<!-- 操作列 -->
				<template v-if="column.dataIndex === 'action'">
					<a-space size="small">
						<template v-if="record.editing">
							<a @click="saveField(record, index)" style="color: #52c41a">
								<CheckOutlined /> 保存
							</a>
							<a-divider type="vertical" />
							<a @click="cancelEdit(record, index)" style="color: #ff4d4f">
								<CloseOutlined /> 取消
							</a>
						</template>
						<template v-else>
							<a @click="editField(record, index)">
								<EditOutlined /> 编辑
							</a>
							<a-divider type="vertical" />
							<a @click="openConfigModal(record)" style="color: #1890ff">
								<SettingOutlined /> 配置
							</a>
							<a-divider type="vertical" />
							<a-popconfirm
								title="确定要删除这个字段吗？"
								@confirm="deleteField(record, index)"
							>
								<a style="color: #ff4d4f">
									<DeleteOutlined /> 删除
								</a>
							</a-popconfirm>
						</template>
					</a-space>
				</template>
			</template>
		</a-table>

		<!-- 字段配置弹窗 -->
		<FieldDetailConfigModal
			v-model:open="configModalVisible"
			:field-data="currentConfigField"
			:template-info="templateInfo"
			:field-scope="fieldScope"
			@save="handleConfigSave"
		/>
	</div>
</template>

<script setup name="FieldForm">
	import { message } from 'ant-design-vue'
	import { nextTick } from 'vue'
	import {
		PlusOutlined,
		DeleteOutlined,
		SaveOutlined,
		EditOutlined,
		CheckOutlined,
		CloseOutlined,
		SettingOutlined
		// 已移除 DragOutlined
	} from '@ant-design/icons-vue'
	import fieldApi from '@/api/label/fieldApi'
	import FieldDetailConfigModal from './FieldDetailConfigModal.vue'
	import { inputTypeOptions, getInputTypeText, getInputTypeColor } from '@/config/inputTypes'
	import { usePerformanceOptimization } from '@/composables/usePerformanceOptimization'
	import pinyin from 'js-pinyin'

	// 组件属性定义
	const props = defineProps({
		// 模板信息
		templateInfo: {
			type: Object,
			required: true
		},
		// 字段类型：MAIN(主字段) 或 DETAIL(明细字段)
		fieldScope: {
			type: String,
			default: 'MAIN',
			validator: (value) => ['MAIN', 'DETAIL'].includes(value)
		}
	})

	// 组件事件
	const emit = defineEmits(['refresh'])

	// 响应式数据
	const loading = ref(false)
	const saveLoading = ref(false)
	const fieldList = ref([])
	const selectedRowKeys = ref([])
	const originalFieldList = ref([]) // 保存原始数据用于取消编辑
	const tempIdCounter = ref(0) // 临时ID计数器
	const tableKey = ref(0) // 表格强制刷新key



	// 配置弹窗相关数据
	const configModalVisible = ref(false)
	const currentConfigField = ref(null)

	// 分页数据
	const pagination = ref({
		current: 1,
		pageSize: 20,
		total: 0
	})

	// 计算属性：字段类型标签
	const fieldTypeLabel = computed(() => {
		return props.fieldScope === 'MAIN' ? '主字段' : '明细字段'
	})

	// 表格列配置
	const columns = [
		{
			title: '字段名称',
			dataIndex: 'title',
		},
		{
			title: '字段标识',
			dataIndex: 'fieldKey',
			ellipsis: true,
			align: 'center',
		},
		{
			title: '控件类型',
			dataIndex: 'inputType',
			width: 150,
			align: 'center',
		},
		{
			title: '必填',
			dataIndex: 'isRequired',
			width: 60,
			align: 'center',

		},
		{
			title: '排序',
			dataIndex: 'sortCode',
			width: 100,
			align: 'center',
		},
		{
			title: '状态',
			dataIndex: 'status',
			width: 100,
			align: 'center'

		},
		{
			title: '操作',
			dataIndex: 'action',
			fixed: 'right',
			width: 230
		}
	]



	// 状态选项
	const statusOptions = [
		{ label: '启用', value: 'ENABLE' },
		{ label: '禁用', value: 'DISABLE' }
	]

	// 时间格式选项
	const dateFormatOptions = [
		{ label: '日期 (YYYY.MM.DD)', value: 'YYYY.MM.DD' },
		{ label: '日期 (YYYY-MM-DD)', value: 'YYYY-MM-DD' },
		{ label: '日期时间 (YYYY-MM-DD HH:mm:ss)', value: 'YYYY-MM-DD HH:mm:ss' },
		{ label: '日期时间 (YYYY-MM-DD HH:mm)', value: 'YYYY-MM-DD HH:mm' },
		{ label: '年月 (YYYY-MM)', value: 'YYYY-MM' },
		{ label: '时间 (HH:mm:ss)', value: 'HH:mm:ss' },
		{ label: '时间 (HH:mm)', value: 'HH:mm' },
		// 新增时间范围格式
		{ label: '日期范围 (YYYY-MM-DD ~ YYYY-MM-DD)', value: 'YYYY-MM-DD~YYYY-MM-DD' },
		{
			label: '日期时间范围 (YYYY-MM-DD HH:mm:ss ~ YYYY-MM-DD HH:mm:ss)',
			value: 'YYYY-MM-DD HH:mm:ss~YYYY-MM-DD HH:mm:ss'
		}
	]

	// 获取时间格式文本的辅助方法
	const getDateFormatText = (dateFormat) => {
		const option = dateFormatOptions.find(item => item.value === dateFormat)
		return option ? option.label : dateFormat
	}



	// 获取字段验证状态
	const getFieldStatus = (record, field) => {
		if (!record.editing) return ''

		// 必填字段验证
		const requiredFields = ['fieldKey', 'title', 'inputType']
		if (requiredFields.includes(field) && !record[field]) {
			return 'error'
		}

		return ''
	}

	// 是否显示字典字段
	const showDictField = (record) => {
		return ['SELECT', 'RADIO', 'CHECKBOX'].includes(record.inputType)
	}

	// 是否显示多选字段
	const showMultipleField = (record) => {
		return ['SELECT', 'CHECKBOX'].includes(record.inputType)
	}

	// 控件类型变化处理
	const onInputTypeChange = (record) => {
		// 清空不相关的字段
		if (!showDictField(record)) {
			record.dictTypeCode = ''
		}
		if (!showMultipleField(record)) {
			record.isMultiple = '0'
		}
		// 更新时间格式清空条件
		if (!['DATE', 'DATE_RANGE'].includes(record.inputType)) {
			record.dateFormat = ''
		}
		// 二维码类型特殊处理
		if (record.inputType === 'QRCODE') {
			record.fieldKey = 'qrContent'
		}
	}

	// 字段名称输入验证 - 只允许汉字
	const onTitleKeyPress = (event) => {
		const char = event.key
		// 只允许汉字字符和控制键
		if (!/[\u4e00-\u9fa5]/.test(char) &&
			char !== 'Backspace' &&
			char !== 'Delete' &&
			char !== 'ArrowLeft' &&
			char !== 'ArrowRight' &&
			char !== 'ArrowUp' &&
			char !== 'ArrowDown' &&
			char !== 'Home' &&
			char !== 'End' &&
			char !== 'Tab' &&
			char !== 'Enter') {
			event.preventDefault()
			// 显示友好提示
			message.warning('字段名称只能输入汉字，请重新输入')
		}
	}

	// 粘贴验证 - 过滤非汉字字符
	const onTitlePaste = (event) => {
		event.preventDefault()
		const pasteData = event.clipboardData.getData('text')
		// 过滤出汉字字符
		const chineseOnly = pasteData.replace(/[^\u4e00-\u9fa5]/g, '')
		if (chineseOnly !== pasteData) {
			if (chineseOnly) {
				message.warning(`已自动过滤非汉字字符，保留内容：${chineseOnly}`)
			} else {
				message.error('粘贴内容不包含汉字，请输入汉字字符')
				return
			}
		}
		// 更新输入框值
		const target = event.target
		const record = fieldList.value.find(item => item.editing)
		if (record) {
			record.title = chineseOnly
			onTitleChange(record)
		}
	}

	// 字段名称变化处理 - 自动生成字段标识
	const onTitleChange = (record) => {
		// 过滤非汉字字符
		if (record.title) {
			const chineseOnly = record.title.replace(/[^\u4e00-\u9fa5]/g, '')
			if (chineseOnly !== record.title) {
				record.title = chineseOnly
				message.warning('字段名称只能包含汉字')
			}
		}

		// 在新增模式下，每次字段名称变化时都重新生成字段标识
		// 但如果是二维码类型，保持fieldKey为qrContent
		if (record.isNew && record.title && record.inputType !== 'QRCODE') {
			// 使用js-pinyin库生成字段标识
			const fieldKey = generateFieldKey(record.title)
			record.fieldKey = fieldKey
		} else if (record.isNew && !record.title && record.inputType !== 'QRCODE') {
			// 如果字段名称被清空，也清空字段标识（二维码类型除外）
			record.fieldKey = ''
		}
	}

	// 生成字段标识的辅助方法 - 使用js-pinyin库
	const generateFieldKey = (title) => {
		if (!title) return ''

		try {
			// 配置js-pinyin选项
			pinyin.setOptions({
				checkPolyphone: false, // 不检查多音字
				charCase: 0 // 小写
			})

			// 获取拼音首字母
			let result = pinyin.getCamelChars(title)

			// 如果结果为空或包含非字母字符，使用备用方案
			if (!result || !/^[a-zA-Z]+$/.test(result)) {
				// 备用方案：逐字符处理
				result = ''
				for (let char of title) {
					if (/[\u4e00-\u9fa5]/.test(char)) {
						const charPinyin = pinyin.getCamelChars(char)
						if (charPinyin && /^[a-zA-Z]$/.test(charPinyin)) {
							result += charPinyin.toLowerCase()
						} else {
							// 如果无法转换，使用字符的Unicode编码后缀
							result += 'c' + char.charCodeAt(0).toString(16).slice(-2)
						}
					}
				}
			}

			// 确保结果为小写
			result = result.toLowerCase()

			// 确保以字母开头
			if (result && !/^[a-zA-Z]/.test(result)) {
				result = 'field' + result
			}

			// 限制长度并确保不为空
			result = result.substring(0, 20) || 'field' + Date.now().toString().slice(-4)

			return result

		} catch (error) {
			console.warn('拼音转换失败，使用备用方案:', error)
			// 错误时的备用方案
			return 'field' + Date.now().toString().slice(-4)
		}
	}

	// 行选择变化
	const onSelectChange = (selectedKeys) => {
		selectedRowKeys.value = selectedKeys
	}

	// 分页变化
	const onPageChange = (page, pageSize) => {
		pagination.value.current = page
		pagination.value.pageSize = pageSize
		loadFieldList()
	}

	// 分页大小变化
	const onPageSizeChange = (current, size) => {
		pagination.value.current = 1
		pagination.value.pageSize = size
		loadFieldList()
	}

	// 加载字段列表
	const loadFieldList = () => {
		loading.value = true

		const params = {
			current: pagination.value.current,
			size: pagination.value.pageSize,
			// 根据模板ID和字段作用域过滤
			templateId: props.templateInfo.id,
			fieldScope: props.fieldScope
		}

		return fieldApi.fieldPage(params)
			.then((res) => {
				if (res) {
					// 为每个字段添加编辑状态标识和解析配置
					const fields = (res.records || []).map(field => {
						let dateFormat = '' // 默认为空
						let status = field.status || 'ENABLE' // 默认状态

						// 尝试从optionsData中解析配置
						if (field.optionsData) {
							try {
								const options = JSON.parse(field.optionsData)
								// 解析时间格式配置
								if (field.inputType === 'DATE' && options.dateFormat) {
									dateFormat = options.dateFormat
								}

								// 解析状态配置
								if (options.status) {
									status = options.status
								} else {
									console.log(`字段 "${field.title}" JSON中没有状态配置，使用默认值:`, status)
								}
							} catch (e) {
								console.log(`字段 "${field.title}" 的原始optionsData:`, field.optionsData)
							}
						} else {
							console.log(`字段 "${field.title}" 没有optionsData，使用默认状态:`, status)
						}

						return {
							...field,
							dateFormat: dateFormat, // 添加解析后的时间格式
							status: status, // 使用解析后的状态
							editing: false,
							isNew: false
						}
					})
					const sortedFields = fields.sort((a, b) => {
						const sortCodeA = parseInt(a.sortCode) || 0
						const sortCodeB = parseInt(b.sortCode) || 0
						return sortCodeA - sortCodeB
					})
					fieldList.value = sortedFields
					originalFieldList.value = JSON.parse(JSON.stringify(sortedFields)) // 深拷贝保存原始数据
					pagination.value.total = res.total || 0
				}
			})
			.catch((error) => {
				console.error('=== 字段列表加载失败 ===')
				console.error('加载错误详情:', error)
				if (error.response) {
					console.error('API响应错误:', error.response.data)
					console.error('HTTP状态码:', error.response.status)
				}
				message.error('加载字段列表失败')
			})
			.finally(() => {
				loading.value = false
			})
	}

	// 新增字段
	const addNewField = () => {
		// 检查是否有正在编辑的行
		const editingRow = fieldList.value.find(item => item.editing)
		if (editingRow) {
			message.warning('请先保存或取消当前编辑的字段')
			return
		}

		// 检查是否选择了模板
		if (!props.templateInfo.id) {
			message.error('请先选择一个模板')
			return
		}

		// 创建新字段对象，关联当前模板ID和字段作用域
		const newField = {
			tempId: `temp_${++tempIdCounter.value}`, // 临时ID
			templateId: props.templateInfo.id, // 关联模板ID
			fieldKey: '',
			title: '',
			fieldScope: props.fieldScope, // 设置字段作用域
			inputType: '',
			dictTypeCode: '',
			optionsData: '',
			optionApiUrl: '',
			selectedDataApiUrl: '',
			isMultiple: '0',
			isRequired: '0', // 默认非必填
			placeholder: '',
			dateFormat: '', // 时间格式字段
			status: 'ENABLE',
			editing: true,
			isNew: true
		}

		// 添加到列表开头
		fieldList.value.unshift(newField)
	}

	// 编辑字段
	const editField = (record, index) => {
		// 检查是否有其他正在编辑的行
		const editingRow = fieldList.value.find(item => item.editing && item !== record)
		if (editingRow) {
			message.warning('请先保存或取消当前编辑的字段')
			return
		}

		// 保存原始数据
		record.originalData = JSON.parse(JSON.stringify(record))
		// 设置编辑状态
		record.editing = true
	}

	// 取消编辑
	const cancelEdit = (record, index) => {
		if (record.isNew) {
			// 如果是新增的记录，直接从列表中移除
			fieldList.value.splice(index, 1)
		} else {
			// 恢复原始数据
			if (record.originalData) {
				Object.assign(record, record.originalData)
				delete record.originalData
			}
			record.editing = false
		}
	}

	// 保存字段
	// 保存字段
	const saveField = (record, index) => {
		// 验证必填字段
		if (!record.fieldKey || !record.title || !record.inputType) {
			message.error('请填写完整的字段信息（字段标识、字段名称、控件类型为必填项）')
			return
		}

		// 验证字段标识唯一性
		const duplicateField = fieldList.value.find(item =>
			item !== record &&
			item.fieldKey === record.fieldKey &&
			item.fieldScope === record.fieldScope
		)
		if (duplicateField) {
			message.error('字段标识在当前作用域内已存在，请使用其他标识')
			return
		}

		try {
			// 准备保存数据
			const saveData = {
				...record,
				templateId: props.templateInfo.id,
				fieldScope: props.fieldScope
			}

			// 处理配置数据
			let optionsData = {}

			// 解析现有的配置数据
			if (record.optionsData) {
				try {
					optionsData = JSON.parse(record.optionsData)
				} catch (e) {
					console.warn('解析现有配置数据失败:', e)
				}
			}

			// 更新状态配置
			optionsData.status = record.status || 'ENABLE'

			// 处理时间格式配置
			if (record.inputType === 'DATE' && record.dateFormat) {
				optionsData.dateFormat = record.dateFormat
			} else {
				// 清空时间格式相关配置
				if (record.inputType !== 'DATE') {
					delete optionsData.dateFormat
				}
			}

			// 设置配置数据
			saveData.optionsData = JSON.stringify(optionsData)

			// 保存 isNew 状态，用于判断是新增还是编辑
			const isNewRecord = record.isNew

			// 移除临时字段
			delete saveData.tempId
			delete saveData.editing
			delete saveData.isNew
			delete saveData.originalData
			delete saveData.dateFormat

			// 使用 fieldSubmitForm 方法，第二个参数表示是否为编辑模式
			fieldApi.fieldSubmitForm(saveData, !isNewRecord).then(async (res) => {
				message.success('保存成功')
				record.editing = false

				// 如果是新增字段，保存成功后需要重新加载列表以获取完整的字段信息
				if (isNewRecord) {
					record.isNew = false
					await loadFieldList()
					// 触发刷新事件
					emit('refresh')
				}
			})


		} catch (error) {
			message.error('保存失败')
		}
	}

	// 删除字段
	const deleteField = async (record, index) => {
		try {
			if (record.isNew) {
				// 如果是新增的记录，直接从数组中移除
				fieldList.value.splice(index, 1)
				message.success('删除成功')
			} else {
				// 调用删除接口
				await fieldApi.fieldDelete([{ id: record.id }])
				message.success('删除成功')
				// 重新加载列表
				await loadFieldList()
				// 触发刷新事件
				emit('refresh')
			}
		} catch (error) {
			message.error('删除失败')
		}
	}

	// 批量删除
	const batchDelete = async () => {
		if (selectedRowKeys.value.length === 0) {
			message.warning('请选择要删除的字段')
			return
		}

		try {
			// 分离新增和已存在的记录
			const newRecords = []
			const existingIds = []

			selectedRowKeys.value.forEach(key => {
				const record = fieldList.value.find(item =>
					(item.id && item.id === key) || (item.tempId && item.tempId === key)
				)
				if (record) {
					if (record.isNew) {
						newRecords.push(record)
					} else {
						existingIds.push(record.id)
					}
				}
			})

			// 删除新增记录（直接从数组中移除）
			newRecords.forEach(record => {
				const index = fieldList.value.indexOf(record)
				if (index > -1) {
					fieldList.value.splice(index, 1)
				}
			})

			// 删除已存在的记录（调用接口）
			if (existingIds.length > 0) {
				const deleteParams = existingIds.map(id => ({ id }))
				await fieldApi.fieldDelete(deleteParams)
				// 重新加载列表
				await loadFieldList()
			}

			// 清空选中状态
			selectedRowKeys.value = []

			message.success('批量删除成功')
			// 触发刷新事件
			emit('refresh')
		} catch (error) {
			message.error('批量删除失败')
		}
	}

	// 保存所有更改
	const saveAllChanges = async () => {
		// 检查是否有正在编辑的字段
		const editingFields = fieldList.value.filter(item => item.editing)
		if (editingFields.length === 0) {
			message.info('没有需要保存的更改')
			return
		}

		saveLoading.value = true
		try {
			// 逐个保存编辑中的字段
			for (let i = 0; i < editingFields.length; i++) {
				const field = editingFields[i]
				const index = fieldList.value.indexOf(field)
				await saveField(field, index)
			}
			message.success('所有更改保存成功')
		} catch (error) {
			message.error('保存失败')
		} finally {
			saveLoading.value = false
		}
	}

	// 打开配置弹窗
	const openConfigModal = (record) => {
		// 检查字段是否已保存基本信息
		if (!record.fieldKey || !record.title || !record.inputType) {
			message.warning('请先保存字段的基本信息（字段标识、字段名称、控件类型）')
			return
		}

		// 深拷贝字段数据，避免直接修改原数据
		currentConfigField.value = JSON.parse(JSON.stringify(record))
		configModalVisible.value = true
	}

	// 处理配置保存
	const handleConfigSave = async (fieldData) => {
		try {
		loading.value = true
		// 判断是否为编辑模式（根据isNew字段判断，isNew为true表示新增，否则为编辑）
		const isEdit = !fieldData.isNew && !!(fieldData.id && fieldData.id !== '')
		const result = await fieldApi.fieldSubmitForm(fieldData, isEdit)
			message.success('配置保存成功')

		// 关闭弹窗并重置当前配置字段
		configModalVisible.value = false
		currentConfigField.value = null

		// 重新从后端加载字段列表
		await loadFieldList()

		} catch (error) {
			message.error('保存配置失败')
		} finally {
			loading.value = false
		}
	}

	// 使用性能优化工具
	const { optimizedWatch, useDOMOptimization } = usePerformanceOptimization()
	const { debouncedDOMUpdate } = useDOMOptimization()

	// 优化的监听器 - 防抖处理模板ID变化
	optimizedWatch(
		() => props.templateInfo.id,
		(newId) => {
			if (newId) {
				loadFieldList()
			}
		},
		{ immediate: true, debounce: 100 }
	)

	// 优化的监听器 - 防抖处理字段作用域变化
	optimizedWatch(
		() => props.fieldScope,
		() => {
			loadFieldList()
		},
		{ debounce: 100 }
	)

	// 监听配置弹窗关闭，重置当前配置字段
	watch(
		() => configModalVisible.value,
		(visible) => {
			if (!visible) {
				// 弹窗关闭时重置当前配置字段
				currentConfigField.value = null
				// 清除表单验证状态
				nextTick(() => {
					// 确保DOM更新后再进行其他操作
				})
			}
		}
	)


	// 已移除拖拽相关的组件挂载和监听器
	// onMounted(() => { ... })
	// optimizedWatch(() => fieldList.value, () => { ... })

	// 暴露方法给父组件
	defineExpose({
		loadFieldList,
		addNewField,
		saveAllChanges
	})
</script>

<style scoped>
.field-form-container {
	position: relative;
}

/* 已移除拖拽相关样式 */
</style>
