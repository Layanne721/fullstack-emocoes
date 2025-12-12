import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// --- VIEWS ---
const LoginView = () => import('../views/autenticacao/LoginView.vue')
const RegistroView = () => import('../views/autenticacao/RegistroView.vue')
const RecuperarSenhaView = () => import('../views/autenticacao/RecuperarSenhaView.vue')
const ResetarSenhaView = () => import('../views/autenticacao/RedefinirSenhaView.vue') 
const SelecionarPerfilView = () => import('../views/autenticacao/SelecaoPerfilView.vue')

// Admin
const AdminDashboard = () => import('../views/autenticacao/AdminDashboard.vue')
const GerenciadorDados = () => import('../views/autenticacao/GerenciadorDados.vue')

// Funcionalidades (Crianças)
const HomeView = () => import('../views/inicio/HomeView.vue')
const DiarioView = () => import('../views/funcionalidades/DiarioView.vue')
const DesenhoView = () => import('../views/funcionalidades/DesenhoView.vue')
const EmocoesView = () => import('../views/funcionalidades/EmocoesView.vue')

// Responsável
const ResponsavelDashboardView = () => import('../views/responsavel/ResponsavelDashboardView.vue')

// Jogos
const JogosView = () => import('../views/jogos/JogosView.vue')
const JogoRespiracaoView = () => import('../views/jogos/JogoRespiracaoView.vue')
const JogoBalaoView = () => import('../views/jogos/JogoBalaoView.vue')
const JogoMemoriaView = () => import('../views/jogos/JogoMemoriaView.vue')
const JogoNojinhoView = () => import('../views/jogos/JogoNojinhoView.vue')
const JogoAlegriaView = () => import('../views/jogos/JogoAlegriaView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    
    // Públicas
    { path: '/login', name: 'login', component: LoginView },
    { path: '/registro', name: 'registro', component: RegistroView },
    { path: '/recuperar-senha', name: 'recuperar-senha', component: RecuperarSenhaView },
    { path: '/resetar-senha', name: 'resetar-senha', component: ResetarSenhaView },

    // Admin
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: AdminDashboard,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/dados',
      name: 'gerenciador-dados',
      component: GerenciadorDados,
      meta: { requiresAuth: true, requiresAdmin: true }
    },

    // Rotas de Usuário Comum
    {
      path: '/selecionar-perfil',
      name: 'selecionar-perfil',
      component: SelecionarPerfilView,
      meta: { requiresAuth: true }
    },
    { path: '/home', name: 'home', component: HomeView, meta: { requiresAuth: true, requiresChild: true } },
    { path: '/diario', name: 'diario', component: DiarioView, meta: { requiresAuth: true, requiresChild: true } },
    { path: '/desenho', name: 'desenho', component: DesenhoView, meta: { requiresAuth: true, requiresChild: true } },
    { path: '/emocoes', name: 'emocoes', component: EmocoesView, meta: { requiresAuth: true, requiresChild: true } },
    { path: '/responsavel', name: 'responsavel-dashboard', component: ResponsavelDashboardView, meta: { requiresAuth: true } },
    
    // Jogos
    { path: '/jogos', name: 'jogos', component: JogosView, meta: { requiresAuth: true, requiresChild: true } },
    { path: '/jogos/respiracao', name: 'jogo-respiracao', component: JogoRespiracaoView, meta: { requiresAuth: true, requiresChild: true } },
    { path: '/jogos/balao', name: 'jogo-balao', component: JogoBalaoView, meta: { requiresAuth: true, requiresChild: true } },
    { path: '/jogos/memoria', name: 'jogo-memoria', component: JogoMemoriaView, meta: { requiresAuth: true, requiresChild: true } },
    { path: '/jogos/nojinho', name: 'jogo-nojinho', component: JogoNojinhoView, meta: { requiresAuth: true, requiresChild: true } },
    { path: '/jogos/alegria', name: 'jogo-alegria', component: JogoAlegriaView, meta: { requiresAuth: true, requiresChild: true } },
  ]
})

// --- GUARDA DE ROTAS (CORRIGIDO PARA EVITAR LOOP) ---
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  const isAuthenticated = !!authStore.token
  const isChildSelected = !!authStore.criancaSelecionada
  const userRole = authStore.user?.perfil
  
  const publicPages = ['/login', '/registro', '/recuperar-senha', '/resetar-senha'];
  const authRequired = !publicPages.includes(to.path);
  const isAdmin = userRole === 'ADMINISTRADOR' || userRole === 'ADMIN';

  // 1. Não logado tentando acessar página privada -> Login
  if (authRequired && !isAuthenticated) {
    return next('/login');
  }

  // 2. Logado tentando ir para Login -> Redireciona para Home/Admin
  if (isAuthenticated && publicPages.includes(to.path)) {
    if (isAdmin) return next('/admin');
    return next('/selecionar-perfil');
  }

  // 3. Admin tentando acessar área de Criança -> Bloqueia
  if (to.meta.requiresChild && isAdmin) {
      return next('/admin');
  }

  // 4. Bloquear não-admins de acessar /admin
  if (to.meta.requiresAdmin && !isAdmin) {
    return next('/selecionar-perfil');
  }

  // 5. Fluxo normal de Crianças (precisa selecionar perfil)
  if (to.meta.requiresChild && isAuthenticated && !isChildSelected && !isAdmin) {
    return next('/selecionar-perfil');
  }

  next();
})

export default router;