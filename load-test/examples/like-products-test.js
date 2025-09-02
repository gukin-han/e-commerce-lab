// like-bench-hot-checks.js
import http from 'k6/http';
import { check, sleep, fail } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// ===== env =====
const BASE      = __ENV.BASE_URL || 'http://localhost:8080';
const MODE      = __ENV.MODE || 'SCHEDULED_REDIS'; // SYNC | ASYNC_EVENT | SCHEDULED_REDIS
const PRODMAX   = parseInt(__ENV.PROD_MAX || '100', 10);
const HOT_ID    = parseInt(__ENV.HOT_KEY_ID || '1', 10);
const HOTR      = parseFloat(__ENV.HOT_RATIO || '0.9'); // 0~1
const LOGIN     = __ENV.LOGIN_HEADER || 'X-USER-ID';
const POLL_MS   = parseInt(__ENV.POLL_MS || '50', 10);
const TIMEOUT_MS= parseInt(__ENV.TIMEOUT_MS || '3000', 10);

// SLA (env로 조절)
const ACK_P95_SLA = parseInt(__ENV.ACK_P95_SLA_MS || '80', 10);
const TTV_P95_SLA = parseInt(__ENV.TTV_P95_SLA_MS || '350', 10);
const NOT_VISIBLE_SLA = parseFloat(__ENV.NOT_VISIBLE_SLA || '0.01'); // 1%

// ===== metrics =====
const ackMs = new Trend('ack_ms');
const ttvMs = new Trend('ttv_ms');
const notVisible = new Rate('not_visible');

// ===== scenario =====
export const options = {
    scenarios: {
        steady_rate: {
            executor: 'constant-arrival-rate',
            rate: parseInt(__ENV.RATE || '1000', 10),
            timeUnit: '1s',
            duration: __ENV.DURATION || '60s',
            preAllocatedVUs: parseInt(__ENV.VUS || '800', 10),
            maxVUs: parseInt(__ENV.MAX_VUS || '1200', 10),
        },
    },
    thresholds: {
        'ack_ms': [`p(95)<${ACK_P95_SLA}`, 'p(99)<200'],    // 예: ACK p99 < 200ms
        'ttv_ms': [`p(95)<${TTV_P95_SLA}`, 'p(99)<2000'],   // 예: TTV p99 < 2s
        'not_visible': [`rate<${NOT_VISIBLE_SLA}`],
    },
    summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

// ===== helpers =====
function pickProductId(rand) {
    if (rand < HOTR) return HOT_ID;
    return 1 + Math.floor(Math.random() * PRODMAX);
}

function getCount(pid) {
    const res = http.get(`${BASE}/api/v1/products/${pid}/likes/count`, { tags: { name: 'GET /likes/count' }});
    const ok = check(res, { 'count 200': (r) => r.status === 200 });
    if (!ok) return NaN;
    try {
        const j = res.json();
        return typeof j === 'number' ? j : j.count;
    } catch {
        return NaN;
    }
}

function postLike(pid, loginId, mode) {
    const headers = {}; headers[LOGIN] = loginId;
    const url = `${BASE}/api/v1/products/${pid}/likes/${mode}`;
    const t0 = Date.now();
    const res = http.post(url, null, { headers, tags: { name: `POST /likes/${mode}` }});
    check(res, { 'like 2xx': (r) => r.status >= 200 && r.status < 300 });
    return Date.now() - t0; // ms
}

function pollUntil(pid, target, timeoutMs, intervalMs) {
    const start = Date.now();
    let last = -Infinity;
    while (Date.now() - start < timeoutMs) {
        const c = getCount(pid);
        if (Number.isFinite(c)) {
            // 단조 증가 체크(감소하면 안 됨)
            check(c, { 'count monotonic': (v) => v >= last });
            last = c;
            if (c >= target) return { t: Date.now() - start, final: c };
        }
        sleep(intervalMs / 1000);
    }
    return { t: -1, final: last };
}

// ===== main =====
export default function () {
    const pid = pickProductId(Math.random());
    const loginId = `u-${__VU}-${__ITER}-${Math.random().toString(36).slice(2, 8)}`;

    // baseline
    const before = getCount(pid);
    check(before, { 'baseline count is number': (v) => Number.isFinite(v) });

    // ACK
    const ack = postLike(pid, loginId, MODE);
    ackMs.add(ack);

    // TTV (가시성 달성까지)
    const target = (Number.isFinite(before) ? before : 0) + 1;
    const { t: ttv, final: afterInPoll } = pollUntil(pid, target, TIMEOUT_MS, POLL_MS);

    if (ttv < 0) {
        notVisible.add(true);
        // 실패 케이스도 드러내기
        check(null, { 'visible within timeout': () => false });
    } else {
        ttvMs.add(ttv);
        notVisible.add(false);
        // 최종 증가 확인(동시성 때문에 >= 만 체크)
        check(afterInPoll, {
            'count increased >= 1': (v) => Number.isFinite(v) && v >= target,
        });
    }
}