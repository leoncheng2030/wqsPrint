<template>
	<div class="preview-content" ref="previewContentRef" @scroll="handleScroll">
		<a-spin :spinning="loading" tip="加载预览中...">
			<!-- 连续滚动模式 - 显示所有页面 -->
			<div v-if="htmlContent && totalPages > 1" class="continuous-preview-wrapper">
				<div
					v-for="(pageData, index) in printDataList"
					:key="`page-${index}`"
					:id="`preview-page-${index + 1}`"
					class="page-content-wrapper"
				>
					<div v-html="getPageHtml(index)"></div>
					<!-- 重新生成按钮 -->
					<div v-if="isPageError(index)" class="page-error-actions">
						<a-button 
							type="primary" 
							size="small" 
							@click="handleRegeneratePage(index)"
							:loading="regeneratingPages.has(index)"
						>
							重新生成第{{ index + 1 }}页
						</a-button>
					</div>
				</div>
			</div>

			<!-- 单页模式 -->
			<div v-else-if="htmlContent" class="preview-wrapper">
				<div v-html="htmlContent"></div>
				<!-- 重新生成按钮 -->
				<div v-if="isPageError(0)" class="page-error-actions">
					<a-button 
						type="primary" 
						size="small" 
						@click="handleRegeneratePage(0)"
						:loading="regeneratingPages.has(0)"
					>
						重新生成
					</a-button>
				</div>
			</div>

			<!-- 空状态 -->
			<div v-else-if="!loading" class="empty-preview">
				<a-empty description="暂无预览数据">
					<template #image>
						<FileTextOutlined style="font-size: 64px; color: #d9d9d9" />
					</template>
				</a-empty>
			</div>
		</a-spin>
	</div>
</template>

<script setup>
	import { ref, toRefs } from 'vue'
	import { FileTextOutlined } from '@ant-design/icons-vue'
	import { message } from 'ant-design-vue'

	// 定义 props
	const props = defineProps({
		loading: {
			type: Boolean,
			default: false
		},
		htmlContent: {
			type: String,
			default: ''
		},
		totalPages: {
			type: Number,
			default: 1
		},
		printDataList: {
			type: Array,
			default: () => []
		},
		previewContentRef: {
			type: Object,
			default: null
		},
		getPageHtml: {
			type: Function,
			required: true
		},
		handleScroll: {
			type: Function,
			required: true
		},
		regeneratePage: {
			type: Function,
			required: false
		}
	})

	// 跟踪正在重新生成的页面
	const regeneratingPages = ref(new Set())

	// 检查页面是否出错
	const isPageError = (pageIndex) => {
		const html = props.getPageHtml(pageIndex)
		return html && (html.includes('预览生成失败') || html.includes('预览生成异常') || html.includes('超出范围'))
	}

	// 重新生成页面
	const handleRegeneratePage = async (pageIndex) => {
		if (!props.regeneratePage) {
			message.error('重新生成功能不可用')
			return
		}

		regeneratingPages.value.add(pageIndex)
		try {
			await props.regeneratePage(pageIndex)
			message.success(`第${pageIndex + 1}页重新生成成功`)
		} catch (error) {
			console.error(`重新生成第${pageIndex + 1}页失败:`, error)
			message.error(`重新生成第${pageIndex + 1}页失败: ${error.message || '未知错误'}`)
		} finally {
			regeneratingPages.value.delete(pageIndex)
		}
	}

	// 暴露方法给父组件
	defineExpose({
		isPageError,
		handleRegeneratePage
	})
</script>

<style scoped lang="less">
	.preview-content {
		flex: 1;
		overflow: auto;
		padding: 16px;
		background: #f5f5f5;
		scroll-behavior: smooth;
		display: flex;
		justify-content: center;
	}

	.preview-wrapper {
		min-height: 400px;
		background: transparent;
		display: flex;
		justify-content: center;
		align-items: flex-start;
		padding: 0;
		width: 100%;

		// 清除hiprint的所有容器样式，只保留内容
		:deep(.hiprint-printPanel) {
			box-shadow: none !important;
			background: transparent !important;
			padding: 0 !important;
			margin: 0 !important;
		}

		:deep(.hiprint-printPaper) {
			margin: 0 !important;
			background: white;
			border: 1px solid #e0e0e0;
			box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
			padding: 0 !important;
			overflow: hidden;
		}

		// 确保内容容器样式简洁
		:deep(.hiprint-printPaper-content) {
			display: block;
			padding: 0 !important;
			margin: 0 !important;
			border: none !important;
			background: transparent !important;
		}

		:deep(div[style*='box-shadow']) {
			box-shadow: none !important;
		}

		:deep(div[style*='background']) {
			background: transparent !important;
		}

		// 确保只有最外层纸张容器有样式
		:deep(.hiprint-printPaper > div) {
			border: none !important;
			box-shadow: none !important;
			background: transparent !important;
			padding: 0 !important;
			margin: 0 !important;
		}

		// 页面错误操作按钮样式
		.page-error-actions {
			text-align: center;
			padding: 10px;
			background: #fff2f0;
			border-radius: 4px;
			margin-top: 10px;
		}
	}

	.empty-preview {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 300px;
		color: #999;
	}

	// 连续滚动预览样式
	.continuous-preview-wrapper {
		position: relative;
		min-height: 400px;
		width: 100%;
		max-width: 800px;
		margin: 0 auto;
	}

	.page-content-wrapper {
		padding: 0;
		background: transparent;
		border-radius: 8px;
		min-height: 200px;
		display: flex;
		justify-content: center;
		align-items: flex-start;
		margin-bottom: 20px;
		width: 100%;

		&:last-child {
			margin-bottom: 0;
		}

		// 清除hiprint的所有容器样式，避免双层显示
		:deep(.hiprint-printPanel) {
			border: none !important;
			box-shadow: none !important;
			background: transparent !important;
			padding: 0 !important;
			margin: 0 !important;
		}

		:deep(.hiprint-printPaper) {
			margin: 0 !important;
			background: white;
			border: 1px solid #e0e0e0;
			border-radius: 8px;
			box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
			padding: 0 !important;
			overflow: hidden;
		}

		:deep(.hiprint-printPaper-content) {
			display: block;
			padding: 0 !important;
			margin: 0 !important;
			border: none !important;
			background: transparent !important;
		}

		// 移除所有可能导致双层显示的样式
		:deep(div[style*='border']) {
			border: none !important;
		}

		:deep(div[style*='box-shadow']) {
			box-shadow: none !important;
		}

		:deep(div[style*='background']) {
			background: transparent !important;
		}

		// 确保只有最外层纸张容器有样式
		:deep(.hiprint-printPaper > div) {
			border: none !important;
			box-shadow: none !important;
			background: transparent !important;
			padding: 0 !important;
			margin: 0 !important;
		}
	}
</style>
