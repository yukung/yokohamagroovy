package org.yukung.yokohamagroovy.libraries.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yukung
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {
    private Long userId;
    private String userName;
    private String userAddress;
    private String phoneNumber;
    private String emailAddress;
    private String otherUserDetails;
}
