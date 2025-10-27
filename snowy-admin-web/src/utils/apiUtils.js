import { message } from 'ant-design-vue'
import { ref, reactive, computed } from 'vue'
import { useErrorHandler } from '@/composables/useErrorHandler'

/**
 * API调用通用工具函数
 * 统一处理分页查询、错误处理、加载状态等逻辑
 */

/**
 * 分页查询组合式函数
 * @param {Function} apiFunction - API调用函数
 * @param {Object} defaultParams - 默认查询参数
 * @param {Object} options - 配置选项
 */
export function usePagination(apiFunction, defaultParams = {}, options = {}) {
  // 默认配置
  const defaultOptions = {
    immediate: true, // 是否立即执行查询
    pageSize: 10, // 默认页大小
    showMessage: true, // 是否显示错误消息
    resetOnParamsChange: true, // 参数变化时是否重置到第一页
    cacheKey: null, // 缓存键
    cacheDuration: 5 * 60 * 1000 // 缓存时长（5分钟）
  }
  
  const config = { ...defaultOptions, ...options }

  // 响应式状态
  const state = reactive({
    // 数据相关
    data: [],
    total: 0,
    
    // 分页相关
    current: 1,
    pageSize: config.pageSize,
    
    // 查询参数
    params: { ...defaultParams },
    
    // 状态相关
    loading: false,
    error: null,
    lastUpdateTime: null
  })

  // 计算属性
  const pagination = computed(() => ({
    current: state.current,
    pageSize: state.pageSize,
    total: state.total,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
    pageSizeOptions: ['10', '20', '50', '100'],
    onChange: handlePageChange,
    onShowSizeChange: handlePageSizeChange
  }))
  
  const hasData = computed(() => state.data.length > 0)
  const isEmpty = computed(() => !state.loading && state.data.length === 0)
  const hasError = computed(() => !!state.error)
  
  // 缓存管理
  const cache = new Map()
  
  /**
   * 生成缓存键
   */
  const generateCacheKey = (params) => {
    if (!config.cacheKey) return null
    
    const keyData = {
      baseKey: config.cacheKey,
      params: params,
      current: state.current,
      pageSize: state.pageSize
    }
    
    return JSON.stringify(keyData)
  }
  
  /**
   * 获取缓存数据
   */
  const getCachedData = (cacheKey) => {
    if (!cacheKey) return null
    
    const cached = cache.get(cacheKey)
    if (!cached) return null
    
    // 检查缓存是否过期
    if (Date.now() - cached.timestamp > config.cacheDuration) {
      cache.delete(cacheKey)
      return null
    }
    
    return cached.data
  }
  
  /**
   * 设置缓存数据
   */
  const setCachedData = (cacheKey, data) => {
    if (!cacheKey) return
    
    cache.set(cacheKey, {
      data: data,
      timestamp: Date.now()
    })
  }

  /**
   * 执行查询
   */
  const fetchData = async (resetPage = false) => {
    try {
      state.loading = true
      state.error = null
      
      if (resetPage) {
        state.current = 1
      }
      
      // 构建查询参数
      const queryParams = {
        ...state.params,
        current: state.current,
        size: state.pageSize
      }
      
      // 检查缓存
      const cacheKey = generateCacheKey(queryParams)
      const cachedData = getCachedData(cacheKey)
      
      if (cachedData) {
        state.data = cachedData.records || cachedData.data || []
        state.total = cachedData.total || 0
        state.lastUpdateTime = new Date()
        return cachedData
      }
      
      // 调用API
      const response = await apiFunction(queryParams)
      
      if (response && response.data) {
        const result = response.data
        
        // 处理不同的响应格式
        if (result.records !== undefined) {
          // 标准分页格式
          state.data = result.records || []
          state.total = result.total || 0
        } else if (Array.isArray(result)) {
          // 简单数组格式
          state.data = result
          state.total = result.length
        } else {
          // 其他格式
          state.data = result.data || result.list || []
          state.total = result.total || result.count || 0
        }
        
        state.lastUpdateTime = new Date()
        
        // 设置缓存
        setCachedData(cacheKey, result)
        
        return result
      } else {
        throw new Error('响应数据格式错误')
      }
    } catch (error) {
      const { handleError } = useErrorHandler({
        showMessage: config.showMessage,
        defaultMessage: '查询失败'
      })
      
      const errorInfo = handleError(error, 'fetchData')
      state.error = errorInfo.error.message || '查询失败'
      
      throw error
    } finally {
      state.loading = false
    }
  }

  /**
   * 处理页码变化
   */
  const handlePageChange = (page, pageSize) => {
    state.current = page
    if (pageSize !== state.pageSize) {
      state.pageSize = pageSize
    }
    fetchData()
  }

  /**
   * 处理页大小变化
   */
  const handlePageSizeChange = (current, size) => {
    state.current = 1 // 重置到第一页
    state.pageSize = size
    fetchData()
  }

  /**
   * 搜索（重置到第一页）
   */
  const search = (params = {}) => {
    state.params = { ...state.params, ...params }
    return fetchData(true)
  }

  /**
   * 重置搜索
   */
  const reset = () => {
    state.params = { ...defaultParams }
    state.current = 1
    return fetchData()
  }

  /**
   * 刷新当前页
   */
  const refresh = () => {
    // 清除缓存
    if (config.cacheKey) {
      cache.clear()
    }
    return fetchData()
  }

  /**
   * 更新查询参数
   */
  const updateParams = (params) => {
    const hasChanged = JSON.stringify(state.params) !== JSON.stringify({ ...state.params, ...params })
    
    state.params = { ...state.params, ...params }
    
    if (hasChanged && config.resetOnParamsChange) {
      return search()
    }
    
    return Promise.resolve()
  }

  /**
   * 获取指定行数据
   */
  const getRowData = (index) => {
    return state.data[index] || null
  }

  /**
   * 根据条件查找数据
   */
  const findData = (predicate) => {
    return state.data.find(predicate)
  }

  /**
   * 根据条件过滤数据
   */
  const filterData = (predicate) => {
    return state.data.filter(predicate)
  }

  /**
   * 清空数据
   */
  const clearData = () => {
    state.data = []
    state.total = 0
    state.current = 1
    state.error = null
  }

  /**
   * 获取查询统计信息
   */
  const getStats = () => {
    return {
      total: state.total,
      current: state.current,
      pageSize: state.pageSize,
      totalPages: Math.ceil(state.total / state.pageSize),
      hasData: hasData.value,
      isEmpty: isEmpty.value,
      hasError: hasError.value,
      loading: state.loading,
      lastUpdateTime: state.lastUpdateTime
    }
  }

  // 初始化
  if (config.immediate) {
    fetchData()
  }

  return {
    // 状态
    state,
    
    // 计算属性
    pagination,
    hasData,
    isEmpty,
    hasError,
    
    // 方法
    fetchData,
    search,
    reset,
    refresh,
    updateParams,
    handlePageChange,
    handlePageSizeChange,
    getRowData,
    findData,
    filterData,
    clearData,
    getStats
  }
}

/**
 * 简单的API调用封装
 * @param {Function} apiFunction - API函数
 * @param {Object} options - 配置选项
 */
export function useApiCall(apiFunction, options = {}) {
  const defaultOptions = {
    immediate: false,
    showMessage: true,
    successMessage: null,
    errorMessage: null
  }
  
  const config = { ...defaultOptions, ...options }
  
  const state = reactive({
    data: null,
    loading: false,
    error: null,
    lastCallTime: null
  })
  
  const hasData = computed(() => state.data !== null)
  const hasError = computed(() => !!state.error)
  
  /**
   * 执行API调用
   */
  const execute = async (params = {}) => {
    try {
      state.loading = true
      state.error = null
      
      const response = await apiFunction(params)
      
      state.data = response.data || response
      state.lastCallTime = new Date()
      
      if (config.showMessage && config.successMessage) {
        message.success(config.successMessage)
      }
      
      return response
    } catch (error) {
      const { handleError } = useErrorHandler({
        showMessage: config.showMessage,
        defaultMessage: config.errorMessage || 'API调用失败'
      })
      
      const errorInfo = handleError(error, 'apiCall')
      state.error = errorInfo.error.message || 'API调用失败'
      
      throw error
    } finally {
      state.loading = false
    }
  }
  
  /**
   * 重置状态
   */
  const reset = () => {
    state.data = null
    state.error = null
    state.lastCallTime = null
  }
  
  // 初始化执行
  if (config.immediate) {
    execute()
  }
  
  return {
    state,
    hasData,
    hasError,
    execute,
    reset
  }
}

/**
 * 批量API调用
 * @param {Array} apiCalls - API调用配置数组
 * @param {Object} options - 配置选项
 */
export function useBatchApiCall(apiCalls = [], options = {}) {
  const defaultOptions = {
    concurrent: false, // 是否并发执行
    stopOnError: true, // 遇到错误是否停止
    showMessage: true
  }
  
  const config = { ...defaultOptions, ...options }
  
  const state = reactive({
    results: [],
    loading: false,
    error: null,
    progress: 0,
    completed: 0,
    total: 0
  })
  
  const isCompleted = computed(() => state.completed === state.total)
  const hasError = computed(() => !!state.error)
  const progressPercent = computed(() => {
    return state.total > 0 ? Math.round((state.completed / state.total) * 100) : 0
  })
  
  /**
   * 执行批量调用
   */
  const execute = async (calls = apiCalls) => {
    try {
      state.loading = true
      state.error = null
      state.results = []
      state.completed = 0
      state.total = calls.length
      
      if (config.concurrent) {
        // 并发执行
        const promises = calls.map(async (call, index) => {
          try {
            const result = await call.apiFunction(call.params || {})
            state.completed++
            return { index, success: true, data: result, error: null }
          } catch (error) {
            state.completed++
            return { index, success: false, data: null, error: error.message }
          }
        })
        
        state.results = await Promise.all(promises)
      } else {
        // 顺序执行
        for (let i = 0; i < calls.length; i++) {
          const call = calls[i]
          
          try {
            const result = await call.apiFunction(call.params || {})
            state.results.push({ index: i, success: true, data: result, error: null })
            state.completed++
          } catch (error) {
            const errorResult = { index: i, success: false, data: null, error: error.message }
            state.results.push(errorResult)
            state.completed++
            
            if (config.stopOnError) {
              throw new Error(`第 ${i + 1} 个API调用失败: ${error.message}`)
            }
          }
        }
      }
      
      // 检查是否有失败的调用
      const failedCalls = state.results.filter(r => !r.success)
      if (failedCalls.length > 0 && config.showMessage) {
        message.warning(`${failedCalls.length} 个API调用失败`)
      }
      
      return state.results
    } catch (error) {
      const { handleError } = useErrorHandler({
        showMessage: config.showMessage,
        defaultMessage: '批量API调用失败'
      })
      
      const errorInfo = handleError(error, 'batchApiCall')
      state.error = errorInfo.error.message
      
      throw error
    } finally {
      state.loading = false
    }
  }
  
  /**
   * 重置状态
   */
  const reset = () => {
    state.results = []
    state.error = null
    state.completed = 0
    state.total = 0
    state.progress = 0
  }
  
  /**
   * 获取成功的结果
   */
  const getSuccessResults = () => {
    return state.results.filter(r => r.success).map(r => r.data)
  }
  
  /**
   * 获取失败的结果
   */
  const getFailedResults = () => {
    return state.results.filter(r => !r.success)
  }
  
  return {
    state,
    isCompleted,
    hasError,
    progressPercent,
    execute,
    reset,
    getSuccessResults,
    getFailedResults
  }
}

/**
 * 防抖API调用
 * @param {Function} apiFunction - API函数
 * @param {number} delay - 防抖延迟（毫秒）
 * @param {Object} options - 配置选项
 */
export function useDebouncedApiCall(apiFunction, delay = 300, options = {}) {
  const { execute, ...rest } = useApiCall(apiFunction, options)
  
  let timeoutId = null
  
  const debouncedExecute = (params = {}) => {
    return new Promise((resolve, reject) => {
      if (timeoutId) {
        clearTimeout(timeoutId)
      }
      
      timeoutId = setTimeout(async () => {
        try {
          const result = await execute(params)
          resolve(result)
        } catch (error) {
          reject(error)
        }
      }, delay)
    })
  }
  
  const cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }
  
  return {
    ...rest,
    execute: debouncedExecute,
    cancel
  }
}

/**
 * 通用错误处理（已废弃，请使用 useErrorHandler）
 * @deprecated 请使用 useErrorHandler 组合式函数
 */
export const handleApiError = (error, customMessage = null) => {
  const { handleError } = useErrorHandler({
    showMessage: true,
    defaultMessage: customMessage || '操作失败'
  })
  
  const errorInfo = handleError(error, 'legacyApiError')
  return errorInfo.error.message
}

/**
 * API响应数据格式化
 */
export const formatApiResponse = (response, options = {}) => {
  const defaultOptions = {
    dataKey: 'data',
    totalKey: 'total',
    recordsKey: 'records'
  }
  
  const config = { ...defaultOptions, ...options }
  
  if (!response) {
    return { data: [], total: 0 }
  }
  
  const data = response[config.dataKey] || response
  
  if (Array.isArray(data)) {
    return { data, total: data.length }
  }
  
  if (data && typeof data === 'object') {
    return {
      data: data[config.recordsKey] || data.list || [],
      total: data[config.totalKey] || data.count || 0
    }
  }
  
  return { data: [], total: 0 }
}