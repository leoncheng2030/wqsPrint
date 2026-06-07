<template>
	<!-- 二维码配置 -->
	<div class="section">
		<div class="section-device"></div>
		<div class="section-title">二维码配置</div>
	</div>

	<!-- 基础配置：格式 + 分隔符/JSON类型 + 标识符 -->
	<a-row :gutter="16">
		<a-col :span="6">
			<a-form-item label="格式" name="formatType" :label-col="{ span: 8 }">
				<a-select v-model:value="localConfig.formatType" @change="onFormatTypeChange">
					<a-select-option value="json">JSON</a-select-option>
					<a-select-option value="custom">分隔符</a-select-option>
				</a-select>
			</a-form-item>
		</a-col>
		<a-col :span="6">
			<template v-if="localConfig.formatType === 'json'">
				<a-form-item label="类型" name="jsonDataType" :label-col="{ span: 8 }">
					<a-select v-model:value="localConfig.jsonDataType" @change="updateQrContent">
						<a-select-option value="object">对象</a-select-option>
						<a-select-option value="array">数组</a-select-option>
					</a-select>
				</a-form-item>
			</template>
			<template v-else>
				<a-form-item label="分隔符" name="separator" :label-col="{ span: 8 }">
					<a-input
						v-model:value="localConfig.separator"
						placeholder="如：| 或 ,"
						@change="updateQrContent"
						allow-clear
					/>
				</a-form-item>
			</template>
		</a-col>
		<a-col :span="6">
			<a-form-item label="开始" name="startMarker" :label-col="{ span: 8 }">
				<a-input v-model:value="localConfig.startMarker" placeholder="如：[)>" @change="updateQrContent" allow-clear />
			</a-form-item>
		</a-col>
		<a-col :span="6">
			<a-form-item label="结束" name="endMarker" :label-col="{ span: 8 }">
				<a-input v-model:value="localConfig.endMarker" placeholder="如：EOT" @change="updateQrContent" allow-clear />
			</a-form-item>
		</a-col>
	</a-row>

	<!-- 编码方式 -->
	<a-form-item label="编码方式" name="encodeMode">
		<a-radio-group v-model:value="localConfig.encodeMode" @change="updateQrContent">
			<a-radio value="none">无</a-radio>
			<a-radio value="base64">仅Base64</a-radio>
			<a-radio value="gzip_base64">Gzip+Base64</a-radio>
		</a-radio-group>
		<div class="ant-form-item-explain">
			<small>Gzip+Base64 可大幅缩短二维码内容，推荐用于复杂数据</small>
		</div>
	</a-form-item>

	<!-- 字段配置（按主字段/明细字段分组） -->
	<template v-for="group in fieldTableGroups" :key="group.label">
		<div class="field-table-group">
			<div class="field-table-group-label">{{ group.label }}</div>

			<div class="field-table">
				<div class="field-table-header">
					<span class="field-table-drag-handle"></span>
					<span class="field-table-check"></span>
					<span class="field-table-name">字段名称</span>
					<span v-if="localConfig.formatType === 'custom'" class="field-table-prefix">DI前缀</span>
				</div>

				<div
					v-for="item in group.items"
					:key="item.value"
					class="field-table-row"
					:class="{
						'field-table-row-active': isFieldSelected(item.value),
						'field-table-row-dragging': dragFieldKey === item.value,
						'field-table-row-dragover': dragOverFieldKey === item.value && dragFieldKey !== item.value
					}"
					:draggable="isFieldSelected(item.value)"
					@dragstart="onSortStart(item.value, $event)"
					@dragover.prevent="onSortOver(item.value)"
					@drop="onSortDrop(item.value)"
					@dragend="onSortEnd"
				>
					<span
						class="field-table-drag-handle"
						:class="{ 'field-table-drag-handle-active': isFieldSelected(item.value) }"
						>⠿</span
					>
					<a-checkbox
						class="field-table-check"
						:checked="isFieldSelected(item.value)"
						@change="(e) => toggleField(item.value, e.target.checked)"
					/>
					<span class="field-table-name">
						<template v-if="group.type === 'MERGED'">
							<span
								class="field-table-scope-badge"
								:class="item.fieldScope === 'MAIN' ? 'scope-main' : 'scope-detail'"
							>{{ item.fieldScope === 'MAIN' ? '主' : '明' }}</span>
						</template>
						{{ item.label }}
					</span>
					<template v-if="localConfig.formatType === 'custom'">
						<a-input
							v-if="isFieldSelected(item.value)"
							v-model:value="localConfig.fieldPrefixes[item.value]"
							class="field-table-prefix"
							placeholder="如：W, 1W, S, 1Q"
							@change="updateQrContent"
							allow-clear
							size="small"
						/>
						<span v-else class="field-table-prefix-disabled">—</span>
					</template>
				</div>
			</div>
		</div>

		<!-- 明细字段专属设置 -->
		<template v-if="group.type !== 'MAIN'">
			<template v-if="hasDetailFields">
				<a-row :gutter="16">
					<a-col :span="6">
						<a-form-item label="值分隔符" name="itemValueSeparator" :label-col="{ span: 10 }">
							<a-input
								v-model:value="localConfig.itemValueSeparator"
								placeholder="如：, 或 ;"
								@change="updateQrContent"
								allow-clear
							/>
							<div class="ant-form-item-explain">
								<small>同一条明细内多字段间的分隔符</small>
							</div>
						</a-form-item>
					</a-col>
					<a-col :span="6">
						<a-form-item label="行前缀" name="detailRowPrefix" :label-col="{ span: 10 }">
							<a-input
								v-model:value="localConfig.detailRowPrefix"
								placeholder="如：\\x1E"
								@change="updateQrContent"
								allow-clear
							/>
							<div class="ant-form-item-explain">
								<small>每行明细前添加的前缀（含首行）</small>
							</div>
						</a-form-item>
					</a-col>
					<a-col :span="6">
						<a-form-item label="行分隔符" name="detailRowSeparator" :label-col="{ span: 10 }">
							<a-input
								v-model:value="localConfig.detailRowSeparator"
								placeholder="如：# 或 @"
								@change="updateQrContent"
								allow-clear
							/>
							<div class="ant-form-item-explain">
								<small>多条明细数据之间的行尾连接符</small>
							</div>
						</a-form-item>
					</a-col>
                    <a-col :span="6">
                        <a-form-item label="行号" name="detailRowNumber" :label-col="{ span: 8 }">
						<a-switch v-model:checked="localConfig.detailRowNumber" @change="updateQrContent" />
						<div class="ant-form-item-explain">
							<small>每行结尾拼接行号+行分隔符</small>
						</div>
					</a-form-item>
                    </a-col>
				</a-row>
				<a-form-item label="循环模式" name="detailLoopMode">
					<a-select
						v-model:value="localConfig.detailLoopMode"
						placeholder="请选择明细字段的循环处理模式"
						@change="updateQrContent"
					>
						<a-select-option value="join">连接（明细值用分隔符连接）</a-select-option>
						<a-select-option value="loop">循环（每个明细项生成完整内容）</a-select-option>
						<a-select-option value="first">首项（只取第一个明细项）</a-select-option>
					</a-select>
				</a-form-item>
				<a-form-item v-if="hasBothScopeFields" label="主字段">
					<a-switch v-model:checked="localConfig.detailIncludeMainFields" @change="updateQrContent" />
					<div class="ant-form-item-explain">
						<small>每行明细携带主字段，拖拽字段可调整输出顺序</small>
					</div>
				</a-form-item>
				
			</template>
		</template>
	</template>

	<!-- 内容预览 -->
	<a-form-item v-if="localConfig.previewContent" label="内容预览">
		<div class="content-preview">
			<a-typography-paragraph code copyable>
				{{ localConfig.previewContent }}
			</a-typography-paragraph>
		</div>
	</a-form-item>
</template>

<script setup name="QrCodeConfigPanel">
	import { ref, computed, onMounted } from 'vue'
	import pako from 'pako'

	const props = defineProps({
		config: {
			type: Object,
			default: () => ({})
		},
		availableFieldOptions: {
			type: Array,
			default: () => []
		}
	})

	const emit = defineEmits(['update:config'])

	// 本地配置引用
	const localConfig = computed(() => props.config)

	// 按作用域分组的字段选项（主字段 / 明细字段）
	const fieldTableGroups = computed(() => {
		const main = props.availableFieldOptions.filter((f) => f.fieldScope === 'MAIN')
		const detail = props.availableFieldOptions.filter((f) => f.fieldScope === 'DETAIL')

		// 当携带主字段开启且两种字段都有选中时，合并为统一列表
		const shouldMerge =
			props.config?.detailIncludeMainFields &&
			main.some((m) => props.config?.selectedFields?.includes(m.value)) &&
			detail.some((d) => props.config?.selectedFields?.includes(d.value))

		if (shouldMerge) {
			const selected = props.config?.selectedFields || []
			const merged = [...main, ...detail]
			merged.sort((a, b) => {
				const aIdx = selected.indexOf(a.value)
				const bIdx = selected.indexOf(b.value)
				if (aIdx !== -1 && bIdx !== -1) return aIdx - bIdx
				if (aIdx !== -1) return -1
				if (bIdx !== -1) return 1
				return (a.label || '').localeCompare(b.label || '')
			})
			return [{ label: '字段顺序（拖拽调整）', type: 'MERGED', items: merged }]
		}

		const groups = []
		if (main.length > 0) groups.push({ label: '主字段', type: 'MAIN', items: main })
		if (detail.length > 0) groups.push({ label: '明细字段', type: 'DETAIL', items: detail })
		return groups
	})

	// 检查选中的字段中是否包含明细字段
	const hasDetailFields = computed(() => {
		if (!props.config?.selectedFields) return false
		return props.config.selectedFields.some((fieldKey) => {
			const field = props.availableFieldOptions.find((f) => f.value === fieldKey)
			return field?.fieldScope === 'DETAIL'
		})
	})

	// 检查选中的字段中是否同时包含主字段和明细字段
	const hasBothScopeFields = computed(() => {
		if (!props.config?.selectedFields) return false
		let hasMain = false
		let hasDetail = false
		props.config.selectedFields.forEach((fieldKey) => {
			const field = props.availableFieldOptions.find((f) => f.value === fieldKey)
			if (field?.fieldScope === 'MAIN') hasMain = true
			if (field?.fieldScope === 'DETAIL') hasDetail = true
		})
		return hasMain && hasDetail
	})

	// 检查字段是否已在选中列表中
	const isFieldSelected = (fieldKey) => {
		return props.config?.selectedFields?.includes(fieldKey) ?? false
	}

	// 切换字段选中状态
	const toggleField = (fieldKey, checked) => {
		if (checked) {
			if (!props.config.selectedFields.includes(fieldKey)) {
				// eslint-disable-next-line vue/no-mutating-props
				props.config.selectedFields.push(fieldKey)
			}
		} else {
			const index = props.config.selectedFields.indexOf(fieldKey)
			if (index > -1) {
				// eslint-disable-next-line vue/no-mutating-props
				props.config.selectedFields.splice(index, 1)
			}
		}
		updateQrContent()
	}

	// 拖拽排序状态（基于 fieldKey）
	const dragFieldKey = ref('')
	const dragOverFieldKey = ref('')

	// 拖拽开始
	const onSortStart = (fieldKey, event) => {
		if (!isFieldSelected(fieldKey)) {
			event.preventDefault()
			return
		}
		dragFieldKey.value = fieldKey
		event.dataTransfer.effectAllowed = 'move'
		event.dataTransfer.setData('text/plain', fieldKey)
	}

	// 拖拽经过目标项
	const onSortOver = (fieldKey) => {
		if (dragFieldKey.value && fieldKey !== dragFieldKey.value && isFieldSelected(fieldKey)) {
			dragOverFieldKey.value = fieldKey
		}
	}

	// 拖拽放置
	const onSortDrop = (targetFieldKey) => {
		const dragKey = dragFieldKey.value
		if (!dragKey || dragKey === targetFieldKey || !isFieldSelected(targetFieldKey)) {
			resetDrag()
			return
		}
		const arr = props.config.selectedFields
		const fromIdx = arr.indexOf(dragKey)
		const toIdx = arr.indexOf(targetFieldKey)
		if (fromIdx === -1 || toIdx === -1) {
			resetDrag()
			return
		}
		const [item] = arr.splice(fromIdx, 1)
		arr.splice(fromIdx < toIdx ? toIdx - 1 : toIdx, 0, item)
		updateQrContent()
		resetDrag()
	}

	// 拖拽结束
	const onSortEnd = () => {
		resetDrag()
	}

	// 重置拖拽状态
	const resetDrag = () => {
		dragFieldKey.value = ''
		dragOverFieldKey.value = ''
	}

	// 根据fieldKey获取字段作用域标签
	const getFieldScopeLabel = (fieldKey) => {
		const field = props.availableFieldOptions.find((f) => f.value === fieldKey)
		return field ? (field.fieldScope === 'DETAIL' ? '明细字段' : '主字段') : ''
	}

	// 根据fieldKey获取字段作用域
	const getFieldScope = (fieldKey) => {
		const field = props.availableFieldOptions.find((f) => f.value === fieldKey)
		return field ? field.fieldScope : 'MAIN'
	}

	// 二维码格式类型变化处理
	const onFormatTypeChange = (value) => {
		if (value === 'json') {
			// eslint-disable-next-line vue/no-mutating-props
			props.config.separator = ''
		} else if (value === 'custom') {
			// eslint-disable-next-line vue/no-mutating-props
			props.config.separator = '|'
		}
		updateQrContent()
	}

	// 组件挂载时初始化内容预览
	onMounted(() => {
		updateQrContent()
	})

	// 更新二维码内容预览
	const updateQrContent = () => {
		const config = props.config

		// 解析控制字符文本（保留原始配置不变）
		const p = (v) => {
			if (!v) return v
			return v
				.replace(/\\x1E/g, String.fromCharCode(30))
				.replace(/\\x1D/g, String.fromCharCode(29))
				.replace(/\\x04/g, String.fromCharCode(4))
				.replace(/\\x1F/g, String.fromCharCode(31))
		}

		// 检查是否有选中的字段
		if (!config.selectedFields || config.selectedFields.length === 0) {
			config.previewContent = ''
			return
		}

		if (config.formatType === 'json') {
			// JSON格式 - 根据数据类型生成不同格式
			if (config.jsonDataType === 'array') {
				const jsonObj = {}
				config.selectedFields.forEach((fieldKey) => {
					jsonObj[fieldKey] = fieldKey
				})
				const jsonArray = [jsonObj]
				config.previewContent = JSON.stringify(jsonArray, null, 2)
			} else {
				const jsonObj = {}
				config.selectedFields.forEach((fieldKey) => {
					jsonObj[fieldKey] = fieldKey
				})
				config.previewContent = JSON.stringify(jsonObj, null, 2)
			}
		} else if (config.formatType === 'custom') {
			const separator = (p(config.separator) || '|').trim()
			const fieldPrefixes = config.fieldPrefixes || {}
			const hasFieldPrefixes = config.selectedFields.some((fk) => fieldPrefixes[fk])

			let mainContent = ''
			if (config.detailIncludeMainFields && config.detailLoopMode === 'loop' && hasBothScopeFields.value) {
				// 携带主字段+循环模式：模拟2行，按selectedFields顺序输出
				const rowSep = (p(config.detailRowSeparator) || '#').trim()
				const rowPrefix = p(config.detailRowPrefix) || ''
				const rows = []
				for (let i = 0; i < 2; i++) {
					let row = ''
					if (rowPrefix) row += rowPrefix
					if (hasFieldPrefixes) {
						config.selectedFields.forEach((fk) => {
							const prefix = fieldPrefixes[fk] || ''
							row += separator + prefix + fk
						})
					} else {
						row = config.selectedFields.join(separator)
					}
					if (config.detailRowNumber) {
						row += rowSep + (i + 1)
					} else {
						row += rowSep
					}
					rows.push(row)
				}
				mainContent = rows.join('')
			} else if (hasFieldPrefixes) {
				const parts = []
				config.selectedFields.forEach((fieldKey) => {
					const prefix = fieldPrefixes[fieldKey] || ''
					parts.push(separator + prefix + fieldKey)
				})
				mainContent = parts.join('')
			} else {
				mainContent = config.selectedFields.join(separator)
			}

			config.previewContent = mainContent
		}

		// 开始标识符
		const startMarker = p(config.startMarker)
		if (startMarker && startMarker.trim() !== '') {
			config.previewContent = startMarker + config.previewContent
		}

		// 结束标识符
		const endMarker = p(config.endMarker)
		if (endMarker && endMarker.trim() !== '') {
			if (config.previewContent && config.previewContent.trim() !== '') {
				config.previewContent = config.previewContent + endMarker
			} else {
				config.previewContent = endMarker
			}
		}

		// 编码处理
		if (config.encodeMode && config.encodeMode !== 'none') {
			let processed = config.previewContent

			if (config.encodeMode === 'gzip_base64') {
				const softEncode = (s) => encodeURI(s).replace(/%[A-F0-9]{2}/g, (m) => m.toLowerCase())
				try {
					const compressed = pako.gzip(softEncode(processed))
					let binaryStr = ''
					const bytes = new Uint8Array(compressed)
					for (let i = 0; i < bytes.length; i++) {
						binaryStr += String.fromCharCode(bytes[i])
					}
					processed = binaryStr
				} catch (e) {
					console.warn('gzip压缩失败:', e)
				}
				try {
					processed = btoa(processed)
				} catch (e) {
					console.warn('base64编码失败:', e)
				}
			} else if (config.encodeMode === 'base64') {
				try {
					processed = btoa(unescape(encodeURIComponent(processed).replace(/%[A-F0-9]{2}/g, (m) => m.toLowerCase())))
				} catch (e) {
					console.warn('base64编码失败:', e)
				}
			}

			config.previewContent = processed
		}
	}
</script>

<style scoped>
	.section {
		margin-bottom: 16px;
		display: flex;
		justify-items: center;
		align-items: center;
	}

	.section-device {
		width: 5px;
		height: 20px;
		margin-right: 10px;
		background: var(--primary-color);
		border-radius: 5px;
	}

	.section-title {
		font-size: 16px;
		font-weight: 500;
		color: #333;
		flex: 1;
	}

	.ant-form-item-explain {
		margin-top: 4px;
	}

	.ant-form-item-explain small {
		color: #999;
		font-size: 12px;
	}

	.field-table {
		border: 1px solid #e8e8e8;
		border-radius: 6px;
		overflow: hidden;
	}

	.field-table-header {
		display: grid;
		grid-template-columns: 20px 32px 1fr 180px;
		gap: 6px;
		align-items: center;
		padding: 8px 12px;
		font-size: 12px;
		font-weight: 500;
		color: #888;
		background: #fafafa;
		border-bottom: 1px solid #e8e8e8;
	}

	.field-table-header .field-table-prefix {
		text-align: left;
	}

	.field-table-row {
		display: grid;
		grid-template-columns: 20px 32px 1fr 180px;
		gap: 6px;
		align-items: center;
		padding: 6px 12px;
		transition: background-color 0.2s;
	}

	.field-table-row:not(:last-child) {
		border-bottom: 1px solid #f0f0f0;
	}

	.field-table-row:hover {
		background-color: #f6f8fa;
	}

	.field-table-row-active {
		background-color: #f0f5ff;
	}

	.field-table-row-active:hover {
		background-color: #e6f0ff;
	}

	.field-table-check {
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.field-table-name {
		font-size: 13px;
		color: #333;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.field-table-prefix {
		width: 100%;
	}

	.field-table-prefix-disabled {
		color: #d9d9d9;
		font-size: 14px;
		text-align: center;
	}

	.field-table-drag-handle {
		color: #ccc;
		font-size: 14px;
		line-height: 1;
		flex-shrink: 0;
		width: 20px;
		text-align: center;
		cursor: default;
		user-select: none;
	}

	.field-table-drag-handle-active {
		color: #bbb;
		cursor: grab;
	}

	.field-table-drag-handle-active:active {
		cursor: grabbing;
	}

	.field-table-row:hover .field-table-drag-handle-active {
		color: #666;
	}

	.field-table-row-dragging {
		opacity: 0.5;
		background-color: #e6f0ff !important;
	}

	.field-table-row-dragover {
		border-top: 2px solid var(--primary-color);
		background-color: #f0f5ff !important;
	}

	.field-table-empty {
		padding: 24px 0;
		text-align: center;
		color: #999;
		font-size: 13px;
	}

	.field-table-group {
		margin-bottom: 16px;
	}

	.field-table-group:last-child {
		margin-bottom: 0;
	}

	.field-table-group-label {
		font-size: 13px;
		font-weight: 500;
		color: #555;
		margin-bottom: 8px;
		padding-left: 4px;
		border-left: 3px solid var(--primary-color);
		line-height: 1.4;
	}

	.content-preview {
		padding: 12px;
		background-color: #f5f5f5;
		border: 1px solid #d9d9d9;
		border-radius: 4px;
		margin-top: 8px;
	}

	.field-table-scope-badge {
		display: inline-block;
		padding: 0 4px;
		font-size: 11px;
		line-height: 16px;
		border-radius: 2px;
		margin-right: 4px;
		vertical-align: middle;
		font-weight: 500;
	}

	.field-table-scope-badge.scope-main {
		background-color: #e6f7ff;
		color: #1890ff;
		border: 1px solid #91d5ff;
	}

	.field-table-scope-badge.scope-detail {
		background-color: #f6ffed;
		color: #52c41a;
		border: 1px solid #b7eb8f;
	}
</style>
