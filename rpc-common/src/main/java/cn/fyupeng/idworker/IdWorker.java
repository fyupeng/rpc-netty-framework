package cn.fyupeng.idworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

public class IdWorker {
    /**
     * 自定义 分布式唯一号 id
     *  1 位 符号位
     * 41 位 时间戳
     * 10 位 工作机器 id
     * 12 位 并发序列号
     *
     *       The distribute unique id is as follows :
     * +---------------++---------------+---------------+-----------------+
     * |     Sign      |     epoch     |    workerId   |     sequence     |
     * |    1 bits     |    41 bits    |    10 bits   |      12 bits      |
     * +---------------++---------------+---------------+-----------------+
     */

    /**
     * 服务器运行时 开始时间戳
     */
    protected long epoch = 1288834974657L;
//    protected long epoch = 1387886498127L; // 2013-12-24 20:01:38.127
    /**
     * 机器 id 所占位数
     */
    protected long workerIdBits = 10L;
    /**
     * 最大机器 id
     * 结果为 1023
     */
    protected long maxWorkerId = -1L ^ (-1L << workerIdBits);
    /**
     * 并发序列在 id 中占的位数
     */
    protected long sequenceBits = 12L;

    /**
     * 机器 id 掩码
     */
    protected long workerIdShift = sequenceBits;

    /**
     * 时间戳 掩码
     */
    protected long timestampLeftShift = sequenceBits + workerIdBits;
    /**
     * 并发序列掩码
     * 二进制表示为 12 位 1 (ob111111111111=0xfff=4095)
     * 也表示序列号最大数
     */
    protected long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 上一次系统时间 生成 id 的时间戳
     */
    protected long lastMillis = -1L;
    /**
     * 工作机器 ID (0-1023)
     */
    protected final long workerId;
    /**
     * 并发冲突序列 (0-4095)
     * 即毫秒内并发量
     */
    protected long sequence = 0L;
    /**
     *  当前 系统时间 已经 不在时间回拨内
     *
     *      ↓ ${timestamp}        ↓ ${lastMillis} 、 ${lockBackTimestamp}
     * 111111111111111111111111111100000000000000000000000000000000000000
     * ---------------------------------------------------------------------------- (时间线 0-未使用, 1-已使用)
     *                               | |
     *                               | |
     *                              _| |__
     *                              \   /
     *                               \ /
     *
     *      ↓ ${lockBackTimestamp}        ↓ ${timestamp}      ↓ ${lastMillis}
     * 111111111111111111111111111111111111111111111111111111110000000000000000000
     * ---------------------------------------------------------------------------- (时间线 0-未使用, 1-已使用)
     */
    /**
     * 是否 处于 时间回拨
     * 处于 时间回拨 lastMillis 不再更新
     */
    protected boolean isLockBack = false;
    /**
     * 是否 首次发生 时间回拨
     */
    protected boolean isFirstLockBack = true;
    /**
     * 发生 时间回拨 那一刻 时间戳
     */
    protected long lockBackTimestamp;

    protected Logger logger = LoggerFactory.getLogger(IdWorker.class);

    public IdWorker(long workerId) {
        this.workerId = checkWorkerId(workerId);
    }

    public long getEpoch() {
        return epoch;
    }

    private long checkWorkerId(long workerId) {
        // sanity check for workerId
        if (workerId > maxWorkerId || workerId < 0) {
            int rand = new SecureRandom().nextInt((int) maxWorkerId + 1);
            logger.warn("worker Id can't be greater than {} or less than 0, use a random {}", maxWorkerId, rand);
            return rand;
        }

        return workerId;
    }

    public synchronized long nextId() {
        long timestamp = millisGen();
        // 上次判定 仍处于 回拨时间内 并且 当前时间系统时间 已经 恢复到 上一次 生成 id 时间戳
        //logger.info("timestamp " + timestamp + " lastMillis " + lastMillis);
        if (isLockBack && timestamp > lastMillis) {
            logger.info(">> Clock dial back to normal <<");
            isFirstLockBack = true;
            isLockBack = false;
        }

        // 发生 时间回拨
        if (timestamp < lastMillis) {
            /**
             * 逻辑 恢复 上一个生成 id 时间戳，当成 时钟回拨 没发生
             * 1. 回拨前 时间戳 大于 上一个 系统时间 生成 id 时间戳
             * 2. 回拨前 时间戳 等于 上一个 系统时间 生成 id 时间戳
              */
            timestamp = lastMillis;
            // 判定 当前仍在 回拨时间内
            isLockBack = true;
            // 首次发生 回拨
            if (isFirstLockBack) {
                logger.warn(">> Clock callback occurs when the ID is generated <<");
                // 记录 当前回拨的时间戳（只会在首次记录）
                lockBackTimestamp = lastMillis;
                // 已经发生回拨了
                isFirstLockBack = false;
            }
        }
        // 当前时间戳 与 上一个 时间戳 在同一毫秒 或 发生时间回拨 逻辑恢复
        if (timestamp == lastMillis) {
            sequence = (sequence + 1) & sequenceMask;
            // 序列号已经最大了，需要阻塞新的时间戳
            // 表示这一毫秒并发量已达上限，新的请求会阻塞到新的时间戳中去
            if (sequence == 0)
                // 发生时间回拨 不能去 阻塞， 因为使用到了当前时间
                if (isLockBack) {
                    // 直接让 上一毫秒 + 1， 产生新的 序列号
                    timestamp = ++lastMillis;
                } else {
                    timestamp = tilNextMillis(lastMillis);
                }
        // 当前毫秒 大于 上一个毫秒，更新 序列号
        } else {
            sequence = 0; // 竞争不激烈时，id 都是偶数
            // 竞争不激烈时 每毫米 刚开始序列号 id 分布均匀
            //sequence = timestamp & 1; // 0 或者 1
        }

        // 前面如果 发生时间回拨 会恢复（逻辑上，系统时间还是没有恢复）发生回拨时的 时间戳

        //正常状态 保存 上一次时间戳
        if (!isLockBack) lastMillis = timestamp;

        long diff = timestamp - getEpoch();

        return (diff << timestampLeftShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    /**
     * 阻塞生成下一个更大的时间戳
     * @param lastMillis
     * @return
     */
    protected long tilNextMillis(long lastMillis) {
        long millis = millisGen();
        while (millis <= lastMillis)
            millis = millisGen();

        return millis;
    }

    protected long millisGen() {
        return System.currentTimeMillis();
    }

    public long getLastMillis() {
        return lastMillis;
    }

    public long getWorkerId() {
        return workerId;
    }
}
