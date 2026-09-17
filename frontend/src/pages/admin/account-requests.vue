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

      <q-table
        title="Demandes de création de compte"
        :rows="requests"
        :columns="columns"
        row-key="id"
        :loading="loading"
        no-data-label="Aucune demande en attente"
        table-header-class="bg-grey-2 text-grey-8"
        flat
        bordered
      >
        <template #body-cell-actions="props">
          <q-td :props="props" class="q-gutter-sm">
            <q-btn
              unelevated
              no-caps
              color="positive"
              icon="check"
              label="Valider"
              size="sm"
              @click="handle(validateAccountCreationRequest, props.row.id)"
            />
            <q-btn
              outline
              no-caps
              color="negative"
              icon="close"
              label="Refuser"
              size="sm"
              @click="handle(refuseAccountCreationRequest, props.row.id)"
            />
          </q-td>
        </template>
      </q-table>

      <q-banner v-if="errorMessage" dense rounded class="bg-red-1 text-negative q-mt-md">
        {{ errorMessage }}
      </q-banner>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { date, type QTableColumn } from 'quasar';
import {
  type AccountCreationRequestResponse,
  fetchAccountCreationRequests,
  refuseAccountCreationRequest,
  validateAccountCreationRequest,
} from '@/api/accounts';

type RequestStatus = AccountCreationRequestResponse['status'];

const statusLabels: Record<RequestStatus, string> = {
  CREATED: 'Créée',
  EMAIL_SENT: 'Mail de confirmation envoyé',
  EMAIL_VALIDATED: 'Mail validé',
  VALIDATED: 'Validée',
  REFUSED: 'Refusée',
};

const columns: QTableColumn<AccountCreationRequestResponse>[] = [
  { name: 'login', label: 'Identifiant', field: 'login', align: 'left' },
  { name: 'email', label: 'Email', field: 'email', align: 'left' },
  {
    name: 'createdAt',
    label: 'Date de la demande',
    field: 'createdAt',
    align: 'left',
    format: (value: string) => date.formatDate(value, 'DD/MM/YYYY HH:mm'),
  },
  {
    name: 'status',
    label: 'Statut',
    field: 'status',
    align: 'left',
    format: (value: RequestStatus) => statusLabels[value],
  },
  { name: 'actions', label: 'Actions', field: 'id', align: 'right' },
];

const requests = ref<AccountCreationRequestResponse[]>([]);
const loading = ref(false);
const errorMessage = ref<string | null>(null);

async function load() {
  loading.value = true;
  try {
    requests.value = await fetchAccountCreationRequests();
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    loading.value = false;
  }
}

async function handle(action: (id: number) => Promise<void>, id: number) {
  errorMessage.value = null;
  try {
    await action(id);
    requests.value = requests.value.filter((request) => request.id !== id);
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  }
}

onMounted(load);
</script>
