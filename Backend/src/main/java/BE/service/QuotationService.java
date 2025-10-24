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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class QuotationService {

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ModelMapper modelMapper;

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
    public void confirmQuotation(Long quotationId, boolean approved) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found with ID: " + quotationId));

        Maintenance maintenance = quotation.getMaintenance();
        Orders order = maintenance.getOrders();

        if (approved) {
            quotation.setStatus("APPROVED");
            maintenance.setStatus("CONFIRMED");
            order.setStatus("PROCESSING");

        } else {
            quotation.setStatus("REJECTED");
            maintenance.setStatus("CANCELLED");
            order.setStatus("CANCELLED");
        }

        quotationRepository.save(quotation);
        maintenanceRepository.save(maintenance);
    }

    @Transactional(readOnly = true)
    public List<QuotationResponse> getAllQuotation() {
        List<Quotation> quotations = quotationRepository.findAll();
        return quotations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private QuotationResponse convertToResponse(Quotation quotation) {
        QuotationResponse response = modelMapper.map(quotation, QuotationResponse.class);

        if (quotation.getMaintenance() != null) {
            Maintenance maintenance = quotation.getMaintenance();
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

            if (maintenance.getMaintenanceChecklists() != null && !maintenance.getMaintenanceChecklists().isEmpty()) {
                response.setChecklistItemsStatus(maintenance.getMaintenanceChecklists().stream()
                        .map(mc -> {
                            CheckList cl = mc.getCheckList();
                            if (cl == null) return null;
                            return new ChecklistItemStatusResponse(
                                    cl.getCheckListId(),
                                    cl.getCheckListName(),
                                    cl.getCheckListType(),
                                    mc.getStatus(),
                                    mc.getNotes()
                            );
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            } else {
                response.setChecklistItemsStatus(Collections.emptyList());
            }

            // danh sách linh kiện sử dụng
            if (maintenance.getMaintenanceComponents() != null && !maintenance.getMaintenanceComponents().isEmpty()) {
                response.setComponentsUsed(maintenance.getMaintenanceComponents().stream()
                        .map(mc -> {
                            MaintenanceComponentResponse compResp = new MaintenanceComponentResponse();
                            compResp.setMaintenanceComponentID(mc.getMaintenanceComponentID());
                            compResp.setMaintenanceId(maintenance.getMaintenanceID());
                            compResp.setQuantity(mc.getQuantity());
                            if (mc.getComponent() != null) {
                                Component component = mc.getComponent();
                                compResp.setComponentId(component.getComponentID());
                                compResp.setComponentName(component.getName());
                                compResp.setComponentCode(component.getCode());
                                compResp.setComponentPrice(component.getPrice());
                            }
                            return compResp;
                        })
                        .collect(Collectors.toList()));
            } else {
                response.setComponentsUsed(Collections.emptyList());
            }

        } else {
            response.setChecklistItemsStatus(Collections.emptyList());
            response.setComponentsUsed(Collections.emptyList());
        }

        if (quotation.getQuotationDetails() != null) {
            response.setQuotationDetails(quotation.getQuotationDetails().stream()
                    .map(detail -> modelMapper.map(detail, QuotationDetailResponse.class))
                    .collect(Collectors.toList()));
        } else {
            response.setQuotationDetails(Collections.emptyList());
        }

        return response;
    }
    
}