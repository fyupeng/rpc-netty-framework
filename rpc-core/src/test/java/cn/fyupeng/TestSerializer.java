package cn.fyupeng;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: fyp
 * @Date: 2022/12/6
 * @Description:
 * @Package: cn.fyupeng.serializer
 * @Version: 1.0
 */


@Slf4j
public class TestSerializer {

   public static <T> byte[] serialize(T t){
      byte[] data = null;
      try {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         HessianOutput output = new HessianOutput(os);
         output.writeObject(t);
         data = os.toByteArray();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return data;
   }

   public static <T> byte[] serialize2(T t){
      byte[] data = null;
      try {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         Hessian2Output output = new Hessian2Output(os);
         output.writeObject(t);
         output.getBytesOutputStream().flush();
         output.completeMessage();
         output.close();
         data = os.toByteArray();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return data;
   }

   public static <T> byte[] jdkSerialize(T t){
      byte[] data = null;
      try {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         ObjectOutputStream output = new ObjectOutputStream(os);
         output.writeObject(t);
         output.flush();
         output.close();
         data = os.toByteArray();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return data;
   }

   @SuppressWarnings("unchecked")
   public static <T> T deserialize(byte[] data){
      if(data==null){
         return null;
      }
      Object result = null;
      try {
         ByteArrayInputStream is = new ByteArrayInputStream(data);
         HessianInput input = new HessianInput(is);
         result = input.readObject();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return (T)result;
   }

   @SuppressWarnings("unchecked")
   public static <T> T deserialize2(byte[] data){
      if(data==null){
         return null;
      }
      Object result = null;
      try {
         ByteArrayInputStream is = new ByteArrayInputStream(data);
         Hessian2Input input = new Hessian2Input(is);
         result = input.readObject();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return (T)result;
   }

   public static void main(String[] args) {




      //Student stu = new Student(1,"hessian",true);
      //String id = Sid.next();
      //
      //CommonSerializer serializer = CommonSerializer.getByCode(CommonSerializer.HESSIAN_SERIALIZER);
      //
      //LRedisHelper.syncSetRetryRequestResult(id, serializer.serialize(stu));
      //byte[] forRetryRequestId = LRedisHelper.getForRetryRequestId(id);
      //Student res = (Student) serializer.deserialize(forRetryRequestId, Student.class);
      //System.out.println(res);

      //NioEventLoopGrou= eventExecutors.next();
      //
      //next.submit(() -> {
      //   System.out.println("---------------");
      //   log.info("开始");
      //   try {
      //      Thread.sleep(3000);
      //   } catch (InterruptedException e) {
      //      e.printStackTrace();
      //   }
      //   System.out.println(Thread.currentThread());
      //   log.info("结束");
      //   System.out.println("---------------");
      //});
      //
      //next.submit(() -> {
      //   System.out.println("---------------");
      //   log.info("开始");
      //   try {
      //      Thread.sleep(3000);
      //   } catch (InterruptedException e) {
      //      e.printStackTrace();
      //   }
      //   System.out.println(Thread.currentThread());
      //   log.info("结束");
      //   System.out.println("---------------");
      //});
      //
      //next.execute(() -> {
      //   System.out.println("---------------");
      //   log.info("开始");
      //   try {
      //      Thread.sleep(3000);
      //   } catch (InterruptedException e) {
      //      e.printStackTrace();
      //   }
      //   System.out.println(Thread.currentThread());
      //   log.info("结束");
      //   System.out.println("---------------");
      //});
      //
      //next.execute(() -> {
      //   System.out.println("---------------");
      //   log.info("开始");
      //   try {
      //      Thread.sleep(3000);
      //   } catch (InterruptedException e) {
      //      e.printStackTrace();
      //   }
      //   System.out.println(Thread.currentThread());
      //   log.info("结束");
      //   System.out.println("---------------");
      //});p eventExecutors = new NioEventLoopGroup();

   }

}