package com.qfnu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qfnu.reggie.entity.ShoppingCart;


public interface ShoppingCartService extends IService<ShoppingCart> {

    public void clean();
}
