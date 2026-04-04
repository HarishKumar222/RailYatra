package com.railyatra.service;

import com.railyatra.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WaitlistPredictorService {

    private final BookingRepository bookingRepository;

    public double predictConfirmationProbability(Long trainId, LocalDate journeyDate,
                                                  String classType, int wlNumber) {
        int daysToJourney = (int)(journeyDate.toEpochDay() - LocalDate.now().toEpochDay());
        int totalWL = bookingRepository.getMaxWaitlistNumber(trainId, journeyDate, classType);

        double baseCancelRate  = 0.18;
        int    estimatedSeats  = 72;
        double expectedCancel  = estimatedSeats * baseCancelRate;
        double daysFactor      = Math.min(1.5, 1.0 + (daysToJourney * 0.02));
        double wlFactor        = Math.max(0.0, 1.0 - ((double) wlNumber / Math.max(1, totalWL)));
        double probability     = (expectedCancel / Math.max(1, totalWL)) * daysFactor * wlFactor;
        if (daysToJourney <= 1) probability *= 0.5;

        return Math.min(0.95, Math.max(0.02, probability));
    }

    public String getPredictionLabel(double probability) {
        if (probability >= 0.75) return "HIGH — Very likely to get confirmed";
        if (probability >= 0.50) return "MEDIUM — Moderate chance";
        if (probability >= 0.25) return "LOW — Unlikely but possible";
        return "VERY LOW — Consider alternative trains";
    }
}
