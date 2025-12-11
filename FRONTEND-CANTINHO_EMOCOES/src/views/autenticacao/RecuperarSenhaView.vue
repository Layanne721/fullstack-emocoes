<script setup>
import { ref } from 'vue';
import api from '@/services/api'; // Usa o serviço centralizado

const email = ref('');
const mensagem = ref(null);
const erro = ref(null);
const isSubmitting = ref(false);

async function solicitar() {
  // Limpa mensagens anteriores
  mensagem.value = null; 
  erro.value = null; 
  
  // Ativa o estado de carregamento
  isSubmitting.value = true;

  try {
    // Faz a chamada para o backend (pode demorar se o Render estiver dormindo)
    await api.post('/auth/recuperar-senha', { email: email.value });
    
    mensagem.value = `Link enviado! Verifique seu e-mail (e a caixa de spam).`;
  } catch (e) {
    console.error(e);
    // Se o backend retornar erro (ex: e-mail não encontrado)
    erro.value = "Erro ao processar. Verifique o e-mail ou a conexão.";
  } finally {
    // IMPORTANTE: Isso roda SEMPRE, liberando o botão
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] flex items-center justify-center px-4 font-nunito relative overflow-hidden">
    
    <div class="absolute top-10 right-20 text-5xl animate-float-delayed opacity-80 rotate-12 pointer-events-none">🔑</div>
    <div class="absolute bottom-20 left-10 text-4xl animate-bounce-slow opacity-60 pointer-events-none">✨</div>

    <div class="w-full max-w-md bg-white p-8 rounded-[50px] shadow-[0_20px_50px_-12px_rgba(167,139,250,0.25)] border-4 border-white text-center transition-all relative z-10">
      <h1 class="text-3xl font-extrabold text-[#A78BFA] mb-2" style="font-family: 'Nunito', sans-serif;">Recuperar Senha</h1>
      <p class="text-gray-400 font-bold text-sm mb-8">Digite seu e-mail para receber o link.</p>
      
      <form @submit.prevent="solicitar" class="space-y-6">
        <div class="relative">
          <input 
            type="email" 
            v-model="email" 
            required 
            placeholder="Seu e-mail cadastrado" 
            class="w-full px-6 py-4 rounded-[20px] bg-[#F9FAFB] border-2 border-transparent focus:bg-white focus:border-[#A78BFA] outline-none font-bold text-gray-600 transition-all placeholder-gray-300 shadow-inner"
          >
          <span class="absolute right-4 top-4 text-xl opacity-50">📧</span>
        </div>

        <button 
          type="submit" 
          :disabled="isSubmitting" 
          class="w-full bg-gradient-to-r from-[#C4B5FD] to-[#A78BFA] hover:to-[#8B5CF6] text-white font-extrabold py-4 rounded-[20px] shadow-lg shadow-purple-200 transform active:scale-[0.98] transition-all disabled:opacity-70 disabled:cursor-not-allowed"
        >
          <span v-if="isSubmitting" class="animate-spin inline-block mr-2">⏳</span>
          {{ isSubmitting ? 'Enviando (pode demorar)...' : 'Enviar Link ✨' }}
        </button>
      </form>
      
      <div v-if="mensagem" class="mt-6 p-4 bg-green-50 text-green-600 rounded-[20px] font-bold border border-green-100 animate-fade-in text-sm">
        {{ mensagem }}
      </div>
      
      <div v-if="erro" class="mt-6 p-4 bg-red-50 text-red-500 rounded-[20px] font-bold border border-red-100 animate-shake text-sm">
        {{ erro }}
      </div>
      
      <div class="mt-8">
        <router-link to="/login" class="text-gray-400 hover:text-[#A78BFA] text-xs font-bold transition-colors">
          ← Voltar para o Login
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
@keyframes floatDelayed { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-10px); } }
.animate-float-delayed { animation: floatDelayed 5s ease-in-out infinite; }
.animate-bounce-slow { animation: floatDelayed 3s ease-in-out infinite; }
.animate-shake { animation: shake 0.3s ease-in-out; }
@keyframes shake { 0%, 100% { transform: translateX(0); } 25% { transform: translateX(-5px); } 75% { transform: translateX(5px); } }
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>