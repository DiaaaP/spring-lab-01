package kz.iitu.springlab.catalog;

public record Book(Long id, @jakarta.validation.constraints.NotBlank String title,
                   @jakarta.validation.constraints.NotBlank String author, int year) { }
