<template>
  <q-page padding>
    <q-toggle
      v-if="userStore.isManager"
      v-model="currentUserOnly"
      class="q-mb-md"
      label="Mes réservations"
      @update:model-value="load"
    />

    <q-table
      :title="roomName ? `Réservations — ${roomName}` : 'Réservations'"
      :rows="reservations"
      :columns="columns"
      row-key="id"
      :loading="loading"
      no-data-label="Aucune réservation"
      flat
      bordered
    />

    <div v-if="errorMessage" class="text-negative q-mt-md">{{ errorMessage }}</div>

    <q-card class="q-mt-md" flat bordered>
      <q-card-section>
        <div class="text-h6">Nouvelle réservation</div>
      </q-card-section>

      <q-card-section>
        <q-form ref="reservationForm" class="q-gutter-md" @submit="onSubmit" @reset="resetForm">
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

          <q-input
            v-model="form.purpose"
            type="textarea"
            label="Objet"
            maxlength="500"
            :rules="rules.purpose"
            stack-label
          />

          <div v-if="successMessage" class="text-positive">{{ successMessage }}</div>
          <div v-if="createErrorMessage" class="text-negative">{{ createErrorMessage }}</div>
          <q-btn type="submit" color="primary" label="Réserver" :loading="creating" />
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
  type ReservationResponse,
  type ReservationStatus,
  createReservation,
  fetchReservations,
} from '@/api/reservations';
import { fetchRoom } from '@/api/rooms';
import { useUserStore } from '@/stores/user-store';

const userStore = useUserStore();
const route = useRoute('/rooms/[id]/reserve');
const roomId = Number(route.params.id);

const statusLabels: Record<ReservationStatus, string> = {
  PENDING_APPROVAL: 'En attente de validation',
  CONFIRMED: 'Confirmée',
  REJECTED: 'Refusée',
  CANCELLED: 'Annulée',
};

function formatDateTime(value: string): string {
  return date.formatDate(value, 'DD/MM/YYYY HH:mm');
}

const columns: QTableColumn<ReservationResponse>[] = [
  { name: 'startAt', label: 'Début', field: 'startAt', align: 'left', format: formatDateTime },
  { name: 'endAt', label: 'Fin', field: 'endAt', align: 'left', format: formatDateTime },
  { name: 'purpose', label: 'Objet', field: 'purpose', align: 'left' },
  {
    name: 'status',
    label: 'Statut',
    field: 'status',
    align: 'left',
    format: (value: ReservationStatus) => statusLabels[value],
  },
];

const roomName = ref<string | null>(null);
const reservations = ref<ReservationResponse[]>([]);
const loading = ref(false);
const errorMessage = ref<string | null>(null);
const currentUserOnly = ref(false);

async function load() {
  loading.value = true;
  try {
    const [room, roomReservations] = await Promise.all([
      fetchRoom(roomId),
      fetchReservations(roomId, currentUserOnly.value),
    ]);
    roomName.value = room.name;
    reservations.value = roomReservations;
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    loading.value = false;
  }
}

onMounted(load);

const reservationForm = useTemplateRef<QForm>('reservationForm');
const emptyForm = () => ({ startAt: '', endAt: '', purpose: '' });
const form = reactive(emptyForm());
const creating = ref(false);
const successMessage = ref<string | null>(null);
const createErrorMessage = ref<string | null>(null);

// Values look like "2026-09-12 14:00", so string order is chronological order
const dateTimePattern = /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/;
const rules = {
  startAt: [
    (value: string) => dateTimePattern.test(value) || 'Date et heure de début obligatoires',
  ],
  endAt: [
    (value: string) => dateTimePattern.test(value) || 'Date et heure de fin obligatoires',
    (value: string) => value > form.startAt || 'La fin doit être après le début',
  ],
  purpose: [(value: string) => !!value.trim() || 'Objet obligatoire'],
};

// "2026-09-12 14:00" -> "2026-09-12T14:00:00", backend LocalDateTime format
function toLocalDateTime(value: string): string {
  return `${value.replace(' ', 'T')}:00`;
}

function resetForm() {
  Object.assign(form, emptyForm());
}

function creationErrorMessage(error: unknown): string {
  if (isAxiosError(error)) {
    if (error.response?.status === 400) {
      return "La période demandée n'est pas couverte par les disponibilités de la salle";
    }
    if (error.response?.status === 409) {
      return 'La salle est déjà réservée sur cette période';
    }
  }
  return 'Impossible de traiter la demande, veuillez réessayer plus tard';
}

async function onSubmit() {
  successMessage.value = null;
  createErrorMessage.value = null;
  creating.value = true;
  try {
    await createReservation({
      roomId,
      startAt: toLocalDateTime(form.startAt),
      endAt: toLocalDateTime(form.endAt),
      purpose: form.purpose.trim(),
    });
    successMessage.value = 'Réservation enregistrée';
    reservationForm.value?.reset();
    await load();
  } catch (error) {
    createErrorMessage.value = creationErrorMessage(error);
  } finally {
    creating.value = false;
  }
}
</script>
