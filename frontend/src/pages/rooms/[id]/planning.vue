<template>
  <q-page padding>
    <div class="text-h6 q-mb-md">{{ roomName ? `Planning — ${roomName}` : 'Planning' }}</div>

    <div v-if="errorMessage" class="text-negative q-mb-md">{{ errorMessage }}</div>
    <q-spinner v-if="loading" color="primary" size="md" />

    <div v-else class="row q-col-gutter-md">
      <div class="col-12 col-md-auto">
        <q-date v-model="selectedDate" :events="eventDays" :event-color="eventColor" />
      </div>

      <div class="col">
        <q-list bordered separator>
          <q-item-label header>Disponibilités</q-item-label>
          <q-item v-for="slot in daySlots" :key="slot.id">
            <q-item-section>{{ periodLabel(slot) }}</q-item-section>
          </q-item>
          <q-item v-if="daySlots.length === 0">
            <q-item-section class="text-grey">Aucune disponibilité ce jour</q-item-section>
          </q-item>

          <q-item-label header>Réservations</q-item-label>
          <q-item v-for="reservation in dayReservations" :key="reservation.id">
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

    <q-btn class="q-mt-md" color="primary" label="Retour aux salles" to="/rooms" />
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
