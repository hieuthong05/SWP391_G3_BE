package BE.repository;

import BE.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckListRepository extends JpaRepository<CheckList, Long> {


     // Tìm checklist theo type

    List<CheckList> findByCheckListType(String checkListType);


     // Tìm checklist active

    List<CheckList> findByIsActiveTrue();


     // Tìm checklist theo type và active

    @Query("SELECT c FROM CheckList c WHERE c.checkListType = :type AND c.isActive = true")
    List<CheckList> findActiveByType(@Param("type") String type);


     // Tìm checklist theo name (like search)

    @Query("SELECT c FROM CheckList c WHERE c.checkListName LIKE %:name% AND c.isActive = true")
    List<CheckList> searchByName(@Param("name") String name);


     // Check checklist name đã tồn tại chưa

    boolean existsByCheckListNameAndIsActiveTrue(String checkListName);
}