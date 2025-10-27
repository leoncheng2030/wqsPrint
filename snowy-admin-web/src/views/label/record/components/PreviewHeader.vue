<template>
	<div class="preview-header">
		<div class="record-info">
			<div class="info-item">
				<span class="label">业务标识：</span>
				<span class="value">{{ recordData.businessKey }}</span>
			</div>
			<div class="info-item">
				<span class="label">模板名称：</span>
				<span class="value">{{ recordData.templateName }}</span>
			</div>
			<div class="info-item">
				<span class="label">打印状态：</span>
				<a-tag :color="getPrintStatusColor(recordData.printStatus)">
					{{ getPrintStatusText(recordData.printStatus) }}
				</a-tag>
			</div>
			<div class="info-item">
				<span class="label">打印次数：</span>
				<span class="value">{{ recordData.printCount || 0 }}</span>
			</div>
		</div>
		<div class="preview-actions">
			<!-- 操作按钮 -->
			<div class="action-buttons">
				<!-- 直接打印按钮 -->
				<a-button type="primary" :loading="printing" @click="handlePrint">
					<template #icon><PrinterOutlined /></template>
					{{ totalPages > 1 ? '直接打印全部' : '直接打印' }}
				</a-button>
				<a-button @click="handleReload" :loading="loading">
					<template #icon><ReloadOutlined /></template>
					刷新
				</a-button>
				<a-button @click="handleExport">
					<template #icon><DownloadOutlined /></template>
					{{ totalPages > 1 ? '导出全部PDF' : '导出PDF' }}
				</a-button>
			</div>
		</div>
	</div>
</template>

<script setup>
	import { PrinterOutlined, ReloadOutlined, DownloadOutlined } from '@ant-design/icons-vue'

	// 定义 props
	const props = defineProps({
		recordData: {
			type: Object,
			default: () => ({})
		},
		totalPages: {
			type: Number,
			default: 1
		},
		printing: {
			type: Boolean,
			default: false
		},
		loading: {
			type: Boolean,
			default: false
		}
	})

	// 定义 emits
	const emit = defineEmits(['print', 'reload', 'export'])

	// 处理打印
	const handlePrint = () => {
		emit('print')
	}

	// 处理刷新
	const handleReload = () => {
		emit('reload')
	}

	// 处理导出
	const handleExport = () => {
		emit('export')
	}

	// 获取打印状态颜色
	const getPrintStatusColor = (status) => {
		switch (status) {
			case 'COMPLETED':
				return 'green'
			case 'FAILED':
				return 'red'
			case 'PENDING':
				return 'orange'
			default:
				return 'default'
		}
	}

	// 获取打印状态文本
	const getPrintStatusText = (status) => {
		switch (status) {
			case 'COMPLETED':
				return '已完成'
			case 'FAILED':
				return '失败'
			case 'PENDING':
				return '待处理'
			default:
				return '未知'
		}
	}
</script>

<style scoped lang="less">
	.preview-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		padding: 16px 24px;
		background: #fafafa;
		border-bottom: 1px solid #e8e8e8;
		flex-shrink: 0;
	}

	.record-info {
		display: flex;
		flex-wrap: wrap;
		gap: 16px;
		flex: 1;
	}

	.info-item {
		display: flex;
		align-items: center;

		.label {
			color: #666;
			font-size: 12px;
			margin-right: 8px;
			white-space: nowrap;
		}

		.value {
			color: #262626;
			font-size: 14px;
			font-weight: 500;
		}
	}

	.preview-actions {
		display: flex;
		align-items: center;
		gap: 16px;
		flex-shrink: 0;
	}

	.action-buttons {
		display: flex;
		gap: 8px;
	}

	// 响应式布局
	@media (max-width: 768px) {
		.preview-header {
			flex-direction: column;
			gap: 12px;

			.record-info {
				flex-direction: column;
				gap: 8px;
			}

			.preview-actions {
				align-self: flex-end;
			}
		}
	}
</style>
