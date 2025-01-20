package searchengine;

import jakarta.persistence.*;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Index;
import searchengine.model.IndexingStatus;
import java.util.ArrayList;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlFetcher {

    private static final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("searchengine");
    private static final EntityManager entityManager = entityManagerFactory.createEntityManager();

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
                "https://www.playback.ru",
                "https://www.ipfran.ru"
        };

        for (String url : urls) {
            System.out.println("\n=====================================");
            System.out.println("Начало обработки страницы: " + url);
            System.out.println("=====================================");

            try {
                Document document = Jsoup.connect(url).get();
                String text = document.body().text();
                Map<String, Integer> lemmas = extractLemmas(text);

                System.out.println("\nЛеммы для страницы:");
                System.out.println("-------------------------------------");
                lemmas.forEach((lemma, count) ->
                        System.out.printf("%-20s : %d%n", lemma, count)
                );

                Site site = getOrCreateSite(url);

                Page page = new Page();
                page.setPath(url);
                page.setContent(document.html());
                page.setCode(200);
                page.setSite(site);

                savePage(page);
                saveLemmasAndIndexes(page, lemmas);

                System.out.println("\nДанные страницы успешно сохранены в базе.");
            } catch (IOException e) {
                System.out.println("Ошибка при запросе страницы " + url + ": " + e.getMessage());
            }

            System.out.println("=====================================");
            System.out.println("Обработка страницы завершена: " + url);
            System.out.println("=====================================\n");
        }

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

    private static void saveLemmasAndIndexes(Page page, Map<String, Integer> lemmas) {
        entityManager.getTransaction().begin();

        Map<String, Lemma> lemmaCache = new HashMap<>();

        // Получаем существующие леммы
        List<Lemma> existingLemmas = entityManager.createQuery(
                        "SELECT l FROM Lemma l WHERE l.lemma IN :lemmas AND l.site = :site", Lemma.class)
                .setParameter("lemmas", lemmas.keySet())
                .setParameter("site", page.getSite())
                .getResultList();

        for (Lemma lemma : existingLemmas) {
            lemmaCache.put(lemma.getLemma(), lemma);
        }

        int batchCounter = 0;
        List<Index> indexesToSave = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
            String lemmaText = entry.getKey();
            int count = entry.getValue();

            Lemma lemma = lemmaCache.getOrDefault(lemmaText, null);

            if (lemma == null) {
                lemma = new Lemma();
                lemma.setLemma(lemmaText);
                lemma.setFrequency(1);
                lemma.setSite(page.getSite());
                entityManager.persist(lemma);
                lemmaCache.put(lemmaText, lemma);
            } else {
                lemma.setFrequency(lemma.getFrequency() + 1);
                entityManager.merge(lemma);
            }

            Index index = new Index();
            index.setPage(page);
            index.setLemma(lemma);
            index.setRank((float) count);
            indexesToSave.add(index);

            // Каждые 30 записей отправляем в базу
            batchCounter++;
            if (batchCounter % 30 == 0) {
                for (Index idx : indexesToSave) {
                    entityManager.persist(idx);
                }
                entityManager.flush();
                entityManager.clear();
                indexesToSave.clear();
            }
        }

        // Оставшиеся индексы
        for (Index idx : indexesToSave) {
            entityManager.persist(idx);
        }

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
