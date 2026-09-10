<template>
  <q-layout>
    <q-page-container>
      <q-page class="flex flex-center">
        <q-card>
          <q-card-section>
            <div class="text-h5">Gestion de salles</div>
            <div class="text-h6">Demande de création de compte</div>
          </q-card-section>

          <q-card-section v-if="submitted">
            <p>Votre demande a bien été enregistrée.</p>
            <p>Un mail de confirmation a été envoyé à {{ form.email }}.</p>
            <q-btn color="primary" label="Retour à l'accueil" to="/" />
          </q-card-section>

          <q-card-section v-else>
            <q-form class="q-gutter-md" @submit="onSubmit">
              <q-input
                v-model="form.login"
                label="Identifiant"
                :rules="rules.login"
                autocomplete="username"
                autofocus
                stack-label
              />
              <q-input
                v-model="form.email"
                type="email"
                label="Email"
                :rules="rules.email"
                autocomplete="email"
                stack-label
              />
              <q-input
                v-model="form.password"
                type="password"
                label="Mot de passe"
                :rules="rules.password"
                autocomplete="new-password"
                stack-label
              />
              <q-input
                v-model="form.passwordConfirmation"
                type="password"
                label="Confirmation du mot de passe"
                :rules="rules.passwordConfirmation"
                autocomplete="new-password"
                stack-label
              />
              <div v-if="errorMessage" class="text-negative">{{ errorMessage }}</div>
              <q-btn type="submit" color="primary" label="Envoyer la demande" :loading="loading" />
            </q-form>
          </q-card-section>
        </q-card>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { patterns } from 'quasar';
import { requestAccountCreation } from '@/api/accounts';

const form = reactive({ login: '', email: '', password: '', passwordConfirmation: '' });
const loading = ref(false);
const submitted = ref(false);
const errorMessage = ref<string | null>(null);

const rules = {
  login: [
    (value: string) =>
      (value.trim().length >= 3 && value.trim().length <= 50) ||
      "L'identifiant doit contenir entre 3 et 50 caractères",
  ],
  email: [(value: string) => patterns.testPattern.email(value.trim()) || 'Adresse mail invalide'],
  password: [
    (value: string) =>
      (value.length >= 8 && value.length <= 72) ||
      'Le mot de passe doit contenir entre 8 et 72 caractères',
  ],
  passwordConfirmation: [
    (value: string) => value === form.password || 'Les mots de passe ne correspondent pas',
  ],
};

async function onSubmit() {
  errorMessage.value = null;
  loading.value = true;
  try {
    await requestAccountCreation({
      ...form,
      login: form.login.trim(),
      email: form.email.trim(),
    });
    submitted.value = true;
  } catch {
    errorMessage.value = "Une erreur s'est produite, veuillez réessayer plus tard";
  } finally {
    loading.value = false;
  }
}
</script>
