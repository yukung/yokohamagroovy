package org.yukung.yokohamagroovy.libraries.infrastructure.db;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
public class LocalDateTimeConverterTest {
    private LocalDateTimeConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new LocalDateTimeConverter();
    }

    @Test
    public void testTimestampToLocalDateTime() throws Exception {
        LocalDateTime localDateTime = converter.from(Timestamp.valueOf("2015-01-01 00:00:000"));
        assertThat(localDateTime, is(LocalDateTime.parse("2015-01-01T00:00:00")));
    }

    @Test
    public void testLocalDateTimeToTimestamp() throws Exception {
        Timestamp timestamp = converter.to(LocalDateTime.parse("2015-01-01T00:00:00"));
        assertThat(timestamp, is(Timestamp.valueOf("2015-01-01 00:00:00")));
    }

    @Test
    public void testFromType() throws Exception {
        assertEquals(Timestamp.class, converter.fromType());
    }

    @Test
    public void testToType() throws Exception {
        assertEquals(LocalDateTime.class, converter.toType());
    }
}
