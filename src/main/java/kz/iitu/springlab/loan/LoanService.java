package kz.iitu.springlab.loan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
@Service
public class LoanService {
    private final LoanRepository loans;
    public LoanService(LoanRepository loans){this.loans=loans;}
    public Loan byId(long id){
        return loans.findById(id).orElseThrow(()->new NoSuchElementException("Loan "+id));
    }
    @Transactional
    public Loan extend(long id,int days){
        if (days <= 0) throw new IllegalArgumentException("Extension must be positive");
        Loan loan=byId(id);
        if (loan.hasQueue()) throw new IllegalStateException("Readers are waiting");
        loan.extend(days);
        return loans.save(loan);
    }
}
