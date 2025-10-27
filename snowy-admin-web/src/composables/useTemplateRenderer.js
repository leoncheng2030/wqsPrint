import {computed, nextTick, reactive} from 'vue'
import {message} from 'ant-design-vue'
import {usePrintOfficial} from './usePrintOfficial'
import templateApi from '@/api/label/templateApi'
import recordApi from '@/api/label/recordApi'
import fieldApi from '@/api/label/fieldApi'

/**
 * 模板渲染器 - 优化版
 * 基于 usePrintOfficial，专注于预览和设计器集成
 * 移除冗余功能，提升性能
 */
export function useTemplateRenderer() {
    // 使用核心hiprint功能，避免重复实现（统一使用同一实例）
    const {
        state: coreState,
        initHiprint,
        createTemplate,
        isReady: isHiprintReady,
        generatePreviewHtml
    } = usePrintOfficial()

    // 精简的响应式状态
    const state = reactive({
        // 设计器相关
        designerUtils: null,
        isDesignerInitialized: false,
        saving: false,

        // 模板信息
        templateInfo: {
            name: '未知模板',
            code: ''
        },

        // 字段相关
        dynamicFields: [],
        loadingFields: false,

        // 预览相关（简化版）
        loading: false,
        previewLoading: false,
        recordData: {},
        printData: null,
        printDataList: [],
        currentPageIndex: 0,
        totalPages: 1,
        htmlContent: '',
        previewContentRef: null,

        // 页面HTML缓存
        pageHtmlCache: new Map(),

        // 防重复加载页面
        loadingPages: new Set()
    })

    // 模板实例缓存 - 直接使用 coreState 的实例
    let currentTemplateId = null
    
    // 原始模板数据缓存 - 从接口获取的 templateContent
    let originalTemplateData = null

    // 计算属性：过滤后的主字段
    const filteredMainFields = computed(() => {
        if (!Array.isArray(state.dynamicFields)) return []
        return state.dynamicFields.filter(field => field.fieldScope === 'MAIN')
    })

    // 计算属性：过滤后的明细字段
    const filteredDetailFields = computed(() => {
        if (!Array.isArray(state.dynamicFields)) return []
        return state.dynamicFields.filter(field => field.fieldScope === 'DETAIL')
    })

    // 删除复杂的预处理函数，直接使用原始数据
    const preprocessTemplate = (template) => {
        return template
    }

    // 设计器初始化事件处理 - 简化版
    const handleDesignerInitialized = async (designerEvent) => {
        if (designerEvent?.detail) {
            const {designerUtils} = designerEvent.detail
            state.designerUtils = designerUtils

            if (designerUtils) {
                loadDynamicFields()
            }

            nextTick(() => {
                state.isDesignerInitialized = true
            })
        }
    }

    // 加载动态字段 - 优化版
    const loadDynamicFields = async (templateId) => {
        if (!templateId) return

        state.loadingFields = true
        try {
            const [mainFields, detailFields] = await Promise.all([
                fieldApi.fieldPage({
                    templateId,
                    fieldScope: 'MAIN',
                    current: 1,
                    size: 1000
                }),
                fieldApi.fieldPage({
                    templateId,
                    fieldScope: 'DETAIL',
                    current: 1,
                    size: 1000
                })
            ])

            state.dynamicFields = [
                ...(mainFields?.records || []),
                ...(detailFields?.records || [])
            ]
        } catch (error) {
            message.error('加载动态字段失败: ' + error.message)
        } finally {
            state.loadingFields = false
        }
    }

    // 加载模板数据 - 最简化版
    const loadTemplateData = async (templateId) => {
        if (!templateId) return

        try {
            const response = await templateApi.templateDetail({id: templateId})
            if (!response) return

            // 更新模板信息
            state.templateInfo = {
                name: response.name || '未知模板',
                code: response.code || ''
            }

            // 解析模板数据
            let template = {}
            if (response.templateContent) {
                try {
                    template = JSON.parse(response.templateContent)
                } catch (parseError) {
                    message.error('模板内容格式错误')
                    return
                }
            }

            // 保存原始模板数据
            originalTemplateData = template
            currentTemplateId = templateId

        } catch (error) {
            message.error('加载模板数据失败: ' + error.message)
        }
    }

    // 保存模板 - 最终版
    const saveTemplate = async (templateId) => {
        if (!state.designerUtils) {
            message.error('设计器未初始化完成，请稍后再试')
            return false
        }

        state.saving = true
        try {
            // 确认设计器实例有printTemplate属性
            if (!state.designerUtils.printTemplate) {
                throw new Error('设计器实例缺少printTemplate属性')
            }

            // 从printTemplate获取模板JSON
            const templateJson = state.designerUtils.printTemplate.getJson()
            if (!templateJson) {
                throw new Error('获取模板数据失败')
            }

            const saveData = {
                id: templateId,
                templateContent: JSON.stringify(templateJson)
            }

            await templateApi.handleDesign(saveData)

            // 更新本地缓存
            coreState.currentTemplate = templateJson
            message.success('保存模板成功')
            return true
        } catch (error) {
            message.error('保存模板失败: ' + error.message)
            return false
        } finally {
            state.saving = false
        }
    }

    // 预览模板 - 使用核心功能，确保hiprint正确初始化
    const previewTemplate = async (testData = null) => {
        try {
            // 1. 首先确保hiprint已经初始化
            if (!isHiprintReady.value) {
                message.loading('正在初始化hiprint...', 0)
                await initHiprint()
                message.destroy()

                if (!isHiprintReady.value) {
                    message.error('hiprint初始化失败，无法预览')
                    return
                }
            }

            // 2. 检查设计器实例
            if (!state.designerUtils) {
                message.error('设计器实例未创建，无法预览')
                return
            }

            // 3. 检查当前模板
            if (!coreState.currentTemplate) {
                message.error('模板数据未加载，无法预览')
                return
            }

            // 4. 准备预览数据
            const previewData = testData || {
                title: '预览标题',
                name: '测试名称',
                date: new Date().toLocaleDateString(),
                content: '这是预览内容'
            }

            // 5. 执行预览
            if (state.designerUtils.preview?.show) {
                state.designerUtils.preview.show(previewData)
                message.success('预览已打开')
            } else {
                message.error('预览方法不可用，请检查设计器版本')
            }
        } catch (error) {
            console.error('预览失败:', error)
            message.error('预览失败: ' + (error.message || '未知错误'))
        }
    }

    // 加载打印记录详情 - 优化版
    const loadRecordDetail = async (recordId) => {
        state.loading = true
        try {
            const recordDetail = await recordApi.recordDetail({id: recordId})
            if (!recordDetail) {
                throw new Error('未获取到记录详情')
            }

            state.recordData = {...state.recordData, ...recordDetail}

            // 解析打印数据 - 增强版，处理重复嵌套问题
            if (recordDetail.printData) {
                try {
                    let parsedData = JSON.parse(recordDetail.printData)

                    // 检查并修复重复嵌套问题
                    if (Array.isArray(parsedData)) {
                        // 检查数组中的每个元素是否有重复嵌套
                        state.printDataList = parsedData.map((item, index) => {
                            // 如果发现重复嵌套（如items数组中包含相同的对象），进行清理
                            if (item && typeof item === 'object' && item.items && Array.isArray(item.items)) {
                                // 创建一个新的清理对象
                                const cleanedItem = {
                                    ...item,
                                    items: item.items.map(subItem => {
                                        // 检查子项是否有重复嵌套
                                        if (subItem && typeof subItem === 'object' && subItem.items) {
                                            // 如果子项也有items，只保留必要字段
                                            const {items, ...cleanedSubItem} = subItem
                                            return cleanedSubItem
                                        }
                                        return subItem
                                    })
                                }
                                return cleanedItem
                            }
                            return item
                        })

                        state.totalPages = state.printDataList.length
                        state.printData = state.printDataList[0] || {}
                    } else {
                        // 单页数据，检查是否有重复嵌套
                        if (parsedData && typeof parsedData === 'object' && parsedData.items && Array.isArray(parsedData.items)) {
                            // 清理重复嵌套
                            const cleanedData = {
                                ...parsedData,
                                items: parsedData.items.map(item => {
                                    if (item && typeof item === 'object' && item.items) {
                                        const {items, ...cleanedItem} = item
                                        return cleanedItem
                                    }
                                    return item
                                })
                            }
                            state.printData = cleanedData
                        } else {
                            state.printData = parsedData || {}
                        }
                        state.printDataList = [state.printData]
                        state.totalPages = 1
                    }
                    state.currentPageIndex = 0
                } catch (e) {
                    console.error('打印数据解析失败:', e)
                    state.printData = {rawData: recordDetail.printData}
                    state.printDataList = [state.printData]
                    state.totalPages = 1
                    state.currentPageIndex = 0
                }
            } else {
                state.printData = {}
                state.printDataList = [{}]
                state.totalPages = 1
                state.currentPageIndex = 0
            }

            // 清除页面缓存，因为数据已更新
            state.pageHtmlCache.clear()  // 清除所有页面HTML缓存

            // 加载模板并初始化预览
            if (recordDetail.templateId) {
                await loadTemplateData(recordDetail.templateId)
                await initPreview()
            } else {
                throw new Error('记录中没有模板ID')
            }
        } catch (error) {
            console.error('加载打印记录详情失败:', error)
            message.error('加载打印记录详情失败: ' + error.message)
            await handlePreviewError(error)
        } finally {
            state.loading = false
        }
    }

    // 初始化预览 - 高性能版本，使用核心功能
    const initPreview = async () => {
        try {
            if (!coreState.currentTemplate) {
                message.warning('模板数据为空，无法生成预览')
                return
            }

            if (!state.printData && (!state.printDataList || state.printDataList.length === 0)) {
                throw new Error('打印数据未加载')
            }

            // 防止重复初始化（仅针对预览过程）
            if (state.previewLoading) {
                console.log('[useTemplateRenderer] 预览正在初始化中，跳过重复调用')
                return
            }

            state.previewLoading = true
            try {
                // 生成预览HTML，使用核心功能
                if (state.totalPages > 1) {
                    // 多页模式：懒加载第一页
                    state.pageHtmlCache.clear()  // 清除所有页面HTML缓存
                    state.loadingPages.clear()   // 清除加载状态
                    const firstPageData = state.printDataList[0] || {}
                    console.log('[useTemplateRenderer] 多页模式 - 第一页数据:', firstPageData)
                    console.log('[useTemplateRenderer] 当前模板:', coreState.currentTemplate)
                    const firstPageHtml = await generatePreviewHtml(coreState.currentTemplate, firstPageData)

                    if (firstPageHtml?.trim()) {
                        state.pageHtmlCache.set(0, firstPageHtml)
                        state.htmlContent = firstPageHtml
                        await nextTick()
                        console.log('[useTemplateRenderer] 多页模式预览生成成功')
                        // 预取下一页，避免滚动isEmptytr空白
                        generatePageHtml(1).catch(err => console.warn('预取第2页失败:', err))
                    } else {
                        console.log('firstPageHtml:', firstPageHtml)
                        const errorHtml = `
                        <div style="padding: 40px; text-align: center; border: 2px dashed #ff4d4f; color: #ff4d4f; background: #fff2f0;">
                            <h3>第1页预览生成失败</h3>
                            <p>请检查模板配置和数据格式</p>
                        </div>
                        `
                        state.pageHtmlCache.set(0, errorHtml)
                        state.htmlContent = errorHtml
                    }
                } else {
                    // 单页模式
                    const printDataToUse = state.printData || state.printDataList[0] || {}
                    const html = await generatePreviewHtml(coreState.currentTemplate, printDataToUse)

                    if (html?.trim()) {
                        state.htmlContent = html
                    } else {
                        console.warn('[useTemplateRenderer] 生成的HTML为空，使用默认错误页面')
                        state.htmlContent = `
                            <div style="padding: 40px; text-align: center; border: 2px dashed #ffa500; color: #ffa500; background: #fff7e6;">
                                <h3>预览生成警告</h3>
                                <p>生成的HTML内容为空，请检查模板配置和数据</p>
                                <p style="font-size: 12px; color: #666;">模板ID: ${currentTemplateId}, 数据键: ${Object.keys(printDataToUse).join(', ')}</p>
                            </div>
                        `
                    }
                }
            } catch (error) {
                console.error('预览初始化失败:', error)
                await handlePreviewError(error)
            } finally {
                state.previewLoading = false
            }
        } catch (error) {
            console.error('预览初始化失败:', error)
            await handlePreviewError(error)
        }
    }

    // 预览错误处理 - 简化版
    const handlePreviewError = async (error) => {
        console.error('预览生成错误:', error)

        const errorHtml = `
        <div style="padding: 40px; text-align: center; background: white; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
            <div style="color: #ff4d4f; font-size: 16px; margin-bottom: 12px;">
                <i class="anticon anticon-warning-circle"></i>
                预览生成失败
            </div>
            <div style="color: #666; font-size: 14px;">
                ${error.message || '无法生成预览HTML'}
            </div>
        </div>
        `
        state.htmlContent = errorHtml
    }

    // 生成页面HTML - 使用核心功能
    const generatePageHtml = async (pageIndex, retryCount = 0) => {
        if (state.pageHtmlCache.has(pageIndex)) {
            return state.pageHtmlCache.get(pageIndex)
        }
        try {
            const pageData = state.printDataList[pageIndex] || {}
            // 检查数据是否为空或undefined
            if (!pageData || (typeof pageData === 'object' && Object.keys(pageData).length === 0)) {
                console.warn(`第${pageIndex + 1}页数据为空，使用默认空对象`)
            }
            // 数据预处理，确保不会导致hiprint内部错误
            const processedPageData = { ...pageData }

            // 尝试生成HTML，添加重试机制
            let html
            try {
                html = await generatePreviewHtml(coreState.currentTemplate, processedPageData)
            } catch (htmlError) {
                console.error(`第${pageIndex + 1}页HTML生成失败:`, htmlError)
            }
            if (html && html.trim()) {
                state.pageHtmlCache.set(pageIndex, html)
                console.log(`[useTemplateRenderer] 已缓存第${pageIndex + 1}页HTML`)
                // 预取下一页以提升滚动体验
                const nextIndex = pageIndex + 1
                if (nextIndex < state.totalPages && !state.pageHtmlCache.has(nextIndex) && !state.loadingPages?.has(nextIndex)) {
                    // 异步预取，避免阻塞当前渲染
                    Promise.resolve().then(() => {
                        if (!state.loadingPages) state.loadingPages = new Set()
                        state.loadingPages.add(nextIndex)
                        generatePageHtml(nextIndex).catch(err => console.warn(`预取第${nextIndex + 1}页失败:`, err)).finally(() => {
                            state.loadingPages.delete(nextIndex)
                        })
                    })
                }
                return html
            } else {
                console.warn(`第${pageIndex + 1}页生成为空或无效，写入错误占位`)
                const errorHtml = `
                    <div style="padding: 40px; text-align: center; border: 2px dashed #ff4d4f; color: #ff4d4f; background: #fff2f0;">
                        <h3>第${pageIndex + 1}页预览生成失败</h3>
                        <p>请检查模板配置与该页数据</p>
                    </div>
                `
                state.pageHtmlCache.set(pageIndex, errorHtml)
                return null
            }
        } catch (error) {
            console.error(`第${pageIndex + 1}页HTML生成失败:`, error)
            const errorHtml = `
                <div style="padding: 40px; text-align: center; border: 2px dashed #ff4d4f; color: #ff4d4f; background: #fff2f0;">
                    <h3>第${pageIndex + 1}页预览生成异常</h3>
                    <p>${error.message || '未知错误'}</p>
                </div>
            `
            state.pageHtmlCache.set(pageIndex, errorHtml)
            return null
        }
    }

    // 获取页面HTML - 同步版本
    const getPageHtml = (pageIndex) => {
        if (state.pageHtmlCache.has(pageIndex)) {
            return state.pageHtmlCache.get(pageIndex)
        }
        // 不在渲染期间触发生成，避免递归更新
        return `
            <div style="padding: 40px; text-align: center; border: 2px dashed #1890ff; color: #1890ff; background: #f0f8ff;">
                <h3>第${pageIndex + 1}页加载中...</h3>
                <p>请稍候...</p>
            </div>
            `
    }

    // 切换到指定页面（支持懒加载）
    const changePage = async (pageIndex) => {
        if (pageIndex < 0 || pageIndex >= state.totalPages) {
            console.warn(`页面索引 ${pageIndex} 超出范围 [0, ${state.totalPages - 1}]`)
            return
        }

        // 懒加载目标页面
        if (!state.pageHtmlCache.has(pageIndex)) {
            await generatePageHtml(pageIndex)
        }

        // 更新当前页面状态
        state.currentPageIndex = pageIndex
        state.printData = state.printDataList[pageIndex]

        // 更新显示的HTML内容
        const pageHtml = state.pageHtmlCache.get(pageIndex)
        if (pageHtml) {
            state.htmlContent = pageHtml
        }
    }

    // 清除指定页面的缓存，强制重新生成
    const clearPageCache = (pageIndex) => {
        if (pageIndex >= 0 && pageIndex < state.totalPages) {
            state.pageHtmlCache.delete(pageIndex)
            console.log(`已清除第${pageIndex + 1}页的缓存`)
        }
    }

    // 重新生成指定页面
    const regeneratePage = async (pageIndex) => {
        if (pageIndex < 0 || pageIndex >= state.totalPages) {
            console.warn(`页面索引 ${pageIndex} 超出范围 [0, ${state.totalPages - 1}]`)
            return
        }

        // 清除缓存
        clearPageCache(pageIndex)

        // 重新生成
        await generatePageHtml(pageIndex)

        // 如果是当前页面，更新显示内容
        if (pageIndex === state.currentPageIndex) {
            const pageHtml = state.pageHtmlCache.get(pageIndex)
            if (pageHtml) {
                state.htmlContent = pageHtml
            }
        }
    }

    // 节流函数
    const throttle = (func, wait) => {
        let timeout
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout)
                func(...args)
            }
            clearTimeout(timeout)
            timeout = setTimeout(later, wait)
        }
    }

    // 滚动处理 - 简化版，添加防抖机制
    const handleScrollInternal = async (e) => {
        if (state.totalPages <= 1) return

        // 优先使用事件目标作为滚动容器，其次回退到状态中的容器引用
        const container = (e && e.target) || state.previewContentRef
        if (!container) return

        const scrollTop = container.scrollTop
        const containerHeight = container.clientHeight
        const centerY = scrollTop + containerHeight / 2
        const scrollBottom = scrollTop + containerHeight
        const maxScroll = container.scrollHeight
        const nearBottomThreshold = 2

        const pageElements = container.querySelectorAll('.page-content-wrapper')
        let visiblePage = 0

        pageElements.forEach((pageEl, index) => {
            const pageTop = pageEl.offsetTop
            const pageBottom = pageTop + pageEl.offsetHeight
            if (centerY >= pageTop && centerY <= pageBottom) {
                visiblePage = index
            }
        })

        const isAtBottom = scrollBottom >= maxScroll - nearBottomThreshold
        if (isAtBottom) {
            visiblePage = state.totalPages - 1
        }
        console.log('[useTemplateRenderer] 滚动定位: ', { centerY, visiblePage, isAtBottom, scrollTop, containerHeight, maxScroll })

        if (visiblePage !== state.currentPageIndex) {
            state.currentPageIndex = visiblePage
            state.printData = state.printDataList[visiblePage]

            // 懒加载当前页面 - 添加防重复检查
            if (!state.pageHtmlCache.has(visiblePage) && !state.loadingPages?.has(visiblePage)) {
                if (!state.loadingPages) {
                    state.loadingPages = new Set()
                }
                console.log(`[useTemplateRenderer] 触发懒加载: 第${visiblePage + 1}页`)
                state.loadingPages.add(visiblePage)
                try {
                    await generatePageHtml(visiblePage)
                } finally {
                    state.loadingPages.delete(visiblePage)
                }
            }
        }
    }

    const handleScroll = throttle(handleScrollInternal, 100)

    // 清理预览数据
    const clearPreviewData = () => {
        state.recordData = {}
        state.printData = null
        state.printDataList = []
        state.currentPageIndex = 0
        state.totalPages = 1
        state.htmlContent = ''
        state.pageHtmlCache.clear()  // 清除所有页面HTML缓存
        state.loadingPages.clear()   // 清除加载状态
        currentTemplateId = null
    }

    // 检查设计器是否就绪
    const isDesignerReady = () => !!state.designerUtils

    // 清理资源
    const dispose = () => {
        state.designerUtils = null
        state.isDesignerInitialized = false
        state.dynamicFields = []
        currentTemplateId = null
        clearPreviewData()
    }

    return {
        // 状态
        state,
        templateData: computed(() => originalTemplateData || {}), // 修复：返回原始模板数据（从接口的 templateContent）
        hiprintTemplate: computed(() => coreState.currentTemplate), // 新增：导出 hiprint 实例
        templateInfo: computed(() => state.templateInfo),
        dynamicFields: computed(() => state.dynamicFields),
        loadingFields: computed(() => state.loadingFields),
        designerUtils: computed(() => state.designerUtils),
        isDesignerInitialized: computed(() => state.isDesignerInitialized),
        saving: computed(() => state.saving),

        // 字段相关
        filteredMainFields,
        filteredDetailFields,

        // 预览相关
        loading: computed(() => state.loading),
        recordData: computed(() => state.recordData),
        printData: computed(() => state.printData),
        printDataList: computed(() => state.printDataList),
        currentPageIndex: computed(() => state.currentPageIndex),
        totalPages: computed(() => state.totalPages),
        htmlContent: computed(() => state.htmlContent),

        // 方法
        handleDesignerInitialized,
        loadDynamicFields,
        loadTemplateData,
        saveTemplate,
        previewTemplate,
        loadRecordDetail,
        initPreview,
        generatePageHtml,
        getPageHtml,
        changePage,
        clearPageCache,
        regeneratePage,
        handleScroll,
        clearPreviewData,
        isDesignerReady,
        dispose,
    }
}
