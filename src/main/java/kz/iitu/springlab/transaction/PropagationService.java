package kz.iitu.springlab.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropagationService {

    private final AuditService auditService;

    public PropagationService(AuditService auditService) {
        this.auditService = auditService;
    }

    @Transactional
    public void runWithRequiredAuditAndFail() {
        auditService.writeRequired("REQUIRED audit");
        throw new IllegalStateException("Outer transaction failed after REQUIRED audit");
    }

    @Transactional
    public void runWithRequiresNewAuditAndFail() {
        auditService.writeRequiresNew("REQUIRES_NEW audit");
        throw new IllegalStateException("Outer transaction failed after REQUIRES_NEW audit");
    }
}
