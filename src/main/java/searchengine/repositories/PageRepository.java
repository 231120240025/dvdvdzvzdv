package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

@Repository
public interface PageRepository extends CrudRepository<Page, Long> {

    // Аннотируем метод как транзакционный и модифицирующий данные
    @Transactional
    @Modifying
    // SQL запрос для удаления всех страниц, связанных с данным сайтом
    @Query("DELETE FROM Page p WHERE p.site = :site")
    int deleteBySite(Site site);  // Удаляет страницы, связанные с указанным сайтом

}
