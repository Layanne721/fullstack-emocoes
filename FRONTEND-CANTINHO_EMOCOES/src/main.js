import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css'

// Importa o api para garantir que ele seja carregado, 
// mas a lógica principal está dentro dele mesmo.
import '@/services/api'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

app.mount('#app')