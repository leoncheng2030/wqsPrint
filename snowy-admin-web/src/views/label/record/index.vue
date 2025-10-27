<template>
	<a-card :bordered="false" class="xn-mb10">
		<a-form ref="searchFormRef" name="advanced_search" class="ant-advanced-search-form" :model="searchFormState">
			<a-row :gutter="24">
				<a-col :span="6">
					<a-form-item name="templateName" label="模板名称">
						<a-input v-model:value="searchFormState.templateName" placeholder="请输入模板名称" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :span="6">
					<a-form-item name="businessKey" label="业务标识">
						<a-input v-model:value="searchFormState.businessKey" placeholder="请输入业务标识（如订单号）" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :span="6">
					<a-form-item name="printStatus" label="打印状态">
						<a-select
							v-model:value="searchFormState.printStatus"
							placeholder="请选择打印状态"
							allow-clear
							:getPopupContainer="(trigger) => trigger.parentNode"
						>
							<a-select-option v-for="item in printStatusOptions" :key="item.value" :value="item.value">
								{{ item.label }}
							</a-select-option>
						</a-select>
					</a-form-item>
				</a-col>
				<a-col :span="6">
					<a-button type="primary" @click="tableRef.refresh(true)">
						<template #icon><SearchOutlined /></template>
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
		<s-table
			ref="tableRef"
			:columns="columns"
			:data="loadData"
			:expand-row-by-click="true"
			bordered
			:alert="options.alert.show"
			:tool-config="toolConfig"
			:row-key="(record) => record.id"
			:row-selection="options.rowSelection"
		>
			<template #operator class="table-operator">
				<a-space>
					<a-button type="primary" @click="formRef.onOpen(undefined)" v-if="hasPerm('labelRecordAdd')">
						<template #icon><plus-outlined /></template>
						<span>新增打印记录</span>
					</a-button>
					<a-button @click="exportBatchRecordVerify" v-if="hasPerm('labelRecordBatchExport')">
						<template #icon><export-outlined /></template>
						批量导出
					</a-button>
					<xn-batch-button
						v-if="hasPerm('labelRecordBatchDelete')"
						buttonName="批量删除"
						icon="DeleteOutlined"
						buttonDanger
						:selectedRowKeys="selectedRowKeys"
						@batchCallBack="deleteBatchRecord"
					/>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'printStatus'">
					<a-tag :color="getPrintStatusColor(record.printStatus)">
						{{ getPrintStatusText(record.printStatus) }}
					</a-tag>
				</template>
				<template v-if="column.dataIndex === 'printData'">
					<a-tooltip :title="record.printData">
						<span>{{ record.printData ? record.printData.substring(0, 50) + '...' : '' }}</span>
					</a-tooltip>
				</template>
				<template v-if="column.dataIndex === 'action'">
					<a @click="handlePreview(record)" v-if="hasPerm('labelRecordPreview')">预览</a>
					<a-divider type="vertical" v-if="hasPerm(['labelRecordPreview', 'labelRecordEdit'], 'and')" />
					<a @click="formRef.onOpen(record)" v-if="hasPerm('labelRecordEdit')">编辑</a>
					<a-divider type="vertical" v-if="hasPerm(['labelRecordEdit', 'labelRecordAdd'], 'and')" />
					<a @click="copyRecord(record)" v-if="hasPerm('labelRecordAdd')">复制</a>
					<a-divider type="vertical" v-if="hasPerm(['labelRecordAdd', 'labelRecordDelete'], 'and')" />
					<a-popconfirm title="确定要删除这条打印记录吗？" @confirm="removeRecord(record)">
						<a-button type="link" danger size="small" v-if="hasPerm('labelRecordDelete')"> 删除 </a-button>
					</a-popconfirm>
					<a-divider type="vertical" v-if="hasPerm(['labelRecordDelete', 'labelRecordExport'], 'and')" />
					<a @click="exportRecordInfo(record)" v-if="hasPerm('labelRecordExport')">导出</a>
				</template>
			</template>
		</s-table>
	</a-card>
	<Form ref="formRef" @successful="tableRef.refresh()" />
	<Preview ref="previewRef" />
</template>

<script setup name="labelRecord">
	import { ref } from 'vue'
	import { message } from 'ant-design-vue'
	import tool from '@/utils/tool'
	import downloadUtil from '@/utils/downloadUtil'
	import { hasPerm } from '@/utils/permission'
	import recordApi from '@/api/label/recordApi'
	import Form from './form.vue'
	import Preview from './components/preview.vue'

	// 表格列定义
	const columns = [
		{
			title: '业务标识',
			dataIndex: 'businessKey',
			width: '150px',
			ellipsis: true
		},
		{
			title: '打印模板',
			dataIndex: 'templateName',
			width: '120px',
			ellipsis: true
		},
		{
			title: '打印状态',
			dataIndex: 'printStatus',
			width: '100px',
			align: 'center'
		},
		{
			title: '打印次数',
			dataIndex: 'printCount',
			width: '100px',
			align: 'center'
		},
		{
			title: '创建时间',
			dataIndex: 'createTime',
			width: '150px',
			sorter: true
		}
	]

	// 如果有相关权限，添加操作列
	if (hasPerm(['labelRecordDetail', 'labelRecordEdit', 'labelRecordAdd', 'labelRecordDelete', 'labelRecordExport'])) {
		columns.push({
			title: '操作',
			dataIndex: 'action',
			align: 'center',
			width: '250px',
			fixed: 'right'
		})
	}

	// 工具栏配置
	const toolConfig = { refresh: true, height: true, columnSetting: true }

	// 打印状态选项
	const printStatusOptions = [
		{ label: '已完成', value: 'COMPLETED' },
		{ label: '失败', value: 'FAILED' }
	]

	// 响应式数据
	const searchFormRef = ref()
	const searchFormState = ref({})
	const tableRef = ref(null)
	const selectedRowKeys = ref([])
	const formRef = ref(null)
	const previewRef = ref(null)

	// 表格查询 返回 Promise 对象
	const loadData = (parameter) => {
		return recordApi.recordPage(Object.assign(parameter, searchFormState.value)).then((res) => {
			return res
		})
	}

	// 重置搜索表单
	const reset = () => {
		searchFormRef.value.resetFields()
		tableRef.value.refresh(true)
	}

	// 列表选择配置
	const options = {
		alert: {
			show: false,
			clear: () => {
				selectedRowKeys.value = ref([])
			}
		},
		rowSelection: {
			onChange: (selectedRowKey, selectedRows) => {
				selectedRowKeys.value = selectedRowKey
			}
		}
	}
	const handlePreview = (record) => {
		if (previewRef.value) {
			previewRef.value.onOpen(record)
		} else {
			message.error('预览组件未加载')
		}
	}
	// 获取打印状态颜色
	const getPrintStatusColor = (status) => {
		switch (status) {
			case 'COMPLETED':
				return 'green'
			case 'FAILED':
				return 'red'
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
			default:
				return '未知'
		}
	}

	// 查看打印记录详情
	const viewRecord = (record) => {
		// 可以打开一个详情弹窗或跳转到详情页面
		// 这里可以实现查看详情的逻辑
	}

	// 删除打印记录
	const removeRecord = (record) => {
		let params = [
			{
				id: record.id
			}
		]
		recordApi.recordDelete(params).then(() => {
			message.success('删除成功')
			tableRef.value.refresh()
		})
	}

	// 批量导出校验并加参数
	const exportBatchRecordVerify = () => {
		if (
			selectedRowKeys.value.length < 1 &&
			!searchFormState.value.templateName &&
			!searchFormState.value.businessKey &&
			!searchFormState.value.printStatus
		) {
			message.warning('请输入查询条件或勾选要导出的记录')
			return
		}

		if (selectedRowKeys.value.length > 0) {
			const params = {
				recordIds: selectedRowKeys.value.join(',')
			}
			exportBatchRecord(params)
			return
		}

		if (searchFormState.value.templateName || searchFormState.value.businessKey || searchFormState.value.printStatus) {
			const params = {
				templateName: searchFormState.value.templateName,
				businessKey: searchFormState.value.businessKey,
				printStatus: searchFormState.value.printStatus
			}
			exportBatchRecord(params)
		}
	}

	// 批量导出
	const exportBatchRecord = (params) => {
		recordApi.recordExport(params).then((res) => {
			downloadUtil.resultDownload(res)
			tableRef.value.clearSelected()
			message.success('导出成功')
		})
	}

	// 批量删除
	const deleteBatchRecord = (params) => {
		recordApi.recordDelete(params).then(() => {
			message.success('批量删除成功')
			tableRef.value.clearRefreshSelected()
		})
	}

	// 导出单个记录信息
	const exportRecordInfo = (record) => {
		const params = {
			id: record.id
		}
		recordApi.recordExport(params).then((res) => {
			downloadUtil.resultDownload(res)
			message.success('导出成功')
		})
	}

	// 复制打印记录
	const copyRecord = (record) => {
		// 创建复制的记录数据，移除id和创建时间等字段
		const copyData = {
			templateId: record.templateId,
			templateName: record.templateName,
			businessKey: record.businessKey + '_copy_' + Date.now(),
			printData: record.printData,
			printStatus: 'COMPLETED',
			printCount: 0
		}

		// 打开表单并传入复制的数据
		formRef.value.onOpen(copyData, true)
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
