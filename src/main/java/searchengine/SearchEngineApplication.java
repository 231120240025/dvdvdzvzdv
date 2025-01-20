package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import searchengine.services.HtmlFetcher;

@SpringBootApplication
public class SearchEngineApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SearchEngineApplication.class, args);

        HtmlFetcher htmlFetcher = context.getBean(HtmlFetcher.class);
        htmlFetcher.fetchAndPrintHtml();
    }
}

