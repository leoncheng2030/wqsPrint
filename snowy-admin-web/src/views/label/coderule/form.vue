<template>
	<xn-form-container
		:title="formData.id ? '编辑编码规则' : '新增编码规则'"
		:width="1200"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
			<!-- 基础信息 -->
			<a-card title="基础信息" size="small" style="margin-bottom: 16px">
				<a-row :gutter="16">
					<a-col :span="8">
						<a-form-item label="规则名称" name="ruleName">
							<a-input v-model:value="formData.ruleName" placeholder="请输入规则名称" allow-clear />
						</a-form-item>
					</a-col>
					<a-col :span="8">
						<a-form-item label="规则编码" name="ruleCode">
							<a-input-group compact>
								<a-input
									v-model:value="formData.ruleCode"
									placeholder="请输入规则编码"
									allow-clear
									style="width: calc(100% - 80px)"
								/>
								<a-button type="primary" @click="generateRuleCode" :loading="generateLoading" style="width: 80px">
									自动生成
								</a-button>
							</a-input-group>
						</a-form-item>
					</a-col>
					<a-col :span="8">
						<a-form-item label="状态" name="status">
							<a-select v-model:value="formData.status" placeholder="请选择状态">
								<a-select-option value="ENABLE">启用</a-select-option>
								<a-select-option value="DISABLE">禁用</a-select-option>
							</a-select>
						</a-form-item>
					</a-col>
					<a-col :span="24">
						<a-form-item label="规则描述" name="description">
							<a-textarea v-model:value="formData.description" placeholder="请输入规则描述" :rows="2" />
						</a-form-item>
					</a-col>
				</a-row>
			</a-card>

			<!-- 编码段设计器 -->
			<a-card title="编码段设计器" size="small" style="margin-bottom: 16px">
				<div class="segment-designer">
					<!-- 编码段类型工具栏 -->
					<div class="segment-toolbar">
						<h4>编码段类型</h4>
						<div class="segment-types">
							<div v-for="type in segmentTypes" :key="type.key" class="segment-type-item" @click="addSegment(type.key)">
								<component :is="type.icon" class="segment-icon" />
								<span>{{ type.label }}</span>
							</div>
						</div>
					</div>

					<!-- 编码段配置区域 -->
					<div class="segment-workspace">
						<h4>
							编码段配置 <span class="preview-text">预览: {{ previewCode }}</span>
						</h4>
						<div class="segment-config-zone">
							<div v-if="segments.length === 0" class="config-placeholder">点击上方编码段类型开始设计</div>
							<draggable
								v-model="segments"
								item-key="id"
								class="segment-list"
								ghost-class="segment-ghost"
								chosen-class="segment-chosen"
								drag-class="segment-drag"
							>
								<template #item="{ element: segment, index }">
									<div class="segment-item-simple">
										<div class="segment-content">
											<!-- 拖拽手柄和类型标识 -->
											<div class="segment-handle">
												<component :is="getSegmentIcon(segment.type)" class="segment-icon" />
												<span class="segment-type">{{ getSegmentLabel(segment.type) }}</span>
											</div>

											<!-- 内联配置区域 -->
											<div class="segment-inline-config">
												<!-- 固定值配置 -->
												<a-input
													v-if="segment.type === 'fixed'"
													v-model:value="segment.value"
													placeholder="固定值"
													size="small"
													allow-clear
												/>

												<!-- 字段值配置 -->
												<a-input
													v-else-if="segment.type === 'field'"
													v-model:value="segment.fieldName"
													placeholder="字段名"
													size="small"
													allow-clear
												/>

												<!-- 日期格式配置 -->
												<a-select
													v-else-if="segment.type === 'date'"
													v-model:value="segment.format"
													placeholder="日期格式"
													size="small"
													style="min-width: 300px"
												>
													<a-select-option value="YYYY">YYYY</a-select-option>
													<a-select-option value="MM">MM</a-select-option>
													<a-select-option value="DD">DD</a-select-option>
													<a-select-option value="YYYYMM">YYYYMM</a-select-option>
													<a-select-option value="YYYYMMDD">YYYYMMDD</a-select-option>
													<a-select-option value="MM_ABC">MM(10/11/12月用A/B/C)</a-select-option>
													<a-select-option value="YYYYMM_ABC">YYYYMM(10/11/12月用A/B/C)</a-select-option>
													<a-select-option value="YYYYMMDDHHmmss">YYYYMMDDHHmmss</a-select-option>
												</a-select>

												<!-- 流水号配置 -->
												<div v-else-if="segment.type === 'serial'" class="serial-config">
													<a-input-number
														v-model:value="segment.length"
														:min="1"
														:max="10"
														placeholder="长度"
														size="small"
														style="width: 60px"
													/>
													<a-input-number
														v-model:value="segment.startValue"
														:min="0"
														placeholder="起始"
														size="small"
														style="width: 60px"
													/>
													<a-select
														v-model:value="segment.resetType"
														placeholder="重置"
														size="small"
														style="width: 80px"
													>
														<a-select-option value="none">不重置</a-select-option>
														<a-select-option value="daily">每日</a-select-option>
														<a-select-option value="monthly">每月</a-select-option>
														<a-select-option value="yearly">每年</a-select-option>
													</a-select>
												</div>

												<!-- 分隔符配置 -->
												<a-input
													v-else-if="segment.type === 'separator'"
													v-model:value="segment.separator"
													placeholder="分隔符"
													size="small"
													style="width: 60px"
													allow-clear
													maxlength="5"
												/>
											</div>

											<!-- 删除按钮 -->
											<div class="segment-actions">
												<a-button size="small" type="text" danger @click="removeSegment(index)">
													<template #icon><DeleteOutlined /></template>
												</a-button>
											</div>
										</div>
									</div>
								</template>
							</draggable>
						</div>
					</div>
				</div>
			</a-card>
		</a-form>

		<template #footer>
			<a-button style="margin-right: 8px" @click="onClose">取消</a-button>
			<a-button type="primary" @click="onSubmit" :loading="submitLoading">保存</a-button>
		</template>
	</xn-form-container>
</template>

<script setup name="wqsCodeRuleForm">
	import { cloneDeep } from 'lodash-es'
	import { required } from '@/utils/formRules'
	import wqsCodeRuleApi from '@/api/barcode/wqsCodeRuleApi'
	import draggable from 'vuedraggable'
	import {
		AppstoreOutlined,
		CalendarOutlined,
		DeleteOutlined,
		FieldStringOutlined,
		LineOutlined,
		NumberOutlined
	} from '@ant-design/icons-vue'
	import { message } from 'ant-design-vue'

	// 抽屉状态
	const open = ref(false)
	const emit = defineEmits({ successful: null })
	const formRef = ref()
	// 表单数据
	const formData = ref({
		status: 'ENABLE'
	})
	const submitLoading = ref(false)
	// 规则编码自动生成加载状态
	const generateLoading = ref(false)

	// 编码段列表
	const segments = ref([])

	// 编码段类型定义
	const segmentTypes = [
		{ key: 'fixed', label: '固定值', icon: AppstoreOutlined, description: '固定不变的字符串' },
		{ key: 'field', label: '字段值', icon: FieldStringOutlined, description: '来自数据字段的值' },
		{ key: 'date', label: '日期格式', icon: CalendarOutlined, description: '格式化的日期时间' },
		{ key: 'serial', label: '流水号', icon: NumberOutlined, description: '自增序列号' },
		{ key: 'separator', label: '分隔符', icon: LineOutlined, description: '分隔各段的字符' }
	]

	// 预览代码
	const previewCode = computed(() => {
		if (segments.value.length === 0) return ''
		return segments.value
			.map((segment) => {
				switch (segment.type) {
					case 'fixed':
						return segment.value || '[固定值]'
					case 'field':
						return `{${segment.fieldName || 'field'}}`
					case 'date':
						// 根据不同的日期格式显示不同的预览
						if (segment.format === 'MM_ABC') {
							return '[MM/A/B/C]'
						} else if (segment.format === 'YYYYMM_ABC') {
							return '[YYYYMM/A/B/C]'
						} else if(segment.format === 'YYYYMMDDHHmmss'){
							return '[YYYYMMDDHHmmss]'
						} else {
							return `[${segment.format || 'YYYY'}]`
						}
					case 'serial':
						return `[${'0'.repeat(segment.length || 3)}]`
					case 'separator':
						return segment.separator || '-'
					default:
						return '[未知]'
				}
			})
			.join('')
	})

	// 添加编码段
	const addSegment = (type) => {
		const newSegment = {
			type,
			id: Date.now() + Math.random(), // 确保每个segment都有唯一的id用于draggable
			// 根据类型设置默认值
			...(type === 'fixed' && { value: '' }),
			...(type === 'field' && { fieldName: '' }),
			...(type === 'date' && { format: 'YYYY' }),
			...(type === 'serial' && { length: 3, startValue: 1, resetType: 'none' }),
			...(type === 'separator' && { separator: '-' })
		}
		segments.value.push(newSegment)
	}

	// 移除编码段
	const removeSegment = (index) => {
		segments.value.splice(index, 1)
	}

	// 获取编码段图标
	const getSegmentIcon = (type) => {
		const typeObj = segmentTypes.find((t) => t.key === type)
		return typeObj ? typeObj.icon : AppstoreOutlined
	}

	// 获取编码段标签
	const getSegmentLabel = (type) => {
		const typeObj = segmentTypes.find((t) => t.key === type)
		return typeObj ? typeObj.label : '未知类型'
	}

	// 自动生成规则编码
	const generateRuleCode = async () => {
		generateLoading.value = true
		try {
			// 生成基于时间戳和随机数的规则编码
			const now = new Date()
			const year = now.getFullYear().toString().slice(-2) // 取年份后两位
			const month = (now.getMonth() + 1).toString().padStart(2, '0') // 月份补零
			const day = now.getDate().toString().padStart(2, '0') // 日期补零
			const random = Math.floor(Math.random() * 1000)
				.toString()
				.padStart(3, '0') // 3位随机数

			// 生成格式：RULE + 年月日 + 随机数，例如：RULE2412251001
			// 设置到表单数据中
			formData.value.ruleCode = `RULE${year}${month}${day}${random}`

			// 提示用户生成成功
			message.success('规则编码生成成功')
		} catch (error) {
			message.error('生成规则编码失败，请重试')
		} finally {
			generateLoading.value = false
		}
	}

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		if (record) {
			let recordData = cloneDeep(record)
			formData.value = Object.assign({}, recordData)
			// 解析segments字段
			if (recordData.segments) {
				try {
					segments.value =
					typeof recordData.segments === 'string' ? JSON.parse(recordData.segments) : recordData.segments
			} catch (e) {
				segments.value = []
				}
			}
		} else {
			// 新增时的默认值
			formData.value = {
				status: 'ENABLE'
			}
			segments.value = []
		}
	}

	// 关闭抽屉
	const onClose = () => {
		formRef.value.resetFields()
		formData.value = { status: 'ENABLE' }
		segments.value = []
		open.value = false
	}

	// 表单验证规则
	const formRules = {
		ruleName: [required('请输入规则名称')],
		ruleCode: [required('请输入规则编码')],
		status: [required('请选择状态')]
	}

	// 验证并提交数据
	const onSubmit = () => {
		if (segments.value.length === 0) {
			message.warning('请至少配置一个编码段')
			return
		}

		formRef.value
			.validate()
			.then(() => {
				submitLoading.value = true
				const formDataParam = cloneDeep(formData.value)
				// 将segments转换为JSON字符串
				formDataParam.segments = JSON.stringify(segments.value)

				wqsCodeRuleApi
					.wqsCodeRuleSubmitForm(formDataParam, formDataParam.id)
					.then(() => {
						onClose()
						emit('successful')
						message.success('保存成功')
					})
					.finally(() => {
						submitLoading.value = false
					})
			})
			.catch(() => {})
	}

	// 抛出函数
	defineExpose({
		onOpen
	})
</script>

<style scoped>
	.segment-designer {
		display: flex;
		flex-direction: column;
		gap: 16px;
		min-height: 400px;
	}

	.segment-toolbar {
		border: 1px solid #d9d9d9;
		border-radius: 6px;
		padding: 16px;
		background: #fafafa;
	}

	.segment-toolbar h4 {
		margin: 0 0 12px 0;
		font-size: 14px;
		color: #262626;
	}

	.segment-types {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
	}

	.segment-type-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 8px 12px;
		border: 1px solid #d9d9d9;
		border-radius: 4px;
		background: white;
		cursor: pointer;
		font-size: 12px;
		min-width: 80px;
	}

	.segment-type-item:hover {
		border-color: #1890ff;
	}

	.segment-type-item:active {
		border-color: #1890ff;
	}

	.segment-icon {
		font-size: 20px;
		margin-bottom: 4px;
		color: #1890ff;
	}

	.segment-workspace {
		flex: 1;
	}

	.segment-workspace h4 {
		margin: 0 0 12px 0;
		font-size: 14px;
		color: #262626;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.preview-text {
		font-size: 12px;
		color: #666;
		font-weight: normal;
		background: #f5f5f5;
		padding: 4px 8px;
		border-radius: 3px;
		font-family: monospace;
	}

	.segment-config-zone {
		min-height: 300px;
		border: 1px solid #d9d9d9;
		border-radius: 6px;
		padding: 16px;
		background: white;
	}

	.config-placeholder {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 268px;
		color: #999;
		font-size: 14px;
	}

	.segment-list {
		min-height: 200px;
	}

	.segment-ghost {
		opacity: 0.5;
	}

	.segment-drag {
		/* 移除旋转动画效果 */
		opacity: 0.8;
	}

	.segment-item-simple {
		margin-bottom: 8px;
		border: 1px solid #d9d9d9;
		border-radius: 6px;
		background: white;
		padding: 8px 12px;
	}

	.segment-content {
		display: flex;
		align-items: center;
		gap: 12px;
		cursor: move;
	}

	.segment-handle {
		display: flex;
		align-items: center;
		gap: 6px;
		min-width: 80px;
		flex-shrink: 0;
	}

	.segment-handle .segment-icon {
		font-size: 14px;
		color: #1890ff;
	}

	.segment-handle .segment-type {
		font-size: 12px;
		font-weight: 500;
		color: #666;
	}

	.segment-inline-config {
		display: flex;
		align-items: center;
		gap: 8px;
		flex: 1;
	}

	.serial-config {
		display: flex;
		align-items: center;
		gap: 6px;
	}

	.segment-actions {
		display: flex;
		gap: 4px;
		flex-shrink: 0;
	}
</style>
