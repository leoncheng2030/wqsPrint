import pako from 'pako'

/**
 * 转换控制字符：将文本形式的 \\x1E(RS)、\\x1D(GS)、\\x04(EOT)、\\x1F(US) 转换为实际字节
 */
export const parseCtrl = (str) => {
	if (!str) return str
	return str
		.replace(/\\x1E/g, String.fromCharCode(30))
		.replace(/\\x1D/g, String.fromCharCode(29))
		.replace(/\\x04/g, String.fromCharCode(4))
		.replace(/\\x1F/g, String.fromCharCode(31))
}


/**
 * 根据二维码配置和动态字段数据生成二维码内容
 * @param {object} qrConfig - 二维码配置对象
 * @param {object} dynamicFieldData - 动态字段数据（主字段数据 + 可选的 items 明细数据）
 * @param {function} [formatValueFn] - 字段值格式化函数，默认 String
 * @returns {string} 生成的二维码内容
 */
export const generateQrCodeContent = (qrConfig, dynamicFieldData, formatValueFn) => {
	if (!qrConfig || !qrConfig.formatType) {
		return ''
	}

	const fmtVal = formatValueFn || ((v) => String(v ?? ''))

	// 转换控制字符
	qrConfig.separator = parseCtrl(qrConfig.separator)
	qrConfig.startMarker = parseCtrl(qrConfig.startMarker)
	qrConfig.endMarker = parseCtrl(qrConfig.endMarker)
	qrConfig.detailRowSeparator = parseCtrl(qrConfig.detailRowSeparator)
	qrConfig.itemValueSeparator = parseCtrl(qrConfig.itemValueSeparator)

	// formatId 前导分隔符，默认 RS(\x1E)。修复问题3/4：不再硬编码 RS
	const formatIdSeparator = parseCtrl(qrConfig.formatIdSeparator || '\\x1E')

	try {
		let content = ''

		// 将格式类型转换为大写以保证兼容
		const formatType = qrConfig.formatType.toUpperCase()

		// 保持用户选择的字段顺序
		const selectedFields = qrConfig.selectedFields || []

		// 根据格式类型生成内容
		switch (formatType) {
			case 'CUSTOM':
				if (selectedFields.length > 0) {
					const separator = qrConfig.separator || qrConfig.customSeparator || '|'
					const detailRowSeparator = qrConfig.detailRowSeparator || '#'
					const fieldPrefixes = qrConfig.fieldPrefixes || {}
					const hasFieldPrefixes = selectedFields.some(fk => fieldPrefixes[fk])

					// 判断是否有明细字段
					const hasDetailFields = selectedFields.some(fieldKey =>
						dynamicFieldData.items && dynamicFieldData.items.length > 0 &&
						dynamicFieldData.items[0].hasOwnProperty(fieldKey)
					)

					// 构建 DI 前缀片段：第一个字段不加前导分隔符（修复问题2）
					const buildDiParts = (fieldKeys, values) => {
						return fieldKeys.map((fieldKey, i) => {
							const prefix = fieldPrefixes[fieldKey] || ''
							// 第一个字段不加前导 separator
							return (i === 0 ? '' : separator) + prefix + (values[i] !== undefined ? values[i] : '')
						}).join('')
					}

					const buildSimpleParts = (fieldKeys, values) => {
						return values.join(separator)
					}

					// 构建单行数据
					const buildItemRow = (item) => {
						const rowValues = []
						selectedFields.forEach((fieldKey) => {
							if (item.hasOwnProperty(fieldKey)) {
								rowValues.push(fmtVal(item[fieldKey], fieldKey))
							} else if (dynamicFieldData[fieldKey] !== undefined) {
								rowValues.push(fmtVal(dynamicFieldData[fieldKey], fieldKey))
							} else {
								rowValues.push('')
							}
						})
						if (hasFieldPrefixes) {
							return buildDiParts(selectedFields, rowValues)
						} else {
							return buildSimpleParts(selectedFields, rowValues)
						}
					}

					// 构建每行的格式标识前缀（如 RS21）
					const buildRowFormatIdPrefix = () => {
						if (qrConfig.formatId && qrConfig.formatId.trim() !== '') {
							return formatIdSeparator + qrConfig.formatId
						}
						return ''
					}

					const RS = String.fromCharCode(30)
					const detailLoopMode = qrConfig.detailLoopMode || 'join'
					const formatRowSep = qrConfig.formatId && qrConfig.formatId.trim() !== ''
						? formatIdSeparator + qrConfig.formatId
						: null
					const rowSeparator = formatRowSep || detailRowSeparator

					if (detailLoopMode === 'first') {
						// 首项模式：只取第一个明细项
						if (dynamicFieldData.items && dynamicFieldData.items.length > 0) {
							const item = dynamicFieldData.items[0]
							content = buildItemRow(item)
						} else if (dynamicFieldData[selectedFields[0]] !== undefined) {
							// 无明细时回退到主字段处理
							const values = selectedFields.map(fk => fmtVal(dynamicFieldData[fk], fk))
							content = hasFieldPrefixes ? buildDiParts(selectedFields, values) : buildSimpleParts(selectedFields, values)
						}
					} else if (detailLoopMode === 'loop') {
						// 循环模式：每条明细以 RS21 为前缀（格式标识），
						// 符合 GS1-128 标准：每行都有独立的格式标识
						// 注意：第一个 item 的 RS21 由全局 startMarker+formatId 提供，所以跳过
						if (dynamicFieldData.items && dynamicFieldData.items.length > 0) {
							const rowFormatIdPrefix = buildRowFormatIdPrefix()
							const itemBlocks = dynamicFieldData.items.map((item, idx) =>
								(idx === 0 ? '' : rowFormatIdPrefix) + buildItemRow(item)
							)
							content = itemBlocks.join('')
							if (formatRowSep && content) {
								content += RS
							}
						} else if (dynamicFieldData[selectedFields[0]] !== undefined) {
							const values = selectedFields.map(fk => fmtVal(dynamicFieldData[fk], fk))
							content = hasFieldPrefixes ? buildDiParts(selectedFields, values) : buildSimpleParts(selectedFields, values)
						}
					} else {
						// join 模式（默认）：所有明细项用行分隔符连接
						if (dynamicFieldData.items && dynamicFieldData.items.length > 0) {
							const detailParts = dynamicFieldData.items.map(item => buildItemRow(item))
							content = detailParts.join(rowSeparator)
							if (formatRowSep && content) {
								content += RS
							}
						} else if (dynamicFieldData[selectedFields[0]] !== undefined) {
							const values = selectedFields.map(fk => fmtVal(dynamicFieldData[fk], fk))
							content = hasFieldPrefixes ? buildDiParts(selectedFields, values) : buildSimpleParts(selectedFields, values)
						}
					}
				} else {
					// 如果没有明细字段，只处理主字段
					const values = []
					selectedFields.forEach((fieldKey) => {
						if (dynamicFieldData[fieldKey] !== undefined) {
							values.push(fmtVal(dynamicFieldData[fieldKey], fieldKey))
						}
					})
					if (hasFieldPrefixes) {
						content = buildDiParts(selectedFields, values)
					} else {
						content = buildSimpleParts(selectedFields, values)
					}
				}
				break

			case 'JSON':
				if (selectedFields.length > 0) {
					const jsonData = {}
					selectedFields.forEach((fieldKey) => {
						if (dynamicFieldData[fieldKey] !== undefined) {
							jsonData[fieldKey] = fmtVal(dynamicFieldData[fieldKey], fieldKey)
						} else if (dynamicFieldData.items && dynamicFieldData.items.length > 0) {
							const itemValues = dynamicFieldData.items.map(item =>
								item[fieldKey] !== undefined ? fmtVal(item[fieldKey], fieldKey) : ''
							)
							const itemValueSeparator = qrConfig.itemValueSeparator || ','
							jsonData[fieldKey] = itemValues.filter(val => val !== '').join(itemValueSeparator)
						} else {
							jsonData[fieldKey] = ''
						}
					})

					if (qrConfig.jsonDataType === 'array') {
						content = JSON.stringify([jsonData])
					} else {
						content = JSON.stringify(jsonData)
					}
				}
				break

			case 'URL':
				if (selectedFields.length > 0) {
					const baseUrl = qrConfig.baseUrl || ''
					const params = new URLSearchParams()
					selectedFields.forEach((fieldKey) => {
						let value = ''
						if (dynamicFieldData[fieldKey] !== undefined) {
							value = fmtVal(dynamicFieldData[fieldKey], fieldKey)
						} else if (dynamicFieldData.items && dynamicFieldData.items.length > 0) {
							const itemValues = dynamicFieldData.items.map(item =>
								item[fieldKey] !== undefined ? fmtVal(item[fieldKey], fieldKey) : ''
							)
							const itemValueSeparator = qrConfig.itemValueSeparator || ','
							value = itemValues.filter(val => val !== '').join(itemValueSeparator)
						}
						if (value) {
							params.append(fieldKey, value)
						}
					})
					content = baseUrl + (params.toString() ? '?' + params.toString() : '')
				}
				break

			default:
				break
		}

		// 在内容最前添加开始标识符
		if (qrConfig.startMarker && qrConfig.startMarker.trim() !== '') {
			let prefix = qrConfig.startMarker
			// 修复问题3：formatId 前使用可配置的 formatIdSeparator
			if (qrConfig.formatId && qrConfig.formatId.trim() !== '') {
				prefix += formatIdSeparator + qrConfig.formatId
			}
			if (content && content.trim() !== '') {
				content = prefix + content
			} else {
				content = prefix
			}
		}
		
		// 在内容最后添加结束标识符
		if (qrConfig.endMarker && qrConfig.endMarker.trim() !== '') {
			if (content && content.trim() !== '') {
				content = content + qrConfig.endMarker
			} else {
				content = qrConfig.endMarker
			}
		}

		// 编码处理
		if (content && qrConfig.encodeMode && qrConfig.encodeMode !== 'none') {
			let processed = content

			if (qrConfig.encodeMode === 'gzip_base64') {
				const softEncode = (s) => encodeURI(s)
				try {
					const compressed = pako.gzip(softEncode(processed))
					let binaryStr = ''
					const bytes = new Uint8Array(compressed)
					for (let i = 0; i < bytes.length; i++) {
						binaryStr += String.fromCharCode(bytes[i])
					}
					processed = binaryStr
				} catch (e) {
					console.warn('gzip压缩失败:', e)
				}
				try {
					processed = btoa(processed)
				} catch (e) {
					console.warn('base64编码失败:', e)
				}
			} else if (qrConfig.encodeMode === 'base64') {
				try {
					processed = btoa(unescape(encodeURIComponent(processed)))
				} catch (e) {
					console.warn('base64编码失败:', e)
				}
			}

			content = processed
		}

		return content
	} catch (error) {
		return ''
	}
}
