package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import searchengine.HtmlFetcher;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final ExecutorService executorService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, ExecutorService executorService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.executorService = executorService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Map<String, Object>> startIndexing() {
        if (indexingService.isIndexingInProgress()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("result", false);
            errorResponse.put("error", "Индексация уже запущена");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Запуск асинхронной индексации
        executorService.submit(indexingService::startFullIndexing);

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("result", true);
        return ResponseEntity.ok(successResponse);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Map<String, Object>> stopIndexing() {
        if (!indexingService.isIndexingInProgress()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("result", false);
            errorResponse.put("error", "Индексация не запущена");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        indexingService.stopIndexing();

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("result", true);
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Map<String, Object>> indexPage(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        // Проверяем, что URL не пустой
        if (url == null || url.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("result", false);
            errorResponse.put("error", "Не указан URL страницы");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Проверяем, что URL соответствует допустимым сайтам
        if (!indexingService.isValidUrl(url)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("result", false);
            errorResponse.put("error", "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Запускаем индексацию страницы
        boolean indexingResult = HtmlFetcher.indexSinglePage(url);

        if (indexingResult) {
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("result", true);
            return ResponseEntity.ok(successResponse);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("result", false);
            errorResponse.put("error", "Ошибка при индексации страницы");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

