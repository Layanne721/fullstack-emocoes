<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const jogos = [
  // JOGOS DE CALMA
  {
    id: 'respiracao',
    titulo: 'Cheira a Florzinha',
    emoji: '🌸',
    descricao: 'Aprenda a respirar fundo e relaxar.',
    rota: '/jogos/respiracao',
    cor: 'border-pink-200 text-pink-500 hover:border-pink-300'
  },
  {
    id: 'balao',
    titulo: 'Encher o Balão',
    emoji: '🎈',
    descricao: 'Segure para encher e solte para voar!',
    rota: '/jogos/balao',
    cor: 'border-blue-200 text-blue-500 hover:border-blue-300'
  },
  // NOVOS JOGOS
  {
    id: 'memoria',
    titulo: 'Memória das Emoções',
    emoji: '🧩',
    descricao: 'Encontre os pares e treine sua memória!',
    rota: '/jogos/memoria',
    cor: 'border-cyan-200 text-cyan-600 hover:border-cyan-300'
  },
  {
    id: 'nojinho',
    titulo: 'Chefes do Nojinho',
    emoji: '🤢',
    descricao: 'Faça uma sopa segura na cozinha!',
    rota: '/jogos/nojinho',
    cor: 'border-emerald-200 text-emerald-600 hover:border-emerald-300'
  },
  {
    id: 'alegria',
    titulo: 'Caça à Alegria',
    emoji: '😀',
    descricao: 'Encontre coisas felizes no quarto!',
    rota: '/jogos/alegria',
    cor: 'border-orange-200 text-orange-500 hover:border-orange-300'
  }
];
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] font-nunito p-6 pb-24 flex flex-col relative overflow-hidden">
    
    <!-- Decoração -->
    <div class="absolute top-10 right-10 text-4xl animate-float-slow opacity-60 pointer-events-none">✨</div>
    <div class="absolute bottom-20 left-10 text-5xl animate-bounce-slow opacity-60 pointer-events-none">🌈</div>
    
    <!-- AJUSTE: Borboleta movida para mais alto e mais para o canto (top-4 left-4) -->
    <div class="absolute top-4 left-4 text-3xl animate-float-delayed opacity-50 pointer-events-none">🦋</div>

    <div class="z-20 mb-4">
      <button @click="router.push('/home')" class="bg-white w-10 h-10 flex items-center justify-center rounded-full shadow-md text-gray-500 font-bold hover:text-[#A78BFA] hover:scale-110 transition-all border-2 border-white">←</button>
    </div>

    <div class="max-w-5xl mx-auto w-full z-10 flex-1 flex flex-col">
      <div class="text-center mb-8 animate-fade-in-down">
        <h1 class="text-3xl md:text-4xl font-extrabold text-[#A78BFA] mb-2 drop-shadow-sm">Sala de Jogos 🎮</h1>
        <p class="text-gray-400 font-bold">Escolha uma brincadeira para começar!</p>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
        <div 
          v-for="jogo in jogos" 
          :key="jogo.id"
          @click="router.push(jogo.rota)"
          :class="['p-6 rounded-[35px] border-4 cursor-pointer shadow-lg transition-all transform hover:-translate-y-1 bg-white group flex flex-col items-center text-center gap-4', jogo.cor]"
        >
          <div class="w-24 h-24 rounded-full bg-[#F9FAFB] flex items-center justify-center text-6xl shadow-inner group-hover:scale-110 transition-transform duration-300">
            {{ jogo.emoji }}
          </div>
          <div>
            <h3 class="text-xl font-black text-gray-700 mb-1 group-hover:text-inherit transition-colors">{{ jogo.titulo }}</h3>
            <p class="text-sm text-gray-400 font-bold leading-tight">{{ jogo.descricao }}</p>
          </div>
        </div>
      </div>

      <div class="mt-auto bg-white/90 backdrop-blur-sm p-6 rounded-[30px] border-2 border-[#E9D5FF] shadow-sm animate-fade-in-up max-w-3xl mx-auto w-full">
        <h4 class="font-extrabold text-[#A78BFA] mb-2 flex items-center gap-2">
          <span>💡</span> Sabia que...
        </h4>
        <p class="text-sm text-gray-500 font-bold leading-relaxed">
          Brincar ajuda a gente a entender o que sentimos! Quando respiramos fundo ou achamos coisas alegres, nosso cérebro fica mais feliz e tranquilo.
        </p>
      </div>
    </div>

  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
.animate-fade-in-down { animation: fadeInDown 0.8s ease-out; }
.animate-fade-in-up { animation: fadeInUp 0.8s ease-out; }
@keyframes fadeInDown { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
@keyframes floatDelayed { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-10px); } }
.animate-float-delayed { animation: floatDelayed 5s ease-in-out infinite; animation-delay: 1s; }
.animate-bounce-slow { animation: floatSlow 3s ease-in-out infinite; }
</style>