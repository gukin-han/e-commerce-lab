package com.loopers.interfaces.api.test;

import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeCountFlusher {

    private static final String DIRTY_SET = "like:dirty";
    private static final int BATCH = 500; // 한번에 처리할 최대 productId 수

    private final StringRedisTemplate redis;
    private final ProductRepository productRepository;

    // Lua: counter -> pending 으로 원자적 이동 + dirty 제거, 이동한 델타 반환
    private static final String LUA_MOVE_TO_PENDING = """
        -- KEYS[1] = counter key (like:count:{pid})
        -- KEYS[2] = pending key (like:pending:{pid})
        -- KEYS[3] = dirty set key (like:dirty)
        -- ARGV[1] = pid (string)
        local v = redis.call('GET', KEYS[1])
        if not v then v = '0' end
        local n = tonumber(v)
        if n == 0 then
            redis.call('SREM', KEYS[3], ARGV[1])
            return 0
        end
        redis.call('SET', KEYS[1], 0)
        redis.call('SREM', KEYS[3], ARGV[1])
        redis.call('INCRBY', KEYS[2], n)
        return n
        """;

    // Lua: DB 커밋 성공 시 pending 감소(0이면 삭제)
    private static final String LUA_COMMIT_PENDING = """
        -- KEYS[1] = pending key (like:pending:{pid})
        -- ARGV[1] = committed amount
        local v = tonumber(redis.call('GET', KEYS[1]) or '0')
        local c = tonumber(ARGV[1])
        if c == 0 or v == 0 then return 0 end
        local remain = v - c
        if remain <= 0 then
            redis.call('DEL', KEYS[1])
        else
            redis.call('SET', KEYS[1], remain)
        end
        return c
        """;

    // Lua: DB 반영 실패 시 pending -> counter로 복원하고 dirty 재등록
    private static final String LUA_ROLLBACK_PENDING = """
        -- KEYS[1] = pending key (like:pending:{pid})
        -- KEYS[2] = counter key (like:count:{pid})
        -- KEYS[3] = dirty set key (like:dirty)
        -- ARGV[1] = pid
        local v = tonumber(redis.call('GET', KEYS[1]) or '0')
        if v == 0 then return 0 end
        redis.call('DEL', KEYS[1])
        redis.call('INCRBY', KEYS[2], v)
        redis.call('SADD', KEYS[3], ARGV[1])
        return v
        """;

    private final DefaultRedisScript<Long> moveToPendingScript =
            new DefaultRedisScript<>(LUA_MOVE_TO_PENDING, Long.class);
    private final DefaultRedisScript<Long> commitPendingScript =
            new DefaultRedisScript<>(LUA_COMMIT_PENDING, Long.class);
    private final DefaultRedisScript<Long> rollbackPendingScript =
            new DefaultRedisScript<>(LUA_ROLLBACK_PENDING, Long.class);

    private static String counterKey(String pid) { return "like:count:" + pid; }
    private static String pendingKey(String pid) { return "like:pending:" + pid; }

    @Scheduled(fixedDelayString = "${like.redis-flush-interval-ms:200}") // 기본 200ms 주기
    @Transactional
    public void flushLikeCounts() {
        for (int i = 0; i < BATCH; i++) {
            String pid = redis.opsForSet().pop(DIRTY_SET); // 소유권 확보
            if (pid == null) break;

            Long delta = redis.execute(
                    moveToPendingScript,
                    Arrays.asList(counterKey(pid), pendingKey(pid), DIRTY_SET),
                    pid
            );
            if (delta == null || delta == 0L) {
                continue; // 처리할 델타 없음
            }

            try {
                // DB에 증분 반영 (QueryDSL: like_count = like_count + :delta)
                productRepository.updateLikeCount(Long.valueOf(pid), delta);

                // 커밋: pending 감소/삭제
                redis.execute(
                        commitPendingScript,
                        Collections.singletonList(pendingKey(pid)),
                        String.valueOf(delta)
                );
            } catch (Exception e) {
                // 롤백: pending -> counter 복구 + dirty 재등록
                redis.execute(
                        rollbackPendingScript,
                        Arrays.asList(pendingKey(pid), counterKey(pid), DIRTY_SET),
                        pid
                );
                log.error("Redis like counter flush failed for pid={} delta={}", pid, delta, e);
                // 트랜잭션 롤백 유도 (선택)
                // throw e;
            }
        }
    }
}
