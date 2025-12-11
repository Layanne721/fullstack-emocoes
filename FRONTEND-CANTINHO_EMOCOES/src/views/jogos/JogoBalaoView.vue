<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import confetti from 'canvas-confetti'; 

const router = useRouter();

const tamanhoBalao = ref(50);
const estourou = ref(false);
const voou = ref(false);
const segurando = ref(false);
const pontos = ref(0);
let intervaloCrescimento = null;

const cores = ['bg-red-400', 'bg-blue-400', 'bg-green-400', 'bg-yellow-400', 'bg-purple-400'];
const corAtual = ref(cores[0]);

const estiloBalao = computed(() => ({
  width: `${tamanhoBalao.value}px`,
  height: `${tamanhoBalao.value * 1.2}px`,
  transition: 'width 0.1s linear, height 0.1s linear, transform 1s ease-out, opacity 0.5s',
  transform: voou.value ? 'translateY(-600px)' : 'translateY(0)',
  opacity: voou.value ? 0 : 1
}));

function iniciarCrescimento() {
  if (voou.value || estourou.value) return;
  segurando.value = true;
  intervaloCrescimento = setInterval(() => {
    if (tamanhoBalao.value >= 300) estourarBalao();
    else tamanhoBalao.value += 4;
  }, 50);
}

function pararCrescimento() {
  if (!segurando.value) return;
  segurando.value = false;
  clearInterval(intervaloCrescimento);
  if (!estourou.value) soltarBalao();
}

function soltarBalao() {
  if (tamanhoBalao.value < 80) { tamanhoBalao.value = 50; return; }
  voou.value = true;
  pontos.value += 10;
  if (window.navigator.vibrate) window.navigator.vibrate(200);
  confetti({ particleCount: 50, spread: 60, origin: { y: 0.7 } });
  setTimeout(resetarJogo, 1500);
}

function estourarBalao() {
  clearInterval(intervaloCrescimento);
  segurando.value = false;
  estourou.value = true;
  if (window.navigator.vibrate) window.navigator.vibrate([100, 50, 100]);
  setTimeout(resetarJogo, 1500);
}

function resetarJogo() {
  tamanhoBalao.value = 50;
  estourou.value = false;
  voou.value = false;
  corAtual.value = cores[Math.floor(Math.random() * cores.length)];
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] flex flex-col items-center justify-center p-6 font-nunito relative overflow-hidden touch-none select-none">
    
    <div class="absolute top-20 right-10 text-4xl opacity-40 pointer-events-none">☁️</div>
    <div class="absolute top-40 left-10 text-3xl opacity-40 pointer-events-none">☁️</div>

    <button @click="router.push('/jogos')" class="absolute top-6 left-6 z-20 bg-white w-12 h-12 flex items-center justify-center rounded-full shadow-md text-gray-500 font-bold hover:text-[#A78BFA] transition-all border-2 border-white">←</button>

    <div class="text-center z-10 w-full flex flex-col items-center h-full justify-between py-10">
      
      <div>
        <h2 class="text-3xl font-extrabold text-[#A78BFA] mb-2">Encher o Balão 🎈</h2>
        <p class="text-gray-400 font-bold text-sm">Segure para encher, solte para voar!</p>
        <div class="mt-4 bg-white px-6 py-2 rounded-full shadow-sm text-purple-500 font-extrabold inline-block border-2 border-purple-50">
          ⭐ Pontos: {{ pontos }}
        </div>
      </div>

      <div class="flex-1 flex items-center justify-center w-full relative min-h-[400px]">
        <div v-if="!estourou" :class="['rounded-[50%] shadow-lg relative flex items-center justify-center transition-transform', corAtual]" :style="estiloBalao">
          <div class="absolute top-[15%] left-[15%] w-[20%] h-[10%] bg-white rounded-[50%] opacity-40 -rotate-45"></div>
          <div class="absolute -bottom-8 left-1/2 w-1 h-12 bg-gray-300 -translate-x-1/2"></div>
        </div>
        <div v-else class="text-6xl font-bold text-gray-300 animate-ping">💥 POP!</div>
        <div class="absolute top-10 w-full border-t-2 border-dashed border-red-200 opacity-50 flex justify-center">
          <span class="bg-red-50 text-red-300 text-xs px-2 rounded -mt-3 font-bold">Limite de estouro</span>
        </div>
      </div>

      <button 
        @mousedown="iniciarCrescimento" 
        @mouseup="pararCrescimento" 
        @mouseleave="pararCrescimento"
        @touchstart.prevent="iniciarCrescimento" 
        @touchend.prevent="pararCrescimento"
        class="bg-[#A78BFA] hover:bg-[#8B5CF6] text-white text-xl font-extrabold px-12 py-6 rounded-[30px] shadow-[0_10px_0_rgb(139,92,246)] active:shadow-none active:translate-y-[10px] transition-all w-full max-w-xs flex items-center justify-center gap-2 select-none"
      >
        <span>🌬️</span> ASSOPRAR
      </button>

    </div>
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
.select-none { user-select: none; -webkit-user-select: none; }
.touch-none { touch-action: none; }
</style>