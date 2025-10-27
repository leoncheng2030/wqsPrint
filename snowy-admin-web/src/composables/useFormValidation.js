/**
 * 通用表单验证组合式函数
 * 用于减少重复的验证规则定义和提供常用的验证逻辑
 */
import { ref, reactive } from 'vue'
import { required, rules } from '@/utils/formRules'

/**
 * 通用表单验证钩子
 * @param {Object} initialRules - 初始验证规则
 * @returns {Object} 验证相关的方法和状态
 */
export function useFormValidation(initialRules = {}) {
	const formRules = ref(initialRules)
	const validationErrors = ref({})

	/**
	 * 设置字段验证规则
	 * @param {string} field - 字段名
	 * @param {Array} rules - 验证规则数组
	 */
	const setFieldRules = (field, rules) => {
		formRules.value[field] = rules
	}

	/**
	 * 移除字段验证规则
	 * @param {string} field - 字段名
	 */
	const removeFieldRules = (field) => {
		delete formRules.value[field]
	}

	/**
	 * 批量设置验证规则
	 * @param {Object} rules - 规则对象
	 */
	const setRules = (rules) => {
		Object.assign(formRules.value, rules)
	}

	/**
	 * 清空所有验证规则
	 */
	const clearRules = () => {
		formRules.value = {}
	}

	/**
	 * 重置验证规则为初始状态
	 */
	const resetRules = () => {
		formRules.value = { ...initialRules }
	}

	return {
		formRules,
		validationErrors,
		setFieldRules,
		removeFieldRules,
		setRules,
		clearRules,
		resetRules
	}
}

/**
 * 常用验证规则组合
 */
export const commonValidationRules = {
	// 手机号验证
	phone: () => [required('请输入手机号'), rules.phone],
	
	// 邮箱验证
	email: () => [required('请输入邮箱'), rules.email],
	
	// 验证码验证
	validCode: (message = '请输入验证码') => [required(message), rules.number],
	
	// 密码验证
	password: (message = '请输入密码') => [required(message)],
	
	// 新密码验证
	newPassword: (message = '请输入新密码') => [required(message)],
	
	// 手机验证码验证
	phoneValidCode: (message = '请输入短信验证码') => [required(message), rules.number],
	
	// 邮箱验证码验证
	emailValidCode: (message = '请输入邮箱验证码') => [required(message), rules.number],
	
	// 图片验证码验证
	picValidCode: (message = '请输入验证码') => [required(message), rules.lettersNum]
}

/**
 * 登录表单验证钩子
 * @param {string} type - 登录类型 ('phone' | 'email')
 * @returns {Object} 登录表单验证相关方法
 */
export function useLoginValidation(type = 'phone') {
	const { formRules, setFieldRules, removeFieldRules } = useFormValidation()

	/**
	 * 设置获取验证码时的验证规则
	 */
	const setGetCodeRules = () => {
		if (type === 'phone') {
			setFieldRules('phone', commonValidationRules.phone())
		} else if (type === 'email') {
			setFieldRules('email', commonValidationRules.email())
		}
		removeFieldRules(`${type}ValidCode`)
	}

	/**
	 * 设置登录时的验证规则
	 */
	const setLoginRules = () => {
		if (type === 'phone') {
			setFieldRules('phone', commonValidationRules.phone())
			setFieldRules('phoneValidCode', commonValidationRules.phoneValidCode())
		} else if (type === 'email') {
			setFieldRules('email', commonValidationRules.email())
			setFieldRules('emailValidCode', commonValidationRules.emailValidCode())
		}
	}

	return {
		formRules,
		setGetCodeRules,
		setLoginRules,
		setFieldRules,
		removeFieldRules
	}
}

/**
 * 找回密码表单验证钩子
 * @param {string} type - 找回类型 ('phone' | 'email')
 * @returns {Object} 找回密码表单验证相关方法
 */
export function useFindPasswordValidation(type = 'phone') {
	const { formRules, setFieldRules, removeFieldRules } = useFormValidation()

	/**
	 * 设置获取验证码时的验证规则
	 */
	const setGetCodeRules = () => {
		if (type === 'phone') {
			setFieldRules('phone', commonValidationRules.phone())
		} else if (type === 'email') {
			setFieldRules('email', commonValidationRules.email())
		}
		removeFieldRules(`${type}ValidCode`)
		removeFieldRules('newPassword')
	}

	/**
	 * 设置重置密码时的验证规则
	 */
	const setResetRules = () => {
		if (type === 'phone') {
			setFieldRules('phone', commonValidationRules.phone())
			setFieldRules('phoneValidCode', commonValidationRules.phoneValidCode())
		} else if (type === 'email') {
			setFieldRules('email', commonValidationRules.email())
			setFieldRules('emailValidCode', commonValidationRules.emailValidCode())
		}
		setFieldRules('newPassword', commonValidationRules.newPassword())
	}

	return {
		formRules,
		setGetCodeRules,
		setResetRules,
		setFieldRules,
		removeFieldRules
	}
}

/**
 * 注册表单验证钩子
 * @returns {Object} 注册表单验证相关方法
 */
export function useRegisterValidation() {
	const { formRules, setFieldRules } = useFormValidation()

	/**
	 * 设置图片验证码验证规则
	 */
	const setPicCodeRules = () => {
		setFieldRules('validCode', commonValidationRules.picValidCode())
	}

	return {
		formRules,
		setPicCodeRules,
		setFieldRules
	}
}