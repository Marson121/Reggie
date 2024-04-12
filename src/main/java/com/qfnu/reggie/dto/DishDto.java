
package com.qfnu.reggie.dto;

import com.qfnu.reggie.entity.Dish;
import com.qfnu.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;        // 用于菜品分页展示的时候显示分类名称

    private Integer copies;

}
