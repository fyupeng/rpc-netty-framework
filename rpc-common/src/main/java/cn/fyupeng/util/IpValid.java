package cn.fyupeng.util;


/**
 * @Auther: fyp
 * @Date: 2022/8/22
 * @Description:
 * @Package: cn.fyupeng.util
 * @Version: 1.0
 */
public class IpValid {

    public static boolean valid(String ip) {
        if(ip.equals("localhost")) return true;
        String z = ip.replace(".", ""); // 用空字符替代点
        int x = ip.length() - z.length(); // 点的个数
        int t =ip.indexOf("..");
        if(t<0)//判断连续点
        {
            boolean y = z.matches("[0-9]+"); // 判断除点外的字符是不是数字
            if (!y || !Character.isDigit(ip.charAt(0))
                    || !Character.isDigit(ip.charAt(ip.length() - 1))) {
                return false;
            }
            else if (x == 3) // 判断点的个数
            {
                int b = ip.indexOf('.'); // 第一个点的位置
                String c = ip.substring(0, ip.indexOf('.'));// 截取第一个数
                int i = Integer.parseInt(c); // 第一个数
                String d = ip.substring(b + 1); // 截取第一个点后面的数
                int e = d.indexOf('.'); // 第二个点的位置
                String f = d.substring(0, e); // 截取第二个数
                int j = Integer.parseInt(f); // 第二个数
                String g = d.substring(e + 1); // 截取第二个点后面的数
                int h = g.indexOf('.'); // 第三个点的位置
                String l = g.substring(0, h); // 截取第三个数
                int k = Integer.parseInt(l); // 第三个数
                String m = g.substring(h + 1); // 截取第三个点后面的数
                int n = Integer.parseInt(m); // 第四个数
                if((i>=0&&i <= 255)&& (j>=0&&j <= 255)&& (k>=0&&k <= 255) && (n>=0&&n <= 255))
                    return true;
                else
                    return false;
            } else
                return false;
        }
        else
            return false;
    }

    public static void main(String[] args) {
        boolean valid = IpValid.valid("127.0.0.1");
        System.out.println(valid);
    }

}
