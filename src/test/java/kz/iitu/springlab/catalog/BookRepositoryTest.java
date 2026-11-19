package kz.iitu.springlab.catalog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.*;
import static org.assertj.core.api.Assertions.*;
@DataJpaTest
class BookRepositoryTest {
    @Autowired CatalogueRows rows;
    @Autowired TestEntityManager em;
    void seed(){
        em.persist(new CatalogueBook("A","Herbert",1965));
        em.persist(new CatalogueBook("B","Herbert",1969));
        em.persist(new CatalogueBook("C","Other",1975));
        em.flush(); em.clear();
    }
    @Test void secondPageContainsOnlySecondMatchingTitle(){
        seed();
        var page=rows.findByAuthor("Herbert",PageRequest.of(1,1,Sort.by("title")));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(b->b.asBook().title()).containsExactly("B");
    }
    @Test void excludesBooksByOtherAuthors(){
        seed();
        assertThat(rows.findByAuthor("Other",Pageable.unpaged()).getContent())
            .extracting(b->b.asBook().title()).containsExactly("C");
    }
}
