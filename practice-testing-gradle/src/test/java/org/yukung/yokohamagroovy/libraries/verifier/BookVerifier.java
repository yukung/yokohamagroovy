package org.yukung.yokohamagroovy.libraries.verifier;

import jp.classmethod.testing.verifier.ArrayVerifier;
import jp.classmethod.testing.verifier.IterableVerifier;
import jp.classmethod.testing.verifier.MapVerifier;
import jp.classmethod.testing.verifier.ObjectVerifier;
import org.yukung.yokohamagroovy.libraries.entity.Book;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author yukung
 */
public class BookVerifier extends ObjectVerifier<Book> {

    public static void verify(Book actual, Book expected) throws Exception {
        new BookVerifier().verifyObject(actual, expected);
    }

    public static void verify(Iterable<Book> actual, Iterable<Book> expected) throws Exception {
        new IterableVerifier<>(new BookVerifier()).verify(actual, expected);
    }

    public static void verify(Book[] actual, Book[] expected) throws Exception {
        new ArrayVerifier<>(new BookVerifier()).verify(actual, expected);
    }

    public static void verify(Map<?, Book> actual, Map<?, Book> expected) throws Exception {
        new MapVerifier<>(new BookVerifier()).verify(actual, expected);
    }

    @Override
    public void verifyNotNullObject(Book actual, Book expected) throws AssertionError, Exception {
        assertThat("isbn", actual.getIsbn(), is(expected.getIsbn()));
        assertThat("bookTitle", actual.getBookTitle(), is(expected.getBookTitle()));
        assertThat("dateOfPublication", actual.getDateOfPublication(), is(expected.getDateOfPublication()));
    }
}
