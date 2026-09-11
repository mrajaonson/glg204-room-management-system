<template>
  <q-page class="flex flex-center">
    <q-card>
      <q-card-section>
        <div class="text-h6">Modifier la salle</div>
      </q-card-section>

      <q-card-section>
        <q-spinner v-if="loading" color="primary" size="md" />
        <q-form v-else-if="form" class="q-gutter-md" @submit="onSubmit">
          <RoomForm v-model="form" />
          <q-btn type="submit" color="primary" label="Enregistrer" :loading="saving" />
        </q-form>
        <div v-if="errorMessage" class="text-negative q-mt-md">{{ errorMessage }}</div>
      </q-card-section>

      <q-card-section>
        <q-btn color="primary" flat label="Retour aux salles" to="/rooms" />
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { isAxiosError } from 'axios';
import { fetchRoom, updateRoom } from '@/api/rooms';
import RoomForm from '@/components/RoomForm.vue';
import { type RoomFormValue, formToRequest, roomToForm } from '@/components/room-form';

const route = useRoute('/rooms/[id]/edit');
const router = useRouter();
const roomId = Number(route.params.id);

const form = ref<RoomFormValue | null>(null);
const loading = ref(true);
const saving = ref(false);
const errorMessage = ref<string | null>(null);

onMounted(async () => {
  try {
    form.value = roomToForm(await fetchRoom(roomId));
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    loading.value = false;
  }
});

async function onSubmit() {
  const request = form.value && formToRequest(form.value);
  if (!request) {
    return;
  }
  errorMessage.value = null;
  saving.value = true;
  try {
    await updateRoom(roomId, request);
    await router.push('/rooms');
  } catch (error) {
    errorMessage.value =
      isAxiosError(error) && error.response?.status === 409
        ? 'Une salle porte déjà ce nom'
        : 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    saving.value = false;
  }
}
</script>
