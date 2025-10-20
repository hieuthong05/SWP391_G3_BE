package BE.service;

import BE.entity.Invoice;
import BE.entity.InvoiceDetail;
import BE.entity.Maintenance;
import BE.entity.MaintenanceComponent;
import BE.model.response.InvoiceDetailResponse;
import BE.model.response.InvoiceResponse;
import BE.repository.InvoiceRepository;
import BE.repository.MaintenanceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository, MaintenanceRepository maintenanceRepository, ModelMapper modelMapper) {
        this.invoiceRepository = invoiceRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public InvoiceResponse createInvoice(Long maintenanceId) {
        if (invoiceRepository.existsByMaintenance_MaintenanceID(maintenanceId)) {
            throw new IllegalArgumentException("Hóa đơn cho phiên bảo dưỡng này đã tồn tại.");
        }

        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy phiên bảo dưỡng với ID: " + maintenanceId));

        Invoice invoice = new Invoice();
        invoice.setMaintenance(maintenance);

        List<InvoiceDetail> details = new ArrayList<>();
        double totalAmount = 0;

        for (MaintenanceComponent mc : maintenance.getMaintenanceComponents()) {
            InvoiceDetail detail = new InvoiceDetail();
            detail.setInvoice(invoice);
            detail.setItemName(mc.getComponent().getName());
            detail.setQuantity(mc.getQuantity());
            detail.setUnitPrice(mc.getComponent().getPrice());


            double subTotal = mc.getQuantity() * mc.getComponent().getPrice();
            detail.setSubTotal(subTotal);

            details.add(detail);
            totalAmount += subTotal;
        }

        invoice.setInvoiceDetails(details);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus("PENDING");

        maintenance.setStatus("Completed");
        maintenanceRepository.save(maintenance);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return convertToResponse(savedInvoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));
        return convertToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByMaintenanceId(Long maintenanceId) {
        Invoice invoice = invoiceRepository.findByMaintenance_MaintenanceID(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hóa đơn cho phiên bảo dưỡng ID: " + maintenanceId));
        return convertToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();

        // Sử dụng lại hàm convertToResponse để chuyển đổi danh sách
        return invoices.stream()
                .map(this::convertToResponse) // 'this::convertToResponse' tương đương 'invoice -> convertToResponse(invoice)'
                .collect(Collectors.toList());
    }

    // Hàm helper private để chuyển đổi từ Entity sang DTO
    private InvoiceResponse convertToResponse(Invoice invoice) {
        InvoiceResponse response = modelMapper.map(invoice, InvoiceResponse.class);
        response.setMaintenanceId(invoice.getMaintenance().getMaintenanceID());

        List<InvoiceDetailResponse> detailResponses = invoice.getInvoiceDetails().stream()
                .map(detail -> modelMapper.map(detail, InvoiceDetailResponse.class))
                .collect(Collectors.toList());

        response.setInvoiceDetails(detailResponses);
        return response;
    }
}