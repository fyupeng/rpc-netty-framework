//package cn.fyupeng;
//
//
//import org.safehaus.uuid.UUID;
//import org.safehaus.uuid.UUIDGenerator;
//
///**
// * @Auther: fyp
// * @Date: 2023/4/14
// * @Description:
// * @Package: cn.fyupeng
// * @Version: 1.0
// */
//public class Test {
//    /**
//     * 生成32位UUID，返回类型为String（32）
//     * @return
//     */
//    public static String getUUID(){
//
//        UUIDGenerator UUIDgenerator = UUIDGenerator.getInstance();
//        UUID uuid = UUIDgenerator.generateRandomBasedUUID();
//        String result = uuid.toString().replaceAll("-", "");
//
//        return result;
//    }
//    /*
//     * Demonstraton and self test of class
//     */
//    public static void main(String args[]) {
//        for (int i = 0; i < 100; i++)
//        System.out.println(Test.getUUID().toUpperCase());
//    }
//
//}
