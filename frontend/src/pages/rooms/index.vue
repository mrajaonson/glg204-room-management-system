<template>
  <q-page padding>
    <q-table
      title="Salles"
      :rows="rooms"
      :columns="columns"
      row-key="id"
      :loading="loading"
      no-data-label="Aucune salle"
      flat
      bordered
    >
      <template #body-cell-actions="props">
        <q-td :props="props" class="q-gutter-sm">
          <q-btn
            color="primary"
            label="Disponibilités"
            size="sm"
            :to="`/rooms/${props.row.id}/availabilities`"
          />
          <q-btn color="primary" label="Modifier" size="sm" :to="`/rooms/${props.row.id}/edit`" />
          <!-- Disabled until the backend exposes DELETE /rooms/{id} -->
          <q-btn color="negative" label="Supprimer" size="sm" disable />
        </q-td>
      </template>
    </q-table>

    <div v-if="errorMessage" class="text-negative q-mt-md">{{ errorMessage }}</div>

    <q-card class="q-mt-md" flat bordered>
      <q-card-section>
        <div class="text-h6">Nouvelle salle</div>
      </q-card-section>

      <q-card-section>
        <q-form ref="createForm" class="q-gutter-md" @submit="onCreate" @reset="resetForm">
          <RoomForm v-model="form" />
          <div v-if="createErrorMessage" class="text-negative">{{ createErrorMessage }}</div>
          <q-btn type="submit" color="primary" label="Créer la salle" :loading="creating" />
        </q-form>
      </q-card-section>
    </q-card>

    <q-btn class="q-mt-md" color="primary" label="Retour à l'accueil" to="/" />
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref, useTemplateRef } from 'vue';
import type { QForm, QTableColumn } from 'quasar';
import { isAxiosError } from 'axios';
import { type RoomResponse, type RoomType, createRoom, fetchRooms } from '@/api/rooms';
import RoomForm from '@/components/RoomForm.vue';
import { emptyRoomForm, formToRequest, roomTypeLabels } from '@/components/room-form';

const columns: QTableColumn<RoomResponse>[] = [
  { name: 'name', label: 'Nom', field: 'name', align: 'left' },
  { name: 'location', label: 'Localisation', field: 'location', align: 'left' },
  { name: 'capacity', label: 'Capacité', field: 'capacity', align: 'right' },
  {
    name: 'type',
    label: 'Type',
    field: 'type',
    align: 'left',
    format: (value: RoomType) => roomTypeLabels[value],
  },
  {
    name: 'reservationRequiresApproval',
    label: 'Validation des réservations',
    field: 'reservationRequiresApproval',
    align: 'left',
    format: (value: boolean) => (value ? 'Oui' : 'Non'),
  },
  { name: 'actions', label: 'Actions', field: 'id', align: 'right' },
];

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
