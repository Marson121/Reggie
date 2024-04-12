package com.qfnu.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qfnu.reggie.common.CustomException;
import com.qfnu.reggie.dto.DishDto;
import com.qfnu.reggie.entity.Dish;
import com.qfnu.reggie.entity.DishFlavor;
import com.qfnu.reggie.entity.SetmealDish;
import com.qfnu.reggie.mapper.DishMapper;
import com.qfnu.reggie.service.DishFlavorService;
import com.qfnu.reggie.service.DishService;
import com.qfnu.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增菜品，同时保存口味数据
     * @param dishDto
     */
    @Override
    @Transactional  // 开事务
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到dish
        this.save(dishDto);


        //菜品id()
        Long dishId = dishDto.getId();

        //菜品口味 把菜品id加入到flavors中
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }



    /**
     * 根据id查询菜品信息和口味信息——用于数据回显
     * @param id
     */
    @Override
    @Transactional
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息  dish
        Dish dish = this.getById(id);

        // 查询菜品口味信息(根据该菜品id  dishId)  dish_flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        DishDto dishDto = new DishDto();
        // 拷贝菜品普通数据
        BeanUtils.copyProperties(dish, dishDto);
        // 设置口味数据
        dishDto.setFlavors(flavors);

        return dishDto ;
    }



    /**
     * 修改菜品及口味
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 1.更新dish表
        this.updateById(dishDto);

        // 2.更新dish_flavor表 先删再加
        // 2.1清理当前菜品对应口味数据 ---delete dish_flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        // 2.2添加当前传过来的口味数据(注意前端返回的DishDto中的flavor中没有dishId，需要设置)
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            //每一个item是一个口味
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        //重新保存口味
        dishFlavorService.saveBatch(flavors);
    }



    /**
     * 根据id修改菜品状态
     * @param ids
     */
    @Override
    public void changeStautsByIds(int status, List<Long> ids) {
        for (Long id : ids) {
            //根据dishId获取每个dish对象，然后把状态设置为前端页面传过来的status值，执行更新操作
            Dish dish = this.getById(id);
            dish.setStatus(status);
            this.updateById(dish);
        }
    }



    /**
     * 删除菜品及口味——只有停售状态且不在套餐中的菜品可以删除
     * @param ids
     */
    @Override
    @Transactional
    public void removeByFlavors(List<Long> ids) {
        // 查询菜品是否停售，只有停售状态才可以删除  select count(*) from dish where id in (ids) and status = 1
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        //查询传过来的id且状态为在售的菜品
        int count1 = this.count(queryWrapper);
        // 如果不能删除，抛出一个业务异常
        if (count1 > 0) {
            throw new CustomException("菜品正在售卖中，不能删除！");
        }

        //查询是否关联了套餐，关联了套餐的菜品不可以删除（根据菜品id查询套餐菜品表中是否有数据）
        LambdaQueryWrapper<SetmealDish> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.in(SetmealDish::getDishId, ids);
        //查询套餐菜品关系表，用前端传过来的id查询，查询结果count>0说明菜品关联了套餐，不能删除
        int count2 = setmealDishService.count(setmealQueryWrapper);
        // 如果不能删除，抛出一个业务异常
        if (count2 > 0) {
            throw new CustomException("该菜品在套餐中，不能删除！");
        }

        // 如果可以删除，删除菜品表  dish
        this.removeByIds(ids);

        // 删除口味表 dish_flavor,这里的id是菜品id，需要根据此id在菜品口味表中查询该菜品的口味，然后删除
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
    }

}







