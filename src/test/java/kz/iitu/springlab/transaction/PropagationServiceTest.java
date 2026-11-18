package kz.iitu.springlab.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PropagationServiceTest {

    @Autowired
    private PropagationService propagationService;

    @Autowired
    private AuditRecordRepository auditRepository;

    @Test
    void comparesRequiredAndRequiresNewAfterOuterRollback() {
        auditRepository.deleteAll();

        assertThatThrownBy(propagationService::runWithRequiredAuditAndFail)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Outer transaction failed after REQUIRED audit");

        long requiredCount = auditRepository.count();
        System.out.printf(
                "REQUIRED: outer transaction rolled back, audit records=%d%n",
                requiredCount
        );

        assertThat(requiredCount).isZero();

        assertThatThrownBy(propagationService::runWithRequiresNewAuditAndFail)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Outer transaction failed after REQUIRES_NEW audit");

        long requiresNewCount = auditRepository.count();
        System.out.printf(
                "REQUIRES_NEW: outer transaction rolled back, audit records=%d%n",
                requiresNewCount
        );

        assertThat(requiresNewCount).isEqualTo(1L);
    }
}
