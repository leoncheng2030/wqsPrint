<template>
	<div class="field-group">
		<div class="group-title">
			<component :is="iconComponent" />
			{{ title }} ({{ fields.length }})
		</div>
		<div class="field-grid">
			<FieldCard
				v-for="field in fields"
				:key="field.id"
				:field="field"
				@copy-field="$emit('copy-field', field)"
			/>
		</div>
	</div>
</template>

<script setup>
import { computed } from 'vue'
import { FolderOutlined, TableOutlined } from '@ant-design/icons-vue'
import FieldCard from './FieldCard.vue'

const props = defineProps({
	fields: {
		type: Array,
		required: true
	},
	title: {
		type: String,
		required: true
	},
	icon: {
		type: String,
		default: 'folder',
		validator: value => ['folder', 'table'].includes(value)
	}
})

const emit = defineEmits(['copy-field'])

const iconComponent = computed(() => {
	return props.icon === 'table' ? TableOutlined : FolderOutlined
})
</script>

<style scoped>
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

.field-grid {
	display: grid;
	grid-template-columns: 1fr;
	gap: 6px;
}
</style>