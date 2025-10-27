import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import fieldApi from '@/api/label/fieldApi'
import tool from '@/utils/tool'

/**
 * 字段管理组合式函数
 * 专门处理动态字段的加载、缓存和验证
 */
export function useFieldManager() {
  // 响应式状态
  const state = reactive({
    dynamicFields: [],
    fieldOptionsCache: {},
    loading: false,
    error: null
  })

  // 计算属性
  const mainFields = computed(() => {
    return state.dynamicFields.filter(field => field.fieldScope === 'MAIN')
  })

  const detailFields = computed(() => {
    return state.dynamicFields.filter(field => field.fieldScope === 'DETAIL')
  })

  // 加载模板字段 - 高性能版本
  const loadTemplateFields = async (templateId) => {
    if (!templateId) return []

    state.loading = true
    state.error = null

    try {
      // 并行加载主字段和明细字段
      const [mainFieldsRes, detailFieldsRes] = await Promise.all([
        fieldApi.fieldPage({
          templateId,
          fieldScope: 'MAIN',
          status: 'ENABLE',
          size: 500
        }),
        fieldApi.fieldPage({
          templateId,
          fieldScope: 'DETAIL',
          status: 'ENABLE',
          size: 500
        })
      ])

      const allFields = [
        ...(mainFieldsRes?.records || []),
        ...(detailFieldsRes?.records || [])
      ]

      // 解析字段配置
      const processedFields = allFields.map(field => ({
        ...field,
        ...parseFieldConfig(field)
      }))

      state.dynamicFields = processedFields

      // 并行加载字段选项
      await loadFieldOptions(processedFields)

      return processedFields
    } catch (error) {
      state.error = error.message
      message.error('加载模板字段失败: ' + error.message)
      throw error
    } finally {
      state.loading = false
    }
  }

  // 解析字段配置
  const parseFieldConfig = (field) => {
    const config = {}
    
    if (field.optionsData) {
      try {
        const options = JSON.parse(field.optionsData)
        
        // 解析不同类型的配置
        if (options.dateConfig) config.dateConfig = options.dateConfig
        if (options.dynamicBarcodeConfig) config.dynamicBarcodeConfig = options.dynamicBarcodeConfig
        if (options.inputConfig) config.inputConfig = options.inputConfig
        if (options.qrCodeConfig) config.qrCodeConfig = options.qrCodeConfig
      } catch (e) {
        // 静默失败，使用默认配置
      }
    }

    return config
  }

  // 加载字段选项
  const loadFieldOptions = async (fields) => {
    const optionsFields = fields.filter(field => 
      ['SELECT', 'RADIO', 'CHECKBOX'].includes(field.inputType)
    )

    // 并行加载所有选项
    await Promise.all(
      optionsFields.map(field => loadSingleFieldOptions(field))
    )
  }

  // 加载单个字段选项
  const loadSingleFieldOptions = async (field) => {
    try {
      let options = []

      if (field.optionsData) {
        const parsedConfig = JSON.parse(field.optionsData)
        
        // 静态选项
        if (parsedConfig.staticOptions) {
          const staticOptions = typeof parsedConfig.staticOptions === 'string'
            ? JSON.parse(parsedConfig.staticOptions)
            : parsedConfig.staticOptions
          
          if (Array.isArray(staticOptions)) {
            options = staticOptions.map(option => ({
              label: option.label || option.value,
              value: option.value
            }))
          }
        }

        // 字典选项
        if (parsedConfig.dictTypeCode && !options.length) {
          const dictOptions = tool.dictList(parsedConfig.dictTypeCode)
          if (dictOptions?.length) {
            options = dictOptions
          }
        }
      }

      // 向后兼容
      if (!options.length && field.dictTypeCode) {
        const dictOptions = tool.dictList(field.dictTypeCode)
        if (dictOptions?.length) {
          options = dictOptions
        }
      }

      state.fieldOptionsCache[field.fieldKey] = options
    } catch (error) {
      state.fieldOptionsCache[field.fieldKey] = []
    }
  }

  // 获取字段选项
  const getFieldOptions = (field) => {
    return state.fieldOptionsCache[field.fieldKey] || []
  }

  // 设置字段默认值
  const setFieldDefaultValue = (field) => {
    if (field.optionsData) {
      try {
        const options = JSON.parse(field.optionsData)
        if (options.defaultValue !== undefined) {
          if (field.inputType === 'CHECKBOX' || 
              (field.inputType === 'SELECT' && field.isMultiple === '1')) {
            return Array.isArray(options.defaultValue) 
              ? options.defaultValue 
              : []
          }
          return options.defaultValue
        }
      } catch (e) {
        // 静默失败
      }
    }

    // 默认值逻辑
    if (field.inputType === 'CHECKBOX' || 
        (field.inputType === 'SELECT' && field.isMultiple === '1')) {
      return []
    } else if (field.inputType === 'DATE') {
      return new Date().toISOString().slice(0, 10)
    } else if (field.inputType === 'DATE_RANGE') {
      const start = new Date().toISOString().slice(0, 10)
      const end = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10)
      return `${start}-${end}`
    }
    
    return ''
  }

  // 获取字段验证规则
  const getFieldRules = (field) => {
    const rules = []

    if (field.isRequired === '1') {
      const message = field.placeholder || `请输入${field.title}`
      rules.push({ required: true, message })
    }

    return rules
  }

  // 清理缓存
  const clearCache = () => {
    state.dynamicFields = []
    state.fieldOptionsCache = {}
    state.error = null
  }

  return {
    // 状态
    state,
    
    // 计算属性
    mainFields,
    detailFields,
    
    // 方法
    loadTemplateFields,
    parseFieldConfig,
    loadFieldOptions,
    getFieldOptions,
    setFieldDefaultValue,
    getFieldRules,
    clearCache
  }
}