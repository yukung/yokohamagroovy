package org.yukung.yokohamagroovy.libraries.verifier;

import jp.classmethod.testing.fixtures.FixtureUtils;
import org.yukung.yokohamagroovy.libraries.entity.Book;

import java.sql.Date;
import java.time.LocalDate;

/**
 * @author yukung
 */
public class Fixtures {

    public static class Books {
        public static Book book01() {
            return FixtureUtils.injectTo(new Book())
                    .field("isbn", "978-4-7981-3643-1")
                    .field("bookTitle", "Gradle徹底入門 次世代ビルドツールによる自動化基盤の構築")
                    .field("dateOfPublication", Date.valueOf(LocalDate.of(2014, 11, 4)))
                    .returnObject();
        }
    }
}
