package org.yukung.yokohamagroovy.libraries.service.author;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.yukung.yokohamagroovy.libraries.entity.Author;
import org.yukung.yokohamagroovy.libraries.repository.AuthorsRepository;
import org.yukung.yokohamagroovy.libraries.verifier.Fixtures;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author yukung
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthorServiceImplTest {

    public static final Long AUTHOR_ID = 2345L;
    public static final String AUTHOR_FIRSTNAME = "test";
    public static final String AUTHOR_SURNAME = "author1";

    @InjectMocks
    private AuthorService service = new AuthorServiceImpl();

    @Mock
    private AuthorsRepository repository;

    @Test
    public void testCreate() throws Exception {
        // SetUp
        Author author = Author.builder()
                .authorFirstname(AUTHOR_FIRSTNAME)
                .authorSurname(AUTHOR_SURNAME)
                .build();
        when(repository.save(author)).thenReturn(Fixtures.Authors.author01());

        // Exercise
        Author actual = service.create(author);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getAuthorId(), is(any(Long.class)));
        assertThat(actual.getAuthorFirstname(), is(AUTHOR_FIRSTNAME));
        assertThat(actual.getAuthorSurname(), is(AUTHOR_SURNAME));
        verify(repository, times(1)).save(author);
    }

    @Test
    public void testFind() throws Exception {
        // SetUp
        when(repository.findOne(AUTHOR_ID)).thenReturn(Fixtures.Authors.author01());

        // Exercise
        Author actual = service.find(AUTHOR_ID);

        // Verify
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getAuthorId(), is(AUTHOR_ID));
        assertThat(actual.getAuthorFirstname(), is(AUTHOR_FIRSTNAME));
        assertThat(actual.getAuthorSurname(), is(AUTHOR_SURNAME));
        verify(repository, times(1)).findOne(AUTHOR_ID);
    }

    @Test
    public void testUpdate() throws Exception {
        // SetUp
        Author author = Fixtures.Authors.author01();
        String UPDATED_AUTHOR_FIRSTNAME = "名無しの";
        String UPDATED_AUTHOR_SURNAME = "権兵衛";
        author.setAuthorFirstname(UPDATED_AUTHOR_FIRSTNAME);
        author.setAuthorSurname(UPDATED_AUTHOR_SURNAME);
        when(repository.save(author)).thenReturn(Fixtures.Authors.author01_updated());
        when(repository.findOne(author.getAuthorId())).thenReturn(Fixtures.Authors.author01_updated());

        // Exercise
        service.update(author);

        // Verify
        Author actual = service.find(author.getAuthorId());
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getAuthorId(), is(author.getAuthorId()));
        assertThat(actual.getAuthorFirstname(), is(author.getAuthorFirstname()));
        assertThat(actual.getAuthorSurname(), is(author.getAuthorSurname()));
        verify(repository, times(1)).save(author);
    }

    @Test
    public void testDelete() throws Exception {
        // SetUp
        Author author = Fixtures.Authors.author01();
        doNothing().when(repository).delete(author.getAuthorId());
        when(repository.findOne(author.getAuthorId())).thenReturn(null);

        // Exercise
        service.delete(author.getAuthorId());

        // Verify
        Author actual = service.find(author.getAuthorId());
        assertThat(actual, is(nullValue()));
        verify(repository, times(1)).delete(author.getAuthorId());
    }
}
