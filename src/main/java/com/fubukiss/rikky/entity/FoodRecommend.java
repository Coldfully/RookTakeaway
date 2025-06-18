package com.fubukiss.rikky.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("food_recommend")
public class FoodRecommend implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String name;

    private String image;

    private BigDecimal price;

    private Integer saleNum;

    private Double recommendScore;

    private String recommendReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 