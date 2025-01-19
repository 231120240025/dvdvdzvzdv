package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LemmaCounter {

    public static HashMap<String, Integer> countLemmas(String text, String language) throws IOException {
        LuceneMorphology morphology = getLuceneMorphology(language);
        HashMap<String, Integer> lemmaCounts = new HashMap<>();

        // Убираем все символы, которые не являются буквами
        text = text.replaceAll("[^a-zA-Zа-яА-ЯёЁ]", " ");

        // Разбиваем текст на слова
        String[] words = text.split("\\s+");

        for (String word : words) {
            // Приводим слово к нижнему регистру
            word = word.toLowerCase();

            // Получаем лемму для каждого слова
            List<String> lemmas = morphology.getMorphInfo(word);
            if (!lemmas.isEmpty()) {
                String lemma = lemmas.get(0).split("\\|")[0]; // Первое значение — это лемма
                lemmaCounts.put(lemma, lemmaCounts.getOrDefault(lemma, 0) + 1);
            }
        }

        return lemmaCounts;
    }

    private static LuceneMorphology getLuceneMorphology(String language) throws IOException {
        if ("ru".equalsIgnoreCase(language)) {
            return new RussianLuceneMorphology();
        } else if ("en".equalsIgnoreCase(language)) {
            return new EnglishLuceneMorphology();
        } else {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }

    public static void main(String[] args) {
        // Русский текст
        String russianText = "Пример текста для лемматизации. Текст содержит несколько слов для анализа.";
        // Английский текст
        String englishText = "Example text for lemmatization. The text contains several words for analysis.";

        try {
            // Обрабатываем русский текст
            System.out.println("Леммы для русского текста:");
            HashMap<String, Integer> russianLemmaCounts = countLemmas(russianText, "ru");
            for (Map.Entry<String, Integer> entry : russianLemmaCounts.entrySet()) {
                System.out.println("Лемма: " + entry.getKey() + ", Количество: " + entry.getValue());
            }

            System.out.println(); // Пустая строка для разделения

            // Обрабатываем английский текст
            System.out.println("Lemmas for English text:");
            HashMap<String, Integer> englishLemmaCounts = countLemmas(englishText, "en");
            for (Map.Entry<String, Integer> entry : englishLemmaCounts.entrySet()) {
                System.out.println("Lemma: " + entry.getKey() + ", Count: " + entry.getValue());
            }

        } catch (IOException e) {
            System.err.println("Ошибка при обработке текста: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
