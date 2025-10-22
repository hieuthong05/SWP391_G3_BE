package BE.service;

import BE.entity.CheckList;
import BE.model.request.CheckListRequest;
import BE.model.response.CheckListResponse;
import BE.repository.CheckListRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckListService {

    @Autowired
    private CheckListRepository checkListRepository;


     // Tạo checklist mới

    @Transactional
    public CheckListResponse createCheckList(CheckListRequest request)
    {
        // Check duplicate name
        if (checkListRepository.existsByCheckListNameAndIsActiveTrue(request.getCheckListName()))
        {
            throw new IllegalArgumentException(
                    "CheckList with name '" + request.getCheckListName() + "' already exists");
        }

        CheckList checkList = new CheckList();
        checkList.setCheckListName(request.getCheckListName());
        checkList.setCheckListType(request.getCheckListType());
        checkList.setDescription(request.getDescription());
        checkList.setIsActive(true);

        CheckList savedCheckList = checkListRepository.save(checkList);
        return mapToResponse(savedCheckList);
    }

    // Lấy tất cả checklists

    @Transactional(readOnly = true)
    public List<CheckListResponse> getAllCheckLists()
    {
        List<CheckList> checkLists = checkListRepository.findAll();
        return checkLists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


     // Lấy active checklists

    @Transactional(readOnly = true)
    public List<CheckListResponse> getActiveCheckLists()
    {
        List<CheckList> checkLists = checkListRepository.findByIsActiveTrue();
        return checkLists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


     // Lấy checklists theo type

    @Transactional(readOnly = true)
    public List<CheckListResponse> getCheckListsByType(String type)
    {
        List<CheckList> checkLists = checkListRepository.findActiveByType(type);
        return checkLists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


     // Lấy checklist theo ID

    @Transactional(readOnly = true)
    public CheckListResponse getCheckListById(Long id)
    {
        CheckList checkList = checkListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "CheckList not found with ID: " + id));
        return mapToResponse(checkList);
    }


     // Update checklist

    @Transactional
    public CheckListResponse updateCheckList(Long id, CheckListRequest request)
    {
        CheckList checkList = checkListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "CheckList not found with ID: " + id));

        checkList.setCheckListName(request.getCheckListName());
        checkList.setCheckListType(request.getCheckListType());
        checkList.setDescription(request.getDescription());

        CheckList updatedCheckList = checkListRepository.save(checkList);
        return mapToResponse(updatedCheckList);
    }


     // Soft delete checklist

    @Transactional
    public void deleteCheckList(Long id)
    {
        CheckList checkList = checkListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "CheckList not found with ID: " + id));

        if (!checkList.getIsActive())
        {
            throw new IllegalStateException("CheckList is already deactivated");
        }

        checkList.setIsActive(false);
        checkListRepository.save(checkList);
    }

    private CheckListResponse mapToResponse(CheckList checkList)
    {
        CheckListResponse response = new CheckListResponse();
        response.setCheckListId(checkList.getCheckListId());
        response.setCheckListName(checkList.getCheckListName());
        response.setCheckListType(checkList.getCheckListType());
        response.setDescription(checkList.getDescription());
        response.setIsActive(checkList.getIsActive());
        response.setCreatedAt(checkList.getCreatedAt());
        return response;
    }
}
