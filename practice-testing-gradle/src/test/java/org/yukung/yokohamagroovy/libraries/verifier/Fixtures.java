package org.yukung.yokohamagroovy.libraries.verifier;

import jp.classmethod.testing.fixtures.FixtureUtils;
import org.yukung.yokohamagroovy.libraries.entity.Author;
import org.yukung.yokohamagroovy.libraries.entity.Book;
import org.yukung.yokohamagroovy.libraries.entity.User;

import java.sql.Date;
import java.time.LocalDate;

/**
 * @author yukung
 */
public class Fixtures {

    public static class Users {
        public static User user01() {
            return FixtureUtils.injectTo(new User())
                    .field("userId", 1234L)
                    .field("userName", "John Doe")
                    .field("userAddress", "東京都渋谷区")
                    .field("phoneNumber", "090-1111-1111")
                    .field("emailAddress", "john_doe@example.com")
                    .field("otherUserDetails", "test user1")
                    .returnObject();
        }
        public static User user01_updated() {
            return FixtureUtils.injectTo(new User())
                    .field("userId", 1234L)
                    .field("userName", "名無しの権兵衛")
                    .field("userAddress", "神奈川県横浜市")
                    .field("phoneNumber", "090-2222-2222")
                    .field("emailAddress", "nanashi@example.com")
                    .field("otherUserDetails", "test user1 is updated")
                    .returnObject();
        }
    }

    public static class Books {
        public static Book book01() {
            return FixtureUtils.injectTo(new Book())
                    .field("isbn", "978-4-7981-3643-1")
                    .field("bookTitle", "Gradle徹底入門 次世代ビルドツールによる自動化基盤の構築")
                    .field("dateOfPublication", Date.valueOf(LocalDate.of(2014, 11, 4)))
                    .returnObject();
        }

        public static Book book01_updated() {
            return FixtureUtils.injectTo(new Book())
                    .field("isbn", "978-4-7981-3643-1")
                    .field("bookTitle", "更新後タイトル")
                    .field("dateOfPublication", Date.valueOf(LocalDate.of(2015, 1, 1)))
                    .returnObject();
        }
    }

    public static class Authors {
        public static Author author01() {
            return FixtureUtils.injectTo(new Author())
                    .field("authorId", 10L)
                    .field("authorFirstname", "test")
                    .field("authorSurname", "author1")
                    .returnObject();
        }
    }
}
