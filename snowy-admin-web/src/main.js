import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 引入 sv-print 及其样式文件
import 'sv-print/dist/style.css'
// 导入 hiprint 相关依赖并挂载到全局
import { hiprint } from '@sv-print/hiprint'
// 注意：@sv-print/vue3不提供hiprintVue导出，需要直接导入Designer和Header组件
import { Designer, Header } from '@sv-print/vue3'

// 新增：导入配置与工具，用于在入口处完成 hiprint 初始化与 host 设置
import config from './config'
import tool from './utils/tool'
// 新增：导入 hiprint 插件（在入口处统一注册）
import { default as e2Table } from '@sv-print/plugin-ele-e2table'
import { default as echarts } from '@sv-print/plugin-ele-echarts'
import { default as fabric } from '@sv-print/plugin-ele-fabric'
import { default as pdfPlugin } from '@sv-print/plugin-api-pdf3'
import fontSize from './utils/fontSize'

// 将 hiprint 挂载到全局 window 对象
window.hiprint = hiprint
// 将Designer和Header组件挂载到全局，以便其他地方使用
window.svPrintDesigner = Designer
window.svPrintHeader = Header

// 新增：在入口处初始化 hiprint（设置 host、注册插件、配置 optionItems）
try {
  const hip = window.hiprint
  if (hip) {
    // 读取打印服务器 host（优先使用 SNOWY_SYS_BASE_CONFIG 与 WQS_SYS_BASE_CONFIG 的打印服务器地址（优先 SNOWY），并修正日志前缀为 [main]；2) 保持在入口处设置 host 并注册插件；3) 新增统一设置 optionItems（fontSize），以便从 snowy.js 移除兜底初始化。
    let printHost = null
    try {
      const sysBaseConfigSnowy = tool.data.get('SNOWY_SYS_BASE_CONFIG')
      const sysBaseConfigWqs = tool.data.get('WQS_SYS_BASE_CONFIG')
      console.log('[main] localStorage SNOWY_SYS_BASE_CONFIG:', sysBaseConfigSnowy)
      console.log('[main] localStorage WQS_SYS_BASE_CONFIG:', sysBaseConfigWqs)
      if (sysBaseConfigSnowy && sysBaseConfigSnowy.SNOWY_SYS_PRINT_HOST) {
        printHost = sysBaseConfigSnowy.SNOWY_SYS_PRINT_HOST
        console.log('[main] 使用 SNOWY_SYS_BASE_CONFIG.SNOWY_SYS_PRINT_HOST:', printHost)
      } else if (sysBaseConfigWqs && sysBaseConfigWqs.SNOWY_SYS_PRINT_HOST) {
        printHost = sysBaseConfigWqs.SNOWY_SYS_PRINT_HOST
        console.log('[main] 使用 WQS_SYS_BASE_CONFIG.SNOWY_SYS_PRINT_HOST:', printHost)
      } else {
        printHost = config.SYS_BASE_CONFIG.SNOWY_SYS_PRINT_HOST
        console.log('[main] 使用默认配置 SYS_BASE_CONFIG.SNOWY_SYS_PRINT_HOST:', printHost)
      }
    } catch (configError) {
      console.warn('[main] 获取打印服务器配置失败，使用默认配置:', configError)
      printHost = config.SYS_BASE_CONFIG.SNOWY_SYS_PRINT_HOST
    }
    
    // 在注册前设置 host，确保 hiwebSocket 使用正确的地址
    if (typeof hip.setConfig === 'function' && printHost) {
      try {
        console.log('[main] 打印服务器 host:', printHost)
        hip.setConfig({ host: printHost })
        console.log('[main] ✅ 通过 hiprint.setConfig 设置 host 成功:', printHost)
      } catch (setConfigError) {
        console.warn('[main] ⚠️ hiprint.setConfig 设置 host 失败:', setConfigError)
      }
    }

    // 统一注册 hiprint 插件
    const plugins = [
      e2Table({}),
      echarts({}),
      fabric({}),
      pdfPlugin({})
    ]
    hip.register({
      authKey: config.authKey,
      plugins,
      // 保持入口处统一设置打印服务器 host
      host: printHost
    })
    
    // 统一设置可选项（如字体大小）
    hip.setConfig({
      optionItems: [
        fontSize,
      ]
    })

    // 标记 hiprint 初始化完成
    window.__hiprintInitialized = true
    console.log('[main] ✅ hiprint 初始化完成，已标记初始化状态')
  } else {
    console.error('❌ hiprint 未找到，请检查 @sv-print/hiprint 是否正确加载')
  }
} catch (error) {
  console.error('在入口处初始化 hiprint 失败:', error)
}

import './style/index.less'
import snowy from './snowy'
import i18n from './locales'
import './tailwind.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Antd)
app.use(i18n)
app.use(snowy)

// 将 hiprint 挂载为全局属性
app.config.globalProperties.$hiprint = hiprint

// 挂载app
app.mount('#app')