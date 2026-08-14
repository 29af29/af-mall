package com.afei.mall.product.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.common.mq.MqConfig;
import com.afei.common.mq.SpuSyncMessage;
import com.afei.common.result.PageResult;
import com.afei.mall.product.domain.dto.SkuSaveDTO;
import com.afei.mall.product.domain.dto.SpuPageQueryDTO;
import com.afei.mall.product.domain.dto.SpuSaveDTO;
import com.afei.mall.product.domain.dto.StockDTO;
import com.afei.mall.product.domain.po.Brand;
import com.afei.mall.product.domain.po.Sku;
import com.afei.mall.product.domain.po.Spu;
import com.afei.mall.product.domain.vo.SkuVO;
import com.afei.mall.product.domain.vo.SpuVO;
import com.afei.mall.product.mapper.SkuMapper;
import com.afei.mall.product.mapper.SpuMapper;
import com.afei.mall.product.service.BrandService;
import com.afei.mall.product.service.SpuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SpuServiceImpl extends ServiceImpl<SpuMapper, Spu> implements SpuService {

    private final BrandService brandService;
    private final SkuMapper skuMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RedissonClient redissonClient;

    @Override
    public PageResult<SpuVO> page(SpuPageQueryDTO dto) {
        Page<Spu> page = this.page(dto.toPage(), new LambdaQueryWrapper<Spu>()
                .eq(dto.getCategory3Id() != null, Spu::getCategory3Id, dto.getCategory3Id())
                .eq(dto.getBrandId() != null, Spu::getBrandId, dto.getBrandId())
                .eq(dto.getSaleable() != null, Spu::getSaleable, Boolean.TRUE.equals(dto.getSaleable()) ? 1 : 0)
                .like(dto.getKey() != null && !dto.getKey().isEmpty(), Spu::getName, dto.getKey())
                .orderByDesc(Spu::getCreateTime));

        List<SpuVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public SpuVO spuDetail(Long id) {
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        SpuVO spuVO = toVO(spu);
        List<SkuVO> skuVOList = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getSpuId, spu.getId()))
                .stream().map(this::toVO).collect(Collectors.toList());
        spuVO.setSkus(skuVOList);
        return spuVO;
    }

    @Transactional
    @Override
    public void addSpu(String role, SpuSaveDTO dto) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能新增商品");
        }
        if (this.lambdaQuery().eq(Spu::getName, dto.getName()).count() > 0) {
            throw new BusinessException("商品名称已存在");
        }
        // 新增时必填字段校验
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException("商品名称不能为空");
        }
        if (dto.getBrandId() == null) {
            throw new BusinessException("品牌ID不能为空");
        }
        if (dto.getCategory3Id() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        // 保存 SPU
        Spu spu = new Spu();
        BeanUtils.copyProperties(dto, spu, "mainImage", "detail", "skus");
        spu.setPics(dto.getMainImage() != null ? dto.getMainImage() : "");
        spu.setDescription(dto.getDetail());
        spu.setSaleable(1);
        this.save(spu);

        // 保存 SKU
        if (dto.getSkus() != null && !dto.getSkus().isEmpty()) {
            for (SkuSaveDTO skuDto : dto.getSkus()) {
                Sku sku = new Sku();
                BeanUtils.copyProperties(skuDto, sku, "ownSpec", "title");
                sku.setSpuId(spu.getId());
                sku.setName(skuDto.getTitle());
                sku.setSpec(skuDto.getOwnSpec() != null ? skuDto.getOwnSpec() : "");
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }

        // 同步到搜索服务（容错：MQ 失败不影响商品新增）
        sendSyncMessage(buildSyncMessage(spu, SpuSyncMessage.TYPE_ADD));
    }

    @Transactional
    @Override
    public void updateSpu(String role, Long id, SpuSaveDTO dto) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能修改商品");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        // 名称去重（排除自身）
        if (dto.getName() != null) {
            boolean exists = this.lambdaQuery()
                    .eq(Spu::getName, dto.getName())
                    .ne(Spu::getId, id)
                    .count() > 0;
            if (exists) {
                throw new BusinessException("商品名称已存在");
            }
        }
        BeanUtils.copyProperties(dto, spu, "mainImage", "detail", "skus", "saleable");
        if (dto.getMainImage() != null) {
            spu.setPics(dto.getMainImage());
        }
        if (dto.getDetail() != null) {
            spu.setDescription(dto.getDetail());
        }
        this.updateById(spu);

        // 如果传了 SKU，先删旧再加新
        if (dto.getSkus() != null && !dto.getSkus().isEmpty()) {
            skuMapper.delete(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id));
            for (SkuSaveDTO skuDto : dto.getSkus()) {
                Sku sku = new Sku();
                BeanUtils.copyProperties(skuDto, sku, "ownSpec", "title");
                sku.setSpuId(id);
                sku.setName(skuDto.getTitle());
                sku.setSpec(skuDto.getOwnSpec() != null ? skuDto.getOwnSpec() : "");
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }

        // 同步到搜索服务（容错）
        sendSyncMessage(buildSyncMessage(spu, SpuSyncMessage.TYPE_UPDATE));
    }

    @Transactional
    @Override
    public void removeSpu(String role, Long id) {
        //校验角色
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能删除商品");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        skuMapper.delete(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id));
        this.removeById(id);

        // 同步删除到搜索服务（容错）
        SpuSyncMessage msg = new SpuSyncMessage();
        msg.setSpuId(id);
        msg.setType(SpuSyncMessage.TYPE_DELETE);
        sendSyncMessage(msg);
    }

    @Transactional
    @Override
    public void updateSaleable(String role, Long id, Boolean saleable) {
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("非管理员角色，不能上下架");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        spu.setSaleable(Boolean.TRUE.equals(saleable) ? 1 : 0);
        this.updateById(spu);

        // 上下架同步到搜索服务（容错）
        sendSyncMessage(buildSyncMessage(spu, SpuSyncMessage.TYPE_UPDATE));
    }

    @Override
    public SkuVO skuDetail(Long id) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }
        return toVO(sku);
    }

    @Override
    public void deductStock(Long skuId, StockDTO dto) {
        RLock lock = redissonClient.getLock("stock:lock:" + skuId);
        boolean locked = false;
        try {
            // 尝试拿锁：最多等 3 秒，拿到后由看门狗自动续期
            locked = lock.tryLock(3, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            // 锁内原子扣减：stock >= num 才执行，防止超卖
            int rows = skuMapper.deductStock(skuId, dto.getNum());
            if (rows == 0) {
                Sku sku = skuMapper.selectById(skuId);
                if (sku == null) {
                    throw new BusinessException("SKU 不存在");
                }
                throw new BusinessException("商品库存不足");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("获取锁被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public void restoreStock(Long skuId, StockDTO dto) {
        RLock lock = redissonClient.getLock("stock:lock:" + skuId);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            // 原子回补库存
            int rows = skuMapper.restoreStock(skuId, dto.getNum());
            if (rows == 0) {
                throw new BusinessException("SKU 不存在");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("获取锁被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 发送同步消息（容错：MQ 故障不影响主业务）
     */
    private void sendSyncMessage(SpuSyncMessage msg) {
        try {
            rabbitTemplate.convertAndSend(MqConfig.SPU_SYNC_QUEUE, msg);
        } catch (Exception e) {
            log.error("发送商品同步消息失败: spuId={}, type={}", msg.getSpuId(), msg.getType(), e);
        }
    }

    /**
     * 组装商品同步消息（含搜索所需字段）
     */
    private SpuSyncMessage buildSyncMessage(Spu spu, String type) {
        SpuSyncMessage msg = new SpuSyncMessage();
        msg.setSpuId(spu.getId());
        msg.setName(spu.getName());
        msg.setCaption(spu.getCaption());
        msg.setSaleable(spu.getSaleable() != null && spu.getSaleable() == 1);
        // 品牌名
        Brand brand = brandService.getById(spu.getBrandId());
        msg.setBrandName(brand != null ? brand.getName() : null);
        // 主图（pics 逗号分隔，取第一张）
        msg.setImage(spu.getPics() != null && !spu.getPics().isEmpty()
                ? spu.getPics().split(",")[0] : null);
        // 最低价
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spu.getId()));
        Long minPrice = skus.stream().map(Sku::getPrice).min(Long::compareTo).orElse(0L);
        msg.setPrice(minPrice);
        msg.setType(type);
        return msg;
    }

    private SpuVO toVO(Spu spu) {
        Brand brand = brandService.getById(spu.getBrandId());
        return SpuVO.builder()
                .id(spu.getId())
                .name(spu.getName())
                .caption(spu.getCaption())
                .brandId(spu.getBrandId())
                .brandName(brand != null ? brand.getName() : null)
                .category3Id(spu.getCategory3Id())
                .mainImage(spu.getPics() != null && !spu.getPics().isEmpty()
                        ? spu.getPics().split(",")[0] : null)
                .detail(spu.getDescription())
                .saleable(spu.getSaleable() != null && spu.getSaleable() == 1)
                .build();
    }

    private SkuVO toVO(Sku sku) {
        return SkuVO.builder()
                .id(sku.getId())
                .spuId(sku.getSpuId())
                .title(sku.getName())
                .price(sku.getPrice())
                .stock(sku.getStock())
                .images(sku.getImages())
                .ownSpec(sku.getSpec())
                .enable(sku.getStatus() != null && sku.getStatus() == 1)
                .build();
    }
}
