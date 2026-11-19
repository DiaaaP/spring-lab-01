package kz.iitu.springlab.catalog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CatalogueRows extends JpaRepository<CatalogueBook,Long> {
    Page<CatalogueBook> findByAuthor(String author, Pageable page);
}
