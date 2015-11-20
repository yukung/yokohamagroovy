package org.yukung.yokohamagroovy.libraries.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yukung.yokohamagroovy.libraries.entity.Author;
import org.yukung.yokohamagroovy.libraries.repository.AuthorsRepository;

/**
 * @author yukung
 */
@Service
public class AuthorServiceImpl implements AuthorService {

    @Autowired
    private AuthorsRepository repository;

    @Override
    public Author create(Author author) {
        return repository.save(author);
    }

    @Override
    public Author find(Long authorId) {
        return repository.findOne(authorId);
    }

    @Override
    public void update(Author author) {

    }

    @Override
    public void delete(Author author) {

    }
}
