import { ref, computed, watch, shallowRef, markRaw, nextTick } from 'vue'
import { debounce, throttle } from 'lodash-es'

/**
 * 性能优化组合式函数
 * 提供各种性能优化工具和方法
 */
export function usePerformanceOptimization() {
  /**
   * 防抖计算属性
   * @param {Function} getter - 计算函数
   * @param {number} delay - 防抖延迟时间
   * @returns {ComputedRef} 防抖后的计算属性
   */
  const debouncedComputed = (getter, delay = 300) => {
    const value = ref()
    const debouncedGetter = debounce(() => {
      value.value = getter()
    }, delay)
    
    // 立即执行一次
    value.value = getter()
    
    return computed({
      get: () => {
        debouncedGetter()
        return value.value
      },
      set: (newValue) => {
        value.value = newValue
      }
    })
  }

  /**
   * 节流计算属性
   * @param {Function} getter - 计算函数
   * @param {number} delay - 节流延迟时间
   * @returns {ComputedRef} 节流后的计算属性
   */
  const throttledComputed = (getter, delay = 100) => {
    const value = ref()
    const throttledGetter = throttle(() => {
      value.value = getter()
    }, delay)
    
    // 立即执行一次
    value.value = getter()
    
    return computed({
      get: () => {
        throttledGetter()
        return value.value
      },
      set: (newValue) => {
        value.value = newValue
      }
    })
  }

  /**
   * 缓存计算属性
   * @param {Function} getter - 计算函数
   * @param {Function} keyGetter - 缓存键生成函数
   * @returns {ComputedRef} 带缓存的计算属性
   */
  const cachedComputed = (getter, keyGetter) => {
    const cache = new Map()
    
    return computed(() => {
      const key = keyGetter ? keyGetter() : 'default'
      
      if (cache.has(key)) {
        return cache.get(key)
      }
      
      const result = getter()
      cache.set(key, result)
      
      // 限制缓存大小，避免内存泄漏
      if (cache.size > 100) {
        const firstKey = cache.keys().next().value
        cache.delete(firstKey)
      }
      
      return result
    })
  }

  /**
   * 浅层响应式引用
   * 用于大型对象或数组，只监听引用变化，不监听内部属性变化
   * @param {any} value - 初始值
   * @returns {ShallowRef} 浅层响应式引用
   */
  const useShallowReactive = (value) => {
    return shallowRef(value)
  }

  /**
   * 标记为原始对象
   * 防止对象被Vue响应式系统处理
   * @param {any} obj - 要标记的对象
   * @returns {any} 标记后的对象
   */
  const useMarkRaw = (obj) => {
    return markRaw(obj)
  }

  /**
   * 优化的监听器
   * @param {Function|Ref} source - 监听源
   * @param {Function} callback - 回调函数
   * @param {Object} options - 选项
   * @returns {Function} 停止监听的函数
   */
  const optimizedWatch = (source, callback, options = {}) => {
    const {
      debounce: debounceDelay,
      throttle: throttleDelay,
      immediate = false,
      deep = false,
      flush = 'pre'
    } = options

    let optimizedCallback = callback

    if (debounceDelay) {
      optimizedCallback = debounce(callback, debounceDelay)
    } else if (throttleDelay) {
      optimizedCallback = throttle(callback, throttleDelay)
    }

    return watch(source, optimizedCallback, {
      immediate,
      deep,
      flush
    })
  }

  /**
   * 虚拟滚动优化
   * @param {Ref} containerRef - 容器引用
   * @param {Ref} dataSource - 数据源
   * @param {number} itemHeight - 每项高度
   * @param {number} visibleCount - 可见项数量
   * @returns {Object} 虚拟滚动相关数据和方法
   */
  const useVirtualScroll = (containerRef, dataSource, itemHeight = 40, visibleCount = 10) => {
    const scrollTop = ref(0)
    const startIndex = computed(() => Math.floor(scrollTop.value / itemHeight))
    const endIndex = computed(() => Math.min(startIndex.value + visibleCount, dataSource.value.length))
    const visibleData = computed(() => dataSource.value.slice(startIndex.value, endIndex.value))
    
    const totalHeight = computed(() => dataSource.value.length * itemHeight)
    const offsetY = computed(() => startIndex.value * itemHeight)

    const handleScroll = throttle((event) => {
      scrollTop.value = event.target.scrollTop
    }, 16) // 约60fps

    return {
      scrollTop,
      startIndex,
      endIndex,
      visibleData,
      totalHeight,
      offsetY,
      handleScroll
    }
  }

  /**
   * 组件懒加载
   * @param {Function} importFn - 动态导入函数
   * @param {Object} options - 选项
   * @returns {Object} 懒加载组件配置
   */
  const useLazyComponent = (importFn, options = {}) => {
    const {
      loading: LoadingComponent,
      error: ErrorComponent,
      delay = 200,
      timeout = 3000
    } = options

    return {
      component: importFn,
      loading: LoadingComponent,
      error: ErrorComponent,
      delay,
      timeout
    }
  }

  /**
   * 内存泄漏防护
   * @returns {Object} 清理函数集合
   */
  const useMemoryGuard = () => {
    const cleanupFunctions = []

    const addCleanup = (fn) => {
      cleanupFunctions.push(fn)
    }

    const cleanup = () => {
      cleanupFunctions.forEach(fn => {
        try {
          fn()
        } catch (error) {
          console.warn('清理函数执行失败:', error)
        }
      })
      cleanupFunctions.length = 0
    }

    return {
      addCleanup,
      cleanup
    }
  }

  /**
   * DOM操作优化
   * @returns {Object} 优化的DOM操作方法
   */
  const useDOMOptimization = () => {
    /**
     * 批量DOM更新
     * @param {Function} updateFn - 更新函数
     */
    const batchDOMUpdate = (updateFn) => {
      nextTick(() => {
        updateFn()
      })
    }

    /**
     * 防抖的DOM更新
     * @param {Function} updateFn - 更新函数
     * @param {number} delay - 延迟时间
     */
    const debouncedDOMUpdate = debounce((updateFn) => {
      nextTick(() => {
        updateFn()
      })
    }, 100)

    /**
     * 节流的DOM更新
     * @param {Function} updateFn - 更新函数
     * @param {number} delay - 延迟时间
     */
    const throttledDOMUpdate = throttle((updateFn) => {
      nextTick(() => {
        updateFn()
      })
    }, 16) // 约60fps

    return {
      batchDOMUpdate,
      debouncedDOMUpdate,
      throttledDOMUpdate
    }
  }

  return {
    debouncedComputed,
    throttledComputed,
    cachedComputed,
    useShallowReactive,
    useMarkRaw,
    optimizedWatch,
    useVirtualScroll,
    useLazyComponent,
    useMemoryGuard,
    useDOMOptimization
  }
}

/**
 * 表单性能优化
 * @returns {Object} 表单优化相关方法
 */
export function useFormPerformanceOptimization() {
  /**
   * 优化的字段监听
   * @param {Ref} formData - 表单数据
   * @param {Function} validator - 验证函数
   * @param {number} delay - 防抖延迟
   * @returns {Object} 优化后的验证方法
   */
  const optimizedFieldValidation = (formData, validator, delay = 300) => {
    const validationErrors = ref({})
    const isValidating = ref(false)

    const debouncedValidate = debounce(async (fieldName, value) => {
      isValidating.value = true
      try {
        const error = await validator(fieldName, value)
        if (error) {
          validationErrors.value[fieldName] = error
        } else {
          delete validationErrors.value[fieldName]
        }
      } catch (error) {
        validationErrors.value[fieldName] = '验证失败'
      } finally {
        isValidating.value = false
      }
    }, delay)

    const validateField = (fieldName) => {
      const value = formData.value[fieldName]
      debouncedValidate(fieldName, value)
    }

    return {
      validationErrors,
      isValidating,
      validateField
    }
  }

  /**
   * 大数据表单优化
   * @param {Ref} formData - 表单数据
   * @returns {Object} 优化后的表单操作方法
   */
  const optimizedLargeFormData = (formData) => {
    // 使用浅层响应式减少深度监听开销
    const shallowFormData = shallowRef(formData.value)

    const updateField = (fieldName, value) => {
      // 创建新对象触发响应式更新
      shallowFormData.value = {
        ...shallowFormData.value,
        [fieldName]: value
      }
    }

    const batchUpdateFields = (updates) => {
      shallowFormData.value = {
        ...shallowFormData.value,
        ...updates
      }
    }

    return {
      formData: shallowFormData,
      updateField,
      batchUpdateFields
    }
  }

  return {
    optimizedFieldValidation,
    optimizedLargeFormData
  }
}

/**
 * 列表性能优化
 * @returns {Object} 列表优化相关方法
 */
export function useListPerformanceOptimization() {
  /**
   * 优化的列表搜索
   * @param {Ref} dataSource - 数据源
   * @param {Function} searchFn - 搜索函数
   * @param {number} delay - 防抖延迟
   * @returns {Object} 优化后的搜索方法
   */
  const optimizedListSearch = (dataSource, searchFn, delay = 300) => {
    const searchKeyword = ref('')
    const isSearching = ref(false)
    const searchResults = ref([])

    const debouncedSearch = debounce(async (keyword) => {
      if (!keyword.trim()) {
        searchResults.value = dataSource.value
        return
      }

      isSearching.value = true
      try {
        const results = await searchFn(keyword, dataSource.value)
        searchResults.value = results
      } catch (error) {
        console.error('搜索失败:', error)
        searchResults.value = []
      } finally {
        isSearching.value = false
      }
    }, delay)

    watch(searchKeyword, (newKeyword) => {
      debouncedSearch(newKeyword)
    })

    // 初始化搜索结果
    searchResults.value = dataSource.value

    return {
      searchKeyword,
      isSearching,
      searchResults
    }
  }

  /**
   * 优化的列表排序
   * @param {Ref} dataSource - 数据源
   * @returns {Object} 优化后的排序方法
   */
  const optimizedListSort = (dataSource) => {
    const sortConfig = ref({ field: '', order: '' })
    
    const sortedData = computed(() => {
      if (!sortConfig.value.field || !sortConfig.value.order) {
        return dataSource.value
      }

      return [...dataSource.value].sort((a, b) => {
        const aValue = a[sortConfig.value.field]
        const bValue = b[sortConfig.value.field]
        
        if (sortConfig.value.order === 'asc') {
          return aValue > bValue ? 1 : -1
        } else {
          return aValue < bValue ? 1 : -1
        }
      })
    })

    const updateSort = (field, order) => {
      sortConfig.value = { field, order }
    }

    return {
      sortConfig,
      sortedData,
      updateSort
    }
  }

  return {
    optimizedListSearch,
    optimizedListSort
  }
}