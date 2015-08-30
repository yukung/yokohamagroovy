package org.yukung.yokohamagroovy.libraries.domain.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.yukung.yokohamagroovy.libraries.App;
import org.yukung.yokohamagroovy.libraries.domain.tables.pojos.Books;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = App.class)
@Transactional
public class BooksRepositoryTest {

    @Autowired
    private BooksRepository booksRepository;

    @Test
    @Rollback
    public void testInsert() throws Exception {
        Books books = new Books("9784101092058", "銀河鉄道の夜", LocalDate.of(1989, 6, 15));
        Books created = booksRepository.save(books);
        assertThat(created, is(notNullValue()));
        Books fetched = booksRepository.findOne("9784101092058");
        assertThat(fetched, is(notNullValue()));
        assertThat(fetched.getIsbn(), is(equalTo(created.getIsbn())));
    }

    @Test
    @Rollback
    public void testUpdate() throws Exception {
        Books books = new Books("9784101092058", "銀河鉄道の夜", LocalDate.of(1989, 6, 15));
        Books created = booksRepository.save(books);
        String title = "新銀河鉄道の夜";
        created.setBookTitle(title);
        booksRepository.save(created);
        Books result = booksRepository.findOne(created.getIsbn());
        assertThat(result.getIsbn(), is(equalTo(created.getIsbn())));
        assertThat(result.getBookTitle(), is(equalTo(title)));
    }

    @Test
    @Rollback
    public void testDelete() throws Exception {
        Books books = new Books("9784101092058", "銀河鉄道の夜", LocalDate.of(1989, 6, 15));
        Books created = booksRepository.save(books);
        assertThat(booksRepository.findOne(created.getIsbn()), is(notNullValue()));
        booksRepository.delete(created);
        assertThat(booksRepository.findOne(created.getIsbn()), is(nullValue()));
    }

    @Test
    @Rollback
    public void testFindAll() throws Exception {
        List<Books> bookses = Arrays.asList(new Books("9784101092058", "銀河鉄道の夜", LocalDate.of(1989, 6, 15)),
                new Books("12345678901", "あいうえお", LocalDate.of(2015, 1, 1)),
                new Books("98765432109", "かきくけこ", LocalDate.of(2047, 12, 31)));
        bookses.forEach(booksRepository::save);
        List<Books> result = booksRepository.findAll();
        assertThat(result, is(notNullValue()));
        assertThat(result, hasSize(3));
    }
}
