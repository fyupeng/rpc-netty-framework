package cn.fyupeng.proxy.factory.javassist;

import javassist.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @Auther: fyp
 * @Date: 2023/3/27
 * @Description: 代理生成器
 * @Package: cn.fyupeng.proxy
 * @Version: 1.0
 */

public class ProxyGenerator {

   private static final AtomicInteger counter = new AtomicInteger(1);

   private static final int MAX_CACHE_SIZE = 1024;

   private static final Map<Class<?>, Object> proxyInstanceCache = new LinkedHashMap<Class<?>, Object>() {
      @Override
      protected boolean removeEldestEntry(Map.Entry eldest) {
         return size() > MAX_CACHE_SIZE;
      }
   };

   public static Object newProxyInstance(ClassLoader classLoader, Class<?> targetInterface, InvocationHandler invocationHandler)
           throws Exception {

      Object proxyInstance = proxyInstanceCache.get(targetInterface);

      if(proxyInstance != null){
         return proxyInstance;
      }
      ClassPool pool = ClassPool.getDefault();
      //生成代理类的全限定名
      String qualifiedName = generateClassName(targetInterface);
      // 创建代理类
      CtClass proxy = pool.makeClass(qualifiedName);
      //添加成员（接口方法列表）
      CtField mf = CtField.make("public static java.lang.reflect.Method[] methods;", proxy);
      proxy.addField(mf);
      // 添加成员（处理器）
      CtField hf = CtField.make("private " + InvocationHandler.class.getName() + " handler;", proxy);
      proxy.addField(hf);
      // 添加构造函数
      CtConstructor constructor = new CtConstructor(new CtClass[]{pool.get(InvocationHandler.class.getName())}, proxy);
      constructor.setBody("this.handler=$1;");
      constructor.setModifiers(Modifier.PUBLIC);
      proxy.addConstructor(constructor);
      proxy.addConstructor(CtNewConstructor.defaultConstructor(proxy));
      // 添加接口
      CtClass ctClass = pool.get(targetInterface.getName());
      proxy.addInterface(ctClass);
      // 对接口 targetInterface 的所有 public 方法 切入 handler的 invoke 回调函数
      List<Method> publicMethods = getPublicMethods(targetInterface);
      Method[] methods = publicMethods.toArray(new Method[0]);

      StringBuilder code = new StringBuilder();
      for (int i = 0; i < methods.length; i++) {
         Method method = methods[i];
         Class<?> returnType = method.getReturnType();
         Class<?>[] parameterTypes = method.getParameterTypes();
         Class<?>[] exceptionTypes = method.getExceptionTypes();    //方法抛出异常

         int idx = i;

         code.append("Object[] args = new Object[").append(parameterTypes.length).append("];");
         for(int j = 0; j < parameterTypes.length; j++) {
            code.append(" args[").append(j).append("] = ($w)$").append(j + 1).append(";");
         }

         code.append(" Object ret = handler.invoke(this, methods[" + idx + "], args);");
         if(!Void.TYPE.equals(returnType) ) {
            code.append(" return ").append(asArgument(returnType, "ret")).append(";");
         }

         StringBuilder sb = new StringBuilder(modifier(method.getModifiers()));
         sb.append(' ').append(getParameterType(returnType)).append(' ').append(method.getName());
         sb.append('(');
         for(int j = 0;j < parameterTypes.length; j++)
         {
            if(j > 0) {
               sb.append(',');
            }
            sb.append(getParameterType(parameterTypes[j]));
            sb.append(" arg").append(j);
         }
         sb.append(')');


         if( exceptionTypes.length > 0 ) {
            sb.append(" throws ");
            for(int j = 0; j < exceptionTypes.length; j++) {
               if(j > 0) {
                  sb.append(',');
               }
               sb.append(getParameterType(exceptionTypes[j]));
            }
         }
         sb.append('{').append(code.toString()).append('}');
         code.setLength(0);
         // 添加方法
         CtMethod ctMethod = CtMethod.make(sb.toString(), proxy);
         proxy.addMethod(ctMethod);
      }
      // 赋值 成员变量 methods
      proxy.setModifiers(Modifier.PUBLIC);
      Class<?> proxyClass = proxy.toClass(classLoader, null);
      proxyClass.getField("methods").set(null, methods);
      // 获取构造函数 并实例化和本地保存
      proxyInstance = proxyClass.getConstructor(InvocationHandler.class).newInstance(invocationHandler);
      Object old = proxyInstanceCache.putIfAbsent(targetInterface, proxyInstance);
      if(old != null){
         proxyInstance = old;
      }
      return proxyInstance;
   }

   private static String modifier(int mod) {
      if( Modifier.isPublic(mod) ) return "public";
      if( Modifier.isProtected(mod) ) return "protected";
      if( Modifier.isPrivate(mod) ) return "private";
      return "";
   }

   /**
    * 数组类型返回 String[]
    * @param c
    * @return
    */
   private static String getParameterType(Class<?> c) {
      if(c.isArray()) {   //数组类型
         StringBuilder sb = new StringBuilder();
         do {
            sb.append("[]");
            c = c.getComponentType();
         } while( c.isArray() );

         return c.getName() + sb.toString();
      }
      return c.getName();
   }

   private static String asArgument(Class<?> cl, String name) {
      if( cl.isPrimitive() ) {
         if( Boolean.TYPE == cl )
            return name + "==null?false:((Boolean)" + name + ").booleanValue()";
         if( Byte.TYPE == cl )
            return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
         if( Character.TYPE == cl )
            return name + "==null?(char)0:((Character)" + name + ").charValue()";
         if( Double.TYPE == cl )
            return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
         if( Float.TYPE == cl )
            return name + "==null?(float)0:((Float)" + name + ").floatValue()";
         if( Integer.TYPE == cl )
            return name + "==null?(int)0:((Integer)" + name + ").intValue()";
         if( Long.TYPE == cl )
            return name + "==null?(long)0:((Long)" + name + ").longValue()";
         if( Short.TYPE == cl )
            return name + "==null?(short)0:((Short)" + name + ").shortValue()";
         throw new RuntimeException(name+" is unknown primitive type.");
      }
      return "(" + getParameterType(cl) + ")"+name;
   }

   private static String generateClassName(Class<?> type){

      return String.format("%s$Proxy%d", type.getName(), counter.getAndIncrement());
   }

   /**
    * 获取指定接口的 public 方法
    *
    * @param targetInterfaces
    * @return
    */
   private static List<Method> getPublicMethods(Class<?> targetInterfaces) {
      List<Method> methods = new ArrayList<>();
      for (Method method : targetInterfaces.getDeclaredMethods()) {
         if (Modifier.isPublic(method.getModifiers())) {
            methods.add(method);
         }
      }
      return methods;
   }

}
