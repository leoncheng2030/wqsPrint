<div align="center">
    <p align="center">
        <img src="./snowy-admin-web/public/img/logo.png" height="150" alt="logo"/>
    </p>
</div>

## 框架介绍

Snowy（SnowyAdmin）是国内首个国密前后端分离快速开发平台，集成国密加解密插件，
软件层面完全符合等保测评要求，同时实现国产化机型、中间件、数据库适配，是您的不二之选！

**本版本特色：集成企业级打印解决方案**，基于sv-print框架，提供完整的打印功能支持。

技术框架与密码结合，让更多的人认识密码，使用密码；更是让前后分离“密”不可分。

采用SpringBoot+MybatisPlus+AntDesignVue+Vite 等更多组件及前沿技术开发，注释丰富，代码简洁，开箱即用！

Snowy谐音“小诺”，恰应小诺团队名称；意思为”下雪的、纯洁的“，寓意框架追求简洁至上，大道至简。

<p align="center">     
    <p align="center">
        <a href="https://gitcode.com/xiaonuobase/Snowy">
            <img src="https://gitcode.com/xiaonuobase/Snowy/star/badge.svg" alt="bootstrap">
        </a>
        <a href="https://gitee.com/xiaonuobase/snowy">
            <img src="https://gitee.com/xiaonuobase/snowy/badge/star.svg?theme=dark" alt="Gitee star">
        </a>
        <a href="https://gitee.com/xiaonuobase/snowy">
            <img src="https://gitee.com/xiaonuobase/snowy/badge/fork.svg?theme=dark" alt="Gitee fork">
        </a>
        <a href="https://www.antdv.com/docs/vue/introduce-cn/">
            <img src="https://img.shields.io/badge/vue-3-blue.svg" alt="bootstrap">
        </a> 
        <a href="http://spring.io/projects/spring-boot">
            <img src="https://img.shields.io/badge/vite-5-green.svg" alt="spring-boot">
        </a>
        <a href="https://www.antdv.com/docs/vue/introduce-cn/">
            <img src="https://img.shields.io/badge/vue--ant--design-4-blue.svg" alt="bootstrap">
        </a> 
        <a href="http://spring.io/projects/spring-boot">
            <img src="https://img.shields.io/badge/spring--boot-3-green.svg" alt="spring-boot">
        </a>
        <a href="http://mp.baomidou.com">
            <img src="https://img.shields.io/badge/mybatis--plus-3-blue.svg" alt="mybatis-plus">
        </a>  
        <a href="./LICENSE">
            <img src="https://img.shields.io/badge/license-Apache%202-red" alt="license Apache 2.0">
        </a>
        <a href="https://old.murphysec.com/dr/mQ1xAybeOLMLOxH8pU" alt="OSCS Status">
            <img src="https://www.oscs1024.com/platform/badge//xiaonuobase/snowy.git.svg?size=small"/>
        </a>
    </p>
</p>

## 快速链接

gitee下载地址：[https://gitee.com/xiaonuobase/snowy](https://gitee.com/xiaonuobase/snowy)

github下载地址（镜像）：[https://github.com/xiaonuobase/Snowy](https://github.com/xiaonuobase/Snowy)

gitcode下载地址：[https://gitcode.com/xiaonuobase/Snowy](https://gitcode.com/xiaonuobase/Snowy)

演示地址：[https://snowy.xiaonuo.vip](https://snowy.xiaonuo.vip)

文档地址：[https://xiaonuo.vip/doc](https://xiaonuo.vip/doc)

## 项目特色

### 🖨️ 集成打印功能

本项目在Snowy框架基础上集成了企业级打印解决方案：

- **sv-print打印框架**: 基于hiprint的企业级打印解决方案
- **WebSocket打印服务**: 支持本地打印客户端连接
- **多插件支持**: 集成表格、图表、PDF等多种打印插件
- **灵活配置**: 支持动态配置打印服务器地址
- **可视化设计**: 提供打印模板可视化设计器
- **安全打印**: 结合国密加密，确保打印数据传输安全

### 打印服务配置

项目支持通过系统配置动态设置打印服务器地址：

```javascript
// 系统配置中的打印服务器配置
SNOWY_SYS_PRINT_HOST: 'http://127.0.0.1:17522'
```

打印功能会自动连接到配置的WebSocket服务，支持：
- 本地打印客户端连接
- 实时打印状态监控
- 自动重连机制
- 错误日志追踪

## 快速启动

全栈工程师推荐idea

### 前端支撑

| 插件      | 版本  | 用途             |
|---------|-----|----------------|
| node.js | ≥18 | JavaScript运行环境 |

### 启动前端

```bash
npm install
```

```bash
npm run dev
```

### 启动打印服务（可选）

如需使用打印功能，请确保：

1. **安装打印客户端**: 下载并安装electron-print客户端
2. **启动打印服务**: 客户端默认监听17521端口
3. **配置服务地址**: 在系统配置中设置`SNOWY_SYS_PRINT_HOST`
4. **测试连接**: 系统会自动连接到配置的打印服务器

### 打印功能使用

```javascript
// 在Vue组件中使用打印功能
import { useHiprintCore } from '@/composables/useHiprintCore'

const { 
  initHiprint,        // 初始化打印
  designTemplate,     // 设计模板
  printTemplate,      // 打印模板
  previewTemplate,    // 预览模板
  checkPrintStatus    // 检查打印状态
} = useHiprintCore()

// 初始化打印功能
await initHiprint()

// 检查打印服务状态
const isConnected = await checkPrintStatus()
```

### 后端支撑

| 插件     | 版本        | 用途     |
|--------|-----------|--------|
| jdk    | 17        | java环境 |
| lombok | idea内     | 代码简化插件 |
| maven  | 最新版       | 包管理工具  |
| redis  | 最新版       | 缓存库    |
| mysql  | 8.0 / 5.7 | 数据库    |

### 启动后端

开发工具内配置好maven并在代码中配置数据库即可启动

## 代码结构

Snowy3.0框架对代码以插件化的模式进行分包，使得包层级结构更加清晰合理，同时降低了耦合度，关于插件模块化开发的规范请查阅文档【SNOWY开源文档——前端手册or后端手册——开发规范】板块。

```
snowy
  |-snowy-admin-web == 前端
    |-public == 基础静态文件
    |-src == 前端源代码
      |-api == API接口转发
      |-assets == 静态文件
      |-components == VUE组件
      |-composables == Vue3组合式函数（包含打印功能）
      |-config == 基础配置
      |-layout == 基础布局
      |-locales == 多语言配置
      |-router == 基础路由配置
      |-store == Pinia缓存配置
      |-style == 样式风格配置
      |-utils == 工具类
      |-views == 所有视图界面
  |-snowy-common == 基础通用模块
  |-snowy-plugin == 插件包
    |-snowy-plugin-auth == 登录鉴权插件
    |-snowy-plugin-biz == 业务功能插件
    |-snowy-plugin-client == C端功能插件
    |-snowy-plugin-dev == 开发工具插件
    |-snowy-plugin-gen == 代码生成插件
    |-snowy-plugin-mobile == 移动端管理插件
    |-snowy-plugin-sys == 系统功能插件
  |-snowy-plugin-api == 插件api包
    |-snowy-plugin-auth-api == 登录鉴权插件api接口
    |-snowy-plugin-biz-api == 业务功能插件api接口
    |-snowy-plugin-client-api == C端功能插件api接口
    |-snowy-plugin-dev-api == 开发工具插件api接口
    |-snowy-plugin-gen == 代码生成插件api接口
    |-snowy-plugin-mobile == 移动端管理插件api接口
    |-snowy-plugin-sys-api == 系统功能插件api接口
  |-snowy-web-app == 主启动模块
```

## 分支说明

- master

正式稳定版本，具体版本升级内容看更新标签

- dev

团队开发的分支（代码可能随时会推，不保证运行和使用）

- snowy1.8

1.x分支，目前已停止新增功能，只限于bug的维护，推荐使用2x版本

- snowy2.5

2.x分支，目前已停止新增功能，只限于bug的维护，可以平滑过渡至3x版本

## 视频教程

教程地址（免费开放）：[https://space.bilibili.com/50101698/channel/collectiondetail?sid=739071](https://space.bilibili.com/50101698/channel/collectiondetail?sid=739071)

<img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9mhLatA%7D%2F&_etag=1730700699-816553&shareID=-9mhLatA"/>

作者也在上班工作，所以在利用休息时间为大家创作，录制视频的目的也是为各位小伙伴提供文档跟技术交流群聊之外的上手学习资料

> 视频由小诺开源技术团队王同学（每天一点）进行录制

## 技术框架

### 核心技术栈

| 类型 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 3.x | Java企业级应用框架 |
| 数据层 | MyBatis Plus | 3.x | ORM数据库框架 |
| 前端框架 | Vue.js | 3.x | 渐进式JavaScript框架 |
| 构建工具 | Vite | 5.x | 下一代前端构建工具 |
| UI组件库 | Ant Design Vue | 4.x | 企业级UI设计语言 |
| 状态管理 | Pinia | 最新版 | Vue3状态管理库 |

### 打印技术栈

| 类型 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 核心框架 | sv-print | 最新版 | 企业级打印解决方案 |
| 基础引擎 | hiprint | 最新版 | Web打印基础引擎 |
| 表格插件 | @sv-print/plugin-ele-e2table | 最新版 | 表格打印插件 |
| 图表插件 | @sv-print/plugin-ele-echarts | 最新版 | ECharts图表打印 |
| 绘图插件 | @sv-print/plugin-ele-fabric | 最新版 | Fabric.js绘图支持 |
| PDF插件 | @sv-print/plugin-api-pdf3 | 最新版 | PDF生成与打印 |
| 连接协议 | WebSocket | - | 实时打印服务连接 |
| 客户端 | electron-print | 最新版 | 文档打印客户端 |

## 架构原理

* 业务架构

<p align="center">
    <img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lQAEoQ%7D%2F&_etag=1730690514-197232&shareID=-9lQAEoQ"/>
</p>

* 应用架构

<p align="center">
    <img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lPMbxA%7D%2F&_etag=1730690514-308767&shareID=-9lPMbxA"/>
</p>

* 数据架构

<p align="center">
    <img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lQja5Q%7D%2F&_etag=1730690514-322422&shareID=-9lQja5Q"/>
</p>

* 技术架构

<p align="center">
    <img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lQxASg%7D%2F&_etag=1730690514-152387&shareID=-9lQxASg"/>
</p>

* 部署架构

<p align="center">
    <img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lRBZAw%7D%2F&_etag=1730690514-222880&shareID=-9lRBZAw"/>
</p>

## 效果展示

<table>
    <tr>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lSg_Dw%7D%2F&_etag=1730384489-1272624&shareID=-9lSg_Dw"/></td>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lTCIQQ%7D%2F&_etag=1730384527-354571&shareID=-9lTCIQQ"/></td>
    </tr>
    <tr>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lTUROA%7D%2F&_etag=1730384568-327201&shareID=-9lTUROA"/></td>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lTdB3A%7D%2F&_etag=1730384595-160612&shareID=-9lTdB3A"/></td>
    </tr>
    <tr>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lTp1iw%7D%2F&_etag=1730384617-182491&shareID=-9lTp1iw"/></td>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lTyI5g%7D%2F&_etag=1730384636-144484&shareID=-9lTyI5g"/></td>
    </tr>
    <tr>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lUAj9A%7D%2F&_etag=1730384732-148422&shareID=-9lUAj9A"/></td>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lUWvfg%7D%2F&_etag=1730384836-112114&shareID=-9lUWvfg"/></td>
    </tr>
    <tr>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9lUuk7g%7D%2F&_etag=1730384925-320963&shareID=-9lUuk7g"/></td>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9mJCOTw%7D%2F&_etag=1730385032-172917&shareID=-9mJCOTw"/></td>
    </tr>
    <tr>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9mJSKTQ%7D%2F&_etag=1730385077-142012&shareID=-9mJSKTQ"/></td>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9mJfjcg%7D%2F&_etag=1730385168-168192&shareID=-9mJfjcg"/></td>
    </tr>
</table>

## 密码分步

| 功能     | 算法类型                 |
|--------|----------------------|
| 登录     | SM2前端加密，后端解密         |
| 登录登出日志 | SM2对登录登出日志做签名完整性保护存储 |
| 操作日志   | SM2对操作日志做签名完整性保护存储   |
| 用户密码   | SM3完整性保护存储，登录时做完整性校验 |
| 用户手机号  | SM4（cbc模式）加解密使用字段脱敏  |

## 官方群聊

QQ技术群：732230670（已满）、685395081

微信技术群：

因群达到200人以上，需加微信拉群，禁止群内艾特群主及管理员，私信提问技术问题无时间精力回答（免开尊口），请群内互动互助才是建群的意义，否则我认为你没有加群的必要

<table>
    <tr>
        <td>微信群</td>
        <td><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9mUnPEw%7D%2F&_etag=1730699307-193261&shareID=-9mUnPEw" width="120"/></td>
    </tr>
</table>

## 代码贡献

近期有很多热心开源的小伙伴陆续为咱们Snowy框架提交PR或者提出好的建议，基本合格的PR我们都接受，这样您的头像就列入到咱们Snowy仓库的贡献者列表啦！

如何贡献

1、fork一份代码至自己的账号下，本地修改您要提的代码，提交至您fork的仓库

2、登录gitee后到Snowy仓库下创建Pull Requests,选择您的仓库到Snowy的dev分支，提交即可

因为dev分支是团队开发分支，并不是统一发版本的测试过的，所以我们建议提代码至dev即可

## 外包开发

如果您（或您公司）有外包开发需求，可以通过联系客服方式，提交您的需求，经过工作量分析，出具合适的报价，合作开发并交付。

本团队具备雄厚的技术人才力量，均跟随小诺团队发展至今，已完全熟练每一处细节代码，您的产品自然也是由Snowy平台进行开发并交付。

1、工期保障
2、源码质量保障
3、节点按时汇报

## 团队成员

| 成员  | 技术 |  昵称   | 
|:---:|:--:|:-----:| 
| 俞宝山 | 全栈 |  俞宝山  | 
| 徐玉祥 | 全栈 | 就是那个锅 | 
| 董夏雨 | 全栈 |  阿董   | 
| 王鹏  | 全栈 | 每天一点  | 
| 陈心雨 | 前端 |   .   | 

## 曾获荣誉

🔥 **2021年度OSC中国开源项目评选-最受欢迎项目**

🔥 **2022年度OSC中国开源项目评选-最火热中国开源项目社区**

🔥 **2024 GitCode项目毕业认证-G-Star优秀毕业项目**

🔥 **2024 GitCode开源共创大会-十大新锐项目**

🔥 **2024中国互联网发展创新与投资大赛（开源）-优秀奖**

<table>
    <tr>
        <td style="width: 50%"><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9mLM-zg%7D%2F&_etag=1730698398-294375&shareID=-9mLM-zg"/></td>
        <td style="width: 50%"><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9mLtqmQ%7D%2F&_etag=1730698444-77496&shareID=-9mLtqmQ"/></td>
    </tr>
    <tr>
        <td style="width: 50%"><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A-9nmI4yQ%7D%2F&_etag=1730707931-764633&shareID=-9nmI4yQ"/></td>
        <td style="width: 50%"><img src="https://pan.xiaonuo.vip/?explorer/share/fileOut&path=%7BshareItemLink%3A_COsphyQ%7D%2F&_etag=1732688010-447888&shareID=_COsphyQ"/></td>
    </tr>
</table>

## 版权说明

- Snowy生态技术框架全系版本采用 Apache License2.0协议

- 代码可用于个人项目等接私活或企业项目脚手架使用，Snowy全系开源版完全免费

- 二次开发如用于开源竞品请先联系群主沟通，禁止任何变相的二开行为，未经审核视为侵权

- 请不要删除和修改Snowy源码头部的版权与作者声明及出处
