package org.yukung.yokohamagroovy.libraries.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
}
