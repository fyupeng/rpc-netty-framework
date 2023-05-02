package cn.fyupeng.proxy.factory.javassist;

import cn.fyupeng.proxy.factory.ProxyFactory;
import java.lang.reflect.InvocationHandler;

/**
 * @Auther: fyp
 * @Date: 2023/3/27
 * @Description: Javassist代理工厂
 * @Package: cn.fyupeng.proxy.factory.javassist
 * @Version: 1.0
 */
public class JavassistProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(Class<?> clazz, InvocationHandler handler) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(clazz.getClassLoader(), clazz, handler);
    }
}
