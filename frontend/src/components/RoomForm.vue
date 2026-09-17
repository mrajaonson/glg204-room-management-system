<!-- Room fields shared by the create form (rooms page) and the edit page -->
<template>
  <q-input
    v-model="model.name"
    class="col-12 col-sm-6"
    label="Nom"
    maxlength="100"
    :rules="rules.name"
    outlined
    stack-label
  />
  <q-input
    v-model="model.location"
    class="col-12 col-sm-6"
    label="Localisation"
    maxlength="255"
    :rules="rules.location"
    outlined
    stack-label
  />
  <q-input
    v-model.number="model.capacity"
    class="col-12 col-sm-6"
    type="number"
    label="Capacité"
    :rules="rules.capacity"
    outlined
    stack-label
  />
  <q-select
    v-model="model.type"
    class="col-12 col-sm-6"
    :options="roomTypeOptions"
    label="Type"
    emit-value
    map-options
    :rules="rules.type"
    outlined
    stack-label
  />
  <q-input
    v-model="model.description"
    class="col-12"
    type="textarea"
    label="Description"
    maxlength="500"
    outlined
    stack-label
  />
  <div class="col-12">
    <q-checkbox
      v-model="model.reservationRequiresApproval"
      label="Les réservations doivent être validées par un responsable"
    />
  </div>
</template>

<script setup lang="ts">
import type { RoomType } from '@/api/rooms';
import { type RoomFormValue, roomTypeLabels } from '@/components/room-form';

const model = defineModel<RoomFormValue>({ required: true });

const roomTypeOptions = Object.entries(roomTypeLabels).map(([value, label]) => ({ value, label }));

// Mirrors backend room request validation (lengths are capped by maxlength)
const rules = {
  name: [(value: string) => !!value.trim() || 'Nom obligatoire'],
  location: [(value: string) => !!value.trim() || 'Localisation obligatoire'],
  capacity: [
    (value: unknown) =>
      (typeof value === 'number' && Number.isInteger(value) && value > 0) ||
      'La capacité doit être un entier positif',
  ],
  type: [(value: RoomType | null) => !!value || 'Type obligatoire'],
};
</script>
