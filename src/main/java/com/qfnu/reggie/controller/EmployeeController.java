package com.qfnu.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qfnu.reggie.common.R;
import com.qfnu.reggie.entity.Employee;
import com.qfnu.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {


    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 1.将页面提交的密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);                // emp是查询数据库返回的对象，因为username不能重复（unique），所以可以直接用getOne()

        // 3.如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("用户名不存在");         // 调用R类中的error方法
        }

        // 4.密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {           // 数据库对象的密码跟用户网页输入的密码不一致
            return R.error("密码不正确");
        }

        // 5.查看员工状态，如果已禁用，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        // 6.登录成功，将员工id存入session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }


    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 1.清理session中保存的当前登录用户的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息{}", employee.toString());

        //设置其他属性
        //1.设置初始密码，需要md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //改由MyMetaObjectHandler.updateFill()自动添加
        ////2.设置创建时间和更新时间
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //
        ////3.设置创建人和修改人（当前登录用户的id）
        ////获得当前登录用户的id，并由Object转成Long类型
        //Long employeeId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(employeeId);
        //employee.setUpdateUser(employeeId);

        //其他属性（如status）入库的时候有默认值，这里不需要设置，直接调用service层进行入库

        employeeService.save(employee);

        return R.success("新增员工成功！");
    }


    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //1.构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //2.构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        // 添加过滤条件, 如果name不为空的话，添加name属性为查询条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件,根据修改时间降序排列
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //3.执行查询，调用service层的分页查询方法，封装为pageInfo对象
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }


    /**
     * 根据id修改员工信息
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

        //由MyMetaObjectHandler.updateFill()自动修改
        ////设置修改时间和修改人
        //employee.setUpdateTime(LocalDateTime.now());
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setUpdateUser(empId);

        // 调用service层的更新方法完成员工的更新
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }


    /**
     * 根据id查询用户——用于修改时的数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询用户");

        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查到该员工信息");
    }


}



























