package org.yukung.yokohamagroovy.libraries.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.yukung.yokohamagroovy.libraries.entity.Book;
import org.yukung.yokohamagroovy.libraries.service.book.BookService;

/**
 * @author yukung
 */
@RestController
@RequestMapping("/api/books")
public class BookRestController {

    @Autowired
    private BookService bookService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Book postUsers(@RequestBody Book book) {
        return bookService.create(book);
    }

    @RequestMapping(method = RequestMethod.GET, path = "{id}")
    public Book getBooks(@PathVariable("id") String isbn) {
        return bookService.find(isbn);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "{id}")
    public void putBooks(@PathVariable("id") String isbn, @RequestBody Book book) {
        bookService.update(book);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "{id}")
    public void deleteBooks(@PathVariable("id") String isbn) {
        bookService.delete(isbn);
    }
}

