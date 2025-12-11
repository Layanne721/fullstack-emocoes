<script setup>
import { RouterView } from 'vue-router'
import { useAccessibilityStore } from '@/stores/accessibility';
import { onMounted, ref } from 'vue';

const a11y = useAccessibilityStore();
const toolbarOpen = ref(false);

// Aplica o tamanho da fonte ao carregar a página
onMounted(() => {
  document.documentElement.style.fontSize = `${a11y.fontSize}%`;

  // --- INICIALIZAÇÃO DO VLIBRAS ---
  // Verifica se o script já existe para evitar duplicação
  if (!document.getElementById('vlibras-script')) {
    const script = document.createElement('script');
    script.id = 'vlibras-script';
    script.src = 'https://vlibras.gov.br/app/vlibras-plugin.js';
    script.async = true;
    script.onload = () => {
      // Inicializa o widget após o script carregar
      new window.VLibras.Widget('https://vlibras.gov.br/app');
    };
    document.body.appendChild(script);
  }
});
</script>

<template>
  <!-- --- WIDGET DO VLIBRAS --- -->
  <div vw class="enabled">
    <div vw-access-button class="active"></div>
    <div vw-plugin-wrapper>
      <div class="vw-plugin-top-wrapper"></div>
    </div>
  </div>

  <div class="fixed top-40 right-2 z-[9999] flex flex-col items-end gap-2 font-nunito">
    
    <button 
      @click="toolbarOpen = !toolbarOpen"
      class="w-10 h-10 bg-blue-600 text-white shadow-xl rounded-full flex items-center justify-center hover:bg-blue-500 hover:scale-105 transition-all focus:outline-none focus:ring-4 focus:ring-blue-300 active:scale-95 border-2 border-blue-400/30"
      title="Acessibilidade"
      aria-label="Menu de Acessibilidade"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
        <path fill-rule="evenodd" d="M12 2.25c-5.385 0-9.75 4.365-9.75 9.75s4.365 9.75 9.75 9.75 9.75-4.365 9.75-9.75S17.385 2.25 12 2.25Zm0 1.5a8.25 8.25 0 1 0 0 16.5 8.25 8.25 0 0 0 0-16.5Z" clip-rule="evenodd" />
        <circle cx="12" cy="8.5" r="1.5" />
        <path d="M14.5 11.5h-5c-.5 0-1 .5-1 1v2.5c0 .3.2.5.5.5s.5-.2.5-.5v-2h1v4.5c0 .3.2.5.5.5s.5-.2.5-.5V13h1v4.5c0 .3.2.5.5.5s.5-.2.5-.5V12.5c0-.5-.5-1-1-1Z" stroke="currentColor" stroke-width="0.2"/>
        <path d="M9.5 11.5a.5.5 0 0 0-.5.5v1a.5.5 0 0 0 1 0v-1a.5.5 0 0 0-.5-.5Zm5 0a.5.5 0 0 0-.5.5v1a.5.5 0 0 0 1 0v-1a.5.5 0 0 0-.5-.5Z" />
      </svg>
    </button>

    <div v-if="toolbarOpen" class="bg-white border-2 border-blue-100 shadow-2xl rounded-2xl p-4 flex flex-col gap-4 animate-fade-in origin-top-right w-64 mr-2">
      
      <div class="text-xs font-extrabold text-blue-400 uppercase tracking-wider text-center">Tamanho do Texto</div>
      
      <div class="flex items-center justify-between gap-2 bg-blue-50 rounded-xl p-1 border border-blue-100">
        <button 
          @click="a11y.decreaseFont" 
          class="w-10 h-10 flex items-center justify-center font-bold text-blue-600 hover:bg-white rounded-lg transition-colors text-lg shadow-sm"
          title="Diminuir fonte"
        >
          A-
        </button>
        
        <span class="text-sm font-mono text-blue-900 font-bold w-12 text-center select-none">
          {{ a11y.fontSize }}%
        </span>
        
        <button 
          @click="a11y.increaseFont" 
          class="w-10 h-10 flex items-center justify-center font-bold text-blue-600 hover:bg-white rounded-lg transition-colors text-lg shadow-sm"
          title="Aumentar fonte"
        >
          A+
        </button>
      </div>
      
    </div>
  </div>

  <RouterView />
</template>

<style scoped>
.animate-fade-in { 
  animation: fadeIn 0.2s cubic-bezier(0.16, 1, 0.3, 1); 
}

@keyframes fadeIn { 
  from { opacity: 0; transform: scale(0.9) translateY(-10px); } 
  to { opacity: 1; transform: scale(1) translateY(0); } 
}
</style>