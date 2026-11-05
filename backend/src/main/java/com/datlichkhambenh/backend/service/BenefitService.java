package com.datlichkhambenh.backend.service;
import com.datlichkhambenh.backend.dto.request.SubmitBenefitRequest;
import com.datlichkhambenh.backend.dto.response.BenefitRequestDto;
import com.datlichkhambenh.backend.entity.BenefitRequest;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.repository.BenefitRequestRepository;
import com.datlichkhambenh.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BenefitService {
    private final BenefitRequestRepository requests;
    private final PatientRepository patients;
    public BenefitService(BenefitRequestRepository requests,PatientRepository patients) {
        this.requests=requests;this.patients=patients;
    }
    @Transactional
    public BenefitRequestDto submit(Long patientId,SubmitBenefitRequest input) {
        if(input.requestedUntil().isBefore(input.requestedFrom())) throw new AppException("Ngày kết thúc phải từ ngày bắt đầu trở đi");
        var patient=patients.findById(patientId).orElseThrow(()->new ResourceNotFoundException("Hồ sơ bệnh nhân không tồn tại"));
        var request=new BenefitRequest();
        request.setPatient(patient);request.setKind(input.kind());request.setReferenceCode(input.referenceCode().trim());
        request.setRequestedFrom(input.requestedFrom());request.setRequestedUntil(input.requestedUntil());
        request.setStatus("PENDING");
        return view(requests.saveAndFlush(request));
    }
    @Transactional(readOnly=true)
    public List<BenefitRequestDto> mine(Long patientId) {
        return requests.findByPatientUserIdOrderByCreatedAtDescIdDesc(patientId).stream().map(this::view).toList();
    }
    @Transactional(readOnly=true)
    public BenefitRequestDto detail(Long patientId,Long id) {
        return view(requests.findByIdAndPatientUserId(id,patientId)
                .orElseThrow(()->new ResourceNotFoundException("Yêu cầu không tồn tại")));
    }
    private BenefitRequestDto view(BenefitRequest r) {
        return new BenefitRequestDto(r.getId(),r.getKind(),r.getReferenceCode(),r.getRequestedFrom(),
                r.getRequestedUntil(),r.getStatus(),r.getCreatedAt());
    }
}
