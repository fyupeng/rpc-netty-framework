package cn.fyupeng.handler;

import cn.fyupeng.exception.RpcException;
import cn.fyupeng.protocol.RpcRequest;
import cn.fyupeng.provider.DefaultServiceProvider;
import cn.fyupeng.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * @Auther: fyp
 * @Date: 2022/3/23
 * @Description: 请求处理器
 * @Package: cn.fyupeng.handler
 * @Version: 1.0
 */
@Slf4j
public class JdkRequestHandler {

    private static final ServiceProvider serviceProvider;

    static {
        serviceProvider = new DefaultServiceProvider();
    }

    /**
     * 调用成功返回 正确结果
     * 调用失败 返回 异常结果 并 打印异常信息
     * @param rpcRequest
     * @return
     */
   public Object handler(RpcRequest rpcRequest) {
       long begin = System.currentTimeMillis();
       Object result = null;
       try {
           Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
           result = invokeTargetMethod(rpcRequest, service);
            log.info("Service: {} completed the method [{}] call", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
           long end = System.currentTimeMillis();
           log.info("==> Task execution takes {} ms", (end - begin));
       } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | RpcException e) {
           log.error("Error occurred while invoking remote method: ", e);
           throw new RuntimeException("Error occurred while invoking remote method: " + e.getMessage());
       }
       // 捕获异常不会return 具体调用的方法结果
       return result;
   }
   private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
       log.info("Proxy target Service {}", service);
       log.info("Service: {} is invoking method [{}], paramTypes [{}] ,parameters [{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName(), rpcRequest.getParamTypes(), rpcRequest.getParameters());
       Class<?>[] paramTypes = Arrays.stream(rpcRequest.getParamTypes())
               .map(className -> {
                   try {
                       return Class.forName(className);
                   } catch (ClassNotFoundException e) {
                       // 处理类找不到的异常
                       log.error("Class parse failed:", e);
                       throw new RuntimeException(e);
                   }
               })
               .toArray(Class<?>[]::new);
       Method method = service.getClass().getMethod(rpcRequest.getMethodName(), paramTypes);
       // 调用方法的 返回结果
       return method.invoke(service, rpcRequest.getParameters());

   }

}
