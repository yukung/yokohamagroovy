package org.yukung.yokohamagroovy.libraries.service.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yukung.yokohamagroovy.libraries.entity.Book;
import org.yukung.yokohamagroovy.libraries.repository.BooksRepository;

/**
 * @author yukung
 */
@Service
public class BookServiceImpl implements BookService {

    @Autowired
    private BooksRepository repository;

    @Override
    public Book create(Book book) {
        return repository.save(book);
    }

    @Override
    public Book find(String isbn) {
        return repository.findOne(isbn);
    }

    @Override
    public void update(Book book) {
        repository.save(book);
    }

    @Override
    public void delete(String isbn) {
        repository.delete(isbn);
    }
}
