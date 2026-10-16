/**
 * ============================================================
 *  SeatRush — READ PERFORMANCE TEST ⚡
 * ============================================================
 *  Amaç: Koltuk listeleme performansını ölçmek.
 *  Otomatik login olup taze token alır.
 * ============================================================
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';
const EVENT_ID = '8c50a341-2cc7-4dd3-b842-1f260cdd9793'; // 500 koltuklu etkinliğimiz

export const options = {
  stages: [
    { duration: '3s', target: 20 },
    { duration: '7s', target: 20 },
  ],
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

// 🔑 Otomatik Login
export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({
      email: 'k6@test.com',
      password: '123456',
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  if (loginRes.status !== 200) {
    throw new Error(`Login başarısız: ${loginRes.status}`);
  }

  const body = JSON.parse(loginRes.body);
  return body.accessToken;
}

export default function (token) {
  const res = http.get(
    `${BASE_URL}/events/${EVENT_ID}/seats`,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    }
  );

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(0.01);
}
