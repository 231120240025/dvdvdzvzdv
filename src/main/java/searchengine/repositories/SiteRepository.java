package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;

@Repository
public interface SiteRepository extends CrudRepository<Site, Long> {

    // Аннотируем метод как транзакционный и модифицирующий данные
    @Transactional
    @Modifying
    // SQL запрос для удаления сайта по его URL
    @Query("DELETE FROM Site s WHERE s.url = :url")
    int deleteByUrl(String url);  // Удаляет сайт по указанному URL

}
