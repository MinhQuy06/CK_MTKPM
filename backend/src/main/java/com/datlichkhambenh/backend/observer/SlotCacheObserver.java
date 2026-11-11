package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Concrete Observer: sau khi đặt / hủy lịch, danh sách ca trống đã cache của bác sĩ và ngày đó không còn đúng. */
@Component
@RequiredArgsConstructor
public class SlotCacheObserver {
    private final RedisCacheService cache;

    @EventListener
    public void onBooked(AppointmentBookedEvent event) {
        evict(event.appointment().getSchedule());
    }

    /** Giờ vừa trống được mở lại thành ca mới, nên danh sách đã cache cũng cũ. */
    @EventListener
    public void onCancelled(AppointmentCancelledEvent event) {
        evict(event.appointment().getSchedule());
    }

    private void evict(Schedule slot) {
        if (slot != null) {
            cache.evictSlotCache(slot.getDoctor().getUserId(), slot.getWorkDate());
        }
    }
}
