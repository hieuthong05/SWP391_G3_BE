package BE.service;

import BE.entity.Certification;
import BE.entity.Employee;
import BE.model.request.CertificationRequest;
import BE.model.response.CertificationResponse;
import BE.repository.CertificationRepository;
import BE.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificationService {

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public CertificationResponse createCertification(CertificationRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeID())
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + request.getEmployeeID()));

        Certification certification = new Certification();
        certification.setCertificateName(request.getCertificateName());
        certification.setIssuedBy(request.getIssuedBy());
        certification.setLevel(request.getLevel());
        certification.setIssuedDate(request.getIssuedDate());
        certification.setExpirationDate(request.getExpirationDate());
        certification.setActive(request.isActive());
        certification.setStatus(true);
        certification.setEmployee(employee);

        Certification saved = certificationRepository.save(certification);
        return mapToResponse(saved);
    }

    public List<CertificationResponse> getAllCertifications() {
        return certificationRepository.findByStatusTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CertificationResponse getCertificationById(Long id) {
        Certification certification = certificationRepository.findByCertificationIDAndStatusTrue(id)
                .orElseThrow(() -> new RuntimeException("Certification not found with ID: " + id));
        return mapToResponse(certification);
    }

    @Transactional
    public CertificationResponse updateCertification(Long id, CertificationRequest request) {
        Certification certification = certificationRepository.findByCertificationIDAndStatusTrue(id)
                .orElseThrow(() -> new RuntimeException("Certification not found with ID: " + id));

        if (request.getEmployeeID() != null && !request.getEmployeeID().equals(certification.getEmployee().getEmployeeID())) {
            Employee newEmployee = employeeRepository.findById(request.getEmployeeID())
                    .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + request.getEmployeeID()));
            certification.setEmployee(newEmployee);
        }

        certification.setCertificateName(request.getCertificateName());
        certification.setIssuedBy(request.getIssuedBy());
        certification.setLevel(request.getLevel());
        certification.setIssuedDate(request.getIssuedDate());
        certification.setExpirationDate(request.getExpirationDate());
        certification.setActive(request.isActive());

        Certification updated = certificationRepository.save(certification);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteCertification(Long id) {
        Certification certification = certificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certification not found with ID: " + id));
        certification.setStatus(false);
        certificationRepository.save(certification);
    }

    public List<CertificationResponse> getCertificationsByEmployee(Long employeeID) {
        return certificationRepository.findByEmployee_EmployeeIDAndStatusTrue(employeeID)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByLevel(String level) {
        return certificationRepository.findByLevelAndStatusTrue(level)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByIssuedBy(String issuedBy) {
        return certificationRepository.findByIssuedByAndStatusTrue(issuedBy)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByIssuedDate(LocalDate issuedDate) {
        return certificationRepository.findByIssuedDateAndStatusTrue(issuedDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByIssuedDateRange(LocalDate startDate, LocalDate endDate) {
        return certificationRepository.findByIssuedDateBetween(startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByExpirationDate(LocalDate expirationDate) {
        return certificationRepository.findByExpirationDateAndStatusTrue(expirationDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByExpirationDateRange(LocalDate startDate, LocalDate endDate) {
        return certificationRepository.findByExpirationDateBetween(startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByStatus(boolean status) {
        return certificationRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByActive(boolean active) {
        return certificationRepository.findByActiveAndStatusTrue(active)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByEmployeeAndActive(Long employeeID, boolean active) {
        return certificationRepository.findByEmployee_EmployeeIDAndActiveAndStatusTrue(employeeID, active)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CertificationResponse> getCertificationsByEmployeeAndLevel(Long employeeID, String level) {
        return certificationRepository.findByEmployee_EmployeeIDAndLevelAndStatusTrue(employeeID, level)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CertificationResponse mapToResponse(Certification certification) {
        CertificationResponse response = new CertificationResponse();
        response.setCertificationID(certification.getCertificationID());
        response.setCertificateName(certification.getCertificateName());
        response.setIssuedBy(certification.getIssuedBy());
        response.setLevel(certification.getLevel());
        response.setIssuedDate(certification.getIssuedDate());
        response.setExpirationDate(certification.getExpirationDate());
        response.setActive(certification.isActive());
        response.setStatus(certification.isStatus());

        if (certification.getEmployee() != null) {
            response.setEmployeeID(certification.getEmployee().getEmployeeID());
            response.setEmployeeName(certification.getEmployee().getName());
        }

        return response;
    }
}
