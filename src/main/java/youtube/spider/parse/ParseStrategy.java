package youtube.spider.parse;

import org.jsoup.nodes.Document;

import java.util.Date;

/**
 * document元素解析策略
 *
 * @author jianlin210349
 */
public interface ParseStrategy<R> {
    R parse(Document document);

    /**
     * 带日期限制解析结果
     *
     * @param document   页面元素
     * @param filterDate 过滤该日期之前的视频数据，不做抓取
     * @return
     */
    R parse(Document document, Date filterDate);
}
