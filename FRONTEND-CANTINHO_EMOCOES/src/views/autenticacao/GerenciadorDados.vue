<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/services/api';
import { 
  Users, GraduationCap, LogOut, Search, Plus, Edit2, Trash2, X, Save, 
  ArrowLeft, BookOpen, Database, Calendar, Filter, Eraser
} from 'lucide-vue-next';

const router = useRouter();
const authStore = useAuthStore();

// --- ESTADO GERAL ---
const loading = ref(false);
const termoPesquisa = ref('');
const listaPais = ref([]); 
const dados = ref([]); 

// Controle de Navegação
const viewMode = ref('lista'); 
const activeTab = ref('usuarios'); 
const subTab = ref('atividades'); 

// Filtros do Histórico (NOVO)
const filtrosHist = ref({
    dataInicio: '',
    dataFim: '',
    tipo: '',
    conteudo: ''
});

// Aluno selecionado para visualização de histórico
const alunoSelecionado = ref(null); 
const historicoJogos = ref([]);
const historicoDiarios = ref([]);

// Controle de Modais
const modalFormAberto = ref(false);
const itemEmEdicao = ref({});

// Listas para Selects de Filtro
const tiposAtividade = ['VOGAL', 'CONSOANTE', 'NUMERO', 'FORMA', 'EMOCAO', 'FRUTA', 'LIVRE', 'OUTRO'];
const tiposEmocao = ['FELIZ', 'TRISTE', 'BRAVO', 'MEDO', 'ANSIOSO', 'CALMO', 'NOJINHO'];

// Configuração das Abas Principais
const configAbas = {
  usuarios: { titulo: 'Usuários do Sistema', url: '/api/admin/usuarios', icone: Users },
  alunos:   { titulo: 'Gerenciar Alunos',    url: '/api/admin/alunos',   icone: GraduationCap }
};

onMounted(async () => {
  await carregarDadosPrincipais();
  carregarListaPais();
});

watch(activeTab, () => {
  viewMode.value = 'lista';
  termoPesquisa.value = '';
  dados.value = [];
  carregarDadosPrincipais();
});

// Limpa filtros ao trocar de aba no histórico
watch(subTab, () => {
    limparFiltrosHistorico();
});

// --- CARREGAMENTO DE DADOS ---

async function carregarDadosPrincipais() {
  loading.value = true;
  try {
    const url = configAbas[activeTab.value].url;
    const res = await api.get(url);
    dados.value = res.data;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function carregarListaPais() {
    try {
        const res = await api.get('/api/admin/usuarios');
        listaPais.value = res.data; // Aqui vêm os professores/responsáveis
    } catch (e) {}
}

const dadosFiltrados = computed(() => {
  if (!termoPesquisa.value) return dados.value;
  const termo = termoPesquisa.value.toLowerCase();
  return dados.value.filter(item => JSON.stringify(item).toLowerCase().includes(termo));
});

// --- FILTROS DE HISTÓRICO (COMPUTEDS) ---

const historicoJogosFiltrados = computed(() => {
    return historicoJogos.value.filter(item => {
        const dataItem = parseDataSimples(item.dataRealizacao);
        const dataIni = filtrosHist.value.dataInicio ? new Date(filtrosHist.value.dataInicio + 'T00:00:00') : null;
        const dataFim = filtrosHist.value.dataFim ? new Date(filtrosHist.value.dataFim + 'T23:59:59') : null;

        // Filtro Data
        if (dataIni && dataItem < dataIni) return false;
        if (dataFim && dataItem > dataFim) return false;

        // Filtro Tipo
        if (filtrosHist.value.tipo && item.tipo !== filtrosHist.value.tipo) return false;

        // Filtro Conteúdo
        if (filtrosHist.value.conteudo && !String(item.conteudo).toLowerCase().includes(filtrosHist.value.conteudo.toLowerCase())) return false;

        return true;
    });
});

const historicoDiariosFiltrados = computed(() => {
    return historicoDiarios.value.filter(item => {
        const dataItem = parseDataSimples(item.dataRegistro);
        const dataIni = filtrosHist.value.dataInicio ? new Date(filtrosHist.value.dataInicio + 'T00:00:00') : null;
        const dataFim = filtrosHist.value.dataFim ? new Date(filtrosHist.value.dataFim + 'T23:59:59') : null;

        // Filtro Data
        if (dataIni && dataItem < dataIni) return false;
        if (dataFim && dataItem > dataFim) return false;

        // Filtro Emoção (usa o campo 'tipo' do filtro visual)
        if (filtrosHist.value.tipo && item.emocao !== filtrosHist.value.tipo) return false;

        // Filtro Relato (usa o campo 'conteudo' do filtro visual)
        if (filtrosHist.value.conteudo && !String(item.relato).toLowerCase().includes(filtrosHist.value.conteudo.toLowerCase())) return false;

        return true;
    });
});

function limparFiltrosHistorico() {
    filtrosHist.value = { dataInicio: '', dataFim: '', tipo: '', conteudo: '' };
}

// Helper interno para comparação de datas no filtro
function parseDataSimples(data) {
    if (Array.isArray(data)) return new Date(data[0], data[1]-1, data[2], data[3]||0, data[4]||0);
    return new Date(data);
}

// --- LÓGICA DO HISTÓRICO DO ALUNO ---

async function abrirHistoricoAluno(aluno) {
    alunoSelecionado.value = aluno;
    viewMode.value = 'historico';
    subTab.value = 'atividades';
    limparFiltrosHistorico();
    await carregarHistorico();
}

async function carregarHistorico() {
    loading.value = true;
    try {
        const [resAtiv, resDiarios] = await Promise.all([
            api.get('/api/admin/atividades'),
            api.get('/api/admin/diarios')
        ]);
        historicoJogos.value = resAtiv.data.filter(a => a.alunoId === alunoSelecionado.value.id || (a.aluno && a.aluno.id === alunoSelecionado.value.id));
        historicoDiarios.value = resDiarios.data.filter(d => d.alunoId === alunoSelecionado.value.id || (d.dependente && d.dependente.id === alunoSelecionado.value.id));
    } catch (e) {
        console.error("Erro ao carregar histórico", e);
    } finally {
        loading.value = false;
    }
}

function voltarParaLista() {
    viewMode.value = 'lista';
    alunoSelecionado.value = null;
}

// --- FUNÇÕES DE FORMATAÇÃO E DEBUG DE DATA ---

function formatarData(data) {
    if (!data) return '-';
    try {
        if (Array.isArray(data)) {
            return new Date(data[0], data[1]-1, data[2], data[3]||0, data[4]||0).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
        }
        return new Date(data).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
    } catch (e) { return data; }
}

function formatarParaInput(data) {
    if (!data) return '';
    if (Array.isArray(data)) {
        const y = data[0];
        const m = String(data[1]).padStart(2, '0');
        const d = String(data[2]).padStart(2, '0');
        const h = String(data[3] || 0).padStart(2, '0');
        const min = String(data[4] || 0).padStart(2, '0');
        return `${y}-${m}-${d}T${h}:${min}`;
    }
    if (typeof data === 'string') {
        return data.slice(0, 16);
    }
    return '';
}

// --- MODAIS ---

function abrirModalNovoPrincipal() {
    itemEmEdicao.value = {};
    if (activeTab.value === 'alunos') {
        itemEmEdicao.value = { responsavel: { id: '' } };
    }
    modalFormAberto.value = true;
}

function abrirModalEditarPrincipal(item) {
    itemEmEdicao.value = JSON.parse(JSON.stringify(item));
    if (activeTab.value === 'alunos' && !itemEmEdicao.value.responsavel) {
        itemEmEdicao.value.responsavel = { id: '' };
    }
    modalFormAberto.value = true;
}

function abrirModalNovoHistorico() {
    itemEmEdicao.value = { 
        aluno: { id: alunoSelecionado.value.id }, 
        dependente: { id: alunoSelecionado.value.id } 
    };

    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    const dataLocal = now.toISOString().slice(0, 16);

    if (subTab.value === 'atividades') {
        itemEmEdicao.value.tipo = 'VOGAL';
        itemEmEdicao.value.dataRealizacao = dataLocal;
    } else {
        itemEmEdicao.value.emocao = 'FELIZ';
        itemEmEdicao.value.intensidade = 3;
        itemEmEdicao.value.dataRegistro = dataLocal;
    }
    modalFormAberto.value = true;
}

function abrirModalEditarHistorico(item) {
    itemEmEdicao.value = JSON.parse(JSON.stringify(item));
    
    if (subTab.value === 'atividades') {
        itemEmEdicao.value.dataRealizacao = formatarParaInput(itemEmEdicao.value.dataRealizacao);
    } else {
        itemEmEdicao.value.dataRegistro = formatarParaInput(itemEmEdicao.value.dataRegistro);
    }

    if (!itemEmEdicao.value.aluno) itemEmEdicao.value.aluno = { id: alunoSelecionado.value.id };
    if (!itemEmEdicao.value.dependente) itemEmEdicao.value.dependente = { id: alunoSelecionado.value.id };
    
    modalFormAberto.value = true;
}

async function salvar() {
    try {
        let url = '';
        let payload = { ...itemEmEdicao.value };

        if (payload.dataRealizacao && payload.dataRealizacao.length === 16) payload.dataRealizacao += ':00';
        if (payload.dataRegistro && payload.dataRegistro.length === 16) payload.dataRegistro += ':00';

        if (viewMode.value === 'lista') {
            url = configAbas[activeTab.value].url;
        } else {
            url = subTab.value === 'atividades' ? '/api/admin/atividades' : '/api/admin/diarios';
            if (subTab.value === 'atividades') {
                payload.aluno = { id: alunoSelecionado.value.id };
            } else {
                payload.id = itemEmEdicao.value.id; 
            }
        }

        await api.post(url, payload);
        alert('Salvo com sucesso!');
        modalFormAberto.value = false;

        if (viewMode.value === 'lista') await carregarDadosPrincipais();
        else await carregarHistorico();

    } catch (e) {
        alert('Erro ao salvar: ' + (e.response?.data?.error || e.message));
    }
}

async function excluir(id) {
    if (!confirm('Tem certeza?')) return;
    try {
        let url = '';
        if (viewMode.value === 'lista') {
            url = configAbas[activeTab.value].url + '/' + id;
        } else {
            url = (subTab.value === 'atividades' ? '/api/admin/atividades/' : '/api/admin/diarios/') + id;
        }
        await api.delete(url);
        
        if (viewMode.value === 'lista') {
            dados.value = dados.value.filter(i => i.id !== id);
        } else {
            if (subTab.value === 'atividades') historicoJogos.value = historicoJogos.value.filter(i => i.id !== id);
            else historicoDiarios.value = historicoDiarios.value.filter(i => i.id !== id);
        }
    } catch (e) {
        alert('Erro ao excluir.');
    }
}

function logout() {
  authStore.logout();
  router.push('/login');
}
</script>

<template>
  <div class="min-h-screen bg-[#F0F7FF] flex font-nunito relative overflow-hidden">
    
    <div class="absolute top-10 left-60 text-4xl opacity-30 pointer-events-none">⚙️</div>
    <div class="absolute bottom-10 right-10 text-5xl opacity-30 pointer-events-none">📂</div>

    <aside class="w-16 md:w-64 bg-white m-2 md:m-4 rounded-[20px] md:rounded-[30px] shadow-sm border border-indigo-50 flex flex-col z-20 transition-all duration-300">
      <div class="p-4 md:p-6 flex flex-col items-center md:items-start">
        <h2 class="text-2xl font-black text-[#4F46E5] hidden md:block">Admin<span class="text-orange-400">Dados</span></h2>
        <span class="md:hidden text-2xl font-bold text-indigo-600">AD</span>
      </div>
      
      <nav class="flex-1 px-2 md:px-4 space-y-2 mt-4">
        <button v-if="viewMode === 'historico'" @click="voltarParaLista" class="w-full flex items-center justify-center md:justify-start gap-3 px-2 md:px-4 py-3 rounded-[20px] bg-orange-50 text-orange-600 font-bold mb-4 shadow-sm hover:bg-orange-100 transition-colors">
            <ArrowLeft size="20" /> <span class="hidden md:block">Voltar p/ Lista</span>
        </button>

        <template v-if="viewMode === 'lista'">
            <button v-for="(cfg, key) in configAbas" :key="key"
              @click="activeTab = key" 
              :class="['w-full flex items-center justify-center md:justify-start gap-3 px-2 md:px-4 py-3 rounded-[20px] font-bold transition-all', 
               activeTab === key ? 'bg-indigo-50 text-indigo-600 shadow-sm' : 'text-gray-400 hover:bg-gray-50']">
              <component :is="cfg.icone" size="20" /> <span class="hidden md:block">{{ cfg.titulo }}</span>
            </button>
        </template>

        <template v-if="viewMode === 'historico'">
            <div class="hidden md:block px-4 py-2 text-xs font-black text-gray-300 uppercase">Visão do Aluno</div>
            
            <button @click="subTab = 'atividades'" 
                :class="['w-full flex items-center justify-center md:justify-start gap-3 px-2 md:px-4 py-3 rounded-[20px] font-bold transition-all', subTab === 'atividades' ? 'bg-green-50 text-green-600' : 'text-gray-400']">
                <BookOpen size="20"/> <span class="hidden md:block">Jogos/Ativ.</span>
            </button>
            <button @click="subTab = 'diarios'" 
                :class="['w-full flex items-center justify-center md:justify-start gap-3 px-2 md:px-4 py-3 rounded-[20px] font-bold transition-all', subTab === 'diarios' ? 'bg-blue-50 text-blue-600' : 'text-gray-400']">
                <Database size="20"/> <span class="hidden md:block">Diários</span>
            </button>
        </template>
      </nav>

      <div class="p-2 md:p-4 border-t border-gray-100">
        <button @click="logout" class="w-full flex items-center justify-center md:justify-start gap-2 px-2 md:px-4 py-3 text-red-400 hover:bg-red-50 rounded-[20px] font-bold">
          <LogOut size="20" /> <span class="hidden md:block">Sair</span>
        </button>
      </div>
    </aside>

    <main class="flex-1 p-3 md:p-8 overflow-y-auto z-10 h-screen">
      
      <header class="bg-white rounded-[20px] md:rounded-[30px] p-4 md:p-6 shadow-sm border-2 border-white mb-6 md:mb-8 flex flex-col md:flex-row justify-between items-center gap-4">
        <div class="text-center md:text-left">
          <h1 class="text-xl md:text-2xl font-black text-gray-700">
              <span v-if="viewMode === 'lista'">{{ configAbas[activeTab].titulo }}</span>
              <span v-else>Histórico de <span class="text-indigo-600">{{ alunoSelecionado?.nome }}</span></span>
          </h1>
          <p class="text-xs md:text-sm text-gray-400 font-bold">
              {{ viewMode === 'lista' ? 'Gerenciamento geral' : 'Visualizando registros individuais' }}
          </p>
        </div>
        
        <button v-if="viewMode === 'lista'" @click="abrirModalNovoPrincipal" class="btn-primary w-full md:w-auto justify-center">
            <Plus size="20" /> Novo {{ activeTab === 'usuarios' ? 'Usuário' : 'Aluno' }}
        </button>
        <button v-else @click="abrirModalNovoHistorico" class="btn-primary w-full md:w-auto justify-center">
            <Plus size="20" /> Novo Registro
        </button>
      </header>

      <div v-if="viewMode === 'lista'" class="bg-white rounded-[20px] md:rounded-[30px] shadow-sm border-2 border-white overflow-hidden min-h-[500px]">
        <div class="p-4 md:p-6 border-b border-gray-100 flex items-center gap-4">
            <div class="relative flex-1 max-w-md w-full">
              <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size="20" />
              <input v-model="termoPesquisa" type="text" placeholder="Pesquisar por nome ou email..." class="w-full pl-12 pr-4 py-3 bg-gray-50 rounded-[20px] outline-none font-bold text-gray-600 text-sm md:text-base">
            </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left min-w-[600px]">
             <thead class="bg-[#F0F7FF] text-xs uppercase text-gray-400 font-extrabold">
               <tr>
                  <th class="p-3 md:p-6">Nome</th>
                  <th class="p-3 md:p-6">{{ activeTab === 'usuarios' ? 'Email / Perfil' : 'Professor(a) / Responsável' }}</th>
                  <th class="p-3 md:p-6 text-right">Ações</th>
               </tr>
             </thead>
             <tbody class="divide-y divide-gray-50">
                <tr v-for="item in dadosFiltrados" :key="item.id" class="hover:bg-[#F9FAFB]">
                   <td class="p-3 md:p-6 font-bold text-gray-700 text-sm md:text-base">{{ item.nome }}</td>
                   
                   <td v-if="activeTab === 'usuarios'" class="p-3 md:p-6">
                       <div class="text-xs md:text-sm text-gray-600">{{ item.email }}</div>
                       <span class="badge-indigo">{{ item.perfil }}</span>
                   </td>
                   <td v-else class="p-3 md:p-6">
                       <span class="badge-purple">{{ item.nomeResponsavel || (item.responsavel ? item.responsavel.nome : 'Sem professor') }}</span>
                   </td>

                   <td class="p-3 md:p-6 text-right flex justify-end gap-2">
                      <button v-if="activeTab === 'alunos'" @click="abrirHistoricoAluno(item)" class="px-2 md:px-3 py-2 bg-orange-100 text-orange-600 rounded-lg font-bold text-xs md:text-sm hover:bg-orange-200 flex items-center gap-2">
                          <Calendar size="16"/> <span class="hidden md:inline">Histórico</span>
                      </button>

                      <button @click="abrirModalEditarPrincipal(item)" class="icon-btn text-indigo-400"><Edit2 size="18"/></button>
                      <button @click="excluir(item.id)" class="icon-btn text-red-400"><Trash2 size="18"/></button>
                   </td>
                </tr>
             </tbody>
          </table>
        </div>
      </div>

      <div v-else class="bg-white rounded-[20px] md:rounded-[30px] shadow-sm border-2 border-white overflow-hidden min-h-[500px]">
         
         <div class="p-4 bg-gray-50 border-b border-gray-200 flex flex-wrap items-center gap-4">
             <div class="flex items-center gap-2">
                 <Filter size="16" class="text-gray-400"/>
                 <span class="text-xs font-bold text-gray-500 uppercase">Filtros:</span>
             </div>
             
             <div class="flex items-center gap-2">
                 <span class="text-xs font-bold text-gray-400">De:</span>
                 <input v-model="filtrosHist.dataInicio" type="date" class="input-filter">
             </div>
             <div class="flex items-center gap-2">
                 <span class="text-xs font-bold text-gray-400">Até:</span>
                 <input v-model="filtrosHist.dataFim" type="date" class="input-filter">
             </div>

             <div class="flex items-center gap-2">
                 <span class="text-xs font-bold text-gray-400">Tipo:</span>
                 <select v-model="filtrosHist.tipo" class="input-filter">
                     <option value="">Todos</option>
                     <option v-for="t in (subTab === 'atividades' ? tiposAtividade : tiposEmocao)" :key="t" :value="t">{{ t }}</option>
                 </select>
             </div>

             <div class="flex items-center gap-2 flex-1">
                 <input v-model="filtrosHist.conteudo" type="text" :placeholder="subTab === 'atividades' ? 'Filtrar conteúdo...' : 'Filtrar relato...'" class="input-filter w-full">
             </div>

             <button @click="limparFiltrosHistorico" class="p-2 bg-white border border-gray-200 rounded-lg hover:bg-gray-100 text-gray-500" title="Limpar Filtros"><Eraser size="16"/></button>
         </div>

         <div v-if="subTab === 'atividades'">
             <div class="p-4 md:p-6 bg-green-50/30 border-b border-green-50 flex justify-between items-center">
                 <h3 class="font-bold text-green-700 flex items-center gap-2 text-sm md:text-base"><BookOpen size="20"/> Jogos e Atividades Realizadas</h3>
                 <span class="text-xs md:text-sm font-bold text-gray-400">{{ historicoJogosFiltrados.length }} registros</span>
             </div>
             <div class="overflow-x-auto">
               <table class="w-full text-left min-w-[600px]">
                  <thead class="bg-gray-50 text-xs uppercase text-gray-400 font-extrabold">
                     <tr>
                        <th class="p-3 md:p-6">Data</th>
                        <th class="p-3 md:p-6">Tipo</th>
                        <th class="p-3 md:p-6">Conteúdo</th>
                        <th class="p-3 md:p-6 text-right">Ações</th>
                     </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-50">
                     <tr v-for="jogo in historicoJogosFiltrados" :key="jogo.id" class="hover:bg-gray-50">
                        <td class="p-3 md:p-6 font-bold text-gray-500 text-sm">{{ formatarData(jogo.dataRealizacao) }}</td>
                        <td class="p-3 md:p-6 font-bold text-indigo-600 text-sm">{{ jogo.tipo }}</td>
                        <td class="p-3 md:p-6 font-bold text-gray-700 text-sm">{{ jogo.conteudo }}</td>
                        <td class="p-3 md:p-6 text-right flex justify-end gap-2">
                            <button @click="abrirModalEditarHistorico(jogo)" class="icon-btn text-indigo-400"><Edit2 size="18"/></button>
                            <button @click="excluir(jogo.id)" class="icon-btn text-red-400"><Trash2 size="18"/></button>
                        </td>
                     </tr>
                     <tr v-if="historicoJogosFiltrados.length === 0">
                         <td colspan="4" class="p-6 text-center text-gray-400 font-bold text-sm">Nenhum registro encontrado com esses filtros.</td>
                     </tr>
                  </tbody>
               </table>
             </div>
         </div>

         <div v-if="subTab === 'diarios'">
             <div class="p-4 md:p-6 bg-blue-50/30 border-b border-blue-50 flex justify-between items-center">
                 <h3 class="font-bold text-blue-700 flex items-center gap-2 text-sm md:text-base"><Database size="20"/> Diários e Emoções</h3>
                 <span class="text-xs md:text-sm font-bold text-gray-400">{{ historicoDiariosFiltrados.length }} registros</span>
             </div>
             <div class="overflow-x-auto">
               <table class="w-full text-left min-w-[600px]">
                  <thead class="bg-gray-50 text-xs uppercase text-gray-400 font-extrabold">
                     <tr>
                        <th class="p-3 md:p-6">Data</th>
                        <th class="p-3 md:p-6">Emoção / Intensidade</th>
                        <th class="p-3 md:p-6">Relato</th>
                        <th class="p-3 md:p-6 text-right">Ações</th>
                     </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-50">
                     <tr v-for="diario in historicoDiariosFiltrados" :key="diario.id" class="hover:bg-gray-50">
                        <td class="p-3 md:p-6 font-bold text-gray-500 text-sm">{{ formatarData(diario.dataRegistro) }}</td>
                        <td class="p-3 md:p-6">
                            <span class="badge-indigo">{{ diario.emocao }} ({{ diario.intensidade }})</span>
                        </td>
                        <td class="p-3 md:p-6 text-sm text-gray-600 italic max-w-[200px] truncate">"{{ diario.relato }}"</td>
                        <td class="p-3 md:p-6 text-right flex justify-end gap-2">
                            <button @click="abrirModalEditarHistorico(diario)" class="icon-btn text-indigo-400"><Edit2 size="18"/></button>
                            <button @click="excluir(diario.id)" class="icon-btn text-red-400"><Trash2 size="18"/></button>
                        </td>
                     </tr>
                     <tr v-if="historicoDiariosFiltrados.length === 0">
                         <td colspan="4" class="p-6 text-center text-gray-400 font-bold text-sm">Nenhum registro encontrado com esses filtros.</td>
                     </tr>
                  </tbody>
               </table>
             </div>
         </div>
      </div>
    </main>

    <div v-if="modalFormAberto" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4 animate-fade-in">
       <div class="bg-white rounded-[20px] md:rounded-[30px] shadow-2xl w-full max-w-md overflow-hidden mx-2 md:mx-0">
          <div class="bg-indigo-600 p-4 md:p-6 flex justify-between items-center text-white">
             <h3 class="font-black text-lg md:text-xl">
                 {{ itemEmEdicao.id ? 'Editar' : 'Novo' }} 
                 {{ viewMode === 'historico' ? 'Registro' : (activeTab === 'usuarios' ? 'Usuário' : 'Aluno') }}
             </h3>
             <button @click="modalFormAberto = false" class="hover:bg-white/20 p-2 rounded-full"><X size="20"/></button>
          </div>
          
          <div class="p-6 md:p-8 space-y-4 max-h-[70vh] overflow-y-auto">
             
             <template v-if="viewMode === 'lista'">
                 <template v-if="activeTab === 'usuarios'">
                    <div><label class="label">Nome</label><input v-model="itemEmEdicao.nome" class="input"></div>
                    <div><label class="label">Email</label><input v-model="itemEmEdicao.email" class="input"></div>
                    <div><label class="label">Senha</label><input v-model="itemEmEdicao.senha" type="password" class="input"></div>
                    <div><label class="label">Perfil</label>
                       <select v-model="itemEmEdicao.perfil" class="input">
                          <option value="RESPONSAVEL">RESPONSAVEL (Professor)</option><option value="ADMINISTRADOR">ADMIN</option>
                       </select>
                    </div>
                 </template>
                 <template v-if="activeTab === 'alunos'">
                    <div><label class="label">Nome do Aluno</label><input v-model="itemEmEdicao.nome" class="input"></div>
                    <div><label class="label">Selecione o Professor(a)</label>
                       <select v-model="itemEmEdicao.responsavel.id" class="input">
                          <option disabled value="">Selecione...</option>
                          <option v-for="p in listaPais" :value="p.id" :key="p.id">{{ p.nome }}</option>
                       </select>
                    </div>
                 </template>
             </template>

             <template v-else>
                 <div class="p-3 bg-indigo-50 rounded-xl border border-indigo-100 mb-4">
                     <span class="block text-xs font-bold text-indigo-400 uppercase">Aluno Vinculado</span>
                     <span class="block font-black text-indigo-700 text-lg">{{ alunoSelecionado.nome }}</span>
                 </div>

                 <template v-if="subTab === 'atividades'">
                    <div><label class="label">Tipo de Jogo</label>
                        <select v-model="itemEmEdicao.tipo" class="input">
                            <option v-for="t in tiposAtividade" :key="t" :value="t">{{ t }}</option>
                        </select>
                    </div>
                    <div><label class="label">Conteúdo</label><input v-model="itemEmEdicao.conteudo" class="input"></div>
                    <div><label class="label">Data Realização</label><input v-model="itemEmEdicao.dataRealizacao" type="datetime-local" class="input"></div>
                 </template>

                 <template v-if="subTab === 'diarios'">
                    <div><label class="label">Emoção</label>
                       <select v-model="itemEmEdicao.emocao" class="input">
                          <option v-for="e in tiposEmocao" :key="e" :value="e">{{ e }}</option>
                       </select>
                    </div>
                    <div><label class="label">Intensidade (1-5)</label><input v-model="itemEmEdicao.intensidade" type="number" min="1" max="5" class="input"></div>
                    <div><label class="label">Data Registro</label><input v-model="itemEmEdicao.dataRegistro" type="datetime-local" class="input"></div>
                    <div><label class="label">Relato</label><textarea v-model="itemEmEdicao.relato" class="input" rows="3"></textarea></div>
                 </template>
             </template>

          </div>

          <div class="p-4 md:p-6 border-t border-gray-100 flex justify-end gap-3 bg-gray-50">
             <button @click="modalFormAberto = false" class="px-4 md:px-6 py-3 rounded-[15px] font-bold text-gray-500 hover:bg-gray-200 text-sm md:text-base">Cancelar</button>
             <button @click="salvar" class="btn-primary"><Save size="18" /> Salvar</button>
          </div>
       </div>
    </div>

  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
.label { @apply block text-sm font-bold text-gray-500 mb-1; }
.input { @apply w-full p-3 bg-gray-50 rounded-xl border border-gray-200 outline-none focus:border-indigo-500 font-bold text-gray-700 text-sm md:text-base; }
.input-filter { @apply p-2 bg-white rounded-lg border border-gray-200 outline-none focus:border-indigo-500 font-bold text-gray-600 text-xs; }
.btn-primary { @apply px-4 md:px-6 py-3 bg-indigo-600 text-white rounded-[15px] font-bold hover:bg-indigo-700 shadow-lg shadow-indigo-200 flex items-center gap-2 transition-all text-sm md:text-base; }
.icon-btn { @apply p-2 rounded-lg hover:bg-gray-100 transition-colors; }
.badge-indigo { @apply text-[10px] md:text-xs bg-indigo-50 text-indigo-600 px-2 py-1 rounded-md font-bold; }
.badge-purple { @apply text-[10px] md:text-xs bg-purple-50 text-purple-600 px-2 py-1 rounded-md font-bold; }
.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
::-webkit-scrollbar { width: 8px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #E0E7FF; border-radius: 10px; }
</style>