<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const emocoes = [
  { id: 1, nome: 'Alegria', emoji: '😄', cor: 'bg-yellow-100 text-yellow-600' },
  { id: 2, nome: 'Tristeza', emoji: '😢', cor: 'bg-blue-100 text-blue-600' },
  { id: 3, nome: 'Raiva', emoji: '😠', cor: 'bg-red-100 text-red-600' },
  { id: 4, nome: 'Medo', emoji: '😨', cor: 'bg-purple-100 text-purple-600' },
  { id: 5, nome: 'Nojo', emoji: '🤢', cor: 'bg-green-100 text-green-600' },
  { id: 6, nome: 'Vergonha', emoji: '😳', cor: 'bg-orange-100 text-orange-600' }
];

const cartas = ref([]);
const cartasViradas = ref([]);
const paresEncontrados = ref([]);
const bloqueado = ref(false);
const fimDeJogo = ref(false);

onMounted(iniciarJogo);

function iniciarJogo() {
  const deck = [...emocoes, ...emocoes]
    .sort(() => Math.random() - 0.5)
    .map((item, index) => ({ ...item, uniqueId: index, virada: false }));
  
  cartas.value = deck;
  cartasViradas.value = [];
  paresEncontrados.value = [];
  bloqueado.value = false;
  fimDeJogo.value = false;
}

function virarCarta(carta) {
  if (bloqueado.value || carta.virada || paresEncontrados.value.includes(carta.id)) return;

  carta.virada = true;
  cartasViradas.value.push(carta);

  if (cartasViradas.value.length === 2) {
    bloqueado.value = true;
    checarPar();
  }
}

function checarPar() {
  const [carta1, carta2] = cartasViradas.value;

  if (carta1.id === carta2.id) {
    paresEncontrados.value.push(carta1.id);
    cartasViradas.value = [];
    bloqueado.value = false;
    
    if (paresEncontrados.value.length === emocoes.length) {
      setTimeout(() => fimDeJogo.value = true, 500);
    }
  } else {
    setTimeout(() => {
      carta1.virada = false;
      carta2.virada = false;
      cartasViradas.value = [];
      bloqueado.value = false;
    }, 1000);
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] font-nunito p-6 pb-20 relative overflow-hidden">
    
    <div class="absolute bottom-10 right-10 text-5xl opacity-40 animate-bounce-slow pointer-events-none">🧩</div>

    <header class="flex items-center gap-4 mb-8 z-20 relative">
      <button @click="router.push('/jogos')" class="bg-white w-12 h-12 flex items-center justify-center rounded-full shadow-md text-gray-500 font-bold hover:text-[#A78BFA] transition-all border-2 border-white">←</button>
      <h1 class="text-2xl font-black text-[#A78BFA]">Memória das Emoções</h1>
    </header>

    <div class="grid grid-cols-3 sm:grid-cols-4 gap-4 max-w-2xl mx-auto z-10 relative">
      <div 
        v-for="carta in cartas" 
        :key="carta.uniqueId"
        @click="virarCarta(carta)"
        :class="['aspect-square rounded-[20px] shadow-sm cursor-pointer transition-all duration-300 transform perspective-1000', carta.virada || paresEncontrados.includes(carta.id) ? 'rotate-y-180' : 'hover:scale-105']"
      >
        <div class="w-full h-full relative transition-transform duration-500 transform-style-3d">
          
          <div v-if="carta.virada || paresEncontrados.includes(carta.id)" :class="['absolute inset-0 flex items-center justify-center text-5xl rounded-[20px] border-4 border-white shadow-inner', carta.cor]">
            {{ carta.emoji }}
          </div>
          
          <div v-else class="absolute inset-0 bg-[#A78BFA] rounded-[20px] border-4 border-white flex items-center justify-center shadow-md">
            <span class="text-white/50 text-3xl font-black">?</span>
          </div>

        </div>
      </div>
    </div>

    <div v-if="fimDeJogo" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
      <div class="bg-white rounded-[40px] p-8 text-center max-w-sm w-full shadow-2xl animate-bounce-in border-4 border-white">
        <div class="text-7xl mb-4">🎉</div>
        <h2 class="text-2xl font-black text-[#A78BFA] mb-2">Parabéns!</h2>
        <p class="text-gray-500 font-bold mb-6">Você encontrou todos os pares!</p>
        <div class="space-y-3">
            <button @click="iniciarJogo" class="bg-[#A78BFA] hover:bg-[#8B5CF6] text-white font-extrabold py-3 px-8 rounded-[20px] shadow-lg transition-all w-full">Jogar Novamente</button>
            <button @click="router.push('/jogos')" class="bg-gray-100 hover:bg-gray-200 text-gray-500 font-extrabold py-3 px-8 rounded-[20px] shadow-sm transition-all w-full">Voltar para Jogos</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
.animate-bounce-in { animation: bounceIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
@keyframes bounceIn { 0% { transform: scale(0.8); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
.animate-bounce-slow { animation: bounce 3s infinite; }
@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
</style>