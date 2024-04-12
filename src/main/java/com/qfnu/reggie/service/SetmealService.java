package com.qfnu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qfnu.reggie.dto.SetmealDto;
import com.qfnu.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {


    /**
     * 把套餐基本信息以及套餐和菜品关系保存到表中   setmeal  setmeal_dish
     */
    public void saveWithDish(SetmealDto setmealDto);



    /**
     * 删除套餐以及套餐和菜品关联关系
     * @param ids
     */
    public void removeWithDish(List<Long> ids);



    /**
     * 修改套餐的状态为起售还是停售
     * @param status
     * @param ids
     */
    public void changStatusByIds(int status, List<Long> ids);


    /**
     * 根据id查询套餐及菜品信息——用于数据回显
     * @param id
     */
    public SetmealDto getByIdWithDish(Long id);



    /**
     * 修改套餐及套餐里的菜品
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);


}
