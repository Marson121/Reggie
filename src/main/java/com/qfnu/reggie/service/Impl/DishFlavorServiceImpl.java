package com.qfnu.reggie.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qfnu.reggie.entity.DishFlavor;
import com.qfnu.reggie.mapper.DishFlavorMapper;
import com.qfnu.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;


@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
