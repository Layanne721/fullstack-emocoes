<script setup>
import { ref, onMounted } from 'vue';
import api from '@/services/api'; // <--- IMPORTAÇÃO
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import PinModal from '@/components/PinModal.vue';
import AvatarSelectorModal from '@/components/AvatarSelectorModal.vue';

const router = useRouter();
const authStore = useAuthStore();
const dependentes = ref([]);
const loading = ref(true);

// Modais e Estados
const showPinModal = ref(false);
const showAddChildModal = ref(false);
const pinAction = ref(''); 
const showAvatarSelector = ref(false);
const modoGerenciamento = ref(false); 
const alunoParaExcluir = ref(null);

const cadastrando = ref(false);
const novoAluno = ref({ nome: '', idade: '', genero: 'M', avatar: '' });

onMounted(async () => {
  await carregarDependentes();
});

async function carregarDependentes() {
  try {
    // Token enviado automaticamente pelo interceptor
    const response = await api.get('/api/responsavel/dependentes');
    dependentes.value = response.data;
  } catch (e) { 
    console.error("Erro ao carregar dependentes:", e); 
  } finally { 
    loading.value = false; 
  }
}

function selecionarAluno(aluno) {
  if (modoGerenciamento.value) return; 
  authStore.selecionarCrianca(aluno);
  router.push('/home');
}

function iniciarExclusao(aluno) {
    alunoParaExcluir.value = aluno;
    pinAction.value = 'DELETE_CHILD';
    showPinModal.value = true;
}

function pedirPin(acao) {
  pinAction.value = acao;
  showPinModal.value = true;
}

async function onPinSuccess() {
  showPinModal.value = false;
  
  if (pinAction.value === 'CONFIG') {
    router.push('/responsavel'); 
  } 
  else if (pinAction.value === 'ADD_CHILD') {
    novoAluno.value = { nome: '', idade: '', genero: 'M', avatar: `https://api.dicebear.com/7.x/adventurer/svg?seed=${Date.now()}` };
    showAddChildModal.value = true;
  }
  else if (pinAction.value === 'DELETE_CHILD' && alunoParaExcluir.value) {
      if(confirm(`Tem certeza que deseja excluir o aluno ${alunoParaExcluir.value.nome}? Todo o histórico será apagado.`)) {
          try {
              await api.delete(`/api/responsavel/dependentes/${alunoParaExcluir.value.id}`);
              await carregarDependentes();
              alert("Aluno removido.");
          } catch(e) {
              alert("Erro ao excluir aluno.");
          }
      }
      alunoParaExcluir.value = null;
  }
}

async function salvarNovoAluno() {
  if (!novoAluno.value.nome) return alert("Preencha o nome!");
  
  cadastrando.value = true;

  try {
    const ano = new Date().getFullYear() - novoAluno.value.idade;
    await api.post('/api/responsavel/dependentes', {
      nome: novoAluno.value.nome,
      dataNascimento: `${ano}-01-01`,
      avatarUrl: novoAluno.value.avatar
    });
    
    showAddChildModal.value = false;
    await carregarDependentes();
    alert("Aluno cadastrado com sucesso!");
    
  } catch(e) { 
      alert("Erro ao criar perfil."); 
  } finally {
      cadastrando.value = false;
  }
}

function handleAvatarSelect(url) { 
    novoAluno.value.avatar = url; 
    showAvatarSelector.value = false; 
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] flex flex-col items-center justify-center p-4 font-nunito relative overflow-hidden">
    
    <div class="absolute top-10 left-10 text-4xl animate-float-slow opacity-80 pointer-events-none">✨</div>
    <div class="absolute bottom-20 right-10 text-5xl animate-bounce-slow opacity-80 pointer-events-none">🌈</div>

    <button @click="authStore.logout()" class="absolute top-4 right-4 md:top-6 md:right-6 text-red-400 font-bold hover:text-red-600 bg-white px-4 py-2 rounded-full shadow-sm border border-red-100 flex items-center gap-2 transition-all hover:shadow-md z-20 hover:bg-red-50">
      <span>🚪</span> Sair
    </button>

    <div class="bg-white px-8 py-4 rounded-full shadow-[0_10px_30px_-10px_rgba(167,139,250,0.2)] mb-8 border border-white z-10 text-center max-w-[90%] relative group">
      <h1 class="text-2xl md:text-3xl font-extrabold text-[#A78BFA]" style="font-family: 'Nunito', sans-serif;">Quem vai aprender hoje?</h1>
      
      <button @click="modoGerenciamento = !modoGerenciamento" :class="['absolute -right-12 top-1/2 -translate-y-1/2 p-2 rounded-full transition-all', modoGerenciamento ? 'bg-red-100 text-red-500' : 'text-gray-300 hover:text-[#A78BFA]']" title="Gerenciar Turma">
          {{ modoGerenciamento ? '✕' : '⚙️' }}
      </button>
    </div>

    <div v-if="modoGerenciamento" class="mb-6 bg-red-50 text-red-500 px-4 py-1 rounded-full text-xs font-bold animate-pulse">
        Modo de Edição: Clique na lixeira para excluir
    </div>

    <div v-if="!loading" class="grid grid-cols-2 md:grid-cols-4 gap-6 md:gap-12 z-10 w-full max-w-5xl justify-items-center px-2">
      
      <div @click="pedirPin('CONFIG')" class="flex flex-col items-center group cursor-pointer w-full relative">
        <div class="absolute top-0 right-1/4 md:-right-2 transform translate-x-4 -translate-y-2 bg-white text-gray-400 rounded-full p-2 shadow-md border border-gray-100 z-10 group-hover:text-[#A78BFA] group-hover:rotate-90 transition-all">
          <span class="text-lg">⚙️</span>
        </div>
        
        <div class="w-24 h-24 md:w-32 md:h-32 rounded-full bg-white border-4 border-[#E9D5FF] shadow-lg group-hover:scale-105 group-hover:border-[#A78BFA] transition-all overflow-hidden shrink-0">
           <img :src="authStore.user?.avatarUrl" class="w-full h-full object-cover">
        </div>
        
        <span class="mt-4 font-extrabold text-gray-600 bg-white px-4 py-2 rounded-xl shadow-sm group-hover:text-[#A78BFA] text-center text-sm md:text-base leading-tight break-words w-full max-w-[130px] md:max-w-[180px]">
            {{ authStore.user?.name || 'Professor' }}
        </span>
      </div>

      <div v-for="aluno in dependentes" :key="aluno.id" @click="selecionarAluno(aluno)" :class="['flex flex-col items-center group cursor-pointer w-full relative transition-all', modoGerenciamento ? 'animate-shake' : '']">
        
        <button v-if="modoGerenciamento" @click.stop="iniciarExclusao(aluno)" class="absolute -top-2 -right-2 z-20 bg-red-500 text-white w-8 h-8 rounded-full flex items-center justify-center shadow-lg hover:bg-red-600 transition-colors animate-pop-in">
            🗑️
        </button>

        <div class="w-24 h-24 md:w-32 md:h-32 rounded-full bg-white border-4 border-[#BBF7D0] shadow-lg group-hover:scale-105 group-hover:border-green-400 transition-all overflow-hidden shrink-0 relative">
           <div v-if="modoGerenciamento" class="absolute inset-0 bg-black/10 z-10"></div> <img :src="aluno.avatarUrl" class="w-full h-full object-cover">
        </div>
        <span class="mt-4 font-extrabold text-gray-600 bg-white px-4 py-2 rounded-xl shadow-sm group-hover:text-green-500 text-center text-sm md:text-base leading-tight break-words w-full max-w-[130px] md:max-w-[180px]">
            {{ aluno.nome }}
        </span>
      </div>

      <div @click="pedirPin('ADD_CHILD')" class="flex flex-col items-center group cursor-pointer opacity-70 hover:opacity-100 transition-all w-full">
        <div class="w-24 h-24 md:w-32 md:h-32 rounded-full border-4 border-dashed border-gray-300 flex items-center justify-center bg-[#F9FAFB] group-hover:border-[#A78BFA] group-hover:shadow-md transition-all shrink-0">
           <span class="text-4xl md:text-5xl text-gray-300 group-hover:text-[#A78BFA] pb-1">+</span>
        </div>
        <span class="mt-4 font-bold text-gray-400 group-hover:text-[#A78BFA] text-sm md:text-base text-center">Novo Aluno</span>
      </div>
    </div>

    <PinModal v-if="showPinModal" @close="showPinModal = false" @success="onPinSuccess" />
    
    <div v-if="showAddChildModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
       <div class="bg-white p-8 rounded-[40px] w-full max-w-md shadow-2xl animate-fade-in border-4 border-white">
        <h2 class="text-xl md:text-2xl font-extrabold text-[#A78BFA] mb-6 text-center">Novo Aluno</h2>
        <form @submit.prevent="salvarNovoAluno" class="space-y-4">
          
          <div class="flex justify-center mb-6">
            <div @click="showAvatarSelector = true" class="w-24 h-24 rounded-full border-4 border-gray-100 shadow-inner flex items-center justify-center cursor-pointer overflow-hidden relative group hover:border-[#A78BFA] transition-colors bg-gray-50">
              <img v-if="novoAluno.avatar" :src="novoAluno.avatar" class="w-full h-full object-cover">
              <span v-else class="text-gray-400 text-xs font-bold group-hover:text-[#A78BFA]">Escolher<br>Foto</span>
            </div>
          </div>

          <input v-model="novoAluno.nome" required placeholder="Nome do Aluno" class="w-full px-6 py-3 rounded-[20px] bg-[#F9FAFB] border-2 border-transparent focus:bg-white focus:border-[#A78BFA] font-bold outline-none text-gray-600 placeholder-gray-300 shadow-inner transition-all">
          
          <div class="grid grid-cols-2 gap-4">
            <input type="number" v-model="novoAluno.idade" required placeholder="Idade" class="w-full px-6 py-3 rounded-[20px] bg-[#F9FAFB] border-2 border-transparent focus:bg-white focus:border-[#A78BFA] font-bold outline-none text-gray-600 placeholder-gray-300 shadow-inner transition-all">
            <select v-model="novoAluno.genero" class="w-full px-6 py-3 rounded-[20px] bg-[#F9FAFB] border-2 border-transparent focus:bg-white focus:border-[#A78BFA] font-bold outline-none text-gray-600 shadow-inner transition-all cursor-pointer">
                <option value="M">Menino</option>
                <option value="F">Menina</option>
            </select>
          </div>

          <div class="flex gap-3 pt-4">
            <button type="button" @click="showAddChildModal = false" class="flex-1 py-3 font-bold text-gray-400 bg-gray-100 rounded-[20px] hover:bg-gray-200 transition-colors">Cancelar</button>
            <button type="submit" :disabled="cadastrando" class="flex-1 py-3 font-bold text-white bg-gradient-to-r from-[#C4B5FD] to-[#A78BFA] hover:to-[#8B5CF6] rounded-[20px] shadow-lg transition-transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed">
                {{ cadastrando ? 'Criando...' : 'Criar' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <AvatarSelectorModal v-if="showAvatarSelector" @close="showAvatarSelector = false" @select="handleAvatarSelect" />
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
.animate-bounce-slow { animation: floatSlow 3s ease-in-out infinite; }
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
.animate-pop-in { animation: popIn 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@keyframes popIn { from { transform: scale(0); } to { transform: scale(1); } }

@keyframes shake {
  0% { transform: rotate(0deg); }
  25% { transform: rotate(1deg); }
  50% { transform: rotate(0deg); }
  75% { transform: rotate(-1deg); }
  100% { transform: rotate(0deg); }
}
.animate-shake {
  animation: shake 0.3s infinite ease-in-out;
}
</style>