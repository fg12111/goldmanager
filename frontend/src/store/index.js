import { createStore } from 'vuex'; // Importiere createStore von 'vuex'

export default createStore({
  state: {
    isAuthenticated: !!sessionStorage.getItem('jwt-token') // Initialisiere den Authentifizierungsstatus
  },
  mutations: {
    setAuthStatus(state, status) {
      state.isAuthenticated = status; // Setze den Authentifizierungsstatus
    }
  },
  actions: {
    login({ commit }) {
      commit('setAuthStatus', true); // Setze Authentifizierungsstatus auf true
    },
    logout({ commit }) {
      commit('setAuthStatus', false); // Setze Authentifizierungsstatus auf false
    }
  },
  getters: {
    isAuthenticated: state => state.isAuthenticated // Getter für den Authentifizierungsstatus
  }
});
