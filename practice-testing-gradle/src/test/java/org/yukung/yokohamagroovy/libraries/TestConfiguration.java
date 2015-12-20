package org.yukung.yokohamagroovy.libraries;

import jp.classmethod.testing.database.DataSourceDbUnitTester;
import jp.classmethod.testing.database.DbUnitTester;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author yukung
 */
@Configuration
public class TestConfiguration {

    @Bean
    DbUnitTester dbUnitTester(DataSource dataSource) {
        return DataSourceDbUnitTester.forDataSource(dataSource).schema("PUBLIC").create();
//        return new DataSourceDbUnitTester(dataSource);    // これは schema がデフォルトで動く
    }
}
