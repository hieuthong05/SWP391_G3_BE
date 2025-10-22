package BE.repository;

import BE.entity.QuotationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotationDetailRepository extends JpaRepository<QuotationDetail, Long> {
}