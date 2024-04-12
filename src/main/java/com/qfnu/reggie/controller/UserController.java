package com.qfnu.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qfnu.reggie.common.R;
import com.qfnu.reggie.entity.User;
import com.qfnu.reggie.service.UserService;
import com.qfnu.reggie.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {


    @Autowired
    private UserService userService;



    /**
     * 发送邮箱验证码
     * @param user
     * @param session
     * @return
     * @throws MessagingException
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) throws MessagingException {
        String phone = user.getPhone();
        if (!phone.isEmpty()) {
            //随机生成一个验证码
            String code = MailUtils.achieveCode();
            log.info(code);
            //这里的phone其实就是邮箱，code是我们生成的验证码
            MailUtils.sendTestMail(phone, code);
            //验证码存session，方便后面拿出来比对
            session.setAttribute(phone, code);
            return R.success("验证码发送成功");
        }
        return R.error("验证码发送失败");
    }


    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {

        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        //验证码比对——前端提交验证码（用户输入）与session保存的验证码（系统生成）
        if (codeInSession != null && codeInSession.equals(code)) {

            //如果能比对成功，则登录

            //判断当前手机号对应的用户是否为新用户，如果是新用户，则自动完成注册，把信息保存到user表
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);

                userService.save(user);
            }

            //把用户id存入session，表示登录状态
            session.setAttribute("user", user.getId());

            return R.success(user);
        }

        return R.error("登录失败！");
    }


    /**
     * 移动端用户退出登录
     * @param session
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpSession session) {

        session.removeAttribute("user");

        return R.success("退出成功！");
    }
}





























