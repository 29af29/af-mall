package com.afei.mall.notify.service;

import com.afei.common.result.PageResult;
import com.afei.mall.notify.domain.po.NotifyRecord;
import com.afei.mall.notify.domain.vo.NotifyVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface NotifyService extends IService<NotifyRecord> {

    /** 查询当前用户的站内信（type=3）列表 */
    PageResult<NotifyVO> page(Long userId, Integer pageNum, Integer pageSize);

    /** 当前用户未读站内信数量 */
    Long unreadCount(Long userId);

    /** 标记单条已读 */
    void markRead(Long userId, Long id);

    /** 全部标记已读 */
    void markAllRead(Long userId);
}
