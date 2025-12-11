<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const fase = ref('INICIO');
const contadorCiclos = ref(0);
const instrucao = ref('Toque para começar');
const emojiCentral = ref('🌸');
const escalaCirculo = ref(1); 

let timeoutId = null;

const fasesConfig = {
  INSPIRAR: { tempo: 4000, texto: 'Cheira a florzinha... 🌸', emoji: '🌸', escala: 1.8 },
  SEGURAR:  { tempo: 2000, texto: 'Segura o ar... ✨', emoji: '😶', escala: 1.8 },
  EXPIRAR:  { tempo: 4000, texto: 'Assopra a velinha... 🕯️', emoji: '🕯️', escala: 1 },
};

function iniciarJogo() {
  if (fase.value !== 'INICIO' && fase.value !== 'FIM') return;
  contadorCiclos.value = 0;
  cicloRespiracao();
}

function cicloRespiracao() {
  setFase('INSPIRAR');
  timeoutId = setTimeout(() => {
    setFase('SEGURAR');
    timeoutId = setTimeout(() => {
      setFase('EXPIRAR');
      timeoutId = setTimeout(() => {
        contadorCiclos.value++;
        if (contadorCiclos.value < 3) {
          cicloRespiracao(); 
        } else {
          fase.value = 'FIM';
          instrucao.value = 'Muito bem! Você está tranquilo! 🌟';
          emojiCentral.value = '😌';
          escalaCirculo.value = 1;
        }
      }, fasesConfig.EXPIRAR.tempo);
    }, fasesConfig.SEGURAR.tempo);
  }, fasesConfig.INSPIRAR.tempo);
}

function setFase(novaFase) {
  fase.value = novaFase;
  const config = fasesConfig[novaFase];
  instrucao.value = config.texto;
  emojiCentral.value = config.emoji;
  escalaCirculo.value = config.escala;
}

onUnmounted(() => {
  clearTimeout(timeoutId);
});
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] flex flex-col items-center justify-center p-6 font-nunito relative overflow-hidden">
    
    <div class="absolute top-10 left-10 text-3xl opacity-40 animate-float-slow pointer-events-none">💨</div>
    <div class="absolute bottom-20 right-10 text-4xl opacity-40 animate-float-delayed pointer-events-none">🍃</div>

    <button 
      @click="router.push('/jogos')" 
      class="absolute top-6 left-6 z-20 bg-white w-12 h-12 flex items-center justify-center rounded-full shadow-md text-gray-500 font-bold hover:text-[#A78BFA] transition-all border-2 border-white"
    >
      ←
    </button>

    <div class="flex flex-col items-center z-10 w-full max-w-md text-center">
      
      <h2 class="text-2xl font-extrabold text-[#A78BFA] mb-10 transition-all duration-500 h-8">
        {{ instrucao }}
      </h2>

      <div 
        @click="iniciarJogo"
        class="relative w-64 h-64 flex items-center justify-center cursor-pointer mb-12"
      >
        <div 
          class="absolute inset-0 bg-[#E9D5FF] rounded-full shadow-xl transition-all duration-[4000ms] ease-in-out opacity-40 border-4 border-white"
          :style="{ transform: `scale(${escalaCirculo})` }"
        ></div>
        
        <div class="relative w-32 h-32 bg-white rounded-full shadow-lg flex items-center justify-center text-6xl z-10 animate-bounce-slow border-4 border-white">
          {{ emojiCentral }}
        </div>

        <div v-if="fase === 'INICIO' || fase === 'FIM'" class="absolute -bottom-20 bg-[#A78BFA] text-white px-8 py-3 rounded-full font-bold shadow-lg hover:bg-[#8B5CF6] transition-transform active:scale-95 animate-pulse">
          {{ fase === 'FIM' ? 'Respirar de novo ↺' : 'Começar ▶' }}
        </div>
      </div>

      <div class="flex gap-2">
        <div v-for="i in 3" :key="i" :class="['w-4 h-4 rounded-full transition-colors duration-500', i <= contadorCiclos ? 'bg-[#A78BFA]' : 'bg-gray-200']"></div>
      </div>
      
      <p class="mt-4 text-gray-400 font-bold text-sm uppercase tracking-widest">
        {{ contadorCiclos }}/3 Ciclos
      </p>

    </div>
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
.animate-bounce-slow { animation: bounce 3s infinite; }
@keyframes bounce { 0%, 100% { transform: translateY(-5%); } 50% { transform: translateY(5%); } }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
.animate-float-delayed { animation: floatSlow 5s ease-in-out infinite; animation-delay: 1s; }
</style>