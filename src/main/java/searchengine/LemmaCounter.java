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
        text = removeHtmlTags(text);
        text = text.replaceAll("[^a-zA-Zа-яА-ЯёЁ]", " ");
        String[] words = text.split("\\s+");

        for (String word : words) {
            word = word.toLowerCase();
            List<String> morphInfo = morphology.getMorphInfo(word);
            if (!morphInfo.isEmpty()) {
                String lemma = morphInfo.get(0).split("\\|")[0];
                String partOfSpeech = morphInfo.get(0).split("\\|")[1];
                if (isStopWord(partOfSpeech, language)) {
                    continue;
                }
                lemmaCounts.put(lemma, lemmaCounts.getOrDefault(lemma, 0) + 1);
            }
        }

        return lemmaCounts;
    }

    private static boolean isStopWord(String partOfSpeech, String language) {
        if ("ru".equalsIgnoreCase(language)) {
            return partOfSpeech.contains("СОЮЗ") || partOfSpeech.contains("МЕЖД") || partOfSpeech.contains("ПРЕДЛОГ")
                    || partOfSpeech.contains("МС") || partOfSpeech.contains("ЧАСТ") || partOfSpeech.contains("ПР_С")
                    || partOfSpeech.contains("ОКР_ЧАСТ");
        } else if ("en".equalsIgnoreCase(language)) {
            return partOfSpeech.contains("PREP") || partOfSpeech.contains("CONJ") || partOfSpeech.contains("DET");
        }
        return false;
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

    private static String removeHtmlTags(String text) {
        return text.replaceAll("<[^>]*>", "").replaceAll("&[^;]+;", "");
    }

    public static void main(String[] args) {
        String russianText = "<html><body>Пример текста для <b>лемматизации</b>. Текст содержит несколько <i>слов</i> для анализа.</body></html>";
        String englishText = "<html><body>Example text for <b>lemmatization</b>. The text contains several <i>words</i> for analysis.</body></html>";

        try {
            System.out.println("Леммы для русского текста:");
            HashMap<String, Integer> russianLemmaCounts = countLemmas(russianText, "ru");
            for (Map.Entry<String, Integer> entry : russianLemmaCounts.entrySet()) {
                System.out.println("Лемма: " + entry.getKey() + ", Количество: " + entry.getValue());
            }

            System.out.println();
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
