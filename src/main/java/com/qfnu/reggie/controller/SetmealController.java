package com.qfnu.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qfnu.reggie.common.R;
import com.qfnu.reggie.dto.DishDto;
import com.qfnu.reggie.dto.SetmealDto;
import com.qfnu.reggie.entity.Category;
import com.qfnu.reggie.entity.Dish;
import com.qfnu.reggie.entity.Setmeal;
import com.qfnu.reggie.entity.SetmealDish;
import com.qfnu.reggie.service.CategoryService;
import com.qfnu.reggie.service.DishService;
import com.qfnu.reggie.service.SetmealDishService;
import com.qfnu.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;


    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息 {}",setmealDto.toString());

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }



    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, queryWrapper);

        //当前Setmeal里面只有套餐分类id，需要转成SetmealDto对象，获取到套餐分类名称(前端页面要的是套餐分类的名称，而前端传过来的数据是套餐分类id，需要处理)
        //1.把分页查询查到的除了records即菜品之外的其他属性进行拷贝
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        //2.当前查询到的records中的category是id，不是名称，下面需要处理
        List<Setmeal> records = pageInfo.getRecords();

        //3.遍历列表中每个Setmeal对象进行处理，处理后是SetmealDto对象列表
        List<SetmealDto> setmealDtoList = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item, setmealDto);

            // 分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);

        return R.success(setmealDtoPage);
    }



    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids  {}",ids);

        setmealService.removeWithDish(ids);

        return R.success("套餐删除成功！");
    }



    /**
     * 套餐的起售和停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, @RequestParam List<Long> ids) {

        setmealService.changStatusByIds(status, ids);

        return R.success("状态修改成功！");
    }



    /**
     * 根据id查询套餐及菜品信息——用于数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        return R.success(setmealDto);
    }



    /**
     * 修改套餐及套餐里的菜品
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {

        log.info(setmealDto.toString());

        setmealService.updateWithDish(setmealDto);

        return R.success("修改套餐成功！");
    }



    /**
     * 查找一个套餐分类里面有什么套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> listR(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        // 只查询状态为1即在售的套餐
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus,1);
        queryWrapper.orderByAsc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }



    /** 移动端点击套餐图片查看套餐具体内容
     * 这里返回的是dto 对象，因为前端需要copies这个属性
     * 前端主要要展示的信息是:套餐中菜品的基本信息，图片，菜品描述，以及菜品的份数
     * @param
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable("id") Long SetmealId) {
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,SetmealId);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        log.info("list的内容{}", list.toString());
        //遍历每一个setmealDish
        List<DishDto> dishDtos = list.stream().map((setmealDish) -> {
            log.info("setmealDish的内容{}", setmealDish.toString());
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(setmealDish, dishDto);//这里是为了把套餐中的菜品的基本信息填充到dto中，比如菜品描述，菜品图片等菜品的基本信息
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);
            log.info("dishDto的内容{}", dishDto.toString());
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtos);
    }
}
















