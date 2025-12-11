<script setup>
defineProps({
  titulo: String,
  conteudo: String,
  tipo: { type: String, default: 'info' } // 'info' ou 'perigo'
});
defineEmits(['close', 'confirm']);
</script>

<template>
  <div class="fixed inset-0 bg-[#A78BFA]/40 backdrop-blur-md z-50 flex items-center justify-center p-4 font-nunito animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-[40px] p-8 w-full max-w-lg shadow-[0_20px_60px_-15px_rgba(167,139,250,0.3)] border-4 border-white transform transition-all scale-100 flex flex-col max-h-[90vh] relative">
      
      <div class="text-center mb-6">
        <div class="w-16 h-16 rounded-full flex items-center justify-center text-3xl mb-3 mx-auto shadow-sm"
             :class="tipo === 'perigo' ? 'bg-red-50 text-red-400' : 'bg-[#F3E8FF] text-[#A78BFA]'">
          {{ tipo === 'perigo' ? '⚠️' : '📜' }}
        </div>
        <h2 class="text-2xl font-black" :class="tipo === 'perigo' ? 'text-red-400' : 'text-[#A78BFA]'">{{ titulo }}</h2>
      </div>

      <div class="overflow-y-auto flex-1 mb-8 pr-2 text-sm text-gray-500 font-medium space-y-3 bg-[#FAFAFA] p-6 rounded-[25px] border border-gray-100 text-justify leading-relaxed shadow-inner">
        <p v-html="conteudo"></p>
      </div>

      <div class="flex gap-4">
        <button 
          @click="$emit('close')"
          class="flex-1 py-4 font-bold text-gray-400 hover:text-gray-600 hover:bg-gray-50 rounded-2xl transition-colors"
        >
          Fechar
        </button>
        <button 
          v-if="tipo === 'perigo'"
          @click="$emit('confirm')"
          class="flex-1 py-4 font-extrabold text-white bg-red-400 hover:bg-red-500 rounded-2xl shadow-lg shadow-red-100 transition-all active:scale-95 hover:-translate-y-0.5"
        >
          Confirmar Exclusão
        </button>
        <button 
          v-else
          @click="$emit('confirm')"
          class="flex-1 py-4 font-extrabold text-white bg-[#A78BFA] hover:bg-[#8B5CF6] rounded-2xl shadow-lg shadow-purple-200 transition-all active:scale-95 hover:-translate-y-0.5"
        >
          Li e Concordo
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in { animation: fadeIn 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>