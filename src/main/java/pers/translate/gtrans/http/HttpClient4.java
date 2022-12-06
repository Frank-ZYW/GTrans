package pers.translate.gtrans.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HttpClient based on apache HttpClient 4.5
 */
public class HttpClient4 {

    private static final int DEFAULT_POOL_MAX_CONNECTION = 200;
    private static final int DEFAULT_POOL_MAX_PER_ROUTE = 200;

    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
    private static final int DEFAULT_REQUEST_TIMEOUT = 500;
    private static final int DEFAULT_SOCKET_TIMEOUT = 10000;

    private final CloseableHttpClient httpClient;
    private final IdleConnectionMonitorThread idleThread;

    /**
     * Default constructor
     */
    public HttpClient4() {
        this(
                HttpClient4.DEFAULT_POOL_MAX_CONNECTION,
                HttpClient4.DEFAULT_POOL_MAX_PER_ROUTE,
                HttpClient4.DEFAULT_CONNECTION_TIMEOUT,
                HttpClient4.DEFAULT_REQUEST_TIMEOUT,
                HttpClient4.DEFAULT_SOCKET_TIMEOUT
        );
    }

    /**
     * constructor
     * @param poolMaxConnection Maximum number of connections
     * @param poolMaxPerRoute Maximum number of connections per route
     * @param connectionTimeout TCP connection timeout
     * @param requestTimeout Timeout to get a connection from the pool
     * @param socketTimeout Timeout between any two neighboring packets
     */
    public HttpClient4(
            int poolMaxConnection,
            int poolMaxPerRoute,
            int connectionTimeout,
            int requestTimeout,
            int socketTimeout
    ){
        // build connection pool
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();

        PoolingHttpClientConnectionManager connectionManger = new PoolingHttpClientConnectionManager(registry);
        connectionManger.setMaxTotal(poolMaxConnection);
        connectionManger.setDefaultMaxPerRoute(poolMaxPerRoute);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .build();

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        this.httpClient = httpClientBuilder
                .setConnectionManager(connectionManger)
                .setDefaultRequestConfig(requestConfig)
                .build();

        // start monitor
        this.idleThread = new IdleConnectionMonitorThread(connectionManger);
        this.idleThread.start();
    }

    /**
     * Get Request with default header and no params
     * @param url url
     * @return Response with type String
     */
    public String doGet(String url) throws IOException {
        return this.doGet(url, null, null);
    }

    /**
     * Get Request with default header
     * @param url url
     * @param params params with type ParamPairList
     * @return Response with type String
     */
    public String doGet(String url, ParamPairList params) throws IOException {
        return this.doGet(url, null, params);
    }

    /**
     * Get Request with url params
     * @param url url
     * @param headers headers with type Map
     * @param params params with type ParamPairList
     * @return Response with type String
     */
    public String doGet(String url, Map<String, String> headers, ParamPairList params) throws IOException {

        HttpGet httpGet = new HttpGet(getUrlWithParams(url, params));
        // set header
        if ( headers != null )
            for (Map.Entry<String, String> entry : headers.entrySet())
                httpGet.addHeader(entry.getKey(), entry.getValue());

        // get response
        CloseableHttpResponse response = httpClient.execute(httpGet);
        return parseResponse(response);
    }

    /**
     * Post Request with no data
     * @param url url
     * @return Response with type String
     */
    public String doPost(String url) throws IOException {
        return this.doPost(url, null, null, null);
    }

    /**
     * Post Request with default header
     * @param url url
     * @param data post data
     * @return Response with type String
     */
    public String doPost(String url, ParamPairList data) throws IOException {
        return this.doPost(url, null, null, data);
    }

    /**
     * Post Request
     * @param url url
     * @param headers headers
     * @param data post data
     * @return Response with type String
     */
    public String doPost(String url, Map<String, String> headers, ParamPairList data) throws IOException {
        return this.doPost(url, headers, null, data);
    }

    /**
     * Post Request with url params
     * @param url url
     * @param headers headers
     * @param params url params
     * @param data post date
     * @return Response with type String
     */
    public String doPost(String url, Map<String, String> headers, ParamPairList params, ParamPairList data)
            throws IOException {

        HttpPost httpPost = new HttpPost(getUrlWithParams(url, params));
        // set headers
        if (headers != null)
            for (Map.Entry<String, String> entry : headers.entrySet())
                httpPost.addHeader(entry.getKey(), entry.getValue());
        // set data
        if (data != null)
            httpPost.setEntity(getUrlEncodedFormEntity(data));

        // get response
        CloseableHttpResponse response = httpClient.execute(httpPost);
        return parseResponse(response);
    }

    /**
     * parse response
     * @param response response
     * @return response to str
     * @throws IOException parse error
     */
    private String parseResponse(CloseableHttpResponse response) throws IOException {
        try {
            if (response == null || response.getStatusLine() == null)
                return null;

            // check Http Code
            int statusCode = response.getStatusLine().getStatusCode();
            if ( statusCode == HttpStatus.SC_OK ) {
                HttpEntity entityRes = response.getEntity();
                if (entityRes != null)
                    return EntityUtils.toString(entityRes, "UTF-8");
            }
        } finally {
            if ( response != null ) {
                try {
                    response.close();
                } catch (IOException ignored) {}
            }
        }
        return null;
    }

    /**
     * build data of post request with 'x-www-form-urlencoded'
     * @param data Map data
     * @return HttpEntity result
     */
    private HttpEntity getUrlEncodedFormEntity(ParamPairList data) {
        return new UrlEncodedFormEntity(data.getPairList(), StandardCharsets.UTF_8);
    }

    /**
     * build url with params
     * @param url url
     * @param params params map
     * @return (string) url with params
     */
    public String getUrlWithParams(String url, ParamPairList params) {
        if (params == null)
            return url;
        String finalUrl = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            for (NameValuePair pair : params.getPairList()) {
                String value = pair.getValue();
                value = URLEncoder.encode(value, "UTF-8");
                uriBuilder.addParameter(pair.getName(), value);
            }
            finalUrl = uriBuilder.build().toString();
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            e.printStackTrace();
        }
        return finalUrl;
    }

    /**
     * client shutdown
     */
    public void shutdown() {
        this.idleThread.shutdown();
    }

    /**
     * Monitor abnormal links
     */
    private static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;
        private volatile boolean exitFlag = false;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            this.connMgr = connMgr;
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!this.exitFlag) {
                synchronized (this) {
                    try {
                        this.wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // release unavailable connection
                connMgr.closeExpiredConnections();
                // Close inactive connections within 30 seconds
                connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
            }
        }

        /**
         * monitor shutdown
         */
        public void shutdown() {
            this.exitFlag = true;
            synchronized (this) {
                notify();
            }
        }
    }
}
