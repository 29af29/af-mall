package com.afei.mall.search.mq;

import com.afei.common.mq.MqConfig;
import com.afei.common.mq.SpuSyncMessage;
import com.afei.mall.search.domain.doc.SpuDoc;
import com.afei.mall.search.repository.SpuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpuSyncConsumer {

    private final SpuRepository spuRepository;

    @RabbitListener(queues = MqConfig.SPU_SYNC_QUEUE)
    public void handle(SpuSyncMessage msg) {
        log.info("收到商品同步消息: type={}, spuId={}", msg.getType(), msg.getSpuId());
        try {
            if (SpuSyncMessage.TYPE_DELETE.equals(msg.getType())) {
                // 删除
                spuRepository.deleteById(msg.getSpuId());
                log.info("已从 ES 删除商品: spuId={}", msg.getSpuId());
            } else {
                // 新增/更新
                SpuDoc doc = new SpuDoc();
                doc.setId(msg.getSpuId());
                doc.setName(msg.getName());
                doc.setCaption(msg.getCaption());
                doc.setBrandName(msg.getBrandName());
                doc.setPrice(msg.getPrice());
                doc.setImage(msg.getImage());
                doc.setSaleable(msg.getSaleable());
                spuRepository.save(doc);
                log.info("已同步商品到 ES: spuId={}, name={}", msg.getSpuId(), msg.getName());
            }
        } catch (Exception e) {
            log.error("同步商品到 ES 失败: spuId={}", msg.getSpuId(), e);
        }
    }
}
