package com.platform.warranty.service.impl;

import com.platform.warranty.domain.entity.User;
import com.platform.warranty.mapper.UserMapper;
import com.platform.warranty.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2025-04-19
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
