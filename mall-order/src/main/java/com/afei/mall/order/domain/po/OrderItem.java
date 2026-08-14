package com.afei.mall.order.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;              // 主键
    private Long orderId;         // 订单 ID
    private String orderNo;       // 订单号
    private Long skuId;           // SKU ID
    private Long spuId;           // SPU ID
    private String skuName;       // 商品名称（快照）
    private String skuPic;        // 商品图片（快照）
    private Long price;           // 单价（分，快照）
    private Integer quantity;     // 购买数量
    private Long totalAmount;     // 小计金额（分）
    private LocalDateTime createTime; // 创建时间
}
