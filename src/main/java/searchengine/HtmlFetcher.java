package searchengine;

import jakarta.persistence.*;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.IndexingStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlFetcher {

    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("searchengine"); // Create the EntityManagerFactory
    private static EntityManager entityManager = entityManagerFactory.createEntityManager(); // Create the EntityManager

    private static LuceneMorphology russianMorphology;
    private static LuceneMorphology englishMorphology;

    static {
        try {
            russianMorphology = new RussianLuceneMorphology();
            englishMorphology = new EnglishLuceneMorphology();
        } catch (IOException e) {
            System.err.println("Ошибка инициализации LuceneMorphology: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String[] urls = {
                "https://www.playback.ru",  // URL первого сайта
                "https://www.ipfran.ru"     // URL второго сайта
        };

        for (String url : urls) {
            System.out.println("\n=====================================");
            System.out.println("Начало обработки страницы: " + url);
            System.out.println("=====================================");

            try {
                // Получаем HTML-код страницы
                Document document = Jsoup.connect(url).get();

                // Получаем текст страницы
                String text = document.body().text();

                // Преобразуем текст в леммы
                Map<String, Integer> lemmas = extractLemmas(text);

                // Выводим результат
                System.out.println("\nЛеммы для страницы:");
                System.out.println("-------------------------------------");
                lemmas.forEach((lemma, count) ->
                        System.out.printf("%-20s : %d%n", lemma, count)
                );

                // Получаем или создаем сайт для сохранения страницы
                Site site = getOrCreateSite(url);

                // Создаем объект страницы и заполняем его
                Page page = new Page();
                page.setPath(url);
                page.setContent(document.html()); // Сохраняем весь HTML
                page.setCode(200); // Код ответа, 200 для успешного запроса
                page.setSite(site); // Связываем страницу с сайтом

                // Сохраняем страницу в базе данных
                savePage(page);

                System.out.println("\nДанные страницы успешно сохранены в базе.");
            } catch (IOException e) {
                System.out.println("Ошибка при запросе страницы " + url + ": " + e.getMessage());
            }

            System.out.println("=====================================");
            System.out.println("Обработка страницы завершена: " + url);
            System.out.println("=====================================\n");
        }

        // Закрываем EntityManager после выполнения всех операций
        entityManager.close();
        entityManagerFactory.close();
    }

    private static Site getOrCreateSite(String url) {
        Site site = entityManager.createQuery("SELECT s FROM Site s WHERE s.url = :url", Site.class)
                .setParameter("url", url)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (site == null) {
            site = new Site();
            site.setUrl(url);
            site.setName(url);
            site.setStatus(IndexingStatus.INDEXING);
            site.setStatusTime(LocalDateTime.now());

            entityManager.getTransaction().begin();
            entityManager.persist(site);
            entityManager.getTransaction().commit();
        }

        return site;
    }

    private static void savePage(Page page) {
        entityManager.getTransaction().begin();
        entityManager.persist(page);
        entityManager.getTransaction().commit();
    }

    private static Map<String, Integer> extractLemmas(String text) {
        Map<String, Integer> lemmas = new HashMap<>();
        String[] words = text.toLowerCase().replaceAll("[^а-яa-z ]", "").split("\\s+");

        for (String word : words) {
            if (word.isBlank()) continue;

            try {
                List<String> wordBaseForms;
                if (word.matches("[а-я]+")) {
                    wordBaseForms = russianMorphology.getNormalForms(word);
                } else if (word.matches("[a-z]+")) {
                    wordBaseForms = englishMorphology.getNormalForms(word);
                } else {
                    continue;
                }

                for (String baseForm : wordBaseForms) {
                    lemmas.put(baseForm, lemmas.getOrDefault(baseForm, 0) + 1);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при лемматизации слова \"" + word + "\": " + e.getMessage());
            }
        }

        return lemmas;
    }
}
