package com.qfnu.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qfnu.reggie.common.BaseContext;
import com.qfnu.reggie.common.CustomException;
import com.qfnu.reggie.entity.*;
import com.qfnu.reggie.mapper.OrderMapper;
import com.qfnu.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private OrderService orderService;


    /**
     * 用户下单
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获取当前用户id
        Long currentId = BaseContext.getCurrentId();

        //查询当前用户购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单！");
        }

        // 查询用户数据
        User user = userService.getById(currentId);

        // 查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            throw new CustomException("地址信息有误，不能下单！");
        }



        //插入前设置订单属性
        long orderId = IdWorker.getId();   // 生成订单号

        AtomicInteger amount = new AtomicInteger(0);  // 保存总金额

        //遍历购物车
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            //生成订单明细对象
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setOrderId(orderId);                    // 主键 订单号
            orderDetail.setNumber(item.getNumber());            // 菜品/套餐份数
            orderDetail.setDishFlavor(item.getDishFlavor());    // 口味
            orderDetail.setDishId(item.getDishId());            // 菜品id
            orderDetail.setSetmealId(item.getSetmealId());      // 套餐id
            orderDetail.setName(item.getName());                // 菜品/套餐名称
            orderDetail.setImage(item.getImage());              // 图片
            orderDetail.setAmount(item.getAmount());            // 单价

            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());       // 单价×份数

            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);                              // 订单号（订单表主键）
        orders.setOrderTime(LocalDateTime.now());           // 下单时间
        orders.setCheckoutTime(LocalDateTime.now());        // 结算时间
        orders.setStatus(2);                                // 订单状态 2是待派送
        orders.setAmount(new BigDecimal(amount.get()));     // 订单金额
        orders.setUserId(currentId);                        // 用户id
        orders.setNumber(String.valueOf(orderId));          // 订单号
        orders.setUserName(user.getName());                 // 用户名
        orders.setConsignee(addressBook.getConsignee());    // 收货人
        orders.setPhone(addressBook.getPhone());            // 联系电话
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                        + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                        + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                        + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));


        //完成订单——向订单表插入一条数据
        this.save(orders);

        //向订单明细表插入数据（可能多条）
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }



    /**
     * 根据订单id查询订单明细
     * @param orderId
     * @return
     */
    @Override
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
        return orderDetailList;
    }


}









