package org.yukung.yokohamagroovy.libraries.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yukung
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Book {
    private String isbn;
    private String bookTitle;
    private Date dateOfPublication;
}
