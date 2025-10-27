/**
 * 统一错误处理组合式函数
 * 用于标准化异步操作的错误处理和用户反馈
 */
import { ref } from 'vue'
import { message } from 'ant-design-vue'

/**
 * 错误类型枚举
 */
export const ErrorTypes = {
	NETWORK: 'network',
	VALIDATION: 'validation',
	AUTHORIZATION: 'authorization',
	BUSINESS: 'business',
	UNKNOWN: 'unknown'
}

/**
 * 错误处理配置
 */
const defaultErrorConfig = {
	// 是否显示错误消息
	showMessage: true,
	// 是否记录错误日志
	logError: true,
	// 默认错误消息
	defaultMessage: '操作失败，请稍后重试',
	// 错误消息持续时间
	messageDuration: 3
}

/**
 * 统一错误处理钩子
 * @param {Object} config - 错误处理配置
 * @returns {Object} 错误处理相关方法和状态
 */
export function useErrorHandler(config = {}) {
	const errorConfig = { ...defaultErrorConfig, ...config }
	const lastError = ref(null)
	const errorHistory = ref([])

	/**
	 * 处理错误
	 * @param {Error|Object} error - 错误对象
	 * @param {string} context - 错误上下文
	 * @param {Object} options - 处理选项
	 */
	const handleError = (error, context = '', options = {}) => {
		const errorInfo = {
			error,
			context,
			timestamp: new Date().toISOString(),
			type: getErrorType(error)
		}

		lastError.value = errorInfo
		errorHistory.value.unshift(errorInfo)

		// 限制错误历史记录数量
		if (errorHistory.value.length > 50) {
			errorHistory.value = errorHistory.value.slice(0, 50)
		}

		const finalOptions = { ...errorConfig, ...options }

		// 显示错误消息
		if (finalOptions.showMessage) {
			const errorMessage = getErrorMessage(error, finalOptions.defaultMessage)
			message.error(errorMessage, finalOptions.messageDuration)
		}

		// 记录错误日志（在生产环境中可以发送到日志服务）
		if (finalOptions.logError && process.env.NODE_ENV === 'development') {
			console.group(`🚨 Error in ${context || 'Unknown Context'}`)
			console.error('Error:', error)
			console.error('Context:', context)
			console.error('Type:', errorInfo.type)
			console.error('Timestamp:', errorInfo.timestamp)
			console.groupEnd()
		}

		return errorInfo
	}

	/**
	 * 获取错误类型
	 * @param {Error|Object} error - 错误对象
	 * @returns {string} 错误类型
	 */
	const getErrorType = (error) => {
		if (!error) return ErrorTypes.UNKNOWN

		// 网络错误
		if (error.code === 'NETWORK_ERROR' || error.message?.includes('Network Error')) {
			return ErrorTypes.NETWORK
		}

		// HTTP 状态码错误
		if (error.response?.status) {
			const status = error.response.status
			if (status === 401 || status === 403) {
				return ErrorTypes.AUTHORIZATION
			}
			if (status >= 400 && status < 500) {
				return ErrorTypes.VALIDATION
			}
			if (status >= 500) {
				return ErrorTypes.NETWORK
			}
		}

		// 业务错误
		if (error.code && typeof error.code === 'string') {
			return ErrorTypes.BUSINESS
		}

		return ErrorTypes.UNKNOWN
	}

	/**
	 * 获取错误消息
	 * @param {Error|Object} error - 错误对象
	 * @param {string} defaultMessage - 默认消息
	 * @returns {string} 错误消息
	 */
	const getErrorMessage = (error, defaultMessage) => {
		if (!error) return defaultMessage

		// 优先使用自定义错误消息
		if (error.message && typeof error.message === 'string') {
			return error.message
		}

		// HTTP 响应错误消息
		if (error.response?.data?.message) {
			return error.response.data.message
		}

		// 根据状态码返回友好消息
		if (error.response?.status) {
			const status = error.response.status
			const statusMessages = {
				400: '请求参数错误',
				401: '未授权，请重新登录',
				403: '权限不足',
				404: '请求的资源不存在',
				408: '请求超时',
				500: '服务器内部错误',
				502: '网关错误',
				503: '服务不可用',
				504: '网关超时'
			}
			return statusMessages[status] || `请求失败 (${status})`
		}

		return defaultMessage
	}

	/**
	 * 清除错误历史
	 */
	const clearErrorHistory = () => {
		errorHistory.value = []
		lastError.value = null
	}

	/**
	 * 获取特定类型的错误
	 * @param {string} type - 错误类型
	 * @returns {Array} 错误列表
	 */
	const getErrorsByType = (type) => {
		return errorHistory.value.filter(error => error.type === type)
	}

	return {
		lastError,
		errorHistory,
		handleError,
		getErrorType,
		getErrorMessage,
		clearErrorHistory,
		getErrorsByType
	}
}

/**
 * 异步操作包装器
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 选项
 * @returns {Function} 包装后的异步函数
 */
export function withErrorHandler(asyncFn, options = {}) {
	const { handleError } = useErrorHandler(options)

	return async (...args) => {
		try {
			return await asyncFn(...args)
		} catch (error) {
			handleError(error, options.context || asyncFn.name)
			throw error
		}
	}
}

/**
 * 安全异步操作包装器（不会抛出错误）
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 选项
 * @returns {Function} 包装后的异步函数
 */
export function safeAsync(asyncFn, options = {}) {
	const { handleError } = useErrorHandler(options)

	return async (...args) => {
		try {
			const result = await asyncFn(...args)
			return { success: true, data: result, error: null }
		} catch (error) {
			const errorInfo = handleError(error, options.context || asyncFn.name)
			return { success: false, data: null, error: errorInfo }
		}
	}
}

/**
 * 重试机制包装器
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 重试选项
 * @returns {Function} 包装后的异步函数
 */
export function withRetry(asyncFn, options = {}) {
	const {
		maxRetries = 3,
		retryDelay = 1000,
		retryCondition = () => true,
		context = asyncFn.name
	} = options

	const { handleError } = useErrorHandler(options)

	return async (...args) => {
		let lastError = null

		for (let attempt = 0; attempt <= maxRetries; attempt++) {
			try {
				return await asyncFn(...args)
			} catch (error) {
				lastError = error

				// 如果是最后一次尝试或不满足重试条件，则抛出错误
				if (attempt === maxRetries || !retryCondition(error)) {
					handleError(error, `${context} (重试 ${attempt} 次后失败)`)
					throw error
				}

				// 等待后重试
				if (retryDelay > 0) {
					await new Promise(resolve => setTimeout(resolve, retryDelay * (attempt + 1)))
				}
			}
		}
	}
}