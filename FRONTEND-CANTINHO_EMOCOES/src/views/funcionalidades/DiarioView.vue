<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api'; // <--- IMPORTAÇÃO NOVA
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const historico = ref([]);
const loading = ref(true);
const salvando = ref(false);
const emocaoSelecionada = ref(null);
const intensidade = ref(3);
const relato = ref('');

// Estado para o Cartão de Enfrentamento (Modal)
const sugestaoAtiva = ref(null);

const nomeQuemEscreve = computed(() => authStore.criancaSelecionada?.nome || 'Você');

const emocoes = [
  { valor: 'FELIZ', emoji: '😊', nome: 'Feliz', cor: 'bg-yellow-100 border-yellow-300 text-yellow-700' },
  { valor: 'CALMO', emoji: '😌', nome: 'Calmo', cor: 'bg-blue-100 border-blue-300 text-blue-700' },
  { valor: 'TRISTE', emoji: '😢', nome: 'Triste', cor: 'bg-indigo-100 border-indigo-300 text-indigo-700' },
  { valor: 'BRAVO', emoji: '😠', nome: 'Bravo', cor: 'bg-red-100 border-red-300 text-red-700' },
  { valor: 'ANSIOSO', emoji: '😬', nome: 'Ansioso', cor: 'bg-orange-100 border-orange-300 text-orange-700' },
  { valor: 'MEDO', emoji: '😨', nome: 'Com Medo', cor: 'bg-purple-100 border-purple-300 text-purple-700' },
];

onMounted(carregarHistorico);

async function carregarHistorico() {
  loading.value = true;
  try {
    const childId = authStore.criancaSelecionada?.id;
    // Configura apenas o header extra se necessário, auth vai automático
    const config = {
      headers: { ...(childId && { 'X-Child-Id': childId }) }
    };
    // URL relativa, o api.js resolve o host
    const response = await api.get('/api/diario/meus', config);
    historico.value = response.data;
  } catch (e) {
    console.error("Erro ao carregar:", e);
  } finally {
    loading.value = false;
  }
}

async function salvarDiario() {
  if (!emocaoSelecionada.value) return alert('Escolha como você está se sentindo! 🧸');
  
  salvando.value = true;
  
  try {
    const childId = authStore.criancaSelecionada?.id;
    const config = {
      headers: { ...(childId && { 'X-Child-Id': childId }) }
    };

    await api.post('/api/diario', {
      emocao: emocaoSelecionada.value,
      intensidade: intensidade.value,
      relato: relato.value
    }, config);
    
    // --- LÓGICA INTELIGENTE DE ENFRENTAMENTO ---
    if (intensidade.value >= 3) { 
      if (emocaoSelecionada.value === 'BRAVO') {
        sugestaoAtiva.value = {
          emoji: '🎈',
          titulo: 'Muita raiva aí?',
          texto: 'A raiva é como um balão cheio prestes a estourar. Que tal jogar o Jogo do Balão para soltar isso?',
          btnTexto: 'Ir para o Balão',
          rota: '/jogos/balao',
          cor: 'from-red-100 to-orange-100'
        };
      } else if (['MEDO', 'ANSIOSO'].includes(emocaoSelecionada.value)) {
        sugestaoAtiva.value = {
          emoji: '🌬️',
          titulo: 'Coração acelerado?',
          texto: 'Quer ajuda para se acalmar? Vamos respirar juntos no Jogo da Respiração?',
          btnTexto: 'Vamos Respirar',
          rota: '/jogos/respiracao',
          cor: 'from-purple-100 to-blue-100'
        };
      } else if (emocaoSelecionada.value === 'TRISTE') {
        sugestaoAtiva.value = {
          emoji: '😀',
          titulo: 'Vamos buscar alegria?',
          texto: 'Sentir tristeza faz parte, mas que tal procurarmos algumas coisas felizes no jogo Caça à Alegria?',
          btnTexto: 'Caçar Alegria',
          rota: '/jogos/caca-alegria',
          cor: 'from-yellow-100 to-orange-100'
        };
      }
    }

    if (!sugestaoAtiva.value) {
      alert('Salvo no seu diário! ✨');
    }

    emocaoSelecionada.value = null;
    relato.value = '';
    intensidade.value = 3;
    await carregarHistorico();

  } catch (e) {
    alert('Erro ao salvar. Tente novamente.');
  } finally {
    salvando.value = false;
  }
}

function fecharSugestao() {
  sugestaoAtiva.value = null;
}

function aceitarSugestao() {
  if (sugestaoAtiva.value) {
    router.push(sugestaoAtiva.value.rota);
    sugestaoAtiva.value = null;
  }
}

function formatarData(dataIso) {
  if (!dataIso) return '';
  return new Date(dataIso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', hour: '2-digit', minute: '2-digit' });
}

function getDetalhesEmocao(valor) { 
  if (valor === 'CRIATIVO') {
    return { valor: 'CRIATIVO', emoji: '🎨', nome: 'Desenho', cor: 'bg-teal-100 border-teal-300 text-teal-700' };
  }
  return emocoes.find(e => e.valor === valor) || emocoes[0]; 
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] font-nunito pb-32 pt-6 px-4 relative overflow-hidden">
    
    <div class="absolute top-10 left-6 text-4xl animate-float-slow opacity-60 pointer-events-none">✨</div>
    <div class="absolute top-20 right-6 text-5xl animate-bounce-slow opacity-60 pointer-events-none">🌈</div>
    <div class="absolute bottom-20 left-10 text-3xl animate-float-delayed opacity-50 pointer-events-none">🦋</div>

    <div class="absolute top-6 left-6 z-20">
      <router-link to="/home" class="bg-white w-10 h-10 flex items-center justify-center rounded-full shadow-md text-gray-500 font-bold hover:text-[#A78BFA] hover:scale-110 transition-all border-2 border-white">
        ←
      </router-link>
    </div>

    <div class="max-w-3xl mx-auto text-center mb-8 pt-4 relative z-10">
      <h1 class="text-3xl font-extrabold text-[#A78BFA] flex items-center justify-center gap-2 drop-shadow-sm" style="font-family: 'Nunito', sans-serif;">
        <span>📔</span> Diário de {{ nomeQuemEscreve }}
      </h1>
      <p class="text-gray-400 font-bold mt-1 text-sm">Como você está se sentindo agora?</p>
    </div>

    <div class="max-w-3xl mx-auto space-y-8 relative z-10">
      
      <div class="bg-white p-6 rounded-[30px] shadow-[0_10px_30px_-10px_rgba(167,139,250,0.15)] border-4 border-white">
        <div class="grid grid-cols-2 sm:grid-cols-3 gap-4">
          <button 
            v-for="emo in emocoes" 
            :key="emo.valor" 
            @click="emocaoSelecionada = emo.valor" 
            :class="[
              'p-4 rounded-[20px] border-4 transition-all transform hover:scale-105 flex flex-col items-center gap-2', 
              emocaoSelecionada === emo.valor 
                ? emo.cor + ' scale-105 shadow-md ring-2 ring-[#E9D5FF]' 
                : 'bg-[#F9FAFB] border-transparent hover:bg-white hover:border-gray-100'
            ]"
          >
            <span class="text-4xl filter drop-shadow-sm">{{ emo.emoji }}</span>
            <span class="font-bold text-sm">{{ emo.nome }}</span>
          </button>
        </div>
      </div>

      <div v-if="emocaoSelecionada" class="bg-white p-6 rounded-[30px] shadow-suave border-4 border-white animate-bounce-in">
        <div class="mb-6 text-center">
          <label class="block text-gray-400 font-extrabold mb-3 text-xs uppercase tracking-wider">Tamanho do sentimento</label>
          <div class="flex justify-center gap-4">
            <button 
              v-for="n in 5" 
              :key="n" 
              @click="intensidade = n" 
              :class="[
                'w-10 h-10 rounded-full font-bold transition-all border-2 flex items-center justify-center', 
                intensidade >= n 
                  ? 'bg-[#A78BFA] border-[#A78BFA] text-white scale-110 shadow-lg' 
                  : 'bg-gray-100 border-gray-200 text-gray-400'
              ]"
            >
              {{ n }}
            </button>
          </div>
        </div>
        
        <div class="mb-6">
          <label class="block text-gray-400 font-extrabold mb-2 ml-1 text-xs uppercase tracking-wider">Quer contar o que houve?</label>
          <textarea 
            v-model="relato" 
            rows="3" 
            class="w-full p-4 rounded-[20px] bg-[#F9FAFB] border-2 border-transparent focus:bg-white focus:border-[#A78BFA] outline-none font-bold text-gray-600 resize-none shadow-inner transition-all placeholder-gray-300" 
            placeholder="Hoje aconteceu..."
          ></textarea>
        </div>
        
        <button 
          @click="salvarDiario" 
          :disabled="salvando" 
          class="w-full bg-gradient-to-r from-[#C4B5FD] to-[#A78BFA] hover:to-[#8B5CF6] text-white font-extrabold py-4 rounded-[20px] shadow-lg shadow-purple-200 transform active:scale-[0.98] transition-all text-lg flex justify-center items-center gap-2"
        >
          <span v-if="salvando">💾 Salvando...</span>
          <span v-else>✨ Guardar no Diário</span>
        </button>
      </div>

      <div class="mt-12">
        <h2 class="text-xl font-extrabold text-[#A78BFA] mb-4 ml-2 flex items-center gap-2">
          <span>📅</span> Histórico
        </h2>
        
        <div v-if="historico.length === 0" class="text-center py-12 opacity-60 bg-white rounded-[30px] border-4 border-white border-dashed shadow-sm">
          <div class="text-4xl mb-2">🍃</div>
          <p class="font-bold text-gray-400">O diário está vazio por enquanto.</p>
        </div>
        
        <div v-else class="space-y-4">
          <div 
            v-for="item in historico" 
            :key="item.id" 
            class="bg-white p-5 rounded-[25px] shadow-sm border border-gray-100 flex gap-4 items-start hover:shadow-md transition-all group"
          >
            <div :class="['w-14 h-14 min-w-[3.5rem] rounded-2xl flex items-center justify-center text-3xl border-2 shadow-sm group-hover:scale-110 transition-transform', getDetalhesEmocao(item.emocao).cor.replace('bg-', 'bg-opacity-20 ')]">
              {{ getDetalhesEmocao(item.emocao).emoji }}
            </div>
            
            <div class="flex-1">
              <div class="flex justify-between items-center mb-1">
                <h3 class="font-extrabold text-gray-700 text-lg">{{ getDetalhesEmocao(item.emocao).nome }}</h3>
                <span class="text-[10px] font-bold text-gray-400 bg-gray-50 px-2 py-1 rounded-lg uppercase tracking-wider border border-gray-100">
                  {{ formatarData(item.dataRegistro) }}
                </span>
              </div>
              
              <div class="flex gap-1 mb-2">
                <div v-for="i in 5" :key="i" :class="['h-1.5 w-4 rounded-full', i <= item.intensidade ? 'bg-[#A78BFA]' : 'bg-gray-100']"></div>
              </div>
              
              <p v-if="item.relato" class="text-gray-600 text-sm font-bold bg-[#F9FAFB] p-3 rounded-xl inline-block border border-gray-50 w-full italic">
                "{{ item.relato }}"
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="sugestaoAtiva" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="fecharSugestao"></div>
        
        <div class="bg-white w-full max-w-sm rounded-[35px] shadow-2xl relative z-10 overflow-hidden animate-pop-in border-4 border-white text-center">
          <div :class="`bg-gradient-to-br ${sugestaoAtiva.cor} p-8 pb-10`">
            <div class="text-6xl mb-4 animate-bounce-slow drop-shadow-md">{{ sugestaoAtiva.emoji }}</div>
            <h3 class="text-2xl font-black text-gray-800 leading-tight">{{ sugestaoAtiva.titulo }}</h3>
          </div>
          
          <div class="p-6 -mt-6 bg-white rounded-t-[30px] relative">
            <p class="text-gray-600 font-bold mb-6 text-lg leading-relaxed">
              {{ sugestaoAtiva.texto }}
            </p>
            
            <div class="flex flex-col gap-3">
              <button @click="aceitarSugestao" class="w-full bg-[#A78BFA] hover:bg-[#8B5CF6] text-white font-extrabold py-4 rounded-[20px] shadow-lg transition-transform active:scale-95 text-lg">
                {{ sugestaoAtiva.btnTexto }}
              </button>
              <button @click="fecharSugestao" class="text-gray-400 font-bold text-sm py-2 hover:text-[#A78BFA] transition-colors">
                Agora não, obrigado
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
.animate-bounce-in { animation: bounceIn 0.4s ease-out; }
@keyframes bounceIn { 0% { opacity: 0; transform: scale(0.9); } 50% { opacity: 1; transform: scale(1.02); } 100% { transform: scale(1); } }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
@keyframes popIn { 0% { transform: scale(0.9) translateY(20px); opacity: 0; } 100% { transform: scale(1) translateY(0); opacity: 1; } }
.animate-pop-in { animation: popIn 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
@keyframes floatDelayed { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-10px); } }
.animate-float-delayed { animation: floatDelayed 5s ease-in-out infinite; animation-delay: 1s; }
.animate-bounce-slow { animation: floatSlow 3s ease-in-out infinite; }
</style>