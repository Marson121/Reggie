package com.qfnu.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qfnu.reggie.common.CustomException;
import com.qfnu.reggie.entity.Category;
import com.qfnu.reggie.entity.Dish;
import com.qfnu.reggie.entity.Setmeal;
import com.qfnu.reggie.mapper.CategoryMapper;
import com.qfnu.reggie.service.CategoryService;
import com.qfnu.reggie.service.DishService;
import com.qfnu.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要判断是否关联了菜品或套餐
     * 如果关联直接抛出业务异常，否则正常删除
     * @param id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id查询是否关联了菜品
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否关联了菜品，count大于0说明是，直接抛出一个异常
        if (count1 > 0) {
            // 抛出异常
            throw new CustomException("当前分类下关联了菜品，不能删除！");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id查询是否关联了套餐
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //查询当前分类是否关联了套餐，count大于0说明是，直接抛出一个异常
        if (count2 > 0) {
            // 抛出异常
            throw new CustomException("当前分类下关联了套餐，不能删除！");
        }

        //正常删除分类
        super.removeById(id);

    }
}
