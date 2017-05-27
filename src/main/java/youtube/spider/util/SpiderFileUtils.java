package youtube.spider.util;

import java.io.File;

/**
 * @author jianlin
 */
public class SpiderFileUtils {
    public static boolean mkdirs(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return false;
    }

    /**
     * 获取下载文件保存地址
     *
     * @param baseDir
     * @param vid            视频ID
     * @return
     */
    public static String getVideoSavePath(String baseDir, String vid) {
        StringBuilder path = new StringBuilder();
        path.append(baseDir).append(File.separator).append(vid).append(".%s");
        return path.toString();
    }
}
