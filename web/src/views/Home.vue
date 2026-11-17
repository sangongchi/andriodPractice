<template>
  <div class="home">
    <header class="header">
      <h1>FLSH 平台</h1>
      <p>WebView 壳 + 原生桥接演示</p>
    </header>
    <button @click="goDetailPage()">跳转app详情页</button>

    <div class="grid">
      <div class="card" @click="handleScan">
        <span class="icon">📷</span>
        <span class="label">扫码</span>
      </div>
      <div class="card" @click="handlePhoto">
        <span class="icon">📸</span>
        <span class="label">拍照</span>
      </div>
      <div class="card" @click="handleLocation">
        <span class="icon">📍</span>
        <span class="label">定位</span>
      </div>
      <div class="card" @click="handleStorage">
        <span class="icon">💾</span>
        <span class="label">本地存储</span>
      </div>
      <div class="card" @click="handleInfo">
        <span class="icon">ℹ️</span>
        <span class="label">应用信息</span>
      </div>
      <router-link to="/scan-demo" class="card link">
        <span class="icon">🔍</span>
        <span class="label">扫码演示页</span>
      </router-link>
    </div>

    <div class="result">
      <div class="result-title">调用结果</div>
      <pre>{{ result }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  scan,
  takePhoto,
  getLocation,
  setItem,
  getItem,
  getAppInfo
} from '../bridge'

const result = ref('点击上方按钮体验原生能力')

const handleScan = async () => {
  try {
    const res = await scan({ prompt: '请对准二维码' })
    result.value = JSON.stringify(res, null, 2)
  } catch (e) {
    result.value = `扫码失败: ${e.message}`
  }
}

const handlePhoto = async () => {
  try {
    const res = await takePhoto({ quality: 0.8, maxWidth: 1920 })
    result.value = `拍照成功: ${res.filePath}`
  } catch (e) {
    result.value = `拍照失败: ${e.message}`
  }
}
window.__nativeCallbacks = window.__nativeCallbacks || {};
function goDetailPage(id="testId",title="我是sangongchi"){
  const callbackId = 'openDetail_' + Date.now();
  console.log('callbackId', callbackId)
  window.__nativeCallbacks[callbackId] = (res) => {
    const data = JSON.parse(res);
    console.log('跳转data',data)
    console.log(data.code === 0 ? '已发起跳转' : data.message);
  };
  window.AndroidBridge.openDetail(
    JSON.stringify({ id: id, title: title }),
    callbackId
  );
}

const handleLocation = async () => {
  try {
    const res = await getLocation({ highAccuracy: true })
    result.value = JSON.stringify(res, null, 2)
  } catch (e) {
    result.value = `定位失败: ${e.message}`
  }
}

const handleStorage = async () => {
  try {
    await setItem('demo_key', 'hello from web')
    const val = await getItem('demo_key')
    result.value = `存储成功，读取到: ${val}`
  } catch (e) {
    result.value = `存储失败: ${e.message}`
  }
}

const handleInfo = async () => {
  try {
    const res = await getAppInfo()
    result.value = JSON.stringify(res, null, 2)
  } catch (e) {
    result.value = `获取失败: ${e.message}`
  }
}
</script>

<style scoped>
.home {
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

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 20px;
}

.card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 8px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.15s;
}

.card:active {
  transform: scale(0.95);
}

.card .icon {
  font-size: 28px;
}

.card .label {
  margin-top: 8px;
  font-size: 13px;
  color: #555;
}

.card.link {
  color: inherit;
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

.result pre {
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  color: #333;
}
</style>