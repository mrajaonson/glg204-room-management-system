<template>
  <q-page class="flex flex-center">
    <q-card>
      <q-card-section>
        <div class="text-h6">Validation de l'adresse mail</div>
      </q-card-section>

      <q-card-section>
        <div v-if="errorMessage" class="text-negative">{{ errorMessage }}</div>
        <div v-else-if="validated">
          Votre adresse mail est validée. Votre demande est maintenant en attente de validation par un administrateur.
        </div>
        <q-spinner v-else color="primary" size="md" />
      </q-card-section>

      <q-card-section>
        <q-btn color="primary" label="Retour à l'accueil" to="/" />
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { isAxiosError } from 'axios';
import { validateEmail } from '@/api/accounts';

const route = useRoute();

const validated = ref(false);
const errorMessage = ref<string | null>(null);

onMounted(async () => {
  const token = route.query.token;
  if (typeof token !== 'string' || !token) {
    errorMessage.value = 'Lien de validation incomplet';
    return;
  }
  try {
    await validateEmail(token);
    validated.value = true;
  } catch (error) {
    errorMessage.value =
      isAxiosError(error) && error.response?.status === 404
        ? 'Ce lien de validation est invalide ou a déjà été utilisé'
        : 'Impossible de traiter la demande, veuillez réessayer plus tard';
  }
});
</script>
