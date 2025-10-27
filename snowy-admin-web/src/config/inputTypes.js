/**
 * 控件类型统一配置
 * 统一管理所有控件类型的定义，避免多处重复定义
 */

/**
 * 控件类型选项配置
 */
export const inputTypeOptions = [
	{ label: '文本输入', value: 'INPUT' },
	{ label: '日期选择', value: 'DATE' },
	{ label: '时间范围选择', value: 'DATE_RANGE' },
	{ label: '多行文本', value: 'TEXTAREA' },
	{ label: '数字输入', value: 'NUMBER' },
	{ label: '下拉选择', value: 'SELECT' },
	{ label: '单选按钮', value: 'RADIO' },
	{ label: '复选框', value: 'CHECKBOX' },
	{ label: '二维码', value: 'QRCODE' },
	{ label: '动态条码', value: 'DYNAMIC_BARCODE' }
]

/**
 * 控件类型颜色映射
 */
export const inputTypeColorMap = {
	INPUT: 'blue',
	DATE: 'orange',
	DATE_RANGE: 'volcano',
	TEXTAREA: 'purple',
	NUMBER: 'cyan',
	SELECT: 'geekblue',
	RADIO: 'magenta',
	CHECKBOX: 'lime',
	QRCODE: 'gold',
	DYNAMIC_BARCODE: 'green'
}

/**
 * 获取控件类型文本
 * @param {string} inputType 控件类型值
 * @returns {string} 控件类型显示文本
 */
export const getInputTypeText = (inputType) => {
	const option = inputTypeOptions.find(item => item.value === inputType)
	return option ? option.label : inputType
}

/**
 * 获取控件类型颜色
 * @param {string} inputType 控件类型值
 * @returns {string} 控件类型对应的颜色
 */
export const getInputTypeColor = (inputType) => {
	return inputTypeColorMap[inputType] || 'default'
}

/**
 * 检查是否为有效的控件类型
 * @param {string} inputType 控件类型值
 * @returns {boolean} 是否为有效类型
 */
export const isValidInputType = (inputType) => {
	return inputTypeOptions.some(option => option.value === inputType)
}

/**
 * 获取所有控件类型值
 * @returns {string[]} 所有控件类型值的数组
 */
export const getAllInputTypeValues = () => {
	return inputTypeOptions.map(option => option.value)
}

/**
 * 获取所有控件类型标签
 * @returns {string[]} 所有控件类型标签的数组
 */
export const getAllInputTypeLabels = () => {
	return inputTypeOptions.map(option => option.label)
}
