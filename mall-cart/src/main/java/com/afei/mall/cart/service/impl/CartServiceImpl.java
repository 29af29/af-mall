package com.afei.mall.cart.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.common.jwt.JwtUtils;
import com.afei.mall.cart.domain.dto.CartItemSaveDTO;
import com.afei.mall.cart.domain.dto.CartMergeDTO;
import com.afei.mall.cart.domain.dto.CartNumDTO;
import com.afei.mall.cart.domain.vo.CartItemVO;
import com.afei.mall.cart.domain.vo.CartVO;
import com.afei.mall.cart.service.CartService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String KEY_PREFIX = "cart:";
    private static final String SKU_API = "http://localhost:8030/api/product/sku/";

    @SneakyThrows
    @Override
    public CartVO getList(String token) {
        Long userId = jwtUtils.getUserId(token);
        String key = KEY_PREFIX + userId;

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<CartItemVO> items = new ArrayList<>();
        for (Object value : entries.values()) {
            CartItemVO item = MAPPER.readValue((String) value, CartItemVO.class);
            items.add(item);
        }

        long totalPrice = items.stream()
                .filter(i -> Boolean.TRUE.equals(i.getSelected()))
                .mapToLong(i -> i.getPrice() * i.getNum())
                .sum();

        return CartVO.builder()
                .items(items)
                .totalPrice(totalPrice)
                .totalCount(items.size())
                .build();
    }

    @Override
    @SneakyThrows
    public void add(CartItemSaveDTO dto, String token) {
        Long userId = jwtUtils.getUserId(token);
        String key = KEY_PREFIX + userId;
        String field = dto.getSkuId().toString();

        // 调 product 模块 HTTP 接口获取 SKU 信息
        String url = SKU_API + dto.getSkuId();
        JsonNode resp;
        try {
            resp = restTemplate.getForObject(url, JsonNode.class);
        } catch (Exception e) {
            throw new BusinessException("获取商品信息失败");
        }
        if (resp == null || resp.get("code").asInt() != 200) {
            throw new BusinessException("商品不存在");
        }
        JsonNode skuData = resp.get("data");

        // 购物车已有同 SKU 则累加
        String existing = (String) redisTemplate.opsForHash().get(key, field);
        int num = dto.getNum();
        if (existing != null) {
            CartItemVO ei = MAPPER.readValue(existing, CartItemVO.class);
            num += ei.getNum();
        }

        int stock = skuData.get("stock").asInt();
        if (stock < num) {
            throw new BusinessException("商品库存不足");
        }

        CartItemVO item = CartItemVO.builder()
                .skuId(dto.getSkuId())
                .title(skuData.get("title").asText())
                .image(skuData.has("images") && !skuData.get("images").asText().isEmpty()
                        ? skuData.get("images").asText() : "")
                .price(skuData.get("price").asLong())
                .stock(stock)
                .num(num)
                .selected(Boolean.TRUE)
                .build();

        redisTemplate.opsForHash().put(key, field, MAPPER.writeValueAsString(item));
    }

    @SneakyThrows
    @Override
    public void updateNum(Long skuId, CartNumDTO dto, String token) {
        Long userId = jwtUtils.getUserId(token);
        String key = KEY_PREFIX + userId;
        String field = skuId.toString();

        String existing = (String) redisTemplate.opsForHash().get(key, field);
        if (existing == null) {
            throw new BusinessException("购物车中不存在该商品");
        }
        CartItemVO item = MAPPER.readValue(existing, CartItemVO.class);

        // 数量 <= 0 则删除
        if (dto.getNum() <= 0) {
            redisTemplate.opsForHash().delete(key, field);
            return;
        }

        // 校验库存
        if (dto.getNum() > item.getStock()) {
            throw new BusinessException("商品库存不足");
        }

        item.setNum(dto.getNum());
        redisTemplate.opsForHash().put(key, field, MAPPER.writeValueAsString(item));
    }

    @Override
    public void delete(Long skuId, String token) {
        Long userId = jwtUtils.getUserId(token);
        String key = KEY_PREFIX + userId;
        String field = skuId.toString();
        String existing = (String) redisTemplate.opsForHash().get(key, field);
        if (existing == null) {
            throw new BusinessException("购物车中不存在该商品");
        }
        redisTemplate.opsForHash().delete(key, field);
    }

    @SneakyThrows
    @Override
    public void select(Long skuId, Boolean selected, String token) {
        Long userId = jwtUtils.getUserId(token);
        String key = KEY_PREFIX + userId;
        String field = skuId.toString();
        String existing = (String) redisTemplate.opsForHash().get(key, field);
        if (existing == null) {
            throw new BusinessException("购物车中不存在该商品");
        }
        CartItemVO item = MAPPER.readValue(existing, CartItemVO.class);
        item.setSelected(selected);
        redisTemplate.opsForHash().put(key, field, MAPPER.writeValueAsString(item));
    }

    @SneakyThrows
    @Override
    public void selectAll(Boolean selected, String token) {
        Long userId = jwtUtils.getUserId(token);
        String key = KEY_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            CartItemVO item = MAPPER.readValue((String) entry.getValue(), CartItemVO.class);
            item.setSelected(selected);
            redisTemplate.opsForHash().put(key, entry.getKey(), MAPPER.writeValueAsString(item));
        }
    }

    @Override
    public Integer getCount(String token) {
        Long userId = jwtUtils.getUserId(token);
        String key = KEY_PREFIX + userId;
        return Math.toIntExact(redisTemplate.opsForHash().size(key));
    }

    @Override
    public void mergeCart(CartMergeDTO dto, String token) {
        for (CartItemSaveDTO item : dto.getItems()) {
            add(item, token);
        }
    }
}
