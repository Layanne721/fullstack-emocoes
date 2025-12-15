<script setup>
import { ref } from 'vue';
import api from '@/services/api'; // <--- IMPORTAÇÃO
import { useRouter } from 'vue-router';
import AvatarSelectorModal from '@/components/AvatarSelectorModal.vue'; 

const router = useRouter();

// Estado do Modal de Avatar
const mostrarSeletorAvatar = ref(false);
const quemEstaSelecionando = ref(null);

// Estado dos Termos de Uso
const aceitouTermos = ref(false);
const mostrarModalTermos = ref(false);

// Dados do Professor
const nomeProfessor = ref('');
const email = ref('');
const senha = ref('');
const confirmarSenha = ref('');
const pin = ref('');
const avatarProfessor = ref('https://api.dicebear.com/7.x/micah/svg?seed=Professor&backgroundColor=e6e6fa'); 

// Controle de Senha
const mostrarSenha = ref(false);
const mostrarConfirmar = ref(false);

// Dados dos Alunos
const alunos = ref([
  { nome: '', genero: 'M', idade: '', avatar: 'https://api.dicebear.com/7.x/adventurer/svg?seed=Aluno1&backgroundColor=b6e3f4' }
]);

const erro = ref(null);
const isSubmitting = ref(false);

// --- Lógica de Avatar ---
function abrirSeletor(tipo, index = null) {
  quemEstaSelecionando.value = { tipo, index };
  mostrarSeletorAvatar.value = true;
}

function salvarAvatarSelecionado(url) {
  const { tipo, index } = quemEstaSelecionando.value;
  if (tipo === 'PAI' || tipo === 'PROFESSOR') {
    avatarProfessor.value = url;
  } else if (tipo === 'FILHO' || tipo === 'ALUNO') {
    alunos.value[index].avatar = url;
  }
  mostrarSeletorAvatar.value = false;
}

function adicionarAluno() {
  alunos.value.push({ 
    nome: '', genero: 'M', idade: '', 
    avatar: `https://api.dicebear.com/7.x/adventurer/svg?seed=${Date.now()}&backgroundColor=ffdfbf` 
  });
}

function removerAluno(index) {
  if (alunos.value.length > 1) {
    alunos.value.splice(index, 1);
  }
}

async function registrar() {
  erro.value = null;

  if (!aceitouTermos.value) {
    erro.value = "Você precisa ler e aceitar os Termos de Uso.";
    return;
  }

  const temTamanho = senha.value.length >= 8;
  const temMaiuscula = /[A-Z]/.test(senha.value);
  const temNumero = /[0-9]/.test(senha.value);
  const temEspecial = /[!@#$%^&*(),.?":{}|<>]/.test(senha.value);

  if (!temTamanho || !temMaiuscula || !temNumero || !temEspecial) {
    erro.value = 'A senha deve conter no mínimo 8 caracteres, uma letra maiúscula, um número e um caractere especial.';
    return;
  }
  
  if (senha.value !== confirmarSenha.value) { erro.value = "As senhas não coincidem!"; return; }
  if (pin.value.length !== 4) { erro.value = "O PIN deve ter 4 números."; return; }
  
  for (let i = 0; i < alunos.value.length; i++) {
    if (!alunos.value[i].nome || !alunos.value[i].idade) { erro.value = `Preencha os dados do ${i + 1}º aluno(a).`; return; }
  }

  isSubmitting.value = true;
  
  try {
    // 1. Cria Professor
    await api.post('/auth/register', {
      nome: nomeProfessor.value,
      email: email.value,
      senha: senha.value,
      pin: pin.value,
      avatarUrl: avatarProfessor.value
    });
    
    // 2. Loga para pegar o token
    const loginResponse = await api.post('/auth/login', {
      email: email.value,
      senha: senha.value
    });
    const token = loginResponse.data.token;

    // 3. Cria Alunos
    // Usamos um config específico aqui porque o localStorage ainda não foi atualizado
    const config = { headers: { Authorization: `Bearer ${token}` } };
    
    for (const aluno of alunos.value) {
      const anoNascimento = new Date().getFullYear() - aluno.idade;
      await api.post('/api/responsavel/dependentes', {
        nome: aluno.nome,
        dataNascimento: `${anoNascimento}-01-01`,
        avatarUrl: aluno.avatar 
      }, config);
    }
    
    alert('Turma/Professor cadastrado com sucesso! 🎉');
    router.push('/login');

  } catch (e) {
    erro.value = e.response?.data?.message || 'Erro ao criar conta. Verifique os dados.';
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#FFF9F4] py-10 px-4 font-nunito flex items-center justify-center relative overflow-hidden">
    
    <div class="absolute top-10 left-10 text-4xl animate-float-slow opacity-80 rotate-12 pointer-events-none">✨</div>
    <div class="absolute top-20 right-10 text-5xl animate-float-delayed opacity-90 -rotate-12 pointer-events-none">📚</div>
    <div class="absolute bottom-20 right-20 text-4xl animate-bounce-slow opacity-80 pointer-events-none">🏫</div>
    <div class="absolute bottom-10 left-20 text-4xl animate-float-slow opacity-80 rotate-45 pointer-events-none">🎨</div>

    <div class="bg-white p-8 rounded-[50px] shadow-[0_20px_50px_-12px_rgba(167,139,250,0.25)] w-full max-w-lg border-4 border-white relative z-10 transition-all">
      
      <div class="text-center mb-8">
        <h1 class="text-3xl font-extrabold text-[#A78BFA] mb-1 flex items-center justify-center gap-2" style="font-family: 'Nunito', sans-serif;">
          <span>👋</span> Cadastro Escolar
        </h1>
        <p class="text-gray-400 text-sm font-bold">Crie o acesso para o Professor e sua Turma</p>
      </div>

      <form @submit.prevent="registrar" class="space-y-6">
        
        <div class="bg-[#F9FAFB] p-6 rounded-[30px] border border-gray-100 shadow-inner">
          <h3 class="text-[#A78BFA] font-extrabold text-lg mb-6 flex items-center justify-center gap-2 border-b border-gray-200 pb-2">
            🧑‍🏫 Dados do Professor(a)
          </h3>
          
          <div class="flex flex-col items-center mb-6">
            <div class="cursor-pointer group relative" @click="abrirSeletor('PAI')">
              <div class="w-24 h-24 rounded-full border-4 border-[#C4B5FD] overflow-hidden shadow-md group-hover:scale-105 transition-transform bg-purple-50">
                <img :src="avatarProfessor" class="w-full h-full object-cover">
              </div>
              <div class="absolute -bottom-2 left-1/2 transform -translate-x-1/2 bg-[#A78BFA] text-white text-[10px] font-bold px-3 py-1 rounded-full shadow-sm group-hover:bg-[#8B5CF6] transition-colors">
                Mudar
              </div>
            </div>
          </div>

          <div class="space-y-4">
            <div>
              <label class="label-padrao">Nome Completo</label>
              <input type="text" v-model="nomeProfessor" required class="input-padrao" placeholder="Ex: Prof. Maria Silva">
            </div>
            <div>
              <label class="label-padrao">E-mail</label>
              <input type="email" v-model="email" required class="input-padrao" placeholder="email@escola.com">
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="relative">
                <label class="label-padrao">Senha</label>
                <div class="relative">
                  <input :type="mostrarSenha ? 'text' : 'password'" v-model="senha" required class="input-padrao pr-10" placeholder="******">
                  <button type="button" @click="mostrarSenha = !mostrarSenha" class="absolute right-3 top-3.5 text-gray-400 hover:text-[#A78BFA] outline-none">
                    <span v-if="!mostrarSenha">👁️</span><span v-else>🙈</span>
                  </button>
                </div>
              </div>
              
              <div class="relative">
                <label class="label-padrao">Confirmar</label>
                <div class="relative">
                  <input :type="mostrarConfirmar ? 'text' : 'password'" v-model="confirmarSenha" required class="input-padrao pr-10" placeholder="******">
                  <button type="button" @click="mostrarConfirmar = !mostrarConfirmar" class="absolute right-3 top-3.5 text-gray-400 hover:text-[#A78BFA] outline-none">
                    <span v-if="!mostrarConfirmar">👁️</span><span v-else>🙈</span>
                  </button>
                </div>
              </div>
            </div>
            
            <p class="text-[10px] text-gray-400 font-bold text-center -mt-2">
              🔒 Mínimo 8 caracteres, letra maiúscula, número e símbolo.
            </p>

            <div class="mt-4 pt-4 border-t border-gray-200">
              <label class="label-padrao text-center text-[#A78BFA] mb-2">PIN de Acesso (4 números)</label>
              <p class="text-[10px] text-gray-400 text-center font-bold mb-2">Para proteger a área do professor</p>
              <input type="text" v-model="pin" maxlength="4" required class="input-padrao text-center tracking-[0.5em] text-2xl font-extrabold text-[#A78BFA] bg-purple-50 focus:bg-white focus:border-[#A78BFA]" placeholder="0000" oninput="this.value = this.value.replace(/[^0-9]/g, '')">
            </div>
          </div>
        </div>

        <div class="bg-[#F0FDF4] p-6 rounded-[30px] border border-green-100 shadow-inner relative">
          <h3 class="text-green-600 font-extrabold text-lg mb-6 flex items-center justify-center gap-2 mt-2">
            🎓 Cadastro de Alunos
          </h3>

          <div v-for="(aluno, index) in alunos" :key="index" class="mb-8 pb-8 border-b border-green-200 last:border-0 last:pb-0 relative animate-fade-in">
            <button v-if="alunos.length > 1" type="button" @click="removerAluno(index)" class="absolute right-0 -top-2 text-red-400 hover:text-red-600 font-bold text-xs bg-red-50 px-2 py-1 rounded-lg transition-colors">Excluir</button>

            <div class="flex flex-col items-center mb-6">
              <div class="cursor-pointer group relative" @click="abrirSeletor('FILHO', index)">
                <div class="w-20 h-20 rounded-full border-4 border-green-300 overflow-hidden shadow-md group-hover:scale-105 transition-transform bg-green-50">
                  <img :src="aluno.avatar" class="w-full h-full object-cover">
                </div>
                <div class="absolute -bottom-2 left-1/2 transform -translate-x-1/2 bg-green-500 text-white text-[10px] font-bold px-3 py-1 rounded-full shadow-sm group-hover:bg-green-600 transition-colors">
                  Escolher
                </div>
              </div>
            </div>

            <div class="space-y-4">
              <div>
                <label class="label-padrao text-green-700">Nome do Aluno</label>
                <input type="text" v-model="aluno.nome" required class="input-filho text-center" placeholder="Ex: Pedrinho">
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="label-padrao text-green-700">Idade</label>
                  <input type="number" v-model="aluno.idade" required class="input-filho text-center" placeholder="Anos">
                </div>
                <div>
                  <label class="label-padrao text-green-700">Gênero</label>
                  <select v-model="aluno.genero" class="input-filho cursor-pointer bg-white text-center">
                    <option value="M">Menino</option>
                    <option value="F">Menina</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <button type="button" @click="adicionarAluno" class="w-full mt-2 py-3 border-2 border-dashed border-green-300 text-green-500 font-bold rounded-[20px] hover:bg-green-50 hover:border-green-500 hover:text-green-700 transition-all flex items-center justify-center gap-2">
            <span class="text-xl font-extrabold">+</span> Adicionar outro aluno(a)
          </button>
        </div>

        <div class="flex items-center gap-3 px-2">
          <input 
            type="checkbox" 
            id="termos" 
            v-model="aceitouTermos"
            class="w-5 h-5 rounded-md border-2 border-gray-300 text-[#A78BFA] focus:ring-[#A78BFA] cursor-pointer"
          >
          <label for="termos" class="text-sm font-bold text-gray-500 cursor-pointer select-none">
            Li e aceito os <span @click.prevent="mostrarModalTermos = true" class="text-[#A78BFA] underline hover:text-purple-700">Termos de Uso</span>.
          </label>
        </div>

        <div v-if="erro" class="text-red-500 text-sm text-center font-bold bg-red-50 p-3 rounded-2xl border border-red-100 animate-shake">{{ erro }}</div>

        <button type="submit" :disabled="isSubmitting" class="w-full bg-gradient-to-r from-[#C4B5FD] to-[#A78BFA] hover:to-[#8B5CF6] text-white font-extrabold py-4 rounded-[20px] shadow-lg shadow-purple-200 transform active:scale-[0.98] transition-all text-lg flex justify-center items-center gap-2 mt-6 disabled:opacity-50 disabled:cursor-not-allowed">
          <span v-if="isSubmitting" class="animate-spin">⏳</span>
          {{ isSubmitting ? 'Cadastrando...' : 'Finalizar Cadastro ✨' }}
        </button>
      </form>
      
      <div class="mt-8 text-center">
        <router-link to="/login" class="text-gray-400 hover:text-[#A78BFA] font-bold text-sm transition-colors px-4 py-2 hover:bg-purple-50 rounded-xl">
          Já tem conta? <span class="underline decoration-2">Voltar para Login</span>
        </router-link>
      </div>
    </div>

    <AvatarSelectorModal v-if="mostrarSeletorAvatar" @close="mostrarSeletorAvatar = false" @select="salvarAvatarSelecionado" />

    <div v-if="mostrarModalTermos" class="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div class="bg-white rounded-[30px] shadow-2xl w-full max-w-2xl max-h-[80vh] flex flex-col animate-fade-in overflow-hidden border-4 border-white">
        <div class="p-6 border-b border-gray-100 flex justify-between items-center bg-[#FFF9F4]">
          <h3 class="text-xl font-extrabold text-[#A78BFA]">📜 Termos de Uso (Escolar)</h3>
          <button @click="mostrarModalTermos = false" class="text-gray-400 hover:text-red-500 font-bold text-xl">✕</button>
        </div>
        
        <div class="p-8 overflow-y-auto text-gray-600 text-sm leading-relaxed space-y-4 font-nunito font-bold">
          <h4 class="font-black text-[#A78BFA]">1. Sobre o Cantinho do Saber</h4>
          <p>Este aplicativo tem fins educativos para auxiliar no monitoramento emocional dos alunos. Os dados são acessíveis pelo professor responsável.</p>
          
          <h4 class="font-black text-[#A78BFA]">2. Privacidade dos Dados</h4>
          <p>Garantimos que os diários e informações das crianças não são compartilhados com terceiros. O acesso é restrito ao professor que possui o PIN.</p>
          
          <p class="text-xs text-gray-400 italic mt-4">Última atualização: Dezembro de 2025.</p>
        </div>

        <div class="p-6 border-t border-gray-100 bg-[#FFF9F4] text-right">
          <button 
            @click="aceitouTermos = true; mostrarModalTermos = false" 
            class="bg-[#A78BFA] text-white font-bold px-8 py-3 rounded-[20px] hover:bg-purple-600 transition-all shadow-md"
          >
            Li e Concordo ✅
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.font-nunito { font-family: 'Nunito', sans-serif; }
.label-padrao { @apply block font-extrabold text-xs ml-1 mb-1 uppercase tracking-wide text-gray-400; }
.input-padrao { @apply w-full px-4 py-3 rounded-[20px] bg-white border-2 border-gray-200 focus:border-[#A78BFA] focus:bg-white focus:ring-0 outline-none transition-all font-bold text-gray-600 placeholder-gray-300; }
.input-filho { @apply w-full px-4 py-3 rounded-[20px] bg-white border-2 border-green-200 focus:border-green-400 focus:bg-white focus:ring-0 outline-none transition-all font-bold text-green-800 placeholder-green-200; }
input::-ms-reveal, input::-ms-clear { display: none; }

@keyframes floatSlow { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-15px); } }
.animate-float-slow { animation: floatSlow 4s ease-in-out infinite; }
@keyframes floatDelayed { 0%, 100% { transform: translateY(0px); } 50% { transform: translateY(-10px); } }
.animate-float-delayed { animation: floatDelayed 5s ease-in-out infinite; animation-delay: 1s; }
.animate-bounce-slow { animation: floatSlow 3s ease-in-out infinite; }
.animate-shake { animation: shake 0.3s ease-in-out; }
@keyframes shake { 0%, 100% { transform: translateX(0); } 25% { transform: translateX(-5px); } 75% { transform: translateX(5px); } }
.animate-fade-in { animation: fadeIn 0.3s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
</style>