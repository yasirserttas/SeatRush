/**
 * ============================================================
 *  SeatRush — KOLTUK DURUMUNU RESET ET 🔄
 * ============================================================
 *  Amaç: Race condition testinden sonra koltuğu tekrar
 *  AVAILABLE'a çevir, böylece testi tekrar koşabilirsin.
 *
 *  Kullanım:
 *    k6 run k6/reset-seat.js
 * ============================================================
 */

import http from 'k6/http';
import { check } from 'k6';

// ═══════════════════════════════════════
// 📝 BURAYA KENDİ DEĞERLERİNİ YAPIŞTIR!
// ═══════════════════════════════════════
const BASE_URL = 'http://localhost:8080';
const TOKEN    = 'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJrNkB0ZXN0LmNvbSIsInJvbGUiOiJBRE1JTiIsInR5cGUiOiJBQ0NFU1MiLCJpYXQiOjE3OTIxNjU5MjYsImV4cCI6MTc5MjE2NjgyNn0._0I1NowLrmCmPmyNktCkCLBj8ivDSlfmpicyEKNL4QLqIIrDi9-1Sl_tvlSq_QoK';
const SEAT_ID  = 'cf0f2bb2-b46a-435f-bab8-ad6f9b7b93b4';
// ═══════════════════════════════════════

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  // Koltuğu release et (RESERVED/SOLD → AVAILABLE)
  const res = http.patch(
    `${BASE_URL}/seats/${SEAT_ID}/release`,
    null,
    {
      headers: {
        'Authorization': `Bearer ${TOKEN}`,
        'Content-Type': 'application/json',
      },
    }
  );

  console.log('═══════════════════════════════════════════');
  console.log(`Release status: ${res.status}`);

  if (res.status === 204) {
    console.log('✅ Koltuk AVAILABLE\'a geri döndü!');
    console.log('   Artık race-condition-test.js\'i tekrar koşabilirsin.');
  } else {
    console.log(`❌ Release başarısız: ${res.body}`);
    console.log('   Koltuğu DB\'den manuel olarak reset edebilirsin:');
    console.log(`   UPDATE seats SET seat_status = 'AVAILABLE' WHERE id = '${SEAT_ID}';`);
  }
  console.log('═══════════════════════════════════════════');

  // Doğrulama: koltuğun durumunu kontrol et
  const checkRes = http.get(
    `${BASE_URL}/seats/${SEAT_ID}`,
    { headers: { 'Authorization': `Bearer ${TOKEN}` } }
  );

  console.log(`\nKoltuk durumu: ${checkRes.body}`);
}
