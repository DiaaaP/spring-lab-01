package kz.iitu.springlab.catalog;
import java.util.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public class BookRepository {
    private final CatalogueRows rows;
    public BookRepository(CatalogueRows rows){this.rows=rows;}
    public List<Book> findAll(){return rows.findAll().stream().map(CatalogueBook::asBook).toList();}
    public Optional<Book> findById(long id){return rows.findById(id).map(CatalogueBook::asBook);}
    @Transactional
    public Book save(Book book){
        CatalogueBook row=book.id()==null ? new CatalogueBook(book.title(),book.author(),book.year())
                : rows.findById(book.id()).orElseThrow();
        row.update(book);
        return rows.save(row).asBook();
    }
    @Transactional
    public boolean deleteById(long id){
        if (!rows.existsById(id)) return false;
        rows.deleteById(id);
        return true;
    }
}
