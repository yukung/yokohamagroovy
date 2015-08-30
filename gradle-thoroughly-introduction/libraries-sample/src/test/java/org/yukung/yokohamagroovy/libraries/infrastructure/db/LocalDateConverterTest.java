package org.yukung.yokohamagroovy.libraries.infrastructure.db;

import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
public class LocalDateConverterTest {

    private LocalDateConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new LocalDateConverter();
    }

    @Test
    public void testDateToLocalDate() throws Exception {
        LocalDate localDate = converter.from(Date.valueOf("2015-01-01"));
        assertThat(localDate, is(LocalDate.parse("2015-01-01")));
    }

    @Test
    public void testLocalDateToDate() throws Exception {
        Date date = converter.to(LocalDate.parse("2015-01-01"));
        assertThat(date, is(Date.valueOf("2015-01-01")));
    }

    @Test
    public void testFromType() throws Exception {
        assertEquals(Date.class, converter.fromType());
    }

    @Test
    public void testToType() throws Exception {
        assertEquals(LocalDate.class, converter.toType());
    }
}
