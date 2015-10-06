package demo;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@SpringBootApplication
@Controller
public class DemoApplication {

    @Autowired
    private BookRepository bookRepository;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @RequestMapping("/")
    public String sayHello(@RequestParam(name = "name", defaultValue = "World") String name, Model model) {
        model.addAttribute("message", "Hello, " + name + "!");
        return "index";
    }

    @RequestMapping("/books")
    public ModelAndView books() {
        List<Book> books = bookRepository.findAll();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("books", books);
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Book {
        private String isbn;
        private String bookTitle;
        private Date dateOfPublication;
    }

    @Repository
    public static class BookRepository {
        @Autowired
        private JdbcTemplate jdbc;

        public Book findOne(String isbn) {
            return jdbc.queryForObject("SELECT * FROM books WHERE isbn = ?", new Object[] { isbn },
                (rs, rowNum) -> {
                    return new Book(rs.getString("isbn"), rs.getString("book_title"),
                        rs.getDate("date_of_publication"));
                });
        }

        public List<Book> findAll() {
            return jdbc.query("SELECT * FROM books", new BeanPropertyRowMapper<>(Book.class));
        }
    }
}
