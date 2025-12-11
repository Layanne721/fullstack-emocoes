<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const emocaoSelecionada = ref(null);

const iconesSintomas = {
  coracao: '❤️⚡',
  suor: '💧',
  musculo: '💪',
  grito: '🗣️',
  choro: '😭',
  cansaco: '💤',
  peito: '🫁',
  barriga: '🦋',
  tremedeira: '〰️',
  rosto: '😳',
  enjoo: '🤢'
};

const emocoes = [
  {
    id: 'alegria',
    nome: 'Alegria',
    emoji: '😄', 
    tema: 'bg-yellow-400',
    corTexto: 'text-yellow-800',
    descricaoCurta: 'É uma luz que brilha dentro de você! Vontade de pular e sorrir.',
    sinaisVisuais: [
      { icone: iconesSintomas.musculo, texto: 'Muita Energia' },
      { icone: iconesSintomas.grito, texto: 'Fala Rápida' },
      { icone: '😆', texto: 'Sorrisos' },
      { icone: '⚡', texto: 'Agitação' }
    ],
    dica: 'Brinque, pule e abrace quem você ama! Espalhe essa luz.',
    jogoSugestao: {
      titulo: 'Brincar de Caça à Alegria! 🏃',
      rota: '/jogos/alegria',
      corBotao: 'bg-yellow-500 hover:bg-yellow-600 text-white'
    }
  },
  {
    id: 'tristeza',
    nome: 'Tristeza',
    emoji: '😢',
    tema: 'bg-blue-400',
    corTexto: 'text-blue-800',
    descricaoCurta: 'Quando o coração fica apertadinho e a gente quer ficar quieto.',
    sinaisVisuais: [
      { icone: iconesSintomas.peito, texto: 'Aperto' },
      { icone: iconesSintomas.cansaco, texto: 'Desânimo' },
      { icone: iconesSintomas.choro, texto: 'Choro' },
      { icone: '🧸', texto: 'Quer Colo' }
    ],
    dica: 'Chorar lava a alma. Peça um abraço de urso bem apertado.',
    jogoSugestao: {
      titulo: 'Jogar Memória para Distrair 🧩',
      rota: '/jogos/memoria',
      corBotao: 'bg-blue-500 hover:bg-blue-600 text-white'
    }
  },
  {
    id: 'raiva',
    nome: 'Raiva',
    emoji: '😠',
    tema: 'bg-red-500',
    corTexto: 'text-red-900',
    descricaoCurta: 'Um vulcão que quer explodir quando algo parece injusto.',
    sinaisVisuais: [
      { icone: iconesSintomas.coracao, texto: 'Coração Rápido' },
      { icone: iconesSintomas.musculo, texto: 'Corpo Duro' },
      { icone: '🔥', texto: 'Calor' },
      { icone: iconesSintomas.grito, texto: 'Vontade de Gritar' }
    ],
    dica: 'Respire fundo: cheire a flor 🌸 e sopre a vela 🕯️.',
    jogoSugestao: {
      titulo: 'Soltar a Raiva no Balão 🎈',
      rota: '/jogos/balao',
      corBotao: 'bg-red-500 hover:bg-red-600 text-white'
    }
  },
  {
    id: 'medo',
    nome: 'Medo',
    emoji: '😨',
    tema: 'bg-purple-500',
    corTexto: 'text-purple-900',
    descricaoCurta: 'O alarme de segurança do corpo quando achamos que há perigo.',
    sinaisVisuais: [
      { icone: iconesSintomas.coracao, texto: 'Bate Rápido' },
      { icone: iconesSintomas.barriga, texto: 'Frio na Barriga' },
      { icone: iconesSintomas.tremedeira, texto: 'Tremedeira' },
      { icone: iconesSintomas.suor, texto: 'Suor Frio' }
    ],
    dica: 'Você é corajoso! Segure na mão de um adulto e conte o que houve.',
    jogoSugestao: {
      titulo: 'Respirar para Acalmar 🌸',
      rota: '/jogos/respiracao',
      corBotao: 'bg-purple-500 hover:bg-purple-600 text-white'
    }
  },
  {
    id: 'nojo',
    nome: 'Nojo',
    emoji: '🤢',
    tema: 'bg-green-500',
    corTexto: 'text-green-900',
    descricaoCurta: 'Serve para nos proteger de coisas estragadas ou ruins.',
    sinaisVisuais: [
      { icone: iconesSintomas.enjoo, texto: 'Enjoo' },
      { icone: '🤐', texto: 'Sem Fome' },
      { icone: '👃', texto: 'Nariz Torto' },
      { icone: '🤮', texto: 'Ânsia' }
    ],
    dica: 'Se for comida ruim, não coma! Se for algo chato, afaste-se.',
    jogoSugestao: {
      titulo: 'Cozinhar Sopa de Nojinho 🥣',
      rota: '/jogos/nojinho',
      corBotao: 'bg-green-600 hover:bg-green-700 text-white'
    }
  },
  {
    id: 'vergonha',
    nome: 'Vergonha',
    emoji: '😳',
    tema: 'bg-orange-400',
    corTexto: 'text-orange-900',
    descricaoCurta: 'Quando achamos que todos estão olhando ou fizemos algo errado.',
    sinaisVisuais: [
      { icone: '🍅', texto: 'Rosto Vermelho' },
      { icone: iconesSintomas.coracao, texto: 'Coração Rápido' },
      { icone: '😶', texto: 'Sem Voz' },
      { icone: '🙈', texto: 'Esconder' }
    ],
    dica: 'Todo mundo erra. Respire, peça desculpas e siga em frente!',
    jogoSugestao: {
      titulo: 'Respirar e Relaxar 🧘',
      rota: '/jogos/respiracao',
      corBotao: 'bg-orange-500 hover:bg-orange-600 text-white'
    }
  },
  {
    id: 'amor',
    nome: 'Amor',
    emoji: '🥰',
    tema: 'bg-pink-400',
    corTexto: 'text-pink-900',
    descricaoCurta: 'Sentir-se quentinho, cuidado e querer o bem de alguém.',
    sinaisVisuais: [
      { icone: iconesSintomas.barriga, texto: 'Borboletas' },
      { icone: '😍', texto: 'Olhos Brilhando' },
      { icone: '💓', texto: 'Coração Feliz' },
      { icone: '🤗', texto: 'Quer Abraço' }
    ],
    dica: 'Faça um desenho lindo para quem você ama hoje!',
    jogoSugestao: {
      titulo: 'Jogar Junto (Memória) 🧩',
      rota: '/jogos/memoria',
      corBotao: 'bg-pink-500 hover:bg-pink-600 text-white'
    }
  }
];

function abrirEmocao(emocao) {
  emocaoSelecionada.value = emocao;
}

function fecharModal() {
  emocaoSelecionada.value = null;
}

function irParaJogo(rota) {
  router.push(rota);
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] font-nunito relative overflow-hidden pb-10">
    
    <div class="absolute top-10 right-10 text-4xl animate-float-slow opacity-60 pointer-events-none">✨</div>
    <div class="absolute bottom-10 left-10 text-5xl animate-bounce-slow opacity-60 pointer-events-none">🌈</div>

    <div class="absolute top-4 left-4 z-20">
      <router-link to="/home" class="bg-white w-10 h-10 flex items-center justify-center rounded-full shadow-md text-gray-500 font-bold hover:text-[#A78BFA] hover:scale-110 transition-all border-2 border-white">
        ←
      </router-link>
    </div>

    <div class="relative pt-8 pb-4 px-4 text-center z-10">
      <h1 class="text-2xl md:text-5xl font-black text-[#A78BFA] drop-shadow-sm tracking-tight mt-6" style="font-family: 'Nunito', sans-serif;">
        Como eu me sinto?
      </h1>
      <p class="text-gray-400 font-bold mt-1 text-sm md:text-lg">Toque no personagem!</p>
    </div>

    <div class="max-w-6xl mx-auto px-4 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3 md:gap-6 relative z-10">
      <div 
        v-for="item in emocoes" 
        :key="item.id"
        @click="abrirEmocao(item)"
        class="group relative h-36 sm:h-48 md:h-64 rounded-[30px] shadow-lg cursor-pointer transition-all duration-300 hover:-translate-y-1 overflow-hidden bg-white border-4 border-white hover:border-[#E9D5FF]"
      >
        <div :class="`absolute inset-0 opacity-20 ${item.tema}`"></div>

        <div class="absolute inset-0 flex flex-col items-center justify-center p-2">
          <div class="text-5xl sm:text-6xl md:text-8xl transform group-hover:scale-110 transition-transform duration-500 drop-shadow-md filter">
            {{ item.emoji }}
          </div>
          
          <h3 :class="`mt-2 md:mt-4 text-sm sm:text-lg md:text-2xl font-black ${item.corTexto} uppercase tracking-wider bg-white/80 px-3 py-0.5 rounded-full backdrop-blur-sm shadow-sm`">
            {{ item.nome }}
          </h3>
        </div>
      </div>
    </div>

    <transition name="pop">
      <div v-if="emocaoSelecionada" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="fecharModal"></div>

        <div class="relative bg-white w-full max-w-2xl rounded-[40px] shadow-2xl overflow-hidden flex flex-col md:flex-row max-h-[90vh] border-4 border-white">
          
          <div :class="`md:w-2/5 ${emocaoSelecionada.tema} p-6 flex flex-col items-center justify-center text-center relative overflow-hidden`">
             <button @click="fecharModal" class="absolute top-4 left-4 md:hidden bg-white/30 text-white rounded-full w-10 h-10 font-bold">✕</button>

            <div class="text-8xl md:text-[10rem] animate-bounce-slow drop-shadow-2xl z-10">
              {{ emocaoSelecionada.emoji }}
            </div>
            <h2 class="text-3xl md:text-4xl font-black text-white mt-2 drop-shadow-md border-b-4 border-white/30 pb-2">
              {{ emocaoSelecionada.nome }}
            </h2>
          </div>

          <div class="md:w-3/5 p-5 md:p-8 bg-white overflow-y-auto custom-scrollbar flex flex-col">
             <button @click="fecharModal" class="hidden md:block absolute top-6 right-6 text-slate-300 hover:text-slate-500 text-3xl font-bold transition-colors">✕</button>

            <p class="text-sm md:text-lg text-slate-600 font-bold leading-relaxed mb-4">
              {{ emocaoSelecionada.descricaoCurta }}
            </p>

            <div class="mb-4">
              <h4 class="text-[10px] md:text-xs font-black text-gray-400 uppercase tracking-widest mb-2 flex items-center gap-2">
                ⚡ No corpo:
              </h4>
              <div class="grid grid-cols-2 gap-2">
                <div 
                  v-for="(sinal, idx) in emocaoSelecionada.sinaisVisuais" 
                  :key="idx"
                  class="flex items-center gap-2 bg-[#F9FAFB] border-2 border-gray-100 p-2 rounded-xl"
                >
                  <span class="text-lg md:text-2xl">{{ sinal.icone }}</span>
                  <span class="text-xs md:text-sm font-bold text-slate-700 leading-none">{{ sinal.texto }}</span>
                </div>
              </div>
            </div>

            <div class="relative bg-blue-50 border-2 border-blue-100 p-4 rounded-[20px] rounded-tl-none mt-1 mb-4">
              <span class="absolute -top-3 -left-[2px] text-blue-200 text-3xl">🗨️</span>
              <h4 class="text-blue-500 font-black text-[10px] uppercase mb-1">Dica Amiga</h4>
              <p class="text-blue-800 font-bold text-sm md:text-lg">
                "{{ emocaoSelecionada.dica }}"
              </p>
            </div>

            <div class="mt-auto pt-2">
              <button 
                @click="irParaJogo(emocaoSelecionada.jogoSugestao.rota)"
                :class="`w-full py-3 md:py-4 rounded-[20px] shadow-lg transform transition-all active:scale-95 hover:-translate-y-1 font-black text-base md:text-lg flex items-center justify-center gap-2 ${emocaoSelecionada.jogoSugestao.corBotao}`"
              >
                 {{ emocaoSelecionada.jogoSugestao.titulo }}
                 <span class="bg-white/20 rounded-full w-6 h-6 md:w-8 md:h-8 flex items-center justify-center text-xs">➜</span>
              </button>
            </div>

          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
.pop-enter-active, .pop-leave-active { transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
.pop-enter-from, .pop-leave-to { transform: scale(0.8); opacity: 0; }
@keyframes float { 0%, 100% { transform: translateY(0) rotate(0deg); } 50% { transform: translateY(-15px) rotate(5deg); } }
.animate-bounce-slow { animation: float 4s infinite ease-in-out; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: #e2e8f0; border-radius: 20px; }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
</style>