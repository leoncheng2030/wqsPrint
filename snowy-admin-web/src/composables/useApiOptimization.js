import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'

/**
 * API请求优化工具
 * 提供请求去重、缓存和统一错误处理
 */
export function useApiOptimization() {
  const state = reactive({
    pendingRequests: new Map(),
    cache: new Map(),
    requestCount: 0
  })

  // 请求去重装饰器
  const withDeduplication = (apiCall, cacheKey) => {
    return async (...args) => {
      const key = cacheKey || JSON.stringify(args)
      
      // 检查是否有相同请求正在进行
      if (state.pendingRequests.has(key)) {
        return state.pendingRequests.get(key)
      }

      // 检查缓存
      if (state.cache.has(key)) {
        const cached = state.cache.get(key)
        if (Date.now() - cached.timestamp < 5 * 60 * 1000) { // 5分钟缓存
          return cached.data
        }
        state.cache.delete(key)
      }

      const promise = apiCall(...args)
      state.pendingRequests.set(key, promise)
      state.requestCount++

      try {
        const result = await promise
        
        // 缓存成功结果
        state.cache.set(key, {
          data: result,
          timestamp: Date.now()
        })
        
        return result
      } catch (error) {
        throw error
      } finally {
        state.pendingRequests.delete(key)
      }
    }
  }

  // 统一错误处理
  const handleError = (error, operation = '操作', showMessage = true) => {
    const errorMsg = error?.response?.data?.message || 
                    error?.message || 
                    `${operation}失败`

    if (showMessage) {
      message.error(errorMsg)
    }

    return {
      error: errorMsg,
      code: error?.response?.status || error?.code
    }
  }

  // 清理过期缓存
  const clearExpiredCache = () => {
    const now = Date.now()
    for (const [key, value] of state.cache.entries()) {
      if (now - value.timestamp > 5 * 60 * 1000) {
        state.cache.delete(key)
      }
    }
  }

  // 清理所有缓存
  const clearAllCache = () => {
    state.cache.clear()
    state.pendingRequests.clear()
  }

  // 批量请求处理
  const batchRequest = async (requests) => {
    try {
      const results = await Promise.allSettled(requests)
      
      const successful = []
      const failed = []
      
      results.forEach((result, index) => {
        if (result.status === 'fulfilled') {
          successful.push({ index, data: result.value })
        } else {
          failed.push({ index, error: result.reason })
        }
      })
      
      return { successful, failed }
    } catch (error) {
      throw error
    }
  }

  return {
    state,
    withDeduplication,
    handleError,
    clearExpiredCache,
    clearAllCache,
    batchRequest
  }
}