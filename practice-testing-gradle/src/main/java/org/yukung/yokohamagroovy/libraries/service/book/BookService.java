package org.yukung.yokohamagroovy.libraries.service.book;

import org.yukung.yokohamagroovy.libraries.entity.Book;

/**
 * @author yukung
 */
public interface BookService {
    Book create(Book book);

    Book find(String isbn);

    void update(Book book);

    void delete(String isbn);
}
