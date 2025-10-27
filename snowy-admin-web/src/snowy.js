import * as antdvIcons from '@ant-design/icons-vue'
import config from './config'
import tool from './utils/tool'
import { hasPerm } from './utils/permission/index'
import errorHandler from './utils/errorHandler'
import customIcons from './assets/icons/index.js'
import 'highlight.js/styles/atom-one-dark.css'
import hljsCommon from 'highlight.js/lib/common'
import hljsVuePlugin from '@highlightjs/vue-plugin'
import STable from './components/Table/index.vue'
import Ellipsis from './components/Ellipsis/index.vue'
import DragModal from './components/DragModal/index.vue'
// 导入 hiprint 插件
import { default as e2Table } from '@sv-print/plugin-ele-e2table'
import { default as echarts } from '@sv-print/plugin-ele-echarts'
import { default as fabric } from '@sv-print/plugin-ele-fabric'
import {default as pdfPlugin} from '@sv-print/plugin-api-pdf3'
import fontSize from './utils/fontSize'
export default {
	install(app) {
		// 挂载全局对象
		app.config.globalProperties.$CONFIG = config
		app.config.globalProperties.$TOOL = tool
		app.config.globalProperties.hasPerm = hasPerm

		// 注册常用组件
		app.component('STable', STable)
		app.component('Ellipsis', Ellipsis)
		app.component('DragModal', DragModal)

		// 统一注册antdv图标
		for (const icon in antdvIcons) {
			app.component(icon, antdvIcons[icon])
		}
		// 统一注册自定义全局图标
		app.use(customIcons)
		// 注册代码高亮组件 （博客：https://blog.csdn.net/weixin_41897680/article/details/124925222）
		// 注意：解决Vue使用highlight.js build打包发布后样式消失问题，原因是webpack在打包的时候没有把未被使用的代码打包进去，因此，在此处引用一下，看似无意义实则有用
		hljsCommon.highlightAuto('<h1>Highlight.js has been registered successfully!</h1>').value
		app.use(hljsVuePlugin)
		// 提供全局hiprint初始化方法
		try {
			// 从全局获取 hiprint 和 sv-print 组件
			const hiprint = window.hiprint
			const svPrintDesigner = window.svPrintDesigner
			const svPrintHeader = window.svPrintHeader

			if (hiprint) {
				console.log('[snowy] 检测到全局 hiprint 对象，开始检查初始化状态...')
				
				// 如果入口已完成初始化，这里仅进行组件注册与属性挂载，避免重复初始化
				if (window.__hiprintInitialized) {
					console.log('[snowy] 检测到入口已初始化 hiprint，跳过重复注册，仅挂载组件与属性')
				} else {
					console.log('[snowy] 入口未标记初始化，进行基础配置检查...')
					
					// 检查 hiprint 是否已经有基本配置
					if (!hiprint.authKey) {
						console.log('[snowy] hiprint 缺少基础配置，进行基础注册...')
						
						// 仅注册插件与 optionItems，不再设置 host，避免与入口冲突
						const plugins = [
							e2Table({}), 
							echarts({}), 
							fabric({}),
							pdfPlugin({}) // PDF插件
						]
						
						// 根据官方文档，正确的方式是在hiprint.register中注册所有插件
						hiprint.register({
							authKey: config.authKey,
							plugins: plugins
						})
						
						hiprint.setConfig({
							optionItems: [
								fontSize,
							]
						})
						
						console.log('[snowy] 基础配置完成')
					} else {
						console.log('[snowy] hiprint 已有基础配置，跳过重复配置')
					}
				}

				// 由于@sv-print/vue3不提供hiprintVue导出，我们需要手动注册组件
				// 全局注册Designer和Header组件
				if (svPrintDesigner) {
					app.component('SvPrintDesigner', svPrintDesigner)
					console.log('[snowy] Designer 组件注册成功')
				}
				if (svPrintHeader) {
					app.component('SvPrintHeader', svPrintHeader)
					console.log('[snowy] Header 组件注册成功')
				}

				// 将 hiprint 和 sv-print 组件挂载到 Vue 实例的全局属性中
				app.config.globalProperties.$hiprint = hiprint
				if (svPrintDesigner) {
					app.config.globalProperties.$svPrintDesigner = svPrintDesigner
				}
				if (svPrintHeader) {
					app.config.globalProperties.$svPrintHeader = svPrintHeader
				}
				
				console.log('[snowy] hiprint 初始化完成')
			} else {
				console.error('[snowy] 全局 hiprint 对象未找到，请检查 main.js 中的导入与挂载')
			}
		} catch (error) {
			console.error('[snowy] hiprint 初始化失败:', error)
			console.error('[snowy] 错误堆栈:', error.stack)
		}

		// 全局代码错误捕捉
		app.config.errorHandler = errorHandler
	}
}
