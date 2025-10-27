<template>
	<xn-form-container
		:title="formData.id ? '编辑字段' : '增加字段'"
		:width="800"
		:visible="visible"
		:destroy-on-close="true"
		:body-style="{ 'padding-top': '0px' }"
		@close="onClose"
	>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
			<a-row :gutter="16">
				<a-col :span="12">
					<a-form-item label="名称：" name="name">
						<a-input v-model:value="formData.name" placeholder="请输入名称" allow-clear />
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

<script setup name="bizField">
	import fieldApi from '@/api/label/fieldApi'
	import { required } from '@/utils/formRules'
	// 默认是关闭状态
	const visible = ref(false)
	const formRef = ref()
	const emit = defineEmits({ successful: null })
	const formLoading = ref(false)
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		visible.value = true
		formData.value = {}
		if (record) {
			formData.value = Object.assign({}, record)
		}
	}
	// 关闭抽屉
	const onClose = () => {
		visible.value = false
	}
	// 默认要校验的
	const formRules = {
		name: [required('请输入名称')]
	}
	// 验证并提交数据
	const onSubmit = () => {
		formRef.value
			.validate()
			.then(() => {
				formLoading.value = true
				fieldApi
					.submitForm(formData.value, formData.value.id)
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

	// 调用这个函数将子组件的一些数据和方法暴露出去
	defineExpose({
		onOpen
	})
</script>

<style scoped lang="less">
	.form-row {
		background-color: var(--item-hover-bg);
		margin-left: 0 !important;
		margin-bottom: 10px;
	}
	.form-row-con {
		padding-bottom: 5px;
		padding-top: 5px;
		padding-left: 15px;
	}
</style>
