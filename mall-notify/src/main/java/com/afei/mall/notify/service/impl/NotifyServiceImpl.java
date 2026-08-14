package com.afei.mall.notify.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.common.jwt.JwtUtils;
import com.afei.common.result.PageResult;
import com.afei.mall.notify.domain.po.NotifyRecord;
import com.afei.mall.notify.domain.vo.NotifyVO;
import com.afei.mall.notify.service.NotifyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.afei.mall.notify.mapper.NotifyRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotifyServiceImpl extends ServiceImpl<NotifyRecordMapper, NotifyRecord> implements NotifyService {

    private final JwtUtils jwtUtils;

    @Override
    public PageResult<NotifyVO> page(String token, Integer pageNum, Integer pageSize) {
        Long userId = jwtUtils.getUserId(token);
        Page<NotifyRecord> page = this.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<NotifyRecord>()
                        .eq(NotifyRecord::getType, 3) // 站内信
                        .eq(NotifyRecord::getTarget, String.valueOf(userId))
                        .orderByDesc(NotifyRecord::getCreateTime));
        List<NotifyVO> voList = page.getRecords().stream().map(n -> {
            NotifyVO vo = new NotifyVO();
            BeanUtils.copyProperties(n, vo);
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public Long unreadCount(String token) {
        Long userId = jwtUtils.getUserId(token);
        return this.count(new LambdaQueryWrapper<NotifyRecord>()
                .eq(NotifyRecord::getType, 3)
                .eq(NotifyRecord::getTarget, String.valueOf(userId))
                .eq(NotifyRecord::getIsRead, 0));
    }

    @Override
    public void markRead(String token, Long id) {
        Long userId = jwtUtils.getUserId(token);
        NotifyRecord record = this.getById(id);
        if (record == null || !String.valueOf(userId).equals(record.getTarget())) {
            throw new BusinessException("通知不存在");
        }
        record.setIsRead(1);
        this.updateById(record);
    }

    @Override
    public void markAllRead(String token) {
        Long userId = jwtUtils.getUserId(token);
        NotifyRecord update = new NotifyRecord();
        update.setIsRead(1);
        this.update(update, new LambdaQueryWrapper<NotifyRecord>()
                .eq(NotifyRecord::getType, 3)
                .eq(NotifyRecord::getTarget, String.valueOf(userId))
                .eq(NotifyRecord::getIsRead, 0));
    }
}