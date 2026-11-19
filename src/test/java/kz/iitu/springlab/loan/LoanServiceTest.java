package kz.iitu.springlab.loan;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.*;
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
    @Mock LoanRepository repository;
    @InjectMocks LoanService service;
    LocalDate due=LocalDate.of(2026,11,19);
    @Test void returnsTheRequestedLoan(){
        Loan loan=new Loan(due,0);
        when(repository.findById(3L)).thenReturn(Optional.of(loan));
        assertThat(service.byId(3)).isSameAs(loan);
    }
    @Test void missingLoanNamesTheRequestedId(){
        when(repository.findById(8L)).thenReturn(Optional.empty());
        assertThatThrownBy(()->service.byId(8)).isInstanceOf(NoSuchElementException.class).hasMessage("Loan 8");
    }
    @Test void savesTheExtendedDueDate(){
        when(repository.findById(3L)).thenReturn(Optional.of(new Loan(due,0)));
        service.extend(3,7);
        var saved=ArgumentCaptor.forClass(Loan.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().dueDate()).isEqualTo(due.plusDays(7));
    }
    @Test void zeroDaysAreRejectedBeforeLookingUpTheLoan() {
        assertThatThrownBy(() -> service.extend(3, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Extension must be positive");
        verifyNoInteractions(repository);
    }

    @Test void queuedReadersPreventExtension(){
        when(repository.findById(3L)).thenReturn(Optional.of(new Loan(due,1)));
        assertThatThrownBy(()->service.extend(3,7)).isInstanceOf(IllegalStateException.class);
        verify(repository,never()).save(any());
    }
}
