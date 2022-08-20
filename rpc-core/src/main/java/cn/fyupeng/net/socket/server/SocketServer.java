package cn.fyupeng.net.socket.server;

import cn.fyupeng.exception.RpcException;
import cn.fyupeng.factory.ThreadPoolFactory;
import cn.fyupeng.handler.RequestHandler;
import cn.fyupeng.hook.ShutdownHook;
import cn.fyupeng.net.AbstractRpcServer;
import cn.fyupeng.provider.DefaultServiceProvider;
import cn.fyupeng.registry.NacosServiceRegistry;
import cn.fyupeng.serializer.CommonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @Auther: fyp
 * @Date: 2022/3/22
 * @Description:
 * @Package: cn.fyupeng.net.socket.server
 * @Version: 1.0
 */
@Slf4j
public class SocketServer extends AbstractRpcServer {

    // static final 修饰的 基本变量 字符串常量 在虚拟机 类加载 就已经 确定，确保线程安全
    /**
    private static final int maximumPoolSize = 50;
    private static final long keepAliveTime = 60;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    private static final int corePoolSize = Runtime.getRuntime().availableProcessors();
     fix : 采用 工厂 模式 来 创建 线程池
     */

    // 非静态 变量在 用户初始化时 传入参数来决定，不需静态
    private final ExecutorService threadPool;
    private final CommonSerializer serializer;
    private final RequestHandler requestHandler = new RequestHandler();

    public SocketServer(String host, int port, Integer serializerCode) throws RpcException {
        /**
        BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workingQueue, threadFactory);
        */
        this.hostName = host;
        this.port = port;
        threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        /**
         * 继承了 抽象类 AbstractRpcServer 需要 在子类 中 赋值 父类的 字段，才可 注册服务到 服务提供者 和 发布服务到 nacos
         * 即可 调用 父类 的 publishService() 将 服务注册到 macos 上
         */
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new DefaultServiceProvider();
        this.serializer = CommonSerializer.getByCode(serializerCode);
        scanServices();
     }


    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)){
            log.info("Server is running...");
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            // 监听 客户端连接
            while ((socket = serverSocket.accept()) != null) {
                log.info("customer has connected successfully! ip = " + socket.getInetAddress());
                threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("Exception throws when connecting, info: {}", e);
        }
    }

}
