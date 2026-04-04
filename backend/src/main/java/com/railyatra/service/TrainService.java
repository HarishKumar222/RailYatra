package com.railyatra.service;

import com.railyatra.dto.request.SearchTrainRequest;
import com.railyatra.dto.response.TrainResponse;
import com.railyatra.entity.Train;
import com.railyatra.exception.ResourceNotFoundException;
import com.railyatra.repository.BookingRepository;
import com.railyatra.repository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainService {

    private final TrainRepository trainRepository;
    private final BookingRepository bookingRepository;

    public List<TrainResponse> searchTrains(SearchTrainRequest req) {
        LocalDate date = req.getDate() != null ? req.getDate() : LocalDate.now().plusDays(1);
        List<Train> trains = trainRepository.findByRoute(req.getFrom(), req.getTo());
        return trains.stream().map(t -> mapToResponse(t, date)).toList();
    }

    @Cacheable(value = "trains", key = "#id")
    public TrainResponse getTrainById(Long id) {
        Train train = trainRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Train not found: " + id));
        return mapToResponse(train, LocalDate.now().plusDays(1));
    }

    @Cacheable(value = "stations")
    public List<String> getAllStations() {
        return trainRepository.findAllStations();
    }

    @Cacheable(value = "popular-routes")
    public List<TrainResponse> getPopularTrains() {
        return trainRepository.findAllActive(PageRequest.of(0, 10))
            .stream().map(t -> mapToResponse(t, LocalDate.now().plusDays(1))).toList();
    }

    public TrainResponse mapToResponse(Train train, LocalDate date) {
        return TrainResponse.builder()
            .id(train.getId())
            .trainNumber(train.getTrainNumber())
            .trainName(train.getTrainName())
            .sourceStation(train.getSourceStation())
            .destStation(train.getDestStation())
            .departureTime(train.getDepartureTime())
            .arrivalTime(train.getArrivalTime())
            .journeyMins(train.getJourneyMins())
            .daysOfRun(train.getDaysOfRun())
            .sl(buildClassAvail(train, "SL", date))
            .threeA(buildClassAvail(train, "3A", date))
            .twoA(buildClassAvail(train, "2A", date))
            .oneA(buildClassAvail(train, "1A", date))
            .build();
    }

    private TrainResponse.ClassAvailability buildClassAvail(Train train, String cls, LocalDate date) {
        int total = train.getTotalSeatsByClass(cls);
        if (total == 0) return null;

        int booked    = bookingRepository.countBookedSeats(train.getId(), date, cls);
        int avail     = total - booked;
        int wlCount   = bookingRepository.getMaxWaitlistNumber(train.getId(), date, cls);
        BigDecimal base    = train.getBaseFareByClass(cls);
        BigDecimal dynamic = calcDynamic(base, booked, total);

        String status;
        if (avail > 0) status = "AVAILABLE (" + avail + ")";
        else if (wlCount < 30) status = "WL/" + (wlCount + 1);
        else status = "FULLY_BOOKED";

        String name = switch (cls) {
            case "SL" -> "Sleeper"; case "3A" -> "AC 3 Tier";
            case "2A" -> "AC 2 Tier"; case "1A" -> "AC First Class"; default -> cls;
        };

        return TrainResponse.ClassAvailability.builder()
            .classCode(cls).className(name)
            .availableSeats(Math.max(0, avail)).totalSeats(total)
            .waitlistCount(wlCount).baseFare(base).dynamicFare(dynamic)
            .totalFare(dynamic.add(new BigDecimal("15.00")))
            .availabilityStatus(status).build();
    }

    private BigDecimal calcDynamic(BigDecimal base, int booked, int total) {
        double occ = (double) booked / Math.max(1, total);
        BigDecimal mult = occ < 0.5 ? BigDecimal.ONE :
                          occ < 0.75 ? new BigDecimal("1.10") :
                          occ < 0.90 ? new BigDecimal("1.25") : new BigDecimal("1.40");
        return base.multiply(mult).setScale(2, RoundingMode.HALF_UP);
    }
}
