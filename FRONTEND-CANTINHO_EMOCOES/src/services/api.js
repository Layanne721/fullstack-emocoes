import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

// Cria a instância do Axios com a URL base automática (Vite)
const api = axios.create({
  // Se existir VITE_API_URL no .env, usa ela. Senão, usa localhost.
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  }
});

// --- INTERCEPTOR DE REQUISIÇÃO (Envia o Token) ---
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// --- INTERCEPTOR DE RESPOSTA (O "Porteiro" / Logout automático) ---
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    // 1. Se o erro for na tela de Login, apenas retorna o erro para o componente mostrar o aviso
    if (error.config && error.config.url.includes('/auth/login')) {
      return Promise.reject(error);
    }

    // 2. Se der erro 401 (Sem autorização) ou 403 (Proibido) em outras telas, desloga
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      console.warn('Sessão expirada. Redirecionando...');
      
      const authStore = useAuthStore();
      if (authStore.clearLoginData) {
        authStore.clearLoginData();
      }
      
      router.push('/login');
    }
    return Promise.reject(error);
  }
);

export default api;