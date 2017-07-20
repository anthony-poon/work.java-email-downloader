# work.java-email-downloader
This is a program I wrote during work to download all the email from a email portal provided by our email host. Currently we can only access our email backup via a web application, and they do not provide an offline copy. To maintain an offline copy, I will have to download each of them one by one in .eml format.

- The program first submit a POST to **search.php** to obtain the list of email. Search is limited to a single day because the maximum number of email that I can obtain is 5000 email. Each query can only display 50 result. If there is more than 5000 email, there is no way I can visit all the email.
- After submitting the POST with the date and page number, the result will be parsed by Jsoup. (Yes, the ajax will return a html instead of JSON. IKR)
- Jsoup will return the id. The id will be used to do another POST to **index.php?route=message/download&id=**.
- The downloader will read the response body and save the file into .eml format and sperate them into folder using the email sent date (YYYY.MM)
- The downloader will also read the sent date from the email header and change the last modified time of the file.

Note:
- On first run, if there is no config.properties file in the same folder of the .jar, the program will write a config template into the folder. Users are expected to fill in the required configuration.
- If no username or password is specified in the config.properties, it will ask during runtime.
