package kz.iitu.springlab.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void transfersMoneyFromAliceToBob() {
        resetBalances();

        Account aliceBefore = repository.findById(1L).orElseThrow();
        Account bobBefore = repository.findById(2L).orElseThrow();

        System.out.printf(
                "BEFORE: Alice=%s, Bob=%s%n",
                aliceBefore.getBalance(),
                bobBefore.getBalance()
        );

        transferService.transfer(1L, 2L, new BigDecimal("100.00"));
        repository.flush();

        Account aliceAfter = repository.findById(1L).orElseThrow();
        Account bobAfter = repository.findById(2L).orElseThrow();

        System.out.printf(
                "AFTER SUCCESSFUL TRANSFER: Alice=%s, Bob=%s%n",
                aliceAfter.getBalance(),
                bobAfter.getBalance()
        );

        assertThat(aliceAfter.getBalance()).isEqualByComparingTo("900.00");
        assertThat(bobAfter.getBalance()).isEqualByComparingTo("600.00");
    }

    @Test
    void rollsBackWhenUncheckedExceptionOccursAfterDebit() {
        resetBalances();

        Account aliceBefore = repository.findById(1L).orElseThrow();
        Account bobBefore = repository.findById(2L).orElseThrow();

        System.out.printf(
                "BEFORE ROLLBACK TEST: Alice=%s, Bob=%s%n",
                aliceBefore.getBalance(),
                bobBefore.getBalance()
        );

        assertThatThrownBy(() -> transferService.transferWithForcedFailure(
                1L,
                2L,
                new BigDecimal("100.00")
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Forced failure after debit");

        Account aliceAfter = repository.findById(1L).orElseThrow();
        Account bobAfter = repository.findById(2L).orElseThrow();

        System.out.println("EXCEPTION: Forced failure after debit");
        System.out.printf(
                "AFTER ROLLBACK: Alice=%s, Bob=%s%n",
                aliceAfter.getBalance(),
                bobAfter.getBalance()
        );

        assertThat(aliceAfter.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(bobAfter.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void rejectsAmountLargerThanBalanceWithoutChangingAccounts() {
        resetBalances();

        assertThatThrownBy(() -> transferService.transfer(
                1L,
                2L,
                new BigDecimal("1500.00")
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Insufficient funds");

        Account aliceAfter = repository.findById(1L).orElseThrow();
        Account bobAfter = repository.findById(2L).orElseThrow();

        System.out.printf(
                "INSUFFICIENT FUNDS: Alice=%s, Bob=%s, balances unchanged=true%n",
                aliceAfter.getBalance(),
                bobAfter.getBalance()
        );

        assertThat(aliceAfter.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(bobAfter.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void comparesCheckedExceptionDefaultAndRollbackFor() {
        resetBalances();

        assertThatThrownBy(() -> transferService.transferWithCheckedFailureWithoutRollback(
                1L,
                2L,
                new BigDecimal("100.00")
        ))
                .isInstanceOf(CheckedTransferException.class)
                .hasMessage("Checked failure after debit");

        Account afterDefaultCheckedException = repository.findById(1L).orElseThrow();
        System.out.printf(
                "CHECKED EXCEPTION DEFAULT: Alice=%s, debit committed=true%n",
                afterDefaultCheckedException.getBalance()
        );
        assertThat(afterDefaultCheckedException.getBalance()).isEqualByComparingTo("900.00");

        resetBalances();

        assertThatThrownBy(() -> transferService.transferWithCheckedFailureAndRollback(
                1L,
                2L,
                new BigDecimal("100.00")
        ))
                .isInstanceOf(CheckedTransferException.class)
                .hasMessage("Checked failure after debit");

        Account afterRollbackFor = repository.findById(1L).orElseThrow();
        Account bobAfterRollbackFor = repository.findById(2L).orElseThrow();

        System.out.printf(
                "CHECKED EXCEPTION rollbackFor: Alice=%s, Bob=%s, rolled back=true%n",
                afterRollbackFor.getBalance(),
                bobAfterRollbackFor.getBalance()
        );

        assertThat(afterRollbackFor.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(bobAfterRollbackFor.getBalance()).isEqualByComparingTo("500.00");
    }

    private void resetBalances() {
        jdbcTemplate.update(
                """
                UPDATE accounts
                SET balance = CASE
                    WHEN id = 1 THEN 1000.00
                    WHEN id = 2 THEN 500.00
                END,
                version = 0
                WHERE id IN (1, 2)
                """
        );
    }
}
