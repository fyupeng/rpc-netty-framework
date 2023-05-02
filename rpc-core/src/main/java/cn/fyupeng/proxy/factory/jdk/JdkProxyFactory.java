package cn.fyupeng.proxy.factory.jdk;

import cn.fyupeng.proxy.factory.ProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @Auther: fyp
 * @Date: 2023/3/27
 * @Description: JDK动态代理工厂
 * @Package: cn.fyupeng.proxy
 * @Version: 1.0
 */
public class JdkProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(Class<?> clazz, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] {clazz},handler);
    }
}
