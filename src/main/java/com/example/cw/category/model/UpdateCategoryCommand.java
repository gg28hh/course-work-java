
package com.example.cw.category.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateCategoryCommand {

    private Integer id;
    private Category category;
}
