/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Cores do Gradiente de Fundo (Lavanda para Pêssego)
        'lavanda': '#E6E6FA',
        'pessego': '#FFDAB9',
        'fundo-inicio': '#FDFBF7', // Creme suave para o fundo da Home
        
        // Cores dos Cartões (Baseado nas imagens do protótipo)
        'card-diario': '#F3E8FF',   // Lilás suave
        'card-desenho': '#E0F2F1',  // Verde água
        'card-jogos': '#FFF9C4',    // Amarelo pastel
        'card-emocoes': '#FCE4EC',  // Rosa bebê
        
        // Cores de Texto e Detalhes
        'texto-escuro': '#4A4A4A',
        'texto-suave': '#787878',
        'roxo-titulo': '#8B5CF6',   // Cor do título principal
        'azul-ceu': '#87CEEB',
        'rosa-suave': '#FFB6C1',
      },
      fontFamily: {
        // Fonte arredondada e amigável para crianças
        'nunito': ['"Nunito"', 'sans-serif'],
      },
      animation: {
        'flutuar': 'flutuar 3s ease-in-out infinite',
      },
      keyframes: {
        flutuar: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-10px)' },
        }
      },
      boxShadow: {
        'suave': '0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03)',
        'flutuar': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
      }
    },
  },
  plugins: [],
}