import { computed, reactive } from 'vue'
import { message } from 'ant-design-vue'
import printManager from '@/utils/printManager'

/**
 * 打印组合式函数 - 基于统一的打印管理模块
 * 提供响应式的打印功能接口
 */
export function usePrintOfficial() {
  // 响应式状态
  const state = reactive({
    // 打印管理器状态
    isInitialized: false,
    isConnected: false,

    // 模板相关状态
    currentTemplate: null,
    templateData: {},
    previewHtml: '',

    // 打印机状态
    printerList: [],
    selectedPrinter: null,

    // 加载状态
    loading: {
      init: false,
      print: false,
      preview: false,
      export: false,
      printers: false
    },

    // 错误状态
    error: null
  })

  /**
   * 初始化打印功能
   */
  const initialize = async () => {
    state.loading.init = true
    state.error = null

    try {
      const success = await printManager.initialize()

      // 确保状态正确更新
      state.isInitialized = success
      state.isConnected = printManager.checkStatus().connected
      if (success) {
        // 初始化后立即获取打印机列表
        await refreshPrinterList()
      }
      return success
    } catch (error) {
      state.error = handleError(error, '初始化失败')
      return false
    } finally {
      state.loading.init = false
    }
  }

  /**
   * 初始化 hiprint - 兼容性函数
   * 为 useTemplateRenderer.js 提供兼容接口
   */
  const initHiprint = async () => {
    return await initialize()
  }

  /**
   * 创建打印模板
   */
  const createTemplate = async (templateData) => {
    state.error = null

    try {

      // 检查打印管理器是否已初始化
      if (!printManager.isInitialized) {
        const initResult = await printManager.initialize()
        if (!initResult) {
          throw new Error('打印管理器初始化失败')
        }
      }
      const template = printManager.createTemplate(templateData)
      state.currentTemplate = template
      return template
    } catch (error) {
      state.error = handleError(error, '创建模板失败')
      throw error
    }
  }

  /**
   * 生成预览 HTML
   */
  const getHtml = (template, data = {}) => {
    state.error = null

    try {
      const targetTemplate = template || state.currentTemplate
      if (!targetTemplate) {
        throw new Error('模板不存在')
      }

      // 使用模板的 getHtml 方法
      const html = targetTemplate.getHtml ? targetTemplate.getHtml(data) : printManager.generatePreview(targetTemplate, data)
      state.previewHtml = html
      return html
    } catch (error) {
      console.error('[usePrintOfficial] 生成预览失败:', error)
      state.error = handleError(error, '生成预览失败')
      return ''
    }
  }

  /**
   * 生成预览 HTML - 兼容性函数
   * 为 useTemplateRenderer.js 提供兼容接口
   */
  const generatePreviewHtml = async (template, data = {}) => {
    try {
      // 确保模板已创建
      let targetTemplate = template || state.currentTemplate
      if (!targetTemplate) {
        // 如果没有模板，先创建模板
        targetTemplate = await createTemplate(template)
      }

      if (!targetTemplate) {
        throw new Error('无法创建模板')
      }

      // 使用模板的 getHtml 方法
      const html = targetTemplate.getHtml ? targetTemplate.getHtml(data) : printManager.generatePreview(targetTemplate, data)
      state.previewHtml = html
      return html
    } catch (error) {
      console.error('[usePrintOfficial] generatePreviewHtml 失败:', error)
      state.error = handleError(error, '生成预览失败')
      throw error
    }
  }

  /**
   * 直接打印
   */
  const printHtml = async (data = {}, options = {}) => {
    state.loading.print = true
    state.error = null

    try {
      if (!state.currentTemplate) {
        throw new Error('打印模板未初始化')
      }
      const result = await printManager.print(state.currentTemplate, data, options)
      message.success('打印任务已发送')
      return result
    } catch (error) {
      state.error = handleError(error, '打印失败')
      throw error
    } finally {
      state.loading.print = false
    }
  }

  /**
   * 多页打印
   */
  const printMulti = async (printItems, options = {}) => {
    state.loading.print = true
    state.error = null

    try {
      if (!state.currentTemplate) {
        throw new Error('打印模板未初始化')
      }

      await printManager.printMulti(printItems, options)
      message.success('多页打印任务已发送')
    } catch (error) {
      state.error = handleError(error, '多页打印失败')
      throw error
    } finally {
      state.loading.print = false
    }
  }

  /**
   * 导出 PDF
   */
  const printPdf = async (data = {}, filename, options = {}) => {
    state.loading.export = true
    state.error = null

    try {
      if (!state.currentTemplate) {
        throw new Error('打印模板未初始化')
      }

      const result = await printManager.exportPdf(state.currentTemplate, data, filename, options)
      message.success('PDF导出成功')
      return result
    } catch (error) {
      state.error = handleError(error, 'PDF导出失败')
      throw error
    } finally {
      state.loading.export = false
    }
  }

  /**
   * 刷新打印机列表
   */
  const refreshPrinterList = async () => {
    state.loading.printers = true
    state.error = null

    try {
      const printers = await printManager.refreshPrinterList()
		console.log('[usePrintOfficial] 刷新打印机列表:', printers)
      state.printerList = printers
      return printers
    } catch (error) {
      state.error = handleError(error, '获取打印机列表失败')
      return []
    } finally {
      state.loading.printers = false
    }
  }

  /**
   * 检查打印服务状态
   */
  const getStatus = () => {
    return printManager.checkStatus()
  }

  /**
   * 设置选中的打印机
   */
  const setSelectedPrinter = (printer) => {
    state.selectedPrinter = printer
  }

  /**
   * 选择打印机 - 兼容性函数
   */
  const selectPrinter = (printer) => {
    setSelectedPrinter(printer)
  }

  /**
   * 错误处理
   */
  const handleError = (error, context = '') => {
    const errorMessage = error?.message || error || '未知错误'
    const fullMessage = context ? `${context}: ${errorMessage}` : errorMessage

    console.error(`[usePrintOfficial] ${fullMessage}`, error)

    return {
      message: fullMessage,
      context,
      timestamp: new Date().toISOString()
    }
  }

  /**
   * 清理资源
   */
  const cleanup = () => {
    printManager.cleanup()
    state.currentTemplate = null
    state.isInitialized = false
    state.isConnected = false
    state.printerList = []
    state.selectedPrinter = null
    state.previewHtml = ''
    state.error = null
  }

  // 计算属性
  const isReady = computed(() => state.isInitialized) // 只需要初始化完成，不要求连接状态
  const hasTemplate = computed(() => !!state.currentTemplate)
  const hasPrinters = computed(() => state.printerList.length > 0)

  return {
    // 状态
    state,

    // 计算属性
    isReady,
    hasTemplate,
    hasPrinters,

    // 方法
    initialize,
    initHiprint,
    createTemplate,
    getHtml,
    generatePreviewHtml,
    printHtml,
    printMulti,
    printPdf,
    refreshPrinterList,
    getStatus,
    setSelectedPrinter,
    selectPrinter,
    cleanup
  }
}
