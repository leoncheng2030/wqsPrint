<template>
	<a-modal
		:title="`${templateInfo.name} - 字段配置`"
		v-model:open="visible"
		:width="1200"
		:destroy-on-close="true"
		@cancel="onClose"
	>
		<div class="field-config-container">
			<!-- 标签页切换 -->
			<a-tabs v-model:activeKey="activeTabsKey" @change="onTabChange">
				<!-- 主字段标签页 -->
				<a-tab-pane key="MAIN" tab="主字段">
					<FieldForm
						ref="mainFieldFormRef"
						:template-info="templateInfo"
						field-scope="MAIN"
						@refresh="handleRefresh"
					/>
				</a-tab-pane>

				<!-- 明细字段标签页 -->
				<a-tab-pane key="DETAIL" tab="明细字段" v-if="templateInfo.requiresDetails==='1'">
					<FieldForm
						ref="detailFieldFormRef"
						:template-info="templateInfo"
						field-scope="DETAIL"
						@refresh="handleRefresh"
					/>
				</a-tab-pane>

				<!-- 二维码配置已移至动态字段配置中，当字段类型为QRCODE时可进行配置 -->
			</a-tabs>
		</div>

		<template #footer>
		<a-space>
			<a-button @click="onClose">关闭</a-button>
		</a-space>
	</template>
	</a-modal>
</template>

<script setup name="fieldConfigModal">
	import { message } from 'ant-design-vue'
	import FieldForm from './FieldForm.vue'
	import fieldApi from '@/api/label/fieldApi'

	// 组件属性
	const props = defineProps({
		// 可以接收外部传入的属性
	})

	// 组件事件
	const emit = defineEmits(['update:visible'])

	// 响应式数据
	const visible = ref(false)
	const templateInfo = ref({})
	const activeTabsKey = ref('MAIN')

	// 子组件引用
	const mainFieldFormRef = ref(null)
	const detailFieldFormRef = ref(null)

	// 打开弹窗
	const onOpen = (templateRecord) => {
		visible.value = true
		templateInfo.value = templateRecord || {}

		// 验证模板信息
		if (!templateRecord || !templateRecord.id) {
			message.error('模板信息不完整，无法配置字段')
			onClose()
			return
		}

		// 重置到主字段标签页
		activeTabsKey.value = 'MAIN'
	}

	// 关闭弹窗
	const onClose = () => {
		visible.value = false
		templateInfo.value = {}
	}

	// 标签页切换处理
	const onTabChange = (activeKey) => {
		activeTabsKey.value = activeKey
		// 可以在这里添加切换时的特殊逻辑
	}



	// 处理子组件刷新事件
	const handleRefresh = () => {
		// 可以触发其他刷新逻辑
	}

	

	// 暴露方法给父组件
	defineExpose({
		onOpen,
		onClose
	})
</script>

<style scoped>
.field-config-container {
	min-height: 500px;
}
</style>