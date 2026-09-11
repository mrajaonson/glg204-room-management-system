<template>
  <q-page class="flex flex-center">
    <q-card>
      <q-card-section>
        <div class="text-h5">Bienvenue</div>
        <div v-if="userStore.login">Connecté en tant que {{ userStore.login }}</div>
        <div v-else>Connectez-vous ou demandez la création d'un compte.</div>
      </q-card-section>

      <q-card-section v-if="!userStore.login" class="q-gutter-sm">
        <q-btn color="primary" label="Se connecter" to="/login" />
        <q-btn color="primary" outline label="Créer un compte" to="/register" />
      </q-card-section>

      <q-card-section v-if="userStore.isAdmin">
        <q-btn
          color="primary"
          label="Demandes de création de compte"
          to="/admin/account-requests"
        />
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user-store';

const userStore = useUserStore();
</script>
