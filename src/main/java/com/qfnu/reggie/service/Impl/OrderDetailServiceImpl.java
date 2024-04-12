package com.qfnu.reggie.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qfnu.reggie.entity.OrderDetail;
import com.qfnu.reggie.mapper.OrderDetailMapper;
import com.qfnu.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
