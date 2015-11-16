package org.yukung.yokohamagroovy.libraries.repository;

import jp.classmethod.testing.database.DbUnitTester;
import jp.classmethod.testing.database.Fixture;
import jp.classmethod.testing.database.YamlDataSet;
import org.dbunit.dataset.IDataSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yukung.yokohamagroovy.libraries.LibrariesApplication;
import org.yukung.yokohamagroovy.libraries.entity.Author;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(LibrariesApplication.class)
@Fixture(resources = "/fixtures/authors/authors.yml")
public class AuthorsRepositoryTest {

    @Autowired
    private AuthorsRepository repository;

    @Autowired
    @Rule
    public DbUnitTester tester;

    @Test
    public void testInsert() throws Exception {
        // SetUp
        Author author = Author.builder()
                .authorFirstname("John")
                .authorSurname("Doe")
                .build();

        // Exercise
        Author actual = repository.save(author);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getAuthorId(), is(notNullValue()));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/authors/authors-inserted.yml"));
        tester.verifyTable("AUTHORS", expected, "AUTHOR_ID");
    }

    @Test
    public void testRead() throws Exception {
        // Exercise
        Author author = repository.findOne(10L);

        // Verify
        assertThat(author, is(notNullValue()));
        assertThat(author.getAuthorId(), is(notNullValue()));
        assertThat(author.getAuthorFirstname(), is("Micheal"));
        assertThat(author.getAuthorSurname(), is("Jordan"));
    }

    @Test
    public void testUpdate() throws Exception {
        // SetUp
        Author before = repository.findOne(10L);
        String expectedFirstname = "Stephen";
        String expectedSurname = "Curry";
        before.setAuthorFirstname(expectedFirstname);
        before.setAuthorSurname(expectedSurname);

        // Exercise
        repository.save(before);

        // Verify
        Author author = repository.findOne(10L);
        assertThat(author, is(notNullValue()));
        assertThat(author.getAuthorId(), is(notNullValue()));
        assertThat(author.getAuthorFirstname(), is(expectedFirstname));
        assertThat(author.getAuthorSurname(), is(expectedSurname));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/authors/authors-updated.yml"));
        tester.verifyTable("AUTHORS", expected);
    }

    @Test
    public void testDelete() throws Exception {
        // SetUp
        Author author = repository.findOne(10L);
        Long authorId = author.getAuthorId();

        // Exercise
        repository.delete(authorId);

        // Verify
        assertThat(repository.findOne(authorId), is(nullValue()));
        IDataSet expected = YamlDataSet.load(getClass().getResourceAsStream("/fixtures/authors/authors-deleted.yml"));
        tester.verifyTable("AUTHORS", expected);
    }
}
