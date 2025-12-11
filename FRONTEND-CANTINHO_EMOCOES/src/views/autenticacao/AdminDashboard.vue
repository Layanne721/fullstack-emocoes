<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api'; // <--- IMPORTAÇÃO
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const usuarios = ref([]); 
const loading = ref(true);
const erro = ref(null);
const deletandoId = ref(null);
const termoPesquisa = ref('');

onMounted(async () => {
  if (authStore.user?.perfil !== 'ADMINISTRADOR') {
    router.push('/');
    return;
  }
  await carregarUsuarios();
});

async function carregarUsuarios() {
  loading.value = true;
  erro.value = null;
  try {
    // Interceptor injeta o token automaticamente
    const response = await api.get('/api/admin/usuarios');
    usuarios.value = response.data;
  } catch (e) {
    erro.value = "Erro ao carregar lista.";
  } finally {
    loading.value = false;
  }
}

function fazerLogout() {
  if (authStore.clearLoginData) {
      authStore.clearLoginData();
  } else {
      authStore.token = null;
      authStore.user = null;
  }
  router.push('/login');
}

async function excluirUsuario(usuario) {
  if (usuario.perfil === 'ADMINISTRADOR') {
    alert("⛔ Ação Negada: Você não pode excluir contas de Administrador.");
    return;
  }

  let mensagem = `⚠️ Tem certeza que deseja excluir o professor(a) ${usuario.nome}?`;
  
  if (usuario.perfil === 'RESPONSAVEL' && usuario.dependentes && usuario.dependentes.length > 0) {
    mensagem += `\n\nATENÇÃO: Isso excluirá também ${usuario.dependentes.length} aluno(s) vinculados e seus diários!`;
  }

  if (!confirm(mensagem)) return;

  deletandoId.value = usuario.id;

  try {
    await api.delete(`/api/admin/usuarios/${usuario.id}`);
    
    usuarios.value = usuarios.value.filter(u => u.id !== usuario.id);
    alert("Usuário e alunos excluídos com sucesso.");
  } catch (e) {
    const msgErro = e.response?.data?.error || "Erro ao excluir.";
    alert("Erro: " + msgErro);
  } finally {
    deletandoId.value = null;
  }
}

const usuariosFiltrados = computed(() => {
  if (!termoPesquisa.value) return usuarios.value;
  const termo = termoPesquisa.value.toLowerCase();
  
  return usuarios.value.filter(u => 
    u.nome.toLowerCase().includes(termo) || 
    (u.email && u.email.toLowerCase().includes(termo))
  );
});

const stats = computed(() => {
  let totalProfessores = 0;
  let totalAlunos = 0;
  let totalAdmins = 0;

  usuarios.value.forEach(u => {
    if (u.perfil === 'ADMINISTRADOR') totalAdmins++;
    if (u.perfil === 'RESPONSAVEL') {
      totalProfessores++;
      totalAlunos += u.dependentes ? u.dependentes.length : 0;
    }
  });

  return {
    total: totalProfessores + totalAlunos + totalAdmins,
    admins: totalAdmins,
    professores: totalProfessores,
    alunos: totalAlunos
  };
});

function formatarData(data) {
  if (!data) return '-';
  if (Array.isArray(data)) return new Date(data[0], data[1]-1, data[2]).toLocaleDateString('pt-BR');
  return new Date(data).toLocaleDateString('pt-BR');
}
</script>

<template>
  <div class="min-h-screen bg-[#F0F7FF] p-4 md:p-8 font-nunito relative overflow-hidden">
    
    <div class="absolute top-10 left-10 text-4xl animate-float-slow opacity-50 pointer-events-none">📚</div>
    <div class="absolute bottom-10 right-10 text-5xl animate-bounce-slow opacity-50 pointer-events-none">🎓</div>

    <div class="max-w-7xl mx-auto space-y-6 relative z-10">
      
      <header class="bg-white rounded-[30px] p-6 shadow-sm border-2 border-white flex flex-col md:flex-row justify-between items-center gap-4">
        <div>
          <h1 class="text-2xl font-black text-[#4F46E5]" style="font-family: 'Nunito', sans-serif;">Painel Escolar</h1>
          <p class="text-sm text-gray-400 font-bold">Gerenciamento de Professores e Alunos</p>
        </div>
        
        <button @click="fazerLogout" class="px-6 py-2 bg-red-50 text-red-500 rounded-[20px] font-bold hover:bg-red-100 flex items-center gap-2 transition-all">
          Sair <span class="text-lg">🚪</span>
        </button>
      </header>

      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="p-6 bg-white rounded-[25px] shadow-sm border border-indigo-100">
          <p class="text-xs font-bold text-indigo-400 uppercase tracking-wider">TOTAL USUÁRIOS</p>
          <p class="text-3xl font-black text-gray-700">{{ stats.total }}</p>
        </div>
        <div class="p-6 bg-white rounded-[25px] shadow-sm border border-blue-100">
          <p class="text-xs font-bold text-blue-500 uppercase tracking-wider">PROFESSORES</p>
          <p class="text-3xl font-black text-gray-700">{{ stats.professores }}</p>
        </div>
        <div class="p-6 bg-white rounded-[25px] shadow-sm border border-green-100">
          <p class="text-xs font-bold text-green-500 uppercase tracking-wider">ALUNOS</p>
          <p class="text-3xl font-black text-gray-700">{{ stats.alunos }}</p>
        </div>
        <div class="p-6 bg-white rounded-[25px] shadow-sm border border-orange-100">
          <p class="text-xs font-bold text-orange-400 uppercase tracking-wider">ADMINS</p>
          <p class="text-3xl font-black text-gray-700">{{ stats.admins }}</p>
        </div>
      </div>

      <div class="bg-white rounded-[30px] shadow-sm overflow-hidden min-h-[400px] border-2 border-white">
        <div class="p-6 border-b border-gray-100">
          <input v-model="termoPesquisa" type="text" placeholder="🔍 Buscar por professor ou email..." 
                 class="w-full md:w-1/3 px-6 py-3 rounded-[20px] bg-[#F9FAFB] border-2 border-transparent focus:bg-white focus:border-[#4F46E5] outline-none font-bold text-gray-600 placeholder-gray-300 transition-all shadow-inner">
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left">
            <thead class="bg-[#F0F7FF] text-xs uppercase text-gray-400 font-bold tracking-wider">
              <tr>
                <th class="p-6">Usuário</th>
                <th class="p-6">Função</th>
                <th class="p-6">Cadastro</th>
                <th class="p-6 text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <template v-for="user in usuariosFiltrados" :key="user.id">
                
                <tr class="hover:bg-[#F9FAFB] transition-colors">
                  <td class="p-6">
                    <div class="flex items-center gap-4">
                      <div class="w-12 h-12 rounded-full bg-[#E0E7FF] flex items-center justify-center text-2xl shadow-sm">
                         <span v-if="user.perfil === 'ADMINISTRADOR'">🛡️</span>
                         <span v-else>👨‍🏫</span>
                      </div>
                      <div>
                        <div class="font-extrabold text-gray-700">{{ user.nome }}</div>
                        <div class="text-xs text-gray-400 font-bold">{{ user.email }}</div>
                      </div>
                    </div>
                  </td>
                  <td class="p-6">
                    <span :class="{
                      'bg-orange-50 text-orange-600 border border-orange-100': user.perfil === 'ADMINISTRADOR',
                      'bg-blue-50 text-blue-600 border border-blue-100': user.perfil === 'RESPONSAVEL'
                    }" class="px-3 py-1.5 rounded-full text-[10px] font-black uppercase tracking-wide">
                      {{ user.perfil === 'RESPONSAVEL' ? 'PROFESSOR' : user.perfil }}
                    </span>
                  </td>
                  <td class="p-6 text-sm font-bold text-gray-400">{{ formatarData(user.dataCadastro) }}</td>
                  <td class="p-6 text-right">
                    <button 
                      @click="excluirUsuario(user)"
                      :disabled="user.perfil === 'ADMINISTRADOR' || deletandoId === user.id"
                      :class="user.perfil === 'ADMINISTRADOR' ? 'opacity-30 cursor-not-allowed' : 'hover:bg-red-50 text-gray-400 hover:text-red-500'"
                      class="px-4 py-2 rounded-[15px] transition-colors border border-gray-100 text-xs font-bold flex items-center gap-2 ml-auto hover:border-red-100"
                      :title="user.perfil === 'ADMINISTRADOR' ? 'Não é possível excluir Admins' : 'Excluir conta e alunos'"
                    >
                      <span v-if="deletandoId === user.id">⏳</span>
                      <span v-else>🗑️ Excluir</span>
                    </button>
                  </td>
                </tr>

                <tr v-if="user.dependentes && user.dependentes.length > 0" class="bg-[#F8FAFC]">
                  <td colspan="4" class="p-0">
                    <div class="pl-20 pr-6 py-4 space-y-3">
                      <div class="text-[10px] font-black text-blue-400 uppercase tracking-widest mb-2 flex items-center gap-2">
                        <span>↘</span> Alunos de {{ user.nome }} ({{ user.dependentes.length }})
                      </div>
                      
                      <div v-for="aluno in user.dependentes" :key="aluno.id" 
                           class="flex items-center justify-between bg-white p-3 rounded-[20px] border border-blue-100 shadow-sm hover:shadow-md transition-all">
                        
                        <div class="flex items-center gap-3">
                          <div class="w-10 h-10 rounded-full bg-green-50 flex items-center justify-center text-lg border-2 border-green-100">🎓</div>
                          <div>
                            <div class="font-bold text-gray-700 text-sm">{{ aluno.nome }}</div>
                            <div class="text-[10px] text-gray-400 font-bold">Conta Aluno</div>
                          </div>
                        </div>

                        <div class="flex items-center gap-4">
                           <span class="bg-green-50 text-green-600 px-3 py-1 rounded-full text-[10px] font-black uppercase border border-green-100">
                             ALUNO
                           </span>
                           <span class="text-[10px] font-bold text-gray-400 mr-2">
                             {{ formatarData(aluno.dataCadastro) }}
                           </span>
                        </div>
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
  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
.animate-bounce-slow { animation: floatSlow 3s ease-in-out infinite; }
</style>