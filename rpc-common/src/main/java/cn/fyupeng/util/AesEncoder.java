package cn.fyupeng.util;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

/**
 * @Auther: fyp
 * @Date: 2023/11/11
 * @Description: Aes跨语言密钥编解码
 * @Package: cn.fyupeng.util
 * @Version: 1.0
 */
public class AesEncoder {

   // 密钥
   private static final String key = DigestUtils.md5Hex("cvrhsdftredhghgfjhgwsfresdsfhjk").substring(0, 16);


   private static byte[] encrypt(String key, byte[] origData) throws GeneralSecurityException {
      byte[] keyBytes = getKeyBytes(key);
      byte[] buf = new byte[16];
      System.arraycopy(keyBytes, 0, buf, 0, Math.max(keyBytes.length, buf.length));
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(buf, "AES"), new IvParameterSpec(keyBytes));
      return cipher.doFinal(origData);

   }

   private static byte[] decrypt(String key, byte[] crypted) throws GeneralSecurityException {
      byte[] keyBytes = getKeyBytes(key);
      byte[] buf = new byte[16];
      System.arraycopy(keyBytes, 0, buf, 0, Math.max(keyBytes.length, buf.length));
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(buf, "AES"), new IvParameterSpec(keyBytes));
      return cipher.doFinal(crypted);
   }

   private static byte[] getKeyBytes(String key) {
      byte[] bytes = key.getBytes();
      return bytes.length == 16 ? bytes : Arrays.copyOf(bytes, 16);
   }

   public static String encrypt(String val) {
      try {
         byte[] origData = val.getBytes();
         byte[] crafted = encrypt(key, origData);
         return Base64.getEncoder().encodeToString(crafted);
      }catch (Exception e){
         return "";
      }
   }

   public static String decrypt(String val) throws GeneralSecurityException {
      byte[] crypted = Base64.getDecoder().decode(val);
      byte[] origData = decrypt(key, crypted);
      return new String(origData);
   }


   public static void main(String[] args) throws Exception {
      // 明文
      String val = "hello,ase";
      String ret = encrypt(val);
      System.out.println(ret);
      // VYGThW0MXPf4v88IKP/o4g==
   }


}
