<template>
   <div class="main">
   <div>
   <div class="content">
    <h1>Login</h1>

    <form @submit.prevent="handleLogin">
      <input v-model="username" type="text" placeholder="Username" required />
      <input v-model="password" type="password" placeholder="Password" required />
      <button class="loginbutton" type="submit">Login</button>
    </form>
    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
  </div>
  </div>
  </div>
</template>

<script>
import axios from '../axios';
import { mapActions } from 'vuex';

export default {
  data() {
    return {
      username: '',
      password: '',
      errorMessage: '',
      logoUrl: require('@/assets/logo.png')
    };
  },
  methods: {
    ...mapActions(['login']), // Mappe die Vuex-Action zum `login`
    async handleLogin() {
      try {
        const response = await axios.post('/auth/login', {
          username: this.username,
          password: this.password
        });
        sessionStorage.setItem('jwt-token', response.data); // Speichere das Token im sessionStorage
        sessionStorage.setItem('username', this.username); // Speichere das Token im sessionStorage
        await this.$store.dispatch('login'); // Aktualisiere den Authentifizierungsstatus im Vuex-Store
        this.$router.push('/'); // Leite zur Home-Seite weiter
      } catch (error) {
        this.errorMessage = 'Login failed. Please check your credentials.';
      }
    }

  }
};
</script>
