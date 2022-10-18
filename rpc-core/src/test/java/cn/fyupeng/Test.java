package cn.fyupeng;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import io.netty.util.concurrent.CompleteFuture;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Auther: fyp
 * @Date: 2022/10/15
 * @Description:
 * @Package: com.fyupeng
 * @Version: 1.0
 */
public class Test {
    public static void main(String[] args) {

        JSONArray objects = new JSONArray();
        JSONObject o1 = new JSONObject();
        o1.put("key1", "value1");
        o1.put("k2", "v2");
        objects.add(o1);
        objects.add(o1);
        objects.add(o1);

        System.out.println(objects);

    }
}
