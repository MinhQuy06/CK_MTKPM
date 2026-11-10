package com.datlichkhambenh.backend.strategy.service;

import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.BookingConflictException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.strategy.dto.request.BenefitDecisionInput;
import com.datlichkhambenh.backend.strategy.dto.request.BenefitRevokeInput;
import com.datlichkhambenh.backend.strategy.dto.request.SubmitBenefitRequest;
import com.datlichkhambenh.backend.strategy.dto.response.BenefitDecisionDto;
import com.datlichkhambenh.backend.strategy.dto.response.BenefitRequestDto;
import com.datlichkhambenh.backend.strategy.entity.BenefitDecision;
import com.datlichkhambenh.backend.strategy.entity.BenefitRequest;
import com.datlichkhambenh.backend.strategy.repository.BenefitDecisionRepository;
import com.datlichkhambenh.backend.strategy.repository.BenefitRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Quyền hưởng Bảo hiểm / VIP: bệnh nhân gửi yêu cầu, Admin duyệt / từ chối / thu hồi, và quy tắc
 * "quyền hưởng còn hiệu lực" (xem GLOSSARY.md) mà BookingOffer dùng khi báo giá và đặt lịch.
 */
@Service
public class BenefitService {
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final BenefitRequestRepository requests;
    private final PatientRepository patients;
    private final BenefitDecisionRepository decisions;

    public BenefitService(BenefitRequestRepository requests, PatientRepository patients,
                          BenefitDecisionRepository decisions) {
        this.requests = requests;
        this.patients = patients;
        this.decisions = decisions;
    }

    // ------------------------------------------------------------------ bệnh nhân

    /** Bệnh nhân gửi yêu cầu mới hoặc gia hạn (cùng loại, bản gốc đã từng được duyệt). */
    @Transactional
    public BenefitRequestDto submit(Long patientId, SubmitBenefitRequest input) {
        if (input.requestedUntil().isBefore(input.requestedFrom())) {
            throw new AppException("Ngày kết thúc phải từ ngày bắt đầu trở đi");
        }
        var patient = patients.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ bệnh nhân không tồn tại"));
        if (input.renewalOfId() != null) {
            var previous = requests.findByIdAndPatientUserId(input.renewalOfId(), patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu gia hạn gốc không tồn tại"));
            boolean everApproved = "APPROVED".equals(previous.getStatus()) || "REVOKED".equals(previous.getStatus());
            if (!previous.getKind().equals(input.kind()) || !everApproved) {
                throw new AppException("Chỉ gia hạn cùng loại quyền đã từng được duyệt");
            }
        }
        var request = new BenefitRequest();
        request.setRenewalOfId(input.renewalOfId());
        request.setPatient(patient);
        request.setKind(input.kind());
        request.setReferenceCode(input.referenceCode().trim());
        request.setRequestedFrom(input.requestedFrom());
        request.setRequestedUntil(input.requestedUntil());
        request.setStatus("PENDING");
        return view(requests.saveAndFlush(request));
    }

    @Transactional(readOnly = true)
    public List<BenefitRequestDto> mine(Long patientId) {
        return requests.findByPatientUserIdOrderByCreatedAtDescIdDesc(patientId).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public BenefitRequestDto detail(Long patientId, Long id) {
        return view(requests.findByIdAndPatientUserId(id, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại")));
    }

    // ------------------------------------------------------------------ Admin

    @Transactional(readOnly = true)
    public List<BenefitRequestDto> all() {
        return requests.findAllByOrderByCreatedAtDescIdDesc().stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public BenefitRequestDto adminDetail(Long id) {
        return view(requests.findById(id).orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại")));
    }

    /** Admin duyệt (kèm khoảng ngày hiệu lực) hoặc từ chối một yêu cầu đang chờ; ghi lịch sử quyết định. */
    @Transactional
    public BenefitRequestDto decide(Long id, Long adminId, BenefitDecisionInput input) {
        var request = requests.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại"));
        if (!"PENDING".equals(request.getStatus())) {
            throw new BookingConflictException("Yêu cầu đã được xử lý; vui lòng tải lại");
        }
        if ("APPROVE".equals(input.action())) {
            if (input.approvedFrom() == null || input.approvedUntil() == null
                    || input.approvedUntil().isBefore(input.approvedFrom())) {
                throw new AppException("Cần khoảng ngày hiệu lực hợp lệ khi duyệt");
            }
            request.setApprovedFrom(input.approvedFrom());
            request.setApprovedUntil(input.approvedUntil());
            request.setStatus("APPROVED");
        } else {
            request.setStatus("REJECTED");
        }
        requests.saveAndFlush(request);
        decisions.saveAndFlush(new BenefitDecision(request, input.action(), input.reason().trim(), adminId));
        return view(request);
    }

    /** Admin thu hồi quyền đã duyệt; lịch sử được giữ, không xóa. */
    @Transactional
    public BenefitRequestDto revoke(Long id, Long adminId, BenefitRevokeInput input) {
        var request = requests.findForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu không tồn tại"));
        if (!"APPROVED".equals(request.getStatus())) {
            throw new BookingConflictException("Chỉ thu hồi quyền đã được duyệt; vui lòng tải lại");
        }
        request.setStatus("REVOKED");
        requests.saveAndFlush(request);
        decisions.saveAndFlush(new BenefitDecision(request, "REVOKE", input.reason().trim(), adminId));
        return view(request);
    }

    // ------------------------------------------------------------------ quyền hưởng còn hiệu lực

    /**
     * Quyền hưởng còn hiệu lực (xem GLOSSARY.md): đã duyệt, chưa thu hồi, khoảng ngày chứa cả hôm nay lẫn ngày khám
     * (giờ Việt Nam, tính cả ngày biên). Đây là quy tắc duy nhất, dùng cho loại phí, báo giá và đặt lịch.
     */
    @Transactional(readOnly = true)
    public Optional<BenefitRequest> covering(Long patientId, String kind, LocalDate appointmentDay) {
        return requests.findByPatientUserIdAndKindAndStatusOrderByIdDesc(patientId, kind, "APPROVED").stream()
                .filter(r -> covers(r, appointmentDay))
                .findFirst();
    }

    /** Cùng quy tắc với {@link #covering}, nhưng khóa dòng quyền hưởng đến hết giao dịch của nơi gọi. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<BenefitRequest> lockCovering(Long patientId, String kind, LocalDate appointmentDay) {
        // Chỉ lấy ID trước, khóa từng dòng rồi mới kiểm tra: không giữ bản entity cũ trong giao dịch.
        for (Long id : requests.findApprovedIds(patientId, kind)) {
            var grant = requests.findForUpdate(id).orElse(null);
            if (grant != null && grant.getPatient().getUserId().equals(patientId) && grant.getKind().equals(kind)
                    && "APPROVED".equals(grant.getStatus()) && covers(grant, appointmentDay)) {
                return Optional.of(grant);
            }
        }
        return Optional.empty();
    }

    private static LocalDate today() {
        return LocalDate.now(CLINIC_ZONE);
    }

    private static boolean inWindow(BenefitRequest grant, LocalDate day) {
        return grant.getApprovedFrom() != null && grant.getApprovedUntil() != null
                && !day.isBefore(grant.getApprovedFrom()) && !day.isAfter(grant.getApprovedUntil());
    }

    private static boolean covers(BenefitRequest grant, LocalDate appointmentDay) {
        return inWindow(grant, today()) && inWindow(grant, appointmentDay);
    }

    /** Trạng thái hiển thị: quyền đã duyệt có thể đã hết hạn hoặc chưa tới ngày hiệu lực. */
    private String effectiveStatus(BenefitRequest r) {
        if (!"APPROVED".equals(r.getStatus())) {
            return r.getStatus();
        }
        if (r.getApprovedFrom() == null || r.getApprovedUntil() == null) {
            return "INVALID";
        }
        var today = today();
        if (today.isAfter(r.getApprovedUntil())) {
            return "EXPIRED";
        }
        if (today.isBefore(r.getApprovedFrom())) {
            return "NOT_YET_VALID";
        }
        return "APPROVED";
    }

    private BenefitRequestDto view(BenefitRequest r) {
        var history = decisions.findByRequestIdOrderByIdAsc(r.getId()).stream()
                .map(d -> new BenefitDecisionDto(d.getId(), d.getAction(), d.getReason(), d.getDecidedBy(),
                        d.getDecidedAt()))
                .toList();
        return new BenefitRequestDto(r.getId(), r.getPatient().getUserId(), r.getPatient().getFullName(), r.getKind(),
                r.getReferenceCode(), r.getRequestedFrom(), r.getRequestedUntil(), r.getStatus(), r.getCreatedAt(),
                r.getApprovedFrom(), r.getApprovedUntil(), r.getRenewalOfId(), effectiveStatus(r), history);
    }
}
