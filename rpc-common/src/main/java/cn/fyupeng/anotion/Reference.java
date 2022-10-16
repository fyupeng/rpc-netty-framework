package cn.fyupeng.anotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Auther: fyp
 * @Date: 2022/10/16
 * @Description: 客户端服务配置
 * @Package: cn.fyupeng.anotion
 * @Version: 1.0
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Reference {
   /**
    * 服务名
    * @return
    */
   public String name() default "";

   /**
    * 重试次数
    * @return
    */
   public int retries() default 0;

   /**
    * 超时时间
    * @return
    */
   public long timeout() default 0;
}
