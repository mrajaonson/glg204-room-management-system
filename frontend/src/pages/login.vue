<template>
  <q-page class="flex flex-center">
    <q-card>
      <q-card-section>
        <div class="text-h6">Connexion</div>
      </q-card-section>

      <q-card-section>
        <q-form class="q-gutter-md" @submit="onSubmit">
          <q-input
            v-model="form.login"
            label="Identifiant"
            :rules="[(value: string) => !!value || 'Identifiant obligatoire']"
            autocomplete="username"
            autofocus
            stack-label
          />
          <q-input
            v-model="form.password"
            type="password"
            label="Mot de passe"
            :rules="[(value: string) => !!value || 'Mot de passe obligatoire']"
            autocomplete="current-password"
            stack-label
          />
          <div v-if="errorMessage" class="text-negative">{{ errorMessage }}</div>
          <q-btn type="submit" color="primary" label="Se connecter" :loading="loading" />
        </q-form>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { isAxiosError } from 'axios';
import { useUserStore } from '@/stores/user-store';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const form = reactive({ login: '', password: '' });
const loading = ref(false);
const errorMessage = ref<string | null>(null);

function redirectTarget(): string {
  const redirect = route.query.redirect;
  return typeof redirect === 'string' ? redirect : '/';
}

async function onSubmit() {
  errorMessage.value = null;
  loading.value = true;
  try {
    await userStore.authenticate(form.login.trim(), form.password);
    await router.replace(redirectTarget());
  } catch (error) {
    errorMessage.value =
      isAxiosError(error) && error.response?.status === 401
        ? 'Identifiant ou mot de passe incorrect'
        : 'Impossible de joindre le serveur';
  } finally {
    loading.value = false;
  }
}
</script>
