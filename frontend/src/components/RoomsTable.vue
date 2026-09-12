<template>
  <q-table
    :title="title"
    :rows="rows"
    :columns="columns"
    row-key="id"
    :loading="loading"
    :no-data-label="noDataLabel"
    flat
    bordered
  >
    <template #body-cell-actions="props">
      <q-td :props="props">
        <q-btn-dropdown color="primary" label="Actions" size="sm">
          <q-list>
            <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/planning`">
              <q-item-section>Planning</q-item-section>
            </q-item>
            <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/reserve`">
              <q-item-section>Réservations</q-item-section>
            </q-item>
            <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/availabilities`">
              <q-item-section>Disponibilités</q-item-section>
            </q-item>
            <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/equipments`">
              <q-item-section>Équipements</q-item-section>
            </q-item>
            <template v-if="userStore.isManager">
              <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/edit`">
                <q-item-section>Modifier</q-item-section>
              </q-item>
              <!-- Disabled until the backend exposes DELETE /rooms/{id} -->
              <q-item clickable disable>
                <q-item-section>Supprimer</q-item-section>
              </q-item>
            </template>
          </q-list>
        </q-btn-dropdown>
      </q-td>
    </template>
  </q-table>
</template>

<script setup lang="ts">
import type { QTableColumn } from 'quasar';
import type { RoomResponse, RoomType } from '@/api/rooms';
import { roomTypeLabels } from '@/components/room-form';
import { useUserStore } from '@/stores/user-store';

const {
  rows,
  loading = false,
  title = 'Salles',
  noDataLabel = 'Aucune salle',
} = defineProps<{
  rows: RoomResponse[];
  loading?: boolean;
  title?: string;
  noDataLabel?: string;
}>();

const userStore = useUserStore();

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
</script>
