/**
 * 统一打印管理模块
 * 整合 hiprint 相关功能，提供统一的 API 接口
 * 替代分散的 usePrintOfficial.js、printService.js、hiprintUtil.js
 */

class PrintManager {
	constructor() {
		this.hiprint = null
		this.hiwebSocket = null
		this.isInitialized = false
		this.isConnected = false
		this.currentTemplate = null
		this.printerList = []
		this.callbacks = new Map()

		// 状态管理
		this.state = {
			loading: {
				init: false,
				print: false,
				preview: false,
				export: false,
				printers: false
			},
			error: null
		}
	}

	/**
	 * 初始化打印管理器
	 */
	async initialize() {
		if (this.isInitialized) {
			console.log('[PrintManager] 打印管理器已初始化，跳过重复初始化')
			return true
		}

		this.state.loading.init = true
		this.state.error = null

		try {
			console.log('[PrintManager] 开始初始化打印管理器...')

			// 检查全局环境
			if (typeof window === 'undefined') {
				throw new Error('window 对象未定义，无法在非浏览器环境中运行')
			}

			// 检查全局 hiprint 对象
			console.log('[PrintManager] 检查 window.hiprint:', window.hiprint)
			if (!window.hiprint) {
				throw new Error('hiprint 未加载，请确保 hiprint 插件已正确引入')
			}

			this.hiprint = window.hiprint
			console.log('[PrintManager] hiprint 对象已设置')

			// 检查 hiwebSocket
			console.log('[PrintManager] 检查 window.hiwebSocket:', window.hiwebSocket)
			if (window.hiwebSocket) {
				this.hiwebSocket = window.hiwebSocket
				console.log('[PrintManager] hiwebSocket 对象已设置')
				this.setupWebSocketHandlers()
			} else {
				console.warn('[PrintManager] hiwebSocket 未找到，打印服务可能无法连接')
			}

			this.isInitialized = true
			this.isConnected = !!this.hiwebSocket?.opened
			console.log('[PrintManager] 初始化完成，连接状态:', this.isConnected)

			// 初始化后立即获取打印机列表
			if (this.isConnected) {
				console.log('[PrintManager] 开始获取打印机列表...')
				await this.refreshPrinterList()
			} else {
				console.warn('[PrintManager] 打印服务未连接，跳过获取打印机列表')
			}

			console.log('[PrintManager] 打印管理器初始化成功')
			return true
		} catch (error) {
			console.error('[PrintManager] 打印管理器初始化失败:', error)
			this.state.error = this.handleError(error, '初始化失败')
			return false
		} finally {
			this.state.loading.init = false
		}
	}

	/**
	 * 设置 WebSocket 事件处理器
	 */
	setupWebSocketHandlers() {
		if (!this.hiwebSocket?.socket) return

		const socket = this.hiwebSocket.socket

		// 连接状态变化
		socket.on('connect', () => {
			this.isConnected = true
		})

		socket.on('disconnect', () => {
			this.isConnected = false
		})

		// 打印机列表更新
		socket.on('printerList', (printers) => {
			this.printerList = printers || []
		})

		// 打印状态回调
		socket.on('render-print-success', (result) => {
			this.handlePrintCallback(result.templateId, true, result)
		})

		socket.on('render-print-error', (error) => {
			this.handlePrintCallback(error.templateId, false, error)
		})
	}

	/**
	 * 处理打印回调
	 */
	handlePrintCallback(templateId, success, data) {
		const callback = this.callbacks.get(templateId)
		if (callback) {
			if (success) {
				callback.resolve(data)
			} else {
				callback.reject(new Error(data?.error || '打印失败'))
			}
			this.callbacks.delete(templateId)
		}
	}

	/**
	 * 创建打印模板
	 */
	createTemplate(templateData) {
		if (!this.isInitialized) {
			throw new Error('打印管理器未初始化')
		}

		if (!templateData) {
			throw new Error('模板数据不能为空')
		}

		try {
			this.currentTemplate = new this.hiprint.PrintTemplate({
				template: templateData
			})
			return this.currentTemplate
		} catch (error) {
			throw new Error(`创建打印模板失败: ${error.message}`)
		}
	}

	/**
	 * 生成预览 HTML（优化版本 - 支持大数据量）
	 * @param {Object} template - 打印模板对象
	 * @param {Object|Array} data - 打印数据，支持单条或多条
	 * @param {Object} options - 配置选项
	 * @param {Number} options.batchSize - 分批处理大小，默认50
	 * @param {Function} options.onProgress - 进度回调函数
	 * @param {Boolean} options.useCache - 是否使用缓存，默认true
	 * @returns {Promise<String>} 生成的 HTML 字符串
	 */
	async generatePreview(template, data = {}, options = {}) {
		if (!template) {
			throw new Error('模板不能为空')
		}
		try {
			// 检查是否为数组数据（大数据量场景）
			const isArrayData = Array.isArray(data)
			const dataCount = isArrayData ? data.length : 1

			// 如果数据量较小，直接生成
			if (dataCount) {
				console.log('[PrintManager] 数据量较小，直接生成预览HTML')
				return await this._generateSingleHtml(template, data)
			}else{
				console.log('[PrintManager] 数据量较大，使用分批生成预览HTML')
			}
		} catch (error) {
			console.error('[PrintManager] 预览生成失败:', error)
			// 返回详细的错误信息页面
			return `
        <div style="padding: 40px; text-align: center; border: 2px dashed #ff4d4f; color: #ff4d4f; background: #fff2f0;">
          <h3>预览生成失败</h3>
          <p>错误信息: ${error.message || '未知错误'}</p>
        </div>
      `
		}
	}
	/**
	 * 生成单个HTML（内部方法）
	 */
	async _generateSingleHtml(template, data) {
		let html = ''
		const result = this.currentTemplate.getHtml(data)
		try {
			const resolvedResult = await result
			html = resolvedResult.html()
		} catch (promiseError) {
			console.error('预览HTML生成失败:', promiseError)
		}
		return html
	}


	/**
	 * 直接打印（使用 print2）
	 */
	async print(template, data = {}, options = {}) {
		if (!this.isInitialized) {
			throw new Error('打印管理器未初始化')
		}

		if (!template) {
			throw new Error('打印模板不能为空')
		}

		this.state.loading.print = true
		this.state.error = null

		const timeout = options.timeout || 1000 // 默认30秒超时
		const timeoutId = setTimeout(() => {
			this.state.loading.print = false
			this.state.error = this.handleError(new Error('打印超时'), '打印操作')
		}, timeout)

		try {
			const printOptions = {
				printer: options.printer,
				title: options.title || '打印任务',
				...options,
			}

			const print2 = await template.print2(data, printOptions);
			this.state.loading.print = false
			return print2
		} catch (error) {
			clearTimeout(timeoutId)
			this.state.loading.print = false
			this.state.error = this.handleError(error, '打印操作')
			throw error
		}
	}

	/**
	 * 多页打印
	 */
	async printMulti(printItems, options = {}) {
		this.state.loading.print = true
		this.state.error = null

		try {
			// 验证打印机是否在可用列表中
			const effectivePrinter = options.printer || options.printerName
			if (effectivePrinter && !this.printerList.some(p => p.name === effectivePrinter)) {
				throw new Error(`指定的打印机 "${effectivePrinter}" 不可用`)
			}

			const printOptions = {
				printer: effectivePrinter,
				printerName: effectivePrinter,
				title: options.title || '打印任务',
				templates: [
					{
						template: this.currentTemplate,
						data: printItems
					}
				],
				// 强制使用指定打印机，不自动选择
				preferDefaultPrinter: false
			}

			console.log('打印选项:', printOptions)
			return await this.hiprint.print2(printOptions)
		} catch (error) {
			this.state.error = this.handleError(error, '多页打印失败')
			throw error
		} finally {
			this.state.loading.print = false
		}
	}

	/**
	 * 导出 PDF
	 */
	async exportPdf(template, data = {}, filename, options = {}) {
		if (!this.isInitialized) {
			throw new Error('打印管理器未初始化')
		}

		if (!template) {
			throw new Error('打印模板不能为空')
		}

		this.state.loading.export = true

		try {
			const defaultOptions = {
				pixelRatio: 2,
				isDownload: true,
				type: 'blob',
				onProgress: (cur, total) => {
					const percent = Math.floor((cur / total) * 100)
					console.log('PDF导出进度:', percent + '%')
				}
			}

			const finalOptions = { ...defaultOptions, ...options }

			return await template.toPdf(data, filename, finalOptions)
		} catch (error) {
			this.state.error = this.handleError(error, 'PDF导出失败')
			throw error
		} finally {
			this.state.loading.export = false
		}
	}

	/**
	 * 刷新打印机列表
	 */
	async refreshPrinterList() {
		if (!this.isInitialized) {
			throw new Error('打印管理器未初始化')
		}

		this.state.loading.printers = true

		try {
			const list = await hiprint.refreshPrinterList();
			this.printerList = list || []

			return this.printerList
		} catch (error) {
			this.state.error = this.handleError(error, '获取打印机列表失败')
			return []
		} finally {
			this.state.loading.printers = false
		}
	}

	/**
	 * 检查连接状态
	 */
	checkStatus() {
		return {
			initialized: this.isInitialized,
			connected: this.isConnected,
			available: !!this.hiprint,
			printers: this.printerList.length
		}
	}

	/**
	 * 错误处理
	 */
	handleError(error, context = '') {
		const errorMessage = error?.message || error || '未知错误'
		const fullMessage = context ? `${context}: ${errorMessage}` : errorMessage
		return {
			message: fullMessage,
			context,
			timestamp: new Date().toISOString(),
			stack: error?.stack
		}
	}

	/**
	 * 清理资源
	 */
	cleanup() {
		this.callbacks.clear()
		this.currentTemplate = null
		this.isInitialized = false
		this.isConnected = false
	}
}

// 创建单例实例
const printManager = new PrintManager()

export default printManager
