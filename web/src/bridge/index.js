/**
 * 桥接 SDK：封装 window.AndroidBridge，Promise 化
 *
 * 用法：
 *   import { scan, takePhoto, getLocation, setItem, getItem } from '../bridge'
 *   const res = await scan({ prompt: '请对准二维码' })
 *
 * 在浏览器（非 App）中运行时，接口会抛出"原生桥接不可用"错误，
 * 便于前端在浏览器中独立开发调试。
 */

const callNative = (method, params = {}) =>
  new Promise((resolve, reject) => {
    if (!window.AndroidBridge || typeof window.AndroidBridge[method] !== 'function') {
      reject(new Error(`原生桥接方法 ${method} 不可用（请确认在 App 内运行）`))
      return
    }

    const callbackId = `cb_${Date.now()}_${Math.random().toString(36).slice(2)}`
    window.__nativeCallbacks = window.__nativeCallbacks || {}
    window.__nativeCallbacks[callbackId] = (res) => {
      delete window.__nativeCallbacks[callbackId]
      if (res && res.code === 0) {
        resolve(res.data !== undefined ? res.data : res)
      } else {
        reject(new Error((res && res.message) || '调用失败'))
      }
    }

    window.AndroidBridge[method](JSON.stringify(params), callbackId)
  })

// ==================== 扫码 ====================
/** 打开原生扫码界面，返回识别结果 */
export const scan = (opts = {}) => callNative('scan', opts)

// ==================== 拍照 ====================
/** 调用系统相机拍照，返回图片文件路径 */
export const takePhoto = (opts = {}) => callNative('takePhoto', opts)

// ==================== 定位 ====================
/** 获取当前定位（经纬度） */
export const getLocation = (opts = {}) => callNative('getLocation', opts)

// ==================== 本地存储 ====================
/** 写入 KV 数据 */
export const setItem = (key, value) => callNative('setItem', { key, value })
/** 读取 KV 数据 */
export const getItem = (key) => callNative('getItem', { key })
/** 删除 KV 数据 */
export const removeItem = (key) => callNative('removeItem', { key })
/** 清空全部本地数据 */
export const clearStorage = () => callNative('clearStorage')

// ==================== 应用信息 ====================
/** 获取 App 版本、当前 URL、设备信息 */
export const getAppInfo = () => callNative('getAppInfo')

// ==================== 关闭应用 ====================
/** 关闭 App */
export const closeApp = () => callNative('closeApp')

export default {
  scan,
  takePhoto,
  getLocation,
  setItem,
  getItem,
  removeItem,
  clearStorage,
  getAppInfo,
  closeApp
}