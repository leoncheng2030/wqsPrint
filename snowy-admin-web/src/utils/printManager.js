/**
 * 统一打印管理模块
 * 整合 hiprint 相关功能，提供统一的 API 接口
 * 替代分散的 usePrintOfficial.js、printService.js、hiprintUtil.js
 */
import Promise from "lodash-es/_Promise";

class PrintManager {
	constructor() {
		this.hiprint = null
		this.hiwebSocket = null
		this.isInitialized = false
		this.isConnected = false
		this.currentTemplate = null
		this.printerList = []
		this.callbacks = new Map()
		this.callbackId = 0

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
	 * 生成预览 HTML
	 */
	async generatePreview(template, data = {}) {
		if (!template) {
			throw new Error('模板不能为空')
		}

		try {
			const htmlResult = template.getHtml(data)

			// 处理不同类型的返回值
			let finalHtml = ''

			// 检查是否是 Promise
			if (htmlResult && typeof htmlResult.then === 'function') {
				// 异步 Promise，等待解析
				const resolvedResult = await htmlResult

				// 处理解析后的结果
				if (resolvedResult && resolvedResult.jquery) {
					// jQuery 对象
					finalHtml = resolvedResult.length > 0 ? resolvedResult[0].outerHTML : resolvedResult.html()
				} else if (typeof resolvedResult === 'string') {
					// 字符串
					finalHtml = resolvedResult
				} else if (resolvedResult && resolvedResult.outerHTML) {
					// DOM 元素
					finalHtml = resolvedResult.outerHTML
				} else if (resolvedResult === null || resolvedResult === undefined) {
					finalHtml = ''
				} else {
					finalHtml = String(resolvedResult)
				}
			} else if (htmlResult && htmlResult.jquery) {
				// jQuery 对象
				finalHtml = htmlResult.length > 0 ? htmlResult[0].outerHTML : htmlResult.html()
			} else if (typeof htmlResult === 'string') {
				// 字符串
				finalHtml = htmlResult
			} else if (htmlResult && htmlResult.outerHTML) {
				// DOM 元素
				finalHtml = htmlResult.outerHTML
			} else if (htmlResult === null || htmlResult === undefined) {
				finalHtml = ''
			} else {
				finalHtml = String(htmlResult)
			}

			// 检查最终HTML是否有效
			if (!finalHtml || finalHtml.trim() === '') {
				finalHtml = `
          <div style="padding: 40px; text-align: center; border: 2px dashed #ffa500; color: #ffa500; background: #fff7e6;">
            <h3>预览生成警告</h3>
            <p>生成的HTML内容为空，请检查模板配置和数据</p>
          </div>
        `
			}

			return finalHtml
		} catch (error) {
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
	 * 直接打印（使用 print2）
	 */
	async print(template, data = {}, options = {}) {
		if (!this.isInitialized) {
			throw new Error('打印管理器未初始化')
		}

		if (!template) {
			throw new Error('打印模板不能为空')
		}

		try {
			const printOptions = {
				printer: options.printer,
				title: options.title || '打印任务',
				...options
			}
			template.print2(data, {
				...printOptions,
			})
		} catch (e) {
			console.log(e)
		}
	}

	/**
	 * 多页打印
	 */
	async printMulti(printItems, options = {}) {
		if (!this.isInitialized) {
			throw new Error('打印管理器未初始化')
		}

		if (!Array.isArray(printItems) || printItems.length === 0) {
			throw new Error('打印数据不能为空')
		}

		try {
			const effectivePrinter = options.printer || options.printerName
			const printOptions = {
				printer: effectivePrinter,
				printerName: effectivePrinter,
				title: options.title || '打印任务',
				templates: [
					{
						template: this.currentTemplate,
						data: printItems
					}
				]
			}

			await window.hiprint.print2(printOptions)
		} catch (error) {
			this.state.error = this.handleError(error, '多页打印失败')
			throw error
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

			const finalOptions = {...defaultOptions, ...options}

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
			// 优先使用 hiwebSocket
			if (this.hiwebSocket && this.isConnected && this.hiwebSocket.socket) {
				return await new Promise((resolve) => {
					const timeout = setTimeout(() => {
						resolve(this.printerList || [])
					}, 3000)

					const handler = (printers) => {
						clearTimeout(timeout)
						this.printerList = printers || []
						resolve(this.printerList)
					}

					this.hiwebSocket.socket.once('printerList', handler)
					this.hiwebSocket.socket.emit('refreshPrinterList')
				})
			}

			// 回退到 hiprint 方法
			if (typeof this.hiprint.refreshPrinterList === 'function') {
				return await new Promise((resolve) => {
					try {
						this.hiprint.refreshPrinterList((list) => {
							this.printerList = list || []
							resolve(this.printerList)
						})
					} catch (err) {
						console.error('获取打印机列表失败:', err)
						resolve([])
					}
				})
			}

			return []
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

		console.error(`[PrintManager] ${fullMessage}`, error)

		return {
			message: fullMessage,
			context,
			timestamp: new Date().toISOString(),
			stack: error?.stack
		}
	}

	/**
	 * 生成回调ID
	 */
	generateCallbackId() {
		return `callback_${Date.now()}_${++this.callbackId}`
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
