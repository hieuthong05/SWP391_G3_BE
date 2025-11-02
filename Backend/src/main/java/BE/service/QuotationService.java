package BE.service;

import BE.entity.*;
import BE.model.response.ChecklistItemStatusResponse;
import BE.model.response.MaintenanceComponentResponse;
import BE.model.response.QuotationDetailResponse;
import BE.model.response.QuotationResponse;
import BE.repository.MaintenanceRepository;
import BE.repository.OrdersRepository;
import BE.repository.QuotationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final OrdersRepository ordersRepository;
    private final ModelMapper modelMapper;
    private final QuotationService self;
    @Autowired
    public QuotationService(
            QuotationRepository quotationRepository,
            MaintenanceRepository maintenanceRepository,
            OrdersRepository ordersRepository,
            ModelMapper modelMapper,
            @Lazy QuotationService self
    ) {
        this.quotationRepository = quotationRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.ordersRepository = ordersRepository;
        this.modelMapper = modelMapper;
        this.self = self;
    }

    @Transactional
    public QuotationResponse createQuotation(Long maintenanceId) {
        if (quotationRepository.findByMaintenance_MaintenanceID(maintenanceId).isPresent()) {
            throw new IllegalArgumentException("Quotation for this maintenance already exists.");
        }

        //Tìm maintenance
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance not found with ID: " + maintenanceId));

        Quotation quotation = new Quotation();
        quotation.setMaintenance(maintenance);
        quotation.setStatus("PENDING");


        //Lưu những linh kiện cần bảo dưỡng
        List<QuotationDetail> details = new ArrayList<>();
        double totalAmount = 0;

        for (MaintenanceComponent mc : maintenance.getMaintenanceComponents()) {
            QuotationDetail detail = new QuotationDetail();
            detail.setQuotation(quotation);
            detail.setItemName(mc.getComponent().getName());
            detail.setQuantity(mc.getQuantity());
            detail.setUnitPrice(mc.getComponent().getPrice());
            double subTotal = mc.getQuantity() * mc.getComponent().getPrice();
            detail.setSubTotal(subTotal);
            details.add(detail);
            totalAmount += subTotal;
        }

        quotation.setQuotationDetails(details);
        quotation.setTotalAmount(totalAmount);

        //update maintenance
        maintenance.setStatus("AWAITING_CUSTOMER_APPROVAL");
        maintenanceRepository.save(maintenance);

        //update order
        Orders order = maintenance.getOrders();
        if (order != null) {
            order.setStatus("AWAITING_CUSTOMER_APPROVAL");
            ordersRepository.save(order);
        }

        Quotation savedQuotation = quotationRepository.save(quotation);
        return convertToResponse(savedQuotation);
    }

    @Transactional(readOnly = true)
    public QuotationResponse getQuotationById(Long quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found with ID: " + quotationId));
        return convertToResponse(quotation);
    }

    @Transactional(readOnly = true)
    public QuotationResponse getQuotationByMaintenanceId(Long maintenanceId) {
        Quotation quotation = quotationRepository.findByMaintenance_MaintenanceID(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found for Maintenance ID: " + maintenanceId));
        return convertToResponse(quotation);
    }

    @Transactional(readOnly = true)
    public QuotationResponse getQuotationByOrderId(Long orderId) {
        Maintenance maintenance = maintenanceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance not found for Order ID: " + orderId));

        Quotation quotation = quotationRepository.findByMaintenance_MaintenanceID(maintenance.getMaintenanceID())
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found for Maintenance ID: " + maintenance.getMaintenanceID() + " (linked to Order ID: " + orderId + ")"));

        return convertToResponse(quotation);
    }


    @Transactional
    public QuotationResponse updateQuotationByMaintenanceId(Long maintenanceId) {
        Quotation quotation = quotationRepository.findByMaintenance_MaintenanceID(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found for Maintenance ID: " + maintenanceId));

        return self.recalculateAndUpdateQuotation(quotation);
    }

    @Transactional
    public void confirmQuotation(Long quotationId, boolean approved) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found with ID: " + quotationId));

        Maintenance maintenance = quotation.getMaintenance();
        Orders order = maintenance.getOrders();

        if (approved) {
            quotation.setStatus("APPROVED");
            maintenance.setStatus("APPROVED");
            order.setStatus("PROCESSING");

        } else {
            quotation.setStatus("REJECTED");
            maintenance.setStatus("CANCELLED");
            order.setStatus("CANCELLED");
        }


        quotationRepository.save(quotation);
        maintenanceRepository.save(maintenance);
        ordersRepository.save(order);

    }

    @Transactional(readOnly = true)
    public List<QuotationResponse> getAllQuotation() {
        List<Quotation> quotations = quotationRepository.findAll();
        return quotations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuotationResponse recalculateAndUpdateQuotation(Quotation quotation) {
        Maintenance maintenance = quotation.getMaintenance();

        List<QuotationDetail> newDetails = new ArrayList<>();
        double newTotalAmount = 0;

        if (maintenance.getMaintenanceComponents() != null && !maintenance.getMaintenanceComponents().isEmpty()) {
            for (MaintenanceComponent mc : maintenance.getMaintenanceComponents()) {
                Component component = mc.getComponent();

                if (component != null && component.getPrice() != null) {
                    QuotationDetail detail = new QuotationDetail();
                    detail.setQuotation(quotation);
                    detail.setItemName(component.getName());
                    detail.setQuantity(mc.getQuantity());
                    detail.setUnitPrice(component.getPrice());
                    double subTotal = mc.getQuantity() * component.getPrice();
                    detail.setSubTotal(subTotal);

                    newDetails.add(detail);
                    newTotalAmount += subTotal;
                }
            }
        }

        quotation.getQuotationDetails().clear();
        quotation.getQuotationDetails().addAll(newDetails);
        quotation.setTotalAmount(newTotalAmount);

        Quotation savedQuotation = quotationRepository.save(quotation);

        return convertToResponse(savedQuotation);
    }

    private QuotationResponse convertToResponse(Quotation quotation) {
        // các trường cơ bản (quotationID, status, createdDate, totalAmount)
        QuotationResponse response = modelMapper.map(quotation, QuotationResponse.class);

        // Load thông tin từ Maintenance
        if (quotation.getMaintenance() != null) {
            populateMaintenanceDetails(response, quotation.getMaintenance());
        } else {
            // Đặt giá trị mặc định nếu không có maintenance
            response.setChecklistItemsStatus(Collections.emptyList());
            response.setComponentsUsed(Collections.emptyList());
        }
        // Tải thông tin chi tiết báo giá
        populateQuotationDetails(response, quotation.getQuotationDetails());

        return response;
    }

    /**
     * Load thông tin từ Maintenance (Customer, Vehicle, Technician)
     */
    private void populateMaintenanceDetails(QuotationResponse response, Maintenance maintenance) {
        response.setMaintenanceId(maintenance.getMaintenanceID());

        if (maintenance.getEmployee() != null) {
            response.setTechnicianName(maintenance.getEmployee().getName());
        }

        if (maintenance.getVehicle() != null) {
            Vehicle vehicle = maintenance.getVehicle();
            response.setVehicleLicensePlate(vehicle.getLicensePlate());
            if (vehicle.getModel() != null) {
                response.setVehicleModel(vehicle.getModel().getModelName());
            }
        }

        if (maintenance.getOrders() != null && maintenance.getOrders().getCustomer() != null) {
            response.setCustomerName(maintenance.getOrders().getCustomer().getName());
        }

        response.setChecklistItemsStatus(mapChecklistItems(maintenance.getMaintenanceChecklists()));
        response.setComponentsUsed(mapComponentsUsed(maintenance.getMaintenanceComponents(), maintenance.getMaintenanceID()));
    }

    /**
     * danh sách Checklist
     */
    private List<ChecklistItemStatusResponse> mapChecklistItems(List<MaintenanceChecklist> checklists) {
        if (checklists == null || checklists.isEmpty()) {
            return Collections.emptyList();
        }

        return checklists.stream()
                .map(this::convertToChecklistItemResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 1 MaintenanceChecklist -> ChecklistItemStatusResponse
     */
    private ChecklistItemStatusResponse convertToChecklistItemResponse(MaintenanceChecklist mc) {
        CheckList cl = mc.getCheckList();
        if (cl == null) return null;

        return new ChecklistItemStatusResponse(
                cl.getCheckListId(),
                cl.getCheckListName(),
                cl.getCheckListType(),
                mc.getStatus(),
                mc.getNotes()
        );
    }

    /**
     * danh sách linh kiện đã sử dụng
     */
    private List<MaintenanceComponentResponse> mapComponentsUsed(List<MaintenanceComponent> components, Long maintenanceId) {
        if (components == null || components.isEmpty()) {
            return Collections.emptyList();
        }

        return components.stream()
                .map(mc -> convertToComponentResponse(mc, maintenanceId))
                .collect(Collectors.toList());
    }

    /**
     * 1 MaintenanceComponent -> MaintenanceComponentResponse
     */
    private MaintenanceComponentResponse convertToComponentResponse(MaintenanceComponent mc, Long maintenanceId) {
        MaintenanceComponentResponse compResp = new MaintenanceComponentResponse();
        compResp.setMaintenanceComponentID(mc.getMaintenanceComponentID());
        compResp.setMaintenanceId(maintenanceId);
        compResp.setQuantity(mc.getQuantity());

        if (mc.getComponent() != null) {
            Component component = mc.getComponent();
            compResp.setComponentId(component.getComponentID());
            compResp.setComponentName(component.getName());
            compResp.setComponentCode(component.getCode());
            compResp.setComponentPrice(component.getPrice());
        }
        return compResp;
    }

    /**
     * danh sách chi tiết báo giá
     */
    private void populateQuotationDetails(QuotationResponse response, List<QuotationDetail> details) {
        if (details == null || details.isEmpty()) {
            response.setQuotationDetails(Collections.emptyList());
            return;
        }

        List<QuotationDetailResponse> detailResponses = details.stream()
                .map(detail -> modelMapper.map(detail, QuotationDetailResponse.class))
                .collect(Collectors.toList());
        response.setQuotationDetails(detailResponses);
    }

}