<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import api from '@/services/api';
import { useRouter } from 'vue-router'; // Importação adicionada para segurança

const router = useRouter(); // Adicionado para botão de voltar
const tipoSelecionado = ref('usuarios');
const dados = ref([]);
const loading = ref(false);
const termoPesquisa = ref('');

const recursos = {
  usuarios: { url: '/api/admin/usuarios', nome: 'Usuários' },
  diarios: { url: '/api/admin/diarios', nome: 'Diários' },
  atividades: { url: '/api/admin/atividades', nome: 'Atividades' }
};

async function carregarDados() {
  loading.value = true;
  dados.value = [];
  try {
    const config = recursos[tipoSelecionado.value];
    const res = await api.get(config.url);
    dados.value = res.data;
  } catch (error) {
    alert('Erro ao carregar dados: ' + error.message);
  } finally {
    loading.value = false;
  }
}

const colunas = computed(() => {
  if (dados.value.length === 0) return [];
  return Object.keys(dados.value[0]).filter(key => typeof dados.value[0][key] !== 'object' || dados.value[0][key] === null);
});

const dadosFiltrados = computed(() => {
  if (!termoPesquisa.value) return dados.value;
  const termo = termoPesquisa.value.toLowerCase();
  return dados.value.filter(item => JSON.stringify(item).toLowerCase().includes(termo));
});

async function excluirItem(id) {
  if (!confirm('Tem certeza que deseja excluir este item permanentemente?')) return;
  try {
    const config = recursos[tipoSelecionado.value];
    await api.delete(`${config.url}/${id}`);
    dados.value = dados.value.filter(item => item.id !== id);
    alert('Item excluído!');
  } catch (error) {
    alert('Erro ao excluir: ' + (error.response?.data?.error || error.message));
  }
}

onMounted(carregarDados);
watch(tipoSelecionado, carregarDados);
</script>

<template>
  <div class="min-h-screen bg-gray-50 p-8 font-nunito">
    <div class="max-w-7xl mx-auto">
      <div class="bg-white p-6 rounded-[20px] shadow-sm mb-6 flex flex-col md:flex-row justify-between items-center gap-4">
        <div>
          <button @click="router.push('/admin')" class="text-sm text-indigo-500 hover:underline mb-2">← Voltar para Painel</button>
          <h1 class="text-2xl font-black text-gray-700">Gerenciador Universal</h1>
          <p class="text-sm text-gray-400">Visualize e limpe dados do sistema</p>
        </div>
        <div class="flex gap-4">
          <select v-model="tipoSelecionado" class="px-4 py-2 bg-indigo-50 text-indigo-700 font-bold rounded-lg border border-indigo-100 outline-none">
            <option value="usuarios">👥 Usuários</option>
            <option value="diarios">📔 Diários</option>
            <option value="atividades">🎮 Atividades</option>
          </select>
          <button @click="carregarDados" class="p-2 text-gray-400 hover:text-indigo-600">🔄</button>
        </div>
      </div>

      <div class="bg-white rounded-[20px] shadow-sm overflow-hidden min-h-[400px]">
        <div class="p-4 border-b border-gray-100 flex justify-end">
          <input v-model="termoPesquisa" type="text" placeholder="🔍 Pesquisar em qualquer campo..." class="px-4 py-2 border rounded-lg w-full md:w-1/3">
        </div>

        <div v-if="loading" class="p-10 text-center text-gray-400">Carregando dados...</div>
        <div v-else-if="dados.length === 0" class="p-10 text-center text-gray-400">Nenhum dado encontrado.</div>
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm">
            <thead class="bg-gray-50 text-gray-500 uppercase font-bold">
              <tr>
                <th v-for="col in colunas" :key="col" class="px-6 py-3 border-b">{{ col }}</th>
                <th class="px-6 py-3 border-b text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100">
              <tr v-for="item in dadosFiltrados" :key="item.id" class="hover:bg-blue-50 transition-colors">
                <td v-for="col in colunas" :key="col" class="px-6 py-4 text-gray-600 truncate max-w-[200px]">{{ item[col] }}</td>
                <td class="px-6 py-4 text-right">
                  <button @click="excluirItem(item.id)" class="text-red-400 hover:text-red-600 font-bold border border-red-100 px-3 py-1 rounded-lg hover:bg-red-50">Excluir</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>