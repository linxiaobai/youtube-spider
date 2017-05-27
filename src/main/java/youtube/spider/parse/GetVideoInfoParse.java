package youtube.spider.parse;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.spider.model.VideoInfo;
import youtube.spider.util.CommonConstants;
import youtube.spider.util.FormatUtils;
import youtube.spider.util.JsoupUtils;
import youtube.spider.util.SpiderDateUtils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author jianlin
 */
public enum GetVideoInfoParse implements ParseStrategy<VideoInfo> {
    INSTANCE("GET YOUTUBE VIDEO INFO");
    private static final Logger logger = LoggerFactory.getLogger(GetVideoInfoParse.class);
    private String strategyName;

    GetVideoInfoParse(String strategyName) {
        this.strategyName = strategyName;
    }


    private static final String TIME_REGEX = "PT(\\d+)M(\\d+)S";
    private static final Pattern PATTERN = Pattern.compile(TIME_REGEX);

    /**
     * 字符串时间转成秒
     *
     * @param timeText 格式:PT2M49S,PT3M7S,PT88M35S
     * @return
     */
    private static int convertTimeTextToSecond(String timeText) {
        Matcher matcher = PATTERN.matcher(timeText);
        int minute = 0;
        int second = 0;
        if (matcher.find()) {
            minute = Integer.valueOf(matcher.group(1));
            second = Integer.valueOf(matcher.group(2));
        }
        return minute * 60 + second;
    }

    @Override
    public VideoInfo parse(Document document) {
        return parse(document, null);
    }

    @Override
    public VideoInfo parse(Document document, Date filterDate) {
        try {
            String title = document.select("#eow-title").first().attr("title");
            String playUrl = document.baseUri();
            String playCount = document.getElementsByAttributeValueStarting("itemprop", "interactionCount").first().attr("content");
            String uploadDate = document.getElementsByAttributeValueStarting("itemprop", "datePublished").first().attr("content");
            String description = document.getElementsByAttributeValueStarting("itemprop", "description").first().attr("content");
            String durationText = document.getElementsByAttributeValueStarting("itemprop", "duration").first().attr("content");
            Element actionSpan = document.select("span.like-button-renderer").first();
            String likeCount = actionSpan.select("span.yt-uix-button-content").first().text();

            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setDescription(description);
            videoInfo.setDuration(convertTimeTextToSecond(durationText));
            videoInfo.setLikeCount(FormatUtils.formatNumber(likeCount).intValue());
            videoInfo.setPlayCount(FormatUtils.formatNumber(playCount));
            videoInfo.setUploadTime(SpiderDateUtils.formatDate(uploadDate).getTime());
            videoInfo.setTitle(title);
            videoInfo.setPlayUrl(playUrl);
            videoInfo.setCrawlTime(System.currentTimeMillis());
            return videoInfo;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(GetVideoInfoParse.INSTANCE.parse(JsoupUtils.getDocument("https://www.youtube.com/watch?v=MUMdOQ1FBGs", true, CommonConstants.HEAD_MAP)));
    }
}
