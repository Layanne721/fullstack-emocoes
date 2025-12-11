<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import confetti from 'canvas-confetti';

const router = useRouter();

const itens = ref([
  { id: 1, nome: 'Bola', emoji: '⚽', feliz: true, encontrado: false, x: '10%', y: '20%' },
  { id: 2, nome: 'Livro', emoji: '📚', feliz: true, encontrado: false, x: '70%', y: '15%' },
  { id: 3, nome: 'Ursinho', emoji: '🧸', feliz: true, encontrado: false, x: '40%', y: '50%' },
  { id: 4, nome: 'Lixo', emoji: '🗑️', feliz: false, encontrado: false, x: '80%', y: '60%' },
  { id: 5, nome: 'Nuvem Cinza', emoji: '🌧️', feliz: false, encontrado: false, x: '20%', y: '70%' },
  { id: 6, nome: 'Presente', emoji: '🎁', feliz: true, encontrado: false, x: '50%', y: '80%' }
]);

const encontradosCount = ref(0);
const fimDeJogo = ref(false);

function clicarItem(item) {
  if (item.encontrado) return;

  if (item.feliz) {
    item.encontrado = true;
    encontradosCount.value++;
    soltarConfetes();
    
    if (encontradosCount.value >= 3) { 
      setTimeout(() => fimDeJogo.value = true, 1000);
    }
  } else {
    const el = document.activeElement;
    if(el) {
        el.classList.add('animate-shake');
        setTimeout(() => el.classList.remove('animate-shake'), 500);
    }
  }
}

function soltarConfetes() {
  confetti({ particleCount: 100, spread: 70, origin: { y: 0.6 } });
}

function reiniciar() {
  itens.value.forEach(i => i.encontrado = false);
  encontradosCount.value = 0;
  fimDeJogo.value = false;
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] font-nunito p-4 relative overflow-hidden">
    
    <div class="absolute top-10 left-10 text-4xl opacity-50 animate-bounce-slow pointer-events-none">✨</div>

    <header class="flex items-center justify-between gap-4 mb-6 z-20 relative max-w-4xl mx-auto">
      <button @click="router.push('/jogos')" class="bg-white w-12 h-12 flex items-center justify-center rounded-full shadow-md text-gray-500 font-bold hover:text-orange-500 transition-all border-2 border-white">←</button>
      <div class="bg-white px-6 py-2 rounded-full shadow-sm text-orange-500 font-extrabold text-sm border-2 border-orange-100">
        Encontrados: {{ encontradosCount }} / 3
      </div>
    </header>

    <div class="text-center mb-6 z-20 relative">
      <h1 class="text-3xl font-black text-orange-400 drop-shadow-sm">😀 Caça à Alegria</h1>
      <p class="text-gray-400 font-bold text-sm mt-1">Toque em 3 coisas que te deixam feliz!</p>
    </div>

    <div class="relative w-full max-w-4xl mx-auto h-[60vh] bg-orange-50 rounded-[40px] border-4 border-white shadow-lg overflow-hidden mt-4">
      <div class="absolute bottom-0 w-full h-1/3 bg-orange-100/50 rounded-b-[36px]"></div>
      
      <div 
        v-for="item in itens" 
        :key="item.id"
        @click="clicarItem(item)"
        class="absolute cursor-pointer transition-transform hover:scale-110 active:scale-95 select-none touch-manipulation"
        :style="{ top: item.y, left: item.x }"
      >
        <div :class="['text-6xl drop-shadow-md transition-all duration-500', item.encontrado ? 'scale-150 rotate-12 opacity-50 grayscale' : 'animate-bounce-slow']">
          {{ item.emoji }}
        </div>
        <div v-if="item.encontrado" class="absolute -top-4 -right-4 text-3xl animate-ping-once">✨</div>
      </div>
    </div>

    <div v-if="fimDeJogo" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
      <div class="bg-white rounded-[40px] p-8 text-center max-w-sm w-full shadow-2xl animate-pop-in border-4 border-white">
        <div class="text-7xl mb-4">🌟</div>
        <h2 class="text-2xl font-black text-orange-400 mb-2">Que dia feliz!</h2>
        <p class="text-gray-500 font-bold mb-6 text-sm leading-relaxed">
          Você encontrou muitas coisas boas!<br>
          Lembre-se: A alegria fica maior quando a gente divide com os amigos!
        </p>
        <div class="space-y-3">
            <button @click="reiniciar" class="bg-orange-400 hover:bg-orange-500 text-white font-extrabold py-3 px-8 rounded-[20px] shadow-lg transition-all w-full">Brincar de Novo</button>
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
.animate-bounce-slow { animation: bounceSlow 3s infinite ease-in-out; }
@keyframes bounceSlow { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
.animate-ping-once { animation: ping 1s cubic-bezier(0, 0, 0.2, 1) 1; }
.animate-shake { animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both; }
@keyframes shake { 10%, 90% { transform: translate3d(-1px, 0, 0); } 20%, 80% { transform: translate3d(2px, 0, 0); } 30%, 50%, 70% { transform: translate3d(-4px, 0, 0); } 40%, 60% { transform: translate3d(4px, 0, 0); } }
</style>