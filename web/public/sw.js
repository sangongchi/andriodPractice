/**
 * FLSH 平台 Service Worker - 离线缓存
 *
 * 说明：
 * - 生产环境建议使用 Workbox 生成（npm run build 时注入构建产物清单）
 * - 本文件为基础版本：网络优先，失败回退缓存
 * - 前提：页面必须通过 HTTPS 访问（Service Worker 仅在 HTTPS 下可注册）
 *
 * 注册方式（在 main.js 中）：
 *   if ('serviceWorker' in navigator) {
 *     window.addEventListener('load', () => {
 *       navigator.serviceWorker.register('./sw.js')
 *     })
 *   }
 */

const CACHE_NAME = 'flsh-cache-v1'
const PRECACHE_URLS = ['./', './index.html']

// 安装：预缓存 App Shell
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then((cache) => cache.addAll(PRECACHE_URLS))
      .then(() => self.skipWaiting())
  )
})

// 激活：清理旧缓存
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) =>
        Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
      )
      .then(() => self.clients.claim())
  )
})

// 请求拦截：网络优先，失败回退缓存
self.addEventListener('fetch', (event) => {
  const { request } = event
  if (request.method !== 'GET') return

  // 仅缓存同源请求
  const url = new URL(request.url)
  if (url.origin !== self.location.origin) return

  event.respondWith(
    fetch(request)
      .then((response) => {
        // 仅缓存成功响应
        if (response && response.status === 200 && response.type === 'basic') {
          const copy = response.clone()
          caches.open(CACHE_NAME).then((cache) => cache.put(request, copy))
        }
        return response
      })
      .catch(() =>
        caches.match(request).then((cached) => cached || caches.match('./index.html'))
      )
  )
})