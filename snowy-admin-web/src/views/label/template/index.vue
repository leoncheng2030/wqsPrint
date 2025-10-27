<template>
	<a-card :bordered="false" class="xn-mb10">
		<a-form ref="searchFormRef" name="advanced_search" class="ant-advanced-search-form" :model="searchFormState">
			<a-row :gutter="24">
				<a-col :span="8">
					<a-form-item name="searchKey" label="关键词">
						<a-input v-model:value="searchFormState.searchKey" placeholder="请输入模板名称关键词" />
					</a-form-item>
				</a-col>
				<a-col :span="8">
					<a-button type="primary" @click="tableRef.refresh(true)">
						<template #icon>
							<SearchOutlined />
						</template>
						查询
					</a-button>
					<a-button class="snowy-button-left" @click="reset">
						<template #icon><redo-outlined /></template>
						重置
					</a-button>
				</a-col>
			</a-row>
		</a-form>
	</a-card>
	<a-card :bordered="false">
		<s-table ref="tableRef" :columns="columns" :data="loadData" :expand-row-by-click="true" bordered
			:tool-config="toolConfig" :row-key="(record) => record.id">
			<template #operator class="table-operator">
				<a-space>
					<a-button type="primary" @click="formRef.onOpen()" v-if="hasPerm('labelTemplateAdd')">
						<template #icon><plus-outlined /></template>
						<span>新增模板</span>
					</a-button>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'type'">
					{{ $TOOL.dictTypeData('BUSINESS_TYPE', record.type) }}
				</template>
				<template v-if="column.dataIndex === 'requiresDetails'">
					{{ $TOOL.dictTypeData('COMMON_YES_OR_NO', record.requiresDetails) }}
				</template>
				<template v-if="column.dataIndex === 'status'">
					{{ $TOOL.dictTypeData('COMMON_STATUS', record.status) }}
				</template>
				<template v-if="column.dataIndex === 'action'">
					<a-space>
						<a @click="formRef.onOpen(record)" v-if="hasPerm('labelTemplateEdit')">编辑</a>
						<a-divider type="vertical" v-if="hasPerm(['labelTemplateEdit', 'labelTemplateEdit'], 'and')" />
						<a @click="fieldConfigModalRef.onOpen(record)" v-if="hasPerm('fieldConfig')">字段配置</a>
						<a-divider type="vertical" v-if="hasPerm(['fieldConfig', 'templateDesign'], 'and')" />
						<!-- 修改为新窗口打开 -->
						<a @click="openTemplateDesigner(record)" v-if="hasPerm('templateDesign')">模板设计</a>
						<a-divider type="vertical" v-if="hasPerm(['templateDesign', 'labelTemplateDelete'], 'and')" />
						<a-popconfirm title="确定要删除吗？" @confirm="removeTemplate(record)" v-if="hasPerm('labelTemplateDelete')">
							<a-button type="link" danger size="small">删除</a-button>
						</a-popconfirm>
					</a-space>


				</template>
			</template>
		</s-table>
	</a-card>
	<!-- 模板表单弹窗 -->
	<Form ref="formRef" @successful="tableRef.refresh()" />

	<!-- 字段配置弹窗 -->
	<FieldConfigModal ref="fieldConfigModalRef" />
</template>

<script setup name="labelTemplate">
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import templateApi from '@/api/label/templateApi'
import Form from './form.vue'
import FieldConfigModal from './components/fieldConfigModal.vue'

const router = useRouter()

// 表格列配置 - 标签模板相关字段
const columns = [
	{
		title: '模板名称',
		dataIndex: 'name',
	},
	{
		title: '模板编号',
		dataIndex: 'code',
	},
	{
		title: '模板类型',
		dataIndex: 'type',
		width: '150px'
	},
	{
		title: '支持明细',
		dataIndex: 'requiresDetails',
		width: '120px'
	},
	{
		title: '状态',
		dataIndex: 'status',
		width: '120px'
	},
	{
		title: '创建时间',
		dataIndex: 'createTime',
		width: '180px'
	},
	{
		title: '备注',
		dataIndex: 'remark',
		ellipsis: true
	}
]

// 如果有编辑和删除权限，添加操作列
if (hasPerm(['labelTemplateEdit', 'labelTemplateDelete'])) {
	columns.push({
		title: '操作',
		dataIndex: 'action',
		align: 'center',
		width: '350px'
	})
}

const toolConfig = { refresh: true, height: true, columnSetting: true }
const searchFormRef = ref()
const searchFormState = ref({})
const tableRef = ref(null)
const formRef = ref(null)
const fieldConfigModalRef = ref(null)

// 表格查询 - 调用标签模板API
const loadData = (parameter) => {
	return templateApi.templatePage(Object.assign(parameter, searchFormState.value)).then((res) => {
		return res
	})
}

// 重置搜索表单
const reset = () => {
	searchFormRef.value.resetFields()
	tableRef.value.refresh(true)
}

// 删除模板
const removeTemplate = (record) => {
	let params = [
		{
			id: record.id
		}
	]
	templateApi.templateDelete(params).then(() => {
		tableRef.value.refresh()
		message.success('删除成功')
	})
}




// 打开模板设计器 - 推荐方案
const openTemplateDesigner = (record) => {
	// 构建设计器页面的URL
	const designerUrl = `/label/template/designer?id=${record.id}&name=${encodeURIComponent(record.name)}`
	
	// 窗口特性配置
	const windowFeatures = [
		'width=1600',
		'height=900',
		'left=' + Math.max(0, (screen.width - 1600) / 2),
		'top=' + Math.max(0, (screen.height - 900) / 2),
		'scrollbars=yes',
		'resizable=yes',
		'status=no',
		'toolbar=no',
		'menubar=no',
		'location=no',
		'directories=no',
		'copyhistory=no'
	].join(',')
	
	// 打开新窗口
	const designerWindow = window.open(
		designerUrl,
		'templateDesigner_' + record.id, // 使用唯一的窗口名称
		windowFeatures
	)
	
	// 检查窗口是否成功打开
	if (!designerWindow) {
		message.error('无法打开设计器窗口，请检查浏览器弹窗设置')
		return
	}
	
	// 聚焦到新窗口
	try {
		designerWindow.focus()
	} catch (e) {
		// 忽略聚焦失败
	}
	
	// 监听窗口关闭事件
	const checkClosed = setInterval(() => {
		if (designerWindow.closed) {
			clearInterval(checkClosed)
			tableRef.value.refresh()
			message.success('设计器已关闭，数据已刷新')
		}
	}, 1000)
	
	// 提示用户如果地址栏仍然显示
	setTimeout(() => {
		if (!designerWindow.closed) {
			// 浏览器安全策略限制
		}
	}, 2000)
}


</script>

<style scoped>
.ant-form-item {
	margin-bottom: 0 !important;
}

.snowy-button-left {
	margin-left: 8px;
}
</style>
