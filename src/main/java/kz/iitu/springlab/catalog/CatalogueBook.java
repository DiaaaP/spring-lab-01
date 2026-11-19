package kz.iitu.springlab.catalog;

import jakarta.persistence.*;

@Entity
@Table(name="catalogue_book")
public class CatalogueBook {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String title;
    private String author;
    @Column(name="publication_year") private int year;
    protected CatalogueBook() {}
    public CatalogueBook(String title,String author,int year) {
        this.title=title; this.author=author; this.year=year;
    }
    Book asBook(){ return new Book(id,title,author,year); }
    void update(Book book){ title=book.title(); author=book.author(); year=book.year(); }
}
