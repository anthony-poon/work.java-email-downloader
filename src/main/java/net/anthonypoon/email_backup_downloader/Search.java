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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author ypoon
 */
public class Search {
    private String url = "";
    private int page = 0;
    private String sort = "date";
    private String order = "1";
    private String type = "search";
    private String search = "";
    private String searchType = "expert";
    private String ref = "";
    private String folders = "";
    private String extraFolders = "";    
    private HttpClient client;
    private HttpContext httpContext;
    private Properties config;
    private Properties saveFile;
    private String saveLocation;
    public Search(Properties config, HttpContext httpContext) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        this.config = config;
        this.saveLocation = config.getProperty("destination") + "/" + "last_run.save";
        saveFile = new Properties();
        try {
            saveFile.load(new FileInputStream(saveLocation));
            page = Integer.valueOf(saveFile.getProperty("page"));
        } catch (FileNotFoundException ex) {
            saveFile.setProperty("page", "0");
            saveFile.store(new FileWriter(saveLocation), null);
        } catch (IOException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.url = config.getProperty("website.search_url");
        this.httpContext = httpContext;
        client = HttpClientFactory.getInstance();
    }
    
    public void setPage(int page) {
        this.page = page;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setFolders(String folders) {
        this.folders = folders;
    }

    public void setExtraFolders(String extraFolders) {
        this.extraFolders = extraFolders;
    }
    
    public void execute() throws IOException, ParseException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        Elements allElements;
        do {
            long startTime = System.currentTimeMillis();
            System.out.println("Getting page " + page + " data");
            HttpPost post = new HttpPost(url);
            List<NameValuePair> postValue = new ArrayList();
            postValue.add(new BasicNameValuePair("page", String.valueOf(page)));
            postValue.add(new BasicNameValuePair("sort", sort));
            postValue.add(new BasicNameValuePair("order", order));
            postValue.add(new BasicNameValuePair("type", type));
            postValue.add(new BasicNameValuePair("search", search));
            postValue.add(new BasicNameValuePair("searchtype", searchType));
            postValue.add(new BasicNameValuePair("ref", ref));
            postValue.add(new BasicNameValuePair("folders", folders));
            postValue.add(new BasicNameValuePair("extra_folders", extraFolders));
            post.setEntity(new UrlEncodedFormEntity(postValue));
            HttpResponse response = client.execute(post, httpContext);
            HttpEntity httpEntity = response.getEntity();
            BufferedReader br = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
            String str;
            String html = "";
            while ((str = br.readLine()) != null) {
                html = html + str;
            }
            Document doc = Jsoup.parse(html);
            //System.out.println(html);
            allElements = doc.getElementsByClass("resultrow new");
            long totalByte = 0;
            for (Element element : allElements) {
                Downloader dl = new Downloader(config, httpContext, element.id());
                Element dateElement = element.getElementsByClass("resultcell date").first();
                String folderPath = config.getProperty("destination") + "/" + dateElement.html().substring(0, 7);
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    folder.mkdir();
                }
                totalByte = totalByte + dl.execute(folderPath + "/" + element.id() + ".eml");
            }
            long endTime = System.currentTimeMillis();
            float secondElapsed = (endTime - startTime) / 1000;
            System.out.println("Download rate: " + allElements.size() / secondElapsed + " email per secound");
            System.out.println("Download rate: " + (totalByte / 1024) / secondElapsed + " KB per secound");
            page = page + 1;
            saveFile.setProperty("page", String.valueOf(page));
            saveFile.store(new FileWriter(saveLocation), null);
        } while (allElements.size() > 0);
        //System.out.println(html);
    }

}
