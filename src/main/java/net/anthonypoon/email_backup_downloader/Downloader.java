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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author ypoon
 */
public class Downloader {
    private String url;
    private String prefix;
    private HttpContext httpContext;
    private HttpClient httpClient;
    private String emailId;
    private Properties config;
    public Downloader(Properties config, HttpContext httpContext, String elementId) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        this.config = config;
        Pattern p = Pattern.compile("e_(\\d+)");
        Matcher m = p.matcher(elementId);
        if (m.matches()) {
            emailId = m.group(1);
        } else {
            throw new RuntimeException("Unexpected email ID:" + elementId);
        }
        this.url = config.getProperty("website.download_url") + emailId;
        this.prefix = url;
        this.httpContext = httpContext;
        httpClient = HttpClientFactory.getInstance();
    }
    
    public long execute(String filePath) throws IOException, ParseException {
        HttpGet get = new HttpGet(url);
        HttpResponse downloadResponse = httpClient.execute(get, httpContext);
        HttpEntity httpEntity = downloadResponse.getEntity();
        PrintStream out = new PrintStream(new File(filePath));
        BufferedReader br = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
        File file = new File(filePath);
        String str;
        Date lastModify = null;
        Pattern p = Pattern.compile("^Date:(.*)");
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");
        while ((str = br.readLine()) != null) {
            Matcher m = p.matcher(str);
            if (lastModify == null && m.find()) {
                lastModify = df.parse(m.group(1).trim());
            }
            out.println(str);
            
        }        
        out.close();
        if (lastModify != null) {
            file.setLastModified(lastModify.getTime());
        }
        return file.length();
    }
}
