# FLSH 平台

Android 原生壳 + Vue 3 前端，远程加载网页，原生扫码 / 拍照 / 定位 / 本地存储桥接。

## 目录结构

```
e:\app\
├── android/          # Android 原生壳工程（Kotlin）
│   └── app/src/main/java/com/jy/flsh/
│       ├── MainActivity.kt          # 入口，WebView 容器
│       ├── WebViewManager.kt        # WebView 配置、缓存、错误处理
│       ├── config/AppConfig.kt      # 多 URL 配置（写死）
│       ├── bridge/                  # JSBridge 桥接层
│       │   ├── JSBridge.kt          # 桥接入口 + 白名单鉴权
│       │   ├── ScanBridge.kt        # 扫码
│       │   ├── CameraBridge.kt      # 拍照
│       │   ├── LocationBridge.kt    # 定位
│       │   └── StorageBridge.kt     # 本地存储
│       ├── scan/                    # 扫码模块（CameraX + ML Kit）
│       └── util/PermissionHelper.kt # 动态权限
│
├── web/              # Vue 3 前端工程
│   ├── src/
│   │   ├── bridge/                  # 桥接 SDK（Promise 化封装）
│   │   ├── router/                  # 前端路由（hash 模式）
│   │   └── views/                   # 示例页面
│   └── public/sw.js                 # Service Worker 离线缓存
│
└── docs/方案设计文档.md              # 方案设计文档
```

## 构建步骤

### 1. 前端（Web）

```bash
cd web
npm install
npm run dev        # 开发调试（浏览器中可预览 UI，桥接接口会提示不可用）
npm run build      # 构建产物输出到 dist/
```

将 `dist/` 目录部署到 HTTPS 静态服务器（如 Nginx、OSS + CDN）。

### 2. Android 壳

1. 用 **Android Studio** 打开 `android/` 目录。
2. 等待 Gradle 同步完成（首次会自动下载 Gradle 8.7 与依赖）。
3. 修改 `app/src/main/java/com/jy/flsh/config/AppConfig.kt` 中的 URL 配置为你的真实地址。
4. 连接 Android 设备（开启 USB 调试）或启动模拟器，点击 **Run** 运行。

> **本地联调地址**：`DEFAULT_URL` 必须带协议头（如 `http://localhost:5174`）。设备上的 `localhost`
> 指向设备自身，需把开发机端口反向映射到设备：
>
> ```bash
> adb reverse tcp:5174 tcp:5174
> ```
>
> 模拟器也可直接使用 `http://10.0.2.2:5174`（已作为第一个备用地址配置）。
> App 启动时会依次尝试默认地址与备用地址，全部不可达才展示错误页。

> 若命令行构建：`cd android && gradlew assembleDebug`（需先通过 Android Studio 或 `gradle wrapper` 生成 wrapper）。

## 配置说明

### 多 URL 配置（`AppConfig.kt`）

| 配置项 | 说明 |
|--------|------|
| `DEFAULT_URL` | 默认入口地址 |
| `FALLBACK_URLS` | 备用地址列表（默认地址不可达时尝试） |
| `ALLOWED_HOSTS` | URL 白名单（仅白名单域名可调用原生桥接） |
| `ALLOW_HTTP` | 是否允许 HTTP（生产建议 false） |

### 桥接接口（前端调用）

```js
import { scan, takePhoto, getLocation, setItem, getItem, getAppInfo } from './src/bridge'

// 扫码（二维码 + 条形码）
const { format, rawValue } = await scan({ prompt: '请对准二维码' })

// 拍照
const { filePath } = await takePhoto({ quality: 0.8 })

// 定位
const { latitude, longitude } = await getLocation()

// 本地存储
await setItem('token', 'abc123')
const token = await getItem('token')

// 应用信息
const info = await getAppInfo()
```

## 技术栈

- **Android**：Kotlin、WebView、CameraX、ML Kit（barcode-scanning，bundled 模式，不依赖 GMS）
- **前端**：Vue 3、Vite、Vue Router、Service Worker（Workbox）

## 安全说明

- 生产环境强制 HTTPS（`usesCleartextTraffic=false`）。
- 仅白名单域名内的页面可调用原生桥接能力。
- 拍照走系统相机 Intent，无需 CAMERA 权限；扫码自建相机界面，需 CAMERA 权限（动态申请）。