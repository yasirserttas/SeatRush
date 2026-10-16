/**
 * ============================================================
 *  SeatRush — RACE CONDITION TEST 🚨
 * ============================================================
 *  Amaç: Aynı koltuğa 50 kişinin aynı anda reserve atmasını
 *  simüle et. Lock yoksa birden fazla kişi başarılı olur = BUG!
 *
 *  Kullanım:
 *    k6 run k6/race-condition-test.js
 *
 *  ÖNCE setup-test-data.js'i çalıştır ve aşağıdaki değişkenleri
 *  oradan aldığın gerçek değerlerle değiştir!
 * ============================================================
 */

import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// ═══════════════════════════════════════
// 📝 BURAYA KENDİ DEĞERLERİNİ YAPIŞTIR!
// ═══════════════════════════════════════
const BASE_URL = 'http://localhost:8080';
const TOKEN    = 'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJrNkB0ZXN0LmNvbSIsInJvbGUiOiJBRE1JTiIsInR5cGUiOiJBQ0NFU1MiLCJpYXQiOjE3OTIxNjY4MzMsImV4cCI6MTc5MjE2NzczM30.IIWYMJFQsknb64KlchE0SNinKDU5m8m3EmB1yV3rsINw9j9mkcQt3lQwoKQKAaGl';
const SEAT_ID  = 'cf0f2bb2-b46a-435f-bab8-ad6f9b7b93b4';
// ═══════════════════════════════════════

// Özel metrikler — bunları README'ye koyacaksın!
const successReserve = new Counter('successful_reserves');
const failedReserve  = new Counter('failed_reserves');

export const options = {
  scenarios: {
    // 50 sanal kullanıcı, her biri tam 1 kere dener
    // shared-iterations = hepsi aynı havuzdan çeker, aynı anda başlar
    race_condition: {
      executor: 'shared-iterations',
      vus: 50,
      iterations: 50,
      maxDuration: '30s',
    },
  },
  // Ek: Özet metrikleri göster
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

export default function () {
  // 50 kişi AYNI ANDA aynı koltuğu reserve etmeye çalışıyor!
  const res = http.patch(
    `${BASE_URL}/seats/${SEAT_ID}/reserve`,
    null,
    {
      headers: {
        'Authorization': `Bearer ${TOKEN}`,
        'Content-Type': 'application/json',
      },
    }
  );

  if (res.status === 204) {
    successReserve.add(1);
    console.log(`✅ VU-${__VU} → RESERVE BAŞARILI! (status: ${res.status})`);
  } else {
    failedReserve.add(1);
    console.log(`❌ VU-${__VU} → BAŞARISIZ (status: ${res.status})`);
  }

  check(res, {
    'reserve response received': (r) => r.status === 204 || r.status >= 400,
  });
}

// Test bittikten sonra koltuğun son durumunu kontrol et
export function teardown() {
  const res = http.get(
    `${BASE_URL}/seats/${SEAT_ID}`,
    {
      headers: { 'Authorization': `Bearer ${TOKEN}` },
    }
  );

  console.log('\n');
  console.log('╔═══════════════════════════════════════════════════╗');
  console.log('║        📊 KOLTUK SON DURUMU (TEST SONRASI)       ║');
  console.log('╠═══════════════════════════════════════════════════╣');
  console.log(`║  ${res.body}`);
  console.log('╠═══════════════════════════════════════════════════╣');
  console.log('║  successful_reserves > 1 ise RACE CONDITION VAR! ║');
  console.log('║  successful_reserves = 1 ise DOĞRU ÇALIŞIYOR ✅  ║');
  console.log('╚═══════════════════════════════════════════════════╝');
}
