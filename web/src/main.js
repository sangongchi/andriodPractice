import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { setupVConsole } from './utils/vconsole'

createApp(App).use(router).mount('#app')

setupVConsole()