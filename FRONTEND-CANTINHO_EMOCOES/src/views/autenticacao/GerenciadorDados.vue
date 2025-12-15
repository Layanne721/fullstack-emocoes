<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/services/api';
import { 
  Users, BookOpen, LogOut, Database, Search, Plus, Edit2, Trash2, X, GraduationCap
} from 'lucide-vue-next';

const router = useRouter();
const authStore = useAuthStore();
const activeTab = ref('usuarios');
const loading = ref(false);
const termoPesquisa = ref('');
const modalAberto = ref(false);
const itemEmEdicao = ref({});

const listaPais = ref([]);
const listaAlunos = ref([]);
const dados = ref([]);

const configAbas = {
  usuarios:   { titulo: 'Usuários', url: '/api/admin/usuarios', icone: Users },
  alunos:     { titulo: 'Alunos',   url: '/api/admin/alunos',   icone: GraduationCap },
  atividades: { titulo: 'Atividades', url: '/api/admin/atividades', icone: BookOpen },
  diarios:    { titulo: 'Diários',    url: '/api/admin/diarios',    icone: Database }
};

onMounted(async () => {
  await carregarDados();
  carregarListas();
});

watch(activeTab, () => {
  termoPesquisa.value = '';
  dados.value = [];
  carregarDados();
});

async function carregarDados() {
  loading.value = true;
  try {
    const res = await api.get(configAbas[activeTab.value].url);
    dados.value = res.data;
  } catch (e) { console.error(e); } finally { loading.value = false; }
}

async function carregarListas() {
    try {
        const [resP, resA] = await Promise.all([api.get('/api/admin/usuarios'), api.get('/api/admin/alunos')]);
        listaPais.value = resP.data;
        listaAlunos.value = resA.data;
    } catch (e) {}
}

const dadosFiltrados = computed(() => {
  if (!termoPesquisa.value) return dados.value;
  return dados.value.filter(i => JSON.stringify(i).toLowerCase().includes(termoPesquisa.value.toLowerCase()));
});

function abrirModal(item = null) {
  if (item) {
     itemEmEdicao.value = JSON.parse(JSON.stringify(item));
  } else {
     itemEmEdicao.value = {};
     if (activeTab.value === 'alunos') itemEmEdicao.value.responsavel = { id: '' };
     if (activeTab.value === 'atividades') itemEmEdicao.value.aluno = { id: '' };
  }
  modalAberto.value = true;
}

async function salvar() {
  try {
    await api.post(configAbas[activeTab.value].url, itemEmEdicao.value);
    alert('Salvo!');
    modalAberto.value = false;
    carregarDados();
  } catch (e) { alert('Erro ao salvar.'); }
}

async function excluir(id) {
  if (confirm('Excluir?')) {
     await api.delete(`${configAbas[activeTab.value].url}/${id}`);
     dados.value = dados.value.filter(i => i.id !== id);
  }
}

function formatData(iso) { return iso ? new Date(iso).toLocaleString('pt-BR') : '-'; }
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex">
    <aside class="w-64 bg-white border-r p-4 flex flex-col">
      <h2 class="text-xl font-bold text-indigo-600 mb-6">AdminPanel</h2>
      <nav class="space-y-2 flex-1">
        <button v-for="(cfg, key) in configAbas" :key="key" @click="activeTab = key"
          :class="['w-full flex items-center gap-2 p-3 rounded font-bold', activeTab===key ? 'bg-indigo-50 text-indigo-600' : 'text-gray-500 hover:bg-gray-100']">
          <component :is="cfg.icone" size="20"/> {{ cfg.titulo }}
        </button>
      </nav>
      <button @click="authStore.logout(); router.push('/login')" class="flex items-center gap-2 text-red-500 font-bold mt-4"><LogOut size="20"/> Sair</button>
    </aside>

    <main class="flex-1 p-8 overflow-y-auto h-screen">
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-bold">{{ configAbas[activeTab].titulo }}</h1>
        <button v-if="activeTab !== 'diarios'" @click="abrirModal()" class="bg-indigo-600 text-white px-4 py-2 rounded font-bold flex gap-2"><Plus size="20"/> Novo</button>
      </div>

      <div class="bg-white p-4 rounded shadow mb-4 flex gap-2 border">
         <Search class="text-gray-400"/>
         <input v-model="termoPesquisa" placeholder="Pesquisar..." class="w-full outline-none">
      </div>

      <div class="bg-white rounded shadow overflow-hidden">
        <table class="w-full text-left">
          <thead class="bg-gray-100 text-gray-600 font-bold uppercase text-xs">
            <tr>
              <th class="p-4">ID</th>
              <th class="p-4" v-if="activeTab !== 'diarios'">Nome/Título</th>
              <th class="p-4" v-if="activeTab === 'diarios'">Aluno</th>
              <th class="p-4" v-if="activeTab === 'diarios'">Emoção / Data</th>
              <th class="p-4 text-right">Ações</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-for="item in dadosFiltrados" :key="item.id" class="hover:bg-gray-50">
              <td class="p-4 text-gray-400">#{{ item.id }}</td>
              
              <td class="p-4 font-bold" v-if="activeTab !== 'diarios'">{{ item.nome || item.titulo }}</td>
              
              <td class="p-4" v-if="activeTab === 'diarios'">{{ item.alunoNome }}</td>
              <td class="p-4" v-if="activeTab === 'diarios'">
                  <span class="bg-blue-100 text-blue-800 px-2 rounded text-xs font-bold">{{ item.emocao }}</span>
                  <div class="text-xs text-gray-500">{{ formatData(item.dataRegistro) }}</div>
              </td>

              <td class="p-4 text-right">
                <button @click="abrirModal(item)" class="text-blue-500 p-2"><Edit2 size="18"/></button>
                <button @click="excluir(item.id)" class="text-red-500 p-2"><Trash2 size="18"/></button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>

    <div v-if="modalAberto" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4">
       <div class="bg-white rounded-lg w-full max-w-md p-6 space-y-4">
          <div class="flex justify-between"><h3 class="font-bold text-lg">Editar</h3><button @click="modalAberto=false"><X/></button></div>
          
          <div v-if="activeTab === 'usuarios'" class="space-y-2">
             <input v-model="itemEmEdicao.nome" class="input" placeholder="Nome">
             <input v-model="itemEmEdicao.email" class="input" placeholder="Email">
             <input v-model="itemEmEdicao.senha" type="password" class="input" placeholder="Senha (vazio mantem)">
             <select v-model="itemEmEdicao.perfil" class="input"><option value="RESPONSAVEL">RESPONSAVEL</option><option value="ADMINISTRADOR">ADMIN</option></select>
          </div>

          <div v-if="activeTab === 'atividades'" class="space-y-2">
             <input v-model="itemEmEdicao.titulo" class="input" placeholder="Título">
             <textarea v-model="itemEmEdicao.descricao" class="input" placeholder="Descrição"></textarea>
             <select v-model="itemEmEdicao.aluno.id" class="input"><option value="">Geral</option><option v-for="a in listaAlunos" :value="a.id">{{ a.nome }}</option></select>
          </div>

          <div v-if="activeTab === 'diarios'" class="space-y-3">
             <label class="font-bold text-sm">Emoção</label>
             <select v-model="itemEmEdicao.emocao" class="input">
                <option value="FELIZ">FELIZ</option><option value="TRISTE">TRISTE</option><option value="BRAVO">BRAVO</option>
                <option value="MEDO">MEDO</option><option value="NOJINHO">NOJINHO</option><option value="CALMO">CALMO</option>
             </select>
             <label class="font-bold text-sm">Intensidade (1-5)</label>
             <input type="number" v-model="itemEmEdicao.intensidade" min="1" max="5" class="input">
             <label class="font-bold text-sm">Data e Hora</label>
             <input type="datetime-local" v-model="itemEmEdicao.dataRegistro" class="input">
             <label class="font-bold text-sm">Relato</label>
             <textarea v-model="itemEmEdicao.relato" class="input" rows="3"></textarea>
          </div>

          <button @click="salvar" class="w-full bg-indigo-600 text-white py-2 rounded font-bold">Salvar</button>
       </div>
    </div>
  </div>
</template>

<style scoped>
.input { @apply w-full border p-2 rounded outline-none focus:border-indigo-500; }
</style>