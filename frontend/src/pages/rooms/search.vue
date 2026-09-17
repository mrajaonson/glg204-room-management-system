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

      <q-card flat bordered>
        <q-card-section>
          <div class="text-h6">Recherche de salles</div>
        </q-card-section>
        <q-separator />

        <q-card-section>
          <q-form class="row q-col-gutter-md" @submit="onSearch" @reset="resetForm">
            <q-input
              v-model="form.name"
              class="col-12 col-sm-6"
              label="Nom"
              maxlength="100"
              outlined
              stack-label
            />
            <q-input
              v-model="form.location"
              class="col-12 col-sm-6"
              label="Localisation"
              maxlength="255"
              outlined
              stack-label
            />
            <q-input
              v-model.number="form.capacityMin"
              class="col-12 col-sm-6 col-md-3"
              type="number"
              label="Capacité minimale"
              :rules="rules.capacityMin"
              outlined
              stack-label
            />
            <q-input
              v-model.number="form.capacityMax"
              class="col-12 col-sm-6 col-md-3"
              type="number"
              label="Capacité maximale"
              :rules="rules.capacityMax"
              outlined
              stack-label
            />
            <q-select
              v-model="form.type"
              class="col-12 col-md-6"
              :options="roomTypeOptions"
              label="Type"
              emit-value
              map-options
              clearable
              outlined
              stack-label
            />
            <q-select
              v-model="form.equipments"
              class="col-12"
              label="Équipements"
              hint="Saisissez un équipement puis validez avec Entrée"
              multiple
              use-input
              use-chips
              new-value-mode="add-unique"
              hide-dropdown-icon
              outlined
              stack-label
            />

            <q-input
              v-model="form.startAt"
              class="col-12 col-sm-6"
              label="Début de la période"
              mask="####-##-## ##:##"
              :rules="rules.startAt"
              clearable
              outlined
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
              class="col-12 col-sm-6"
              label="Fin de la période"
              mask="####-##-## ##:##"
              :rules="rules.endAt"
              clearable
              outlined
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

            <div v-if="errorMessage" class="col-12 text-negative">{{ errorMessage }}</div>
            <div class="col-12 row justify-end q-gutter-sm">
              <q-btn type="reset" flat no-caps color="primary" label="Réinitialiser" />
              <q-btn
                type="submit"
                unelevated
                no-caps
                color="primary"
                icon="search"
                label="Rechercher"
                :loading="loading"
              />
            </div>
          </q-form>
        </q-card-section>
      </q-card>

      <RoomsTable
        v-if="searched"
        class="q-mt-lg"
        title="Résultats"
        :rows="rooms"
        :loading="loading"
        no-data-label="Aucune salle ne correspond à cette recherche"
      />
    </div>
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
