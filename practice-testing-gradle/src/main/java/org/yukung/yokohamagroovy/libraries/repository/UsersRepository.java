package org.yukung.yokohamagroovy.libraries.repository;

import java.sql.PreparedStatement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.yukung.yokohamagroovy.libraries.entity.User;

/**
 * @author yukung
 */
@Repository
public class UsersRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (user_name, user_address, phone_number, email_address, other_user_details) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, user.getUserName());
            ps.setString(2, user.getUserAddress());
            ps.setString(3, user.getPhoneNumber());
            ps.setString(4, user.getEmailAddress());
            ps.setString(5, user.getOtherUserDetails());
            return ps;
        }, keyHolder);
        user.setUserId(keyHolder.getKey().longValue());
        return user;
    }
}
