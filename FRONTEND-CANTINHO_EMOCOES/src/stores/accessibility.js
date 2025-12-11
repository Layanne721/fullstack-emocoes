import { defineStore } from 'pinia';
import { ref, watch } from 'vue';

export const useAccessibilityStore = defineStore('accessibility', () => {
  // Estado inicial recuperado do localStorage ou padrão
  const fontSize = ref(Number(localStorage.getItem('a11y_fontSize')) || 100); // Em porcentagem (100%)

  // Ações
  function increaseFont() {
    if (fontSize.value < 150) fontSize.value += 10; // Limite de 150%
  }

  function decreaseFont() {
    if (fontSize.value > 70) fontSize.value -= 10; // Limite de 70%
  }

  function reset() {
    fontSize.value = 100;
  }

  // Observador para salvar e aplicar alterações automaticamente
  watch(fontSize, (newVal) => {
    document.documentElement.style.fontSize = `${newVal}%`;
    localStorage.setItem('a11y_fontSize', newVal);
  }, { immediate: true });

  return { 
    fontSize, 
    increaseFont, 
    decreaseFont, 
    reset
  };
});