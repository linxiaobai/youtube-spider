package youtube.spider.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Description: http工具类
 * </p>
 *
 * @author jianlin210349
 * @Date 2016-06-07
 */
public class HttpUtils {
    private final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static PoolingHttpClientConnectionManager pcm = null;
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int SOCKET_TIMEOUT = 20000;
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    private static final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECTION_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
    private static SSLContext sslContext = null;
    //proxy config
    private static final HttpHost proxy = new HttpHost(CommonConstants.PROXY_IP, CommonConstants.PROXY_PORT);
    private static final DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

    private static enum SchemeType {
        HTTP, HTTPS
    }

    static {
        if (pcm == null) {
            pcm = new PoolingHttpClientConnectionManager();
            pcm.setMaxTotal(640);
            pcm.setDefaultMaxPerRoute(160);
        }
        //ssl config
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }


    private static CloseableHttpClient getHttpClient(SchemeType schemeType) {
        if (schemeType.equals(SchemeType.HTTP)) {
            return HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(pcm).build();
        } else if (schemeType.equals(SchemeType.HTTPS)) {
            return HttpClients.custom().setSslcontext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).setDefaultRequestConfig(requestConfig).setConnectionManager(pcm).build();
        }
        throw new IllegalArgumentException("unknown scheme type:" + schemeType);
    }

    private static CloseableHttpClient getProxyHttpClient(SchemeType schemeType) {
        if (schemeType.equals(SchemeType.HTTP)) {
            return HttpClients.custom().setDefaultRequestConfig(requestConfig).setRoutePlanner(routePlanner).setConnectionManager(pcm).build();
        } else if (schemeType.equals(SchemeType.HTTPS)) {
            return HttpClients.custom().setSslcontext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).setDefaultRequestConfig(requestConfig).setRoutePlanner(routePlanner).setConnectionManager(pcm).build();
        }
        throw new IllegalArgumentException("unknown scheme type:" + schemeType);
    }

    private static SchemeType getSchemeTypeByUrl(String requestUrl) {
        try {
            URL url = new URL(requestUrl);
            if ("https".equals(url.getProtocol())) {
                return SchemeType.HTTPS;
            } else if ("http".equals(url.getProtocol())) {
                return SchemeType.HTTP;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("unknown scheme type, url:" + requestUrl);
    }

    private static CloseableHttpClient getHttpClient(SchemeType schemeType, boolean useProxy) {
        if (useProxy) {
            return getProxyHttpClient(schemeType);
        } else {
            return getHttpClient(schemeType);
        }
    }

    /*============分割线===============*/

    /**
     * post请求 无代理，请求参数以utf8编码
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return
     */
    public static String post(String url, Map<String, String> params) {
        SchemeType schemeType = getSchemeTypeByUrl(url);
        CloseableHttpClient closeableHttpClient = getHttpClient(schemeType);
        HttpPost httpPost = new HttpPost(url);
        buildHeaders(httpPost, null);
        List<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
        if (params != null && !params.isEmpty()) {
            Iterator<Map.Entry<String, String>> iterEntry = params.entrySet().iterator();
            Map.Entry<String, String> entry;
            while (iterEntry.hasNext()) {
                entry = iterEntry.next();
                String key = entry.getKey();
                String value = entry.getValue().toString();
                postParams.add(new BasicNameValuePair(key, value));
            }
        }

        CloseableHttpResponse httpResponse = null;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(postParams, "utf8"));
            httpResponse = closeableHttpClient.execute(httpPost);
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }






    /**
     * 构建请求头
     * @param httpRequestBase 请求对象
     * @param headers 自定义请求头信息  如果为null，则使用默认的请求头信息，默认请求信息
     * @see CommonConstants
     */
    private static void buildHeaders(HttpRequestBase httpRequestBase, Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            for (String s : headers.keySet()) {
                httpRequestBase.setHeader(s, headers.get(s));
            }
        } else {
            httpRequestBase.setHeader(CommonConstants.HEADER_ACCEPT_NAME, CommonConstants.HEADER_ACCEPT);
            //请求头带上接受编码可能会导致返回结果乱码
//        httpRequestBase.setHeader("Accept-Encoding", CommonConstants.HEADER_ACCEPT_ENCODING);
            httpRequestBase.setHeader(CommonConstants.HEADER_USER_AGENT_NAME, CommonConstants.HEADER_USER_AGENT);
            httpRequestBase.setHeader(CommonConstants.HEADER_CONNECTION_NAME, CommonConstants.HEADER_CONNECTION);
        }
    }

    public static String getRobust(String url, int retryTimes) {
        for (int i = 0; i < retryTimes; i++) {
            String ret = get(url);
            if (StringUtils.isNotBlank(ret)) {
                return ret;
            }
            logger.warn("request url {} failed, than retry: {}", url, i);
        }
        logger.error("request url {} failed, retry times {}", url, retryTimes);
        return null;
    }

    public static String getRobust(String url, int retryTimes, boolean useProxy) {
        for (int i = 0; i < retryTimes; i++) {
            String ret = get(url, useProxy);
            if (StringUtils.isNotBlank(ret)) {
                return ret;
            }
            logger.warn("request url {} failed, than retry: {}", url, i);
        }
        logger.error("request url {} failed, retry times {}", url, retryTimes);
        return null;
    }


    /**
     * get请求，带重试机制
     *
     * @param url        请求url
     * @param retryTimes 重试次数
     * @param useProxy   是否启用代理 true 启用 false 不启用
     * @param headers    自定义请求头信息
     * @return
     */
    public static String getRobust(String url, int retryTimes, boolean useProxy, Map<String, String> headers) {
        for (int i = 0; i < retryTimes; i++) {
            String ret = get(url, useProxy, headers);
            if (StringUtils.isNotBlank(ret)) {
                return ret;
            }
            logger.warn("request url {} failed, than retry: {}", url, i);
        }
        logger.error("request url {} failed, retry times {}", url, retryTimes);
        return null;
    }

    /**
     * get请求，带重试机制 不启用代理 带自定义请求头
     *
     * @param url        请求url
     * @param retryTimes 重试次数
     * @param headers    自定义请求头信息
     * @return
     */
    public static String getRobust(String url, int retryTimes, Map<String, String> headers) {
        for (int i = 0; i < retryTimes; i++) {
            String ret = get(url, false, headers);
            if (StringUtils.isNotBlank(ret)) {
                return ret;
            }
            logger.warn("request url {} failed, than retry: {}", url, i);
        }
        logger.error("request url {} failed, retry times {}", url, retryTimes);
        return null;
    }


    /**
     * get请求
     *
     * @param url       请求地址
     * @param charset   返回结果编码
     * @param userProxy 是否启用代理 true 启用 false 不启用
     * @param headers   自定义请求头信息
     * @return
     */
    public static String get(String url, String charset, boolean userProxy, Map<String, String> headers) {
        SchemeType schemeType = getSchemeTypeByUrl(url);
        CloseableHttpClient httpClient = getHttpClient(schemeType, userProxy);
        HttpGet httpGet = new HttpGet(url);
        //如果有自定义头信息，使用自定义的头进行请求
        buildHeaders(httpGet, headers);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(httpResponse.getEntity(), charset);
                return result;
            } else {
                logger.error("execute http get request error, url={}, statusCode={}", url, httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * get请求 默认编码 UTF-8  不启用代理，使用默认头信息
     *
     * @param url 请求地址
     * @return
     */
    public static String get(String url) {
        return get(url, DEFAULT_CHARSET, false, null);
    }

    /**
     * get请求  默认编码 UTF-8 ，使用默认头信息
     *
     * @param url      请求地址
     * @param useProxy 是否启用代理 true 启用 false 不启用
     * @return
     */
    public static String get(String url, boolean useProxy) {
        return get(url, DEFAULT_CHARSET, useProxy, null);
    }

    /**
     * get请求 默认编码 UTF-8 ，使用自定义头信息
     *
     * @param url
     * @param useProxy
     * @param headers
     * @return
     */
    public static String get(String url, boolean useProxy, Map<String, String> headers) {
        return get(url, DEFAULT_CHARSET, useProxy, headers);
    }


    private static String getDownloadFileType(HttpResponse httpResponse) {
        String contentType = httpResponse.getFirstHeader("Content-Type").getValue();
        if (StringUtils.isNotBlank(contentType)) {
            String[] arr = contentType.split("/");
            return arr[arr.length - 1];
        }
        throw new IllegalArgumentException("get download response content type error, content type:" + contentType);
    }

    /**
     * 下载文件
     *
     * @param url      请求地址
     * @param saveFile 文件保存路径
     * @param useProxy 是否启用代理 true 启用 false 不启用
     * @return map    key:status -> 下载成功与否   key:path  -> 下载文件保存路径
     */
    public static Map<String, Object> download(String url, String saveFile, boolean useProxy, Map<String, String> headers) {
        Map<String, Object> ret = new HashMap<String, Object>(2);

        SchemeType schemeType = getSchemeTypeByUrl(url);
        CloseableHttpClient closeableHttpClient = getHttpClient(schemeType, useProxy);
        HttpGet httpGet = new HttpGet(url);
        buildHeaders(httpGet, headers);
        CloseableHttpResponse httpResponse = null;
        InputStream is = null;
        OutputStream os = null;
        File file = null;
        try {
            httpResponse = closeableHttpClient.execute(httpGet);
            String contentType = getDownloadFileType(httpResponse);
            if (!"mp4".equals(contentType)) {
                logger.error("content is not mp4, content type:" + contentType);
                ret.put("status", false);
                return ret;
            }
            byte[] buffer = new byte[2048];
            is = httpResponse.getEntity().getContent();
            file = new File(String.format(saveFile, contentType));
            os = new FileOutputStream(file);

            int n;
            while ((n = is.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
            os.flush();
        } catch (IOException e) {
            logger.error("download error, url: {}, save path:{}, use proxy:{}", url, file.getPath(), useProxy);
            logger.error(e.getMessage(), e);
            ret.put("status", false);
            return ret;
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }

        if (file.exists() && file.length() > 0) {
            ret.put("status", true);
            ret.put("path", file.getPath());
            return ret;
        }
        ret.put("status", false);
        return ret;
    }

    /**
     * 下载文件
     *
     * @param url      请求地址
     * @param saveFile 文件保存路径
     * @param useProxy 是否启用代理 true 启用 false 不启用
     * @return map    key:status -> 下载成功与否   key:path  -> 下载文件保存路径
     */
    public static Map<String, Object> download(String url, String saveFile, boolean useProxy) {
        Map<String, Object> ret = new HashMap<String, Object>(2);

        SchemeType schemeType = getSchemeTypeByUrl(url);
        CloseableHttpClient closeableHttpClient = getHttpClient(schemeType, useProxy);
        HttpGet httpGet = new HttpGet(url);
        buildHeaders(httpGet, null);

        CloseableHttpResponse httpResponse = null;
        InputStream is = null;
        OutputStream os = null;
        File file = null;
        try {
            httpResponse = closeableHttpClient.execute(httpGet);
            String contentType = getDownloadFileType(httpResponse);
            if (!"mp4".equals(contentType)) {
                logger.error("content is not mp4, content type:" + contentType);
                ret.put("status", false);
                return ret;
            }
            byte[] buffer = new byte[2048];
            is = httpResponse.getEntity().getContent();
            file = new File(String.format(saveFile, contentType));
            os = new FileOutputStream(file);

            int n;
            while ((n = is.read(buffer)) != -1) {
                os.write(buffer, 0, n);
            }
            os.flush();
        } catch (IOException e) {
            logger.error("download error, url: {}, save path:{}, use proxy:{}", url, file.getPath(), useProxy);
            logger.error(e.getMessage(), e);
            ret.put("status", false);
            return ret;
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }

        if (file.exists() && file.length() > 0) {
            ret.put("status", true);
            ret.put("path", file.getPath());
            return ret;
        }
        ret.put("status", false);
        return ret;
    }

    public static void main(String[] args) {
        long t = System.currentTimeMillis();
        download("https://redirector.googlevideo.com/videoplayback?mt=1493202809&mv=m&ei=JngAWfDLD8Py8gSjgqqIBQ&id=o-AH3jGoK-GVUHVUFSdyeQ_xVhCz_UemPOU0gaH_baS5FU&ms=au&source=youtube&pl=38&dur=219.149&key=yt6&ip=2001%3A19f0%3A5%3A1de%3A5400%3Aff%3Afe4f%3A2207&sparams=dur%2Cei%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cupn%2Cexpire&mn=sn-ab5l6n6e&initcwndbps=3296250&lmt=1493074827006979&expire=1493224582&mime=video%2Fmp4&requiressl=yes&mm=31&ratebypass=yes&ipbits=0&itag=22&upn=vCV2Z2UAKok&signature=42BEAE81200014795056AB8E8B99F44617335E81.685273EC7A90E376E6E7F7BAEB16956C6915BB3B", "C:\\Users\\jianlin210349\\Desktop\\youtube_download\\nba.mp4", true);
        System.out.println(System.currentTimeMillis() - t);
    }
}
