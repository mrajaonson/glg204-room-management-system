<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-toolbar-title>Gestion de salles</q-toolbar-title>

        <div class="row items-center q-gutter-sm">
          <q-icon name="account_circle" size="sm" />
          <span>{{ userStore.login }}</span>
          <q-btn flat dense round icon="logout" aria-label="Se déconnecter" @click="logout">
            <q-tooltip>Se déconnecter</q-tooltip>
          </q-btn>
        </div>
      </q-toolbar>
    </q-header>

    <q-page-container>
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
  await router.push({ name: '/login' });
}
</script>
