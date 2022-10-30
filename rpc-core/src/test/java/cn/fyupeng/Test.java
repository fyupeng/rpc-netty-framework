package cn.fyupeng;


import cn.fyupeng.protocol.RpcRequest;

/**
 * @Auther: fyp
 * @Date: 2022/10/15
 * @Description:
 * @Package: com.fyupeng
 * @Version: 1.0
 */
public class Test {
    public static void main(String[] args) {

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaceName("userService");
        System.out.println(rpcRequest.getInterfaceName());
        System.out.println(rpcRequest.getGroup());

    }
}
