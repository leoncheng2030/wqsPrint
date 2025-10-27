/**
 *  Copyright [2022] [http://www.weiqisheng.cn]
 *	wqs采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *	1.请不要删除和修改根目录下的LICENSE文件。
 *	2.请不要删除和修改wqs源码头部的版权声明。
 *	3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 *	4.分发源码时候，请注明软件出处 http://www.weiqisheng.cn
 *	5.不可二次分发开源参与同类竞品，如有想法可联系团队nywqs@outlook.com商议合作。
 *	6.若您的项目无法满足以上几点，需要更多功能代码，获取wqs商业授权许可，请在官网购买授权，地址为 http://www.weiqisheng.cn
 */
// 统一的请求发送
import axios from 'axios'
import qs from 'qs'
import { Modal, message } from 'ant-design-vue'
import sysConfig from '@/config/index'
import tool from '@/utils/tool'
import { convertUrl } from './apiAdaptive'
import { configManager } from '@/config/index'

// 以下这些code需要重新登录
const reloadCodes = [401, 1011007, 1011008]
const errorCodeMap = {
	400: '发出的请求有错误，服务器没有进行新建或修改数据的操作。',
	401: '用户没有权限（令牌、用户名、密码错误）。',
	403: '用户得到授权，但是访问是被禁止的。',
	404: '发出的请求针对的是不存在的记录，服务器没有进行操作。',
	406: '请求的格式不可得。',
	410: '请求的资源被永久删除，且不会再得到的。',
	422: '当创建一个对象时，发生一个验证错误。',
	500: '服务器发生错误，请检查服务器。',
	502: '网关错误。',
	503: '服务不可用，服务器暂时过载或维护。',
	504: '网关超时。'
}

// 定义一个重新登录弹出窗的变量
const loginBack = ref(false)

// 等待配置加载完成
const waitForConfig = async () => {
	let retryCount = 0
	const maxRetries = 50 // 最多等待5秒

	while (!configManager.isConfigLoaded() && retryCount < maxRetries) {
		await new Promise(resolve => setTimeout(resolve, 100))
		retryCount++
	}

	if (!configManager.isConfigLoaded()) {
		console.warn('Config not loaded, using default values')
	}
}

// 创建 axios 实例
const service = axios.create({
	baseURL: '/api', // 初始baseURL，会在请求拦截器中动态更新
	timeout: sysConfig.TIMEOUT // 请求超时时间
})

// 动态更新axios配置
const updateAxiosConfig = () => {
	if (configManager.isConfigLoaded()) {
		service.defaults.timeout = sysConfig.TIMEOUT
		// 更新其他配置...
	}
}

// 监听配置变化
configManager.addListener((config) => {
	updateAxiosConfig()
})

// HTTP request 拦截器
service.interceptors.request.use(
	async (config) => {
		// 确保配置已加载
		if (!configManager.isConfigLoaded()) {
			await waitForConfig()
		}

		const token = tool.data.get('TOKEN')
		if (token) {
			config.headers[sysConfig.TOKEN_NAME] = sysConfig.TOKEN_PREFIX + token
		}
		if (!sysConfig.REQUEST_CACHE && config.method === 'get') {
			config.params = config.params || {}
			config.params._ = new Date().getTime()
		}
		Object.assign(config.headers, sysConfig.HEADERS)
		return config
	},
	(error) => {
		return Promise.reject(error)
	}
)

// 保持重新登录Modal的唯一性
const error = () => {
	loginBack.value = true
	Modal.error({
		title: '提示：',
		okText: '重新登录',
		content: '登录已失效， 请重新登录',
		onOk: () => {
			loginBack.value = false
			tool.data.remove('TOKEN')
			tool.data.remove('USER_INFO')
			tool.data.remove('MENU')
			tool.data.remove('PERMISSIONS')
			window.location.reload()
		}
	})
}

// HTTP response 拦截器
service.interceptors.response.use(
	(response) => {
		// 配置了blob，不处理直接返回文件流
		if (response.config.responseType === 'blob') {
			if (response.status === 200) {
				return response
			} else {
				message.warning('文件下载失败或此文件不存在')
				return
			}
		}
		const data = response.data
		const code = data.code
		if (reloadCodes.includes(code)) {
			if (!loginBack.value) {
				error()
			}
			return
		}
		if (code !== 200) {
			const customErrorMessage = response.config.customErrorMessage
			message.error(customErrorMessage || data.msg)
			return Promise.reject(data)
		} else {
			// 统一成功提示
			const functionName = response.config.url.split('/').pop()
			const apiNameArray = [
				'add',
				'edit',
				'delete',
				'update',
				'grant',
				'reset',
				'stop',
				'pass',
				'disable',
				'enable',
				'revoke',
				'suspend',
				'active',
				'turn',
				'adjust',
				'reject',
				'saveDraft'
			]
			apiNameArray.forEach((apiName) => {
				// 上面去掉接口路径后，方法内包含内置的进行统一提示成功
				if (functionName.includes(apiName)) {
					message.success(data.msg)
				}
			})
		}
		return Promise.resolve(data.data)
	},
	(error) => {
		if (error) {
			const status = 503
			const description = errorCodeMap[status]
			console.error({
				message: '请求错误',
				description
			})
			return Promise.reject(status)
		}
	}
)

// 适配器, 用于适配不同的请求方式
export const baseRequest = (url, value = {}, method = 'post', options = {}) => {
	url = sysConfig.API_URL + convertUrl(url)
	if (method === 'post') {
		// 如果有transformRequest选项，需要特殊处理
		if (options.transformRequest) {
			return service.post(url, value, options)
		}
		return service.post(url, value, options)
	} else if (method === 'get') {
		return service.get(url, { params: value, ...options })
	} else if (method === 'formdata') {
		// form-data表单提交的方式
		return service.post(url, qs.stringify(value), {
			headers: {
				'Content-Type': 'multipart/form-data'
			},
			...options
		})
	} else {
		// 其他请求方式，例如：put、delete
		return service({
			method: method,
			url: url,
			data: value,
			...options
		})
	}
}

export default service
