<template>
  <q-page padding class="row justify-center items-start">
    <div class="col-12 col-lg-10 col-xl-8">
      <q-btn
        class="q-mb-md"
        flat
        no-caps
        color="primary"
        icon="arrow_back"
        label="Retour à l'accueil"
        to="/"
      />

      <RoomsTable title="Salles" :rows="rooms" :loading="loading" />

      <q-banner v-if="errorMessage" dense rounded class="bg-red-1 text-negative q-mt-md">
        {{ errorMessage }}
      </q-banner>

      <q-card v-if="userStore.isManager" class="q-mt-lg" flat bordered>
        <q-card-section>
          <div class="text-h6">Nouvelle salle</div>
        </q-card-section>
        <q-separator />

        <q-card-section>
          <q-form
            ref="createForm"
            class="row q-col-gutter-md"
            @submit="onCreate"
            @reset="resetForm"
          >
            <RoomForm v-model="form" />
            <div v-if="createErrorMessage" class="col-12 text-negative">
              {{ createErrorMessage }}
            </div>
            <div class="col-12">
              <q-btn
                type="submit"
                unelevated
                no-caps
                color="primary"
                icon="add"
                label="Créer la salle"
                :loading="creating"
              />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref, useTemplateRef } from 'vue';
import type { QForm } from 'quasar';
import { isAxiosError } from 'axios';
import { type RoomResponse, createRoom, fetchRooms } from '@/api/rooms';
import RoomForm from '@/components/RoomForm.vue';
import RoomsTable from '@/components/RoomsTable.vue';
import { emptyRoomForm, formToRequest } from '@/components/room-form';
import { useUserStore } from '@/stores/user-store';

const userStore = useUserStore();

const rooms = ref<RoomResponse[]>([]);
const loading = ref(false);
const errorMessage = ref<string | null>(null);

async function load() {
  loading.value = true;
  try {
    rooms.value = await fetchRooms();
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    loading.value = false;
  }
}

onMounted(load);

const createForm = useTemplateRef<QForm>('createForm');
const form = ref(emptyRoomForm());
const creating = ref(false);
const createErrorMessage = ref<string | null>(null);

// Called by q-form reset(), which then clears the validation errors itself
function resetForm() {
  form.value = emptyRoomForm();
}

async function onCreate() {
  const request = formToRequest(form.value);
  if (!request) {
    return;
  }
  createErrorMessage.value = null;
  creating.value = true;
  try {
    await createRoom(request);
    createForm.value?.reset();
    await load();
  } catch (error) {
    createErrorMessage.value =
      isAxiosError(error) && error.response?.status === 409
        ? 'Une salle porte déjà ce nom'
        : 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    creating.value = false;
  }
}
</script>
