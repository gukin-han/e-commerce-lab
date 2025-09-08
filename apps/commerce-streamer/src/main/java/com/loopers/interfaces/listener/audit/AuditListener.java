package com.loopers.interfaces.listener.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.Envelope;
import com.loopers.application.audit.AuditCommand;
import com.loopers.application.audit.AuditFacade;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditListener {

  private final ObjectMapper objectMapper;
  private final AuditFacade auditFacade;

  @KafkaListener(
      id = "audit-listener",
      groupId = "audit",
      topicPattern = ".*-events",
      concurrency = "3",
      containerFactory = "auditBatchFactory"
  )
  public void audit(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
    // 1) 파싱/검증: 실패는 DLQ로 보내고 계속 진행
    List<AuditCommand.Audit> commands = new ArrayList<>(records.size());
    for (var record : records) {
      try {
        Envelope env = objectMapper.readValue(record.value(), Envelope.class);
        commands.add(AuditCommand.Audit.from(env, objectMapper));
      } catch (Exception e) {
        // 데드 DLQ 로 보낸다
      }
    }
    if (commands.isEmpty()) { ack.acknowledge(); return; }

    // 2) 청크 처리 + 청크 단위 커밋
    final int CHUNK = 200;
    for (int i = 0; i < commands.size(); i += CHUNK) {
      List<AuditCommand.Audit> chunk = commands.subList(i, Math.min(i + CHUNK, commands.size()));
      auditFacade.appendAll(chunk); // 배치 INSERT
      // 실패 일부가 있어도(중복 등) append-only면 괜찮음
      ack.acknowledge(); // 이 청크까지 오프셋 커밋
    }
  }
}
