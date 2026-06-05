/**
 * 字段配置工具函数
 * 集中管理 optionsData 解析和字段配置提取，避免多处复制不一致
 */

/**
 * 安全解析字段的 optionsData JSON 字符串
 * @param {object} field 字段配置对象
 * @returns {object|null} 解析后的 options 对象，解析失败返回 null
 */
export function parseFieldOptions(field) {
	if (!field || !field.optionsData) return null
	try {
		return typeof field.optionsData === 'string'
			? JSON.parse(field.optionsData)
			: field.optionsData
	} catch (e) {
		return null
	}
}

/**
 * 从字段的 optionsData 中解析日期配置（dateConfig/dateFormat）
 * dateConfig/dateFormat 存储在 optionsData JSON 字符串中，不是 field 的直接属性
 * @param {object} field 字段配置对象
 * @returns {object} 解析后的日期配置对象 { displayFormat, saveFormat, inputFormat, dateFormat, ... }
 */
export function parseDateConfig(field) {
	if (!field) return {}
	// 如果已经有直接属性，优先使用
	if (field.dateConfig) return field.dateConfig
	const options = parseFieldOptions(field)
	if (!options) return {}
	return {
		...options.dateConfig,
		// 兼容：optionsData 中也可能直接在根级有 dateFormat
		dateFormat: options.dateConfig?.displayFormat || options.dateFormat || field.dateFormat
	}
}

/**
 * 获取日期格式
 * 优先使用字段配置中的 dateConfig，如果没有则使用 dateFormat，最后通过 placeholder 判断
 * @param {object} field 字段配置对象
 * @returns {string} 日期格式字符串，如 'YYYY-MM-DD'
 */
export function getDateFormat(field) {
	// 从 optionsData 中解析日期配置
	const dateConfig = parseDateConfig(field)
	// 优先使用新的dateConfig配置
	if (dateConfig.displayFormat) {
		return dateConfig.displayFormat
	}
	// 兼容：optionsData 根级的 dateFormat
	if (dateConfig.dateFormat) {
		return dateConfig.dateFormat
	}

	// 向后兼容：使用字段配置中的时间格式
	if (field.dateFormat) {
		return field.dateFormat
	}

	// 如果没有配置时间格式，则通过 placeholder 来判断（向后兼容）
	if (field.placeholder) {
		// 检查是否包含常见的时间格式关键词
		if (field.placeholder.includes('YYYY-MM-DD HH:mm:ss')) {
			return 'YYYY-MM-DD HH:mm:ss'
		} else if (field.placeholder.includes('YYYY-MM-DD HH:mm')) {
			return 'YYYY-MM-DD HH:mm'
		} else if (field.placeholder.includes('YYYY-MM-DD')) {
			return 'YYYY-MM-DD'
		} else if (field.placeholder.includes('YYYY/MM/DD')) {
			return 'YYYY/MM/DD'
		} else if (field.placeholder.includes('MM-DD')) {
			return 'MM-DD'
		}
	}
	// 默认日期格式
	return 'YYYY-MM-DD'
}

/**
 * 获取日期范围格式
 * 处理日期范围选择器的格式配置，返回单个日期的格式
 * @param {object} field 字段配置对象
 * @returns {string} 单个日期格式字符串
 */
export function getDateRangeFormat(field) {
	// 从 optionsData 中解析日期配置
	const dateConfig = parseDateConfig(field)
	// 优先使用新的dateConfig配置
	if (dateConfig.displayFormat) {
		// 如果是范围格式（包含~或其他分隔符），提取单个日期格式
		const displayFormat = dateConfig.displayFormat
		if (displayFormat.includes('~')) {
			// 例如：'YYYY.MM.DD~YYYY.MM.DD' -> 'YYYY.MM.DD'
			return displayFormat.split('~')[0].trim()
		} else if (displayFormat.includes(' - ')) {
			// 例如：'YYYY.MM.DD - YYYY.MM.DD' -> 'YYYY.MM.DD'
			return displayFormat.split(' - ')[0].trim()
		} else if (displayFormat.includes('-') && displayFormat.match(/\d{4}.*-.*\d{4}/)) {
			// 处理可能的日期范围格式，但要避免误判单个日期中的连字符
			const parts = displayFormat.split('-')
			if (parts.length > 3) {
				// 如果分割后超过3部分，可能是范围格式，取前半部分
				const halfLength = Math.floor(parts.length / 2)
				return parts.slice(0, halfLength).join('-')
			}
		}
		// 如果不是范围格式，直接使用
		return displayFormat
	}

	// 向后兼容：使用字段配置中的时间格式
	if (field.dateFormat) {
		// 如果是范围格式（包含~），提取单个日期格式
		if (field.dateFormat.includes('~')) {
			// 例如：'YYYY-MM-DD~YYYY-MM-DD' -> 'YYYY-MM-DD'
			return field.dateFormat.split('~')[0].trim()
		} else if (field.dateFormat.includes(' - ')) {
			// 例如：'YYYY-MM-DD - YYYY-MM-DD' -> 'YYYY-MM-DD'
			return field.dateFormat.split(' - ')[0].trim()
		}
		// 如果不是范围格式，直接使用
		return field.dateFormat
	}

	// 如果没有配置时间格式，则通过 placeholder 来判断（向后兼容）
	if (field.placeholder) {
		// 检查是否包含常见的时间格式关键词
		if (field.placeholder.includes('YYYY-MM-DD HH:mm:ss')) {
			return 'YYYY-MM-DD HH:mm:ss'
		} else if (field.placeholder.includes('YYYY-MM-DD HH:mm')) {
			return 'YYYY-MM-DD HH:mm'
		} else if (field.placeholder.includes('YYYY-MM-DD')) {
			return 'YYYY-MM-DD'
		} else if (field.placeholder.includes('YYYY/MM/DD')) {
			return 'YYYY/MM/DD'
		} else if (field.placeholder.includes('MM-DD')) {
			return 'MM-DD'
		}
	}
	// 默认日期格式
	return 'YYYY-MM-DD'
}

/**
 * 判断是否为日期时间格式（需要显示时间选择器）
 * @param {object} field 字段配置对象
 * @returns {boolean}
 */
export function isDateTimeFormat(field) {
	let format
	// 根据字段类型获取相应的格式
	if (field.inputType === 'DATE_RANGE') {
		format = getDateRangeFormat(field)
	} else {
		format = getDateFormat(field)
	}
	// 如果格式包含时间部分，则显示时间选择器
	return format.includes('HH:mm')
}

/**
 * 获取数字字段的小数精度配置
 * 从 optionsData 中解析 numberConfig.precision
 * @param {object} field 字段配置对象
 * @returns {number|undefined} 小数精度，未配置时返回 undefined
 */
export function getNumberPrecision(field) {
	if (!field || field.inputType !== 'NUMBER') return undefined
	const options = parseFieldOptions(field)
	if (options && options.numberConfig && options.numberConfig.precision !== undefined && options.numberConfig.precision >= 0) {
		return options.numberConfig.precision
	}
	return undefined
}
