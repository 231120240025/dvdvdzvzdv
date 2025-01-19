package searchengine.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IndexingService {

    // Используем AtomicBoolean для отслеживания состояния индексации
    private final AtomicBoolean indexingInProgress = new AtomicBoolean(false);

    // Метод для проверки, выполняется ли индексация
    public boolean isIndexingInProgress() {
        return indexingInProgress.get();
    }

    // Метод для запуска полной индексации
    public void startFullIndexing() {
        // Проверяем, не выполняется ли уже индексация, если нет - начинаем
        if (indexingInProgress.compareAndSet(false, true)) {
            try {
                // Симуляция процесса индексации (например, сбор данных и их индексирование)
                System.out.println("Индексация началась...");
                // Симуляция времени, необходимого для индексации (например, ожидание 5 секунд)
                Thread.sleep(5000); // Пример: 5 секунд
                System.out.println("Индексация завершена!");
            } catch (InterruptedException e) {
                // Обработка прерывания процесса индексации
                Thread.currentThread().interrupt();
                System.err.println("Индексация была прервана");
            } finally {
                // После завершения индексации или при возникновении ошибки сбрасываем флаг
                indexingInProgress.set(false);
            }
        } else {
            // Если индексация уже выполняется, выводим соответствующее сообщение
            System.out.println("Индексация уже в процессе.");
        }
    }
}
