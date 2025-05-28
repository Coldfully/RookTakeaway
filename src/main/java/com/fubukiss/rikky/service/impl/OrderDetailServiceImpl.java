package com.fubukiss.rikky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fubukiss.rikky.entity.OrderDetail;
import com.fubukiss.rikky.entity.Orders;
import com.fubukiss.rikky.mapper.OrderDetailMapper;
import com.fubukiss.rikky.service.OrderDetailService;
import com.fubukiss.rikky.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FileName: OrderDetailServiceImpl
 * Date: 2023/01/20
 * Time: 23:58
 * Author: river
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

    @Autowired
    private OrdersService ordersService;

    @Override
    public List<OrderDetail> getUserOrderDetails(Long userId) {
        // 1. 获取用户的所有订单ID
        LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
        ordersQueryWrapper.eq(Orders::getUserId, userId)
                         .select(Orders::getId);
        List<Long> orderIds = ordersService.list(ordersQueryWrapper)
                .stream()
                .map(Orders::getId)
                .collect(Collectors.toList());
        
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 2. 根据订单ID获取订单详情
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrderDetail::getOrderId, orderIds)
                   .orderByDesc(OrderDetail::getOrderId);
        return this.list(queryWrapper);
    }

    @Override
    public List<OrderDetail> getDishOrderDetails(Long dishId) {
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getDishId, dishId);
        return this.list(queryWrapper);
    }
}
