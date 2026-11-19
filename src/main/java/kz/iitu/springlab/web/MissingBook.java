package kz.iitu.springlab.web;
class MissingBook extends RuntimeException {
    MissingBook(long id){super("Book "+id+" was not found");}
}
