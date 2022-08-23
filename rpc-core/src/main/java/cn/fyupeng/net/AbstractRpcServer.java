package cn.fyupeng.net;

import cn.fyupeng.anotion.Service;
import cn.fyupeng.anotion.ServiceScan;
import cn.fyupeng.exception.AnnotationMissingException;
import cn.fyupeng.exception.RpcException;
import cn.fyupeng.provider.ServiceProvider;
import cn.fyupeng.registry.ServiceRegistry;
import cn.fyupeng.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @Auther: fyp
 * @Date: 2022/3/27
 * @Description:
 * @Package: cn.fyupeng.net
 * @Version: 1.0
 */
@Slf4j
public abstract class AbstractRpcServer implements RpcServer {
   /**
    * 修饰符 protected 才可以将 字段 继承都 子类中
    * 在 子类中 执行 赋值操作
    */
   protected String hostName;
   protected int port;
   protected ServiceProvider serviceProvider;
   protected ServiceRegistry serviceRegistry;

   public void scanServices() throws RpcException {
      // 获取调用者 start 服务时所在的主类名, 即 调用者 调用 AbstractRpcServer 的子类 类名
      String mainClassName = ReflectUtil.getStackTrace();
      log.info("mainClassName: {}", mainClassName);
      Class<?> startClass;
      try {
         startClass = Class.forName(mainClassName);
         for (Annotation annotation : startClass.getAnnotations()) {
            log.info("discover annotation: {}", annotation);
         }
         if (!startClass.isAnnotationPresent(ServiceScan.class)) {
            log.error("The startup class is missing the @ServiceScan annotation");
            throw new AnnotationMissingException("The startup class is missing the @ServiceScan annotation Exception");
         }
      } catch (ClassNotFoundException e) {
         log.error("An unknown error has occurred:{}",e.getMessage());
         throw new RpcException("An unknown error has occurred Exception");
      }

      String basePackage = startClass.getAnnotation(ServiceScan.class).value();
      if ("".equals(basePackage)) {
         // 如果前缀有 包名
         if(mainClassName.lastIndexOf(".") != -1) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
         // 如果没有 包名
         } else {
            basePackage = mainClassName;
         }
      }
      Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
      for (Class<?> clazz : classSet) {
         if (clazz.isAnnotationPresent(Service.class)) {
            String serviceName = clazz.getAnnotation(Service.class).name();
            Object obj;
            try {
               obj = clazz.newInstance();

            }catch (InstantiationException | IllegalAccessException e) {
               log.error("An error occurred while creating the {} : {}",clazz, e);
               continue;
            }
            if ("".equals(serviceName)) {
               Class<?>[] interfaces = clazz.getInterfaces();
               for (Class<?> oneInterface : interfaces) {
                  publishService(obj, oneInterface.getCanonicalName());
               }
            } else {
               publishService(obj, serviceName);
            }
         }
      }
   }

   @Override
   public <T> void publishService(T service, String serviceName) throws RpcException {
      serviceProvider.addServiceProvider(service, serviceName);
      serviceRegistry.register(serviceName, new InetSocketAddress(hostName, port));
   }
}
