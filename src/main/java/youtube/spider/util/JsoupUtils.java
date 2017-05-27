/**
 * Copyright (c) 2015 Sohu. All Rights Reserved
 */
package youtube.spider.util;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * jsoup工具类
 *
 * @author jianlin
 */
public class JsoupUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsoupUtils.class);
    //默认请求头信息
    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>();

    //超时时间
    private static final int LONG_TIMEOUT = 20000;
    private static final int SHORT_TIMEOUT = 5000;
    //http重试间隔时间
    private static final int RETRY_INTERVAL_TIME = 400;
    private static final Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(CommonConstants.PROXY_IP, CommonConstants.PROXY_PORT));

    static {
        DEFAULT_HEADERS.put(CommonConstants.HEADER_ACCEPT_NAME, CommonConstants.HEADER_ACCEPT);
        DEFAULT_HEADERS.put(CommonConstants.HEADER_ACCEPT_ENCODING_NAME, CommonConstants.HEADER_ACCEPT_ENCODING);
        DEFAULT_HEADERS.put(CommonConstants.HEADER_USER_AGENT_NAME, CommonConstants.HEADER_USER_AGENT);
        DEFAULT_HEADERS.put(CommonConstants.HEADER_CONNECTION_NAME, CommonConstants.HEADER_CONNECTION);
    }


    private static Connection addHeaders(Connection jsoupConnection, Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            jsoupConnection.headers(headers);
        } else {
            jsoupConnection.headers(DEFAULT_HEADERS);
        }
        return jsoupConnection;
    }

    /**
     * 给http请求头添加指定cookies
     *
     * @param jsoupConnection
     * @param cookies
     * @return
     */
    private static Connection addCookies(Connection jsoupConnection, Map<String, String> cookies) {
        jsoupConnection.cookies(cookies);
        return jsoupConnection;
    }

    private static Connection setProxy(Connection jsoupConnection) {
        jsoupConnection.proxy(proxy);
        return jsoupConnection;
    }


    /**
     * 获取connection对象
     */
    public static Document getDocument(String url, boolean useProxy) {
        try {
            Connection conn = Jsoup.connect(url).ignoreContentType(true);
            conn = addHeaders(conn, null);
            if (useProxy) {
                setProxy(conn);
            }
            // 设置超时时间
            conn.timeout(LONG_TIMEOUT);
            return conn.get();
        } catch (Exception e) {
            logger.error("get document error, url = {}", url);
        }
        return null;
    }

    /**
     * 获取connection对象
     */
    public static Document getDocument(String url, boolean useProxy, Map<String, String> headers) {
        try {
            Connection conn = Jsoup.connect(url).ignoreContentType(true);
            conn = addHeaders(conn, headers);
            if (useProxy) {
                setProxy(conn);
            }
            // 设置超时时间
            conn.timeout(LONG_TIMEOUT);
            return conn.get();
        } catch (Exception e) {
            logger.error("get document error, url = {}", url);
        }
        return null;
    }


    /**
     * 带重试机制的Jsoup Document对象获取
     *
     * @param url        请求url
     * @param retryTimes 重试次数
     * @param useProxy   是否使用代理
     * @return
     */
    public static Document getJsoupDocRobust(String url, int retryTimes, boolean useProxy) {
        Document doc = null;
        for (int i = 1; i <= retryTimes; i++) {
            try {
                Connection conn = Jsoup.connect(url).ignoreContentType(true);
                conn = addHeaders(conn, null).timeout(LONG_TIMEOUT);
                if (useProxy) {
                    setProxy(conn);
                }
                doc = conn.get();
                if (doc != null) {
                    return doc;
                }
                TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL_TIME);
            } catch (IOException e) {
                logger.error("get url {} failed, retry times {}", url, i, e);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.error("get url: {} info failed, return null...", url);
        return doc;
    }

    /**
     * 带重试机制的Jsoup Document对象获取 带自定义请求头信息
     *
     * @param url        请求url
     * @param retryTimes 重试次数
     * @param useProxy   是否使用代理
     * @return
     */
    public static Document getJsoupDocRobust(String url, int retryTimes, boolean useProxy, Map<String, String> headers) {
        Document doc = null;
        for (int i = 1; i <= retryTimes; i++) {
            try {
                Connection conn = Jsoup.connect(url).ignoreContentType(true);
                conn = addHeaders(conn, headers).timeout(LONG_TIMEOUT);
                if (useProxy) {
                    setProxy(conn);
                }
                doc = conn.get();
                if (doc != null) {
                    return doc;
                }
                TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL_TIME);
            } catch (HttpStatusException e) {
                logger.error("get url {} failed, status code: {}, retry times: {}", url, e.getStatusCode(), i);
            } catch (IOException e) {
                logger.error("get url {} failed, retry times {}", url, i, e);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.error("get url: {} info failed, return null...", url);
        return doc;
    }

    /**
     * 带重试机制的Jsoup Document对象获取
     *
     * @param url        请求地址
     * @param retryTimes 重试次数
     * @param useProxy   是否使用代理
     * @param headers    请求头 为null表示使用默认头
     * @param cookies    cookies
     * @return
     */
    public static Document getJsoupDocRobust(String url, int retryTimes, boolean useProxy, Map<String, String> headers, Map<String, String> cookies) {
        Document doc = null;
        for (int i = 1; i <= retryTimes; i++) {
            try {
                Connection conn = Jsoup.connect(url).ignoreContentType(true);
                conn = addHeaders(conn, headers).timeout(LONG_TIMEOUT);
                if (useProxy) {
                    setProxy(conn);
                }
                if (cookies != null && !cookies.isEmpty()) {
                    conn = addCookies(conn, cookies);
                }
                doc = conn.get();
                if (doc != null) {
                    return doc;
                }
                TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL_TIME);
            } catch (IOException e) {
                logger.error("get url {} failed, retry times {}", url, i, e);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.error("get url: {} info failed, return null...", url);
        return doc;
    }

    public static Document getDoc(String url, Map<String, String> cookiesMap) {
        Connection connection = Jsoup.connect(url).timeout(20000);
        if (cookiesMap != null && cookiesMap.size() > 0) {
            connection.cookies(cookiesMap);
        }
        try {
            return connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断是否包含重定向
     *
     * @param url
     * @return
     */
    public static boolean checkIsRedirect(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            connection.timeout(SHORT_TIMEOUT);
            connection.followRedirects(false);//默认是true，也就是连接遵循重定向！设置为false，对重定向的地址进行筛选
            connection = addHeaders(connection, null);
            connection.ignoreContentType(true);
            Connection.Response response = connection.execute();
            if (response.statusCode() == 301 || response.statusCode() == 302) {////重定向地址，位于信息头header中
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }


    /**
     * 获取重定向后的url
     *
     * @param url
     * @return
     */
    public static String getRedirectUrl(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            connection.timeout(SHORT_TIMEOUT);
            connection.followRedirects(false);//默认是true，也就是连接遵循重定向！设置为false，对重定向的地址进行筛选
            connection = addHeaders(connection, null);
            connection.ignoreContentType(true);
            Connection.Response response = connection.execute();
            Map<String, String> headers = response.headers();
            if (response.statusCode() == 301 || response.statusCode() == 302) {//重定向地址，位于信息头header中
                return headers.get("Location");
            } else {
                return url;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return url;
    }

    /**
     * 获取一个Jsoup定义的Document对象，增加失败重连机制以及404监测机制
     *
     * @param url 抓取数据的url
     * @return
     * @throws org.jsoup.HttpStatusException statusCode值为404表示页面不存在了
     */
    public static Document getJsoupDocInclude404Detect(String url, int retryTimes) throws HttpStatusException {
        Document doc = null;
        for (int i = 1; i <= retryTimes; i++) {
            try {
                Connection connection = Jsoup.connect(url).timeout(LONG_TIMEOUT).ignoreContentType(true);
                connection = addHeaders(connection, null);
                doc = connection.get();
                if (doc != null) {
                    return doc;
                }
                TimeUnit.MILLISECONDS.sleep(RETRY_INTERVAL_TIME);
            } catch (HttpStatusException e) {
                throw new HttpStatusException(e.getMessage(), e.getStatusCode(), e.getUrl());
            } catch (IOException e) {
                logger.error("get url {} failed, retry times {}", url, i, e);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.error("get url: {} info failed, return null...", url);
        return doc;
    }


}
