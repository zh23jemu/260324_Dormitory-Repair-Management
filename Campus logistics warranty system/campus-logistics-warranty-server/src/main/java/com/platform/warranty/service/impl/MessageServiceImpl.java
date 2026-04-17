package com.platform.warranty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.warranty.domain.entity.Message;
import com.platform.warranty.mapper.MessageMapper;
import com.platform.warranty.service.IMessageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户留言表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2025-05-13
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

}
