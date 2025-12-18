<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import api from '@/services/api';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { Line, Bar } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale, BarElement, Filler } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, LineElement, PointElement, CategoryScale, LinearScale, BarElement, Filler);

const router = useRouter();
const authStore = useAuthStore();

// --- CATEGORIAS ---
const tiposAtividade = [
    { label: 'Vogal', value: 'VOGAL' },
    { label: 'Consoante', value: 'CONSOANTE' },
    { label: 'Números', value: 'NUMERO' },
    { label: 'Formas', value: 'FORMA' },
    { label: 'Emoções', value: 'EMOCAO' },
    { label: 'Frutas', value: 'FRUTA' },
    { label: 'Livre', value: 'LIVRE' }
];

// --- LISTA DE AVATARES ---
const listaAvatares = [
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Felix',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Aneka',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Bella',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Jack',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Coco',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Mittens',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Max',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Luna',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Zoey',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Buddy',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Oliver',
    'https://api.dicebear.com/7.x/adventurer/svg?seed=Molly'
];

// --- BANCO DE DADOS VISUAL ---
const listaFormas = [
    { label: 'Círculo', valor: 'Círculo', path: 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 Z' },
    { label: 'Quadrado', valor: 'Quadrado', path: 'M3 3 H21 V21 H3 Z' },
    { label: 'Triângulo', valor: 'Triângulo', path: 'M12 3 L22 21 H2 Z' },
    { label: 'Retângulo', valor: 'Retângulo', path: 'M2 6 H22 V18 H2 Z' },
    { label: 'Estrela', valor: 'Estrela', path: 'M12 2 L15 9 L22 9 L17 14 L19 21 L12 17 L5 21 L7 14 L2 9 L9 9 Z' },
    { label: 'Coração', valor: 'Coração', path: 'M12 21.35 L10.55 20.03 C5.4 15.36 2 12.28 2 8.5 C2 5.42 4.42 3 7.5 3 C9.24 3 10.91 3.81 12 5.09 C13.09 3.81 14.76 3 16.5 3 C19.58 3 22 5.42 22 8.5 C22 12.28 18.6 15.36 13.45 20.04 L12 21.35 Z' },
    { label: 'Losango', valor: 'Losango', path: 'M12 2 L22 12 L12 22 L2 12 Z' },
    { label: 'Pentágono', valor: 'Pentágono', path: 'M12 2 L22 9 L18 21 H6 L2 9 Z' },
    { label: 'Hexágono', valor: 'Hexágono', path: 'M12 2 L21 7 L21 17 L12 22 L3 17 L3 7 Z' },
    { label: 'Oval', valor: 'Oval', path: 'M12 4 C6 4 2 7 2 12 C2 17 6 20 12 20 C18 20 22 17 22 12 C22 7 18 4 12 4 Z' },
    { label: 'Trapézio', valor: 'Trapézio', path: 'M5 18 L8 6 H16 L19 18 H5 Z' }
];

const listaEmocoes = [
    { label: 'Feliz', valor: 'FELIZ', path: 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M8 9 A1 1 0 1 1 8 9.01 M16 9 A1 1 0 1 1 16 9.01 M7 14 Q12 19 17 14' },
    { label: 'Triste', valor: 'TRISTE', path: 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M8 9 A1 1 0 1 1 8 9.01 M16 9 A1 1 0 1 1 16 9.01 M7 17 Q12 13 17 17' },
    { label: 'Bravo', valor: 'BRAVO', path: 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M7 8 L9 10 M17 8 L15 10 M8 10 A1 1 0 1 1 8 10.01 M16 10 A1 1 0 1 1 16 10.01 M8 16 Q12 14 16 16' },
    { label: 'Calmo', valor: 'CALMO', path: 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M7 10 Q9 8 11 10 M13 10 Q15 8 17 10 M8 15 H16' },
    { label: 'Medo', valor: 'MEDO', path: 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M8 9 A1 1 0 1 1 8 9.01 M16 9 A1 1 0 1 1 16 9.01 M10 14 A2 3 0 1 1 14 14 A2 3 0 1 1 10 14' },
    { label: 'Ansioso', valor: 'ANSIOSO', path: 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M8 9 A1 1 0 1 1 8 9.01 M16 9 A1 1 0 1 1 16 9.01 M7 15 H17 M7 15 Q7 17 9 17 H15 Q17 17 17 15 M9 15 V17 M11 15 V17 M13 15 V17 M15 15 V17' }
];

const listaFrutas = [
    { label: 'Maçã', valor: 'MACA', path: 'M12 21 C12 21 7 20 7 15 C7 11 10 9 12 11 C14 9 17 11 17 15 C17 20 12 21 12 21 Z M12 11 Q12 6 15 4 M12 11 L12 9' },
    { label: 'Banana', valor: 'BANANA', path: 'M6 18 Q4 10 14 4 Q18 4 18 6 Q16 10 18 18 Q16 20 6 18 M18 6 L20 4' },
    { label: 'Uva', valor: 'UVA', path: 'M12 6 A3 3 0 1 1 12 12 A3 3 0 1 1 12 6 M9 10 A3 3 0 1 1 9 16 A3 3 0 1 1 9 10 M15 10 A3 3 0 1 1 15 16 A3 3 0 1 1 15 10 M12 15 A3 3 0 1 1 12 21 A3 3 0 1 1 12 15 M12 6 L12 2 M12 2 L15 4' },
    { label: 'Laranja', valor: 'LARANJA', path: 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 Z M12 2 V5 M4 10 L6 11 M20 10 L18 11 M8 20 L9 18' },
    { label: 'Morango', valor: 'MORANGO', path: 'M7 8 Q12 23 17 8 Q17 5 12 5 Q7 5 7 8 Z M6 8 L8 5 M18 8 L16 5 M12 5 V3' },
    { label: 'Abacaxi', valor: 'ABACAXI', path: 'M7 10 Q7 22 12 22 Q17 22 17 10 Q17 8 12 8 Q7 8 7 10 Z M12 8 L10 2 M12 8 L14 2 M12 8 L12 1 M7 14 L17 18 M7 18 L17 14' }
];

const emotionValueMap = { 'BRAVO': 1, 'MEDO': 2, 'TRISTE': 2, 'ANSIOSO': 3, 'CALMO': 4, 'CRIATIVO': 5, 'FELIZ': 6 };
const valueEmotionLabel = { 1: '😠 Bravo', 2: '😢 Triste', 3: '😬 Ansioso', 4: '😌 Calmo', 5: '🎨 Criativo', 6: '😊 Feliz' };
const emojiMap = { 'FELIZ': '😊', 'TRISTE': '😢', 'BRAVO': '😠', 'CALMO': '😌', 'MEDO': '😨', 'ANSIOSO': '😬', 'CRIATIVO': '🎨' };
const emocoesOpcoes = [
  { valor: 'FELIZ', emoji: '😊', label: 'Feliz' },
  { valor: 'CALMO', emoji: '😌', label: 'Calmo' },
  { valor: 'TRISTE', emoji: '😢', label: 'Triste' },
  { valor: 'BRAVO', emoji: '😠', label: 'Bravo' },
  { valor: 'ANSIOSO', emoji: '😬', label: 'Ansioso' },
  { valor: 'MEDO', emoji: '😨', label: 'Com Medo' }
];

// --- CONFIGURAÇÃO GRÁFICOS ---
const emojiPlugin = {
  id: 'emojiPlugin',
  afterDatasetsDraw(chart) {
    const { ctx } = chart;
    const meta = chart.getDatasetMeta(0);
    if (!meta.data || meta.data.length === 0) return;
    const emojis = chart.data.datasets[0].pointEmojis; 
    if(!emojis) return;
    ctx.save();
    meta.data.forEach((element, index) => {
      if (!emojis[index]) return;
      ctx.font = '20px Arial';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(emojis[index], element.x, element.y - 15);
    });
    ctx.restore();
  }
};

// --- ESTADOS REATIVOS ---
const viewAtual = ref('semanario'); 
const abaAlunoAtual = ref('rendimento'); 
const alunoSelecionado = ref(null);
const dependentes = ref([]);
const dadosTurma = ref([]); 
const abaMobile = ref('painel'); 
const termoBusca = ref(''); 

// FILTRO DE DATA (NOVO)
// Inicializa com os últimos 7 dias por padrão
const filtroDataInicio = ref(new Date(new Date().setDate(new Date().getDate() - 6)).toISOString().split('T')[0]);
const filtroDataFim = ref(new Date().toISOString().split('T')[0]);

// Form Semanário
const formSemanario = ref({ segunda: '', terca: '', quarta: '', quinta: '', sexta: '', objetivos: [] });
const salvandoSemanario = ref(false);
const objetivoTempDia = ref(''); 
const objetivoTempCategoria = ref('');
const objetivoTempDescricao = ref('');

// Form Diário
const novoDiario = ref({ emocao: null, intensidade: 3, relato: '' });
const salvandoDiario = ref(false);

// Dashboard
const dadosDashboard = ref(null);
const registrosDiario = ref([]); 
const listaAtividades = ref([]);
const carregandoDados = ref(false);
const carregandoGeral = ref(false);

// Avaliação
const templatesAvaliacao = ref({});
const subTabAvaliacao = ref('EF'); 
const unidadeSelecionada = ref("PADRAO"); 
const formAvaliacao = ref({});
const salvandoAvaliacao = ref(false);
const carregandoFicha = ref(false);
const showRelatorioModal = ref(false);

// Nova Atividade
const novaAtividade = ref({ tipo: 'VOGAL', conteudo: '' });
const enviandoAtividade = ref(false);
const alunosSelecionadosParaEnvio = ref([]); 
const selecionarTodos = ref(false); 
const totalAtividadesAtribuidasGlobal = ref(0); 

// Gerenciamento Aluno
const modoEdicao = ref(false);
const alunoEmEdicao = ref(null);
const cadastrandoAluno = ref(false);
const novoAluno = ref({ nome: '', dataNascimento: '', avatarUrl: listaAvatares[0] });

// Gerenciamento Professor
const showTeacherModal = ref(false);
const professorForm = ref({ nome: authStore.user?.name || '', avatarUrl: authStore.user?.avatarUrl || '' });
const salvandoProfessor = ref(false);

// --- HELPER DE DATA (CORREÇÃO DE ARRAY vs STRING) ---
function parseData(data) {
    if (!data) return new Date();
    // Verifica se é array do Java [ano, mes, dia, hora, min, seg]
    // Nota: O mês no JS é 0-indexado, mas no Java é 1-indexado
    if (Array.isArray(data)) {
        return new Date(data[0], data[1] - 1, data[2], data[3] || 0, data[4] || 0, data[5] || 0);
    }
    // Caso seja String ISO
    return new Date(data);
}

// --- FUNÇÃO PARA MOVER O PERÍODO DE DATA ---
function moverPeriodo(dias) {
    const inicio = new Date(filtroDataInicio.value);
    const fim = new Date(filtroDataFim.value);
    
    // Adiciona os dias (pode ser negativo para voltar)
    inicio.setDate(inicio.getDate() + dias);
    fim.setDate(fim.getDate() + dias);
    
    // Atualiza os inputs
    filtroDataInicio.value = inicio.toISOString().split('T')[0];
    filtroDataFim.value = fim.toISOString().split('T')[0];
}

// --- COMPUTEDS ---
const alunosFiltrados = computed(() => {
    if (!termoBusca.value) return dadosTurma.value;
    const termo = termoBusca.value.toLowerCase();
    return dadosTurma.value.filter(aluno => aluno.nome.toLowerCase().includes(termo));
});

const atividadesEscolares = computed(() => {
    if (!listaAtividades.value) return [];
    return listaAtividades.value
        .filter(a => a.tipo !== 'LIVRE')
        .sort((a, b) => parseData(b.dataRealizacao) - parseData(a.dataRealizacao));
});

const galeriaLivre = computed(() => {
    if (!listaAtividades.value) return [];
    return listaAtividades.value
        .filter(a => a.tipo === 'LIVRE')
        .sort((a, b) => parseData(b.dataRealizacao) - parseData(a.dataRealizacao));
});

const questoesDisponiveis = computed(() => {
    if (!objetivoTempCategoria.value || !templatesAvaliacao.value[objetivoTempCategoria.value]) return [];
    return templatesAvaliacao.value[objetivoTempCategoria.value].questoes;
});

const questoesFiltradas = computed(() => {
    const categoriaAtual = subTabAvaliacao.value;
    const template = templatesAvaliacao.value[categoriaAtual];
    if (!template) return {};
    const objetivosSemana = formSemanario.value.objetivos || [];
    const objetivosDestaCategoria = objetivosSemana.filter(obj => obj.categoria === categoriaAtual);
    if (objetivosDestaCategoria.length === 0) return {};
    const resultado = {};
    for (const [key, texto] of Object.entries(template.questoes)) {
        const estaNoSemanario = objetivosDestaCategoria.some(obj => (obj.id && obj.id === key) || obj.descricao === texto);
        if (estaNoSemanario) resultado[key] = texto;
    }
    return resultado;
});

// --- LIFECYCLE ---
onMounted(async () => {
  await carregarAlunos();
  await carregarTemplatesAvaliacao(); 
  await buscarTotalAtribuicoesGlobal(); 
  await carregarDadosGeraisTurma(); 
  await carregarSemanario();
});

watch(selecionarTodos, (val) => {
    if (val) {
        alunosSelecionadosParaEnvio.value = dependentes.value.map(a => a.id);
    } else {
        alunosSelecionadosParaEnvio.value = [];
    }
});

// --- API ACTIONS ---
async function carregarAlunos() {
  try {
    const response = await api.get('/api/responsavel/dependentes');
    dependentes.value = response.data;
  } catch (e) { console.error(e); }
}

async function carregarTemplatesAvaliacao() {
    try {
        const res = await api.get('/api/avaliacoes/templates');
        templatesAvaliacao.value = res.data;
    } catch (e) { console.error(e); }
}

async function buscarTotalAtribuicoesGlobal() {
    try {
        const res = await api.get('/api/atividades/total-enviadas-global'); 
        totalAtividadesAtribuidasGlobal.value = res.data.total || 0;
    } catch (e) {
        totalAtividadesAtribuidasGlobal.value = 0;
    }
}

async function carregarDadosGeraisTurma() {
    carregandoGeral.value = true;
    dadosTurma.value = [];
    try {
        const promises = dependentes.value.map(async (aluno) => {
            try {
                const totalAtribuidasRes = await api.get(`/api/atividades/total-atribuidas/${aluno.id}`);
                const totalAtribuidas = totalAtribuidasRes.data.total || 0;
                
                const ativRes = await api.get(`/api/atividades/aluno/${aluno.id}`);
                const atividadesRealizadas = ativRes.data; 
                
                const entregues = atividadesRealizadas.length;
                const pendentes = Math.max(0, totalAtribuidas - entregues); 

                return {
                    id: aluno.id,
                    nome: aluno.nome,
                    avatarUrl: aluno.avatarUrl,
                    atividadesFeitas: entregues,
                    atividadesPendentes: pendentes,
                    ultimaAtividade: atividadesRealizadas.length > 0 ? atividadesRealizadas[0].dataRealizacao : null
                };
            } catch (e) {
                 console.error(`Erro ao carregar dados do aluno ${aluno.id}:`, e);
                 return { id: aluno.id, nome: aluno.nome, avatarUrl: aluno.avatarUrl, atividadesFeitas: 0, atividadesPendentes: 0, ultimaAtividade: null };
            }
        });
        const resultados = await Promise.all(promises);
        dadosTurma.value = resultados.sort((a, b) => b.atividadesFeitas - a.atividadesFeitas);
    } catch (e) { console.error("Erro ao carregar dados gerais da turma:", e); }
    finally { carregandoGeral.value = false; }
}

async function carregarDadosAluno(id) {
    carregandoDados.value = true;
    try {
        const dashRes = await api.get(`/api/responsavel/dependentes/${id}/dashboard`);
        dadosDashboard.value = dashRes.data;
        registrosDiario.value = dashRes.data.ultimosRegistros || []; 
        
        const ativRes = await api.get(`/api/atividades/aluno/${id}`);
        listaAtividades.value = ativRes.data;
        
        if(abaAlunoAtual.value === 'avaliacao') {
             await buscarAvaliacaoSalva(false); 
        }
    } catch (e) { console.error(e); } finally { carregandoDados.value = false; }
}

async function excluirAtividade(atividadeId) {
    if (!confirm("Tem certeza que deseja apagar esta atividade do aluno?")) return;
    try {
        await api.delete(`/api/atividades/${atividadeId}`);
        alert("Atividade excluída.");
        if (alunoSelecionado.value) {
            await carregarDadosAluno(alunoSelecionado.value.id);
            await carregarDadosGeraisTurma(); 
        }
    } catch (e) {
        console.error("Erro ao excluir atividade:", e);
        alert("Erro ao excluir atividade.");
    }
}

async function salvarPerfilProfessor() {
    if(!professorForm.value.nome) return alert("O nome é obrigatório.");
    
    salvandoProfessor.value = true;
    
    try {
        await api.put('/auth/meu-perfil', { 
            name: professorForm.value.nome, 
            nome: professorForm.value.nome, 
            avatarUrl: professorForm.value.avatarUrl 
        });

        alert("Perfil atualizado com sucesso!");

    } catch (e) {
        console.error("O backend deu erro, mas vamos atualizar visualmente:", e);
        alert("Aviso: O servidor falhou (Erro 500), mas atualizei seu perfil localmente.");
    } finally {
        if(authStore.user) {
            authStore.user.name = professorForm.value.nome;
            authStore.user.avatarUrl = professorForm.value.avatarUrl;
            
            const userData = JSON.parse(localStorage.getItem('user') || '{}');
            userData.name = professorForm.value.nome;
            userData.avatarUrl = professorForm.value.avatarUrl;
            localStorage.setItem('user', JSON.stringify(userData));
        }

        showTeacherModal.value = false;
        salvandoProfessor.value = false;
    }
}

// --- FUNÇÕES DE ROTINA ---
function iniciarNovaSemana() {
    if(!confirm("Tem certeza que deseja LIMPAR TUDO para iniciar uma nova semana?")) return;
    formSemanario.value = { segunda: '', terca: '', quarta: '', quinta: '', sexta: '', objetivos: [] };
    salvarSemanario();
}

function adicionarObjetivo() {
    if (!objetivoTempDia.value) return alert("Selecione o Dia da Semana.");
    if (!objetivoTempCategoria.value || !objetivoTempDescricao.value) return alert("Selecione a categoria e a habilidade.");
    
    const textoQuestao = templatesAvaliacao.value[objetivoTempCategoria.value].questoes[objetivoTempDescricao.value];
    const tituloCategoria = templatesAvaliacao.value[objetivoTempCategoria.value].titulo.split('(')[0].trim(); 

    const jaExisteNaSemana = formSemanario.value.objetivos.some(o => 
        o.categoria === objetivoTempCategoria.value && 
        (o.id === objetivoTempDescricao.value || o.descricao === textoQuestao)
    );

    if (jaExisteNaSemana) return alert("Este objetivo já foi adicionado.");

    formSemanario.value.objetivos.push({
        id: objetivoTempDescricao.value, 
        categoria: objetivoTempCategoria.value,
        tituloCategoria: tituloCategoria,
        descricao: textoQuestao,
        dia: objetivoTempDia.value 
    });
    objetivoTempDescricao.value = ''; 
}

function removerObjetivo(objParaRemover) {
    const index = formSemanario.value.objetivos.indexOf(objParaRemover);
    if (index > -1) formSemanario.value.objetivos.splice(index, 1);
}

function getObjetivosPorDia(dia) {
    return formSemanario.value.objetivos.filter(o => o.dia === dia);
}

async function carregarSemanario() {
    try {
        const res = await api.get('/api/semanario/atual');
        if(res.data) {
            formSemanario.value = {
                segunda: res.data.segunda || '',
                terca: res.data.terca || '',
                quarta: res.data.quarta || '',
                quinta: res.data.quinta || '',
                sexta: res.data.sexta || '',
                objetivos: res.data.objetivos ? JSON.parse(res.data.objetivos) : []
            };
        }
    } catch (e) { console.error("Erro ao carregar semanário", e); }
}

async function salvarSemanario() {
    salvandoSemanario.value = true;
    try {
        const payload = { ...formSemanario.value, objetivos: JSON.stringify(formSemanario.value.objetivos) };
        await api.post('/api/semanario', payload);
        alert('Planejamento salvo!');
    } catch (e) { alert('Erro ao salvar semanário.'); } 
    finally { salvandoSemanario.value = false; }
}

async function salvarDiarioProfessor() {
    if (!novoDiario.value.emocao) return alert("Por favor, selecione uma emoção.");
    salvandoDiario.value = true;
    try {
        await api.post('/api/diario', {
            emocao: novoDiario.value.emocao,
            intensidade: novoDiario.value.intensidade,
            relato: novoDiario.value.relato
        }, { headers: { 'x-child-id': alunoSelecionado.value.id } });
        alert('Registro emocional salvo!');
        novoDiario.value = { emocao: null, intensidade: 3, relato: '' };
        await carregarDadosAluno(alunoSelecionado.value.id);
    } catch (e) { alert('Erro ao salvar registro.'); } 
    finally { salvandoDiario.value = false; }
}

// --- FUNÇÕES ALUNO ---
async function salvarAluno() {
    if (!novoAluno.value.nome) return alert("Preencha o nome!");
    if (!novoAluno.value.dataNascimento) return alert("Preencha a data de nascimento!");

    cadastrandoAluno.value = true;
    try {
        const payload = {
            nome: novoAluno.value.nome,
            dataNascimento: novoAluno.value.dataNascimento,
            avatarUrl: novoAluno.value.avatarUrl, 
            genero: 'M'
        };
        if (modoEdicao.value && alunoEmEdicao.value) {
            await api.put(`/api/responsavel/dependentes/${alunoEmEdicao.value.id}`, payload);
            alert("Aluno atualizado!");
        } else {
            await api.post('/api/responsavel/dependentes', payload);
            alert("Aluno cadastrado!");
        }
        novoAluno.value = { nome: '', dataNascimento: '', avatarUrl: listaAvatares[0] };
        modoEdicao.value = false;
        alunoEmEdicao.value = null;
        await carregarAlunos();
    } catch (e) { alert("Erro ao salvar."); } 
    finally { cadastrandoAluno.value = false; }
}

async function excluirAluno(id) {
    if(!confirm("Tem certeza? O histórico será apagado.")) return;
    try {
        await api.delete(`/api/responsavel/dependentes/${id}`);
        await carregarAlunos();
        if(alunoSelecionado.value && alunoSelecionado.value.id === id) verVisaoGeral();
    } catch (e) { alert("Erro ao excluir."); }
}

function prepararEdicao(aluno) {
    modoEdicao.value = true;
    alunoEmEdicao.value = aluno;
    novoAluno.value = { nome: aluno.nome, dataNascimento: aluno.dataNascimento, avatarUrl: aluno.avatarUrl };
}

function cancelarEdicao() {
    modoEdicao.value = false;
    alunoEmEdicao.value = null;
    novoAluno.value = { nome: '', dataNascimento: '', avatarUrl: listaAvatares[0] };
}

function gerarNovoAvatar() {
    novoAluno.value.avatarUrl = 'https://api.dicebear.com/7.x/adventurer/svg?seed=' + Date.now();
}

// --- GRÁFICOS E HELPERS ---
const dicaRendimentoGeral = computed(() => {
    if (dadosTurma.value.length === 0) return { texto: "Aguardando dados...", cor: "bg-gray-100 text-gray-500" };
    const totalEntregues = dadosTurma.value.reduce((acc, curr) => acc + curr.atividadesFeitas, 0);
    const totalAtribuicoesReal = dadosTurma.value.reduce((acc, curr) => acc + (curr.atividadesFeitas + curr.atividadesPendentes), 0);
    const proporcaoReal = totalAtribuicoesReal > 0 ? (totalEntregues / totalAtribuicoesReal) : 0;
    if (proporcaoReal > 0.6) return { texto: "Excelente! A turma está engajada.", cor: "bg-green-100 text-green-700", icon: "🌟" };
    if (proporcaoReal > 0.3) return { texto: "Bom progresso. Incentive mais.", cor: "bg-blue-100 text-blue-700", icon: "📈" };
    return { texto: "Atenção necessária. Engajamento baixo.", cor: "bg-orange-100 text-orange-700", icon: "⚠️" };
});

const chartDataTurma = computed(() => {
    return {
        labels: dadosTurma.value.map(d => d.nome),
        datasets: [
            { label: 'Entregues', data: dadosTurma.value.map(d => d.atividadesFeitas), backgroundColor: '#10B981', borderRadius: 4 },
            { label: 'Pendentes', data: dadosTurma.value.map(d => d.atividadesPendentes), backgroundColor: '#F59E0B', borderRadius: 4 }
        ]
    };
});

const chartOptionsTurma = { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'top' } }, scales: { x: { stacked: true, grid: { display: false } }, y: { beginAtZero: true, stacked: true } } };

// --- CORREÇÃO DO GRÁFICO DE ATIVIDADES PARA USAR FILTRO DE DATA ---
const chartDataAtividades = computed(() => {
    const atividadesPorData = {};
    const dias = [];
    
    // Converte strings de input para objetos Date
    // Adiciona "T00:00" para garantir o início do dia no fuso local
    const dataInicio = new Date(filtroDataInicio.value + 'T00:00:00');
    const dataFim = new Date(filtroDataFim.value + 'T23:59:59');
    
    // Loop para gerar todos os dias entre Início e Fim
    // Clona dataInicio para não alterar a original
    let loopData = new Date(dataInicio);
    
    while (loopData <= dataFim) {
        const key = loopData.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
        atividadesPorData[key] = 0;
        dias.push(key);
        // Avança 1 dia
        loopData.setDate(loopData.getDate() + 1);
    }
    
    if (listaAtividades.value) {
        listaAtividades.value.forEach(ativ => {
            const dataObj = parseData(ativ.dataRealizacao);
            if (!isNaN(dataObj)) {
                // Verifica se a data da atividade está dentro do intervalo
                if (dataObj >= dataInicio && dataObj <= dataFim) {
                    const d = dataObj.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' });
                    if (atividadesPorData[d] !== undefined) atividadesPorData[d]++;
                }
            }
        });
    }
    return {
        labels: dias,
        datasets: [{ label: 'Atividades Entregues', data: dias.map(d => atividadesPorData[d]), backgroundColor: '#10B981', borderRadius: 6, barThickness: 15 }]
    };
});

const chartDataEmocoes = computed(() => {
    if (!dadosDashboard.value?.historicoGrafico) return { labels: [], datasets: [] };
    const registros = dadosDashboard.value.historicoGrafico.filter(r => r.emocao !== 'CRIATIVO').slice(-10); 
    const emotionValues = registros.map(r => emotionValueMap[r.emocao] || 4); 
    const pointEmojis = registros.map(r => emojiMap[r.emocao] || '😐');
    return {
        labels: registros.map(r => parseData(r.dataRegistro).toLocaleDateString('pt-BR', {day:'2-digit', month:'2-digit'})),
        datasets: [{ label: 'Estado Emocional', data: emotionValues, borderColor: '#8B5CF6', backgroundColor: 'rgba(139, 92, 246, 0.1)', fill: true, tension: 0.4, pointRadius: 6, pointHoverRadius: 8, pointBackgroundColor: '#FFF', pointBorderColor: '#8B5CF6', pointBorderWidth: 2, pointEmojis: pointEmojis }]
    };
});

const chartOptionsEmocoes = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { min: 0, max: 7, grid: { display: true, borderDash: [5, 5] }, ticks: { callback: function(value) { return valueEmotionLabel[value] || ''; } } }, x: { grid: { display: false } } } };
const chartOptionsCommon = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true, grid: { display: false }, ticks: { stepSize: 1 } }, x: { grid: { display: false } } } };

// SUGESTÃO PEDAGÓGICA
const sugestaoPedagogica = computed(() => {
    const registros = dadosDashboard.value?.historicoGrafico || [];
    if (registros.length === 0) return null;
    const registrosOrdenados = [...registros].sort((a, b) => parseData(b.dataRegistro) - parseData(a.dataRegistro));
    const ultimaEmocao = registrosOrdenados[0].emocao;
    
    const mapaSugestoes = {
        'FELIZ': { titulo: 'Aluno Motivado!', texto: 'Que tal aproveitar essa energia para apresentar um novo desafio?', cor: 'bg-yellow-50 text-yellow-800 border-yellow-200' },
        'TRISTE': { titulo: 'Acolhimento Necessário', texto: 'Tente uma abordagem mais gentil e atividades lúdicas em grupo.', cor: 'bg-blue-50 text-blue-800 border-blue-200' },
        'BRAVO': { titulo: 'Gestão de Frustração', texto: 'Ofereça um momento de calma ou desenho livre para expressão.', cor: 'bg-red-50 text-red-800 border-red-200' },
        'CALMO': { titulo: 'Momento de Foco', texto: 'Ótimo momento para atividades de leitura e concentração.', cor: 'bg-green-50 text-green-800 border-green-200' },
        'MEDO': { titulo: 'Segurança Emocional', texto: 'Reforce a confiança do aluno com tarefas que ele domina.', cor: 'bg-purple-50 text-purple-800 border-purple-200' },
        'ANSIOSO': { titulo: 'Redução de Ansiedade', texto: 'Divida as tarefas em passos menores e respire junto com ele.', cor: 'bg-orange-50 text-orange-800 border-orange-200' },
        'CRIATIVO': { titulo: 'Potencial Criativo', texto: 'Explore atividades artísticas e resolução de problemas.', cor: 'bg-teal-50 text-teal-800 border-teal-200' }
    };
    return mapaSugestoes[ultimaEmocao] || { titulo: 'Observar', texto: 'Acompanhe o comportamento.', cor: 'bg-gray-50 text-gray-600 border-gray-200' };
});

async function buscarAvaliacaoSalva(force = true) {
    if (!force && Object.keys(formAvaliacao.value).length > 0) return;
    carregandoFicha.value = true;
    try {
        const res = await api.get('/api/avaliacoes/buscar', { params: { tipo: subTabAvaliacao.value, unidade: unidadeSelecionada.value }, headers: { 'x-child-id': alunoSelecionado.value.id } });
        if (res.data && res.data.respostas) formAvaliacao.value = res.data.respostas;
        else if (force) formAvaliacao.value = {}; 
    } catch (e) { console.error("Erro ao buscar ficha", e); } 
    finally { carregandoFicha.value = false; }
}

async function salvarAvaliacao() {
    salvandoAvaliacao.value = true;
    try {
        await api.post('/api/avaliacoes', { tipo: subTabAvaliacao.value, unidade: unidadeSelecionada.value, respostas: formAvaliacao.value }, { headers: { 'x-child-id': alunoSelecionado.value.id } });
        alert('Avaliação salva!');
    } catch(e) { alert('Erro ao salvar'); } 
    finally { salvandoAvaliacao.value = false; }
}

function marcarTodos(valor) {
    if(!questoesFiltradas.value) return;
    for (const key in questoesFiltradas.value) formAvaliacao.value[key] = valor;
}

function verVisaoGeral() {
    alunoSelecionado.value = null;
    viewAtual.value = 'geral';
    abaMobile.value = 'painel'; 
    buscarTotalAtribuicoesGlobal().then(() => carregarDadosGeraisTurma());
}

function verSemanario() {
    alunoSelecionado.value = null;
    viewAtual.value = 'semanario';
    abaMobile.value = 'painel';
    carregarSemanario();
}

function irParaGerenciar() {
    viewAtual.value = 'gerenciar';
    abaMobile.value = 'painel'; 
}

async function selecionarAluno(aluno) {
    if (alunoSelecionado.value && alunoSelecionado.value.id !== aluno.id) formAvaliacao.value = {};
    alunoSelecionado.value = aluno;
    viewAtual.value = 'aluno';
    abaAlunoAtual.value = 'rendimento';
    abaMobile.value = 'painel'; 
    await carregarDadosAluno(aluno.id);
}

async function enviarAtividade() {
    if (!novaAtividade.value.conteudo && novaAtividade.value.tipo !== 'LIVRE') return alert("Por favor, digite o conteúdo ou selecione uma opção.");
    if (alunosSelecionadosParaEnvio.value.length === 0) return alert("Selecione pelo menos um aluno para enviar a tarefa.");
    
    enviandoAtividade.value = true;
    try {
        await api.post('/api/atividades/definir-tarefa', { tipo: novaAtividade.value.tipo, conteudo: novaAtividade.value.conteudo || '', alunoIds: alunosSelecionadosParaEnvio.value });
        alert(`Atividade de ${novaAtividade.value.tipo} enviada para ${alunosSelecionadosParaEnvio.value.length} aluno(s)!`);
        novaAtividade.value = { tipo: 'VOGAL', conteudo: '' };
        alunosSelecionadosParaEnvio.value = [];
        selecionarTodos.value = false;
        await buscarTotalAtribuicoesGlobal();
        await carregarDadosGeraisTurma();
    } catch (e) { alert("Erro ao enviar atividade. Tente novamente."); } 
    finally { enviandoAtividade.value = false; }
}

function imprimirRelatorio() { window.print(); }
// Atualizado para usar parseData
function formatarData(data) { return parseData(data).toLocaleString('pt-BR'); }

watch([subTabAvaliacao, abaAlunoAtual], async () => {
    if (alunoSelecionado.value && abaAlunoAtual.value === 'avaliacao') {
        const deveForcar = Object.keys(formAvaliacao.value).length === 0;
        await buscarAvaliacaoSalva(deveForcar);
    }
});
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-nunito flex flex-col">
    <header class="bg-white border-b border-gray-200 px-4 md:px-6 py-4 flex justify-between items-center sticky top-0 z-30 shadow-sm print:hidden">
        <div class="flex items-center gap-3 md:gap-4">
            <div class="bg-indigo-600 text-white p-2 rounded-lg text-xl md:text-2xl shadow-md">👨‍🏫</div>
            <div>
                <h1 class="text-lg md:text-xl font-black text-gray-800 leading-none">Portal do Professor</h1>
                <p class="text-xs text-gray-500 font-bold" v-if="authStore.user">Olá, {{ authStore.user.name }}</p>
            </div>
        </div>
        <div class="flex gap-2">
            <button @click="showTeacherModal = true" class="px-3 py-2 rounded-xl font-bold text-xs bg-indigo-50 text-indigo-600 border border-indigo-100 hover:bg-indigo-100">✏️ Editar Perfil</button>
            <button @click="viewAtual = 'criar-atividade'" class="px-3 py-2 rounded-xl font-bold text-xs bg-white border text-gray-600">📤 Adicionar Atividade</button>
            <button @click="router.push('/login')" class="px-3 py-2 rounded-xl font-bold text-xs bg-red-50 text-red-500">Sair</button>
        </div>
    </header>

    <div class="md:hidden flex border-b border-gray-200 bg-white sticky top-[73px] z-20">
        <button @click="abaMobile = 'turma'" :class="['flex-1 py-3 text-center font-bold text-sm border-b-2', abaMobile === 'turma' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-500']">Minha Turma</button>
        <button @click="abaMobile = 'painel'" :class="['flex-1 py-3 text-center font-bold text-sm border-b-2', abaMobile === 'painel' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-500']">Painel Principal</button>
    </div>

    <div class="flex flex-1 overflow-hidden flex-col md:flex-row relative">
        <aside :class="['w-full md:w-72 bg-white border-r border-gray-200 flex-col overflow-y-auto z-10 print:hidden shrink-0 h-full absolute md:relative', abaMobile === 'turma' ? 'flex' : 'hidden md:flex']">
            <div class="p-4 border-b border-gray-100 space-y-2">
                <button @click="verSemanario" :class="['w-full py-3 rounded-xl font-black text-sm flex items-center justify-center gap-2 transition-all', viewAtual === 'semanario' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-indigo-50 text-indigo-700 hover:bg-indigo-100']"><span>📅</span> Semanário</button>
                <button @click="verVisaoGeral" :class="['w-full py-3 rounded-xl font-black text-sm flex items-center justify-center gap-2 transition-all', viewAtual === 'geral' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-indigo-50 text-indigo-700 hover:bg-indigo-100']"><span>📊</span> Rendimento Geral</button>
                <button @click="irParaGerenciar" :class="['w-full py-3 rounded-xl font-black text-sm flex items-center justify-center gap-2 transition-all', viewAtual === 'gerenciar' ? 'bg-indigo-600 text-white shadow-lg' : 'bg-indigo-50 text-indigo-700 hover:bg-indigo-100']"><span>⚙️</span> Gerenciar Turma</button>
            </div>
            <div class="p-4 border-b border-gray-100 bg-gray-50 md:bg-white sticky top-0">
                <div class="flex justify-between items-center mb-2">
                    <h2 class="text-xs font-black text-gray-400 uppercase">Ranking da Turma</h2>
                    <span class="text-[10px] bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded font-bold">🏆</span>
                </div>
                <input v-model="termoBusca" type="text" placeholder="🔍 Buscar aluno..." class="w-full px-3 py-2 text-xs rounded-lg border border-gray-300 focus:border-indigo-500 outline-none">
            </div>
            <div class="p-2 space-y-1 overflow-y-auto pb-20">
                <button v-for="(aluno, index) in alunosFiltrados" :key="aluno.id" @click="selecionarAluno(aluno)" :class="['w-full flex items-center gap-3 p-3 rounded-xl text-left border transition-all', alunoSelecionado?.id === aluno.id ? 'bg-indigo-50 border-indigo-200 shadow-sm' : 'border-transparent hover:bg-gray-50']">
                    <div :class="['w-6 h-6 flex items-center justify-center rounded-full text-xs font-black shrink-0', index === 0 ? 'bg-yellow-400 text-white' : 'bg-gray-100 text-gray-400']">{{ index + 1 }}</div>
                    <img :src="aluno.avatarUrl" class="w-8 h-8 rounded-full border border-gray-200 object-cover bg-gray-100">
                    <div class="flex-1 min-w-0">
                        <span class="font-bold text-sm text-gray-700 truncate block">{{ aluno.nome }}</span>
                        <div class="flex justify-between items-center text-[10px]">
                            <span class="text-green-600 font-bold">{{ aluno.atividadesFeitas }} OK</span>
                            <span v-if="aluno.atividadesPendentes > 0" class="text-orange-500 font-bold">{{ aluno.atividadesPendentes }} Pend.</span>
                        </div>
                    </div>
                </button>
                <div v-if="alunosFiltrados.length === 0" class="text-center p-4 text-xs text-gray-400">Nenhum aluno encontrado.</div>
            </div>
        </aside>

        <main :class="['flex-1 bg-[#F3F4F6] overflow-y-auto p-4 md:p-8 print:p-0 print:bg-white relative h-full', abaMobile === 'painel' ? 'block' : 'hidden md:block']">
            <div class="mobile-zoomed h-full">
                <div v-if="viewAtual === 'semanario'" class="max-w-4xl mx-auto animate-fade-in print:hidden">
                    <div class="bg-white p-6 md:p-8 rounded-3xl border border-gray-200 shadow-sm">
                        <div class="mb-6 border-b border-gray-100 pb-4 flex justify-between items-center">
                            <div><h2 class="text-2xl font-black text-indigo-600">Planejamento Semanal</h2><p class="text-gray-500 text-sm">Defina objetivos e descreva as atividades.</p></div>
                            <button @click="iniciarNovaSemana" class="px-4 py-2 bg-red-50 text-red-500 font-bold rounded-xl text-xs hover:bg-red-100">🗑️ Limpar Tudo (Nova Semana)</button>
                        </div>
                        <div class="mb-8 p-4 bg-indigo-50 rounded-2xl border border-indigo-100">
                            <h3 class="text-sm font-black text-indigo-800 mb-4 uppercase">1. Definir Objetivos de Avaliação</h3>
                            <div class="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
                                <div><label class="block text-xs font-bold text-gray-400 uppercase mb-1">Dia</label><select v-model="objetivoTempDia" class="w-full p-3 rounded-xl bg-white border border-gray-200 font-bold text-sm focus:border-indigo-500"><option value="">Selecione...</option><option value="segunda">Segunda</option><option value="terca">Terça</option><option value="quarta">Quarta</option><option value="quinta">Quinta</option><option value="sexta">Sexta</option></select></div>
                                <div class="md:col-span-1"><label class="block text-xs font-bold text-gray-400 uppercase mb-1">Categoria</label><select v-model="objetivoTempCategoria" class="w-full p-3 rounded-xl bg-white border border-gray-200 font-bold text-sm focus:border-indigo-500"><option value="">Selecione...</option><option v-for="(template, key) in templatesAvaliacao" :key="key" :value="key">{{ key }}</option></select></div>
                                <div class="md:col-span-1"><label class="block text-xs font-bold text-gray-400 uppercase mb-1">Objetivo / Habilidade</label><select v-model="objetivoTempDescricao" :disabled="!objetivoTempCategoria" class="w-full p-3 rounded-xl bg-white border border-gray-200 font-bold text-sm focus:border-indigo-500"><option value="">Selecione...</option><option v-for="(texto, id) in questoesDisponiveis" :key="id" :value="id">{{ id }} - {{ texto.substring(0, 30) }}...</option></select></div>
                                <div><button @click="adicionarObjetivo" class="w-full bg-indigo-600 text-white px-4 py-3 rounded-xl font-bold shadow hover:bg-indigo-700 transition-colors">+ Adicionar</button></div>
                            </div>
                        </div>
                        <div class="grid gap-8">
                            <div v-for="dia in ['segunda', 'terca', 'quarta', 'quinta', 'sexta']" :key="dia" class="relative">
                                <label class="block text-xs font-black text-gray-400 uppercase mb-2 ml-1">{{ dia.charAt(0).toUpperCase() + dia.slice(1) }}-Feira</label>
                                <textarea v-model="formSemanario[dia]" rows="3" class="w-full p-4 rounded-xl bg-gray-50 border-2 border-gray-100 focus:border-indigo-500 focus:bg-white transition-all font-bold text-gray-700 resize-none" placeholder="Digite o planejamento para este dia..."></textarea>
                                <div v-if="getObjetivosPorDia(dia).length > 0" class="mt-2 space-y-2 pl-4 border-l-4 border-indigo-200">
                                    <div v-for="(obj, index) in getObjetivosPorDia(dia)" :key="index" class="flex justify-between items-center bg-white p-2 rounded-lg border border-gray-100 shadow-sm text-sm">
                                        <div><span class="text-[10px] font-black uppercase text-indigo-500 block">{{ obj.categoria }}</span><span class="font-bold text-gray-700">{{ obj.descricao }}</span></div>
                                        <button @click="removerObjetivo(obj)" class="text-red-400 hover:text-red-600 font-bold text-xs ml-2 px-2 py-1 bg-red-50 rounded">Remover</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="mt-8 pt-6 border-t border-gray-100 flex justify-end">
                            <button @click="salvarSemanario" :disabled="salvandoSemanario" class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-4 px-8 rounded-xl shadow-lg transition-all flex items-center gap-2">
                                <span v-if="salvandoSemanario">Salvando...</span><span v-else>💾 Salvar Planejamento</span>
                            </button>
                        </div>
                    </div>
                </div>

                <div v-else-if="viewAtual === 'geral'" class="max-w-6xl mx-auto animate-fade-in print:hidden">
                    <div class="mb-8 flex justify-between items-end">
                        <div><h2 class="text-xl md:text-2xl font-black text-gray-800">Rendimento Geral</h2><p class="text-gray-500 font-bold text-xs">Total de Atribuições: <span class="text-indigo-600">{{ totalAtividadesAtribuidasGlobal }}</span></p></div>
                    </div>
                    <div :class="['p-4 md:p-6 rounded-3xl mb-8 flex items-center gap-4 shadow-sm border', dicaRendimentoGeral.cor]">
                        <div class="text-3xl">{{ dicaRendimentoGeral.icon }}</div>
                        <div><h4 class="font-black text-sm uppercase opacity-80">Análise</h4><p class="font-bold text-sm md:text-lg leading-tight">{{ dicaRendimentoGeral.texto }}</p></div>
                    </div>
                    <div class="bg-white p-4 md:p-6 rounded-3xl border border-gray-200 shadow-sm h-64 md:h-96 mb-8">
                        <div class="flex justify-between items-center mb-4"><h4 class="text-xs font-black text-gray-400 uppercase">Progresso da Turma (Tarefas Guiadas)</h4></div>
                        <Bar :data="chartDataTurma" :options="chartOptionsTurma" />
                    </div>
                </div>

                <div v-else-if="viewAtual === 'gerenciar'" class="max-w-4xl mx-auto animate-fade-in">
                      <h2 class="text-2xl font-black text-gray-800 mb-6">Gerenciar Alunos</h2>
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div class="md:col-span-1 bg-white p-6 rounded-3xl border border-gray-200 shadow-sm h-fit">
                            <h3 class="font-bold text-lg mb-4 text-indigo-600">{{ modoEdicao ? 'Editar' : 'Novo' }}</h3>
                            <div class="space-y-4">
                                <div><label class="block text-xs font-bold text-gray-400 uppercase mb-1">Nome</label><input v-model="novoAluno.nome" type="text" class="w-full p-3 bg-gray-50 rounded-xl border border-gray-200 font-bold"></div>
                                <div><label class="block text-xs font-bold text-gray-400 uppercase mb-1">Nascimento</label><input v-model="novoAluno.dataNascimento" type="date" class="w-full p-3 bg-gray-50 rounded-xl border border-gray-200 font-bold"></div>
                                
                                <div class="mt-4">
                                    <label class="block text-xs font-bold text-gray-400 uppercase mb-2">Selecione o Avatar</label>
                                    <div class="flex flex-wrap gap-2 justify-center max-h-32 overflow-y-auto p-2 border border-gray-100 rounded-xl">
                                        <button v-for="av in listaAvatares" :key="av" @click="novoAluno.avatarUrl = av" :class="['rounded-full p-1 border-2 transition-all', novoAluno.avatarUrl === av ? 'border-indigo-500 scale-110' : 'border-transparent']">
                                            <img :src="av" class="w-10 h-10 rounded-full bg-gray-100">
                                        </button>
                                    </div>
                                    <div class="text-center mt-2">
                                        <img :src="novoAluno.avatarUrl" class="w-20 h-20 rounded-full bg-gray-100 border-2 border-indigo-100 mx-auto">
                                    </div>
                                </div>

                                <button @click="salvarAluno" :disabled="cadastrandoAluno" class="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl shadow-md transition-all">{{ cadastrandoAluno ? 'Salvando...' : (modoEdicao ? 'Atualizar' : 'Cadastrar') }}</button>
                                <button v-if="modoEdicao" @click="cancelarEdicao" class="w-full py-2 text-gray-400 font-bold text-xs">Cancelar</button>
                            </div>
                        </div>
                        <div class="md:col-span-2 bg-white p-6 rounded-3xl border border-gray-200 shadow-sm">
                            <h3 class="font-bold text-lg mb-4 text-gray-700">Turma ({{ dependentes.length }})</h3>
                            <div class="space-y-2 max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
                                <div v-for="aluno in dependentes" :key="aluno.id" class="flex items-center justify-between p-3 rounded-xl border border-gray-100 hover:bg-gray-50">
                                    <div class="flex items-center gap-3"><img :src="aluno.avatarUrl" class="w-10 h-10 rounded-full border"><div><p class="font-bold text-gray-800 text-sm">{{ aluno.nome }}</p></div></div>
                                    <div class="flex gap-2"><button @click="prepararEdicao(aluno)" class="p-2 bg-blue-50 text-blue-600 rounded-lg">✏️</button><button @click="excluirAluno(aluno.id)" class="p-2 bg-red-50 text-red-600 rounded-lg">🗑️</button></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-else-if="viewAtual === 'criar-atividade'" class="max-w-4xl mx-auto animate-fade-in print:hidden">
                    <div class="flex flex-col md:flex-row gap-6">
                        <div class="flex-1 bg-white p-6 md:p-8 rounded-3xl shadow-sm border border-gray-200">
                            <h2 class="text-xl font-black text-gray-800 mb-6">Nova Tarefa para a Turma</h2>
                            <div class="grid grid-cols-2 md:grid-cols-3 gap-3 mb-6">
                                <div v-for="tipo in tiposAtividade" :key="tipo.value" @click="{ novaAtividade.tipo = tipo.value; novaAtividade.conteudo = ''; }" :class="['p-3 rounded-xl border-2 text-center font-bold text-xs cursor-pointer transition-all flex items-center justify-center h-16', novaAtividade.tipo === tipo.value ? 'border-indigo-500 bg-indigo-50 text-indigo-700' : 'border-gray-100 hover:border-gray-200 text-gray-500']">{{ tipo.label }}</div>
                            </div>
                            <div v-if="novaAtividade.tipo !== 'LIVRE'" class="mb-6">
                                <div v-if="['VOGAL', 'CONSOANTE', 'NUMERO'].includes(novaAtividade.tipo)">
                                    <label class="block text-xs font-bold text-gray-400 uppercase mb-2">Qual Letra ou Número?</label>
                                    <input v-model="novaAtividade.conteudo" type="text" maxlength="2" class="w-full p-4 rounded-xl bg-gray-50 border-2 font-black text-center text-2xl uppercase focus:border-indigo-500 focus:bg-white transition-all dashed-outline-text">
                                </div>
                                <div v-if="novaAtividade.tipo === 'FORMA'">
                                    <label class="block text-xs font-bold text-gray-400 uppercase mb-2">Escolha a Forma:</label>
                                    <div class="opcoes-visuais">
                                        <button v-for="forma in listaFormas" :key="forma.valor" @click="novaAtividade.conteudo = forma.valor" :class="['opcao-visual-btn', { 'ativo': novaAtividade.conteudo === forma.valor }]" type="button">
                                            <div class="icone-shape"><svg viewBox="0 0 24 24" class="shape-svg-dashed"><path :d="forma.path" /></svg></div><span class="text-xs font-bold mt-2">{{ forma.label }}</span>
                                        </button>
                                    </div>
                                </div>
                                <div v-if="novaAtividade.tipo === 'EMOCAO'">
                                    <label class="block text-xs font-bold text-gray-400 uppercase mb-2">Escolha a Emoção (Para Colorir):</label>
                                    <div class="opcoes-visuais">
                                        <button v-for="emocao in listaEmocoes" :key="emocao.valor" @click="novaAtividade.conteudo = emocao.valor" :class="['opcao-visual-btn', { 'ativo': novaAtividade.conteudo === emocao.valor }]" type="button">
                                            <div class="icone-shape"><svg viewBox="0 0 24 24" class="shape-svg-dashed"><path :d="emocao.path" /></svg></div><span class="text-xs font-bold mt-2">{{ emocao.label }}</span>
                                        </button>
                                    </div>
                                </div>
                                <div v-if="novaAtividade.tipo === 'FRUTA'">
                                    <label class="block text-xs font-bold text-gray-400 uppercase mb-2">Escolha a Fruta (Para Colorir):</label>
                                    <div class="opcoes-visuais">
                                        <button v-for="fruta in listaFrutas" :key="fruta.valor" @click="novaAtividade.conteudo = fruta.valor" :class="['opcao-visual-btn', { 'ativo': novaAtividade.conteudo === fruta.valor }]" type="button">
                                            <div class="icone-shape"><svg viewBox="0 0 24 24" class="shape-svg-dashed"><path :d="fruta.path" /></svg></div><span class="text-xs font-bold mt-2">{{ fruta.label }}</span>
                                        </button>
                                    </div>
                                </div>
                            </div>
                            <button @click="enviarAtividade" :disabled="enviandoAtividade" class="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-4 rounded-xl shadow-lg transition-all">{{ enviandoAtividade ? 'Enviando...' : `Enviar para ${alunosSelecionadosParaEnvio.length} Aluno(s)` }}</button>
                        </div>
                        <div class="w-full md:w-80 bg-white p-6 rounded-3xl shadow-sm border border-gray-200 flex flex-col h-auto md:h-[500px]">
                            <div class="flex justify-between items-center mb-4 border-b border-gray-100 pb-2"><h3 class="font-bold text-gray-700">Destinatários</h3><label class="flex items-center gap-2 cursor-pointer text-xs font-bold text-indigo-600 hover:text-indigo-800"><input type="checkbox" v-model="selecionarTodos" class="rounded text-indigo-600 focus:ring-indigo-500"> Todos</label></div>
                            <div class="flex-1 overflow-y-auto space-y-2 pr-2 custom-scrollbar max-h-60 md:max-h-full">
                                <label v-for="aluno in dependentes" :key="aluno.id" :class="['flex items-center gap-3 p-3 rounded-xl border-2 cursor-pointer transition-all', alunosSelecionadosParaEnvio.includes(aluno.id) ? 'border-indigo-500 bg-indigo-50' : 'border-transparent hover:bg-gray-50']">
                                    <input type="checkbox" :value="aluno.id" v-model="alunosSelecionadosParaEnvio" class="w-5 h-5 text-indigo-600 rounded focus:ring-indigo-500 border-gray-300">
                                    <img :src="aluno.avatarUrl" class="w-8 h-8 rounded-full bg-gray-200">
                                    <span class="text-sm font-bold text-gray-700">{{ aluno.nome }}</span>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-else-if="viewAtual === 'aluno' && alunoSelecionado" class="max-w-6xl mx-auto">
                    <div class="bg-white rounded-3xl p-4 shadow-sm border border-gray-200 mb-6 flex flex-col md:flex-row justify-between items-center gap-4 print:hidden">
                        <div class="flex items-center gap-4 w-full md:w-auto"><img :src="alunoSelecionado.avatarUrl" class="w-16 h-16 rounded-full border-4 border-indigo-50"><div><h2 class="text-2xl font-black text-gray-800">{{ alunoSelecionado.nome }}</h2></div></div>
                        <div class="flex bg-gray-100 p-1 rounded-xl w-full md:w-auto overflow-x-auto">
                            <button @click="abaAlunoAtual = 'rendimento'" :class="['flex-1 px-4 py-2 rounded-lg font-bold text-xs', abaAlunoAtual==='rendimento'?'bg-white shadow text-gray-800':'text-gray-500']">Rendimento</button>
                            <button @click="abaAlunoAtual = 'avaliacao'" :class="['flex-1 px-4 py-2 rounded-lg font-bold text-xs', abaAlunoAtual==='avaliacao'?'bg-white shadow text-gray-800':'text-gray-500']">Avaliação</button>
                            <button @click="abaAlunoAtual = 'diario'" :class="['flex-1 px-4 py-2 rounded-lg font-bold text-xs', abaAlunoAtual==='diario'?'bg-white shadow text-gray-800':'text-gray-500']">Diário</button>
                        </div>
                    </div>
                    
                    <div v-if="abaAlunoAtual === 'rendimento'" class="space-y-8 animate-fade-in print:hidden">
                        <div v-if="sugestaoPedagogica" :class="['p-4 rounded-2xl border mb-6 flex items-center gap-4 shadow-sm', sugestaoPedagogica.cor]">
                            <div class="text-2xl">💡</div><div><h4 class="font-bold text-sm uppercase opacity-80">Sugestão Pedagógica</h4><p class="font-bold">{{ sugestaoPedagogica.texto }}</p></div>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            
                            <div class="bg-white p-6 rounded-3xl border border-gray-200 shadow-sm h-auto md:h-96 flex flex-col">
                                <div class="flex flex-wrap items-center justify-between mb-4 gap-2">
                                    <h4 class="text-xs font-black text-gray-400 uppercase">Entregas</h4>
                                    <div class="flex items-center gap-2 bg-gray-50 p-1 rounded-lg border border-gray-200">
                                        <button @click="moverPeriodo(-7)" class="w-6 h-6 flex items-center justify-center rounded hover:bg-white hover:shadow-sm text-gray-500 font-bold" title="Voltar 7 dias">‹</button>
                                        <input type="date" v-model="filtroDataInicio" class="bg-transparent border-none text-[10px] font-bold text-gray-600 focus:ring-0 w-20">
                                        <span class="text-[10px] text-gray-400">até</span>
                                        <input type="date" v-model="filtroDataFim" class="bg-transparent border-none text-[10px] font-bold text-gray-600 focus:ring-0 w-20">
                                        <button @click="moverPeriodo(7)" class="w-6 h-6 flex items-center justify-center rounded hover:bg-white hover:shadow-sm text-gray-500 font-bold" title="Avançar 7 dias">›</button>
                                    </div>
                                </div>
                                <div class="flex-1 relative min-h-[200px]">
                                    <Bar :data="chartDataAtividades" :options="chartOptionsCommon" />
                                </div>
                            </div>

                            <div class="bg-white p-6 rounded-3xl border border-gray-200 shadow-sm h-64 md:h-96"><h4 class="text-xs font-black text-gray-400 uppercase mb-4">Oscilação Emocional (Valência)</h4><Line :data="chartDataEmocoes" :options="chartOptionsEmocoes" :plugins="[emojiPlugin]" /></div>
                        </div>
                        
                        <div>
                            <div class="flex items-center gap-2 mb-4"><span class="text-2xl">📝</span><h3 class="text-lg font-black text-gray-700">Tarefas da Escola</h3></div>
                            <div v-if="atividadesEscolares.length > 0" class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                                <div v-for="ativ in atividadesEscolares" :key="ativ.id" class="relative group bg-white p-3 rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition-all">
                                    <div class="aspect-square bg-gray-50 rounded-xl mb-3 overflow-hidden border border-gray-100"><img :src="ativ.desenhoBase64" class="w-full h-full object-contain"></div>
                                    <div class="flex justify-between items-center px-1"><span :class="['text-[10px] font-black px-2 py-1 rounded', ativ.tipo === 'LIVRE' ? 'bg-purple-100 text-purple-700' : 'bg-indigo-50 text-indigo-700']">{{ ativ.tipo }}: {{ ativ.conteudo }}</span><span class="text-[10px] text-gray-400 font-bold">{{ formatarData(ativ.dataRealizacao).split(' ')[0] }}</span></div>
                                    <button @click="excluirAtividade(ativ.id)" class="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full opacity-0 group-hover:opacity-100 transition-opacity" title="Apagar Atividade">🗑️</button>
                                </div>
                            </div>
                            <div v-else class="text-center py-10 bg-white rounded-3xl border border-dashed border-gray-300"><p class="text-gray-400 font-bold">Nenhuma atividade escolar entregue ainda.</p></div>
                        </div>

                        <div>
                            <div class="flex items-center gap-2 mb-4"><span class="text-2xl">🎨</span><h3 class="text-lg font-black text-gray-700">Galeria de Arte (Desenho Livre)</h3></div>
                            <div v-if="galeriaLivre.length > 0" class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                                <div v-for="ativ in galeriaLivre" :key="ativ.id" class="relative group bg-white p-3 rounded-2xl border border-gray-200 shadow-sm hover:shadow-md transition-all">
                                    <div class="aspect-square bg-gray-50 rounded-xl mb-3 overflow-hidden border border-gray-100"><img :src="ativ.desenhoBase64" class="w-full h-full object-contain"></div>
                                    <div class="flex justify-between items-center px-1"><span class="text-[10px] font-black px-2 py-1 rounded bg-yellow-50 text-yellow-700">ARTE</span><span class="text-[10px] text-gray-400 font-bold">{{ formatarData(ativ.dataRealizacao).split(' ')[0] }}</span></div>
                                    <button @click="excluirAtividade(ativ.id)" class="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full opacity-0 group-hover:opacity-100 transition-opacity" title="Apagar Atividade">🗑️</button>
                                </div>
                            </div>
                            <div v-else class="text-center py-10 bg-white rounded-3xl border border-dashed border-gray-300"><p class="text-gray-400 font-bold">Nenhum desenho livre extra encontrado.</p></div>
                        </div>
                    </div>

                    <div v-else-if="abaAlunoAtual === 'avaliacao'" class="bg-white p-8 rounded-3xl border border-gray-200 shadow-sm animate-fade-in print:border-0 print:shadow-none print:p-0">
                        <div class="hidden print:block mb-8 text-center border-b pb-4">
                            <h1 class="text-2xl font-bold text-gray-800">Relatório de Avaliação Individual</h1>
                            <p class="text-sm text-gray-600">Aluno: {{ alunoSelecionado.nome }} | Avaliação: {{ templatesAvaliacao[subTabAvaliacao]?.titulo }}</p>
                        </div>
                        <div class="print:hidden">
                            <div class="flex gap-2 overflow-x-auto pb-4 mb-4">
                                <button v-for="(template, key) in templatesAvaliacao" :key="key" @click="subTabAvaliacao = key" :class="['px-6 py-3 rounded-xl font-bold text-xs transition-all border-2', subTabAvaliacao === key ? 'bg-indigo-600 text-white border-indigo-600 shadow-md' : 'bg-white text-gray-500 border-gray-100 hover:border-gray-200']">
                                    <span class="block text-lg">{{ key }}</span>
                                    <span class="text-[10px] opacity-80 whitespace-nowrap">{{ template.titulo.split('(')[0] }}</span>
                                </button>
                            </div>
                        </div>
                        <div v-if="carregandoFicha" class="text-center py-10 opacity-50 font-bold">Carregando ficha...</div>
                        <div v-else-if="Object.keys(questoesFiltradas).length > 0" class="space-y-0 bg-gray-50 rounded-2xl overflow-hidden border border-gray-200 print:bg-white print:border-gray-800">
                             <div class="p-4 bg-gray-100 border-b border-gray-300 print:bg-white flex justify-between items-center">
                                 <h3 class="font-black text-gray-800">{{ templatesAvaliacao[subTabAvaliacao].titulo }}</h3>
                                 <div class="flex gap-2 print:hidden">
                                    <button @click="marcarTodos('S')" class="text-[10px] bg-green-100 text-green-700 px-2 py-1 rounded font-bold hover:bg-green-200">Todos SIM</button>
                                    <button @click="marcarTodos('N')" class="text-[10px] bg-red-100 text-red-700 px-2 py-1 rounded font-bold hover:bg-red-200">Todos NÃO</button>
                                    <button @click="marcarTodos('NA')" class="text-[10px] bg-gray-200 text-gray-700 px-2 py-1 rounded font-bold hover:bg-gray-300">Todos N/A</button>
                                 </div>
                             </div>
                             <div v-for="(texto, num) in questoesFiltradas" :key="num" class="flex items-center justify-between p-4 border-b border-gray-200 bg-white hover:bg-indigo-50/30 transition-colors">
                                <div class="flex gap-4 pr-4"><span class="font-black text-gray-300 w-6">{{ num }}</span><p class="text-sm font-bold text-gray-600 leading-snug">{{ texto }}</p></div>
                                <div class="flex gap-2 shrink-0">
                                    <label class="cursor-pointer flex flex-col items-center group"><input type="radio" :name="'q'+num" value="S" v-model="formAvaliacao[num]" class="peer sr-only"><div class="w-8 h-8 rounded border-2 border-gray-200 bg-white peer-checked:bg-green-500 peer-checked:border-green-500 flex items-center justify-center transition-all shadow-sm"><span class="text-white text-sm font-bold hidden peer-checked:block">✓</span></div><span class="text-[9px] font-bold text-gray-300 mt-1 peer-checked:text-green-500 print:hidden">SIM</span></label>
                                    <label class="cursor-pointer flex flex-col items-center group"><input type="radio" :name="'q'+num" value="N" v-model="formAvaliacao[num]" class="peer sr-only"><div class="w-8 h-8 rounded border-2 border-gray-200 bg-white peer-checked:bg-red-500 peer-checked:border-red-500 flex items-center justify-center transition-all shadow-sm"><span class="text-white text-sm font-bold hidden peer-checked:block">✕</span></div><span class="text-[9px] font-bold text-gray-300 mt-1 peer-checked:text-red-500 print:hidden">NÃO</span></label>
                                    <label class="cursor-pointer flex flex-col items-center group"><input type="radio" :name="'q'+num" value="NA" v-model="formAvaliacao[num]" class="peer sr-only"><div class="w-8 h-8 rounded border-2 border-gray-200 bg-white peer-checked:bg-gray-400 peer-checked:border-gray-400 flex items-center justify-center transition-all shadow-sm"><span class="text-white text-sm font-bold hidden peer-checked:block">-</span></div><span class="text-[9px] font-bold text-gray-300 mt-1 peer-checked:text-gray-500 print:hidden">N/A</span></label>
                                </div>
                             </div>
                        </div>
                        <div v-else class="text-center py-10 bg-gray-50 rounded-2xl border border-dashed border-gray-300">
                             <p class="text-gray-400 font-bold mb-2">Nenhum objetivo definido para esta categoria no Semanário.</p>
                             <button @click="verSemanario" class="text-indigo-600 text-xs font-bold hover:underline">Ir para Semanário e adicionar objetivos</button>
                        </div>
                        <div class="mt-6 flex gap-4 print:hidden">
                            <button @click="salvarAvaliacao" :disabled="salvandoAvaliacao" class="flex-1 bg-gray-900 hover:bg-black text-white font-bold py-4 rounded-xl shadow-lg transition-all active:scale-95 flex items-center justify-center gap-2">
                                <span v-if="salvandoAvaliacao">Salvando...</span><span v-else>💾 Salvar Avaliação</span>
                            </button>
                            <button @click="showRelatorioModal = true" class="px-6 py-4 bg-white border-2 border-gray-200 text-gray-600 font-bold rounded-xl hover:bg-gray-50">📄 Imprimir</button>
                        </div>
                    </div>

                    <div v-else-if="abaAlunoAtual === 'diario'" class="bg-white p-6 rounded-3xl border border-gray-200 shadow-sm animate-fade-in print:hidden">
                          <div class="flex items-center justify-between mb-6"><div class="flex items-center gap-2"><span class="text-2xl">📝</span><h3 class="text-lg font-black text-gray-700">Registrar Diário Emocional</h3></div></div>
                          <div class="bg-indigo-50/50 p-6 rounded-2xl border border-indigo-100 mb-8">
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                                <div>
                                    <label class="block text-xs font-bold text-gray-500 uppercase mb-3">Como o aluno está se sentindo?</label>
                                    <div class="flex flex-wrap gap-2">
                                        <button v-for="emo in emocoesOpcoes" :key="emo.valor" @click="novoDiario.emocao = emo.valor" :class="['flex-1 min-w-[80px] py-3 rounded-xl border-2 flex flex-col items-center gap-1 transition-all', novoDiario.emocao === emo.valor ? 'bg-white border-indigo-500 shadow-md transform scale-105' : 'bg-white border-gray-200 hover:border-indigo-200 opacity-80']">
                                            <span class="text-2xl">{{ emo.emoji }}</span><span :class="['text-[10px] font-bold uppercase', novoDiario.emocao === emo.valor ? 'text-indigo-600' : 'text-gray-400']">{{ emo.label }}</span>
                                        </button>
                                    </div>
                                </div>
                                <div>
                                    <label class="block text-xs font-bold text-gray-500 uppercase mb-3">Intensidade (1 a 5)</label>
                                    <div class="flex gap-2"><button v-for="n in 5" :key="n" @click="novoDiario.intensidade = n" :class="['w-10 h-10 rounded-full font-bold border-2 transition-all', novoDiario.intensidade === n ? 'bg-indigo-600 text-white border-indigo-600' : 'bg-white text-gray-400 border-gray-200']">{{ n }}</button></div>
                                    <div class="mt-4"><label class="block text-xs font-bold text-gray-500 uppercase mb-2">Observações / Relato</label><textarea v-model="novoDiario.relato" rows="3" class="w-full p-3 rounded-xl bg-white border border-gray-200 focus:border-indigo-500 transition-all font-bold text-gray-600 text-sm resize-none" placeholder="Descreva o motivo ou observações importantes..."></textarea></div>
                                </div>
                            </div>
                            <div class="flex justify-end"><button @click="salvarDiarioProfessor" :disabled="salvandoDiario" class="bg-indigo-600 text-white font-bold py-3 px-8 rounded-xl shadow-md hover:bg-indigo-700 transition-all flex items-center gap-2"><span v-if="salvandoDiario">Salvando...</span><span v-else>✅ Registrar no Diário</span></button></div>
                         </div>
                         <h4 class="text-xs font-black text-gray-400 uppercase mb-4 pl-1">Histórico de Registros</h4>
                         <div v-if="registrosDiario.length > 0" class="space-y-4">
                            <div v-for="reg in registrosDiario" :key="reg.id" class="flex items-start gap-4 p-4 rounded-2xl bg-white border border-gray-100 hover:shadow-sm transition-all">
                                <div class="text-3xl bg-gray-50 w-12 h-12 flex items-center justify-center rounded-xl border border-gray-100">{{ emojiMap[reg.emocao] || '😐' }}</div>
                                <div class="flex-1">
                                    <div class="flex justify-between items-start"><h4 class="font-bold text-gray-800 text-sm uppercase">{{ reg.emocao }} <span class="text-gray-400 text-xs font-normal ml-2">Nível {{ reg.intensidade }}/5</span></h4><span class="text-[10px] font-bold text-gray-400 bg-gray-50 px-2 py-1 rounded border border-gray-100">{{ formatarData(reg.dataRegistro) }}</span></div>
                                    <p v-if="reg.relato" class="text-sm text-gray-600 mt-2 italic">"{{ reg.relato }}"</p><p v-else class="text-xs text-gray-300 mt-2 italic">(Sem observações)</p>
                                </div>
                            </div>
                         </div>
                         <div v-else class="text-center py-10 bg-gray-50 rounded-2xl border border-dashed border-gray-200"><p class="text-gray-400 font-bold">Nenhum registro emocional realizado.</p></div>
                    </div>

                </div>
            </div>
        </main>
    </div>
    
    <div v-if="showRelatorioModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm print:hidden">
        <div class="bg-white w-full max-w-3xl h-[80vh] rounded-2xl shadow-2xl flex flex-col overflow-hidden">
            <div class="p-6 border-b flex justify-between items-center bg-gray-50 relative"><h3 class="font-bold text-lg text-gray-800">Visualizar Relatório</h3><button @click="showRelatorioModal = false" class="text-gray-400 hover:text-red-500 text-2xl absolute right-6">✕</button></div>
            <div class="flex-1 overflow-y-auto p-8 bg-white" id="area-impressao">
                <div class="border-2 border-gray-800 p-8 mb-4">
                    <div class="flex justify-between items-center border-b-2 border-gray-800 pb-4 mb-6">
                        <div><h1 class="text-2xl font-black text-gray-900 uppercase">Ficha de Avaliação</h1><p class="text-sm font-bold text-gray-600">Educação Infantil</p></div>
                        <div class="text-right text-sm"><p><strong>Aluno:</strong> {{ alunoSelecionado?.nome }}</p><p><strong>Tipo:</strong> {{ templatesAvaliacao[subTabAvaliacao]?.titulo }}</p></div>
                    </div>
                    <table class="w-full text-sm">
                        <thead><tr class="bg-gray-100"><th class="p-2 text-left border">Critério</th><th class="p-2 w-16 text-center border">SIM</th><th class="p-2 w-16 text-center border">NÃO</th><th class="p-2 w-16 text-center border">N/A</th></tr></thead>
                        <tbody>
                            <tr v-for="(texto, num) in questoesFiltradas" :key="num">
                                <td class="p-2 border">{{ num }}. {{ texto }}</td>
                                <td class="p-2 border text-center font-bold text-green-600">{{ formAvaliacao[num] === 'S' ? 'X' : '' }}</td>
                                <td class="p-2 border text-center font-bold text-red-600">{{ formAvaliacao[num] === 'N' ? 'X' : '' }}</td>
                                <td class="p-2 border text-center font-bold text-gray-400">{{ formAvaliacao[num] === 'NA' ? 'X' : '' }}</td>
                            </tr>
                            <tr v-if="Object.keys(questoesFiltradas).length === 0">
                                <td colspan="4" class="p-4 text-center text-gray-400 italic">Nenhum objetivo definido para esta categoria nesta semana.</td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="mt-8 pt-4 border-t-2 border-gray-800 flex justify-between"><div class="text-xs">Data: {{ new Date().toLocaleDateString() }}</div><div class="text-xs">Assinatura do Professor: __________________________</div></div>
                </div>
            </div>
            <div class="p-4 border-t bg-gray-50 flex justify-end gap-3"><button @click="imprimirRelatorio" class="px-6 py-2 bg-indigo-600 text-white font-bold rounded-lg hover:bg-indigo-700">🖨️ Imprimir</button><button @click="showRelatorioModal = false" class="px-6 py-2 bg-gray-200 text-gray-700 font-bold rounded-lg hover:bg-gray-300">Fechar</button></div>
        </div>
    </div>

    <div v-if="showTeacherModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm print:hidden">
        <div class="bg-white w-full max-w-md rounded-2xl shadow-2xl overflow-hidden p-6">
            <h3 class="text-xl font-black text-gray-800 mb-4">Editar Meu Perfil</h3>
            <div class="space-y-4">
                <div>
                    <label class="block text-xs font-bold text-gray-400 uppercase mb-1">Nome de Exibição</label>
                    <input v-model="professorForm.nome" type="text" class="w-full p-3 bg-gray-50 rounded-xl border border-gray-200 font-bold">
                </div>
                
                <div>
                    <label class="block text-xs font-bold text-gray-400 uppercase mb-2">Selecione seu Avatar</label>
                    <div class="flex flex-wrap gap-2 justify-center max-h-40 overflow-y-auto p-2 border border-gray-100 rounded-xl custom-scrollbar">
                        <button v-for="av in listaAvatares" :key="av" @click="professorForm.avatarUrl = av" :class="['rounded-full p-1 border-2 transition-all', professorForm.avatarUrl === av ? 'border-indigo-500 scale-110' : 'border-transparent']">
                            <img :src="av" class="w-12 h-12 rounded-full bg-gray-100 object-cover">
                        </button>
                    </div>
                    <div class="text-center mt-2">
                        <span class="text-xs text-gray-400 font-bold">Atual:</span>
                        <img :src="professorForm.avatarUrl" class="w-16 h-16 rounded-full bg-gray-100 border-2 border-indigo-100 mx-auto mt-1 object-cover">
                    </div>
                </div>

                <div class="flex justify-end gap-2 mt-4">
                    <button @click="showTeacherModal = false" class="px-4 py-2 text-gray-500 font-bold hover:text-gray-700">Cancelar</button>
                    <button @click="salvarPerfilProfessor" :disabled="salvandoProfessor" class="px-6 py-2 bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-700 shadow-md">
                        {{ salvandoProfessor ? 'Salvando...' : 'Salvar Alterações' }}
                    </button>
                </div>
            </div>
        </div>
    </div>

  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;700;900&display=swap');
.font-nunito { font-family: 'Nunito', sans-serif; }
.animate-fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
@media print {
    body * { visibility: hidden; }
    #area-impressao, #area-impressao * { visibility: visible; }
    #area-impressao { position: absolute; left: 0; top: 0; width: 100%; margin: 0; padding: 0; }
    .print\:hidden { display: none !important; }
}
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: #f1f1f1; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #c7c7c7; border-radius: 3px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #a8a8a8; }

@media (max-width: 768px) {
  .mobile-zoomed {
    zoom: 0.8;
  }
}

.opcoes-visuais {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    justify-content: center;
}
.opcao-visual-btn {
    background: #ffffff;
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    padding: 10px;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 90px;
    height: 90px;
    transition: all 0.2s ease;
    color: #6b7280;
}
.opcao-visual-btn:hover {
    border-color: #d1d5db;
    background: #f9fafb;
}
.opcao-visual-btn.ativo {
    border-color: #6366f1;
    background: #eef2ff;
    color: #4338ca;
    box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.1);
}

.icone-shape {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.shape-svg-dashed {
    width: 32px; height: 32px;
    fill: none;
    stroke: currentColor;
    stroke-width: 2;
    stroke-dasharray: 4, 3;
}

.dashed-outline-text {
    color: transparent;
    -webkit-text-stroke: 1px #374151;
    text-align: center;
    font-size: 3rem;
}
</style>