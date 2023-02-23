package cn.fyupeng.net;

import cn.fyupeng.exception.RpcException;

/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description:
 * @Package: cn.fyupeng.net
 * @Version: 1.0
 */
public interface RpcServer {
    void start();
    <T> void publishService(T service, String serviceClass) throws RpcException;

    <T> void publishService(T service, String groupName, String serviceClass) throws RpcException;

    /**
     *
     * @param name clazz 对应的 类名
     * @param clazz Class 类，可用于发射
     * @return
     */
    Object newInstance(String name, Class<?> clazz) throws Exception;

}
