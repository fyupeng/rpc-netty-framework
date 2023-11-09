import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ParameterMetaData;

/**
 * @Auther: fyp
 * @Date: 2023/3/27
 * @Description:
 * @Package: PACKAGE_NAME
 * @Version: 1.0
 */
public class Test {
   public static void main(String[] args) {

      Class<?> aClass = HelloWorldService.class;

      InvocationHandler handler = new InvocationHandler() {
         @Override
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (Class<?> parameterType : parameterTypes) {
               System.out.println(parameterType.getName());
            }
            return null;
         }
      };

      HelloWorldService service = (HelloWorldService) Proxy.newProxyInstance(aClass.getClassLoader(), new Class<?>[]{aClass}, handler);
      service.sayHello("hello");


   }
}
