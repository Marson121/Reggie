package com.qfnu.reggie.dto;

import com.qfnu.reggie.entity.Setmeal;
import com.qfnu.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
