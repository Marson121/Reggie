package com.qfnu.reggie.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qfnu.reggie.entity.User;
import com.qfnu.reggie.mapper.UserMapper;
import com.qfnu.reggie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
