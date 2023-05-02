package cn.fyupeng.proxy.factory;

import java.lang.reflect.InvocationHandler;

/**
 * @Auther: fyp
 * @Date: 2023/3/27
 * @Description: 代理工厂
 * @Package: cn.fyupeng.proxy
 * @Version: 1.0
 */
public interface ProxyFactory {
    <T> T getProxy(Class<?> clazz, InvocationHandler handler) throws Throwable;
}
