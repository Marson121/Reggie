package com.qfnu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qfnu.reggie.dto.DishDto;
import com.qfnu.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {


    /**
     * 新增菜品，同时插入菜品对应的口味数据，同时操作两张表：dish dish_flavor
     * @param dishDto
     */
    public void saveWithFlavor(DishDto dishDto);


    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id);


    /**
     * 修改菜品及口味
     * @param dishDto
     */
    public void updateWithFlavor(DishDto dishDto);


    /**
     * 根据id修改菜品状态
     * @param ids
     */
    public void changeStautsByIds(int status, List<Long> ids);


    /**
     * 删除菜品及口味
     * @param ids
     */
    public void removeByFlavors(List<Long> ids);

}
