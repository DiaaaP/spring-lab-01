package kz.iitu.springlab.aspect;

import kz.iitu.springlab.service.CatalogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component

public class ChangeLogAspect {

    private static final Logger log =
            LoggerFactory.getLogger(ChangeLogAspect.class);

    @AfterReturning(
            pointcut = "execution(* kz.iitu.springlab.service..*Service.update*(..))",
            returning = "result"
    )
    public void logChange(JoinPoint jp, Object result) {
        if (result instanceof CatalogService.ChangeResult change) {
            log.info("[CHANGE] {} old='{}' new='{}'",
                    jp.getSignature().toShortString(),
                    change.oldValue(),
                    change.newValue());
        }
    }
}