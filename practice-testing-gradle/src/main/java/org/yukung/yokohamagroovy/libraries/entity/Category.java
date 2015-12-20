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
public class Category {
    private Long categoryId;
    private String categoryName;
}
