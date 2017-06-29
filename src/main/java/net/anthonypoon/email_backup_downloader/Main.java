package net.anthonypoon.email_backup_downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author ypoon
 */
public class Main {
    private static Properties config = new Properties();
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class.getName());
        
        try {        
            File configFile = new File("config.properties");
            if (!configFile.exists()) {
                InputStream template = Main.class.getResourceAsStream("/config.dist.properties");
                PrintWriter writer = new PrintWriter("config.properties");
                BufferedReader br = new BufferedReader(new InputStreamReader(template));
                String str;
                while ((str = br.readLine()) != null) {
                    writer.println(str);
                }
                writer.close();
            }
            config.load(new FileInputStream("config.properties"));
            FileHandler handler = new FileHandler(config.getProperty("destination") + "/error.log"); 
            logger.addHandler(handler);
            HttpClientFactory.setRetryCount(Integer.valueOf(config.getProperty("retry_count")));
            HttpClientFactory.setTimeout(Integer.valueOf(config.getProperty("timeout")));
            HttpContext httpContext = login();
            Search defaultSearch = new Search(config, httpContext);
            defaultSearch.execute();      
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | IOException | ParseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static HttpContext login() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnsupportedEncodingException, IOException {
        HttpClient httpclient = HttpClientFactory.getInstance();
        HttpPost loginPost = new HttpPost(config.getProperty("website.login_url"));
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        List<NameValuePair> postValue = new ArrayList();
        postValue.add(new BasicNameValuePair("username", config.getProperty("username")));
        postValue.add(new BasicNameValuePair("password", config.getProperty("password")));
        loginPost.setEntity(new UrlEncodedFormEntity(postValue));
        HttpResponse loginResponse = httpclient.execute(loginPost, httpContext);
        HttpEntity httpEntity = loginResponse.getEntity();
        BufferedReader br = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
        String str;
        String html = "";
        while ((str = br.readLine()) != null) {
            html = html + str;
        }
        return httpContext;
    }
}
