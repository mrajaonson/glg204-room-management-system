<template>
  <q-page padding class="row justify-center items-start">
    <div class="col-12 col-lg-10 col-xl-8">
      <q-btn
        class="q-mb-md"
        flat
        no-caps
        color="primary"
        icon="arrow_back"
        label="Retour aux salles"
        to="/rooms"
      />

      <q-table
        :title="roomName ? `Équipements — ${roomName}` : 'Équipements'"
        :rows="equipments"
        :columns="columns"
        row-key="id"
        :loading="loading"
        no-data-label="Aucun équipement"
        table-header-class="bg-grey-2 text-grey-8"
        flat
        bordered
      />

      <q-banner v-if="errorMessage" dense rounded class="bg-red-1 text-negative q-mt-md">
        {{ errorMessage }}
      </q-banner>

      <q-card v-if="userStore.isManager" class="q-mt-lg" flat bordered>
        <q-card-section>
          <div class="text-h6">Ajouter un équipement</div>
        </q-card-section>
        <q-separator />

        <q-card-section>
          <q-form ref="equipmentForm" class="q-gutter-md" @submit="onSubmit" @reset="resetForm">
            <q-input
              v-model="form.name"
              label="Nom"
              maxlength="100"
              :rules="rules.name"
              outlined
              stack-label
            />
            <q-input
              v-model="form.description"
              type="textarea"
              label="Description"
              maxlength="500"
              outlined
              stack-label
            />

            <div v-if="successMessage" class="text-positive">{{ successMessage }}</div>
            <div v-if="createErrorMessage" class="text-negative">{{ createErrorMessage }}</div>
            <q-btn
              type="submit"
              unelevated
              no-caps
              color="primary"
              icon="add"
              label="Ajouter"
              :loading="creating"
            />
          </q-form>
        </q-card-section>
      </q-card>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, useTemplateRef } from 'vue';
import { useRoute } from 'vue-router';
import type { QForm, QTableColumn } from 'quasar';
import { type EquipmentResponse, addEquipment, fetchEquipments, fetchRoom } from '@/api/rooms';
import { useUserStore } from '@/stores/user-store';

const userStore = useUserStore();
const route = useRoute('/rooms/[id]/equipments');
const roomId = Number(route.params.id);

const columns: QTableColumn<EquipmentResponse>[] = [
  { name: 'name', label: 'Nom', field: 'name', align: 'left' },
  { name: 'description', label: 'Description', field: 'description', align: 'left' },
];

const roomName = ref<string | null>(null);
const equipments = ref<EquipmentResponse[]>([]);
const loading = ref(false);
const errorMessage = ref<string | null>(null);

async function load() {
  loading.value = true;
  try {
    const [room, roomEquipments] = await Promise.all([fetchRoom(roomId), fetchEquipments(roomId)]);
    roomName.value = room.name;
    equipments.value = roomEquipments;
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    loading.value = false;
  }
}

onMounted(load);

const equipmentForm = useTemplateRef<QForm>('equipmentForm');
const emptyForm = () => ({ name: '', description: '' });
const form = reactive(emptyForm());
const creating = ref(false);
const successMessage = ref<string | null>(null);
const createErrorMessage = ref<string | null>(null);

const rules = {
  name: [(value: string) => !!value.trim() || 'Nom obligatoire'],
};

function resetForm() {
  Object.assign(form, emptyForm());
}

async function onSubmit() {
  successMessage.value = null;
  createErrorMessage.value = null;
  creating.value = true;
  try {
    await addEquipment(roomId, {
      name: form.name.trim(),
      description: form.description.trim() || null,
    });
    successMessage.value = 'Équipement ajouté';
    equipmentForm.value?.reset();
    await load();
  } catch {
    createErrorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    creating.value = false;
  }
}
</script>
