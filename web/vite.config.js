import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  // 相对路径，兼容部署到任意子路径
  base: './',
  build: {
    outDir: 'dist',
    sourcemap: false,
    // 产物体积提示
    chunkSizeWarningLimit: 1500
  },
  server: {
    host: true,
    // 与 Android 壳 AppConfig.DEFAULT_URL 保持一致，端口被占用时直接报错而不是漂移
    port: 5174,
    strictPort: true
  }
})