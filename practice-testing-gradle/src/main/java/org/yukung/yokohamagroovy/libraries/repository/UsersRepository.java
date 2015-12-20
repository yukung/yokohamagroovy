package org.yukung.yokohamagroovy.libraries.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.yukung.yokohamagroovy.libraries.entity.User;

import javax.annotation.PostConstruct;

/**
 * @author yukung
 */
@Repository
public class UsersRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private SimpleJdbcInsert insert;

    @PostConstruct
    void init() {
        insert = new SimpleJdbcInsert((JdbcTemplate) jdbcTemplate.getJdbcOperations())
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
    }

    public User save(User user) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(user);
        if (user.getUserId() == null) {
            Number key = insert.executeAndReturnKey(param);
            user.setUserId(key.longValue());
        } else {
            jdbcTemplate.update(
                    "UPDATE users " +
                            "SET user_name = :userName," +
                            " user_address = :userAddress," +
                            " phone_number = :phoneNumber," +
                            " email_address = :emailAddress," +
                            " other_user_details = :otherUserDetails " +
                            "WHERE user_id = :userId",
                    param);
        }
        return user;
    }

    public User findOne(Long userId) {
        MapSqlParameterSource param = new MapSqlParameterSource("id", userId);
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT user_id, user_name, user_address, phone_number, email_address, other_user_details " +
                            "FROM users " +
                            "WHERE user_id = :id",
                    param,
                    new BeanPropertyRowMapper<>(User.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void delete(Long userId) {
        SqlParameterSource param = new MapSqlParameterSource().addValue("id", userId);
        jdbcTemplate.update(
                "DELETE FROM users WHERE user_id = :id",
                param);
    }
}
