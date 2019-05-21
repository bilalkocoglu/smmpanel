package com.thelastcodebenders.follower.ceo;

import com.redfin.sitemapgenerator.ChangeFreq;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Date;

@Service
public final class SitemapService {
    private static final String BASE_URL = "https://selfprestige.net";

    public String createSitemap() throws MalformedURLException {
        WebSitemapGenerator sitemap = new WebSitemapGenerator(BASE_URL);

        WebSitemapUrl url = new WebSitemapUrl.Options(BASE_URL)
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();

        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/all-packages")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/package/instagram-paketleri")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/package/facebook-paketleri")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/fiyat-listesi")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/login")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/order/status")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/registration")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/iade-ve-iptal")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/kullanim-kosullari")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/package/youtube-paketleri")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/package/twitter-paketleri")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        url = new WebSitemapUrl.Options(BASE_URL + "/forgot-password")
                .lastMod(new Date())
                .priority(1.0)
                .changeFreq(ChangeFreq.DAILY).build();
        sitemap.addUrl(url);

        return String.join("", sitemap.writeAsStrings());
    }
}
