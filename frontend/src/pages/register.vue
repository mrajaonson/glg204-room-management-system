<template>
  <q-page class="flex flex-center q-pa-md">
    <q-card flat bordered class="q-pa-md" style="width: 100%; max-width: 480px">
      <q-card-section class="text-center">
        <q-icon name="person_add" color="primary" size="40px" />
        <div class="text-h6 q-mt-sm">Demande de création de compte</div>
      </q-card-section>

      <q-card-section v-if="submitted" class="text-center">
        <q-icon name="mark_email_read" color="positive" size="48px" />
        <p class="q-mt-md">Votre demande a bien été enregistrée.</p>
        <p class="text-grey-7">Un mail de confirmation a été envoyé à {{ form.email }}.</p>
        <q-btn unelevated no-caps color="primary" label="Retour à l'accueil" to="/" />
      </q-card-section>

      <q-card-section v-else>
        <q-form class="q-gutter-md" @submit="onSubmit">
          <q-input
            v-model="form.login"
            label="Identifiant"
            :rules="rules.login"
            autocomplete="username"
            autofocus
            outlined
            stack-label
          />
          <q-input
            v-model="form.email"
            type="email"
            label="Email"
            :rules="rules.email"
            autocomplete="email"
            outlined
            stack-label
          />
          <q-input
            v-model="form.password"
            type="password"
            label="Mot de passe"
            :rules="rules.password"
            autocomplete="new-password"
            outlined
            stack-label
          />
          <q-input
            v-model="form.passwordConfirmation"
            type="password"
            label="Confirmation du mot de passe"
            :rules="rules.passwordConfirmation"
            autocomplete="new-password"
            outlined
            stack-label
          />
          <q-banner v-if="errorMessage" dense rounded class="bg-red-1 text-negative">
            {{ errorMessage }}
          </q-banner>
          <q-btn
            type="submit"
            unelevated
            no-caps
            color="primary"
            label="Envoyer la demande"
            class="full-width"
            :loading="loading"
          />
        </q-form>
      </q-card-section>
    </q-card>
  </q-page>
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
