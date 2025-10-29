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

        // tổng tiền từ các Services trong Order
        if (order.getServices() != null && !order.getServices().isEmpty()) {
            for (BE.entity.Service serviceEntity : order.getServices()) {
                InvoiceDetail serviceDetail = new InvoiceDetail();
                serviceDetail.setInvoice(invoice);
                serviceDetail.setItemName("Dịch vụ: " + serviceEntity.getServiceName());
                serviceDetail.setQuantity(1);
                serviceDetail.setUnitPrice(serviceEntity.getPrice() != null ? serviceEntity.getPrice() : 0.0);
                serviceDetail.setSubTotal(serviceDetail.getUnitPrice());

                details.add(serviceDetail);
                totalAmount += serviceDetail.getSubTotal();
            }
        }

        // tổng tiền từ MaintenanceComponents
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
    public InvoiceResponse getInvoiceByOrderId(Long orderId) {
        Maintenance maintenance = maintenanceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Maintenance cho Order ID: " + orderId));

        Invoice invoice = invoiceRepository.findByMaintenance_MaintenanceID(maintenance.getMaintenanceID())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Invoice cho Maintenance ID: " + maintenance.getMaintenanceID()));

        return convertToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoices.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    private InvoiceResponse convertToResponse(Invoice invoice) {
        InvoiceResponse response = modelMapper.map(invoice, InvoiceResponse.class);

        Maintenance maintenance = invoice.getMaintenance();
        if (maintenance != null) {
            response.setMaintenanceId(maintenance.getMaintenanceID());
            response.setStartTime(maintenance.getStartTime());
            response.setEndTime(maintenance.getEndTime());

            Vehicle vehicle = maintenance.getVehicle();
            if (vehicle != null) {
                response.setVehicleLicensePlate(vehicle.getLicensePlate());
                if (vehicle.getModel() != null) {
                    response.setVehicleModel(vehicle.getModel().getModelName());
                }
            }

            Orders order = maintenance.getOrders();
            if (order != null && order.getCustomer() != null) {
                Customer customer = order.getCustomer();
                response.setCustomerName(customer.getName());
                response.setCustomerPhone(customer.getPhone());
                response.setCustomerEmail(customer.getEmail());
            }
        }

        if (invoice.getInvoiceDetails() != null) {
            List<InvoiceDetailResponse> detailResponses = invoice.getInvoiceDetails().stream()
                    .map(detail -> modelMapper.map(detail, InvoiceDetailResponse.class))
                    .collect(Collectors.toList());
            response.setInvoiceDetails(detailResponses);
        } else {
            response.setInvoiceDetails(new ArrayList<>());
        }

        return response;
    }
}