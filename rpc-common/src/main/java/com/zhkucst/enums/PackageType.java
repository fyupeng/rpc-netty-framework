package com.zhkucst.enums;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: com.zhkucst.enums
 * @Version: 1.0
 */
public enum PackageType {
   REQUEST_PACK(726571, "req"),
   RESPONSE_PACK(726574,"res");

   private final int code;
   private final  String message;

   PackageType(int code, String message) {
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
