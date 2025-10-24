package BE.service;

import BE.entity.CheckList;
import BE.entity.Maintenance;
import BE.entity.MaintenanceChecklist;
import BE.model.DTO.MaintenanceChecklistDTO;
import BE.model.response.MaintenanceChecklistResponse;
import BE.repository.CheckListRepository;
import BE.repository.MaintenanceChecklistRepository;
import BE.repository.MaintenanceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceChecklistService {

    @Autowired
    private MaintenanceChecklistRepository maintenanceChecklistRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private CheckListRepository checkListRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public MaintenanceChecklistResponse addOrUpdateChecklistItem(Long maintenanceId, MaintenanceChecklistDTO dto) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance not found with ID: " + maintenanceId));

        CheckList checkList = checkListRepository.findById(dto.getCheckListId())
                .orElseThrow(() -> new EntityNotFoundException("CheckList not found with ID: " + dto.getCheckListId()));

        // Tìm maintenance và checklist
        MaintenanceChecklist item = maintenanceChecklistRepository
                .findByMaintenance_MaintenanceIDAndCheckList_CheckListId(maintenanceId, dto.getCheckListId())
                .orElse(new MaintenanceChecklist());

        item.setMaintenance(maintenance);
        item.setCheckList(checkList);
        item.setStatus(dto.getStatus());
        item.setNotes(dto.getNotes());

        MaintenanceChecklist savedItem = maintenanceChecklistRepository.save(item);
        return convertToResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceChecklistResponse> getChecklistItemsByMaintenanceId(Long maintenanceId) {
        if (!maintenanceRepository.existsById(maintenanceId)) {
            throw new EntityNotFoundException("Maintenance not found with ID: " + maintenanceId);
        }
        List<MaintenanceChecklist> items = maintenanceChecklistRepository.findByMaintenance_MaintenanceID(maintenanceId);
        return items.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteChecklistItem(Long maintenanceChecklistId) {
        MaintenanceChecklist item = maintenanceChecklistRepository.findById(maintenanceChecklistId)
                .orElseThrow(() -> new EntityNotFoundException("MaintenanceChecklist item not found with ID: " + maintenanceChecklistId));

        maintenanceChecklistRepository.delete(item);
    }


    private MaintenanceChecklistResponse convertToResponse(MaintenanceChecklist item) {
        MaintenanceChecklistResponse response = modelMapper.map(item, MaintenanceChecklistResponse.class);
        if (item.getMaintenance() != null) {
            response.setMaintenanceId(item.getMaintenance().getMaintenanceID());
        }
        if (item.getCheckList() != null) {
            response.setCheckListId(item.getCheckList().getCheckListId());
            response.setCheckListName(item.getCheckList().getCheckListName());
            response.setCheckListType(item.getCheckList().getCheckListType());
        }
        return response;
    }

}