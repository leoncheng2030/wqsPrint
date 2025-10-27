<template>
	<xn-form-container
		:title="formData.id ? '编辑标签模板' : '新增标签模板'"
		:width="800"
		:visible="visible"
		:destroy-on-close="true"
		:body-style="{ 'padding-top': '0px' }"
		@close="onClose"
	>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
			<a-row :gutter="16">
				<a-col :span="12">
					<a-form-item label="模板编码：" name="code">
						<a-input v-model:value="formData.code" placeholder="根据模板名称自动生成" :disabled="true" />
					</a-form-item>
				</a-col>
				<a-col :span="12">
					<a-form-item label="模板名称：" name="name">
						<a-input v-model:value="formData.name" placeholder="请输入模板名称" allow-clear @input="onNameChange" />
					</a-form-item>
				</a-col>
			</a-row>
			<a-row :gutter="16">
				<a-col :span="12">
					<a-form-item label="业务类型：" name="type">
						<a-select v-model:value="formData.type" placeholder="请选择业务类型" :options="typeOptions" allow-clear>
						</a-select>
					</a-form-item>
				</a-col>
				<a-col :span="12">
					<a-form-item label="是否明细模板：" name="requiresDetails">
						<a-select v-model:value="formData.requiresDetails" :options="requiresDetailsOptions">
						</a-select>
					</a-form-item>
				</a-col>
			</a-row>
			<a-row :gutter="16" v-if="formData.id">
				<a-col :span="12">
					<a-form-item label="状态：" name="status">
						<a-radio-group v-model:value="formData.status" :options="statusOptions">
						</a-radio-group>
					</a-form-item>
				</a-col>
			</a-row>
		</a-form>
		<template #footer>
			<a-button class="xn-mr8" @click="onClose">关闭</a-button>
			<a-button type="primary" :loading="formLoading" @click="onSubmit">保存</a-button>
		</template>
	</xn-form-container>
</template>

<script setup name="labelTemplate">
	import templateApi from '@/api/label/templateApi'
	import { required } from '@/utils/formRules'
	import tool from '@/utils/tool'

	// 默认是关闭状态
	const visible = ref(false)
	const formRef = ref()
	const emit = defineEmits({ successful: null })
	const formLoading = ref(false)

	// 表单数据
	const formData = ref({})

	// 根据模板名称生成编码
	const generateCodeFromName = (name) => {
		if (!name) {
			return ''
		}

		// 移除特殊字符，保留中文、英文、数字
		let code = name.replace(/[^\u4e00-\u9fa5a-zA-Z0-9]/g, '')

		// 如果包含中文，转换为拼音首字母
		if (/[\u4e00-\u9fa5]/.test(code)) {
			// 简单的中文转拼音首字母处理（可以根据需要使用更完善的拼音库）
			code = code.replace(/[\u4e00-\u9fa5]/g, (char) => {
				// 这里可以集成拼音转换库，暂时使用简单映射
				return getPinyinFirstLetter(char)
			})
		}

		// 转换为大写并添加前缀
		code = 'TPL_' + code.toUpperCase()

		// 添加时间戳确保唯一性（仅在新增时）
		if (!formData.value.id) {
			const timestamp = Date.now().toString().slice(-4)
			code += '_' + timestamp
		}

		return code
	}

	// 简单的中文转拼音首字母函数（可以根据需要扩展）
	const getPinyinFirstLetter = (char) => {
		const charCode = char.charCodeAt(0)
		// 简单映射，实际项目中建议使用专业的拼音转换库
		if (charCode >= 0x4e00 && charCode <= 0x9fa5) {
			// 返回一个基于字符编码的字母
			return String.fromCharCode(65 + (charCode % 26))
		}
		return char
	}

	// 监听模板名称变化
	const onNameChange = (e) => {
		const name = e.target.value
		// 只在新增模式下自动生成编码
		if (!formData.value.id) {
			formData.value.code = generateCodeFromName(name)
		}
	}

	// 打开抽屉
	const onOpen = (record) => {
		visible.value = true
		// 初始化表单数据
		formData.value = {
			requiresDetails: '2', // 默认不是明细模板
			status: 'ENABLE' // 默认启用状态
		}

		// 如果是编辑模式，加载数据
		if (record) {
			convertFormData(record)
		}
	}

	// 关闭抽屉
	const onClose = () => {
		visible.value = false
		formData.value = {}
	}

	// 回显数据
	const convertFormData = (record) => {
		const param = {
			id: record.id
		}
		// 查询详情
		templateApi.templateDetail(param).then((data) => {
			formData.value = Object.assign(formData.value, data)
		})
	}

	// 表单验证规则
	const formRules = {
		// 编码字段不再需要手动验证，因为是自动生成的
		name: [required('请输入模板名称')],
		type: [required('请选择业务类型')],
		requiresDetails: [required('请选择是否为明细模板')]
	}

	// 验证并提交数据
	const onSubmit = () => {
		formRef.value
			.validate()
			.then(() => {
				formLoading.value = true
				templateApi
					.templateSubmitForm(formData.value, formData.value.id)
					.then(() => {
						onClose()
						emit('successful')
					})
					.finally(() => {
						formLoading.value = false
					})
			})
			.catch(() => {})
	}

	// 业务类型选项
	const typeOptions = tool.dictList('BUSINESS_TYPE')

	// 是否明细模板选项
	const requiresDetailsOptions = tool.dictList('COMMON_YES_OR_NO')

	// 状态选项
	const statusOptions = tool.dictList('COMMON_STATUS')

	// 调用这个函数将子组件的一些数据和方法暴露出去
	defineExpose({
		onOpen
	})
</script>

<style scoped lang="less">
	// 可以根据需要添加样式
</style>
