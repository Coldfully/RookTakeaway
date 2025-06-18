package com.fubukiss.rikky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fubukiss.rikky.entity.OrderDetail;

import java.util.List;

/**
 * FileName: OrderDetailService
 * Date: 2023/01/20
 * Time: 23:51
 * Author: river
 */
public interface OrderDetailService extends IService<OrderDetail> {
    
    /**
     * 获取用户的订单详情列表
     * @param userId 用户ID
     * @return 订单详情列表
     */
    List<OrderDetail> getUserOrderDetails(Long userId);

    /**
     * 获取菜品的订单详情
     * @param dishId 菜品ID
     * @return 订单详情列表
     */
    List<OrderDetail> getDishOrderDetails(Long dishId);
}
