/**
 * ============================================================
 *  SeatRush — Test Verisi Hazırlama Scripti
 * ============================================================
 *  Bu scripti k6 ile DEĞİL, doğrudan çalıştırıyoruz.
 *  Amacı: Register → Login → Event oluştur → Seat ID'lerini al
 *  Sonra bu ID'leri diğer test scriptlerinde kullanacağız.
 *
 *  Kullanım:
 *    k6 run k6/setup-test-data.js
 *
 *  Çıktıdaki UUID'leri kopyala, diğer scriptlere yapıştır.
 * ============================================================
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 1,
  iterations: 1,
};

const BASE_URL = 'http://localhost:8080';

export default function () {

  // ─── ADIM 1: Admin/Organizer kullanıcı register ───
  console.log('═══════════════════════════════════════════');
  console.log('📝 ADIM 1: Kullanıcı register ediliyor...');
  console.log('═══════════════════════════════════════════');

  // Her çalıştırmada benzersiz kullanıcı oluştur
  const timestamp = Date.now();
  const EMAIL = `loadtest${timestamp}@seatrush.com`;
  const USERNAME = `loadtester${timestamp}`;

  const registerRes = http.post(
    `${BASE_URL}/auth/register`,
    JSON.stringify({
      username: USERNAME,
      email: EMAIL,
      password: 'Test123456',
      firstName: 'Load',
      lastName: 'Tester',
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  console.log(`Register status: ${registerRes.status}`);
  console.log(`Register body: ${registerRes.body}`);

  // ─── ADIM 2: Login → Token al ───
  console.log('\n═══════════════════════════════════════════');
  console.log('🔑 ADIM 2: Login yapılıyor...');
  console.log('═══════════════════════════════════════════');

  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({
      email: EMAIL,
      password: 'Test123456',
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  check(loginRes, {
    'login successful': (r) => r.status === 200,
  });

  const loginBody = JSON.parse(loginRes.body);
  const TOKEN = loginBody.accessToken;

  console.log(`Login status: ${loginRes.status}`);
  console.log(`\n🔑 TOKEN (bunu kopyala!):\n${TOKEN}\n`);

  const authHeaders = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${TOKEN}`,
  };

  // ─── ADIM 3: Event oluştur (ADMIN veya ORGANIZER rolü gerekli) ───
  console.log('═══════════════════════════════════════════');
  console.log('🎪 ADIM 3: Event oluşturuluyor...');
  console.log('═══════════════════════════════════════════');
  console.log('⚠️  NOT: Event oluşturmak için ADMIN veya ORGANIZER rolü lazım.');
  console.log('   Eğer 403 alırsan, DB\'den kullanıcının rolünü ADMIN yap:');
  console.log('   UPDATE users SET role = \'ADMIN\' WHERE email = \'loadtest@seatrush.com\';');
  console.log('   Sonra tekrar login ol.\n');

  const eventRes = http.post(
    `${BASE_URL}/events`,
    JSON.stringify({
      title: 'Load Test Concert',
      description: 'k6 ile test edilen konser',
      startTime: '2026-12-31T20:00:00',
      endTime: '2026-12-31T23:00:00',
      totalCapacity: 10,
    }),
    { headers: authHeaders }
  );

  console.log(`Event status: ${eventRes.status}`);
  console.log(`Event body: ${eventRes.body}`);

  if (eventRes.status === 201) {
    const eventBody = JSON.parse(eventRes.body);
    const EVENT_ID = eventBody.eventId;

    console.log(`\n🎪 EVENT_ID (bunu kopyala!): ${EVENT_ID}\n`);

    // ─── ADIM 4: Koltukları listele ───
    sleep(1); // Event oluşturulduktan sonra seat'ler oluşturulana kadar bekle

    console.log('═══════════════════════════════════════════');
    console.log('💺 ADIM 4: Koltuklar listeleniyor...');
    console.log('═══════════════════════════════════════════');

    const seatsRes = http.get(
      `${BASE_URL}/events/${EVENT_ID}/seats`,
      { headers: authHeaders }
    );

    console.log(`Seats status: ${seatsRes.status}`);

    if (seatsRes.status === 200) {
      const seatsBody = JSON.parse(seatsRes.body);
      console.log(`Toplam koltuk: ${seatsBody.totalSeats}`);

      if (seatsBody.seats && seatsBody.seats.length > 0) {
        console.log('\n💺 SEAT ID\'leri (bunları kopyala!):');
        seatsBody.seats.forEach((seat, i) => {
          console.log(`  Seat ${i + 1}: ${seat.id} (status: ${seat.seatStatus})`);
        });

        const TARGET_SEAT = seatsBody.seats[0].id;

        console.log('\n═══════════════════════════════════════════');
        console.log('✅ HAZIR! Aşağıdaki değerleri diğer test scriptlerine yapıştır:');
        console.log('═══════════════════════════════════════════');
        console.log(`const TOKEN    = '${TOKEN}';`);
        console.log(`const EVENT_ID = '${EVENT_ID}';`);
        console.log(`const SEAT_ID  = '${TARGET_SEAT}';`);
        console.log('═══════════════════════════════════════════');
      }
    }
  } else {
    console.log('\n❌ Event oluşturulamadı! Yukarıdaki NOT\'a bak.');
  }
}
