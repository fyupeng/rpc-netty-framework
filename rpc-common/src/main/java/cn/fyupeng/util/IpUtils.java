package cn.fyupeng.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther: fyp
 * @Date: 2022/8/22
 * @Description:
 * @Package: cn.fyupeng.util
 * @Version: 1.0
 */

@Slf4j
public class IpUtils {

    public static boolean valid(String ip) {
        if(ip.equals("localhost")) {
            return true;
        }
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

    /**
     * 目前较为稳定
     * @return
     */
    public static String getPubIpAddr() {
        String ip = "";
        String chinaz = "https://ip.chinaz.com";

        try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet request = new HttpGet(chinaz);

                // 设置 User-Agent 请求头
                request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

                // 执行请求并获取响应
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    HttpEntity entity = response.getEntity();
                    String content = EntityUtils.toString(entity);
                    // 如果请求成功，读取响应内容
                    //Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
                    Pattern p = Pattern.compile("\"请输入你想查询的IP或者域名\" value=\"(.*?)\"\n");
                    Matcher m = p.matcher(content);
                    if (m.find()) {
                        String ipstr = m.group(1);
                        ip = ipstr;
                    }
                } catch (MalformedURLException e) {
                    log.warn("get public ip error, try get from [http://checkip.amazonaws.com]");
                    throw new RuntimeException("get public ip error, try get from [http://checkip.amazonaws.com]");
                }
            } catch (IOException e) {
                log.warn("get public ip error, try get from [http://checkip.amazonaws.com]");
            throw new RuntimeException("get public ip error, try get from [http://checkip.amazonaws.com]");
            }
        return ip;
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10; i++) {
            String pubIpAddr = getPubIpAddr();
            System.out.println(pubIpAddr);
        }
    }

}