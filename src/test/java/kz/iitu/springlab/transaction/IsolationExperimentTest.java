package kz.iitu.springlab.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IsolationExperimentTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void comparesReadCommittedAndRepeatableReadOnH2() throws Exception {
        IsolationResult readCommitted = runExperiment(
                TransactionDefinition.ISOLATION_READ_COMMITTED
        );
        IsolationResult repeatableRead = runExperiment(
                TransactionDefinition.ISOLATION_REPEATABLE_READ
        );

        String databaseVersion = jdbcTemplate.queryForObject(
                "SELECT H2VERSION()",
                String.class
        );

        System.out.printf("DATABASE: H2 %s%n", databaseVersion);
        System.out.printf(
                "READ_COMMITTED: first=%s, second=%s, changed=%s%n",
                readCommitted.firstRead(),
                readCommitted.secondRead(),
                readCommitted.valueChanged()
        );
        System.out.printf(
                "REPEATABLE_READ: first=%s, second=%s, changed=%s%n",
                repeatableRead.firstRead(),
                repeatableRead.secondRead(),
                repeatableRead.valueChanged()
        );

        assertThat(readCommitted.firstRead()).isEqualByComparingTo("1000.00");
        assertThat(readCommitted.secondRead()).isEqualByComparingTo("1100.00");
        assertThat(repeatableRead.firstRead()).isEqualByComparingTo("1000.00");
        assertThat(repeatableRead.secondRead()).isEqualByComparingTo("1000.00");
    }

    private IsolationResult runExperiment(int isolationLevel) throws Exception {
        jdbcTemplate.update("UPDATE accounts SET balance = 1000.00 WHERE id = 1");

        CountDownLatch firstReadCompleted = new CountDownLatch(1);
        CountDownLatch writerCommitted = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<IsolationResult> reader = executor.submit(() -> {
                TransactionTemplate transaction = transactionTemplate(isolationLevel);

                return transaction.execute(status -> {
                    BigDecimal first = readAliceBalance();
                    firstReadCompleted.countDown();
                    await(writerCommitted);
                    BigDecimal second = readAliceBalance();
                    return new IsolationResult(first, second);
                });
            });

            Future<?> writer = executor.submit(() -> {
                await(firstReadCompleted);

                TransactionTemplate transaction = transactionTemplate(
                        Connection.TRANSACTION_READ_COMMITTED
                );
                transaction.executeWithoutResult(status -> jdbcTemplate.update(
                        "UPDATE accounts SET balance = 1100.00 WHERE id = 1"
                ));
                writerCommitted.countDown();
            });

            IsolationResult result = reader.get(10, TimeUnit.SECONDS);
            writer.get(10, TimeUnit.SECONDS);
            return result;
        } finally {
            executor.shutdownNow();
        }
    }

    private TransactionTemplate transactionTemplate(int isolationLevel) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setIsolationLevel(isolationLevel);
        return transaction;
    }

    private BigDecimal readAliceBalance() {
        return jdbcTemplate.queryForObject(
                "SELECT balance FROM accounts WHERE id = 1",
                BigDecimal.class
        );
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while coordinating transactions");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Transaction experiment was interrupted", exception);
        }
    }

    private record IsolationResult(BigDecimal firstRead, BigDecimal secondRead) {

        boolean valueChanged() {
            return firstRead.compareTo(secondRead) != 0;
        }
    }
}
