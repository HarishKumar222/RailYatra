package com.railyatra.websocket;

import com.railyatra.repository.BookingRepository;
import com.railyatra.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SeatAvailabilityHandler {

    private final SimpMessagingTemplate messaging;
    private final TrainRepository trainRepository;
    private final BookingRepository bookingRepository;

    @MessageMapping("/seats.subscribe")
    public void subscribeToSeats(@Payload Map<String, String> payload) {
        String trainId   = payload.get("trainId");
        String date      = payload.get("date");
        String classType = payload.get("classType");
        if (trainId != null && date != null && classType != null)
            broadcast(Long.parseLong(trainId), LocalDate.parse(date), classType);
    }

    @Scheduled(fixedDelay = 30000)
    public void scheduledBroadcast() {
        LocalDate today = LocalDate.now();
        trainRepository.findAll().stream()
            .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
            .forEach(train -> {
                for (int i = 1; i <= 3; i++) {
                    LocalDate date = today.plusDays(i);
                    for (String cls : new String[]{"SL", "3A", "2A", "1A"}) {
                        if (train.getTotalSeatsByClass(cls) > 0)
                            broadcast(train.getId(), date, cls);
                    }
                }
            });
    }

    private void broadcast(Long trainId, LocalDate date, String classType) {
        int booked = bookingRepository.countBookedSeats(trainId, date, classType);
        int total  = trainRepository.findById(trainId)
            .map(t -> t.getTotalSeatsByClass(classType)).orElse(0);
        messaging.convertAndSend(
            "/topic/seats/" + trainId + "/" + date + "/" + classType,
            Map.of("trainId", trainId, "date", date.toString(),
                   "classType", classType, "availableSeats", Math.max(0, total - booked),
                   "totalSeats", total));
    }
}
