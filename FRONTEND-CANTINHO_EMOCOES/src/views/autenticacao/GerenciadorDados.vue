<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/services/api';
import { 
  Users, BookOpen, BarChart2, LogOut, Database, 
  Search, Plus, Edit2, Trash2, X, Save, GraduationCap
} from 'lucide-vue-next';

const router = useRouter();
const authStore = useAuthStore();
const activeTab = ref('usuarios');
const loading = ref(false);
const termoPesquisa = ref('');

const listaPais = ref([]);
const listaAlunos = ref([]);

const dados = ref([]);
const modalAberto = ref(false);
const itemEmEdicao = ref({});

const configAbas = {
  usuarios:   { titulo: 'Usuários (Pais/Admins)', url: '/api/admin/usuarios', icone: Users },
  alunos:     { titulo: 'Alunos (Dependentes)',   url: '/api/admin/alunos',   icone: GraduationCap },
  atividades: { titulo: 'Jogos Realizados',       url: '/api/admin/atividades', icone: BookOpen },
  diarios:    { titulo: 'Registros de Diário',    url: '/api/admin/diarios',    icone: Database }
};

onMounted(async () => {
  await carregarDados();
  carregarListasAuxiliares();
});

watch(activeTab, () => {
  termoPesquisa.value = '';
  dados.value = [];
  carregarDados();
});

async function carregarDados() {
  loading.value = true;
  try {
    const url = configAbas[activeTab.value].url;
    const res = await api.get(url);
    dados.value = res.data;
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
}

async function carregarListasAuxiliares() {
    try {
        const [resPais, resAlunos] = await Promise.all([
            api.get('/api/admin/usuarios'),
            api.get('/api/admin/alunos')
        ]);
        listaPais.value = resPais.data;
        listaAlunos.value = resAlunos.data;
    } catch (e) {
        console.error("Erro ao carregar listas auxiliares", e);
    }
}

const dadosFiltrados = computed(() => {
  if (!termoPesquisa.value) return dados.value;
  const termo = termoPesquisa.value.toLowerCase();
  return dados.value.filter(item => JSON.stringify(item).toLowerCase().includes(termo));
});

function abrirModalNovo() {
  itemEmEdicao.value = {};
  
  if (activeTab.value === 'alunos') {
      itemEmEdicao.value = { responsavel: { id: '' } };
  } else if (activeTab.value === 'atividades') {
      itemEmEdicao.value = { aluno: { id: '' }, tipo: 'VOGAL', dataRealizacao: new Date().toISOString().slice(0, 16) };
  } else if (activeTab.value === 'diarios') {
      itemEmEdicao.value = { emocao: 'FELIZ', intensidade: 3, dataRegistro: new Date().toISOString().slice(0, 16) };
  }
  
  modalAberto.value = true;
}

function abrirModalEditar(item) {
  itemEmEdicao.value = JSON.parse(JSON.stringify(item));
  
  if (activeTab.value === 'alunos' && !itemEmEdicao.value.responsavel) {
      itemEmEdicao.value.responsavel = { id: '' };
  }
  if (activeTab.value === 'atividades') {
      if (!itemEmEdicao.value.aluno) itemEmEdicao.value.aluno = { id: '' };
      // Ajuste data HTML
      if (itemEmEdicao.value.dataRealizacao) itemEmEdicao.value.dataRealizacao = itemEmEdicao.value.dataRealizacao.slice(0, 16);
  }
  if (activeTab.value === 'diarios' && itemEmEdicao.value.dataRegistro) {
      itemEmEdicao.value.dataRegistro = itemEmEdicao.value.dataRegistro.slice(0, 16);
  }
  
  modalAberto.value = true;
}

async function salvarItem() {
  try {
    const url = configAbas[activeTab.value].url;
    await api.post(url, itemEmEdicao.value);
    alert('Salvo com sucesso!');
    modalAberto.value = false;
    await carregarDados();
  } catch (error) {
    alert('Erro ao salvar: ' + (error.response?.data?.error || error.message));
  }
}

async function excluirItem(id) {
  if (!confirm('Tem certeza que deseja excluir?')) return;
  try {
    let urlDelecao = configAbas[activeTab.value].url + '/' + id;
    await api.delete(urlDelecao);
    dados.value = dados.value.filter(i => i.id !== id);
    alert('Excluído!');
  } catch (error) {
    alert('Erro ao excluir.');
  }
}

function formatarData(data) {
    if (!data) return '-';
    // Se vier array [ano, mes, dia...]
    if (Array.isArray(data)) {
        return new Date(data[0], data[1]-1, data[2], data[3]||0, data[4]||0).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
    }
    return new Date(data).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

function logout() {
  if (authStore.clearLoginData) authStore.clearLoginData();
  else authStore.logout();
  router.push('/login');
}
</script>

<template>
  <div class="min-h-screen bg-[#F0F7FF] flex font-nunito relative overflow-hidden">
    
    <div class="absolute top-10 left-60 text-4xl animate-float-slow opacity-30 pointer-events-none z-0">⚙️</div>
    <div class="absolute bottom-10 right-10 text-5xl animate-bounce-slow opacity-30 pointer-events-none z-0">📂</div>

    <aside class="w-20 md:w-64 bg-white m-4 rounded-[30px] shadow-sm border border-indigo-50 flex flex-col z-20 transition-all duration-300">
      <div class="p-6 flex flex-col items-center md:items-start">
        <h2 class="text-2xl font-black text-[#4F46E5] hidden md:block tracking-tight">Admin<span class="text-orange-400">Dados</span></h2>
      </div>
      
      <nav class="flex-1 px-4 space-y-3 mt-4">
        <button @click="router.push('/admin')" class="w-full flex items-center justify-center md:justify-start gap-3 px-4 py-3 rounded-[20px] transition-all font-bold text-gray-400 hover:bg-gray-50 hover:text-indigo-600">
           <BarChart2 size="22" /> <span class="hidden md:block">Voltar ao Painel</span>
        </button>
        <div class="h-px bg-gray-100 my-2"></div>
        
        <button v-for="(cfg, key) in configAbas" :key="key"
          @click="activeTab = key" 
          :class="['w-full flex items-center justify-center md:justify-start gap-3 px-4 py-3 rounded-[20px] transition-all font-bold', 
            activeTab === key ? 'bg-indigo-50 text-indigo-600 shadow-sm' : 'text-gray-400 hover:bg-gray-50']">
          <component :is="cfg.icone" size="22" /> <span class="hidden md:block">{{ cfg.titulo.split(' ')[0] }}</span>
        </button>
      </nav>

      <div class="p-4 border-t border-gray-100 mb-2">
        <button @click="logout" class="w-full flex items-center justify-center md:justify-start gap-2 px-4 py-3 text-red-400 hover:bg-red-50 hover:text-red-500 rounded-[20px] transition-colors font-bold">
          <LogOut size="20" /> <span class="hidden md:block">Sair</span>
        </button>
      </div>
    </aside>

    <main class="flex-1 p-4 md:p-8 overflow-y-auto z-10 h-screen">
      
      <header class="bg-white rounded-[30px] p-6 shadow-sm border-2 border-white mb-8 flex flex-col md:flex-row justify-between items-center gap-4">
        <div>
          <h1 class="text-2xl font-black text-gray-700">{{ configAbas[activeTab].titulo }}</h1>
          <p class="text-sm text-gray-400 font-bold">Gerenciamento completo do sistema</p>
        </div>
        <button @click="abrirModalNovo" class="px-6 py-3 bg-indigo-600 text-white rounded-[15px] font-bold hover:bg-indigo-700 flex items-center gap-2 shadow-lg shadow-indigo-200 transition-all">
            <Plus size="20" /> Novo Item
        </button>
      </header>

      <div class="bg-white rounded-[30px] shadow-sm border-2 border-white overflow-hidden min-h-[500px] relative">
        <div class="p-6 border-b border-gray-100 bg-gray-50/50 flex justify-between items-center">
            <div class="relative w-full md:w-1/3">
              <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size="20" />
              <input v-model="termoPesquisa" type="text" placeholder="Pesquisar..." class="w-full pl-12 pr-4 py-3 bg-white rounded-[20px] border-2 border-transparent focus:border-indigo-200 outline-none font-bold text-gray-600 shadow-sm">
            </div>
            <span class="text-sm font-bold text-gray-400">{{ dadosFiltrados.length }} registros</span>
        </div>

        <div class="overflow-x-auto">
           <table class="w-full text-left border-collapse">
              <thead class="bg-[#F0F7FF] text-xs uppercase text-gray-400 font-extrabold tracking-wider">
                <tr>
                   <th class="p-6">ID</th>
                   <th v-if="['usuarios','alunos'].includes(activeTab)" class="p-6">Nome</th>
                   <th v-if="activeTab === 'usuarios'" class="p-6">Email / Perfil</th>
                   <th v-if="activeTab === 'alunos'" class="p-6">Responsável</th>
                   
                   <th v-if="activeTab === 'atividades'" class="p-6">Tipo / Data</th>
                   <th v-if="activeTab === 'atividades'" class="p-6">Conteúdo</th>
                   <th v-if="activeTab === 'atividades'" class="p-6">Aluno</th>

                   <th v-if="activeTab === 'diarios'" class="p-6">Aluno</th>
                   <th v-if="activeTab === 'diarios'" class="p-6">Emoção / Data</th>
                   <th v-if="activeTab === 'diarios'" class="p-6">Relato</th>
                   
                   <th class="p-6 text-right">Ações</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                 <tr v-for="item in dadosFiltrados" :key="item.id" class="hover:bg-[#F9FAFB] transition-colors">
                    <td class="p-6 font-bold text-gray-400">#{{ item.id }}</td>
                    
                    <td v-if="['usuarios','alunos'].includes(activeTab)" class="p-6 font-bold text-gray-700">{{ item.nome }}</td>
                    
                    <td v-if="activeTab === 'usuarios'" class="p-6">
                        <div class="text-sm text-gray-600">{{ item.email }}</div>
                        <span class="text-[10px] bg-indigo-50 text-indigo-600 px-2 py-1 rounded-md font-bold">{{ item.perfil }}</span>
                    </td>

                    <td v-if="activeTab === 'alunos'" class="p-6">
                        <span class="text-xs bg-purple-50 text-purple-600 px-2 py-1 rounded-md font-bold">
                            {{ item.nomeResponsavel || (item.responsavel ? item.responsavel.nome : 'Sem Responsável') }}
                        </span>
                    </td>

                    <td v-if="activeTab === 'atividades'" class="p-6">
                        <span class="block font-bold text-indigo-600">{{ item.tipo }}</span>
                        <span class="text-xs text-gray-400">{{ formatarData(item.dataRealizacao) }}</span>
                    </td>
                    <td v-if="activeTab === 'atividades'" class="p-6 font-bold text-gray-700">{{ item.conteudo }}</td>
                    <td v-if="activeTab === 'atividades'" class="p-6">
                         <span class="text-xs bg-green-50 text-green-600 px-2 py-1 rounded-md font-bold">
                            {{ item.alunoNome || (item.aluno ? item.aluno.nome : 'Geral') }}
                         </span>
                    </td>

                    <td v-if="activeTab === 'diarios'" class="p-6 font-bold text-indigo-600">{{ item.alunoNome || (item.dependente ? item.dependente.nome : 'Unknown') }}</td>
                    <td v-if="activeTab === 'diarios'" class="p-6">
                        <div class="flex flex-col gap-1">
                            <span class="text-xs bg-blue-50 text-blue-600 px-2 py-1 rounded-md font-bold w-max">{{ item.emocao }} ({{ item.intensidade }})</span>
                            <span class="text-xs text-gray-400 font-bold">{{ formatarData(item.dataRegistro) }}</span>
                        </div>
                    </td>
                    <td v-if="activeTab === 'diarios'" class="p-6 text-sm text-gray-600 italic truncate max-w-[200px]">"{{ item.relato || item.texto }}"</td>

                    <td class="p-6 text-right flex justify-end gap-2">
                       <button @click="abrirModalEditar(item)" class="p-2 text-indigo-400 hover:bg-indigo-50 rounded-lg transition-colors"><Edit2 size="18"/></button>
                       <button @click="excluirItem(item.id)" class="p-2 text-red-400 hover:bg-red-50 rounded-lg transition-colors"><Trash2 size="18"/></button>
                    </td>
                 </tr>
              </tbody>
           </table>
        </div>
      </div>
    </main>

    <div v-if="modalAberto" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4 animate-fade-in">
       <div class="bg-white rounded-[30px] shadow-2xl w-full max-w-lg overflow-hidden">
          <div class="bg-indigo-600 p-6 flex justify-between items-center text-white">
             <h3 class="font-black text-xl">{{ itemEmEdicao.id ? 'Editar' : 'Novo' }} Registro</h3>
             <button @click="modalAberto = false" class="hover:bg-white/20 p-2 rounded-full"><X size="20"/></button>
          </div>
          
          <div class="p-8 space-y-4 max-h-[70vh] overflow-y-auto">
             
             <template v-if="activeTab === 'usuarios'">
                <div><label class="label">Nome</label><input v-model="itemEmEdicao.nome" class="input"></div>
                <div><label class="label">Email</label><input v-model="itemEmEdicao.email" class="input"></div>
                <div><label class="label">Senha (vazio = manter)</label><input v-model="itemEmEdicao.senha" type="password" class="input"></div>
                <div><label class="label">Perfil</label>
                   <select v-model="itemEmEdicao.perfil" class="input">
                      <option value="RESPONSAVEL">RESPONSAVEL</option>
                      <option value="ADMINISTRADOR">ADMINISTRADOR</option>
                   </select>
                </div>
             </template>

             <template v-if="activeTab === 'alunos'">
                <div><label class="label">Nome do Aluno</label><input v-model="itemEmEdicao.nome" class="input"></div>
                <div><label class="label">Responsável (Pai/Mãe)</label>
                   <select v-model="itemEmEdicao.responsavel.id" class="input">
                      <option disabled value="">Selecione um Responsável...</option>
                      <option v-for="pai in listaPais" :key="pai.id" :value="pai.id">{{ pai.nome }} ({{ pai.email }})</option>
                   </select>
                </div>
             </template>

             <template v-if="activeTab === 'atividades'">
                <div>
                    <label class="label">Tipo de Jogo</label>
                    <select v-model="itemEmEdicao.tipo" class="input">
                        <option value="VOGAL">VOGAL</option>
                        <option value="NUMERO">NUMERO</option>
                        <option value="ALFABETO">ALFABETO</option>
                        <option value="OUTRO">OUTRO</option>
                    </select>
                </div>
                <div><label class="label">Conteúdo (ex: "A", "1")</label><input v-model="itemEmEdicao.conteudo" class="input"></div>
                <div><label class="label">Data Realização</label><input v-model="itemEmEdicao.dataRealizacao" type="datetime-local" class="input"></div>
                <div><label class="label">Vincular a Aluno</label>
                   <select v-model="itemEmEdicao.aluno.id" class="input">
                      <option value="">-- Geral --</option>
                      <option v-for="aluno in listaAlunos" :key="aluno.id" :value="aluno.id">{{ aluno.nome }}</option>
                   </select>
                </div>
             </template>

             <template v-if="activeTab === 'diarios'">
                <div>
                   <label class="label">Emoção</label>
                   <select v-model="itemEmEdicao.emocao" class="input">
                      <option value="FELIZ">FELIZ</option>
                      <option value="TRISTE">TRISTE</option>
                      <option value="BRAVO">BRAVO</option>
                      <option value="MEDO">MEDO</option>
                      <option value="NOJINHO">NOJINHO</option>
                      <option value="CALMO">CALMO</option>
                   </select>
                </div>
                <div>
                   <label class="label">Intensidade (1 a 5)</label>
                   <input v-model="itemEmEdicao.intensidade" type="number" min="1" max="5" class="input">
                </div>
                <div>
                   <label class="label">Data e Hora</label>
                   <input v-model="itemEmEdicao.dataRegistro" type="datetime-local" class="input">
                </div>
                <div>
                    <label class="label">Relato</label>
                    <textarea v-model="itemEmEdicao.relato" rows="5" class="input"></textarea>
                </div>
             </template>

          </div>

          <div class="p-6 border-t border-gray-100 flex justify-end gap-3 bg-gray-50">
             <button @click="modalAberto = false" class="btn-secondary">Cancelar</button>
             <button @click="salvarItem" class="btn-primary"><Save size="18" /> Salvar</button>
          </div>
       </div>
    </div>
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
.label { @apply block text-sm font-bold text-gray-500 mb-1; }
.input { @apply w-full p-3 bg-gray-50 rounded-xl border border-gray-200 outline-none focus:border-indigo-500 font-bold text-gray-700; }
.btn-primary { @apply px-6 py-3 bg-indigo-600 text-white rounded-[15px] font-bold hover:bg-indigo-700 shadow-lg shadow-indigo-200 flex items-center gap-2; }
.btn-secondary { @apply px-6 py-3 rounded-[15px] font-bold text-gray-500 hover:bg-gray-200 transition-colors; }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
.animate-bounce-slow { animation: floatSlow 3s ease-in-out infinite; animation-delay: 1.5s; }
.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
::-webkit-scrollbar { width: 8px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #E0E7FF; border-radius: 10px; }
</style>