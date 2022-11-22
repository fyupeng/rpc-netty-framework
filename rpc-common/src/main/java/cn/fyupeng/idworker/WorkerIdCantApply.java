package cn.fyupeng.idworker;

/**
 * 机器码节点无法申请，因为节点数已满足最大值
 */
public class WorkerIdCantApply extends RuntimeException {
    public WorkerIdCantApply(String message) {
        super(message);
    }
}
