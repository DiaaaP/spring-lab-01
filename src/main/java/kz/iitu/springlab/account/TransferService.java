package kz.iitu.springlab.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountRepository repository;

    public TransferService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        Account to = repository.findById(toId).orElseThrow();

        from.debit(amount);
        to.credit(amount);
        repository.save(from);
        repository.save(to);
    }

    @Transactional
    public void transferWithForcedFailure(Long fromId, Long toId, BigDecimal amount) {
        Account from = repository.findById(fromId).orElseThrow();
        repository.findById(toId).orElseThrow();

        from.debit(amount);
        repository.saveAndFlush(from);

        throw new IllegalStateException("Forced failure after debit");
    }

    @Transactional
    public void transferWithCheckedFailureWithoutRollback(
            Long fromId,
            Long toId,
            BigDecimal amount
    ) throws CheckedTransferException {
        Account from = repository.findById(fromId).orElseThrow();
        repository.findById(toId).orElseThrow();

        from.debit(amount);
        repository.saveAndFlush(from);

        throw new CheckedTransferException("Checked failure after debit");
    }

    @Transactional(rollbackFor = CheckedTransferException.class)
    public void transferWithCheckedFailureAndRollback(
            Long fromId,
            Long toId,
            BigDecimal amount
    ) throws CheckedTransferException {
        Account from = repository.findById(fromId).orElseThrow();
        repository.findById(toId).orElseThrow();

        from.debit(amount);
        repository.saveAndFlush(from);

        throw new CheckedTransferException("Checked failure after debit");
    }
}
