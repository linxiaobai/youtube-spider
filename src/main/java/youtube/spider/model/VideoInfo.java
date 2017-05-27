package youtube.spider.model;


import java.io.Serializable;

/**
 * 抓取的视频信息
 *
 * @author jianlin
 */
public class VideoInfo implements Serializable {
    private static final long serialVersionUID = 6991610393664419966L;

    //视频标题
    private String title;

    //视频播放地址
    private String playUrl;

    //视频播放时长
    private Integer duration;

    //视频播放数
    private Long playCount;

    //视频评论数
    private Integer commentCount;

    //点赞数
    private Integer likeCount;

    //视频上传时间
    private Long uploadTime;

    //抓取时间
    private Long crawlTime;

    //视频描述
    private String description;

    //下载地址
    private String downloadUrl;

    //视频保存路径
    private String savePath;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Long playCount) {
        this.playCount = playCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Long getCrawlTime() {
        return crawlTime;
    }

    public void setCrawlTime(Long crawlTime) {
        this.crawlTime = crawlTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }


    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }


    @Override
    public String toString() {
        return "VideoInfo{" +
                "title='" + title + '\'' +
                ", playUrl='" + playUrl + '\'' +
                ", duration=" + duration +
                ", playCount=" + playCount +
                ", commentCount=" + commentCount +
                ", likeCount=" + likeCount +
                ", uploadTime=" + uploadTime +
                ", crawlTime=" + crawlTime +
                ", description='" + description + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", savePath='" + savePath + '\'' +
                '}';
    }
}



