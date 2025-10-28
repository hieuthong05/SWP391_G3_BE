package BE.service;

import BE.entity.*;
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

        Orders order = maintenance.getOrders();
        if (order == null) {
            throw new EntityNotFoundException("Không tìm thấy Order liên kết với Maintenance ID: " + maintenanceId);
        }

        Invoice invoice = new Invoice();
        invoice.setMaintenance(maintenance);

        List<InvoiceDetail> details = new ArrayList<>();
        double totalAmount = 0;

        // 1. Tính tổng tiền từ các dịch vụ (Services) trong Order
        if (order.getServices() != null && !order.getServices().isEmpty()) {
            // Đổi tên biến 'service' thành 'serviceEntity' để tránh trùng lặp
            for (BE.entity.Service serviceEntity : order.getServices()) {
                InvoiceDetail serviceDetail = new InvoiceDetail();
                serviceDetail.setInvoice(invoice);
                // Sử dụng serviceEntity ở đây
                serviceDetail.setItemName("Dịch vụ: " + serviceEntity.getServiceName());
                serviceDetail.setQuantity(1);
                serviceDetail.setUnitPrice(serviceEntity.getPrice() != null ? serviceEntity.getPrice() : 0.0);
                serviceDetail.setSubTotal(serviceDetail.getUnitPrice());

                details.add(serviceDetail);
                totalAmount += serviceDetail.getSubTotal();
            }
        }

        // 2. Tính tổng tiền từ các linh kiện đã sử dụng (MaintenanceComponents) - Giữ nguyên
        if (maintenance.getMaintenanceComponents() != null && !maintenance.getMaintenanceComponents().isEmpty()) {
            for (MaintenanceComponent mc : maintenance.getMaintenanceComponents()) {
                if (mc.getComponent() != null && mc.getComponent().getPrice() != null) {
                    InvoiceDetail componentDetail = new InvoiceDetail();
                    componentDetail.setInvoice(invoice);
                    componentDetail.setItemName("Linh kiện: " + mc.getComponent().getName());
                    componentDetail.setQuantity(mc.getQuantity());
                    componentDetail.setUnitPrice(mc.getComponent().getPrice());
                    double subTotal = mc.getQuantity() * mc.getComponent().getPrice();
                    componentDetail.setSubTotal(subTotal);

                    details.add(componentDetail);
                    totalAmount += subTotal;
                }
            }
        }

        invoice.setInvoiceDetails(details);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus("PENDING");

        maintenance.setStatus("Waiting For Payment");
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