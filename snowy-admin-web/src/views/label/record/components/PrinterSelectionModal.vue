<template>
	<a-modal
		v-model:open="visible"
		title="选择打印机"
		width="500px"
		:footer="null"
		:destroyOnClose="true"
		@cancel="handleCancel"
		class="printer-selection-modal"
	>
		<div class="printer-selection-content">
			<div class="printer-section">
				<div class="section-title">
					<PrinterOutlined />
					可用打印机
				</div>
				<a-spin :spinning="printState.loading.printers" tip="正在获取打印机列表...">
					<div class="printer-list">
						<!-- 使用单选按钮组替代下拉框 -->
						<a-radio-group
							v-model:value="printState.selectedPrinter"
							:disabled="printState.printerList.length === 0"
							class="printer-radio-group"
							@change="(e) => selectPrinter(e.target.value)"
						>
							<div
								v-for="printer in printState.printerList"
								:key="printer.name"
								class="printer-radio-item"
							>
								<a-radio :value="printer.name" class="printer-radio">
									<div class="printer-option">
										<div class="printer-info">
											<PrinterOutlined class="printer-icon" />
											<div class="printer-details">
												<div class="printer-name">
													{{ printer.name }}
													<a-tag v-if="printer.isDefault" color="blue" size="small" style="margin-left: 8px;">默认</a-tag>
												</div>
												<div class="printer-type">本地打印机</div>
											</div>
										</div>
										<a-tag :color="getPrinterStatusColor(printer.status)" size="small">
											{{ getPrinterStatusText(printer.status) }}
										</a-tag>
									</div>
								</a-radio>
							</div>
						</a-radio-group>
						</div>

						<div v-if="printState.printerList.length === 0 && !printState.loading.printers" class="no-printers">
							<div class="empty-state">
								<PrinterOutlined style="font-size: 48px; color: #d9d9d9;" />
								<div class="empty-text">未找到可用的打印机</div>
								<div class="empty-desc">请检查打印机连接或安装打印服务</div>
							</div>
						</div>
				</a-spin>
			</div>

			<div class="action-section">
				<a-space>
					<a-button @click="refreshPrinterList" :loading="printState.loading.printers">
						<ReloadOutlined />
						刷新
					</a-button>
					<a-button @click="handleCancel">
						取消
					</a-button>
					<a-button
						@click="handleConfirm"
						:disabled="!printState.selectedPrinter"
						type="primary"
					>
						<PrinterOutlined />
						开始打印
					</a-button>
				</a-space>
			</div>
		</div>
	</a-modal>
</template>

<script setup>
import { toRefs, watch, onMounted } from 'vue'
import { PrinterOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { usePrintOfficial } from '@/composables/usePrintOfficial'
import { message } from 'ant-design-vue'

// 定义 props
const props = defineProps({
	visible: {
		type: Boolean,
		default: false
	}
})

// 定义 emits
const emit = defineEmits(['update:visible', 'confirm'])

// 使用官方打印API
const {
	state: printState,
	initialize: initializePrint,
	refreshPrinterList,
	selectPrinter
} = usePrintOfficial()

// 监听打印机列表变化，自动设置默认打印机
watch(() => printState.printerList, (newList) => {
	if (newList && newList.length > 0) {
		// 查找默认打印机
		const defaultPrinter = newList.find(printer => printer.isDefault)
		if (defaultPrinter) {
			printState.selectedPrinter = defaultPrinter.name
		} else if (!printState.selectedPrinter) {
			// 如果没有默认打印机且当前没有选中任何打印机，选中第一个
			printState.selectedPrinter = newList[0].name
		}
	}
}, { immediate: true })

// 将 props 转换为响应式引用
const { visible } = toRefs(props)

// 组件挂载时初始化打印服务
onMounted(async () => {
	await initializePrint()

})

// 获取打印机状态颜色
const getPrinterStatusColor = (status) => {
	switch (status) {
		case 0:
			return 'green'
		case 'busy':
			return 'orange'
		case 'error':
			return 'red'
		case 128:
			return 'default'
		default:
			return 'blue'
	}
}

// 获取打印机状态文本
const getPrinterStatusText = (status) => {
	switch (status) {
		case 0:
			return '就绪'
		case 'busy':
			return '忙碌'
		case 'error':
			return '错误'
		case 28:
			return '离线'
		case 128:
			return '离线'
		default:
			return '未知'
	}
}

// 同步外部 visible 状态
const syncVisible = (value) => {
	emit('update:visible', value)
}

// 处理取消
const handleCancel = () => {
	syncVisible(false)
}

// 处理确认打印
const handleConfirm = () => {
	// 兼容 selectedPrinter 可能是字符串或 Ref 的情况
	const printerName = typeof printState.selectedPrinter === 'string'
		? printState.selectedPrinter
		: (printState.selectedPrinter && printState.selectedPrinter.value) || ''

	if (printerName) {
		emit('confirm', printerName)
		syncVisible(false)
	} else {
		message.warning('请先选择一个打印机')
	}
}

</script>

<style scoped lang="less">
/* 打印机选择弹窗样式 */
.printer-selection-modal {
	:deep(.ant-modal-body) {
		padding: 0;
		min-height: 300px;
		max-height: 80vh;
		overflow-y: auto;
	}

	:deep(.ant-modal-content) {
		min-height: 400px;
	}
}

.printer-selection-content {
	padding: 20px;
	min-height: 280px;
	/* 确保内容区域不会限制下拉框的显示 */
	overflow: visible;
}

.printer-section {
	margin-bottom: 20px;
}

.section-title {
	display: flex;
	align-items: center;
	gap: 8px;
	font-size: 16px;
	font-weight: 600;
	color: #262626;
	margin-bottom: 16px;
}

.printer-list {
	margin-bottom: 16px;
}

/* 单选按钮组样式 */
.printer-radio-group {
	width: 100%;
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.printer-radio-item {
	width: 100%;
}

.printer-radio {
	width: 100%;
	margin: 0;
	padding: 12px;
	border: 1px solid #e8e8e8;
	border-radius: 8px;
	transition: all 0.3s ease;
	cursor: pointer;

	&:hover {
		border-color: #1890ff;
		background-color: #f0f8ff;
	}

	/* 选中状态样式 */
	:deep(.ant-radio-checked) {
		.ant-radio-inner {
			border-color: #1890ff;
			background-color: #1890ff;
		}
	}

	/* 单选按钮本身的样式调整 */
	:deep(.ant-radio) {
		margin-right: 12px;
		flex-shrink: 0;
	}

	/* 选中状态的整体样式 */
	&.ant-radio-wrapper-checked {
		border-color: #1890ff;
		background-color: #f0f8ff;
		box-shadow: 0 2px 4px rgba(24, 144, 255, 0.1);
	}
}

.printer-option {
	display: flex;
	justify-content: space-between;
	align-items: center;
	width: 100%;
	margin-left: 0;
}

.printer-info {
	display: flex;
	align-items: center;
	gap: 12px;
	flex: 1;
	min-width: 0; /* 确保文本可以截断 */
}

.printer-icon {
	font-size: 16px;
	color: #1890ff;
}

.printer-details {
	flex: 1;
}

.printer-name {
	font-size: 14px;
	font-weight: 500;
	color: #262626;
	margin-bottom: 2px;
	display: flex;
	align-items: center;
	flex-wrap: wrap;
	gap: 8px;
}

.printer-type {
	font-size: 12px;
	color: #8c8c8c;
}

.no-printers {
	text-align: center;
	padding: 40px 20px;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12px;
}

.empty-text {
	font-size: 16px;
	color: #666;
	font-weight: 500;
}

.empty-desc {
	font-size: 14px;
	color: #999;
}

.action-section {
	display: flex;
	justify-content: flex-end;
	padding-top: 16px;
	border-top: 1px solid #e8e8e8;
}
</style>
