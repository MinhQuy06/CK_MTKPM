package com.datlichkhambenh.backend.strategy.service;

import com.datlichkhambenh.backend.strategy.dto.request.SubmitBenefitRequest;
import com.datlichkhambenh.backend.strategy.dto.request.BenefitRevokeInput;
import com.datlichkhambenh.backend.strategy.dto.request.BenefitDecisionInput;
import com.datlichkhambenh.backend.strategy.dto.response.BenefitDecisionDto;
import com.datlichkhambenh.backend.strategy.entity.BenefitDecision;
import com.datlichkhambenh.backend.strategy.repository.BenefitDecisionRepository;
import com.datlichkhambenh.backend.exception.BookingConflictException;
import com.datlichkhambenh.backend.strategy.dto.response.BenefitRequestDto;
import com.datlichkhambenh.backend.strategy.entity.BenefitRequest;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.strategy.repository.BenefitRequestRepository;
import com.datlichkhambenh.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BenefitService {
    private final BenefitRequestRepository requests;
    private final PatientRepository patients;
    private final BenefitDecisionRepository decisions;
    public BenefitService(BenefitRequestRepository requests,PatientRepository patients,BenefitDecisionRepository decisions) {
        this.requests=requests;this.patients=patients;this.decisions=decisions;
    }
    @Transactional
    public BenefitRequestDto submit(Long patientId,SubmitBenefitRequest input) {
        if(input.requestedUntil().isBefore(input.requestedFrom())) throw new AppException("Ngày kết thúc phải từ ngày bắt đầu trở đi");
        var patient=patients.findById(patientId).orElseThrow(()->new ResourceNotFoundException("Hồ sơ bệnh nhân không tồn tại"));
        if(input.renewalOfId()!=null) {
            var previous=requests.findByIdAndPatientUserId(input.renewalOfId(),patientId)
                    .orElseThrow(()->new ResourceNotFoundException("Yêu cầu gia hạn gốc không tồn tại"));
            if(!previous.getKind().equals(input.kind()) || !("APPROVED".equals(previous.getStatus()) || "REVOKED".equals(previous.getStatus())))
                throw new AppException("Chỉ gia hạn cùng loại quyền đã từng được duyệt");
        }
        var request=new BenefitRequest();request.setRenewalOfId(input.renewalOfId());
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
    @Transactional(readOnly=true)
    public List<BenefitRequestDto> all() {
        return requests.findAllByOrderByCreatedAtDescIdDesc().stream().map(this::view).toList();
    }
    @Transactional(readOnly=true)
    public BenefitRequestDto adminDetail(Long id) {
        return view(requests.findById(id).orElseThrow(()->new ResourceNotFoundException("Yêu cầu không tồn tại")));
    }
    @Transactional
    public BenefitRequestDto decide(Long id,Long adminId,BenefitDecisionInput input) {
        var request=requests.findForUpdate(id).orElseThrow(()->new ResourceNotFoundException("Yêu cầu không tồn tại"));
        if(!"PENDING".equals(request.getStatus())) throw new BookingConflictException("Yêu cầu đã được xử lý; vui lòng tải lại");
        if("APPROVE".equals(input.action())) {
            if(input.approvedFrom()==null || input.approvedUntil()==null || input.approvedUntil().isBefore(input.approvedFrom()))
                throw new AppException("Cần khoảng ngày hiệu lực hợp lệ khi duyệt");
            request.setApprovedFrom(input.approvedFrom());request.setApprovedUntil(input.approvedUntil());
            request.setStatus("APPROVED");
        } else request.setStatus("REJECTED");
        requests.saveAndFlush(request);
        decisions.saveAndFlush(new BenefitDecision(request,input.action(),input.reason().trim(),adminId));
        return view(request);
    }
    @Transactional
    public BenefitRequestDto revoke(Long id,Long adminId,BenefitRevokeInput input) {
        var request=requests.findForUpdate(id).orElseThrow(()->new ResourceNotFoundException("Yêu cầu không tồn tại"));
        if(!"APPROVED".equals(request.getStatus())) throw new BookingConflictException("Chỉ thu hồi quyền đã được duyệt; vui lòng tải lại");
        request.setStatus("REVOKED");requests.saveAndFlush(request);
        decisions.saveAndFlush(new BenefitDecision(request,"REVOKE",input.reason().trim(),adminId));
        return view(request);
    }
    @Transactional(readOnly=true)
    public java.util.Optional<BenefitRequest> eligible(Long patientId,String kind,java.time.LocalDate appointmentDay) {
        var today=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        return requests.findByPatientUserIdAndKindAndStatusOrderByIdDesc(patientId,kind,"APPROVED").stream()
                .filter(r->r.getApprovedFrom()!=null && r.getApprovedUntil()!=null
                        && !today.isBefore(r.getApprovedFrom()) && !today.isAfter(r.getApprovedUntil())
                        && !appointmentDay.isBefore(r.getApprovedFrom()) && !appointmentDay.isAfter(r.getApprovedUntil()))
                .findFirst();
    }
    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
    public BenefitRequest lockedEligible(Long patientId,String kind,java.time.LocalDate appointmentDay) {
        // Query scalar IDs first: do not cache a pre-lock entity snapshot in this transaction.
        for(Long id:requests.findApprovedIds(patientId,kind)) {
            var grant=requests.findForUpdate(id).orElse(null);
            if(grant!=null && grant.getPatient().getUserId().equals(patientId) && grant.getKind().equals(kind)
                    && "APPROVED".equals(grant.getStatus()) && coversDates(grant,appointmentDay)) return grant;
        }
        throw new com.datlichkhambenh.backend.exception.BookingRejectionException("BENEFIT_UNAVAILABLE",
                "Quyền hưởng không còn hợp lệ cho ngày khám. Vui lòng chọn lại loại phí.",org.springframework.http.HttpStatus.FORBIDDEN);
    }
    private boolean coversDates(BenefitRequest grant,java.time.LocalDate day) {
        var today=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        return grant.getApprovedFrom()!=null && grant.getApprovedUntil()!=null
                && !today.isBefore(grant.getApprovedFrom()) && !today.isAfter(grant.getApprovedUntil())
                && !day.isBefore(grant.getApprovedFrom()) && !day.isAfter(grant.getApprovedUntil());
    }
    private String effectiveStatus(BenefitRequest r) {
        if(!"APPROVED".equals(r.getStatus())) return r.getStatus();
        if(r.getApprovedFrom()==null || r.getApprovedUntil()==null) return "INVALID";
        var today=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        if(today.isAfter(r.getApprovedUntil())) return "EXPIRED";
        if(today.isBefore(r.getApprovedFrom())) return "NOT_YET_VALID";
        return "APPROVED";
    }
    private BenefitRequestDto view(BenefitRequest r) {
        var history=decisions.findByRequestIdOrderByIdAsc(r.getId()).stream()
                .map(d->new BenefitDecisionDto(d.getId(),d.getAction(),d.getReason(),d.getDecidedBy(),d.getDecidedAt())).toList();
        return new BenefitRequestDto(r.getId(),r.getPatient().getUserId(),r.getPatient().getFullName(),r.getKind(),
                r.getReferenceCode(),r.getRequestedFrom(),r.getRequestedUntil(),r.getStatus(),r.getCreatedAt(),
                r.getApprovedFrom(),r.getApprovedUntil(),r.getRenewalOfId(),effectiveStatus(r),history);
    }
}
