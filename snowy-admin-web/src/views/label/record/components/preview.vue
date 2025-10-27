<template>
	<a-modal
		v-model:open="modalVisible"
		:title="`打印记录预览 - ${currentRecord.businessKey || '未知'}`"
		:width="modalWidth"
		:footer="null"
		:destroy-on-close="true"
		:mask-closable="false"
		@cancel="onClose"
		class="print-preview-modal"
		:body-style="{ height: modalHeight, padding: '0', overflow: 'hidden' }"
	>
		<div class="preview-container">
			<!-- 头部信息组件 -->
			<PreviewHeader
				:record-data="recordDataForPreview"
				:total-pages="totalPages"
				:printing="printing"
				:loading="loading"
				@print="handleDirectPrint"
				@reload="handleReload"
				@export="handleExport"
			/>

			<!-- 预览内容组件 -->
			<PreviewContent
				:loading="loading"
				:html-content="htmlContent"
				:total-pages="totalPages"
				:print-data-list="printDataList"
				:preview-content-ref="previewContentRef"
				:get-page-html="getPageHtml"
				:handle-scroll="handleScroll"
				:regenerate-page="regeneratePage"
			/>
		</div>

		<!-- 打印机选择弹窗组件 -->
		<PrinterSelectionModal v-model:visible="printerModalVisible" @confirm="handlePrintWithPrinter" />
	</a-modal>
</template>

<script setup>
	import { ref, computed, onUnmounted, reactive } from 'vue'
	import { message } from 'ant-design-vue'
	// 导入子组件
	import PreviewHeader from './PreviewHeader.vue'
	import PreviewContent from './PreviewContent.vue'
	import PrinterSelectionModal from './PrinterSelectionModal.vue'
	// 导入优化的组合式函数
	import { usePrintOfficial } from '@/composables/usePrintOfficial'
	import templateApi from "@/api/label/templateApi";
	import recordApi from '@/api/label/recordApi'

	// Props 定义
	const props = defineProps({
		recordData: { type: Object, default: () => ({}) }
	})

	// Emits 定义
	const emit = defineEmits(['refresh'])

	// 响应式引用 - 内部管理弹窗状态
	const modalVisible = ref(false)
	const currentRecord = ref({})
	const printerModalVisible = ref(false)
	const printing = ref(false)
	const templateJSON = ref({})

	// 弹窗尺寸默认值
	const modalWidth = ref('80%')
	const modalHeight = ref('70vh')
	// 使用新的统一打印管理架构
	const {
		state: printState,
		initialize: initializePrint,
		createTemplate,
		printHtml,
		printMulti,
		printPdf,
		getStatus: getPrintStatus,
		refreshPrinterList,
		generatePreviewHtml
	} = usePrintOfficial()

	// 响应式状态
	const rendererState = reactive({
		loading: false,
		htmlContent: '',
		printData: {},
		printDataList: [],
		currentPageIndex: 0,
		totalPages: 1,
		previewContentRef: null
	})

	// 计算属性
	const loading = computed(() => rendererState.loading)
	const htmlContent = computed(() => rendererState.htmlContent)
	const totalPages = computed(() => rendererState.totalPages)
	const printDataList = computed(() => rendererState.printDataList)
	const previewContentRef = computed(() => rendererState.previewContentRef)

	// 加载打印记录详情
	const loadRecordDetail = async (recordId) => {
		rendererState.loading = true
		try {
			const response = await recordApi.recordDetail({id: recordId})
			if (!response) {
				throw new Error('未获取到记录详情')
			}

			// 解析打印数据
			let printDataList = []
			if (response.printData) {
				try {
					const parsedData = JSON.parse(response.printData)
					if (Array.isArray(parsedData)) {
						printDataList = parsedData
					} else {
						printDataList = [parsedData]
					}
				} catch (e) {
					printDataList = [{}]
				}
			} else {
				printDataList = [{}]
			}

			// 更新状态
			rendererState.printDataList = printDataList
			rendererState.totalPages = printDataList.length
			rendererState.currentPageIndex = 0
			rendererState.printData = printDataList[0] || {}

			return response
		} catch (error) {
			message.error('加载打印记录详情失败: ' + error.message)
			throw error
		} finally {
			rendererState.loading = false
		}
	}

	// 生成预览HTML
	const generatePreview = async () => {
		try {
			if (!templateJSON.value || Object.keys(templateJSON.value).length === 0) {
				throw new Error('模板数据为空')
			}

			const printData = rendererState.printData || {}

			// 确保打印服务已初始化
			const status = getPrintStatus()
			if (!status.connected) {
				const initialized = await initializePrint()
				if (!initialized) {
					throw new Error('打印服务初始化失败')
				}
			}

			// 创建模板实例
			const template = await createTemplate(templateJSON.value)
			if (!template) {
				throw new Error('创建模板实例失败')
			}

			// 生成预览HTML
			let html = ''

			// 尝试使用模板的 getHtml 方法
			if (template && typeof template.getHtml === 'function') {
				const result = template.getHtml(printData)

				// 检查是否是 Promise
				if (result && typeof result.then === 'function') {
					try {
						const resolvedResult = await result
						html = resolvedResult.html()
					} catch (promiseError) {
						html = await generatePreviewHtml(templateJSON.value, printData)
					}
				}
			} else {
				// 降级方案：使用 generatePreviewHtml
				html = await generatePreviewHtml(templateJSON.value, printData)
			}

			// 确保 html 是字符串
			if (typeof html !== 'string') {
				html = String(html || '')
			}
			// 删除旋转样式标签(transform和transform-origin的值可能是任意数值)
			html = html.replace(/<style expandcss="">\.hiprint-printPaper\{\s*transform:\s*rotate\([^)]+\);\s*transform-origin:\s*[\d.]+mm\s+[\d.]+mm;\s*}<\/style>/g, '')


			if (html.trim()) {
				rendererState.htmlContent = html
			} else {
				rendererState.htmlContent = `
					<div style="padding: 40px; text-align: center; border: 2px dashed #ffa500; color: #ffa500; background: #fff7e6;">
						<h3>预览生成警告</h3>
						<p>生成的HTML内容为空，请检查模板配置和数据</p>
					</div>
				`
			}
		} catch (error) {
			rendererState.htmlContent = `
				<div style="padding: 40px; text-align: center; background: white; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
					<div style="color: #ff4d4f; font-size: 16px; margin-bottom: 12px;">
						<i class="anticon anticon-warning-circle"></i>
						预览生成失败
					</div>
					<div style="color: #666; font-size: 14px; margin-bottom: 12px;">
						${error.message || '无法生成预览HTML'}
					</div>
				</div>
			`
		}
	}

	// 打开预览弹窗
	const onOpen = async (record) => {
		if (!record?.id) {
			message.error('打印记录数据无效')
			return
		}
		// 保存当前记录数据
		currentRecord.value = record
		modalVisible.value = true

		try {
			// 加载记录详情
			await loadRecordDetail(record.id)
			// 获取模板数据
			await getTemplateData()
			// 生成预览
			await generatePreview()
		} catch (error) {
			// 预览初始化失败
		}
	}

	// 关闭处理
	const onClose = () => {
		modalVisible.value = false
		printerModalVisible.value = false
		clearPreviewData()
		currentRecord.value = {}
	}
	// 根据打印记录数据中的模板地获取模板数据
	const getTemplateData = async () => {
		return templateApi.templateDetail({id: currentRecord.value.templateId}).then(res => {
			templateJSON.value = JSON.parse(res.templateContent || '{}')
			return templateJSON.value
		})
	}
	// 直接打印处理
	const handleDirectPrint = () => {
		printerModalVisible.value = true
	}

	// 重新加载
	const handleReload = async () => {
		if (currentRecord.value?.id) {
			await loadRecordDetail(currentRecord.value.id)
		}
	}

	// 导出PDF
	const handleExport = async () => {
		try {
			printing.value = true
			// 检查打印服务状态
			const status = getPrintStatus()
			if (!status.connected) {
				const initialized = await initializePrint()
				if (!initialized) {
					message.error('打印服务未连接，请启动打印客户端')
					return
				}
			}
			await createTemplate(templateJSON.value)
			await printPdf(
				rendererState.printData,
				`${currentRecord.value.businessKey || 'export'}.pdf`
			)
		} catch (error) {
			message.error('导出失败: ' + error.message)
		} finally {
			printing.value = false
		}
	}

	// 打印机确认
	const handlePrintWithPrinter = async (selectedPrinter) => {
		if (!selectedPrinter) {
			message.warning('请先选择一个打印机')
			return
		}

		try {
			printing.value = true
			// 检查打印服务状态
			const status = getPrintStatus()
			if (!status.connected) {
				const initialized = await initializePrint()
				if (!initialized) {
					message.error('打印服务未连接，请启动打印客户端')
					return
				}
			}

			// 确保模板已加载并创建模板实例
			if (!templateJSON.value || Object.keys(templateJSON.value).length === 0) {
				await getTemplateData()
			}
			await createTemplate(templateJSON.value)

			// 执行打印
			if (totalPages.value > 1) {
				await printMulti(printDataList.value, {
					printer: selectedPrinter,
					title: currentRecord.value.businessKey || '打印任务',
				})
			} else {
				// 单页打印
				await printHtml(JSON.parse(currentRecord.value.printData), {
					printer: selectedPrinter,
					title: currentRecord.value.businessKey || '打印任务',
				})
			}

			printerModalVisible.value = false
		} catch (error) {
			message.error('打印失败: ' + error.message)
		} finally {
			printing.value = false
		}
	}

	// 获取页面HTML（简化版：无论单页多页都显示一个预览）
	const getPageHtml = (pageIndex) => {
		// 无论请求哪一页，都返回当前预览内容
		return rendererState.htmlContent
	}

	// 滚动处理
	const handleScroll = () => {
		// 简化的滚动处理
	}

	// 重新生成页面
	const regeneratePage = async (pageIndex) => {
		try {
			await generatePreview()
		} catch (error) {
			// 重新生成页面失败
		}
	}

	// 清理预览数据
	const clearPreviewData = () => {
		rendererState.htmlContent = ''
		rendererState.printData = {}
		rendererState.printDataList = []
		rendererState.currentPageIndex = 0
		rendererState.totalPages = 1
	}

	// 处理预览内容的计算属性 - 传递当前记录数据
	const recordDataForPreview = computed(() => currentRecord.value)

	// 清理资源
	onUnmounted(() => {
		clearPreviewData()
	})

	// 定义组件暴露的方法
	defineExpose({
		onOpen
	})
</script>

<style scoped lang="less">
	.print-preview-modal {
		/* 确保弹窗高度设置生效 */
		:deep(.ant-modal-wrap) {
			display: flex;
			align-items: center;
			justify-content: center;
		}

		:deep(.ant-modal) {
			height: v-bind(modalHeight) !important;
			max-height: v-bind(modalHeight) !important;
			margin: 0 !important;
			top: 0 !important;
			padding-bottom: 0 !important;
		}

		:deep(.ant-modal-content) {
			height: 100%;
			display: flex;
			flex-direction: column;
		}

		:deep(.ant-modal-header) {
			flex-shrink: 0;
		}

		:deep(.ant-modal-body) {
			flex: 1;
			padding: 0;
			overflow: hidden;
			height: 100%;
			min-height: 0;
		}
	}

	.preview-container {
		display: flex;
		flex-direction: column;
		height: 100%;
		min-height: 0;
	}

	/* 美化滚动条样式 */
	:deep(.preview-content) {
		/* Webkit 浏览器滚动条样式 */
		&::-webkit-scrollbar {
			width: 8px;
			height: 8px;
		}

		&::-webkit-scrollbar-track {
			background: #f1f1f1;
			border-radius: 4px;
			margin: 2px;
		}

		&::-webkit-scrollbar-thumb {
			background: #c1c1c1;
			border-radius: 4px;
			border: 1px solid #f1f1f1;
			transition: all 0.3s ease;

			&:hover {
				background: #a8a8a8;
			}

			&:active {
				background: #999;
			}
		}

		&::-webkit-scrollbar-corner {
			background: #f1f1f1;
		}

		/* Firefox 滚动条样式 */
		scrollbar-width: thin;
		scrollbar-color: #c1c1c1 #f1f1f1;
	}

	/* 预览内容区域的滚动条 */
	:deep(.preview-wrapper) {
		&::-webkit-scrollbar {
			width: 10px;
			height: 10px;
		}

		&::-webkit-scrollbar-track {
			background: #f8f9fa;
			border-radius: 5px;
			border: 1px solid #e9ecef;
		}

		&::-webkit-scrollbar-thumb {
			background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
			border-radius: 5px;
			border: 2px solid #f8f9fa;
			box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
			transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

			&:hover {
				background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
				box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
			}

			&:active {
				background: linear-gradient(135deg, #4c51bf 0%, #553c9a 100%);
			}
		}

		&::-webkit-scrollbar-corner {
			background: #f8f9fa;
		}
	}

	/* 多页预览容器的滚动条 */
	:deep(.multi-page-preview) {
		&::-webkit-scrollbar {
			width: 12px;
			height: 12px;
		}

		&::-webkit-scrollbar-track {
			background: rgba(255, 255, 255, 0.9);
			border-radius: 6px;
			margin: 3px;
			box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.05);
		}

		&::-webkit-scrollbar-thumb {
			background: linear-gradient(180deg, #4facfe 0%, #00f2fe 100%);
			border-radius: 6px;
			border: 2px solid rgba(255, 255, 255, 0.9);
			box-shadow: 0 2px 6px rgba(79, 172, 254, 0.3);
			transition: all 0.3s ease;

			&:hover {
				background: linear-gradient(180deg, #3d8bfd 0%, #00d4fe 100%);
				box-shadow: 0 4px 12px rgba(79, 172, 254, 0.4);
			}

			&:active {
				background: linear-gradient(180deg, #2e7dd9 0%, #00b6e6 100%);
			}
		}

		&::-webkit-scrollbar-corner {
			background: rgba(255, 255, 255, 0.9);
		}
	}

	/* 页面导航区域的滚动条 */
	:deep(.page-navigation) {
		&::-webkit-scrollbar {
			width: 6px;
		}

		&::-webkit-scrollbar-track {
			background: transparent;
		}

		&::-webkit-scrollbar-thumb {
			background: rgba(24, 144, 255, 0.4);
			border-radius: 3px;
			transition: all 0.2s ease;

			&:hover {
				background: rgba(24, 144, 255, 0.6);
			}
		}
	}

	/* 打印内容区域的滚动条 */
	:deep(.print-content) {
		&::-webkit-scrollbar {
			width: 8px;
			height: 8px;
		}

		&::-webkit-scrollbar-track {
			background: #fafafa;
			border-radius: 4px;
		}

		&::-webkit-scrollbar-thumb {
			background: #d9d9d9;
			border-radius: 4px;
			border: 1px solid #fafafa;
			transition: all 0.2s ease;

			&:hover {
				background: #bfbfbf;
			}
		}
	}

	/* 响应式滚动条 */
	@media (max-width: 768px) {
		:deep(.preview-content),
		:deep(.preview-wrapper),
		:deep(.multi-page-preview) {
			&::-webkit-scrollbar {
				width: 4px;
				height: 4px;
			}

			&::-webkit-scrollbar-thumb {
				border-width: 1px;
			}
		}
	}

	/* 暗色主题支持 */
	@media (prefers-color-scheme: dark) {
		:deep(.preview-content) {
			&::-webkit-scrollbar-track {
				background: #2f2f2f;
			}

			&::-webkit-scrollbar-thumb {
				background: #555;
				border-color: #2f2f2f;

				&:hover {
					background: #777;
				}
			}
		}

		:deep(.preview-wrapper) {
			&::-webkit-scrollbar-track {
				background: #1f1f1f;
				border-color: #333;
			}

			&::-webkit-scrollbar-thumb {
				background: linear-gradient(135deg, #7c3aed 0%, #a855f7 100%);
				border-color: #1f1f1f;
			}
		}
	}
</style>
