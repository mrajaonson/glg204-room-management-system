<template>
  <q-page class="flex flex-center q-pa-md">
    <q-card flat bordered class="q-pa-md text-center" style="width: 100%; max-width: 480px">
      <q-card-section>
        <q-icon name="mark_email_read" color="primary" size="40px" />
        <div class="text-h6 q-mt-sm">Validation de l'adresse mail</div>
      </q-card-section>

      <q-card-section>
        <q-banner v-if="errorMessage" dense rounded class="bg-red-1 text-negative">
          {{ errorMessage }}
        </q-banner>
        <div v-else-if="validated" class="text-grey-8">
          Votre adresse mail est validée. Votre demande est maintenant en attente de validation par
          un administrateur.
        </div>
        <q-spinner v-else color="primary" size="md" />
      </q-card-section>

      <q-card-section>
        <q-btn unelevated no-caps color="primary" label="Retour à l'accueil" to="/" />
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
