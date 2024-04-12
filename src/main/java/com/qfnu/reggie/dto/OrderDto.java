package com.qfnu.reggie.dto;

import com.qfnu.reggie.entity.OrderDetail;
import com.qfnu.reggie.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;

	
}
