package BE.service;

import BE.entity.Invoice;
import BE.entity.InvoiceDetail;
import BE.entity.Payment;
import BE.repository.InvoiceRepository;
import BE.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// CÁC IMPORT CHÍNH XÁC CHO PHIÊN BẢN v1
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;


import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final PayOS payOS;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Autowired
    public PaymentService(PayOS payOS, PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.payOS = payOS;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public String createPaymentLink(Long invoiceId) throws Exception {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        Payment newPayment = new Payment();
        newPayment.setInvoice(invoice);
        newPayment.setAmount(invoice.getTotalAmount());
        newPayment.setPaymentMethod("PayOS");
        newPayment.setPaymentStatus("PENDING");
        Payment savedPayment = paymentRepository.save(newPayment);

        long orderCode = savedPayment.getPaymentID();
        String description = "TT hoa don #" + invoice.getInvoiceID();
        String returnUrl = "http://localhost:5173/payment-success";
        String cancelUrl = "http://localhost:5173/payment-cancel";

        List<ItemData> items = new ArrayList<>();
        for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
            String itemName = "Hang muc bao duong/sua chua";

            // Dùng builder để tạo ItemData như trong doc v1
            ItemData item = ItemData.builder()
                    .name(itemName)
                    .quantity(detail.getQuantity())
                    .price((int) detail.getUnitPrice())
                    .build();
            items.add(item);
        }

        // Dùng builder để tạo PaymentData như trong doc v1
        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount((int) savedPayment.getAmount())
                .description(description)
                .items(items)
                .cancelUrl(cancelUrl)
                .returnUrl(returnUrl)
                .build();

        // Gọi PayOS và nhận về CheckoutResponseData
        CheckoutResponseData response = this.payOS.createPaymentLink(paymentData);

        if (response == null) {
            throw new Exception("Lỗi khi tạo link thanh toán từ PayOS, response trả về null.");
        }

        savedPayment.setPaymentLinkId(response.getPaymentLinkId());
        paymentRepository.save(savedPayment);

        return response.getCheckoutUrl();
    }
}