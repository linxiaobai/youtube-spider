package youtube.spider.util;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jianlin210349
 */
public class GetVideoDownloadUrlUtils {


    //提取页面中的json数据
    private static final String JSON_REGEX = "ytplayer.config = (.*?});";
    private static final Pattern JSON_PATTERN = Pattern.compile(JSON_REGEX);


    /**

     */

    /**
     * 根据视频URL获取下载地址
     *
     * @param playUrl 播放地址
     * @param tag     码率类别
     *                "13" => ("3GP", "Low Quality - 176x144"),
     *                "17" => ("3GP", "Medium Quality - 176x144"),
     *                "36" => ("3GP", "High Quality - 320x240"),
     *                "5" => ("FLV", "Low Quality - 400x226"),
     *                "34" => ("FLV", "Medium Quality - 640x360"),
     *                "35" => ("FLV", "High Quality - 854x480"),
     *                "43" => ("WEBM", "Low Quality - 640x360"),
     *                "44" => ("WEBM", "Medium Quality - 854x480"),
     *                "45" => ("WEBM", "High Quality - 1280x720"),
     *                "18" => ("MP4", "Medium Quality - 480x360"),
     *                "22" => ("MP4", "High Quality - 1280x720"),
     *                "37" => ("MP4", "High Quality - 1920x1080"),
     *                "38" => ("MP4", "High Quality - 4096x230")
     * @return
     */
    public static String getDownloadUrl(String playUrl, String tag) {
        String htmlContent = HttpUtils.getRobust(playUrl, CommonConstants.DEFAULT_RETRY_TIME, CommonConstants.USE_PROXY);
        Matcher matcher = JSON_PATTERN.matcher(htmlContent);

        String jsonData = null;
        if (matcher.find()) {
            jsonData = matcher.group(1);
        }
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONObject argsObject = jsonObject.getJSONObject("args");
        String streamMapJson = argsObject.getString("url_encoded_fmt_stream_map");

        streamMapJson.replaceAll("\\u0026", "&");

        String[] urls = streamMapJson.split(",");
        for (String url : urls) {
            if (url.contains("&itag=" + tag)) {
                //有些url解析出来带重复的itag，需要去掉一个
                url = url.replaceAll("&itag=" + tag, "");
                url = url.replaceAll("%26itag%3D" + tag, "");
                int httpUrlIndex = url.indexOf("url=") + 4;
                url = url.substring(httpUrlIndex);
                if (url.contains("%3B+codecs")) {//需要过滤掉后缀，否则URL解析不了
                    url = url.split("%3B\\+codecs")[0];
                }
                try {
                    return URLDecoder.decode(url, "UTF-8") + "&itag=" + tag;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(getDownloadUrl("https://www.youtube.com/watch?v=GBRxe6mfjbI", CommonConstants.MP4_M_480x360));
    }
}
