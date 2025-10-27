<template>
	<!-- 加载状态 -->
	<div v-if="!isDesignerReady" class="loading-container">
		<a-spin size="large" tip="正在初始化设计器..." />
	</div>
	
	<Designer
		v-show="isDesignerReady"
		ref="designerRef"
		:template="templateData"
		:auto-connect="true"
		:authKey="DEFAULT_CONFIG.authKey"
		:showOption="showOptions"
		:plugins="plugins"
		:show-panels="true"
		@onDesigned="onDesigned"
		@onTemplateChange="handleTemplateChange"
	>
		<template #header>
			<div class="header">
				<div class="left">
					<div class="title">微企胜打印模板设计系统</div>
				</div>
				<div class="right">
					<a-space>
						<!-- 新增字段填充按钮 -->
						<a-button @click="showFieldPanel = !showFieldPanel">
							<template #icon>
								<FieldBinaryOutlined />
							</template>
							{{ showFieldPanel ? '隐藏字段' : '显示字段' }}
						</a-button>
						<a-button type="primary" @click="handlePreview">
							<template #icon>
								<EyeOutlined />
							</template>
							预览
						</a-button>
						<a-button type="primary" @click="handleSave" :loading="saving">
							<template #icon>
								<SaveOutlined />
							</template>
							保存模板
						</a-button>
					</a-space>
				</div>
			</div>
		</template>
	</Designer>

	<!-- 动态字段浮动面板 - 优化为紧凑布局 -->
	<a-drawer
		v-model:open="showFieldPanel"
		title="模板动态字段"
		placement="left"
		:width="350"
		:closable="true"
		:mask="false"
		:get-container="false"
		class="field-panel-drawer"
	>
		<div class="field-panel-content">
			<!-- 字段搜索 -->
			<div class="field-search">
				<a-input v-model:value="fieldSearchText" placeholder="搜索字段" allow-clear size="small">
					<template #prefix>
						<SearchOutlined />
					</template>
				</a-input>
			</div>

			<!-- 字段分组显示 - 紧凑网格布局 -->
			<div class="field-groups">
				<!-- 主字段 -->
				<div v-if="filteredMainFieldsWithSearch.length > 0" class="field-group">
					<div class="group-title"><FolderOutlined /> 主字段 ({{ filteredMainFieldsWithSearch.length }})</div>
					<div class="field-grid">
						<div
							v-for="field in filteredMainFieldsWithSearch"
							:key="field.id"
							class="field-card"
							@click="copyFieldKey(field)"
							:title="`点击复制字段属性名：${field.fieldKey}`"
						>
							<div class="field-name">{{ field.title }}</div>
							<div class="field-meta">
								<span class="field-key">{{ field.fieldKey }}</span>
								<a-tag :color="getInputTypeColor(field.inputType)" size="small">
									{{ getInputTypeText(field.inputType) }}
								</a-tag>
							</div>
						</div>
					</div>
				</div>

				<!-- 明细字段 -->
				<div v-if="filteredDetailFieldsWithSearch.length > 0" class="field-group">
					<div class="group-title"><TableOutlined /> 明细字段 ({{ filteredDetailFieldsWithSearch.length }})</div>
					<div class="field-grid">
						<div
							v-for="field in filteredDetailFieldsWithSearch"
							:key="field.id"
							class="field-card"
							@click="copyFieldKey(field)"
							:title="`点击复制字段属性名：${field.fieldKey}`"
						>
							<div class="field-name">{{ field.title }}</div>
							<div class="field-meta">
								<span class="field-key">{{ field.fieldKey }}</span>
								<a-tag :color="getInputTypeColor(field.inputType)" size="small">
									{{ getInputTypeText(field.inputType) }}
								</a-tag>
							</div>
						</div>
					</div>
				</div>

				<!-- 空状态 -->
				<div v-if="filteredMainFieldsWithSearch.length === 0 && filteredDetailFieldsWithSearch.length === 0" class="empty-state">
					<a-empty description="暂无字段数据" size="small" />
				</div>
			</div>

			<!-- 刷新按钮 -->
			<div class="field-actions-bar">
				<a-button @click="loadDynamicFields" :loading="loadingFields" size="small" block>
					<template #icon>
						<ReloadOutlined />
					</template>
					刷新字段
				</a-button>
			</div>
		</div>
	</a-drawer>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'
import {Designer} from '@sv-print/vue3'
import {message} from 'ant-design-vue'
import {
	EyeOutlined,
	FieldBinaryOutlined,
	FolderOutlined,
	ReloadOutlined,
	SaveOutlined,
	SearchOutlined,
	TableOutlined
} from '@ant-design/icons-vue'
import DEFAULT_CONFIG from '@/config'
import {default as e2Table} from '@sv-print/plugin-ele-e2table'
import {default as echarts} from '@sv-print/plugin-ele-echarts'
import {default as fabric} from '@sv-print/plugin-ele-fabric'
import {getInputTypeColor, getInputTypeText} from '@/config/inputTypes'
import templateApi from '@/api/label/templateApi'
import fieldApi from '@/api/label/fieldApi'

// 路由参数
const route = useRoute()
const templateId = route.query.id
const templateName = route.query.name

// 设计器工具类
const utils = ref()

// 模板数据
const templateData = ref({})
const templateInfo = ref({ name: '未知模板', code: '' })

// 动态字段
const dynamicFields = ref([])
const loadingFields = ref(false)

// 设计器是否就绪
const isDesignerReady = ref(false)
// 插件配置
const plugins = ref([e2Table(), echarts(), fabric()])
const showOptions = ref({
	showHeader: true, // 是否显示头部
	showToolbar: true, // 是否显示工具栏
	// showFooter: false, // 是否显示底部 需要 authKey 支持
	// showPower: false // 是否显示 "powered by sv-print" 需要 authKey 支持
})

// 响应式数据
const designerRef = ref(null)
const showFieldPanel = ref(false)
const fieldSearchText = ref('')
const printData = ref({})



// 计算属性：过滤后的主字段
const filteredMainFields = computed(() => {
	if (!Array.isArray(dynamicFields.value)) return []
	return dynamicFields.value.filter(field => field.fieldScope === 'MAIN')
})

// 计算属性：过滤后的明细字段
const filteredDetailFields = computed(() => {
	if (!Array.isArray(dynamicFields.value)) return []
	return dynamicFields.value.filter(field => field.fieldScope === 'DETAIL')
})

// 计算属性：过滤后的主字段（增加搜索功能）
const filteredMainFieldsWithSearch = computed(() => {
	if (!Array.isArray(filteredMainFields.value)) return []
	return filteredMainFields.value.filter((field) => {
		return !fieldSearchText.value ||
			field.title.toLowerCase().includes(fieldSearchText.value.toLowerCase()) ||
			field.fieldKey.toLowerCase().includes(fieldSearchText.value.toLowerCase())
	})
})

// 计算属性：过滤后的明细字段（增加搜索功能）
const filteredDetailFieldsWithSearch = computed(() => {
	if (!Array.isArray(filteredDetailFields.value)) return []
	return filteredDetailFields.value.filter((field) => {
		return !fieldSearchText.value ||
			field.title.toLowerCase().includes(fieldSearchText.value.toLowerCase()) ||
			field.fieldKey.toLowerCase().includes(fieldSearchText.value.toLowerCase())
	})
})

// 设计器初始化完成事件 - 按照官方实例
const onDesigned = (e) => {
	console.log('设计器初始化完成:', e)
	const {hiprint, designerUtils} = e.detail
	
	// 核心工具类, 单例对象
	utils.value = designerUtils
	console.log('设计器工具类:', designerUtils)
	
	// 标记设计器就绪
	isDesignerReady.value = true
	
	// 加载模板数据
	const currentTemplateId = templateId || route.query.id
	if (currentTemplateId) {
		loadTemplateData(currentTemplateId)
		loadDynamicFields(currentTemplateId)
	}
}

// 模板变化事件处理
const handleTemplateChange = (template) => {
	console.log('[designer.vue] 模板发生变化:', template)
}

// 复制字段属性名到剪贴板
const copyFieldKey = async (field) => {
	try {
		const fieldKey = field.fieldKey

		// 使用现代浏览器的Clipboard API
		if (navigator.clipboard && window.isSecureContext) {
			await navigator.clipboard.writeText(fieldKey)
			message.success(`已复制字段属性名：${fieldKey}`)
		} else {
			// 降级方案：使用传统的document.execCommand方法
			const textArea = document.createElement('textarea')
			textArea.value = fieldKey
			textArea.style.position = 'fixed'
			textArea.style.left = '-999999px'
			textArea.style.top = '-999999px'
			document.body.appendChild(textArea)
			textArea.focus()
			textArea.select()

			try {
				document.execCommand('copy')
				message.success(`已复制字段属性名：${fieldKey}`)
			} catch (err) {
				message.error('复制失败，请手动复制')
			}

			document.body.removeChild(textArea)
		}
	} catch (error) {
		message.error('复制失败: ' + error.message)
	}
}

// 加载模板数据
const loadTemplateData = async (templateId) => {
	if (!templateId) return

	try {
		const response = await templateApi.templateDetail({id: templateId})
		if (!response) return

		// 更新模板信息
		templateInfo.value = {
			name: response.name || '未知模板',
			code: response.code || ''
		}

		// 解析模板数据
		let template = {}
		if (response.templateContent) {
			try {
				template = JSON.parse(response.templateContent)
			} catch (parseError) {
				message.error('模板内容格式错误')
				return
			}
		}

		// 保存模板数据
		templateData.value = template

		// 使用官方的 update 方法更新设计器
		if (utils.value && Object.keys(template).length > 0) {
			console.log('更新设计器模板数据')
			utils.value.printTemplate.update(template)
		}

	} catch (error) {
		message.error('加载模板数据失败: ' + error.message)
	}
}

// 加载动态字段
const loadDynamicFields = async (templateId) => {
	if (!templateId) return

	loadingFields.value = true
	try {
		const [mainFields, detailFields] = await Promise.all([
			fieldApi.fieldPage({
				templateId,
				fieldScope: 'MAIN',
				current: 1,
				size: 1000
			}),
			fieldApi.fieldPage({
				templateId,
				fieldScope: 'DETAIL',
				current: 1,
				size: 1000
			})
		])

		dynamicFields.value = [
			...(mainFields?.records || []),
			...(detailFields?.records || [])
		]
	} catch (error) {
		message.error('加载动态字段失败: ' + error.message)
	} finally {
		loadingFields.value = false
	}
}

// 保存模板处理函数
const handleSave = async () => {
	if (!utils.value) {
		message.error('设计器未初始化')
		return
	}
	
	try {
		// 获取模板JSON
		const templateJson = utils.value.printTemplate.getJson()
		if (!templateJson) {
			throw new Error('获取模板数据失败')
		}

		const saveData = {
			id: templateId,
			templateContent: JSON.stringify(templateJson)
		}

		await templateApi.handleDesign(saveData)
		message.success('保存模板成功')
		
	} catch (error) {
		message.error('保存模板失败: ' + error.message)
	}
}

// 预览模板处理函数
const handlePreview = () => {
	if (!utils.value) {
		message.error('设计器未初始化')
		return
	}
	
	try {
		// 更新打印数据
		utils.value.printData = { 
			name: '微企胜测试',
			title: '预览标题',
			date: new Date().toLocaleDateString()
		}
		
		// 显示预览
		if (utils.value.preview?.show) {
			utils.value.preview.show(utils.value.printData)
		}
	} catch (error) {
		message.error('预览失败: ' + error.message)
	}
}

// 组件挂载时设置模板信息
onMounted(() => {
	if (templateName) {
		templateInfo.value.name = templateName
	}
})
</script>

<style scoped>
.header {
	height: 50px;
	line-height: 50px;
	display: flex;
}

.header .left {
	flex: 1;
	display: flex;
	margin-left: 10px;
}

.header .logo {
	display: flex;
	align-items: center;
	margin-right: 15px;
}

.header .title {
	font-size: 18px;
	font-weight: bold;
}

.header .right {
	text-align: right;
	width: 500px;
	margin-right: 10px;
}

.template-designer-page {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: #f5f5f5;
}

.designer-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 16px 24px;
	background: #fff;
	border-bottom: 1px solid #e8e8e8;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	flex-shrink: 0;
}

.header-left h2 {
	margin: 0;
	font-size: 18px;
	color: #262626;
}

.template-code {
	color: #8c8c8c;
	font-size: 12px;
	margin-left: 12px;
}

.designer-container {
	flex: 1;
	overflow: hidden;
	margin: 16px;
	background: #fff;
	border-radius: 6px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 字段面板样式 - 优化为紧凑布局 */
.field-panel-drawer {
	position: absolute !important;
}

.field-panel-content {
	height: 100%;
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.field-search {
	flex-shrink: 0;
}

.field-groups {
	flex: 1;
	overflow-y: auto;
	padding-right: 4px;
}

.field-group {
	margin-bottom: 16px;
}

.group-title {
	font-weight: 600;
	font-size: 13px;
	color: #262626;
	margin-bottom: 8px;
	padding: 6px 0;
	border-bottom: 1px solid #f0f0f0;
	display: flex;
	align-items: center;
	gap: 6px;
}

/* 网格布局 - 紧凑排列 */
.field-grid {
	display: grid;
	grid-template-columns: 1fr;
	gap: 6px;
}

.field-card {
	padding: 8px 10px;
	border: 1px solid #e8e8e8;
	border-radius: 4px;
	cursor: copy;
	transition: all 0.2s;
	background: #fafafa;
	min-height: 50px;
	display: flex;
	flex-direction: column;
	justify-content: center;
	position: relative;
}

.field-card::after {
	content: '📋';
	position: absolute;
	top: 4px;
	right: 4px;
	font-size: 10px;
	opacity: 0.5;
	transition: opacity 0.2s;
}

.field-card:hover {
	border-color: #1890ff;
	background: #f6ffed;
	transform: translateY(-1px);
	box-shadow: 0 2px 6px rgba(24, 144, 255, 0.15);
}

.field-card:hover::after {
	opacity: 1;
	color: #1890ff;
}

.field-name {
	font-weight: 500;
	color: #262626;
	font-size: 13px;
	line-height: 1.2;
	margin-bottom: 4px;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.field-meta {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 6px;
}

.field-key {
	color: #8c8c8c;
	font-size: 11px;
	font-family: 'Courier New', monospace;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	flex: 1;
	min-width: 0;
}

.field-actions-bar {
	flex-shrink: 0;
	padding-top: 12px;
	border-top: 1px solid #f0f0f0;
}

.empty-state {
	text-align: center;
	padding: 30px 20px;
	color: #8c8c8c;
}

/* 滚动条样式优化 */
.field-groups::-webkit-scrollbar {
	width: 4px;
}

.field-groups::-webkit-scrollbar-track {
	background: #f1f1f1;
	border-radius: 2px;
}

.field-groups::-webkit-scrollbar-thumb {
	background: #c1c1c1;
	border-radius: 2px;
}

.field-groups::-webkit-scrollbar-thumb:hover {
	background: #a8a8a8;
}

.field-list {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.field-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 12px;
	border: 1px solid #e8e8e8;
	border-radius: 6px;
	cursor: pointer;
	transition: all 0.2s;
	background: #fafafa;
}

.field-item:hover {
	border-color: #1890ff;
	background: #f6ffed;
	transform: translateY(-1px);
	box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.field-info {
	flex: 1;
	min-width: 0;
}

.field-title {
	font-weight: 500;
	color: #262626;
	margin-bottom: 4px;
	font-size: 14px;
}

.field-key {
	color: #8c8c8c;
	font-size: 12px;
	font-family: 'Courier New', monospace;
	margin-bottom: 6px;
}

.field-type {
	display: flex;
	align-items: center;
}

.field-actions {
	margin-left: 12px;
}

.field-actions-bar {
	margin-top: 16px;
	padding-top: 16px;
	border-top: 1px solid #f0f0f0;
	text-align: center;
}

.empty-state {
	text-align: center;
	padding: 40px 20px;
	color: #8c8c8c;
}

/* 深度样式覆盖，确保设计器正常显示 */
:deep(.sv-print-designer) {
	height: 100%;
	border-radius: 6px;
	overflow: hidden;
}

:deep(.sv-print-designer .designer-content) {
	height: calc(100% - 60px);
}

:deep(.sv-print-designer .designer-toolbar) {
	background: #fafafa;
	border-bottom: 1px solid #e8e8e8;
}

:deep(.sv-print-designer .designer-panel) {
	border-right: 1px solid #e8e8e8;
}

:deep(.sv-print-designer .designer-canvas) {
	background: #f0f0f0;
}

/* 加载状态样式 */
.loading-container {
	height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #f5f5f5;
}

/* 抽屉样式调整 */
:deep(.ant-drawer-body) {
	padding: 16px;
}

:deep(.ant-drawer-header) {
	border-bottom: 1px solid #f0f0f0;
}
</style>
