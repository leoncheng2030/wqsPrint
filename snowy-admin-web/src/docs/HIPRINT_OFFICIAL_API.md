# Hiprint 官方 API 使用文档

## 概述

本文档介绍如何在项目中使用 Hiprint 官方 `hiwebSocket` API 进行打印操作。官方 API 提供了更直接、更高效的打印方式，支持多种打印类型。

## 官方 API 特性

### 支持的打印类型

1. **PDF URL 打印** - 直接打印网络 PDF 文件
2. **HTML 打印** - 打印 HTML 内容
3. **模板打印** - 使用 Hiprint 模板进行打印

### 核心优势

- **直接通信**: 使用 `hiwebSocket.send()` 直接发送数据
- **统一回调**: 通过 `success` 和 `error` 事件处理所有打印结果
- **高效传输**: 减少中间层处理，提高打印效率
- **批量支持**: 支持批量打印操作

## API 使用方法

### 基础用法

```javascript
// 检查 hiwebSocket 是否可用
if (window.hiwebSocket && window.hiwebSocket.opened) {
  // 发送打印数据
  hiwebSocket.send(printData);
}
```

### 1. PDF URL 打印

```javascript
// 打印网络 PDF 文件
hiwebSocket.send({
  pdf_path: 'https://example.com/document.pdf',
  type: 'url_pdf'
});
```

### 2. HTML 打印

```javascript
// 打印 HTML 内容
hiwebSocket.send({
  html: '<div>要打印的HTML内容</div>'
});
```

### 3. 模板打印

```javascript
// 使用模板打印
hiwebSocket.send({
  template: {
    panels: [
      {
        index: 0,
        height: 297,
        width: 210,
        printElements: [
          {
            options: {
              left: 20,
              top: 20,
              height: 16,
              width: 100,
              fontSize: 12,
              textAlign: "left"
            },
            printElementType: {
              title: "文本",
              type: "text"
            },
            text: "姓名: {{name}}"
          }
        ]
      }
    ]
  },
  printData: { name: '张三' }
});
```

### 4. 回调处理

```javascript
// 监听打印成功事件
hiwebSocket.socket.on('success', (data) => {
  console.log('打印成功:', data);
});

// 监听打印错误事件
hiwebSocket.socket.on('error', (error) => {
  console.log('打印失败:', error);
});
```

## 项目集成

### 1. 使用 useHiprintOfficial 组合函数

```javascript
import { useHiprintOfficial } from '@/composables/useHiprintOfficial'

export default {
  setup() {
    const {
      isConnected,
      isLoading,
      error,
      printHistory,
      printPdfUrl,
      printHtml,
      printTemplate,
      printMultiple
    } = useHiprintOfficial()

    // 打印 PDF
    const handlePdfPrint = async () => {
      try {
        await printPdfUrl('https://example.com/document.pdf')
        console.log('PDF 打印成功')
      } catch (error) {
        console.error('PDF 打印失败:', error)
      }
    }

    return {
      isConnected,
      isLoading,
      error,
      printHistory,
      handlePdfPrint
    }
  }
}
```

### 2. 使用 usePrintActions 组合函数

```javascript
import { usePrintActions } from '@/composables/usePrintActions'

export default {
  setup() {
    const {
      state,
      toggleOfficialApi,
      handleOfficialTemplatePrint,
      handleOfficialHtmlPrint,
      handleOfficialPdfPrint,
      handleOfficialMultiplePrint
    } = usePrintActions()

    // 启用官方 API 模式
    const enableOfficialApi = async () => {
      await toggleOfficialApi(true)
    }

    // 模板打印
    const templatePrint = async () => {
      const template = { /* 模板配置 */ }
      const data = { name: '张三' }
      await handleOfficialTemplatePrint(template, data)
    }

    return {
      state,
      enableOfficialApi,
      templatePrint
    }
  }
}
```

## 配置选项

### 打印配置

```javascript
const config = {
  // 超时时间（毫秒）
  timeout: 30000,
  
  // 批量打印并发数
  concurrency: 3,
  
  // 批量打印间隔（毫秒）
  batchDelay: 1000,
  
  // 是否启用打印历史记录
  enableHistory: true,
  
  // 历史记录最大条数
  maxHistorySize: 100
}
```

### 错误处理

```javascript
try {
  await printTemplate(template, data)
} catch (error) {
  switch (error.code) {
    case 'CONNECTION_ERROR':
      console.error('连接错误:', error.message)
      break
    case 'TIMEOUT_ERROR':
      console.error('打印超时:', error.message)
      break
    case 'PRINT_ERROR':
      console.error('打印失败:', error.message)
      break
    default:
      console.error('未知错误:', error.message)
  }
}
```

## 最佳实践

### 1. 连接状态检查

```javascript
// 在打印前检查连接状态
const checkConnection = () => {
  if (!window.hiwebSocket) {
    throw new Error('hiwebSocket 未初始化')
  }
  
  if (!window.hiwebSocket.opened) {
    throw new Error('打印服务未连接')
  }
}
```

### 2. 批量打印优化

```javascript
// 使用批量打印功能
const batchPrint = async (items) => {
  const printItems = items.map(item => ({
    template: getTemplate(),
    printData: item
  }))
  
  await printMultiple(printItems, {
    concurrency: 3,
    delay: 1000
  })
}
```

### 3. 错误重试机制

```javascript
const printWithRetry = async (printData, maxRetries = 3) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      await hiwebSocket.send(printData)
      return
    } catch (error) {
      if (i === maxRetries - 1) throw error
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)))
    }
  }
}
```

### 4. 性能监控

```javascript
const printWithMonitoring = async (printData) => {
  const startTime = Date.now()
  
  try {
    await hiwebSocket.send(printData)
    const duration = Date.now() - startTime
    console.log(`打印完成，耗时: ${duration}ms`)
  } catch (error) {
    const duration = Date.now() - startTime
    console.error(`打印失败，耗时: ${duration}ms`, error)
    throw error
  }
}
```

## 故障排除

### 常见问题

1. **hiwebSocket 未定义**
   - 确保 Hiprint 客户端已正确安装并启动
   - 检查页面是否正确加载了 hiwebSocket

2. **连接状态为 false**
   - 检查 Hiprint 服务是否正在运行
   - 确认端口配置是否正确

3. **打印无响应**
   - 检查打印数据格式是否正确
   - 确认回调事件监听器是否正确设置

4. **批量打印失败**
   - 减少并发数量
   - 增加打印间隔时间
   - 检查系统资源使用情况

### 调试技巧

```javascript
// 启用详细日志
const enableDebugMode = () => {
  window.hiprintDebug = true
  
  // 监听所有 socket 事件
  if (hiwebSocket && hiwebSocket.socket) {
    hiwebSocket.socket.onAny((event, ...args) => {
      console.log(`Socket事件: ${event}`, args)
    })
  }
}
```

## 版本兼容性

- **Hiprint 客户端**: >= 1.0.0
- **Vue**: >= 3.0.0
- **Node.js**: >= 14.0.0

## 参考链接

- [Hiprint 官方文档](https://www.ibujian.cn/svp/api2/a01.html)
- [项目 GitHub 仓库](#)
- [问题反馈](#)

## 更新日志

### v1.0.0 (2024-01-15)
- 初始版本发布
- 支持 PDF、HTML、模板打印
- 实现批量打印功能
- 添加错误处理和重试机制