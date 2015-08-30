package org.yukung.yokohamagroovy.libraries.infrastructure.db;

import org.jooq.Converter;

import java.sql.Time;
import java.time.LocalTime;

/**
 * @author yukung
 */
public class LocalTimeConverter implements Converter<Time, LocalTime> {
    @Override
    public LocalTime from(Time databaseObject) {
        return databaseObject.toLocalTime();
    }

    @Override
    public Time to(LocalTime userObject) {
        return Time.valueOf(userObject);
    }

    @Override
    public Class<Time> fromType() {
        return Time.class;
    }

    @Override
    public Class<LocalTime> toType() {
        return LocalTime.class;
    }
}
