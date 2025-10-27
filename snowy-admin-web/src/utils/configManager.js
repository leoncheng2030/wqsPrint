/**
 * 配置管理器
 * 用于动态管理应用配置
 */
class ConfigManager {
	constructor() {
		this.config = null
		this.listeners = []
		this.isLoaded = false
	}

	// 加载配置
	async loadConfig() {
		try {
			const response = await fetch('/config.json')
			if (response.ok) {
				this.config = await response.json()
				this.isLoaded = true
				this.notifyListeners()
				return this.config
			}
		} catch (error) {
			console.warn('Failed to load external config:', error)
		}
		return null
	}

	// 获取配置
	getConfig() {
		return this.config
	}

	// 获取API地址
	getApiUrl() {
		const env = import.meta.env.MODE || 'development'
		if (env === 'production' && this.config?.baseURL_production) {
			return this.config.baseURL_production
		}
		return this.config?.baseURL || import.meta.env.VITE_API_BASEURL || '/api'
	}

	// 获取微服务配置
	getCloudServer() {
		return this.config?.cloudServer || false
	}

	// 检查配置是否已加载
	isConfigLoaded() {
		return this.isLoaded
	}

	// 添加监听器
	addListener(listener) {
		this.listeners.push(listener)
	}

	// 移除监听器
	removeListener(listener) {
		const index = this.listeners.indexOf(listener)
		if (index > -1) {
			this.listeners.splice(index, 1)
		}
	}

	// 通知所有监听器
	notifyListeners() {
		this.listeners.forEach((listener) => {
			try {
				listener(this.config)
			} catch (error) {
				console.error('Config listener error:', error)
			}
		})
	}

	// 重新加载配置
	async reloadConfig() {
		this.isLoaded = false
		return await this.loadConfig()
	}
}

export default new ConfigManager()
