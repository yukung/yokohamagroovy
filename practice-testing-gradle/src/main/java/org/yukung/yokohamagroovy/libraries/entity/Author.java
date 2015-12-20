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
public class Author {
    private Long authorId;
    private String authorFirstname;
    private String authorSurname;
}
