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

      <div class="text-h5 text-weight-medium q-mb-md">
        {{ roomName ? `Planning — ${roomName}` : 'Planning' }}
      </div>

      <q-banner v-if="errorMessage" dense rounded class="bg-red-1 text-negative q-mb-md">
        {{ errorMessage }}
      </q-banner>
      <div v-if="loading" class="flex flex-center q-pa-lg">
        <q-spinner color="primary" size="md" />
      </div>

      <div v-else class="row q-col-gutter-md">
        <div class="col-12 col-md-auto">
          <q-date
            v-model="selectedDate"
            :events="eventDays"
            :event-color="eventColor"
            flat
            bordered
          />
        </div>

        <div class="col">
          <q-list bordered separator class="rounded-borders bg-white">
            <q-item-label header class="text-weight-medium">Disponibilités</q-item-label>
            <q-item v-for="slot in daySlots" :key="slot.id">
              <q-item-section avatar><q-icon name="schedule" color="positive" /></q-item-section>
              <q-item-section>{{ periodLabel(slot) }}</q-item-section>
            </q-item>
            <q-item v-if="daySlots.length === 0">
              <q-item-section class="text-grey">Aucune disponibilité ce jour</q-item-section>
            </q-item>

            <q-item-label header class="text-weight-medium">Réservations</q-item-label>
            <q-item v-for="reservation in dayReservations" :key="reservation.id">
              <q-item-section avatar><q-icon name="event" color="primary" /></q-item-section>
              <q-item-section>
                <q-item-label>{{ periodLabel(reservation) }}</q-item-label>
                <q-item-label caption>
                  {{ reservation.purpose }} — {{ reservation.requesterLogin }} —
                  {{ reservationStatusLabels[reservation.status] }}
                </q-item-label>
              </q-item-section>
            </q-item>
            <q-item v-if="dayReservations.length === 0">
              <q-item-section class="text-grey">Aucune réservation ce jour</q-item-section>
            </q-item>
          </q-list>
        </div>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { date } from 'quasar';
import { type ReservationResponse, fetchReservations } from '@/api/reservations';
import { type AvailabilitySlotResponse, fetchAvailabilitySlots, fetchRoom } from '@/api/rooms';
import { reservationStatusLabels } from '@/components/reservation-status';

const route = useRoute('/rooms/[id]/planning');
const roomId = Number(route.params.id);

const DAY_FORMAT = 'YYYY/MM/DD';

interface Period {
  startAt: string;
  endAt: string;
}

const roomName = ref<string | null>(null);
const slots = ref<AvailabilitySlotResponse[]>([]);
const reservations = ref<ReservationResponse[]>([]);
const loading = ref(false);
const errorMessage = ref<string | null>(null);
const selectedDate = ref(date.formatDate(Date.now(), DAY_FORMAT));

async function load() {
  loading.value = true;
  try {
    const [room, roomSlots, roomReservations] = await Promise.all([
      fetchRoom(roomId),
      fetchAvailabilitySlots(roomId),
      fetchReservations(roomId),
    ]);
    roomName.value = room.name;
    slots.value = roomSlots;
    reservations.value = roomReservations.filter(
      (reservation) =>
        reservation.status === 'PENDING_APPROVAL' || reservation.status === 'CONFIRMED',
    );
  } catch {
    errorMessage.value = 'Impossible de traiter la demande, veuillez réessayer plus tard';
  } finally {
    loading.value = false;
  }
}

onMounted(load);

function coveredDays(period: Period): string[] {
  const end = new Date(period.endAt).getTime();
  const days: string[] = [];
  for (
    let day = date.startOfDate(new Date(period.startAt), 'day');
    day.getTime() < end;
    day = date.addToDate(day, { days: 1 })
  ) {
    days.push(date.formatDate(day, DAY_FORMAT));
  }
  return days;
}

const availableDays = computed(() => new Set(slots.value.flatMap(coveredDays)));
const reservedDays = computed(() => new Set(reservations.value.flatMap(coveredDays)));
const eventDays = computed(() => [...new Set([...availableDays.value, ...reservedDays.value])]);

function eventColor(day: string): string {
  return reservedDays.value.has(day) ? 'primary' : 'positive';
}

function coversSelectedDay(period: Period): boolean {
  return coveredDays(period).includes(selectedDate.value);
}

const daySlots = computed(() => slots.value.filter(coversSelectedDay));
const dayReservations = computed(() =>
  reservations.value.filter(coversSelectedDay).sort((a, b) => a.startAt.localeCompare(b.startAt)),
);

function boundLabel(value: string): string {
  const sameDay = date.formatDate(value, DAY_FORMAT) === selectedDate.value;
  return date.formatDate(value, sameDay ? 'HH:mm' : 'DD/MM HH:mm');
}

function periodLabel(period: Period): string {
  return `${boundLabel(period.startAt)} – ${boundLabel(period.endAt)}`;
}
</script>
