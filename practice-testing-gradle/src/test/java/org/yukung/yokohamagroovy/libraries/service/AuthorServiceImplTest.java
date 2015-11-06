package org.yukung.yokohamagroovy.libraries.service;

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
        fail();
    }

    @Test
    public void testUpdate() throws Exception {
        fail();
    }

    @Test
    public void testDelete() throws Exception {
        fail();
    }
}
