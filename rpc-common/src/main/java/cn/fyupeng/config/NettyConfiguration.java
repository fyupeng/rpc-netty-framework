package cn.fyupeng.config;

import cn.fyupeng.constant.PropertiesConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @Auther: fyp
 * @Date: 2025/9/4
 * @Description:
 * @Package: cn.fyupeng.util
 * @Version: 1.0
 */
@Slf4j
public class NettyConfiguration implements Configuration {

    private static Integer frameLength = PropertiesConstants.DEFAULT_NETTY_FRAME_LENGTH;

    static {
        String currentWorkPath = System.getProperty("user.dir");
        PropertyResourceBundle configResource = null;
        
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(currentWorkPath + "/config/resource.properties"));) {

            configResource = new PropertyResourceBundle(bufferedReader);
            String nettyFrameLength = configResource.getString(PropertiesConstants.NETTY_FRAME_LENGTH);
            int nettyFrameLengthValue = 0;
            try {
                nettyFrameLengthValue = Integer.parseInt(nettyFrameLength);
            } catch (Exception e) {
                log.warn("netty frameLength must be Integer, current value is {}", nettyFrameLength);
                log.info("use default FrameLength {}", frameLength);
            }
            if (nettyFrameLengthValue != 0) {
                frameLength = nettyFrameLengthValue;
            }
            log.info("read resource from resource path: {}", currentWorkPath + "/config/resource.properties");
        } catch (MissingResourceException frameLengthUseException) {
            log.info("netty frameLength is missing and use default value {}", frameLength);

        } catch (IOException ioException) {
            log.info("not found resource from resource path: {}", currentWorkPath + "/config/resource.properties");
            try {
                ResourceBundle resource = ResourceBundle.getBundle("resource");
                String nettyFrameLength = resource.getString(PropertiesConstants.NETTY_FRAME_LENGTH);
                int nettyFrameLengthValue = 0;
                try {
                    try {
                        nettyFrameLengthValue = Integer.parseInt(nettyFrameLength);
                    } catch (Exception e) {
                        log.warn("netty frameLength must be Integer, current value is {}", nettyFrameLength);
                        log.info("use default FrameLength {}", frameLength);
                    }
                    if (nettyFrameLengthValue != 0) {
                        frameLength = nettyFrameLengthValue;
                    }
                } catch (MissingResourceException frameLengthUseException) {
                    log.info("netty frameLength is missing and use default value {}", frameLength);
                }
            } catch (MissingResourceException resourceException) {
                log.info("not found resource from resource path: {}", "resource.properties");
                log.info("use default FrameLength {}", frameLength);
            }
        }
        log.info("read resource from resource path: {}", "resource.properties");
        log.info("------------ netty Configuration 【 begin 】 ------------");
        log.info("frameLength: [{}]", frameLength);
        log.info("------------ netty Configuration 【 end 】 ------------");
    }

    public static int getNettyFrameLength() {
        return frameLength;
    }

}

