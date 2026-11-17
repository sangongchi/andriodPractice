<template>
  <div class="scan-demo">
    <header class="header">
      <h1>扫码演示</h1>
      <p>调用原生摄像头识别二维码 / 条形码</p>
    </header>

    <button class="btn primary" @click="startScan">📷 开始扫码</button>
    <button class="btn" @click="startContinuous">🔁 连续扫码模式</button>

    <div v-if="result" class="result">
      <div class="result-title">识别结果</div>
      <p><strong>格式：</strong>{{ result.format }}</p>
      <p><strong>内容：</strong>{{ result.rawValue }}</p>
    </div>

    <router-link to="/" class="back">← 返回首页</router-link>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { scan } from '../bridge'

const result = ref(null)

const startScan = async () => {
  try {
    result.value = await scan({ prompt: '请对准二维码/条形码' })
  } catch (e) {
    alert(`扫码失败: ${e.message}`)
  }
}

const startContinuous = async () => {
  try {
    result.value = await scan({ prompt: '连续扫码模式', continuous: true })
  } catch (e) {
    alert(`扫码失败: ${e.message}`)
  }
}
</script>

<style scoped>
.scan-demo {
  min-height: 100vh;
  padding: 20px;
}

.header {
  text-align: center;
  padding: 30px 0 20px;
}

.header h1 {
  font-size: 24px;
  color: #1a73e8;
}

.header p {
  margin-top: 8px;
  color: #888;
  font-size: 14px;
}

.btn {
  display: block;
  width: 100%;
  padding: 14px;
  margin: 12px 0;
  font-size: 16px;
  background: #fff;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 10px;
  cursor: pointer;
}

.btn.primary {
  background: #1a73e8;
  color: #fff;
  border: none;
}

.result {
  margin-top: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.result-title {
  font-size: 14px;
  color: #888;
  margin-bottom: 8px;
}

.result p {
  margin: 6px 0;
  word-break: break-all;
}

.back {
  display: block;
  margin-top: 24px;
  text-align: center;
  color: #1a73e8;
}
</style>