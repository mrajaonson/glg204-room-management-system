<template>
  <q-page padding class="row justify-center items-start">
    <div class="col-12 col-md-8 col-lg-6">
      <q-btn
        class="q-mb-md"
        flat
        no-caps
        color="primary"
        icon="arrow_back"
        label="Retour aux salles"
        to="/rooms"
      />

      <q-card flat bordered>
        <q-card-section>
          <div class="text-h6">Modifier la salle</div>
        </q-card-section>
        <q-separator />

        <q-card-section>
          <div v-if="loading" class="flex flex-center q-pa-lg">
            <q-spinner color="primary" size="md" />
          </div>
          <q-form v-else-if="form" class="row q-col-gutter-md" @submit="onSubmit">
            <RoomForm v-model="form" />
            <div class="col-12">
              <q-btn
                type="submit"
                unelevated
                no-caps
                color="primary"
                icon="save"
                label="Enregistrer"
                :loading="saving"
              />
            </div>
          </q-form>
          <q-banner v-if="errorMessage" dense rounded class="bg-red-1 text-negative q-mt-md">
            {{ errorMessage }}
          </q-banner>
        </q-card-section>
      </q-card>
    </div>
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
