package com.qfnu.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qfnu.reggie.common.R;
import com.qfnu.reggie.dto.DishDto;
import com.qfnu.reggie.entity.Category;
import com.qfnu.reggie.entity.Dish;
import com.qfnu.reggie.entity.DishFlavor;
import com.qfnu.reggie.service.CategoryService;
import com.qfnu.reggie.service.DishFlavorService;
import com.qfnu.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品及菜品口味管理
 */

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;


    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }


    /**
     * 菜品分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo, queryWrapper);

        log.info("pageInfo: {}",pageInfo.toString());

        //1.把分页查询查到的除了records即菜品列表之外的其他属性进行拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //2.当前查询到的records中的category是id，不是名称(前端页面要显示的是名称而不是id)，下面需要处理
        List<Dish> records = pageInfo.getRecords();

        //3.遍历列表中每个Dish对象进行处理，处理后是DishDto对象列表
        List<DishDto> dishDtoList = records.stream().map((item) -> {
            // 3.1records里面是多个Dish对象，需要处理categoryName后用DishDto对象来接收（Dish对象中只有categoryId,没有categoryName）
            DishDto dishDto = new DishDto();
            // 3.2刚创建完的DishDto其他属性为空，需要拷过来
            BeanUtils.copyProperties(item, dishDto);

            // 3.3获取到当前Dish对象中categoryId，根据此来查询数据库获取category对象，从中取出categoryName放入DishDto对象
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        //4.最后把records属性加入到分页查询的dishDtoPage对象中进行返回
        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }



    /**
     * 根据id查询菜品信息和口味信息——用于数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }


    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }


    /**
     * 菜品的起售和停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, @RequestParam List<Long> ids) {

        dishService.changeStautsByIds(status, ids);

        return R.success("状态修改成功");
    }


    /**
     * 删除菜品——只有停售状态且不在套餐中的菜品可以删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除菜品的ids  {}", ids);

        dishService.removeByFlavors(ids);

        return R.success("删除菜品成功！");
    }



    /**
     * 查找一个分类里有什么菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish != null, Dish::getCategoryId, dish.getCategoryId());
        //只查询状态为1即在售的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        //把分类id改为分类名称，设置菜品的口味
        List<DishDto> dishDtoList = list.stream().map((item) -> {

            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);

            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());


        return R.success(dishDtoList);

    }

}





















