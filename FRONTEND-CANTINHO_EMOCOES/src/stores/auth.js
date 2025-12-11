import { defineStore } from 'pinia';
import axios from 'axios';
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

export const useAuthStore = defineStore('auth', () => {
    const router = useRouter();

    // Estado
    const user = ref(JSON.parse(localStorage.getItem('user')) || null);
    const token = ref(localStorage.getItem('token') || null);
    const criancaSelecionada = ref(JSON.parse(localStorage.getItem('crianca')) || null);

    // Getters
    const isAuthenticated = computed(() => !!token.value);
    // Verifica se o perfil é RESPONSAVEL (ajustado para bater com o enum do Java)
    const isResponsavel = computed(() => user.value?.perfil === 'RESPONSAVEL');
    
    // Actions
    function setLoginData(userData, tokenData) {
        user.value = userData;
        token.value = tokenData;
        
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('token', tokenData);
        
        // Configura o cabeçalho padrão
        axios.defaults.headers.common['Authorization'] = `Bearer ${tokenData}`;
    }

    function selecionarCrianca(crianca) {
        criancaSelecionada.value = crianca;
        localStorage.setItem('crianca', JSON.stringify(crianca));
    }

    // Função essencial para o interceptor de segurança
    function clearLoginData() {
        user.value = null;
        token.value = null;
        criancaSelecionada.value = null;
        
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        localStorage.removeItem('crianca');
        localStorage.removeItem('filhoSelecionadoId');
        
        delete axios.defaults.headers.common['Authorization'];
    }

    function logout() {
        clearLoginData();
        // Redireciona via router para não recarregar a página inteira bruscamente
        router.push('/login');
    }

    // Inicialização: Se tiver token salvo, já injeta no Axios
    if (token.value) {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token.value}`;
    }

    return { 
        user, 
        token, 
        criancaSelecionada, 
        isAuthenticated, 
        isResponsavel,
        setLoginData, 
        selecionarCrianca,
        clearLoginData,
        logout 
    };
});
