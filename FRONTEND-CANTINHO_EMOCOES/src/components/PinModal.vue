<script setup>
import { ref, onMounted } from 'vue';
import api from '@/services/api'; // <--- ALTERAÇÃO: Importa o serviço centralizado
import { useAuthStore } from '@/stores/auth'; 

const emit = defineEmits(['close', 'success']);
const authStore = useAuthStore(); 

const pin = ref('');
const erro = ref(null);
const loading = ref(false);

// --- LÓGICA DO AVISO DE STATUS ---
const verificandoConexao = ref(true);
const bancoOnline = ref(false);

onMounted(async () => {
  await checarStatusSistema();
});

async function checarStatusSistema() {
  verificandoConexao.value = true;
  try {
    // ALTERAÇÃO: Usa api.get e URL relativa
    await api.get('/api/health');
    bancoOnline.value = true;
  } catch (e) {
    // Se der erro de rede, assume offline.
    if (e.code === 'ERR_NETWORK') {
       bancoOnline.value = false;
    } else {
       // Se o servidor respondeu (mesmo com erro 4xx/5xx), ele está online
       bancoOnline.value = true; 
    }
  } finally {
    verificandoConexao.value = false;
  }
}

async function validarPin() {
  if (pin.value.length !== 4) {
    erro.value = "Digite os 4 números.";
    return;
  }

  if (!bancoOnline.value) {
    erro.value = "Sistema Offline.";
    return;
  }

  loading.value = true;
  erro.value = null;

  try {
    // ALTERAÇÃO: Usa api.post, URL relativa e remove header de Authorization (automático)
    const response = await api.post('/api/responsavel/validar-pin', 
      { pin: pin.value },
      {
        validateStatus: (status) => status < 500 
      }
    );

    if (response.status === 200 && response.data.valid === true) {
      emit('success');
    } else {
      throw new Error('PIN incorreto');
    }

  } catch (e) {
    console.error("Erro na validação do PIN:", e);
    erro.value = "PIN incorreto.";
    pin.value = ''; 
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="fixed inset-0 bg-[#A78BFA]/60 backdrop-blur-md z-[100] flex flex-col items-center justify-center p-4 font-nunito animate-fade-in">
    
    <div class="bg-white rounded-[40px] p-10 w-full max-w-md shadow-[0_20px_60px_-15px_rgba(0,0,0,0.1)] border-4 border-white text-center relative transition-all">
      
      <button @click="$emit('close')" class="absolute top-6 right-6 text-gray-300 hover:text-[#A78BFA] font-bold transition-colors text-xl">
        ✕
      </button>

      <div class="mb-8 flex justify-center">
        <div v-if="verificandoConexao" class="text-[10px] uppercase tracking-wider text-gray-400 font-bold animate-pulse bg-gray-50 px-3 py-1 rounded-full border border-gray-100">
          📡 Verificando...
        </div>
        <div v-else-if="bancoOnline" class="text-[10px] uppercase tracking-wider text-green-500 font-bold bg-green-50 px-3 py-1 rounded-full border border-green-100 flex items-center gap-2">
          <span class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span> Online
        </div>
        <div v-else class="text-[10px] uppercase tracking-wider text-red-500 font-bold bg-red-50 px-3 py-1 rounded-full border border-red-100 flex items-center gap-2 cursor-pointer hover:bg-red-100" @click="checarStatusSistema">
          🔴 Offline (Tentar)
        </div>
      </div>

      <div class="mb-8">
        <div class="w-20 h-20 bg-[#F3E8FF] rounded-full flex items-center justify-center text-4xl mx-auto mb-4 shadow-sm text-[#A78BFA]">🔐</div>
        <h2 class="text-2xl font-black text-[#A78BFA] mb-1">Acesso Restrito</h2>
        <p class="text-gray-400 font-bold text-sm">Área exclusiva para os Professores.</p>
      </div>

      <form @submit.prevent="validarPin" class="space-y-8">
        <div>
          <label class="block text-xs font-extrabold text-gray-300 uppercase tracking-[0.2em] mb-4">Digite o PIN</label>
          <div class="relative">
            <input 
              type="text" 
              v-model="pin" 
              maxlength="4"
              autofocus
              class="w-full text-center text-5xl font-black tracking-[0.5em] py-4 border-b-4 border-gray-100 focus:border-[#A78BFA] outline-none text-[#A78BFA] bg-transparent transition-all placeholder-gray-200 focus:placeholder-transparent"
              placeholder="••••"
              oninput="this.value = this.value.replace(/[^0-9]/g, '')"
            >
          </div>
        </div>

        <div v-if="erro" class="text-red-400 font-bold text-center text-sm bg-red-50 p-3 rounded-2xl border border-red-100 animate-shake">
          {{ erro }}
        </div>

        <div class="flex gap-4 pt-2">
          <button 
            type="button" 
            @click="$emit('close')"
            class="flex-1 py-4 font-bold text-gray-400 hover:text-gray-600 hover:bg-gray-50 rounded-2xl transition-colors"
          >
            Voltar
          </button>
          <button 
            type="submit" 
            :disabled="loading || pin.length !== 4"
            class="flex-1 py-4 font-extrabold text-white bg-[#A78BFA] hover:bg-[#8B5CF6] rounded-2xl shadow-lg shadow-purple-200 transition-all active:scale-95 hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <span v-if="loading" class="animate-spin">⏳</span>
            <span>{{ loading ? 'Verificando...' : 'Acessar' }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in { animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }

.animate-shake { animation: shake 0.3s ease-in-out; }
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}
</style>