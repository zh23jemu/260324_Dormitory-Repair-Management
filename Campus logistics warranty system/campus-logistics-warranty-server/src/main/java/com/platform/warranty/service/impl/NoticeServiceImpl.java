package com.platform.warranty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.warranty.domain.entity.Notice;
import com.platform.warranty.mapper.NoticeMapper;
import com.platform.warranty.service.INoticeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 公告表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2025-05-13
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

}
