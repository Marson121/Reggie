package com.qfnu.reggie.common;


/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户的id
 * 每一次的http请求都是一个独立的线程，每次请求中涉及到的方法都在这同一个线程中，因此他们的线程id相同，
 * 在该线程中存储用户id这个临时变量，只要在同线程中都可以获取到该值
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
