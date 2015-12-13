package org.yukung.yokohamagroovy.libraries.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.yukung.yokohamagroovy.libraries.entity.Author;
import org.yukung.yokohamagroovy.libraries.service.author.AuthorService;

/**
 * @author yukung
 */
@RestController
@RequestMapping("api")
public class AuthorRestController {

    @Autowired
    private AuthorService authorService;

    @RequestMapping(method = RequestMethod.POST, path = "authors")
    public Author postAuthors(@RequestBody Author author) {
        return authorService.create(author);
    }

    @RequestMapping(method = RequestMethod.GET, path = "authors/{id}")
    public Author getAuthors(@PathVariable("id") Long authorId) {
        return authorService.find(authorId);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "authors/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void putAuthors(@PathVariable("id") Long authorId, @RequestBody Author author) {
        authorService.update(author);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "authors/{id}")
    public void deleteAuthors(@PathVariable("id") Long authorId) {
        authorService.delete(authorId);
    }
}
