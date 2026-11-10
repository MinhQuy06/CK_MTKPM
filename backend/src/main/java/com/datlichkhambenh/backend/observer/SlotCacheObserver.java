package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Concrete Observer: the cached free-slot list of that doctor and day is stale after a booking. */
@Component
@RequiredArgsConstructor
public class SlotCacheObserver {
    private final RedisCacheService cache;

    @EventListener
    public void onBooked(AppointmentBookedEvent event) {
        evict(event.appointment().getSchedule());
    }

    /** The freed time is offered again as a new slot, so the cached list is stale too. */
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
