<script setup>
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { useAuthStore } from '@/stores/auth';
import AvatarSelectorModal from '@/components/AvatarSelectorModal.vue';

const router = useRouter();
const authStore = useAuthStore();

const nome = computed(() => authStore.criancaSelecionada?.nome || authStore.user?.name || 'Amiguinho');
const avatarAtual = computed(() => authStore.criancaSelecionada?.avatarUrl || authStore.user?.avatarUrl);

// Estados do Menu e Modal
const menuAberto = ref(false);
const modalAvatarAberto = ref(false);

// Dados do Dashboard
const atividadesPendentes = ref([]); 
const mostrarTodasPendencias = ref(false);
const dicaDoDia = ref(null);
const carregando = ref(true);

// Semanário
const semanarioAtual = ref(null);
const verSemanarioCompleto = ref(false);

const listaPendenciasExibida = computed(() => {
    if (mostrarTodasPendencias.value) return atividadesPendentes.value;
    return atividadesPendentes.value.slice(0, 3);
});

// Lógica para determinar o dia da semana atual
const diaSemanaHoje = computed(() => {
    const dia = new Date().getDay();
    const map = { 1: 'segunda', 2: 'terca', 3: 'quarta', 4: 'quinta', 5: 'sexta' };
    return map[dia] || 'fim_de_semana';
});

function getObjetivosDoDia(diaKey) {
    if (!semanarioAtual.value || !semanarioAtual.value.objetivos || !Array.isArray(semanarioAtual.value.objetivos)) {
        return [];
    }
    return semanarioAtual.value.objetivos.filter(obj => obj.dia === diaKey);
}

const conteudoHoje = computed(() => {
    if (!semanarioAtual.value) return null;
    const diaKey = diaSemanaHoje.value;
    if (diaKey === 'fim_de_semana') return { titulo: "Fim de Semana", texto: "Aproveite para descansar e brincar! 🎈", objetivos: [] };
    const titulos = { segunda: 'Segunda-feira', terca: 'Terça-feira', quarta: 'Quarta-feira', quinta: 'Quinta-feira', sexta: 'Sexta-feira' };
    return {
        titulo: titulos[diaKey],
        texto: semanarioAtual.value[diaKey] || "Sem atividades registradas para hoje.",
        objetivos: getObjetivosDoDia(diaKey)
    };
});

onMounted(async () => {
  await carregarDados();
});

async function carregarDados() {
  const childId = authStore.criancaSelecionada?.id || authStore.user?.id;
  try {
    // Carregar Semanário
    const resSemanario = await api.get('/api/semanario/atual');
    semanarioAtual.value = resSemanario.data;
    if (semanarioAtual.value && semanarioAtual.value.objetivos) {
        try { semanarioAtual.value.objetivos = JSON.parse(semanarioAtual.value.objetivos); } catch (e) { semanarioAtual.value.objetivos = []; }
    }

    // Carregar Atividades Pendentes (O backend agora retorna múltiplas tarefas LIVRES se houver)
    const resPendentes = await api.get('/api/atividades/pendentes', { headers: { 'x-child-id': childId } });
    atividadesPendentes.value = Array.isArray(resPendentes.data) ? resPendentes.data : [];

    // Definir Dica do Dia baseada nas pendências
    const qtdPendentes = atividadesPendentes.value.length;
    if (qtdPendentes === 0) {
        dicaDoDia.value = { titulo: 'Parabéns, tudo em dia!', texto: 'Você é incrível! Todas as suas tarefas estão completas. Aproveite para brincar ou fazer um desenho livre! 🌟', cor: 'bg-green-50 text-green-700 border-green-200', icon: '🏆' };
    } else if (qtdPendentes <= 2) {
        dicaDoDia.value = { titulo: 'Falta pouco!', texto: `Você tem apenas ${qtdPendentes} tarefinha(s) para fazer. Que tal terminar agora e ficar livre? 🚀`, cor: 'bg-blue-50 text-blue-700 border-blue-200', icon: '⏳' };
    } else {
        dicaDoDia.value = { titulo: 'Vamos nos organizar?', texto: `Você tem ${qtdPendentes} atividades esperando. Respire fundo e faça uma de cada vez. Você consegue! 💪`, cor: 'bg-orange-50 text-orange-700 border-orange-200', icon: '📅' };
    }
  } catch (e) { 
      console.error("Erro ao carregar dados", e); 
      dicaDoDia.value = { titulo: 'Olá!', texto: 'Bem-vindo ao seu cantinho!', cor: 'bg-gray-50 text-gray-600 border-gray-200', icon: '👋' };
  } finally {
      carregando.value = false;
  }
}

function irParaDesenho(tarefaEspecifica = null) {
  const tarefa = tarefaEspecifica || atividadesPendentes.value[0];
  if (tarefa) {
    // Envia os dados da tarefa para a tela de desenho
    router.push(`/desenho?tipo=${tarefa.tipo}&conteudo=${tarefa.conteudo}`);
  } else {
    router.push('/desenho');
  }
}

function trocarPerfil() { router.push('/selecionar-perfil'); }
function sairTotalmente() { authStore.logout(); }

async function atualizarAvatar(novoAvatar) {
  try {
    if (authStore.criancaSelecionada && authStore.criancaSelecionada.id) {
        await api.put(`/api/responsavel/dependentes/${authStore.criancaSelecionada.id}/avatar`, { avatarUrl: novoAvatar });
        authStore.criancaSelecionada.avatarUrl = novoAvatar;
    } else {
        await api.put('/auth/meu-perfil/avatar', { avatarUrl: novoAvatar });
        authStore.user.avatarUrl = novoAvatar;
    }
    // Atualiza estado local
    authStore.setLoginData(authStore.user, authStore.token);
    modalAvatarAberto.value = false;
  } catch (e) { console.error("Erro ao atualizar avatar:", e); }
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] font-nunito relative overflow-x-hidden pb-40 transition-colors duration-300 flex flex-col items-center">
    
    <!-- Elementos decorativos de fundo -->
    <div class="absolute top-10 left-10 text-4xl animate-float-slow opacity-80 rotate-12 pointer-events-none">✨</div>
    <div class="absolute top-20 right-10 text-5xl animate-float-delayed opacity-90 -rotate-12 pointer-events-none">🌈</div>

    <!-- Cabeçalho -->
    <header class="pt-8 px-6 text-center relative z-10 w-full max-w-4xl">
      <div class="flex items-center justify-center gap-2 mb-2">
        <span class="text-2xl md:text-3xl">🩷</span>
        <h1 class="text-3xl md:text-4xl font-extrabold text-[#C084FC] drop-shadow-sm leading-tight">Cantinho do<br>Saber</h1>
        <span class="text-2xl md:text-3xl">🩷</span>
      </div>
      <p class="text-gray-400 font-bold text-sm md:text-lg tracking-wide">Olá, {{ nome }}! 💜</p>
    </header>

    <!-- Conteúdo Principal -->
    <div class="w-full px-4 mt-6 flex flex-col items-center gap-6 max-w-4xl relative z-10 animate-fade-in">
        
        <!-- Bloco do Semanário -->
        <div v-if="semanarioAtual" class="w-full bg-white rounded-3xl border-2 border-indigo-100 shadow-md overflow-hidden">
            <div class="bg-indigo-50 p-4 flex items-center justify-between border-b border-indigo-100">
                <div class="flex items-center gap-3">
                    <span class="text-2xl">📅</span>
                    <h3 class="font-black text-indigo-600 text-sm md:text-base uppercase">Semanário da Turma</h3>
                </div>
                <button @click="verSemanarioCompleto = !verSemanarioCompleto" class="text-xs font-bold bg-white text-indigo-500 border border-indigo-200 px-3 py-1.5 rounded-xl hover:bg-indigo-50 transition-colors">
                    {{ verSemanarioCompleto ? 'Ver só Hoje' : 'Ver Semana Toda' }}
                </button>
            </div>
            
            <div class="p-6">
                <!-- Visão Simplificada (Só Hoje) -->
                <div v-if="!verSemanarioCompleto && conteudoHoje">
                    <h4 class="font-black text-gray-800 text-base md:text-lg mb-3">{{ conteudoHoje.titulo }} <span class="text-xs font-normal text-gray-400 ml-2">(Hoje)</span></h4>
                    <p class="text-sm md:text-base font-bold text-gray-600 leading-relaxed whitespace-pre-line bg-gray-50 p-4 rounded-2xl border border-dashed border-gray-200 mb-4">
                        {{ conteudoHoje.texto }}
                    </p>
                    <div v-if="conteudoHoje.objetivos && conteudoHoje.objetivos.length > 0">
                        <h5 class="text-xs font-black text-indigo-400 uppercase mb-2">🎯 O que vamos aprender hoje:</h5>
                        <ul class="space-y-2">
                            <li v-for="(obj, idx) in conteudoHoje.objetivos" :key="idx" class="flex items-start gap-2 bg-indigo-50 p-2 rounded-xl border border-indigo-100">
                                <span class="text-indigo-500 mt-0.5">🔹</span>
                                <div><span class="block text-[10px] font-black uppercase text-indigo-400">{{ obj.categoria }}</span><span class="text-xs font-bold text-gray-700 leading-tight">{{ obj.descricao }}</span></div>
                            </li>
                        </ul>
                    </div>
                </div>
                <!-- Visão Completa (Todos os dias) -->
                <div v-else class="space-y-6 animate-fade-in">
                    <div v-for="(dia, key) in {segunda: 'Segunda', terca: 'Terça', quarta: 'Quarta', quinta: 'Quinta', sexta: 'Sexta'}" :key="key">
                        <div class="flex items-center gap-2 mb-1"><h5 class="text-xs font-black text-indigo-400 uppercase">{{ dia }}</h5><span v-if="getObjetivosDoDia(key).length > 0" class="bg-indigo-100 text-indigo-600 text-[9px] px-1.5 py-0.5 rounded-md font-bold">{{ getObjetivosDoDia(key).length }} obj.</span></div>
                        <p class="text-sm font-bold text-gray-600 bg-gray-50 p-3 rounded-xl border border-gray-100 whitespace-pre-line">{{ semanarioAtual[key] || '---' }}</p>
                        <div v-if="getObjetivosDoDia(key).length > 0" class="mt-2 pl-2 border-l-2 border-indigo-200 ml-1"><div v-for="(obj, idx) in getObjetivosDoDia(key)" :key="idx" class="text-[10px] text-gray-500 font-bold mb-1">• {{ obj.descricao }}</div></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Dica do Dia -->
        <div v-if="dicaDoDia" class="w-full">
            <div :class="['p-4 rounded-3xl border flex items-center gap-4 shadow-sm transition-all', dicaDoDia.cor]">
                <div class="text-3xl shrink-0">{{ dicaDoDia.icon }}</div>
                <div class="flex-1"><h3 class="font-extrabold text-sm md:text-base uppercase opacity-80 mb-1">{{ dicaDoDia.titulo }}</h3><p class="text-sm md:text-base font-bold leading-snug">{{ dicaDoDia.texto }}</p></div>
            </div>
        </div>

        <!-- Lista de Pendências -->
        <div v-if="atividadesPendentes.length > 0" class="w-full min-h-[80px]">
            <div class="flex items-center justify-between px-2 mb-2">
                <span class="text-sm font-black text-red-500 uppercase animate-pulse">⚠️ Você tem {{ atividadesPendentes.length }} tarefas!</span>
            </div>

            <div class="space-y-3">
                <div v-for="(tarefa, index) in listaPendenciasExibida" :key="index" @click="irParaDesenho(tarefa)" :class="['w-full bg-white border-2 rounded-2xl p-1 shadow-md cursor-pointer hover:scale-[1.01] transition-transform', index === 0 && !mostrarTodasPendencias ? 'border-red-400 ring-2 ring-red-100' : 'border-orange-200 opacity-90']">
                    <div :class="['rounded-xl p-4 flex items-center gap-4', index === 0 && !mostrarTodasPendencias ? 'bg-red-50' : 'bg-orange-50']">
                        <div class="bg-white p-3 rounded-full text-2xl shadow-sm border border-gray-100">{{ index === 0 && !mostrarTodasPendencias ? '🔥' : '📩' }}</div>
                        <div class="flex-1">
                            <h3 :class="['font-black text-sm uppercase', index === 0 && !mostrarTodasPendencias ? 'text-red-500' : 'text-orange-400']">{{ index === 0 && !mostrarTodasPendencias ? 'Fazer Agora' : 'Pendente' }}</h3>
                            <p class="text-gray-600 font-bold text-xs md:text-sm">
                                <span v-if="tarefa.tipo !== 'LIVRE'">Atividade: <span class="font-black text-gray-800">{{ tarefa.tipo }} {{ tarefa.conteudo }}</span></span>
                                <span v-else class="font-black text-gray-800">Atividade Livre 🎨</span>
                            </p>
                        </div>
                        <span :class="['text-xl', index === 0 && !mostrarTodasPendencias ? 'text-red-400' : 'text-orange-300']">➜</span>
                    </div>
                </div>
                <div v-if="atividadesPendentes.length > 3" class="text-center pt-2">
                    <button @click="mostrarTodasPendencias = !mostrarTodasPendencias" class="text-sm font-bold text-indigo-500 hover:text-indigo-700 underline">{{ mostrarTodasPendencias ? 'Ver menos' : `Ver mais ${atividadesPendentes.length - 3} tarefas...` }}</button>
                </div>
            </div>
        </div>
        
        <div v-else-if="carregando" class="text-center py-6 text-gray-400 text-sm font-bold animate-pulse w-full">Verificando tarefas...</div>
    </div>

    <!-- Botões Grandes de Ação -->
    <main class="w-full max-w-4xl px-4 mt-6 grid grid-cols-2 md:grid-cols-3 gap-4 pb-4 relative z-10">
      <button @click="irParaDesenho()" class="bg-white p-5 rounded-[30px] border-2 border-white shadow-sm hover:-translate-y-1 transition-all group flex flex-col items-center justify-center text-center h-36 relative overflow-hidden">
        <div class="absolute top-0 right-0 w-20 h-20 bg-orange-50 rounded-bl-[50px] -mr-5 -mt-5"></div>
        <div class="text-4xl mb-3 relative z-10">🎨</div>
        <h2 class="text-base font-black text-orange-400 leading-tight relative z-10">Pintar<br>Agora</h2>
      </button>
      <router-link to="/jogos" class="bg-white p-5 rounded-[30px] border-2 border-white shadow-sm hover:-translate-y-1 transition-all group flex flex-col items-center justify-center text-center h-36 relative overflow-hidden">
        <div class="absolute top-0 right-0 w-20 h-20 bg-teal-50 rounded-bl-[50px] -mr-5 -mt-5"></div>
        <div class="text-4xl mb-3 relative z-10">🎮</div>
        <h2 class="text-base font-black text-teal-400 leading-tight relative z-10">Jogos<br>Divertidos</h2>
      </router-link>
      <router-link to="/emocoes" class="bg-white p-5 rounded-[30px] border-2 border-white shadow-sm hover:-translate-y-1 transition-all group flex flex-col items-center justify-center text-center h-36 col-span-2 md:col-span-1 relative overflow-hidden">
          <div class="absolute top-0 right-0 w-20 h-20 bg-purple-50 rounded-bl-[50px] -mr-5 -mt-5"></div>
          <div class="text-4xl mb-3 relative z-10">❤️</div>
          <h2 class="text-base font-black text-[#A78BFA] leading-tight relative z-10">Minhas<br>Emoções</h2>
      </router-link>
    </main>

    <!-- Menu de Perfil Flutuante -->
    <div v-if="menuAberto" class="fixed inset-0 z-40 flex items-end sm:items-center justify-center pointer-events-none">
      <div class="absolute inset-0 bg-[#A78BFA]/20 backdrop-blur-sm pointer-events-auto transition-opacity" @click="menuAberto = false"></div>
      <div class="bg-white w-full max-w-md p-8 rounded-t-[40px] sm:rounded-[40px] shadow-2xl pointer-events-auto transform transition-transform animate-slide-up border-t-8 border-white flex flex-col gap-6">
        <div class="text-center border-b border-gray-100 pb-6">
          <div class="w-24 h-24 mx-auto rounded-full p-1.5 bg-white border-4 border-[#C4B5FD] mb-3 relative shadow-lg">
            <img :src="avatarAtual" class="w-full h-full rounded-full object-cover">
            <button @click="modalAvatarAberto = true" class="absolute bottom-0 right-0 bg-[#C084FC] text-white rounded-full p-2 border-2 border-white">✏️</button>
          </div>
          <h3 class="text-2xl font-extrabold text-[#A78BFA]">{{ nome }}</h3>
        </div>
        <button @click="trocarPerfil" class="w-full bg-purple-50 text-[#A78BFA] font-bold py-4 rounded-[20px]">🔄 Trocar Perfil</button>
        <button @click="sairTotalmente" class="w-full bg-red-50 text-red-400 font-bold py-4 rounded-[20px]">🚪 Sair da Conta</button>
        <button @click="menuAberto = false" class="text-gray-300 font-bold text-sm">Fechar</button>
      </div>
    </div>

    <!-- Modal de Seleção de Avatar -->
    <AvatarSelectorModal v-if="modalAvatarAberto" @close="modalAvatarAberto = false" @select="atualizarAvatar" />

    <!-- Barra de Navegação Inferior -->
    <nav class="fixed bottom-0 left-0 right-0 bg-white/95 backdrop-blur-md border-t-2 border-purple-50 px-4 pb-4 pt-3 rounded-t-[30px] z-30 flex justify-around items-end h-20 shadow-lg max-w-4xl mx-auto">
      <router-link to="/home" class="nav-item"><span class="text-2xl">🏠</span><span class="label">Início</span></router-link>
      <button @click="irParaDesenho()" class="nav-item"><span class="text-2xl">🎨</span><span class="label">Pintar</span></button>
      <button @click="menuAberto = true" class="nav-item group">
        <div class="w-10 h-10 rounded-full border-2 border-purple-100 shadow-sm overflow-hidden group-hover:border-[#C084FC] group-hover:scale-110 transition-all bg-gray-100">
          <img :src="avatarAtual" class="w-full h-full object-cover">
        </div>
        <span class="label group-hover:text-[#C084FC]">Perfil</span>
      </button>
    </nav>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;700;900&display=swap');
.font-nunito { font-family: 'Nunito', sans-serif; }
.nav-item { @apply flex flex-col items-center justify-center gap-1 w-14 h-full cursor-pointer transition-transform active:scale-95; }
.nav-item .label { @apply text-[9px] font-extrabold uppercase tracking-wide text-gray-300 mt-1; }
.animate-slide-up { animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-fade-in { animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }
.animate-float-delayed { animation: floatSlow 6s ease-in-out infinite; animation-delay: 2s; }
</style>