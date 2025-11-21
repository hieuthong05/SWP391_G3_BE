package BE.repository;

import BE.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentLinkId(String paymentLinkId);
    List<Payment> findByInvoice_InvoiceID(Long invoiceID);
}