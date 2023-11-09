package cn.fyupeng.enums;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: cn.fyupeng.enums
 * @Version: 1.0
 */
public enum ProtocolType {


   JAVA_PROTOCOL(67, "java"),
   GOLANG_PROTOCOL(54,"go");

   private final int code;
   private final  String message;

   ProtocolType(int code, String message) {
      this.code = code;
      this.message = message;
   }

   public int getCode() {
      return code;
   }

   public String getMessage() {
      return message;
   }
}
