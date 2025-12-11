<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/services/api'; // <--- IMPORTAÇÃO

const route = useRoute();
const router = useRouter();
const token = ref(null);
const novaSenha = ref('');
const erro = ref(null);
const sucesso = ref(null);

onMounted(() => {
  token.value = route.query.token;
  if (!token.value) erro.value = 'Link inválido ou expirado.';
});

async function redefinir() {
  try {
    await api.post('/auth/resetar-senha', {
      token: token.value,
      newPassword: novaSenha.value
    });
    sucesso.value = 'Senha alterada! Redirecionando...';
    setTimeout(() => router.push('/login'), 3000);
  } catch (e) {
    erro.value = 'Erro ao redefinir senha.';
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] flex items-center justify-center px-4 font-nunito relative overflow-hidden">
    
    <div class="absolute top-10 left-10 text-5xl opacity-80 animate-float-slow">🔒</div>
    <div class="absolute bottom-20 right-10 text-4xl opacity-70 animate-bounce-slow">✨</div>

    <div class="bg-white p-8 rounded-[50px] shadow-[0_20px_50px_-12px_rgba(167,139,250,0.25)] w-full max-w-md border-4 border-white text-center relative z-10 transition-all">
      
      <h1 class="text-2xl font-extrabold text-[#A78BFA] mb-6" style="font-family: 'Nunito', sans-serif;">Criar Nova Senha</h1>
      
      <form v-if="token && !sucesso" @submit.prevent="redefinir" class="space-y-6">
        <div class="relative">
          <input 
            type="password" 
            v-model="novaSenha" 
            required 
            placeholder="Nova senha segura"
            class="w-full px-6 py-4 rounded-[20px] bg-[#F9FAFB] border-2 border-transparent focus:bg-white focus:border-[#A78BFA] outline-none font-bold text-gray-600 placeholder-gray-300 transition-all shadow-inner"
          >
          <span class="absolute right-4 top-4 text-xl opacity-50">🛡️</span>
        </div>
        
        <button 
          type="submit" 
          class="w-full bg-gradient-to-r from-[#C4B5FD] to-[#A78BFA] hover:to-[#8B5CF6] text-white font-extrabold py-4 rounded-[20px] shadow-lg shadow-purple-200 transform active:scale-[0.98] transition-all text-lg"
        >
          Salvar Nova Senha
        </button>
      </form>

      <div v-if="sucesso" class="p-4 bg-green-50 text-green-600 rounded-[20px] font-bold border border-green-100 shadow-sm animate-fade-in">{{ sucesso }}</div>
      <div v-if="erro" class="p-4 bg-red-50 text-red-500 rounded-[20px] font-bold border border-red-100 shadow-sm animate-shake">{{ erro }}</div>
    </div>
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
.animate-bounce-slow { animation: floatSlow 3s ease-in-out infinite; }
.animate-shake { animation: shake 0.3s ease-in-out; }
@keyframes shake { 0%, 100% { transform: translateX(0); } 25% { transform: translateX(-5px); } 75% { transform: translateX(5px); } }
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>