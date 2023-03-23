package cn.fyupeng.factory;


import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: fyp
 * @Date: 2022/3/30
 * @Description:
 * @Package: cn.fyupeng.factory
 * @Version: 1.0
 */
public class SingleFactory {

    private static Map<Class, Object> objectMap = new HashMap<>();

    private SingleFactory() {}

    /**
     * 使用 双重 校验锁 实现 单例模式
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getInstance(Class<T> clazz) {
        Object instance = objectMap.get(clazz);
        if (instance == null) {
            synchronized (clazz) {
                if (instance == null) {
                    try {
                        instance = clazz.newInstance();
                        objectMap.put(clazz, instance);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
        }
        return clazz.cast(instance);
    }

}
