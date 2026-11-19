package kz.iitu.springlab.loan;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
public class Loan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Version private long version;
    private LocalDate dueDate;
    private int waitingReaders;
    protected Loan(){}
    public Loan(LocalDate dueDate,int waitingReaders){this.dueDate=dueDate;this.waitingReaders=waitingReaders;}
    public LocalDate dueDate(){return dueDate;}
    boolean hasQueue(){return waitingReaders>0;}
    void extend(int days){dueDate=dueDate.plusDays(days);}
}
