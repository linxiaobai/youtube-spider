package youtube.spider.parse;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.spider.util.CommonConstants;
import youtube.spider.util.HttpUtils;
import youtube.spider.util.JsoupUtils;
import youtube.spider.util.ParseJsonUtils;
import youtube.spider.util.SpiderDateUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jianlin
 */
public enum GetVideoUrlsParse implements ParseStrategy<Set<String>> {
    INSTANCE("GET YOUTUBE VIDEO URLS");
    private static final Logger logger = LoggerFactory.getLogger(GetVideoUrlsParse.class);
    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>();

    static {
        DEFAULT_HEADERS.put(CommonConstants.HEADER_ACCEPT_NAME, CommonConstants.HEADER_ACCEPT);
        DEFAULT_HEADERS.put(CommonConstants.HEADER_ACCEPT_ENCODING_NAME, CommonConstants.HEADER_ACCEPT_ENCODING);
        DEFAULT_HEADERS.put(CommonConstants.HEADER_USER_AGENT_NAME, CommonConstants.HEADER_USER_AGENT);
        DEFAULT_HEADERS.put(CommonConstants.HEADER_CONNECTION_NAME, CommonConstants.HEADER_CONNECTION);
        DEFAULT_HEADERS.put("accept-language", "zh-CN,zh;q=0.8"); //youtube会根据请求头accept-language返回对应的国家语言
    }

    private String strategyName;

    GetVideoUrlsParse(String strategyName) {
        this.strategyName = strategyName;
    }

    private static String getNextUrlFromDocument(Document document) {
        if (document == null) {
            return null;
        }
        return document.getElementsByAttributeValue("data-uix-load-more-target-id", "channels-browse-content-grid").first().attr("data-uix-load-more-href");
    }

    private static Set<String> getVideoUrlsFromDocument(Document document, Date filterDate) {
        if (document == null) {
            return null;
        }

        Set<String> videoUrls = new HashSet<String>();
        Elements videoInfoElements = document.select("div.yt-lockup-dismissable");
        for (Element videoInfoElement : videoInfoElements) {
            try {
                Element timeInfo = videoInfoElement.select("div.yt-lockup-content").first();
                String uploadDateStr = timeInfo.select("ul.yt-lockup-meta-info").first().child(1).text();
                Date uploadDate = SpiderDateUtils.getUploadTime(uploadDateStr);
                if (filterDate != null && filterDate.compareTo(uploadDate) > 0) {
                    break;
                }
                Element thumbInfo = videoInfoElement.select("div.yt-lockup-thumbnail").first();
                Element hrefElement = thumbInfo.select("a.yt-uix-sessionlink").first();
                videoUrls.add(hrefElement.attr("href"));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                continue;
            }
        }

        return videoUrls;
    }

    @Override
    public Set<String> parse(Document document) {
        return parse(document, null);
    }

    @Override
    public Set<String> parse(Document document, Date filterDate) {
        Set<String> allVideoUrls = new HashSet<String>();
        //从document对象中获取所有视频的地址
        try {
            Set<String> videoUrls = getVideoUrlsFromDocument(document, filterDate);
            allVideoUrls.addAll(videoUrls);
            String nextPageUrl = getNextUrlFromDocument(document);
            while (StringUtils.isNotBlank(nextPageUrl)) {
                String jsonStr = HttpUtils.get(CommonConstants.YOUTUBE_URL + nextPageUrl, CommonConstants.USE_PROXY, DEFAULT_HEADERS);
                if (StringUtils.isNotBlank(jsonStr)) {
                    String nextPageUrlData = ParseJsonUtils.getValueByKey(jsonStr, "load_more_widget_html");
                    String nextPageData = ParseJsonUtils.getValueByKey(jsonStr, "content_html");
                    document = Jsoup.parse(nextPageData);
                    videoUrls = getVideoUrlsFromDocument(document, filterDate);
                    if (filterDate != null && videoUrls.isEmpty()) { //如果该页数据都为空，表面后面几页视频也不满足上传时间为最近几个月要求
                        break;
                    }
                    allVideoUrls.addAll(videoUrls);
                    if (StringUtils.isBlank(nextPageUrlData)) {
                        break;
                    }
                    Document nextDocument = Jsoup.parse(nextPageUrlData);
                    nextPageUrl = getNextUrlFromDocument(nextDocument);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return allVideoUrls;
    }

    public static void main(String[] args) {
        Set<String> list = GetVideoUrlsParse.INSTANCE.parse(JsoupUtils.getDocument("https://www.youtube.com/user/failarmy/videos", true, CommonConstants.HEAD_MAP), DateUtils.addDays(new Date(), CommonConstants.FILTER_DATE));
        for (String s : list) {
            System.out.println(s);
        }
        System.out.println(list.size());
    }
}
