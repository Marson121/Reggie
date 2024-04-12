package com.qfnu.reggie.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理（AOP思想）
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})         // 拦截所有加了RestController和Controller注解的controller类
@ResponseBody               // 该注解可以把返回值封装为json数据
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class) // sql违反完整性约束异常 当某些属性有唯一约束时,重复添加会抛异常
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.info(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")) {              // 判断异常情况是否是违反唯一性约束
            String[] split = ex.getMessage().split(" ");          // ex.getMessage() 就是报的异常情况Duplicate entry 'masong' for key 'employee.idx_username'，把他们用空格分隔
            String msg = split[2] + "已存在";                            // 从异常信息中把重复的字段取出来
            return R.error(msg);
        }

        return R.error("未知错误");
    }



    /**
     * 异常处理方法
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class) // 自定义异常，删除分类时该分类关联了菜品或套餐
    public R<String> exceptionHandler(CustomException ex) {
        log.info(ex.getMessage());

        // 返回CustomException.class异常的提示信息
        return R.error(ex.getMessage());
    }

}
