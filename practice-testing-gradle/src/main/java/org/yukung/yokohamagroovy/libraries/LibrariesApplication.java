package org.yukung.yokohamagroovy.libraries;

import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
public class LibrariesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibrariesApplication.class, args);
    }

    @Bean
    DataSource dataSource(DataSourceProperties dataSourceProperties) {
        return new DataSourceSpy(DataSourceBuilder.create(dataSourceProperties.getClassLoader())
                .url(dataSourceProperties.getUrl())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .build());
    }
}
