package youtube.spider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.spider.model.VideoInfo;
import youtube.spider.parse.GetVideoInfoParse;
import youtube.spider.parse.GetVideoUrlsParse;
import youtube.spider.util.CommonConstants;
import youtube.spider.util.GetVideoDownloadUrlUtils;
import youtube.spider.util.HttpUtils;
import youtube.spider.util.JsoupUtils;
import youtube.spider.util.SpiderFileUtils;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author jianlin
 */
public class SpiderVideoInfo {
    private static final Logger logger = LoggerFactory.getLogger(SpiderVideoInfo.class);

    public static void main(String[] args) {
        //创建视频下载目录
        String savePath = "/opt/youtube-download/";
        SpiderFileUtils.mkdirs(savePath);
        //出品人视频列表页
        String userVideoListUrl = "https://www.youtube.com/user/failarmy/videos";
        logger.info("spider video start...");
        //过滤日期传null标识抓取所有视频
        Set<String> videoList = GetVideoUrlsParse.INSTANCE.parse(JsoupUtils.getDocument(userVideoListUrl, true, CommonConstants.HEAD_MAP)
                , DateUtils.addDays(new Date(), CommonConstants.FILTER_DATE));
        logger.info("user video list url: {}, filter date: {}, spider video count: {}", userVideoListUrl, DateUtils.addDays(new Date(), CommonConstants.FILTER_DATE), videoList.size());
        for (String s : videoList) {
            String videoUrl = CommonConstants.YOUTUBE_URL + s;
            logger.info("start spider video url: {}", videoUrl);
            try {
                VideoInfo videoInfo = GetVideoInfoParse.INSTANCE.parse(JsoupUtils.getDocument(videoUrl, true, CommonConstants.HEAD_MAP));
                //视频下载地址有有效期，建议用工具类实时获取
                String realTimeDownloadUrl = GetVideoDownloadUrlUtils.getDownloadUrl(videoUrl, CommonConstants.MP4_M_480x360);
                if (StringUtils.isNotBlank(realTimeDownloadUrl)) {
                    videoInfo.setDownloadUrl(realTimeDownloadUrl);
                    logger.info("start download video..");
                    long startTime = System.currentTimeMillis();
                    Map<String, Object> downloadRet = HttpUtils.download(realTimeDownloadUrl, SpiderFileUtils.getVideoSavePath(savePath, getVid(videoUrl)), CommonConstants.USE_PROXY);
                    Object[] logParams = {realTimeDownloadUrl, getVid(videoUrl), downloadRet.get("status"), downloadRet.get("path"), System.currentTimeMillis() - startTime};
                    logger.info("download video end, url:{}, vid:{}, download ret:{}, path:{}, use time:{} ms", logParams);
                }
                logger.info("spider video info:{}", videoInfo);
            } catch (Exception e) {
                logger.error("spider failed, video url:{}", videoUrl);
                continue;
            }
        }
        logger.info("spider video end...");
    }

    private static String getVid(String url) {
        return url.substring(url.indexOf("v=") + 2);
    }
}
