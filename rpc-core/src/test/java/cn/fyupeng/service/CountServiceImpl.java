package cn.fyupeng.service;

import cn.fyupeng.CountService;

/**
 * @Auther: fyp
 * @Date: 2023/3/28
 * @Description:
 * @Package: cn.fyupeng.service
 * @Version: 1.0
 */
public class CountServiceImpl implements CountService {

    private int count = 0;

    public int count() {

        return count++;

    }

}