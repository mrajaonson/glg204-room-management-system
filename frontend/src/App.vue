<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar class="q-px-md">
        <q-btn flat round dense icon="meeting_room" aria-label="Accueil" to="/" />
        <q-toolbar-title class="text-weight-medium">Gestion de salles</q-toolbar-title>

        <div v-if="userStore.login" class="row items-center q-gutter-sm">
          <q-chip dense color="white" text-color="primary" icon="account_circle">
            {{ userStore.login }}
          </q-chip>
          <q-btn flat dense round icon="logout" aria-label="Se déconnecter" @click="logout">
            <q-tooltip>Se déconnecter</q-tooltip>
          </q-btn>
        </div>
      </q-toolbar>
    </q-header>

    <q-page-container class="bg-grey-1">
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user-store';

const router = useRouter();
const userStore = useUserStore();

async function logout() {
  userStore.logout();
  await router.push('/');
}
</script>
