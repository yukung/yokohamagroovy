package org.yukung.yokohamagroovy.libraries.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDate;

/**
 * @author yukung
 */
@NoArgsConstructor
@Data
public class Book {
    private String isbn;
    private String bookTitle;
    private Date dateOfPublication;

    public Book(String isbn, String bookTitle, LocalDate dateOfPublication) {
        this.isbn = isbn;
        this.bookTitle = bookTitle;
        this.dateOfPublication = Date.valueOf(dateOfPublication);
    }

    public LocalDate getDateOfPublication() {
        return dateOfPublication.toLocalDate();
    }

    public void setDateOfPublication(LocalDate dateOfPublication) {
        this.dateOfPublication = Date.valueOf(dateOfPublication);
    }
}
