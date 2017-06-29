/*
 * Copyright 2017 ypoon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.anthonypoon.email_backup_downloader;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;

/**
 *
 * @author ypoon
 */
public class HttpClientFactory {
    private static int retryCount = 5;
    private static int timeout = 5000;
    public static HttpClient getInstance() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        // Set connection timeout
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build();
        
        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                System.err.println("Retry count: " + executionCount);
                //System.err.println(exception.getMessage());
                if (retryCount != -1 && executionCount > retryCount) {
                    System.err.println("Exceeded maximum retry count. Aborting.");
                    return false;
                }
                return true;
            }
        };
        SSLContextBuilder contextBuilder = SSLContextBuilder.create();
        contextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());
        SSLContext context = contextBuilder.build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(context,
                new DefaultHostnameVerifier());
        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(config)
                .setRetryHandler(retryHandler);
        return clientBuilder.build();
    }
    
    public static void setTimeout(int timeout) {
        HttpClientFactory.timeout = timeout;
    }
    
    public static void setRetryCount(int count) {
        HttpClientFactory.retryCount = count;
    }
}
