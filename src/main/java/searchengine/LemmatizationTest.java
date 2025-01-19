package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LemmatizationTest {

    private static final Set<String> EXCLUDED_PARTS_OF_SPEECH = new HashSet<>(Arrays.asList(
            "CONJ", "PREP", "PART", "INTJ"  // Список частей речи для исключения
    ));

    public LemmatizationTest() {
    }

    public Map<String, Integer> getLemmasWithFrequency(String text, String language) {
        String[] words = text.split("\\s+");
        Map<String, Integer> lemmaCount = new HashMap<>();
        LuceneMorphology morphology = null;

        try {
            // Выбираем соответствующую морфологию в зависимости от языка
            if ("ru".equals(language)) {
                morphology = new RussianLuceneMorphology();
            } else if ("en".equals(language)) {
                morphology = new EnglishLuceneMorphology();
            }

            for (String word : words) {
                List<String> lemmas = getLemmas(word, morphology);
                for (String lemma : lemmas) {
                    // Проверяем, не является ли лемма частью речи, которую нужно исключить
                    if (!EXCLUDED_PARTS_OF_SPEECH.contains(getPartOfSpeech(word, morphology))) {
                        lemmaCount.put(lemma, lemmaCount.getOrDefault(lemma, 0) + 1);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error initializing morphology: " + e.getMessage());
        }

        return lemmaCount;
    }

    private List<String> getLemmas(String word, LuceneMorphology morphology) {
        try {
            List<String> baseForms = morphology.getMorphInfo(word)
                    .stream()
                    .filter(info -> info.split(",")[1].equals("SING"))  // Пример фильтрации для существительных
                    .map(info -> info.split(",")[0])
                    .distinct()
                    .collect(Collectors.toList());
            return baseForms.isEmpty() ? Collections.singletonList(word) : baseForms;
        } catch (Exception e) {
            return Collections.singletonList(word);
        }
    }

    private String getPartOfSpeech(String word, LuceneMorphology morphology) {
        try {
            List<String> forms = morphology.getMorphInfo(word);
            if (!forms.isEmpty()) {
                return forms.get(0).split(",")[1]; // Извлекаем часть речи
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    public static void main(String[] args) {
        LemmatizationTest lemmatizer = new LemmatizationTest();

        // Пример текста на русском языке
        String russianText = "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.";
        // Пример текста на английском языке
        String englishText = "The repeated appearance of the leopard in Ossetia suggests that the leopard constantly inhabits some regions of the North Caucasus.";

        // Лемматизация текста на русском
        Map<String, Integer> russianLemmaFrequency = lemmatizer.getLemmasWithFrequency(russianText, "ru");
        System.out.println("Russian Lemmas:");
        for (Map.Entry<String, Integer> entry : russianLemmaFrequency.entrySet()) {
            System.out.println(entry.getKey() + " — " + entry.getValue());
        }

        // Лемматизация текста на английском
        Map<String, Integer> englishLemmaFrequency = lemmatizer.getLemmasWithFrequency(englishText, "en");
        System.out.println("\nEnglish Lemmas:");
        for (Map.Entry<String, Integer> entry : englishLemmaFrequency.entrySet()) {
            System.out.println(entry.getKey() + " — " + entry.getValue());
        }
    }
}
