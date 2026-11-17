import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../views/Home.vue'

// 使用 hash 模式：兼容 WebView 静态部署，无需服务器路由配置
const routes = [
  {
    path: '/',
    name: 'home',
    component: Home
  },
  {
    path: '/scan-demo',
    name: 'scan-demo',
    component: () => import('../views/ScanDemo.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router