import { createApp } from 'vue'
import App from './App.vue'
import "@/assets/less/index.less"
// import ElementPlus from 'element-plus'
// import 'element-plus/dist/index.css'
import router from "./router"
import {createPinia} from 'pinia'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
const pinia = createPinia()



const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }
app.use(router)
app.use(pinia)
//app.use(ElementPlus)
app.mount('#app')
