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

      <q-table
        title="Demandes de réservation"
        :rows="requests"
        :columns="columns"
        row-key="id"
        :loading="loading"
        no-data-label="Aucune demande en attente"
        table-header-class="bg-grey-2 text-grey-8"
        flat
        bordered
      >
        <template #body-cell-actions="props">
          <q-td :props="props" class="q-gutter-sm">
            <q-btn
              unelevated
              no-caps
              color="positive"
              icon="check"
              label="Valider"
              size="sm"
              @click="onApprove(props.row.id)"
            />
            <q-btn
              outline
              no-caps
              color="negative"
              icon="close"
              label="Refuser"
              size="sm"
              @click="openReject(props.row.id)"
            />
          </q-td>
        </template>
      </q-table>

      <q-banner v-if="errorMessage" dense rounded class="bg-red-1 text-negative q-mt-md">
        {{ errorMessage }}
      </q-banner>
    </div>

    <q-dialog v-model="rejectDialogOpen">
      <q-card style="width: 100%; max-width: 480px">
        <q-card-section>
          <div class="text-h6">Refuser la réservation</div>
        </q-card-section>
        <q-separator />

        <q-card-section>
          <q-form class="q-gutter-md" @submit="onReject">
            <q-input
              v-model="rejectReason"
              type="textarea"
              label="Motif du refus"
              maxlength="500"
              :rules="[(value: string) => !!value.trim() || 'Motif obligatoire']"
              autofocus
              outlined
              stack-label
            />
            <div class="row justify-end q-gutter-sm">
              <q-btn v-close-popup flat no-caps color="primary" label="Annuler" />
              <q-btn
                type="submit"
                unelevated
                no-caps
                color="negative"
                label="Refuser"
                :loading="rejecting"
              />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { date, type QTableColumn } from 'quasar';
import {
  type ReservationResponse,
  approveReservation,
  fetchPendingReservations,
  rejectReservation,
} from '@/api/reservations';
import { fetchRooms } from '@/api/rooms';

function formatDateTime(value: string): string {
  return date.formatDate(value, 'DD/MM/YYYY HH:mm');
}

const roomNames = ref(new Map<number, string>());

const columns: QTableColumn<ReservationResponse>[] = [
  {
    name: 'room',
    label: 'Salle',
    field: (row) => roomNames.value.get(row.roomId) ?? `#${row.roomId}`,
    align: 'left',
  },
  { name: 'startAt', label: 'Début', field: 'startAt', align: 'left', format: formatDateTime },
  { name: 'endAt', label: 'Fin', field: 'endAt', align: 'left', format: formatDateTime },
  { name: 'purpose', label: 'Objet', field: 'purpose', align: 'left' },
  { name: 'requesterLogin', label: 'Demandeur', field: 'requesterLogin', align: 'left' },
  { name: 'actions', label: 'Actions', field: 'id', align: 'right' },
];

const requests = ref<ReservationResponse[]>([]);
const loading = ref(false);
const errorMessage = ref<string | null>(null);

async function load() {
  loading.value = true;
  try {
    const [pending, rooms] = await Promise.all([fetchPendingReservations(), fetchRooms()]);
    roomNames.value = new Map(rooms.map((room) => [room.id, room.name]));
    requests.value = pending;
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    loading.value = false;
  }
}

onMounted(load);

function removeRequest(id: number) {
  requests.value = requests.value.filter((request) => request.id !== id);
}

async function onApprove(id: number) {
  errorMessage.value = null;
  try {
    await approveReservation(id);
    removeRequest(id);
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  }
}

const rejectDialogOpen = ref(false);
const rejectingId = ref<number | null>(null);
const rejectReason = ref('');
const rejecting = ref(false);

function openReject(id: number) {
  rejectingId.value = id;
  rejectReason.value = '';
  rejectDialogOpen.value = true;
}

async function onReject() {
  if (rejectingId.value === null) {
    return;
  }
  const id = rejectingId.value;
  errorMessage.value = null;
  rejecting.value = true;
  try {
    await rejectReservation(id, rejectReason.value.trim());
    removeRequest(id);
    rejectDialogOpen.value = false;
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    rejecting.value = false;
  }
}
</script>
