package kz.iitu.springlab.account;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OptimisticLockingTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void rejectsUpdateFromStalePersistenceContext() {
        jdbcTemplate.update(
                "UPDATE accounts SET balance = 1000.00, version = 0 WHERE id = 1"
        );

        EntityManager firstContext = entityManagerFactory.createEntityManager();
        EntityManager secondContext = entityManagerFactory.createEntityManager();

        try {
            firstContext.getTransaction().begin();
            secondContext.getTransaction().begin();

            Account firstCopy = firstContext.find(Account.class, 1L);
            Account staleSecondCopy = secondContext.find(Account.class, 1L);

            System.out.printf(
                    "BOTH CONTEXTS LOADED: first version=%d, second version=%d%n",
                    firstCopy.getVersion(),
                    staleSecondCopy.getVersion()
            );

            firstCopy.credit(new BigDecimal("10.00"));
            firstContext.getTransaction().commit();

            System.out.printf(
                    "FIRST COMMIT: balance=%s, version=%d%n",
                    firstCopy.getBalance(),
                    firstCopy.getVersion()
            );

            staleSecondCopy.credit(new BigDecimal("20.00"));

            RuntimeException conflict = null;
            try {
                secondContext.getTransaction().commit();
            } catch (RuntimeException exception) {
                conflict = exception;
            }

            OptimisticLockException optimisticLock = findOptimisticLockException(conflict);
            assertThat(optimisticLock).isNotNull();

            System.out.printf(
                    "SECOND COMMIT CONFLICT: %s%n",
                    optimisticLock.getClass().getSimpleName()
            );

            BigDecimal finalBalance = jdbcTemplate.queryForObject(
                    "SELECT balance FROM accounts WHERE id = 1",
                    BigDecimal.class
            );
            Long finalVersion = jdbcTemplate.queryForObject(
                    "SELECT version FROM accounts WHERE id = 1",
                    Long.class
            );

            System.out.printf(
                    "FINAL DATABASE VALUE: balance=%s, version=%d%n",
                    finalBalance,
                    finalVersion
            );

            assertThat(finalBalance).isEqualByComparingTo("1010.00");
            assertThat(finalVersion).isEqualTo(1L);
        } finally {
            rollbackIfActive(firstContext);
            rollbackIfActive(secondContext);
            firstContext.close();
            secondContext.close();
        }
    }

    private OptimisticLockException findOptimisticLockException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof OptimisticLockException optimisticLockException) {
                return optimisticLockException;
            }
            current = current.getCause();
        }
        return null;
    }

    private void rollbackIfActive(EntityManager entityManager) {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
    }
}
