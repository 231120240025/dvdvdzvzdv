package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IndexingService {

    private final AtomicBoolean indexingInProgress = new AtomicBoolean(false);

    @Autowired
    private SitesList sitesList; // Внедрение зависимости списка сайтов

    public boolean isIndexingInProgress() {
        return indexingInProgress.get();
    }

    public void startFullIndexing() {
        if (indexingInProgress.compareAndSet(false, true)) {
            try {
                // Начинаем индексацию всех сайтов из конфигурации
                System.out.println("Индексация началась...");
                for (var site : sitesList.getSites()) {
                    System.out.println("Индексация сайта: " + site.getName() + " (" + site.getUrl() + ")");
                    // Симуляция индексации для каждого сайта (например, ожидание 2 секунд для каждого сайта)
                    Thread.sleep(2000);
                }
                System.out.println("Индексация завершена!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Индексация была прервана");
            } finally {
                indexingInProgress.set(false);
            }
        } else {
            System.out.println("Индексация уже в процессе.");
        }
    }
}
