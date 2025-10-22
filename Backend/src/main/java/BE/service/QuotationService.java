package BE.service;

import BE.entity.*;
import BE.model.response.QuotationDetailResponse;
import BE.model.response.QuotationResponse;
import BE.repository.MaintenanceRepository;
import BE.repository.QuotationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuotationService {

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public QuotationResponse createQuotation(Long maintenanceId) {
        if (quotationRepository.findByMaintenance_MaintenanceID(maintenanceId).isPresent()) {
            throw new IllegalArgumentException("Quotation for this maintenance already exists.");
        }

        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance not found with ID: " + maintenanceId));

        Quotation quotation = new Quotation();
        quotation.setMaintenance(maintenance);
        quotation.setStatus("PENDING");

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

        maintenance.setStatus("AWAITING_CUSTOMER_APPROVAL");
        maintenanceRepository.save(maintenance);

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


    @Transactional
    public void confirmQuotation(Long quotationId, boolean approved) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found with ID: " + quotationId));

        Maintenance maintenance = quotation.getMaintenance();

        if (approved) {
            quotation.setStatus("APPROVED");
            maintenance.setStatus("CONFIRMED");
        } else {
            quotation.setStatus("REJECTED");
            maintenance.setStatus("CANCELLED");
        }

        quotationRepository.save(quotation);
        maintenanceRepository.save(maintenance);
    }

    private QuotationResponse convertToResponse(Quotation quotation) {
        QuotationResponse response = modelMapper.map(quotation, QuotationResponse.class);
        response.setMaintenanceId(quotation.getMaintenance().getMaintenanceID());

        List<QuotationDetailResponse> detailResponses = quotation.getQuotationDetails().stream()
                .map(detail -> modelMapper.map(detail, QuotationDetailResponse.class))
                .collect(Collectors.toList());

        response.setQuotationDetails(detailResponses);
        return response;
    }
}