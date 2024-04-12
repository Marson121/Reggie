package com.qfnu.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qfnu.reggie.entity.OrderDetail;
import com.qfnu.reggie.entity.Orders;

import java.util.List;


public interface OrderService extends IService<Orders> {


    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);


    /**
     * 根据订单id查询订单明细
     * @param orderId
     * @return
     */
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId);

}
