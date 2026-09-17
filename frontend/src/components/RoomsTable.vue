<template>
  <q-table
    :title="title"
    :rows="rows"
    :columns="columns"
    row-key="id"
    :loading="loading"
    :no-data-label="noDataLabel"
    table-header-class="bg-grey-2 text-grey-8"
    flat
    bordered
  >
    <template #body-cell-actions="props">
      <q-td :props="props">
        <q-btn-dropdown outline no-caps color="primary" label="Actions" size="sm">
          <q-list dense>
            <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/planning`">
              <q-item-section avatar><q-icon name="calendar_month" size="xs" /></q-item-section>
              <q-item-section>Planning</q-item-section>
            </q-item>
            <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/reserve`">
              <q-item-section avatar><q-icon name="event" size="xs" /></q-item-section>
              <q-item-section>Réservations</q-item-section>
            </q-item>
            <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/availabilities`">
              <q-item-section avatar><q-icon name="schedule" size="xs" /></q-item-section>
              <q-item-section>Disponibilités</q-item-section>
            </q-item>
            <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/equipments`">
              <q-item-section avatar><q-icon name="devices" size="xs" /></q-item-section>
              <q-item-section>Équipements</q-item-section>
            </q-item>
            <template v-if="userStore.isManager">
              <q-separator />
              <q-item v-close-popup clickable :to="`/rooms/${props.row.id}/edit`">
                <q-item-section avatar><q-icon name="edit" size="xs" /></q-item-section>
                <q-item-section>Modifier</q-item-section>
              </q-item>
              <!-- Disabled until the backend exposes DELETE /rooms/{id} -->
              <q-item clickable disable>
                <q-item-section avatar><q-icon name="delete" size="xs" /></q-item-section>
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
