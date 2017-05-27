# youtube-spider
fetch youtube video info and download video


抓取youtube频道中视频列表中的所有数据，并下载视频。


你需要改一些配置:
1、将代理IP和端口号设置成你自己的
在`youtube.spider.util.CommonConstants`里面做修改
```
public static final String PROXY_IP = "";
public static final int PROXY_PORT = 8888;

```
当然，如果你自己的网直接就能访问youtube，那么你在代码里将HTTP请求的是否启用代理参数设置成false即可。

2、程序的入口类为`youtube.spider.SpiderVideoInfo`
频道页URL在这里更改`String userVideoListUrl = "https://www.youtube.com/user/failarmy/videos";` 
改成你想要进行下载的频道页视频列表，URL规则是*频道主页+/videos*

然后run it 就会进行下载。

整个程序主要解析规则在parse包里。
主要有以下2个类
1. GetVideoUrlsParse
遍历频道页视频列表，返回所有的视频URL.(遍历的时候会有一个filterDate作为过滤条件，这个日期以前的视频不做抓取,如果设置成null，表示抓取所有)

2. GetVideoInfoParse
根据视频url解析视频详细信息，返回封装的VideoInfo对象








