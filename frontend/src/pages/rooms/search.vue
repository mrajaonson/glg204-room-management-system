<template>
  <q-page padding>
    <q-card flat bordered>
      <q-card-section>
        <div class="text-h6">Recherche de salles</div>
      </q-card-section>

      <q-card-section>
        <q-form class="q-gutter-md" @submit="onSearch" @reset="resetForm">
          <q-input v-model="form.name" label="Nom" maxlength="100" stack-label />
          <q-input v-model="form.location" label="Localisation" maxlength="255" stack-label />
          <q-input
            v-model.number="form.capacityMin"
            type="number"
            label="Capacité minimale"
            :rules="rules.capacityMin"
            stack-label
          />
          <q-input
            v-model.number="form.capacityMax"
            type="number"
            label="Capacité maximale"
            :rules="rules.capacityMax"
            stack-label
          />
          <q-select
            v-model="form.type"
            :options="roomTypeOptions"
            label="Type"
            emit-value
            map-options
            clearable
            stack-label
          />
          <q-select
            v-model="form.equipments"
            label="Équipements"
            hint="Saisissez un équipement puis validez avec Entrée"
            multiple
            use-input
            use-chips
            new-value-mode="add-unique"
            hide-dropdown-icon
            stack-label
          />

          <q-input
            v-model="form.startAt"
            label="Début de la période"
            mask="####-##-## ##:##"
            :rules="rules.startAt"
            clearable
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
            label="Fin de la période"
            mask="####-##-## ##:##"
            :rules="rules.endAt"
            clearable
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

          <div v-if="errorMessage" class="text-negative">{{ errorMessage }}</div>
          <div class="q-gutter-sm">
            <q-btn type="submit" color="primary" label="Rechercher" :loading="loading" />
            <q-btn type="reset" color="primary" outline label="Réinitialiser" />
          </div>
        </q-form>
      </q-card-section>
    </q-card>

    <RoomsTable
      v-if="searched"
      class="q-mt-md"
      title="Résultats"
      :rows="rooms"
      :loading="loading"
      no-data-label="Aucune salle ne correspond à cette recherche"
    />

    <q-btn class="q-mt-md" color="primary" label="Retour à l'accueil" to="/" />
  </q-page>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { type RoomResponse, type RoomSearchFilter, searchRooms } from '@/api/rooms';
import RoomsTable from '@/components/RoomsTable.vue';
import { roomTypeLabels } from '@/components/room-form';

const roomTypeOptions = Object.entries(roomTypeLabels).map(([value, label]) => ({ value, label }));

const emptyForm = (): RoomSearchFilter => ({
  name: '',
  location: '',
  capacityMin: null,
  capacityMax: null,
  type: null,
  equipments: [],
  startAt: null,
  endAt: null,
});
const form = reactive(emptyForm());

const rooms = ref<RoomResponse[]>([]);
const searched = ref(false);
const loading = ref(false);
const errorMessage = ref<string | null>(null);

function capacityOf(value: unknown): number | null {
  return typeof value === 'number' && Number.isInteger(value) && value > 0 ? value : null;
}

const rules = {
  capacityMin: [
    (value: unknown) => value === null || value === '' || !!capacityOf(value) || 'Entier positif',
  ],
  capacityMax: [
    (value: unknown) => value === null || value === '' || !!capacityOf(value) || 'Entier positif',
    (value: unknown) => {
      const min = capacityOf(form.capacityMin);
      const max = capacityOf(value);
      return !min || !max || min <= max || 'La capacité maximale doit dépasser la minimale';
    },
  ],
  startAt: [
    (value: string | null) =>
      !form.endAt || !!value || 'Renseignez le début et la fin, ou aucun des deux',
  ],
  endAt: [
    (value: string | null) =>
      !form.startAt || !!value || 'Renseignez le début et la fin, ou aucun des deux',
    (value: string | null) =>
      !value || !form.startAt || value > form.startAt || 'La fin doit être après le début',
  ],
};

// "2026-09-12 14:00" -> "2026-09-12T14:00:00", backend LocalDateTime format
function toLocalDateTime(value: string): string {
  return `${value.replace(' ', 'T')}:00`;
}

function toFilter(): RoomSearchFilter {
  return {
    ...form,
    name: form.name.trim(),
    location: form.location.trim(),
    capacityMin: capacityOf(form.capacityMin),
    capacityMax: capacityOf(form.capacityMax),
    equipments: [...form.equipments],
    startAt: form.startAt ? toLocalDateTime(form.startAt) : null,
    endAt: form.endAt ? toLocalDateTime(form.endAt) : null,
  };
}

// Called by q-form reset(), which then clears the validation errors itself
function resetForm() {
  Object.assign(form, emptyForm());
  rooms.value = [];
  searched.value = false;
  errorMessage.value = null;
}

async function onSearch() {
  errorMessage.value = null;
  loading.value = true;
  try {
    rooms.value = await searchRooms(toFilter());
    searched.value = true;
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    loading.value = false;
  }
}
</script>
