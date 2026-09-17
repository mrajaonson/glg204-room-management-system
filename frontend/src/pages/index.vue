<template>
  <q-page class="flex flex-center q-pa-md">
    <q-card flat bordered class="q-pa-md" style="width: 100%; max-width: 480px">
      <q-card-section class="text-center">
        <q-icon name="meeting_room" color="primary" size="56px" />
        <div class="text-h5 text-weight-medium q-mt-sm">Bienvenue</div>
        <div v-if="userStore.login" class="text-grey-7">
          Connecté en tant que {{ userStore.login }}
        </div>
        <div v-else class="text-grey-7">Connectez-vous ou demandez la création d'un compte.</div>
      </q-card-section>

      <q-card-section v-if="!userStore.login" class="column q-gutter-sm">
        <q-btn unelevated no-caps color="primary" label="Se connecter" to="/login" />
        <q-btn outline no-caps color="primary" label="Créer un compte" to="/register" />
      </q-card-section>

      <q-card-section v-else>
        <q-list bordered separator class="rounded-borders">
          <q-item v-ripple clickable to="/rooms">
            <q-item-section avatar><q-icon name="meeting_room" color="primary" /></q-item-section>
            <q-item-section>Salles</q-item-section>
            <q-item-section side><q-icon name="chevron_right" /></q-item-section>
          </q-item>
          <q-item v-ripple clickable to="/rooms/search">
            <q-item-section avatar><q-icon name="search" color="primary" /></q-item-section>
            <q-item-section>Recherche de salles</q-item-section>
            <q-item-section side><q-icon name="chevron_right" /></q-item-section>
          </q-item>
          <q-item v-if="userStore.isManager" v-ripple clickable to="/manager/reservation-requests">
            <q-item-section avatar
              ><q-icon name="event_available" color="primary"
            /></q-item-section>
            <q-item-section>Demandes de réservation</q-item-section>
            <q-item-section side><q-icon name="chevron_right" /></q-item-section>
          </q-item>
          <q-item v-if="userStore.isAdmin" v-ripple clickable to="/admin/account-requests">
            <q-item-section avatar><q-icon name="person_add" color="primary" /></q-item-section>
            <q-item-section>Demandes de création de compte</q-item-section>
            <q-item-section side><q-icon name="chevron_right" /></q-item-section>
          </q-item>
        </q-list>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { useUserStore } from '@/stores/user-store';

const userStore = useUserStore();
</script>
