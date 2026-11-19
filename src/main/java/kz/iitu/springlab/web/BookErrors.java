package kz.iitu.springlab.web;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice(assignableTypes=BookRestController.class)
class BookErrors {
    @ExceptionHandler(MissingBook.class)
    ProblemDetail missing(MissingBook ex){return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,ex.getMessage());}
}
