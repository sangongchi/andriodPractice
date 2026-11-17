/**
 * 桥接接口类型定义（JSDoc，供 IDE 提示）
 */

/**
 * 扫码结果
 * @typedef {Object} ScanResult
 * @property {string} format - 条码格式（QR_CODE / EAN_13 / CODE_128 ...）
 * @property {string} rawValue - 识别内容
 * @property {number} timestamp - 识别时间戳
 */

/**
 * 拍照结果
 * @typedef {Object} PhotoResult
 * @property {string} filePath - 本地文件路径
 * @property {number} width - 图片宽度
 * @property {number} height - 图片高度
 */

/**
 * 定位结果
 * @typedef {Object} LocationResult
 * @property {number} latitude - 纬度
 * @property {number} longitude - 经度
 * @property {number} accuracy - 精度（米）
 * @property {string} provider - 来源（gps / network）
 * @property {number} timestamp - 定位时间戳
 */

/**
 * 应用信息
 * @typedef {Object} AppInfo
 * @property {string} appVersion - App 版本
 * @property {string} osVersion - 系统版本
 * @property {string} deviceModel - 设备型号
 * @property {string} currentUrl - 当前加载的 URL
 */

export default {} // 类型声明文件，无运行时逻辑