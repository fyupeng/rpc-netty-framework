package org.n3r.idworker;

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
     */
    protected long sequenceMask = -1L ^ (-1L << sequenceBits);

    /**
     * 上一次系统时间 时间戳
     * 用于 判断系统 是否发生 时钟回拨 异常
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
    protected Logger logger = LoggerFactory.getLogger(IdWorker.class);

    public IdWorker(long workerId) {
        this.workerId = checkWorkerId(workerId);

        logger.debug("worker starting. timestamp left shift {}, worker id {}", timestampLeftShift, workerId);
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

        if (timestamp < lastMillis) {
            logger.error("clock is moving backwards.  Rejecting requests until {}.", lastMillis);
            throw new InvalidSystemClock(String.format(
                    "Clock moved backwards.  Refusing to generate id for {} milliseconds", lastMillis - timestamp));
        }

        if (lastMillis == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0)
                timestamp = tilNextMillis(lastMillis);
        } else {
            sequence = 0;
        }

        lastMillis = timestamp;
        long diff = timestamp - getEpoch();
        return (diff << timestampLeftShift) |
                (workerId << workerIdShift) |
                sequence;
    }

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
