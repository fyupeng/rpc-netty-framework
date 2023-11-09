package cn.fyupeng;


import java.lang.reflect.InvocationHandler;

import java.lang.reflect.Method;

import java.lang.reflect.Proxy;

import java.text.DecimalFormat;

import cn.fyupeng.proxy.factory.javassist.ProxyGenerator;
import cn.fyupeng.service.CountServiceImpl;

import net.sf.cglib.proxy.Enhancer;

import net.sf.cglib.proxy.MethodInterceptor;

import net.sf.cglib.proxy.MethodProxy;

/**
 * @Auther: fyp
 * @Date: 2023/3/28
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */

public class DynamicProxyPerformanceTest {

   public static void main(String[] args) throws Exception {

      CountService delegate = new CountServiceImpl();

      long time = System.currentTimeMillis();

      CountService jdkProxy = createJdkDynamicProxy(delegate);

      time = System.currentTimeMillis() - time;

      System.out.println("Create JDK Proxy: " + time + " ms");

      time = System.currentTimeMillis();

      CountService cglibProxy = createCglibDynamicProxy(delegate);

      time = System.currentTimeMillis() - time;

      System.out.println("Create CGLIB Proxy: " + time + " ms");


      time = System.currentTimeMillis();

      CountService javassistBytecodeProxy = createJavassistBytecodeDynamicProxy(delegate);

      time = System.currentTimeMillis() - time;

      System.out.println("Create JAVAASSIST Bytecode Proxy: " + time + " ms");


      System.out.println("================");

      for (int i = 0; i < 3; i++) {

         test(jdkProxy, "Run JDK Proxy: ");

         test(cglibProxy, "Run CGLIB Proxy: ");

         test(javassistBytecodeProxy, "Run JAVAASSIST Bytecode Proxy: ");

         System.out.println("----------------");

      }

   }

   private static void test(CountService service, String label) throws Exception {

      service.count(); // warm up

      int count = 10000000;

      long time = System.currentTimeMillis();

      for (int i = 0; i < count; i++) {

         service.count();

      }

      time = System.currentTimeMillis() - time;

      System.out.println(label + time + " ms, " + new DecimalFormat().format(count * 1000 / time) + " t/s");

   }

   private static CountService createJdkDynamicProxy(final CountService delegate) {

      CountService jdkProxy = (CountService) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] { CountService.class }, new JdkHandler(delegate));

      return jdkProxy;

   }

   private static class JdkHandler implements InvocationHandler {

      final Object delegate;

      JdkHandler(Object delegate) {

         this.delegate = delegate;

      }

      public Object invoke(Object object, Method method, Object[] objects) throws Throwable {

         return method.invoke(delegate, objects);

      }

   }

   private static CountService createCglibDynamicProxy(final CountService delegate) throws Exception {

      Enhancer enhancer = new Enhancer();

      enhancer.setCallback(new CglibInterceptor(delegate));

      enhancer.setInterfaces(new Class[] { CountService.class });

      CountService cglibProxy = (CountService) enhancer.create();

      return cglibProxy;

   }

   private static class CglibInterceptor implements MethodInterceptor {

      final Object delegate;

      CglibInterceptor(Object delegate) {

         this.delegate = delegate;

      }

      public Object intercept(Object object, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

         return methodProxy.invoke(delegate, objects);

      }

   }



   //private static CountService createJavassistBytecodeDynamicProxy(CountService delegate) throws Exception {
   //
   //   ClassPool mPool = new ClassPool(true);
   //
   //   CtClass mCtc = mPool.makeClass(CountService.class.getName() + "JavaassistProxy");
   //
   //   mCtc.addInterface(mPool.get(CountService.class.getName()));
   //
   //   mCtc.addConstructor(CtNewConstructor.defaultConstructor(mCtc));
   //
   //   mCtc.addField(CtField.make("public " + CountService.class.getName() + " delegate;", mCtc));
   //
   //   mCtc.addMethod(CtNewMethod.make("public int count() { return delegate.count(); }", mCtc));
   //
   //   Class<?> pc = mCtc.toClass();
   //
   //   CountService bytecodeProxy = (CountService) pc.newInstance();
   //
   //   Field filed = bytecodeProxy.getClass().getField("delegate");
   //
   //   filed.set(bytecodeProxy, delegate);
   //
   //   return bytecodeProxy;
   //
   //}

   private static CountService createJavassistBytecodeDynamicProxy(CountService delegate) throws Exception {
      return (CountService) ProxyGenerator.newProxyInstance(delegate.getClass().getClassLoader(), CountService.class ,new JavassistHandler(delegate));

   }

   private static class JavassistHandler implements InvocationHandler {
      final Object delegate;
      JavassistHandler(Object delegate) {
         this.delegate = delegate;
      }
      public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
         return method.invoke(delegate, objects);
      }
   }



}
