package com.qfnu.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qfnu.reggie.common.CustomException;
import com.qfnu.reggie.dto.SetmealDto;
import com.qfnu.reggie.entity.Setmeal;
import com.qfnu.reggie.entity.SetmealDish;
import com.qfnu.reggie.mapper.SetmealMapper;
import com.qfnu.reggie.service.SetmealDishService;
import com.qfnu.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 把套餐基本信息以及套餐和菜品关系保存到表中   setmeal  setmeal_dish
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐基本信息 操作setmeal 执行insert操作
        this.save(setmealDto);

        // 获取该套餐下的菜品（list），但是里面是没有套餐id的，因此在保存到setmeal_dish表时要处理该id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        // 遍历每一个菜品，给他设置套餐id
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 保存套餐和菜品关系 操作setmeal_dish 执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }




    /**
     * 删除套餐以及套餐和菜品关联关系
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态，是否时停售，只有停售状态才可以删除    select count(*) from setmeal where id in (ids) and status = 1
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        //查询传过来的id且状态为在售的套餐
        int count = this.count(queryWrapper);

        // 如果不能删除，抛出一个业务异常
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除！");
        }

        // 如果可以删除，删除套餐表 setmeal
        this.removeByIds(ids);

        // 删除关联表 setmeal_dish 这里的id是套餐id，需要根据套餐id在setmeal_dish表中查询关联的菜品，然后把他们删除
        // 只要套餐id在传过来的ids里面都删除
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(dishLambdaQueryWrapper);
    }




    /**
     * 修改套餐的状态为起售还是停售
     * @param status
     * @param ids
     */
    @Override
    public void changStatusByIds(int status, List<Long> ids) {
        for (Long id : ids) {
            //根据前端传过来的套餐id集合查询每一个套餐，然后把套餐的状态设置为前端传过来的状态，然后进行更新操作
            Setmeal setmeal = this.getById(id);
            setmeal.setStatus(status);
            this.updateById(setmeal);
        }
    }



    /**
     * 根据id查询套餐及菜品信息——用于数据回显
     * @param id
     */
    @Override
    @Transactional
    public SetmealDto getByIdWithDish(Long id) {
        //1.查询套餐基本信息  setmeal
        Setmeal setmeal = this.getById(id);

        //2.查询套餐包含的菜品信息（根据套餐id setmealId） setmeal_dish
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> dishes = setmealDishService.list(setmealDishLambdaQueryWrapper);

        SetmealDto setmealDto = new SetmealDto();
        // 拷贝套餐普通数据
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 设置套餐包含菜品数据
        setmealDto.setSetmealDishes(dishes);

        return setmealDto;
    }




    /**
     * 修改套餐及套餐里的菜品
     * @param setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {

        // 1.更新setmeal表
        this.updateById(setmealDto);

        // 2.更新setmeal_dish表 先删再加
        // 2.1清理当前套餐包含的菜品信息(用套餐id在setmeal_dish中查)   delete
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        // 2.2添加当前传过来的菜品信息（注意前端传过来的菜品数据Dish中没有setmealId，需要设置）
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            //每一个item是一个菜品
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //重新保存菜品
        setmealDishService.saveBatch(setmealDishes);
    }

}


