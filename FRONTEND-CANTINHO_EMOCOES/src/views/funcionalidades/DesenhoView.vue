<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router'; 
import api from '@/services/api'; 
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute(); 
const authStore = useAuthStore();
const canvasRef = ref(null);
const containerRef = ref(null);
const ctx = ref(null);
const isDrawing = ref(false);
const enviando = ref(false);

// Configurações do Pincel
const corAtual = ref('#3B82F6'); 
const tamanhoPincel = ref(5);
const ferramentaAtual = ref('pincel');

// --- ATIVIDADE ---
const conteudoBase = ref(route.query.conteudo || ''); 
const tipoAtividade = ref(route.query.tipo || 'LIVRE');

// --- BANCO DE DESENHOS (PATHS SVG - OUTLINES PARA COLORIR) ---
// Agora inclui as chaves corretas (FELIZ, MACA, etc.) para corresponder ao envio do professor
const bancoDesenhos = {
    // 1. Formas Geométricas
    'Círculo': 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 Z',
    'Quadrado': 'M3 3 H21 V21 H3 Z',
    'Triângulo': 'M12 3 L22 21 H2 Z',
    'Retângulo': 'M2 6 H22 V18 H2 Z',
    'Estrela': 'M12 2 L15 9 L22 9 L17 14 L19 21 L12 17 L5 21 L7 14 L2 9 L9 9 Z',
    'Coração': 'M12 21.35 L10.55 20.03 C5.4 15.36 2 12.28 2 8.5 C2 5.42 4.42 3 7.5 3 C9.24 3 10.91 3.81 12 5.09 C13.09 3.81 14.76 3 16.5 3 C19.58 3 22 5.42 22 8.5 C22 12.28 18.6 15.36 13.45 20.04 L12 21.35 Z',
    'Losango': 'M12 2 L22 12 L12 22 L2 12 Z',
    'Pentágono': 'M12 2 L22 9 L18 21 H6 L2 9 Z',
    'Hexágono': 'M12 2 L21 7 L21 17 L12 22 L3 17 L3 7 Z',
    'Oval': 'M12 4 C6 4 2 7 2 12 C2 17 6 20 12 20 C18 20 22 17 22 12 C22 7 18 4 12 4 Z',
    'Trapézio': 'M5 18 L8 6 H16 L19 18 H5 Z',

    // 2. Emoções (Códigos em MAIÚSCULO enviados pelo professor)
    'FELIZ': 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M8 9 A1 1 0 1 1 8 9.01 M16 9 A1 1 0 1 1 16 9.01 M7 14 Q12 19 17 14',
    'TRISTE': 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M8 9 A1 1 0 1 1 8 9.01 M16 9 A1 1 0 1 1 16 9.01 M7 17 Q12 13 17 17',
    'BRAVO': 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M7 8 L9 10 M17 8 L15 10 M8 10 A1 1 0 1 1 8 10.01 M16 10 A1 1 0 1 1 16 10.01 M8 16 Q12 14 16 16',
    'CALMO': 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M7 10 Q9 8 11 10 M13 10 Q15 8 17 10 M8 15 H16',
    'MEDO': 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M8 9 A1 1 0 1 1 8 9.01 M16 9 A1 1 0 1 1 16 9.01 M10 14 A2 3 0 1 1 14 14 A2 3 0 1 1 10 14',
    'ANSIOSO': 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 M8 9 A1 1 0 1 1 8 9.01 M16 9 A1 1 0 1 1 16 9.01 M7 15 H17 M7 15 Q7 17 9 17 H15 Q17 17 17 15 M9 15 V17 M11 15 V17 M13 15 V17 M15 15 V17',

    // 3. Frutas (Códigos em MAIÚSCULO enviados pelo professor)
    'MACA': 'M12 21 C12 21 7 20 7 15 C7 11 10 9 12 11 C14 9 17 11 17 15 C17 20 12 21 12 21 Z M12 11 Q12 6 15 4 M12 11 L12 9',
    'BANANA': 'M6 18 Q4 10 14 4 Q18 4 18 6 Q16 10 18 18 Q16 20 6 18 M18 6 L20 4',
    'UVA': 'M12 6 A3 3 0 1 1 12 12 A3 3 0 1 1 12 6 M9 10 A3 3 0 1 1 9 16 A3 3 0 1 1 9 10 M15 10 A3 3 0 1 1 15 16 A3 3 0 1 1 15 10 M12 15 A3 3 0 1 1 12 21 A3 3 0 1 1 12 15 M12 6 L12 2 M12 2 L15 4',
    'LARANJA': 'M12 2 A10 10 0 1 1 12 22 A10 10 0 1 1 12 2 Z M12 2 V5 M4 10 L6 11 M20 10 L18 11 M8 20 L9 18',
    'MORANGO': 'M7 8 Q12 23 17 8 Q17 5 12 5 Q7 5 7 8 Z M6 8 L8 5 M18 8 L16 5 M12 5 V3',
    'ABACAXI': 'M7 10 Q7 22 12 22 Q17 22 17 10 Q17 8 12 8 Q7 8 7 10 Z M12 8 L10 2 M12 8 L14 2 M12 8 L12 1 M7 14 L17 18 M7 18 L17 14'
};

// Paleta de Cores
const cores = [
  '#EF4444', '#F59E0B', '#FCD34D', '#10B981', '#3B82F6', '#8B5CF6', '#EC4899', '#000000', '#8B4513'
];

const tamanhos = [
  { valor: 5, label: 'Fino' },
  { valor: 10, label: 'Médio' },
  { valor: 20, label: 'Grosso' }
];

onMounted(async () => {
  await nextTick();
  setTimeout(() => {
    inicializarCanvas();
    window.addEventListener('resize', redimensionarCanvas);
  }, 100);
});

onUnmounted(() => {
  window.removeEventListener('resize', redimensionarCanvas);
});

function inicializarCanvas() {
  if (!containerRef.value || !canvasRef.value) return;

  const canvas = canvasRef.value;
  const container = containerRef.value;

  canvas.width = container.clientWidth;
  canvas.height = container.clientHeight;
  
  ctx.value = canvas.getContext('2d', { willReadFrequently: true });
  ctx.value.lineCap = 'round';
  ctx.value.lineJoin = 'round';
  
  // Fundo branco inicial
  ctx.value.fillStyle = '#FFFFFF';
  ctx.value.fillRect(0, 0, canvas.width, canvas.height);
  
  desenharGuia();
  configurarEstilo();
}

function desenharGuia() {
    if (!conteudoBase.value || !ctx.value) return;

    const canvas = canvasRef.value;
    const conteudo = conteudoBase.value;
    
    // Tenta encontrar no banco de desenhos
    const pathSvg = bancoDesenhos[conteudo];

    ctx.value.save(); 
    
    // Configuração do traço guia (Tracejado cinza claro)
    ctx.value.strokeStyle = '#D1D5DB'; // Cinza claro
    ctx.value.lineWidth = 2;
    ctx.value.setLineDash([10, 10]); // Tracejado
    ctx.value.fillStyle = 'rgba(0,0,0,0)'; // Transparente TOTAL

    if (pathSvg) {
        // --- DESENHAR SVG (Forma, Emoção ou Fruta) ---
        const path = new Path2D(pathSvg);
        
        // Calcular escala para centralizar no canvas
        const padding = 40;
        const tamanhoMenorTela = Math.min(canvas.width, canvas.height);
        const tamanhoDesenho = tamanhoMenorTela - (padding * 2);
        const escala = tamanhoDesenho / 24; // SVGs são base 24px
        
        // Centralizar
        const offsetX = (canvas.width - (24 * escala)) / 2;
        const offsetY = (canvas.height - (24 * escala)) / 2;

        ctx.value.translate(offsetX, offsetY);
        ctx.value.scale(escala, escala);
        ctx.value.stroke(path);
        // IMPORTANTE: Não usamos fill() para garantir que fique "vazado" para colorir
    } else {
        // --- DESENHAR TEXTO (Letras/Números apenas se não achar desenho) ---
        const texto = conteudo.toUpperCase();
        const tamanhoFonte = Math.min(canvas.width, canvas.height) * 0.6; 
        
        ctx.value.font = `900 ${tamanhoFonte}px Nunito, sans-serif`;
        ctx.value.textAlign = 'center';
        ctx.value.textBaseline = 'middle';
        
        // Apenas o contorno (stroke)
        ctx.value.strokeText(texto, canvas.width / 2, canvas.height / 2);
    }

    ctx.value.restore(); 
}

function redimensionarCanvas() {
  if (canvasRef.value && containerRef.value) {
    const canvas = canvasRef.value;
    const container = containerRef.value;
    
    const tempImage = ctx.value.getImageData(0, 0, canvas.width, canvas.height);
    
    canvas.width = container.clientWidth;
    canvas.height = container.clientHeight;
    
    ctx.value.fillStyle = '#FFFFFF';
    ctx.value.fillRect(0, 0, canvas.width, canvas.height);
    
    desenharGuia(); 
    
    ctx.value.putImageData(tempImage, 0, 0);
    configurarEstilo();
  }
}

function configurarEstilo() {
  if (!ctx.value) return;
  if (ferramentaAtual.value === 'borracha') {
    ctx.value.strokeStyle = '#FFFFFF';
  } else {
    ctx.value.strokeStyle = corAtual.value;
  }
  ctx.value.lineWidth = tamanhoPincel.value;
  ctx.value.setLineDash([]); 
}

function getCoordenadas(event) {
  const canvas = canvasRef.value;
  const rect = canvas.getBoundingClientRect();
  let clienteX, clienteY;

  if (event.touches && event.touches.length > 0) {
    clienteX = event.touches[0].clientX;
    clienteY = event.touches[0].clientY;
  } else {
    clienteX = event.clientX;
    clienteY = event.clientY;
  }
  return { x: clienteX - rect.left, y: clienteY - rect.top };
}

function iniciarDesenho(event) {
  if (event.cancelable) event.preventDefault();
  isDrawing.value = true;
  const { x, y } = getCoordenadas(event);
  ctx.value.beginPath();
  ctx.value.moveTo(x, y);
}

function desenhar(event) {
  if (!isDrawing.value) return;
  if (event.cancelable) event.preventDefault();
  const { x, y } = getCoordenadas(event);
  ctx.value.lineTo(x, y);
  ctx.value.stroke();
}

function pararDesenho(event) {
  if (!isDrawing.value) return;
  if (event && event.cancelable) event.preventDefault();
  isDrawing.value = false;
  ctx.value.closePath();
}

function selecionarCor(cor) {
  ferramentaAtual.value = 'pincel';
  corAtual.value = cor;
  configurarEstilo();
}

function selecionarTamanho(tamanho) {
  tamanhoPincel.value = tamanho;
  configurarEstilo();
}

function ativarBorracha() {
  ferramentaAtual.value = 'borracha';
  configurarEstilo();
}

function limparTela(confirmar = true) {
  if (confirmar && !confirm('Começar de novo?')) return;
  const canvas = canvasRef.value;
  
  ctx.value.fillStyle = '#FFFFFF';
  ctx.value.fillRect(0, 0, canvas.width, canvas.height);
  
  desenharGuia();
  configurarEstilo(); 
}

// --- SALVAR NO BACKEND ---
async function salvarDesenho() {
  enviando.value = true;
  const canvas = canvasRef.value;
  const imagemBase64 = canvas.toDataURL('image/png');

  try {
    await api.post('/api/atividades', {
      tipo: tipoAtividade.value,
      conteudo: conteudoBase.value,
      desenhoBase64: imagemBase64
    }, {
      headers: { 
        'x-child-id': authStore.criancaSelecionada?.id
      }
    });

    if (conteudoBase.value) {
        alert(`Parabéns! Você completou a atividade! ⭐`);
    } else {
        alert("Que desenho lindo! Obrigado por compartilhar! 🎨");
    }

    router.push('/home'); 

  } catch (e) {
    console.error(e);
    alert("Erro ao enviar. Verifique a conexão.");
  } finally {
    enviando.value = false;
  }
}

function fecharDesenho() {
  router.push('/home'); 
}
</script>

<template>
  <div class="fixed inset-0 bg-fundo-inicio flex flex-col font-nunito overflow-hidden touch-none">
    
    <header class="bg-white p-3 shadow-sm flex items-center justify-between z-20 shrink-0 h-16">
      <div class="flex items-center gap-2">
        <button @click="fecharDesenho" class="bg-purple-100 text-purple-600 rounded-full w-10 h-10 font-bold hover:bg-purple-200 flex items-center justify-center">
          ✕
        </button>
        <h1 class="text-lg md:text-2xl font-extrabold text-roxo-titulo hidden md:block">
          🎨 {{ conteudoBase ? `Vamos pintar: ${conteudoBase}` : 'Desenho Livre' }}
        </h1>
      </div>
      
      <button 
        @click="salvarDesenho"
        :disabled="enviando"
        class="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-6 rounded-xl shadow-md transition-all active:scale-95 flex items-center gap-2"
      >
        <span v-if="enviando">🚀 Enviando...</span>
        <span v-else>✅ Terminei!</span>
      </button>
    </header>

    <div class="flex-1 flex flex-col-reverse md:flex-row h-[calc(100vh-64px)] relative overflow-hidden">
      
      <aside class="bg-white md:w-24 w-full p-2 md:p-4 flex md:flex-col flex-row items-center justify-between md:justify-start gap-3 shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.1)] md:shadow-md z-10 shrink-0 overflow-x-auto h-20 md:h-auto">
        
        <div class="flex md:flex-col flex-row gap-2 p-1 overflow-x-auto no-scrollbar">
          <button 
            v-for="cor in cores" 
            :key="cor"
            @click="selecionarCor(cor)"
            class="w-10 h-10 md:w-10 md:h-10 rounded-full border-2 shrink-0 transition-transform active:scale-90 shadow-sm"
            :style="{ backgroundColor: cor, borderColor: (corAtual === cor && ferramentaAtual === 'pincel') ? '#333' : 'transparent' }"
            :class="(corAtual === cor && ferramentaAtual === 'pincel') ? 'scale-110 ring-2 ring-gray-300' : ''"
          ></button>
        </div>

        <div class="w-px h-8 md:w-full md:h-px bg-gray-200 mx-1 md:mx-0"></div>

        <div class="flex md:flex-col flex-row gap-2 items-center">
          <button 
            v-for="t in tamanhos" 
            :key="t.valor"
            @click="selecionarTamanho(t.valor)"
            class="rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center shrink-0 transition-all active:scale-95"
            :class="tamanhoPincel === t.valor ? 'bg-purple-200 ring-2 ring-purple-400' : ''"
            style="width: 40px; height: 40px;"
          >
            <div class="bg-gray-700 rounded-full" :style="{ width: t.valor + 'px', height: t.valor + 'px' }"></div>
          </button>
        </div>

        <div class="w-px h-8 md:w-full md:h-px bg-gray-200 mx-1 md:mx-0"></div>

        <div class="flex md:flex-col flex-row gap-2">
          <button 
            @click="ativarBorracha"
            class="w-10 h-10 rounded-xl flex items-center justify-center text-2xl border-2 transition-all active:scale-95"
            :class="ferramentaAtual === 'borracha' ? 'bg-pink-100 border-pink-400 text-pink-600 scale-110' : 'bg-gray-50 border-transparent text-gray-400'"
          >🧼</button>
          
          <button 
            @click="limparTela(true)"
            class="w-10 h-10 rounded-xl bg-red-50 text-red-500 flex items-center justify-center text-xl hover:bg-red-100 active:scale-95 transition-all"
          >🗑️</button>
        </div>

      </aside>

      <main ref="containerRef" class="flex-1 bg-gray-200 relative cursor-crosshair overflow-hidden w-full h-full p-2 md:p-4 flex items-center justify-center">
        <canvas
          ref="canvasRef"
          class="block bg-white shadow-xl rounded-2xl touch-none"
          @mousedown="iniciarDesenho"
          @mousemove="desenhar"
          @mouseup="pararDesenho"
          @mouseleave="pararDesenho"
          @touchstart="iniciarDesenho"
          @touchmove="desenhar"
          @touchend="pararDesenho"
        ></canvas>
      </main>

    </div>
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>