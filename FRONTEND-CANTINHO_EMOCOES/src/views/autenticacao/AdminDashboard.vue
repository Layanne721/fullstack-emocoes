<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/services/api';

// Ícones (Lucide)
import { 
  Users, BookOpen, BarChart2, LogOut, 
  Trash2, Search, UploadCloud, DownloadCloud, Database
} from 'lucide-vue-next';

// --- CONFIGURAÇÃO ---
const router = useRouter();
const authStore = useAuthStore();
const activeTab = ref('usuarios');
const loading = ref(false);

// --- DADOS ---
const usuarios = ref([]);
const stats = ref({});
const termoPesquisa = ref('');

// --- VARIÁVEIS DE BACKUP ---
const fileInput = ref(null);
const loadingBackup = ref(false);
const loadingRestore = ref(false);
const deletandoId = ref(null);

onMounted(async () => {
  const perfil = authStore.user?.perfil;
  if (perfil !== 'ADMIN' && perfil !== 'ADMINISTRADOR') {
    router.push('/');
    return;
  }
  await carregarDadosIniciais();
});

async function carregarDadosIniciais() {
  loading.value = true;
  try {
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

// --- BACKUP & RESTORE ---
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
        alert("Erro ao baixar backup.");
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

    if (!confirm(`⚠️ PERIGO: Isso substituirá TODOS os dados atuais pelo backup "${file.name}". Continuar?`)) {
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
        const msg = e.response?.data?.error || "Falha na restauração.";
        alert("❌ Erro: " + msg);
    } finally {
        loadingRestore.value = false;
        event.target.value = '';
    }
}

// --- FILTROS E AÇÕES ---
const usuariosFiltrados = computed(() => {
  if (!termoPesquisa.value) return usuarios.value;
  const termo = termoPesquisa.value.toLowerCase();
  return usuarios.value.filter(u => 
    u.nome.toLowerCase().includes(termo) || 
    (u.email && u.email.toLowerCase().includes(termo))
  );
});

async function excluirUsuario(usuario) {
  if (usuario.perfil === 'ADMIN' || usuario.perfil === 'ADMINISTRADOR') {
    alert("⛔ Você não pode excluir administradores.");
    return;
  }
  let mensagem = `⚠️ Excluir ${usuario.nome}?`;
  if (usuario.dependentes?.length > 0) {
    mensagem += `\n\nATENÇÃO: Isso excluirá também ${usuario.dependentes.length} alunos vinculados!`;
  }
  if (!confirm(mensagem)) return;

  deletandoId.value = usuario.id;
  try {
    await api.delete(`/api/admin/usuarios/${usuario.id}`);
    usuarios.value = usuarios.value.filter(u => u.id !== usuario.id);
    alert('Usuário excluído!');
  } catch (error) {
    alert('Erro ao excluir usuário.');
  } finally {
    deletandoId.value = null;
  }
}

function formatarData(data) {
  if (!data) return '-';
  if (Array.isArray(data)) return new Date(data[0], data[1]-1, data[2]).toLocaleDateString('pt-BR');
  return new Date(data).toLocaleDateString('pt-BR');
}

function logout() {
  if (authStore.clearLoginData) authStore.clearLoginData();
  else authStore.logout();
  router.push('/login');
}
</script>

<template>
  <div class="min-h-screen bg-[#F0F7FF] flex font-nunito relative overflow-hidden">
    <div class="absolute top-10 left-60 text-4xl animate-float-slow opacity-30 pointer-events-none z-0">📚</div>
    <div class="absolute bottom-10 right-10 text-5xl animate-bounce-slow opacity-30 pointer-events-none z-0">🎓</div>

    <aside class="w-16 md:w-64 bg-white m-2 md:m-4 rounded-[20px] md:rounded-[30px] shadow-sm border border-indigo-50 flex flex-col z-20 transition-all duration-300">
      <div class="p-4 md:p-6 flex flex-col items-center md:items-start">
        <h2 class="text-2xl font-black text-[#4F46E5] hidden md:block tracking-tight">Admin<span class="text-orange-400">Panel</span></h2>
        <span class="md:hidden text-2xl">🛡️</span>
      </div>
      
      <nav class="flex-1 px-2 md:px-4 space-y-3 mt-4">
        <button @click="activeTab = 'usuarios'" :class="['w-full flex items-center justify-center md:justify-start gap-3 px-2 md:px-4 py-3 rounded-[20px] transition-all font-bold', activeTab === 'usuarios' ? 'bg-indigo-50 text-indigo-600 shadow-sm' : 'text-gray-400 hover:bg-gray-50']">
          <Users size="22" /> <span class="hidden md:block">Usuários</span>
        </button>

        <button @click="activeTab = 'atividades'" :class="['w-full flex items-center justify-center md:justify-start gap-3 px-2 md:px-4 py-3 rounded-[20px] transition-all font-bold', activeTab === 'atividades' ? 'bg-orange-50 text-orange-500 shadow-sm' : 'text-gray-400 hover:bg-gray-50']">
          <BookOpen size="22" /> <span class="hidden md:block">Atividades</span>
        </button>

        <button @click="activeTab = 'stats'" :class="['w-full flex items-center justify-center md:justify-start gap-3 px-2 md:px-4 py-3 rounded-[20px] transition-all font-bold', activeTab === 'stats' ? 'bg-green-50 text-green-600 shadow-sm' : 'text-gray-400 hover:bg-gray-50']">
          <BarChart2 size="22" /> <span class="hidden md:block">Estatísticas</span>
        </button>

        <button @click="router.push('/admin/dados')" class="w-full flex items-center justify-center md:justify-start gap-3 px-2 md:px-4 py-3 rounded-[20px] transition-all font-bold text-gray-400 hover:bg-gray-50 hover:text-indigo-600">
          <Database size="22" /> <span class="hidden md:block">Gerenciar Dados</span>
        </button>
      </nav>

      <div class="p-2 md:p-4 border-t border-gray-100 mb-2">
        <button @click="logout" class="w-full flex items-center justify-center md:justify-start gap-2 px-2 md:px-4 py-3 text-red-400 hover:bg-red-50 hover:text-red-500 rounded-[20px] transition-colors font-bold">
          <LogOut size="20" /> <span class="hidden md:block">Sair</span>
        </button>
      </div>
    </aside>

    <main class="flex-1 p-3 md:p-8 overflow-y-auto z-10 h-screen">
      
      <header class="bg-white rounded-[20px] md:rounded-[30px] p-4 md:p-6 shadow-sm border-2 border-white mb-6 md:mb-8 flex flex-col md:flex-row justify-between items-center gap-4">
        <div class="text-center md:text-left">
          <h1 class="text-xl md:text-2xl font-black text-gray-700">Olá, {{ authStore.user?.nome }}</h1>
          <p class="text-xs md:text-sm text-gray-400 font-bold">Gerencie o Cantinho do Saber</p>
        </div>
        
        <div class="flex items-center gap-2 md:gap-3 w-full md:w-auto justify-center">
           <input type="file" ref="fileInput" class="hidden" accept=".sql,.dum,.dump" @change="enviarRestore">
           
           <button @click="baixarBackup" :disabled="loadingBackup" class="flex-1 md:flex-none px-4 py-2 bg-indigo-50 text-indigo-600 rounded-[15px] font-bold hover:bg-indigo-100 flex items-center justify-center gap-2 transition-all border border-indigo-100 text-xs md:text-sm">
              <DownloadCloud size="18" v-if="!loadingBackup"/>
              <span v-if="loadingBackup" class="animate-spin">⏳</span>
              <span v-else>Backup</span>
           </button>
           
           <button @click="acionarInputRestore" :disabled="loadingRestore" class="flex-1 md:flex-none px-4 py-2 bg-orange-50 text-orange-600 rounded-[15px] font-bold hover:bg-orange-100 flex items-center justify-center gap-2 transition-all border border-orange-100 text-xs md:text-sm">
              <UploadCloud size="18" v-if="!loadingRestore"/>
              <span v-if="loadingRestore" class="animate-spin">🔄</span>
              <span v-else>Restaurar</span>
           </button>
        </div>
      </header>

      <div v-if="activeTab === 'usuarios'" class="space-y-6 animate-fade-in">
        <div class="bg-white rounded-[20px] md:rounded-[30px] shadow-sm border-2 border-white overflow-hidden min-h-[500px]">
          
          <div class="p-4 md:p-6 border-b border-gray-100 bg-gray-50/50 flex flex-col md:flex-row justify-between items-center gap-4">
            <h3 class="font-black text-lg text-indigo-900 flex items-center gap-2">👥 Lista de Usuários</h3>
            <div class="relative w-full md:w-1/3">
              <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size="20" />
              <input v-model="termoPesquisa" type="text" placeholder="Buscar..." class="w-full pl-12 pr-4 py-3 bg-white rounded-[20px] border-2 border-transparent focus:border-indigo-200 outline-none font-bold text-gray-600 shadow-sm transition-all placeholder-gray-300">
            </div>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse min-w-[600px]">
              <thead class="bg-[#F0F7FF] text-xs uppercase text-gray-400 font-extrabold tracking-wider">
                <tr>
                  <th class="p-3 md:p-6 rounded-tl-[30px]">Usuário</th>
                  <th class="p-3 md:p-6">Função</th>
                  <th class="p-3 md:p-6">Cadastro</th>
                  <th class="p-3 md:p-6 text-right rounded-tr-[30px]">Ações</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                <template v-for="user in usuariosFiltrados" :key="user.id">
                  <tr class="hover:bg-[#F9FAFB] transition-colors group">
                    <td class="p-3 md:p-6">
                      <div class="flex items-center gap-2 md:gap-4">
                        <div class="w-10 h-10 md:w-12 md:h-12 rounded-full bg-gradient-to-br from-indigo-100 to-white flex items-center justify-center text-lg md:text-2xl shadow-sm border border-indigo-50">
                           <span v-if="user.perfil === 'ADMIN' || user.perfil === 'ADMINISTRADOR'">🛡️</span>
                           <span v-else>👨‍🏫</span>
                        </div>
                        <div>
                          <div class="font-extrabold text-gray-700 group-hover:text-indigo-600 transition-colors text-sm md:text-base">{{ user.nome }}</div>
                          <div class="text-[10px] md:text-xs text-gray-400 font-bold max-w-[120px] md:max-w-none truncate">{{ user.email }}</div>
                        </div>
                      </div>
                    </td>
                    <td class="p-3 md:p-6">
                      <span :class="{'bg-purple-50 text-purple-600 border-purple-100': user.perfil === 'ADMIN' || user.perfil === 'ADMINISTRADOR', 'bg-blue-50 text-blue-600 border-blue-100': user.perfil === 'RESPONSAVEL', 'bg-green-50 text-green-600 border-green-100': user.perfil === 'DEPENDENTE'}" class="px-2 md:px-3 py-1.5 rounded-full text-[9px] md:text-[10px] font-black uppercase tracking-wide border">
                        {{ user.perfil === 'RESPONSAVEL' ? 'PROFESSOR' : user.perfil }}
                      </span>
                    </td>
                    <td class="p-3 md:p-6 text-xs md:text-sm font-bold text-gray-400">{{ formatarData(user.dataCadastro) }}</td>
                    <td class="p-3 md:p-6 text-right">
                      <button @click="excluirUsuario(user)" :disabled="deletandoId === user.id" class="p-2 bg-white text-gray-300 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all border border-gray-100 hover:border-red-100 shadow-sm">
                        <span v-if="deletandoId === user.id" class="animate-spin text-xs">⏳</span>
                        <Trash2 size="18" v-else />
                      </button>
                    </td>
                  </tr>
                  <tr v-if="user.dependentes && user.dependentes.length > 0" class="bg-[#F8FAFC]">
                    <td colspan="4" class="p-0">
                      <div class="pl-16 md:pl-20 pr-4 md:pr-6 py-4 space-y-3">
                          <div class="text-[10px] font-black text-blue-400 uppercase tracking-widest mb-2 flex items-center gap-2"><span>↘</span> Alunos de {{ user.nome }}</div>
                          <div v-for="aluno in user.dependentes" :key="aluno.id" class="flex items-center justify-between bg-white p-3 rounded-[20px] border border-blue-50 shadow-sm hover:shadow-md transition-all">
                             <div class="flex items-center gap-3">
                               <div class="w-8 h-8 rounded-full bg-green-50 text-sm flex items-center justify-center border border-green-100">🎓</div>
                               <span class="font-bold text-gray-600 text-sm">{{ aluno.nome }}</span>
                             </div>
                             <span class="text-[10px] font-bold text-gray-400 bg-gray-50 px-2 py-1 rounded-lg">{{ formatarData(aluno.dataCadastro) }}</span>
                          </div>
                      </div>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'stats'" class="animate-fade-in">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-white p-8 rounded-[30px] shadow-sm border border-indigo-50 relative overflow-hidden group hover:-translate-y-1 transition-transform duration-300">
             <div class="absolute -right-4 -top-4 w-24 h-24 bg-indigo-50 rounded-full opacity-50 group-hover:scale-150 transition-transform"></div>
             <p class="text-xs font-black text-indigo-400 uppercase tracking-wider relative z-10">Total de Usuários</p>
             <p class="text-5xl font-black text-gray-700 mt-2 relative z-10">{{ stats.totalUsuarios || 0 }}</p>
          </div>
          <div class="bg-white p-8 rounded-[30px] shadow-sm border border-green-50 relative overflow-hidden group hover:-translate-y-1 transition-transform duration-300">
             <div class="absolute -right-4 -top-4 w-24 h-24 bg-green-50 rounded-full opacity-50 group-hover:scale-150 transition-transform"></div>
             <p class="text-xs font-black text-green-500 uppercase tracking-wider relative z-10">Diários de Emoções</p>
             <p class="text-5xl font-black text-gray-700 mt-2 relative z-10">{{ stats.totalDiarios || 0 }}</p>
          </div>
          <div class="bg-white p-8 rounded-[30px] shadow-sm border border-orange-50 relative overflow-hidden group hover:-translate-y-1 transition-transform duration-300">
             <div class="absolute -right-4 -top-4 w-24 h-24 bg-orange-50 rounded-full opacity-50 group-hover:scale-150 transition-transform"></div>
             <p class="text-xs font-black text-orange-400 uppercase tracking-wider relative z-10">Atividades Realizadas</p>
             <p class="text-5xl font-black text-gray-700 mt-2 relative z-10">{{ stats.totalAtividades || 0 }}</p>
          </div>
        </div>
      </div>

      <div v-if="activeTab === 'atividades'" class="animate-fade-in">
        <div class="bg-white rounded-[30px] p-10 text-center shadow-sm border-2 border-dashed border-gray-200">
            <BookOpen size="48" class="mx-auto text-gray-300 mb-4" />
            <h3 class="text-xl font-bold text-gray-400">Gerenciamento de Atividades</h3>
            <p class="text-gray-400 text-sm mt-2">Funcionalidade em desenvolvimento...</p>
        </div>
      </div>

    </main>
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
.animate-bounce-slow { animation: floatSlow 3s ease-in-out infinite; animation-delay: 1.5s; }
.animate-fade-in { animation: fadeIn 0.3s ease-out forwards; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
::-webkit-scrollbar { width: 8px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: #E0E7FF; border-radius: 10px; }
::-webkit-scrollbar-thumb:hover { background: #C7D2FE; }
</style>