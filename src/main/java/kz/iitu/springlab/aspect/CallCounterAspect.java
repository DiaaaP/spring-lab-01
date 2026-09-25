package kz.iitu.springlab.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class CallCounterAspect {

    private final ConcurrentHashMap<String, AtomicInteger> counts =
            new ConcurrentHashMap<>();

    @Before("kz.iitu.springlab.aspect.Pointcuts.serviceLayer()")
    public void countCall(JoinPoint jp) {
        String methodName = jp.getSignature().getName();

        counts.computeIfAbsent(
                methodName,
                key -> new AtomicInteger(0)
        ).incrementAndGet();
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> result = new ConcurrentHashMap<>();

        counts.forEach((method, count) ->
                result.put(method, count.get()));

        return result;
    }
}