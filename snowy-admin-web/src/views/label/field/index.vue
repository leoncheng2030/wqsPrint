<template>
	<a-card :bordered="false" class="xn-mb10">
		<a-form ref="searchFormRef" name="advanced_search" class="ant-advanced-search-form" :model="searchFormState">
			<a-row :gutter="24">
				<a-col :span="8">
					<a-form-item name="searchKey" :label="$t('common.searchKey')">
						<a-input
							v-model:value="searchFormState.searchKey"
							:placeholder="$t('field.placeholderNameAndSearchKey')"
						/>
					</a-form-item>
				</a-col>
				<a-col :span="8">
					<a-form-item name="fieldStatus" :label="$t('field.fieldStatus')">
						<a-select
							v-model:value="searchFormState.fieldStatus"
							:placeholder="$t('field.placeholderFieldStatus')"
							:getPopupContainer="(trigger) => trigger.parentNode"
						>
							<a-select-option v-for="item in statusData" :key="item.value" :value="item.value">{{
								item.label
							}}</a-select-option>
						</a-select>
					</a-form-item>
				</a-col>
				<a-col :span="8">
					<a-button type="primary" @click="tableRef.refresh(true)">
						<template #icon><SearchOutlined /></template>
						{{ $t('common.searchButton') }}
					</a-button>
					<a-button class="snowy-button-left" @click="reset">
						<template #icon><redo-outlined /></template>
						{{ $t('common.resetButton') }}
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
					<a-button
						type="primary"
						@click="formRef.onOpen(undefined)"
						v-if="hasPerm('bizFieldAdd')"
					>
						<template #icon><plus-outlined /></template>
						<span>{{ $t('common.addButton') }}{{ $t('model.field') }}</span>
					</a-button>
					<a-button @click="exportBatchFieldVerify" v-if="hasPerm('bizFieldBatchExport')">
						<template #icon><export-outlined /></template>
						{{ $t('field.batchExportButton') }}
					</a-button>
					<xn-batch-button
						v-if="hasPerm('bizFieldBatchDelete')"
						:buttonName="$t('common.batchRemoveButton')"
						icon="DeleteOutlined"
						buttonDanger
						:selectedRowKeys="selectedRowKeys"
						@batchCallBack="deleteBatchField"
					/>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'fieldStatus'">
					<a-switch
						:loading="loading"
						:checked="record.fieldStatus === 'ENABLE'"
						@change="editStatus(record)"
						v-if="hasPerm('bizFieldUpdataStatus')"
					/>
					<span v-else>{{ $TOOL.dictTypeData('COMMON_STATUS', record.fieldStatus) }}</span>
				</template>
				<template v-if="column.dataIndex === 'action'">
					<a @click="formRef.onOpen(record)" v-if="hasPerm('bizFieldEdit')">{{ $t('common.editButton') }}</a>
					<a-divider type="vertical" v-if="hasPerm(['bizFieldEdit', 'bizFieldDelete'], 'and')" />
					<a-popconfirm :title="$t('field.popconfirmDeleteField')" @confirm="removeField(record)">
						<a-button type="link" danger size="small" v-if="hasPerm('bizFieldDelete')">{{
							$t('common.removeButton')
						}}</a-button>
					</a-popconfirm>
					<a-divider
						type="vertical"
						v-if="hasPerm(['bizFieldGrantRole', 'bizFieldPwdReset', 'bizFieldExportFieldInfo'])"
					/>
					<a-dropdown v-if="hasPerm(['bizFieldGrantRole', 'bizFieldPwdReset', 'bizFieldExportFieldInfo'])">
						<a class="ant-dropdown-link">
							{{ $t('common.more') }}
							<DownOutlined />
						</a>
						<template #overlay>
							<a-menu>
								<a-menu-item v-if="hasPerm('bizFieldPwdReset')">
									<a-popconfirm
										:title="$t('field.popconfirmResatFieldPwd')"
										placement="topRight"
										@confirm="resetPassword(record)"
									>
										<a>{{ $t('field.resetPassword') }}</a>
									</a-popconfirm>
								</a-menu-item>
								<a-menu-item v-if="hasPerm('bizFieldGrantRole')">
									<a @click="selectRole(record)">{{ $t('field.grantRole') }}</a>
								</a-menu-item>
								<a-menu-item v-if="hasPerm('bizFieldExportFieldInfo')">
									<a @click="exportFieldInfo(record)">{{ $t('field.exportFieldInfo') }}</a>
								</a-menu-item>
							</a-menu>
						</template>
					</a-dropdown>
				</template>
			</template>
		</s-table>
	</a-card>
	<Form ref="formRef" @successful="tableRef.refresh()" />
</template>
<script setup name="bizField">
	import { message } from 'ant-design-vue'
	import tool from '@/utils/tool'
	import downloadUtil from '@/utils/downloadUtil'
	import fieldApi from '@/api/label/fieldApi'
	import Form from './form.vue'

	const columns = [
		{
			title: 'ID',
			dataIndex: 'id',
			width: '80px'
		},
		{
			title: '名称',
			dataIndex: 'name',
			ellipsis: true
		},
		{
			title: '状态',
			dataIndex: 'fieldStatus',
			width: '80px'
		}
	]
	if (hasPerm(['bizFieldEdit', 'bizFieldGrantRole', 'bizFieldPwdReset', 'bizFieldExportFieldInfo', 'bizFieldDelete'])) {
		columns.push({
			title: '操作',
			dataIndex: 'action',
			align: 'center',
			width: '220px'
		})
	}
	const toolConfig = { refresh: true, height: true, columnSetting: true }
	const statusData = tool.dictList('COMMON_STATUS')
	const searchFormRef = ref()
	const searchFormState = ref({})
	const tableRef = ref(null)
	const selectedRowKeys = ref([])
	const formRef = ref(null)
	const loading = ref(false)
	// 表格查询 返回 Promise 对象
	const loadData = (parameter) => {
		return fieldApi.fieldPage(Object.assign(parameter, searchFormState.value)).then((res) => {
			return res
		})
	}
	// 重置
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
	// 修改状态
	const editStatus = (record) => {
		loading.value = true
		if (record.fieldStatus === 'ENABLE') {
			fieldApi
				.fieldDisableField(record)
				.then(() => {
					tableRef.value.refresh()
				})
				.finally(() => {
					loading.value = false
				})
		} else {
			fieldApi
				.fieldEnableField(record)
				.then(() => {
					tableRef.value.refresh()
				})
				.finally(() => {
					loading.value = false
				})
		}
	}
	// 删除人员
	const removeField = (record) => {
		let params = [
			{
				id: record.id
			}
		]
		fieldApi.fieldDelete(params).then(() => {
			tableRef.value.refresh()
		})
	}
	// 批量导出校验并加参数
	const exportBatchFieldVerify = () => {
		if ((selectedRowKeys.value.length < 1) & !searchFormState.value.searchKey & !searchFormState.value.fieldStatus) {
			message.warning('请输入查询条件或勾选要导出的信息')
		}
		if (selectedRowKeys.value.length > 0) {
			const params = {
				fieldIds: selectedRowKeys.value
					.map((m) => {
						return m
					})
					.join()
			}
			exportBatchField(params)
			return
		}
		if (searchFormState.value.searchKey || searchFormState.value.fieldStatus) {
			const params = {
				searchKey: searchFormState.value.searchKey,
				fieldStatus: searchFormState.value.fieldStatus
			}
			exportBatchField(params)
		}
	}
	// 批量导出
	const exportBatchField = (params) => {
		fieldApi.fieldExport(params).then((res) => {
			downloadUtil.resultDownload(res)
			tableRef.value.clearSelected()
		})
	}
	// 批量删除
	const deleteBatchField = (params) => {
		fieldApi.fieldDelete(params).then(() => {
			tableRef.value.clearRefreshSelected()
		})
	}
	// 重置人员密码

</script>

<style scoped>
	.ant-form-item {
		margin-bottom: 0 !important;
	}
	.snowy-table-avatar {
		margin-top: -10px;
		margin-bottom: -10px;
	}
	.snowy-button-left {
		margin-left: 8px;
	}
</style>
