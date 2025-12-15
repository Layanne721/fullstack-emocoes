<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api'; 
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const email = ref('');
const senha = ref('');
const erro = ref(null);
const isSubmitting = ref(false);
const verificandoConexao = ref(true);
const bancoOnline = ref(false);

onMounted(async () => {
  await checarStatusSistema();
});

async function checarStatusSistema() {
  verificandoConexao.value = true;
  erro.value = null;
  try {
    await api.get('/api/health');
    bancoOnline.value = true;
  } catch (e) {
    // Se der erro de rede, avisa. Se der 403/500, o server ta online.
    if (e.code === 'ERR_NETWORK') {
        bancoOnline.value = false;
        erro.value = "⚠️ Sem conexão com o servidor.";
    } else {
        bancoOnline.value = true;
    }
  } finally {
    verificandoConexao.value = false;
  }
}

async function fazerLogin() {
  if (!bancoOnline.value) return; 

  erro.value = null;
  isSubmitting.value = true;
  
  try {
    const response = await api.post('/auth/login', {
      email: email.value,
      senha: senha.value
    });
    
    const userData = response.data;

    // --- CORREÇÃO CRÍTICA DO LOGIN ---
    // Verifica 'role' ou 'perfil' para garantir que pegamos o dado certo
    const userRole = userData.role || userData.perfil;

    authStore.setLoginData(
      { 
        name: userData.nome, 
        email: userData.email, 
        perfil: userRole, 
        avatarUrl: userData.avatarUrl 
      },
      userData.token
    );
    
    // Redirecionamento
    if (userRole === 'ADMINISTRADOR' || userRole === 'ADMIN') {
        router.push('/admin');
    } else {
        router.push('/selecionar-perfil');
    }

  } catch (e) {
    console.error(e);
    if (e.response && (e.response.status === 401 || e.response.status === 403)) {
      erro.value = 'E-mail ou senha incorretos.';
    } else if (e.code === 'ERR_NETWORK') {
      bancoOnline.value = false;
      erro.value = "Perda de conexão com o servidor.";
    } else {
      erro.value = 'Erro ao processar login.';
    }
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] flex items-center justify-center px-4 font-nunito relative overflow-hidden">
    
    <div class="absolute top-10 left-10 text-4xl animate-float-slow opacity-80 rotate-12 pointer-events-none">✨</div>
    <div class="absolute top-20 right-10 text-5xl animate-float-delayed opacity-90 -rotate-12 pointer-events-none">🎒</div>
    <div class="absolute bottom-20 right-20 text-4xl animate-bounce-slow opacity-80 pointer-events-none">📝</div>
    <div class="absolute bottom-10 left-20 text-4xl animate-float-slow opacity-80 rotate-45 pointer-events-none">🎨</div>

    <div class="w-full max-w-md bg-white p-8 md:p-12 rounded-[50px] shadow-[0_20px_50px_-12px_rgba(167,139,250,0.25)] border-4 border-white relative z-10">
      
      <div class="text-center mb-8">
        <p class="text-pink-400 font-bold text-sm tracking-widest uppercase mb-1 flex items-center justify-center gap-2">
          <span>🩷</span> Bem-vindo ao <span>🩷</span>
        </p>
        <h1 class="text-3xl md:text-4xl font-extrabold text-[#C084FC] drop-shadow-sm" style="font-family: 'Nunito', sans-serif;">
          Cantinho do <br /> Saber
        </h1>
        <p class="text-gray-400 text-xs mt-2 font-bold">Portal do Professor e Aluno 🎓</p>

        <div class="mt-4 flex justify-center">
           <div v-if="verificandoConexao" class="text-xs text-gray-400 font-bold bg-gray-100 px-3 py-1 rounded-full animate-pulse">
             📡 Conectando...
           </div>
           <div v-else-if="bancoOnline" class="text-xs text-green-600 font-bold bg-green-50 border border-green-100 px-3 py-1 rounded-full flex items-center gap-1">
             <span class="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span> Online
           </div>
           <div v-else class="text-xs text-red-500 font-bold bg-red-50 border border-red-100 px-3 py-1 rounded-full cursor-pointer hover:bg-red-100 transition-colors" @click="checarStatusSistema">
             🔴 Offline (Toque para tentar)
           </div>
        </div>
      </div>

      <form @submit.prevent="fazerLogin" class="space-y-6">
        
        <div class="group">
          <label class="label-padrao">E-mail do Professor</label>
          <div class="relative">
            <input 
              type="email" 
              v-model="email" 
              required
              :disabled="!bancoOnline" 
              class="input-padrao peer"
              placeholder="ex: professor@escola.com"
            >
            <span class="absolute right-4 top-3.5 text-xl grayscale opacity-50 peer-focus:grayscale-0 peer-focus:opacity-100 transition-all">📧</span>
          </div>
        </div>

        <div class="group">
          <label class="label-padrao">Senha</label>
          <div class="relative">
            <input 
              type="password" 
              v-model="senha" 
              required
              :disabled="!bancoOnline"
              class="input-padrao peer"
              placeholder="••••••••"
            >
            <span class="absolute right-4 top-3.5 text-xl grayscale opacity-50 peer-focus:grayscale-0 peer-focus:opacity-100 transition-all">🔒</span>
          </div>
        </div>

        <div v-if="erro" class="bg-red-50 border-2 border-red-100 rounded-2xl p-3 flex items-center gap-3 animate-shake">
          <span class="text-2xl">🚫</span>
          <p class="text-red-500 text-sm font-bold leading-tight">{{ erro }}</p>
        </div>

        <button 
          type="submit" 
          :disabled="isSubmitting || !bancoOnline || verificandoConexao"
          :class="[
            'w-full py-4 rounded-[20px] font-black text-lg shadow-lg transform transition-all active:scale-95 flex justify-center items-center gap-2',
            (!bancoOnline || verificandoConexao) 
              ? 'bg-gray-200 text-gray-400 cursor-not-allowed shadow-none' 
              : 'bg-gradient-to-r from-[#C4B5FD] to-[#A78BFA] hover:to-[#8B5CF6] text-white shadow-purple-200 hover:shadow-purple-300'
          ]"
        >
          <span v-if="isSubmitting" class="animate-spin">⏳</span>
          <span v-else>Entrar</span>
        </button>
      </form>

      <div class="mt-8 flex flex-col items-center space-y-3 text-center">
        <router-link to="/registro" class="text-gray-500 font-bold text-sm hover:text-[#A78BFA] transition-colors p-2 rounded-xl hover:bg-purple-50">
          Novo Professor? <span class="text-[#A78BFA] underline decoration-2 decoration-purple-200">Cadastre sua turma</span>
        </router-link>
        
        <router-link to="/recuperar-senha" class="text-gray-400 text-xs font-bold hover:text-gray-600 transition-colors">
          Esqueci minha senha
        </router-link>
      </div>

    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;700;900&display=swap');

.font-nunito { font-family: 'Nunito', sans-serif; }
.label-padrao { @apply block text-gray-500 font-extrabold mb-2 ml-2 text-xs uppercase tracking-wider; }
.input-padrao { @apply w-full px-6 py-4 rounded-[20px] bg-[#F9FAFB] border-2 border-transparent focus:bg-white focus:border-[#C084FC] outline-none transition-all text-gray-600 font-bold placeholder-gray-300 shadow-inner; }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
@keyframes floatDelayed { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-10px); } }
.animate-float-delayed { animation: floatDelayed 5s ease-in-out infinite; animation-delay: 1s; }
.animate-bounce-slow { animation: floatSlow 3s ease-in-out infinite; }
@keyframes shake { 0%, 100% { transform: translateX(0); } 25% { transform: translateX(-5px); } 75% { transform: translateX(5px); } }
.animate-shake { animation: shake 0.3s ease-in-out; }
</style>