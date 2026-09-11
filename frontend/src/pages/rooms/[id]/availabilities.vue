<template>
  <q-page padding>
    <q-table
      :title="roomName ? `Disponibilités — ${roomName}` : 'Disponibilités'"
      :rows="slots"
      :columns="columns"
      row-key="id"
      :loading="loading"
      no-data-label="Aucune disponibilité"
      flat
      bordered
    />

    <div v-if="errorMessage" class="text-negative q-mt-md">{{ errorMessage }}</div>

    <q-card class="q-mt-md" flat bordered>
      <q-card-section>
        <div class="text-h6">Ajouter une disponibilité</div>
      </q-card-section>

      <q-card-section>
        <q-form ref="slotForm" class="q-gutter-md" @submit="onSubmit" @reset="resetForm">
          <q-input
            v-model="form.startAt"
            label="Début"
            mask="####-##-## ##:##"
            :rules="rules.startAt"
            stack-label
          >
            <template #append>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover>
                  <q-date v-model="form.startAt" mask="YYYY-MM-DD HH:mm" />
                </q-popup-proxy>
              </q-icon>
              <q-icon name="access_time" class="cursor-pointer">
                <q-popup-proxy cover>
                  <q-time v-model="form.startAt" mask="YYYY-MM-DD HH:mm" format24h />
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>

          <q-input
            v-model="form.endAt"
            label="Fin"
            mask="####-##-## ##:##"
            :rules="rules.endAt"
            stack-label
          >
            <template #append>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover>
                  <q-date v-model="form.endAt" mask="YYYY-MM-DD HH:mm" />
                </q-popup-proxy>
              </q-icon>
              <q-icon name="access_time" class="cursor-pointer">
                <q-popup-proxy cover>
                  <q-time v-model="form.endAt" mask="YYYY-MM-DD HH:mm" format24h />
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>

          <div v-if="successMessage" class="text-positive">{{ successMessage }}</div>
          <div v-if="createErrorMessage" class="text-negative">{{ createErrorMessage }}</div>
          <q-btn type="submit" color="primary" label="Ajouter" :loading="creating" />
        </q-form>
      </q-card-section>
    </q-card>

    <q-btn class="q-mt-md" color="primary" label="Retour aux salles" to="/rooms" />
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, useTemplateRef } from 'vue';
import { useRoute } from 'vue-router';
import { date, type QForm, type QTableColumn } from 'quasar';
import { isAxiosError } from 'axios';
import {
  type AvailabilitySlotResponse,
  addAvailabilitySlot,
  fetchAvailabilitySlots,
  fetchRoom,
} from '@/api/rooms';

const route = useRoute('/rooms/[id]/availabilities');
const roomId = Number(route.params.id);

function formatDateTime(value: string): string {
  return date.formatDate(value, 'DD/MM/YYYY HH:mm');
}

const columns: QTableColumn<AvailabilitySlotResponse>[] = [
  { name: 'startAt', label: 'Début', field: 'startAt', align: 'left', format: formatDateTime },
  { name: 'endAt', label: 'Fin', field: 'endAt', align: 'left', format: formatDateTime },
];

const roomName = ref<string | null>(null);
const slots = ref<AvailabilitySlotResponse[]>([]);
const loading = ref(false);
const errorMessage = ref<string | null>(null);

async function load() {
  loading.value = true;
  try {
    const [room, roomSlots] = await Promise.all([
      fetchRoom(roomId),
      fetchAvailabilitySlots(roomId),
    ]);
    roomName.value = room.name;
    slots.value = roomSlots;
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    loading.value = false;
  }
}

onMounted(load);

const slotForm = useTemplateRef<QForm>('slotForm');
const emptyForm = () => ({ startAt: '', endAt: '' });
const form = reactive(emptyForm());
const creating = ref(false);
const successMessage = ref<string | null>(null);
const createErrorMessage = ref<string | null>(null);

// Values look like "2026-09-11 14:00", so string order is chronological order
const dateTimePattern = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/;
const rules = {
  startAt: [
    (value: string) => dateTimePattern.test(value) || 'Date et heure de début obligatoires',
  ],
  endAt: [
    (value: string) => dateTimePattern.test(value) || 'Date et heure de fin obligatoires',
    (value: string) => value > form.startAt || 'La fin doit être après le début',
  ],
};

// "2026-09-11 14:00" -> "2026-09-11T14:00:00", the backend LocalDateTime format
function toLocalDateTime(value: string): string {
  return `${value.replace(' ', 'T')}:00`;
}

// Called by q-form reset(), which then clears the validation errors itself
function resetForm() {
  Object.assign(form, emptyForm());
}

async function onSubmit() {
  successMessage.value = null;
  createErrorMessage.value = null;
  creating.value = true;
  try {
    await addAvailabilitySlot(roomId, {
      startAt: toLocalDateTime(form.startAt),
      endAt: toLocalDateTime(form.endAt),
    });
    successMessage.value = 'Disponibilité ajoutée';
    slotForm.value?.reset();
    await load();
  } catch (error) {
    createErrorMessage.value =
      isAxiosError(error) && error.response?.status === 409
        ? 'Cette période chevauche une disponibilité existante'
        : 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    creating.value = false;
  }
}
</script>
