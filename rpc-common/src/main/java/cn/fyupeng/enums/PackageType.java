package cn.fyupeng.enums;

import sun.security.pkcs11.wrapper.CK_SSL3_MASTER_KEY_DERIVE_PARAMS;

/**
 * @Auther: fyp
 * @Date: 2022/3/24
 * @Description:
 * @Package: cn.fyupeng.enums
 * @Version: 1.0
 */
public enum PackageType {


   /**
    * 61 -> a 的 anssi 值
    * 726571 = > request(req) 的 单字符 anssi 组合值 =>
    * 726573 = > response(res) 的 单字符 anssi 组合值
    * MASK = > 掩码 => 726500
    */
   REQUEST_PACK(71, "q"),
   RESPONSE_PACK(73,"s");

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
