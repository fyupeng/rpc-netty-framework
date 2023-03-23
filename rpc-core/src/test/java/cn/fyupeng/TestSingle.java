package cn.fyupeng;

import cn.fyupeng.factory.SingleFactory;
import cn.fyupeng.net.netty.client.UnprocessedResults;

/**
 * @Auther: fyp
 * @Date: 2023/3/18
 * @Description:
 * @Package: cn.fyupeng
 * @Version: 1.0
 */
public class TestSingle {
    public static void main(String[] args) {
        UnprocessedResults instance1 = SingleFactory.getInstance(UnprocessedResults.class);
        UnprocessedResults instance2 = SingleFactory.getInstance(UnprocessedResults.class);
        UnprocessedResults instance3 = SingleFactory.getInstance(UnprocessedResults.class);

        System.out.println(instance1);
        System.out.println(instance2);
        System.out.println(instance3);
    }
}
