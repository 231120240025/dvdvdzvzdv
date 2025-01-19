package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;
import searchengine.repositories.PageRepository;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IndexingService {

    private final AtomicBoolean indexingInProgress = new AtomicBoolean(false);  // Флаг для проверки, идет ли индексация

    @Autowired
    private SitesList sitesList;  // Внедрение списка сайтов (список конфигурируемых сайтов)

    @Autowired
    private SiteRepository siteRepository;  // Внедрение репозитория для работы с таблицей сайтов

    @Autowired
    private PageRepository pageRepository;  // Внедрение репозитория для работы с таблицей страниц

    // Метод для проверки, идет ли индексация
    public boolean isIndexingInProgress() {
        return indexingInProgress.get();
    }

    // Метод для начала полной индексации
    public void startFullIndexing() {
        if (indexingInProgress.compareAndSet(false, true)) {  // Если индексация не идет, начинаем её
            try {
                System.out.println("Индексация началась...");

                // Проходим по всем сайтам из списка конфигурации
                for (var configSite : sitesList.getSites()) {
                    System.out.println("Индексация сайта: " + configSite.getName() + " (" + configSite.getUrl() + ")");

                    // Преобразуем конфигурационный сайт в модельный объект Site
                    Site site = new Site();
                    site.setUrl(configSite.getUrl());
                    site.setName(configSite.getName());
                    // При необходимости добавьте другие свойства в объект site

                    // Удаляем все страницы, связанные с этим сайтом
                    int deletedPages = pageRepository.deleteBySite(site);  // Удаляет страницы, связанные с данным сайтом
                    int deletedSite = siteRepository.deleteByUrl(site.getUrl());  // Удаляет сам сайт по URL

                    // Выводим информацию о том, сколько записей было удалено
                    System.out.println("Удалено страниц: " + deletedPages);
                    System.out.println("Удалено сайтов: " + deletedSite);

                    // Симуляция процесса индексации для каждого сайта (например, задержка в 2 секунды)
                    Thread.sleep(2000);
                }

                System.out.println("Индексация завершена!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Индексация была прервана");
            } finally {
                indexingInProgress.set(false);  // Завершаем индексацию
            }
        } else {
            System.out.println("Индексация уже в процессе.");  // Если индексация уже идет
        }
    }
}
