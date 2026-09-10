<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-toolbar-title>Gestion de salles</q-toolbar-title>

        <div v-if="userStore.login" class="row items-center q-gutter-sm">
          <q-icon name="account_circle" size="sm" />
          <span>{{ userStore.login }}</span>
          <q-btn
            flat
            dense
            round
            icon="logout"
            aria-label="Se déconnecter"
            @click="userStore.logout()"
          >
            <q-tooltip>Se déconnecter</q-tooltip>
          </q-btn>
        </div>
      </q-toolbar>
    </q-header>

    <q-page-container>
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
        </q-card>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user-store';

const userStore = useUserStore();
</script>
