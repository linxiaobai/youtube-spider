package youtube.spider.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jianlin210349
 */
public class CommonConstants {

    //http请求头名称
    public static final String HEADER_ACCEPT_NAME = "Accept";
    public static final String HEADER_ACCEPT_ENCODING_NAME = "Accept-Encoding";
    public static final String HEADER_ACCEPT_LANGUAGE_NAME = "Accept-Language";
    public static final String HEADER_USER_AGENT_NAME = "User-Agent";
    public static final String HEADER_CONNECTION_NAME = "Connection";
    public static final String HEADER_HOST = "Host";

    //http请求通用头信息配置
    public static final String HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    public static final String HEADER_ACCEPT_ENCODING = "gzip, deflate, sdch";
    public static final String HEADER_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
    public static final String HEADER_CONNECTION = "keep-alive";

    //HTTP请求默认重试次数
    public static final int DEFAULT_RETRY_TIME = 2;


    //TODO 这里改成你的代理IP和端口
    public static final String PROXY_IP = "";
    public static final int PROXY_PORT = 8888;


    public static final boolean USE_PROXY = true;
    //太高清晰度版本不一定有
    public static final String MP4_M_480x360 = "18";
    public static final String MP4_H_1280x720 = "22";

    public static final Map<String, String> HEAD_MAP = new HashMap<String, String>() {
        {
            put(CommonConstants.HEADER_ACCEPT_LANGUAGE_NAME, "zh-CN,zh;q=0.8");
        }
    };


    public static final String YOUTUBE_URL = "https://www.youtube.com";


    //过滤3个月前的视频
    public static final int FILTER_DATE = -90;

}
