package demo;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
@WebAppConfiguration
public class DemoApplicationTests {

    @Autowired
    private DemoApplication.BookRepository repository;

    @Test
    public void testFindOne() throws Exception {
        DemoApplication.Book book = repository.findOne("9784101327457");
        assertThat(book, is(notNullValue()));
        assertThat(book.getBookTitle(), is("天国と地獄"));
        assertThat(book.getDateOfPublication(), is(Date.from(LocalDate.of(2011, 5, 1).atStartOfDay(
            ZoneId.systemDefault()).toInstant())));
    }

    @Test
    public void testFindAll() throws Exception {
        List<DemoApplication.Book> books = repository.findAll();
        assertThat(books, is(notNullValue()));
        assertThat(books.size(), is(not(0)));
    }
}
