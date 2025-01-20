package searchengine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class HtmlFetcher {
    public static void main(String[] args) {
        String[] urls = {
                "https://www.playback.ru",  // URL первого сайта
                "https://www.ipfran.ru"     // URL второго сайта
        };

        for (String url : urls) {
            try {
                // Получаем HTML-код страницы
                Document document = Jsoup.connect(url).get();
                // Выводим HTML-код
                System.out.println("HTML код страницы " + url + ":");
                System.out.println(document.html());  // Выводит весь HTML
            } catch (IOException e) {
                System.out.println("Ошибка при запросе страницы " + url + ": " + e.getMessage());
            }
        }
    }
}
