package kz.iitu.springlab.loan;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LoanRepository extends JpaRepository<Loan,Long> {}
