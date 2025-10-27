<template>
	<a-card :bordered="false">
		<s-table
			ref="tableRef"
			:columns="columns"
			:data="loadData"
			:alert="options.alert.show"
			bordered
			:row-key="(record) => record.id"
			:tool-config="toolConfig"
			:row-selection="options.rowSelection"
		>
			<template #operator class="table-operator">
				<a-space>
					<a-button type="primary" @click="formRef.onOpen()" v-if="hasPerm('wqsCodeRuleAdd')">
						<template #icon><plus-outlined /></template>
						新增
					</a-button>
					<a-button @click="importModelRef.onOpen()" v-if="hasPerm('wqsCodeRuleImport')">
						<template #icon><import-outlined /></template>
						<span>导入</span>
					</a-button>
					<a-button @click="exportData" v-if="hasPerm('wqsCodeRuleExport')">
						<template #icon><export-outlined /></template>
						<span>导出</span>
					</a-button>
					<xn-batch-button
						v-if="hasPerm('wqsCodeRuleBatchDelete')"
						buttonName="批量删除"
						icon="DeleteOutlined"
						buttonDanger
						:selectedRowKeys="selectedRowKeys"
						@batchCallBack="deleteBatchWqsCodeRule"
					/>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'status'">
					{{ $TOOL.dictTypeData('COMMON_STATUS', record.status) }}
				</template>
				<template v-if="column.dataIndex === 'action'">
					<a-space>
						<a @click="formRef.onOpen(record)" v-if="hasPerm('wqsCodeRuleEdit')">编辑</a>
						<a-divider type="vertical" v-if="hasPerm(['wqsCodeRuleEdit', 'wqsCodeRuleDelete'], 'and')" />
						<a-popconfirm title="确定要删除吗？" @confirm="deleteWqsCodeRule(record)">
							<a-button type="link" danger size="small" v-if="hasPerm('wqsCodeRuleDelete')">删除</a-button>
						</a-popconfirm>
					</a-space>
				</template>
			</template>
		</s-table>
	</a-card>
	<ImportModel ref="importModelRef" />
	<Form ref="formRef" @successful="tableRef.refresh()" />
</template>

<script setup name="coderule">
	import { cloneDeep } from 'lodash-es'
	import Form from './form.vue'
	import ImportModel from './importModel.vue'
	import downloadUtil from '@/utils/downloadUtil'
	import wqsCodeRuleApi from '@/api/barcode/wqsCodeRuleApi'
	const tableRef = ref()
	const importModelRef = ref()
	const formRef = ref()
	const toolConfig = { refresh: true, height: true, columnSetting: true, striped: false }
	const columns = [
		{
			title: '规则名称',
			dataIndex: 'ruleName'
		},
		{
			title: '规则编码',
			dataIndex: 'ruleCode'
		},
		{
			title: '规则描述',
			dataIndex: 'description'
		},
		{
			title: '状态',
			dataIndex: 'status'
		}
	]
	// 操作栏通过权限判断是否显示
	if (hasPerm(['wqsCodeRuleEdit', 'wqsCodeRuleDelete'])) {
		columns.push({
			title: '操作',
			dataIndex: 'action',
			align: 'center',
			width: 150
		})
	}
	const selectedRowKeys = ref([])
	// 列表选择配置
	const options = {
		// columns数字类型字段加入 needTotal: true 可以勾选自动算账
		alert: {
			show: true,
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
	const loadData = (parameter) => {
		return wqsCodeRuleApi.wqsCodeRulePage(parameter).then((data) => {
			return data
		})
	}
	// 重置
	const reset = () => {
		searchFormRef.value.resetFields()
		tableRef.value.refresh(true)
	}
	// 删除
	const deleteWqsCodeRule = (record) => {
		let params = [
			{
				id: record.id
			}
		]
		wqsCodeRuleApi.wqsCodeRuleDelete(params).then(() => {
			tableRef.value.refresh(true)
		})
	}
	// 导出
	const exportData = () => {
		if (selectedRowKeys.value.length > 0) {
			const params = selectedRowKeys.value.map((m) => {
				return {
					id: m
				}
			})
			wqsCodeRuleApi.wqsCodeRuleExport(params).then((res) => {
				downloadUtil.resultDownload(res)
			})
		} else {
			wqsCodeRuleApi.wqsCodeRuleExport([]).then((res) => {
				downloadUtil.resultDownload(res)
			})
		}
	}
	// 批量删除
	const deleteBatchWqsCodeRule = (params) => {
		wqsCodeRuleApi.wqsCodeRuleDelete(params).then(() => {
			tableRef.value.clearRefreshSelected()
		})
	}
</script>
