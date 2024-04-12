package com.qfnu.reggie.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qfnu.reggie.entity.Employee;
import com.qfnu.reggie.mapper.EmployeeMapper;
import com.qfnu.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
