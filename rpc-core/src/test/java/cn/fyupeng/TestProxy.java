package cn.fyupeng;

import cn.fyupeng.service.HelloWorldService;

import java.lang.reflect.Method;

/**
 * @Auther: fyp
 * @Date: 2023/3/28
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */
public class TestProxy {
    public static void main(String[] args) {
        Class<HelloWorldService> clazz = HelloWorldService.class;
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            System.out.println(declaredMethod);
        }
    }
}
