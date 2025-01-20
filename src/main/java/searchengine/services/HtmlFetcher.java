package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;

import java.io.IOException;

@Service
public class HtmlFetcher {

    private final SitesList sitesList;

    @Autowired
    public HtmlFetcher(SitesList sitesList) {
        this.sitesList = sitesList;
    }

    public void fetchAndPrintHtml() {
        if (sitesList.getSites() == null || sitesList.getSites().isEmpty()) {
            System.out.println("Список сайтов в конфигурации пуст.");
            return;
        }

        for (var site : sitesList.getSites()) {
            try {
                System.out.println("Загрузка HTML для сайта: " + site.getUrl());
                Document document = Jsoup.connect(site.getUrl()).get();
                System.out.println("HTML для " + site.getUrl() + ":");
                System.out.println(document.html());
            } catch (IOException e) {
                System.err.println("Не удалось загрузить HTML для сайта: " + site.getUrl());
                e.printStackTrace();
            }
        }
    }
}
