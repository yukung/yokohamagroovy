package org.yukung.yokohamagroovy.libraries.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    public Book postUsers(@RequestBody Book book) {
        return bookService.create(book);
    }

    @RequestMapping(method = RequestMethod.GET, path = "{id}")
    public Book getBooks(@PathVariable("id") String isbn) {
        return bookService.find(isbn);
    }
}
