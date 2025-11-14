package BE.service;


import BE.entity.Shift;
import BE.model.DTO.ShiftDTO;
import BE.repository.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    public List<ShiftDTO> getAllShifts() {
        // Gọi query đã tạo trong Repository
        List<Shift> shifts = shiftRepository.findAllWithServiceCenter();

        // Chuyển đổi danh sách Entity sang danh sách DTO
        return shifts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ShiftDTO convertToDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();
        dto.setShiftID(shift.getShiftID());
        dto.setName(shift.getName());
        dto.setShift_date(shift.getShift_date());
        dto.setStart_time(shift.getStart_time());
        dto.setEnd_time(shift.getEnd_time());
        dto.setStatus(shift.isStatus());

        if (shift.getServiceCenter() != null) {
            dto.setServiceCenterID(shift.getServiceCenter().getServiceCenterID());
            dto.setServiceCenterName(shift.getServiceCenter().getName());
        }

        return dto;
    }
}