package cn.fyupeng.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

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

     * 方法描述：获取公网ip

     *@return

     */
    public static String getPubIpAddr() {
        try {
            URL url = new URL("http://pv.sohu.com/cityjson?ie=utf-8");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String s = "";
            StringBuffer sb = new StringBuffer("");
            String webContent = "";
            while ((s = br.readLine()) != null) {
                sb.append(s + "\r\n");
            }
            br.close();
            webContent = sb.toString();
            int start = webContent.indexOf("{");
            int end = webContent.indexOf("}") + 1;
            webContent = webContent.substring(start,end);
            CitySN target = JsonUtils.jsonToPojo(webContent, CitySN.class);
            return target.getCip();
        } catch (Exception e) {
            log.warn("get public IP address error: {}", e);
            return getPubIpAddr0();
        }
    }

    private static String getPubIpAddr0() {
        String url = "https://ip.renfei.net/";
        // 创建CloseableHttpClient
        CloseableHttpClient client =  HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(url);
        /**
         * Accept -- value
         * xml、text/xml、application/xml ==> xml
         * text、text/plain ==> ip
         * text/html、application/xhtml+xml ==> html
         */
        httpPost.setHeader("Accept", "text/plain");
        String result = "127.0.0.1";
        try {
            CloseableHttpResponse response = client.execute(httpPost);
            log.info("response: {}", response);
            log.info("response: {}", response.getEntity().getContent());
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode != 200) {
                log.error("statusCode={}", statusCode);
                log.error("responseEntity={}", response.getEntity());
                response.close();
                throw new RuntimeException("接口调用失败");
            }
            result = EntityUtils.toString(response.getEntity(), "utf-8");
            log.info("get public IP address result：{}", result);
        } catch (IOException e) {
            throw new RuntimeException("get public IP address error");
        }
        return result;
    }


    public static void main(String[] args) {
        //boolean valid = IpUtils.valid("127.0.0.1");
        //System.out.println(valid);
        String pubIpAddr = getPubIpAddr();
        System.out.println(pubIpAddr);
    }

}

class CitySN {
    private String cip;
    private String cid;
    private String cname;

    public String getCip() {
        return cip;
    }

    public void setCip(String cip) {
        this.cip = cip;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    @Override
    public String toString() {
        return "CitySN{" +
                "cip='" + cip + '\'' +
                ", cid=" + cid +
                ", cname='" + cname + '\'' +
                '}';
    }
}
