package org.yukung.yokohamagroovy.libraries.service;

import org.yukung.yokohamagroovy.libraries.entity.Author;

/**
 * @author yukung
 */
public interface AuthorService {
    Author create(Author author);

    Author find(Long authorId);

    void update(Author author);

    void delete(Author author);
}
