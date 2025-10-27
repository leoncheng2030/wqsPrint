/**
 * 统一打印服务 - 基于官方hiwebSocket API
 * 替代多个分散的composables，简化架构
 */
class PrintService {
	constructor() {
		this.hiwebSocket = null
		this.isConnected = false
		this.isInitialized = false
		this.callbacks = new Map()
		this.callbackId = 0
		this.printerList = [] // 添加打印机列表属性
		this.printTemplate = null
	}

	/**
	 * 初始化打印服务
	 */
	async initialize() {
		try {
			// 检查官方API是否可用
			if (typeof window !== 'undefined' && window.hiwebSocket) {
				this.hiwebSocket = window.hiwebSocket
				// 无论当前是否已打开，都先挂载事件，等待后续 connect 事件更新状态
				if (this.hiwebSocket.socket) {
					this.setupEventHandlers()
				}
				// 依据当前 opened 值设置一次初始状态
				this.isConnected = !!this.hiwebSocket.opened
				this.isInitialized = true

				// 如果已经连接，则立即请求打印机列表
				if (this.isConnected && this.hiwebSocket.socket) {
					this.hiwebSocket.socket.emit('refreshPrinterList')
				}
				return true
			}

			throw new Error('hiwebSocket未初始化或不可用')
		} catch (error) {
			console.error('打印服务初始化失败:', error)
			return false
		}
	}

	/**
	 * 设置事件处理器
	 */
	setupEventHandlers() {
		if (!this.hiwebSocket || !this.hiwebSocket.socket) return

		const socket = this.hiwebSocket.socket

		// 监听打印成功事件
		socket.on('render-print-success', (result) => {
			const callback = this.callbacks.get(result.templateId)
			if (callback) {
				callback.resolve(result)
				this.callbacks.delete(result.templateId)
			}
		})

		// 监听打印失败事件
		socket.on('render-print-error', (error) => {
			const callback = this.callbacks.get(error.templateId)
			if (callback) {
				callback.reject(new Error(error.error || '打印失败'))
				this.callbacks.delete(error.templateId)
			}
		})

		// 监听连接状态变化
		socket.on('connect', () => {
			this.isConnected = true
		})

		socket.on('disconnect', () => {
			this.isConnected = false
		})

		// 监听客户端信息
		socket.on('clientInfo', (data) => {
			// clientInfo可能包含打印机列表，但不是主要用途
			if (data.printers) {
				this.printerList = data.printers
			}
		})

		// 监听打印机列表（主要用途）
		socket.on('printerList', (printers) => {
			this.printerList = printers || []
		})
	}

	/**
	 * 检查服务状态
	 */
	checkStatus() {
		return {
			initialized: this.isInitialized,
			// 优先读取全局 hiwebSocket.opened，其次使用本地 isConnected 标志
			connected: (this.hiwebSocket && typeof this.hiwebSocket.opened !== 'undefined') ? !!this.hiwebSocket.opened : this.isConnected,
			available: !!this.hiwebSocket
		}
	}

	/**
	 * 获取打印机列表
	 * 优先使用 hiwebSocket，失败时回退到 hiprint.refreshPrinterList
	 */
	async getPrinterList() {
		try {
			if (this.hiwebSocket && this.isConnected && this.hiwebSocket.socket) {
				const socket = this.hiwebSocket.socket
				return await new Promise((resolve) => {
					// 使用一次性监听，避免重复堆积
					const timeout = setTimeout(() => {
						resolve(this.printerList || [])
					}, 3000)
					const handler = (printers) => {
						clearTimeout(timeout)
						this.printerList = printers || []
						resolve(this.printerList)
					}
					socket.once('printerList', handler)
					socket.emit('refreshPrinterList')
				})
			} else if (typeof window !== 'undefined' && window.hiprint && typeof window.hiprint.refreshPrinterList === 'function') {
				// 回退到 hiprint 的刷新方法
				return await new Promise((resolve) => {
					try {
						window.hiprint.refreshPrinterList((list) => {
							this.printerList = list || []
							resolve(this.printerList)
						})
					} catch (err) {
						console.error('获取打印机列表失败(hiprint):', err)
						resolve([])
					}
				})
			} else {
				return []
			}
		} catch (error) {
			console.error('获取打印机列表异常:', error)
			return []
		}
	}

	/**
	 * 设置模板实例（由外部创建）
	 */
	setTemplate(templateInstance) {
		this.printTemplate = templateInstance
	}

	/**
	 * 直接打印HTML
	 */
	async printHtml(recordData, options = {}) {
		// 防御：确保模板已设置且方法可用
		if (!this.printTemplate || typeof this.printTemplate.print2 !== 'function') {
			throw new Error('打印模板未初始化或不支持直接打印，请先创建模板实例')
		}
		// 调试：打印所选打印机
		try { console.debug?.('[printService.printHtml] options.printer:', options?.printer, 'printerName:', options?.printerName) } catch (_) {}
		return await this.printTemplate.print2(recordData,options)
	}

	/**
	 * 打印PDF URL
	 */
	async printPdf(printData, options = {}) {
		this.printTemplate.toPdf(printData, '', {
			isDownload: true, // 不自动下载
			type: 'blob', // 默认 blob  支持: blob、bloburl、pdfobjectnewwindow、dataurl  --> jspdf.output(type)
			onProgress: (cur, total) => {
				const percent = Math.floor((cur / total) * 100);
				console.log('toPdf 进度', percent);
			}
		})
	}
	async printMulti(printItems, options = {}) {
		// 防御：确保模板存在
		if (!this.printTemplate) {
			throw new Error('打印模板未初始化，请先创建模板实例')
		}
		// 优先使用传入 options，其次使用全局选中打印机
		const effectivePrinter = options.printer || options.printerName || this.selectedPrinter || undefined
		// 组装打印参数，确保传递所选打印机
		const printOptions = {
			printer: effectivePrinter,
			printerName: effectivePrinter,
			title: options.title || '打印任务',
			templates: [
				{
					template: this.printTemplate,
					data: printItems
				}
			]
		}
		// 调试：打印所选打印机
		try { console.debug?.('[printService.printMulti] options.printer:', printOptions?.printer, 'printerName:', printOptions?.printerName) } catch (_) {}
		await window.hiprint.print2(printOptions)
	}
	/**
	 * 批量打印
	 */
	async printBatch(printItems, options = {}) {
		const concurrency = options.concurrency || 3
		const delay = options.delay || 200
		const results = []

		// 分批处理
		for (let i = 0; i < printItems.length; i += concurrency) {
			const batch = printItems.slice(i, i + concurrency)

			const batchPromises = batch.map(async (item, index) => {
				// 添加延迟避免冲突
				if (index > 0) {
					await new Promise(resolve => setTimeout(resolve, delay))
				}

				try {
					if (item.type === 'html') {
						const mergedOptions = { ...(item.options || {}) }
						if (options.printer && !mergedOptions.printer) {
							mergedOptions.printer = options.printer
						}
						return await this.printHtml(item.html, mergedOptions)
					} else if (item.type === 'pdf') {
						const mergedOptions = { ...(item.options || {}) }
						if (options.printer && !mergedOptions.printer) {
							mergedOptions.printer = options.printer
						}
						return await this.printPdf(item.pdfUrl, mergedOptions)
					} else {
						throw new Error(`不支持的打印类型: ${item.type}`)
					}
				} catch (error) {
					return {success: false, error: error.message}
				}
			})

			const batchResults = await Promise.all(batchPromises)
			results.push(...batchResults)
		}

		return results
	}

	/**
	 * 清理资源
	 */
	cleanup() {
		// 清理所有回调
		this.callbacks.clear()

		// 移除事件监听
		if (this.hiwebSocket && this.hiwebSocket.socket) {
			const socket = this.hiwebSocket.socket
			socket.off('render-print-success')
			socket.off('render-print-error')
			socket.off('connect')
			socket.off('disconnect')
			socket.off('clientInfo')
			socket.off('printerList')
		}

		this.isInitialized = false
	}

	/**
	 * 为HTML添加打印必要的样式和结构
	 */
	enhanceHtmlForPrint(html) {
		if (!html || html.trim() === '') {
			return html
		}

		// 检查是否已经是完整的HTML文档
		const isCompleteHtml = html.toLowerCase().includes('<!doctype') || html.toLowerCase().includes('<html')

		if (isCompleteHtml) {
			// 如果已经是完整HTML，直接返回
			return html
		}

		// 为片段HTML添加必要的样式和结构
		const enhancedHtml = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>打印页面</title>
  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    body {
      font-family: "Microsoft YaHei", "PingFang SC", "Helvetica Neue", Arial, sans-serif;
      line-height: 1.4;
      color: #333;
      background: white;
      -webkit-print-color-adjust: exact;
      print-color-adjust: exact;
    }

    /* hiprint 基本样式 */
    .hiprint-printPaper {
      background: white;
      margin: 0;
      overflow: visible;
      page-break-after: always;
    }

    .hiprint-printPaper:last-child {
      page-break-after: auto;
    }

    .hiprint-printElement {
      position: absolute;
      display: block;
    }

    /* 打印时隐藏页面边距 */
    @media print {
      @page {
        margin: 0;
      }

      body {
        margin: 0;
        padding: 0;
      }

      .hiprint-printPaper {
        page-break-after: always;
      }

      .hiprint-printPaper:last-child {
        page-break-after: auto;
      }
    }

    /* 确保文本和背景颜色正常显示 */
    * {
      -webkit-print-color-adjust: exact !important;
      color-adjust: exact !important;
      print-color-adjust: exact !important;
    }
  </style>
</head>
<body>
  ${html}
</body>
</html>`

		return enhancedHtml
	}
}

// 创建单例实例
const printService = new PrintService()

export default printService
