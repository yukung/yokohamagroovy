package org.yukung.yokohamagroovy.libraries.infrastructure.db;

import org.junit.Before;
import org.junit.Test;

import java.sql.Time;
import java.time.LocalTime;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author yukung
 */
public class LocalTimeConverterTest {

    private LocalTimeConverter converter;

    @Before
    public void setUp() throws Exception {
        converter = new LocalTimeConverter();
    }

    @Test
    public void testTimeToLocalTime() throws Exception {
        LocalTime localTime = converter.from(Time.valueOf("00:00:00"));
        assertThat(localTime, is(LocalTime.parse("00:00:00")));
    }

    @Test
    public void testLocalTimeToTime() throws Exception {
        Time time = converter.to(LocalTime.parse("00:00:00"));
        assertThat(time, is(Time.valueOf("00:00:00")));
    }

    @Test
    public void testFromType() throws Exception {
        assertEquals(Time.class, converter.fromType());
    }

    @Test
    public void testToType() throws Exception {
        assertEquals(LocalTime.class, converter.toType());
    }
}
