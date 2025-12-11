<script setup>
import { ref } from 'vue';

const emit = defineEmits(['close', 'select']);

// Lista de Avatares Divertidos (Estilo Adventurer e Micah)
const avatares = [
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Felix&backgroundColor=b6e3f4',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Bella&backgroundColor=c0aede',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Max&backgroundColor=d1d4f9',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Lila&backgroundColor=ffdfbf',
  'https://api.dicebear.com/7.x/micah/svg?seed=Oliver&backgroundColor=f0f0f0',
  'https://api.dicebear.com/7.x/micah/svg?seed=Sophie&backgroundColor=e6e6fa',
  'https://api.dicebear.com/7.x/notionists/svg?seed=Cookie&backgroundColor=ffdfbf',
  'https://api.dicebear.com/7.x/notionists/svg?seed=Bear&backgroundColor=b6e3f4',
];

function selecionar(url) {
  emit('select', url);
}
</script>

<template>
  <div class="fixed inset-0 bg-[#A78BFA]/40 backdrop-blur-md z-[60] flex items-center justify-center p-4 font-nunito animate-fade-in" @click.self="$emit('close')">
    <div class="bg-white rounded-[40px] p-8 w-full max-w-md shadow-2xl border-4 border-white flex flex-col max-h-[80vh] relative">
      
      <!-- Botão Fechar -->
      <button @click="$emit('close')" class="absolute top-5 right-5 text-gray-300 hover:text-[#A78BFA] transition-colors font-bold text-xl">✕</button>

      <div class="text-center mb-6">
        <h2 class="text-2xl font-black text-[#A78BFA] mb-1">Escolha seu Avatar</h2>
        <p class="text-gray-400 font-bold text-sm">Como você quer ser hoje? 🎨</p>
      </div>

      <div class="grid grid-cols-3 gap-4 overflow-y-auto p-2 scrollbar-hide">
        <button 
          v-for="avatar in avatares" 
          :key="avatar" 
          @click="selecionar(avatar)"
          class="aspect-square rounded-full border-4 border-transparent hover:border-[#A78BFA] hover:scale-110 transition-all shadow-sm bg-[#F9FAFB] overflow-hidden group"
        >
          <img :src="avatar" class="w-full h-full object-cover group-hover:scale-105 transition-transform">
        </button>
      </div>

      <button 
        @click="$emit('close')"
        class="mt-8 w-full py-4 bg-gray-50 text-gray-400 font-extrabold rounded-2xl hover:bg-gray-100 transition-colors border-2 border-transparent hover:border-gray-200"
      >
        Cancelar
      </button>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }

/* Scrollbar customizada/escondida para limpeza visual */
.scrollbar-hide::-webkit-scrollbar {
    width: 6px;
}
.scrollbar-hide::-webkit-scrollbar-track {
    background: transparent;
}
.scrollbar-hide::-webkit-scrollbar-thumb {
    background-color: #E5E7EB;
    border-radius: 20px;
}
</style>