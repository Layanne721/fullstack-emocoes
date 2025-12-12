<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/services/api';

// Imports de componentes (mantidos do seu original)
import { 
  Users, BookOpen, Activity, BarChart2, LogOut, 
  Trash2, Edit, CheckCircle, Search, UserPlus 
} from 'lucide-vue-next';

// Configuração inicial
const router = useRouter();
const authStore = useAuthStore();
const activeTab = ref('usuarios');
const loading = ref(false);

// Dados
const usuarios = ref([]);
const atividades = ref([]);
const emocoes = ref([]);
const stats = ref({});
const termoPesquisa = ref('');

// --- VARIÁVEIS PARA BACKUP ---
const fileInput = ref(null);
const loadingBackup = ref(false);
const loadingRestore = ref(false);

onMounted(async () => {
  if (authStore.user?.perfil !== 'ADMIN') {
    router.push('/');
    return;
  }
  await carregarDadosIniciais();
});

async function carregarDadosIniciais() {
  loading.value = true;
  try {
    // Carrega usuários e estatísticas
    const [usersRes, statsRes] = await Promise.all([
      api.get('/api/admin/usuarios'),
      api.get('/api/admin/dashboard/stats')
    ]);
    usuarios.value = usersRes.data;
    stats.value = statsRes.data;
  } catch (error) {
    console.error('Erro ao carregar dados:', error);
  } finally {
    loading.value = false;
  }
}

// --- FUNÇÕES DE BACKUP E RESTORE ---

async function baixarBackup() {
    loadingBackup.value = true;
    try {
        const response = await api.get('/api/admin/backup/download', { responseType: 'blob' });
        
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        const date = new Date().toISOString().slice(0, 10);
        link.setAttribute('download', `backup_cantinho_${date}.sql`);
        
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    } catch (e) {
        console.error(e);
        alert("Erro ao baixar backup. Verifique os logs do servidor.");
    } finally {
        loadingBackup.value = false;
    }
}

function acionarInputRestore() {
    fileInput.value.click();
}

async function enviarRestore(event) {
    const file = event.target.files[0];
    if (!file) return;

    const confirmacao = confirm(
        `⚠️ PERIGO: Restaurar o banco substituirá TODOS os dados atuais pelos do arquivo "${file.name}".\n\nDeseja continuar?`
    );
    
    if (!confirmacao) {
        event.target.value = '';
        return;
    }

    loadingRestore.value = true;
    const formData = new FormData();
    formData.append('file', file);

    try {
        await api.post('/api/admin/backup/restore', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        alert("✅ Banco restaurado com sucesso! A página será recarregada.");
        window.location.reload();
    } catch (e) {
        const msg = e.response?.data?.error || "Erro na restauração.";
        alert("❌ Falha: " + msg);
    } finally {
        loadingRestore.value = false;
        event.target.value = '';
    }
}

// --- FUNÇÕES ORIGINAIS ---

const usuariosFiltrados = computed(() => {
  if (!termoPesquisa.value) return usuarios.value;
  const termo = termoPesquisa.value.toLowerCase();
  return usuarios.value.filter(u => 
    u.nome.toLowerCase().includes(termo) || 
    u.email.toLowerCase().includes(termo)
  );
});

async function excluirUsuario(id) {
  if (!confirm('Tem certeza que deseja excluir este usuário?')) return;
  try {
    await api.delete(`/api/admin/usuarios/${id}`);
    usuarios.value = usuarios.value.filter(u => u.id !== id);
    alert('Usuário excluído com sucesso!');
  } catch (error) {
    alert('Erro ao excluir usuário.');
  }
}

function formatarData(dataArray) {
  if (!dataArray) return '-';
  const data = new Date(dataArray[0], dataArray[1]-1, dataArray[2]);
  return data.toLocaleDateString('pt-BR');
}

function logout() {
  authStore.logout();
  router.push('/login');
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex">
    
    <aside class="w-64 bg-indigo-700 text-white hidden md:flex flex-col">
      <div class="p-6">
        <h2 class="text-2xl font-bold flex items-center gap-2">
          🛡️ Admin
        </h2>
      </div>
      
      <nav class="flex-1 px-4 space-y-2">
        <button 
          @click="activeTab = 'usuarios'"
          :class="['w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors', 
            activeTab === 'usuarios' ? 'bg-indigo-800' : 'hover:bg-indigo-600']"
        >
          <Users size="20" /> Usuários
        </button>

        <button 
          @click="activeTab = 'atividades'"
          :class="['w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors', 
            activeTab === 'atividades' ? 'bg-indigo-800' : 'hover:bg-indigo-600']"
        >
          <BookOpen size="20" /> Atividades
        </button>

        <button 
          @click="activeTab = 'stats'"
          :class="['w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-colors', 
            activeTab === 'stats' ? 'bg-indigo-800' : 'hover:bg-indigo-600']"
        >
          <BarChart2 size="20" /> Estatísticas
        </button>
      </nav>

      <div class="p-4 border-t border-indigo-600">
        <button @click="logout" class="w-full flex items-center gap-2 text-indigo-200 hover:text-white transition-colors">
          <LogOut size="20" /> Sair do Sistema
        </button>
      </div>
    </aside>

    <main class="flex-1 overflow-auto">
      <div class="p-8">
        
        <header class="bg-white rounded-2xl p-6 shadow-sm mb-8 flex justify-between items-center">
          <div>
            <h1 class="text-2xl font-bold text-gray-800">Painel Administrativo</h1>
            <p class="text-gray-500">Bem-vindo, {{ authStore.user?.nome }}</p>
          </div>
          
          <div class="flex items-center gap-3">
             <input 
                type="file" 
                ref="fileInput" 
                class="hidden" 
                accept=".sql,.dum,.dump" 
                @change="enviarRestore"
            >

             <button 
                @click="baixarBackup"
                :disabled="loadingBackup"
                class="flex items-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-600 rounded-lg hover:bg-indigo-100 transition-colors border border-indigo-100 font-bold"
             >
                <span v-if="loadingBackup" class="animate-spin">⏳</span>
                <span v-else>💾 Backup</span>
             </button>

             <button 
                @click="acionarInputRestore"
                :disabled="loadingRestore"
                class="flex items-center gap-2 px-4 py-2 bg-orange-50 text-orange-600 rounded-lg hover:bg-orange-100 transition-colors border border-orange-100 font-bold"
             >
                <span v-if="loadingRestore" class="animate-spin">🔄</span>
                <span v-else>♻️ Restaurar</span>
             </button>
          </div>
        </header>

        <div v-if="activeTab === 'usuarios'" class="space-y-6">
          <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
            <div class="p-6 border-b border-gray-100 flex justify-between items-center">
              <h3 class="font-bold text-lg text-gray-700">Gerenciar Usuários</h3>
              <div class="relative w-64">
                <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size="18" />
                <input 
                  v-model="termoPesquisa"
                  type="text" 
                  placeholder="Buscar usuário..." 
                  class="w-full pl-10 pr-4 py-2 bg-gray-50 rounded-lg border-none focus:ring-2 focus:ring-indigo-500"
                >
              </div>
            </div>

            <div class="overflow-x-auto">
              <table class="w-full text-left">
                <thead class="bg-gray-50 text-xs uppercase text-gray-500 font-semibold">
                  <tr>
                    <th class="p-6">Nome</th>
                    <th class="p-6">Email</th>
                    <th class="p-6">Perfil</th>
                    <th class="p-6">Cadastro</th>
                    <th class="p-6 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  <tr v-for="user in usuariosFiltrados" :key="user.id" class="hover:bg-gray-50 transition-colors">
                    <td class="p-6 font-medium text-gray-700">{{ user.nome }}</td>
                    <td class="p-6 text-gray-500">{{ user.email }}</td>
                    <td class="p-6">
                      <span :class="{
                        'bg-purple-100 text-purple-700': user.perfil === 'ADMIN',
                        'bg-blue-100 text-blue-700': user.perfil === 'RESPONSAVEL',
                        'bg-green-100 text-green-700': user.perfil === 'DEPENDENTE'
                      }" class="px-3 py-1 rounded-full text-xs font-bold uppercase">
                        {{ user.perfil }}
                      </span>
                    </td>
                    <td class="p-6 text-gray-500">{{ formatarData(user.dataCadastro) }}</td>
                    <td class="p-6 text-right">
                      <button 
                        @click="excluirUsuario(user.id)"
                        class="p-2 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="Excluir Usuário"
                      >
                        <Trash2 size="18" />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'stats'" class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-white p-6 rounded-2xl shadow-sm border-l-4 border-blue-500">
            <p class="text-sm text-gray-500 font-bold uppercase">Total Usuários</p>
            <p class="text-4xl font-bold text-gray-800 mt-2">{{ stats.totalUsuarios || 0 }}</p>
          </div>
          <div class="bg-white p-6 rounded-2xl shadow-sm border-l-4 border-green-500">
            <p class="text-sm text-gray-500 font-bold uppercase">Total Diários</p>
            <p class="text-4xl font-bold text-gray-800 mt-2">{{ stats.totalDiarios || 0 }}</p>
          </div>
          <div class="bg-white p-6 rounded-2xl shadow-sm border-l-4 border-purple-500">
             <p class="text-sm text-gray-500 font-bold uppercase">Total Atividades</p>
             <p class="text-4xl font-bold text-gray-800 mt-2">{{ stats.totalAtividades || 0 }}</p>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>