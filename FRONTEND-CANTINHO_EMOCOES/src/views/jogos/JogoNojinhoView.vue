<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const ingredientes = ref([
  { id: 1, nome: 'Gosma Verde', emoji: '🤢', selecionado: false },
  { id: 2, nome: 'Minhoca Gelada', emoji: '🪱', selecionado: false },
  { id: 3, nome: 'Sopa Azul', emoji: '🥣', selecionado: false },
  { id: 4, nome: 'Meia Fedida', emoji: '🧦', selecionado: false },
  { id: 5, nome: 'Peixe Podre', emoji: '🐟', selecionado: false },
  { id: 6, nome: 'Queijo Bolorento', emoji: '🧀', selecionado: false }
]);

const panela = ref([]);
const fimDeJogo = ref(false);

function adicionarIngrediente(item) {
  if (panela.value.length < 3 && !item.selecionado) {
    item.selecionado = true;
    panela.value.push(item);
    
    if (panela.value.length === 3) {
      setTimeout(() => fimDeJogo.value = true, 1000);
    }
  }
}

function reiniciar() {
  ingredientes.value.forEach(i => i.selecionado = false);
  panela.value = [];
  fimDeJogo.value = false;
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] font-nunito p-6 pb-20 relative overflow-hidden">
    
    <header class="flex items-center gap-4 mb-6 z-20 relative">
      <button @click="router.push('/jogos')" class="bg-white w-12 h-12 flex items-center justify-center rounded-full shadow-md text-gray-500 font-bold hover:text-green-500 transition-all border-2 border-white">←</button>
      <h1 class="text-2xl font-black text-green-600">🤢 Chefes do Nojinho</h1>
    </header>

    <div class="text-center mb-6 relative z-10">
      <p class="text-green-700 font-bold bg-green-50 px-6 py-2 rounded-full inline-block border border-green-100">Escolha 3 ingredientes para a sopa!</p>
    </div>

    <div class="grid grid-cols-3 gap-4 mb-8 max-w-md mx-auto relative z-10">
      <div 
        v-for="ing in ingredientes" 
        :key="ing.id"
        @click="adicionarIngrediente(ing)"
        :class="['bg-white p-4 rounded-[20px] shadow-sm flex flex-col items-center gap-2 transition-all border-b-4 border-green-100', ing.selecionado ? 'opacity-40 cursor-not-allowed scale-90' : 'cursor-pointer hover:-translate-y-1 hover:shadow-md hover:border-green-200']"
      >
        <div class="text-4xl">{{ ing.emoji }}</div>
        <span class="text-[10px] font-extrabold text-gray-400 uppercase tracking-wide">{{ ing.nome }}</span>
      </div>
    </div>

    <div class="relative w-56 h-48 mx-auto mt-10 z-10">
      <div class="absolute bottom-0 w-full h-36 bg-gray-700 rounded-b-[50px] rounded-t-[10px] shadow-xl border-4 border-gray-600 flex items-center justify-center overflow-hidden">
        <div class="absolute bottom-0 w-full h-24 bg-green-400 opacity-90 flex items-end justify-center gap-2 pb-6 transition-all duration-500" :style="{ height: (panela.length * 30) + '%' }">
           <div v-for="p in panela" :key="p.id" class="text-3xl animate-bounce">{{ p.emoji }}</div>
        </div>
      </div>
      <div class="absolute top-12 -left-4 w-4 h-10 bg-gray-600 rounded-l-lg"></div>
      <div class="absolute top-12 -right-4 w-4 h-10 bg-gray-600 rounded-r-lg"></div>
      <div v-if="panela.length > 0" class="absolute -top-12 left-1/2 -translate-x-1/2 text-5xl opacity-60 animate-pulse">♨️</div>
    </div>

    <div v-if="fimDeJogo" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
      <div class="bg-white rounded-[40px] p-8 text-center max-w-sm w-full shadow-2xl animate-pop-in border-4 border-white">
        <div class="text-7xl mb-4">🍵</div>
        <h2 class="text-2xl font-black text-green-600 mb-2">Sopa Pronta!</h2>
        <p class="text-gray-500 font-bold mb-6 text-sm leading-relaxed">
          Eca! Ficou bem nojento, né? 🤢<br>
          Mas sabia que o <b>NOJO</b> é importante? Ele avisa para não comermos coisas estragadas!
        </p>
        <div class="space-y-3">
            <button @click="reiniciar" class="bg-green-500 hover:bg-green-600 text-white font-extrabold py-3 px-8 rounded-[20px] shadow-lg transition-all w-full">Fazer Outra Sopa</button>
            <button @click="router.push('/jogos')" class="bg-gray-100 hover:bg-gray-200 text-gray-500 font-extrabold py-3 px-8 rounded-[20px] shadow-sm transition-all w-full">Voltar para Jogos</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
.animate-pop-in { animation: popIn 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes popIn { 0% { transform: scale(0.9) translateY(20px); opacity: 0; } 100% { transform: scale(1) translateY(0); opacity: 1; } }
</style>