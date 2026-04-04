package com.railyatra.service;

import com.railyatra.dto.request.BookingRequest;
import com.railyatra.dto.response.BookingResponse;
import com.railyatra.entity.*;
import com.railyatra.entity.Booking.BookingStatus;
import com.railyatra.exception.BookingException;
import com.railyatra.exception.ResourceNotFoundException;
import com.railyatra.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;
    
    private final EmailService emailService;

    @Value("${booking.convenience-fee}")
    private BigDecimal convenienceFee;

    @Value("${booking.max-passengers-per-booking}")
    private int maxPassengers;

    @Transactional
    public BookingResponse createBooking(BookingRequest req, Long userId) {
        if (req.getPassengers().size() > maxPassengers)
            throw new BookingException("Max " + maxPassengers + " passengers per booking");

        if (req.getJourneyDate().isBefore(LocalDate.now()))
            throw new BookingException("Journey date cannot be in the past");

        Train train = trainRepository.findById(req.getTrainId())
            .orElseThrow(() -> new ResourceNotFoundException("Train not found: " + req.getTrainId()));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        int total = train.getTotalSeatsByClass(req.getClassType());
        if (total == 0) throw new BookingException("Class " + req.getClassType() + " not available on this train");

        int booked = bookingRepository.countBookedSeats(train.getId(), req.getJourneyDate(), req.getClassType());
        int avail  = total - booked;
        int pCount = req.getPassengers().size();

        BookingStatus status;
        Integer wlNumber = null;

        if (avail >= pCount) {
            status = BookingStatus.PENDING;
        } else {
            int maxWL = bookingRepository.getMaxWaitlistNumber(train.getId(), req.getJourneyDate(), req.getClassType());
            wlNumber = maxWL + 1;
            status = BookingStatus.WAITLISTED;
        }

        BigDecimal base   = train.getBaseFareByClass(req.getClassType());
        BigDecimal amount = base.multiply(BigDecimal.valueOf(pCount)).add(convenienceFee);
        String pnr = generatePNR();

        Booking booking = Booking.builder()
            .pnr(pnr).user(user).train(train)
            .journeyDate(req.getJourneyDate())
            .classType(req.getClassType().toUpperCase())
            .status(status).waitlistNumber(wlNumber)
            .totalAmount(amount).convenienceFee(convenienceFee)
            .build();

        List<Passenger> passengers = req.getPassengers().stream().map(p ->
            Passenger.builder()
                .booking(booking).name(p.getName()).age(p.getAge())
                .gender(p.getGender()).berthPref(p.getBerthPref())
                .seatNumber(status == BookingStatus.CONFIRMED ? req.getClassType() + "-" + (booked + 1) : null)
                .build()
        ).toList();

        booking.setPassengers(passengers);
        Booking saved = bookingRepository.save(booking);
        
        return mapToResponse(saved);
    }

    public BookingResponse getPNRStatus(String pnr) {
        Booking b = bookingRepository.findByPnr(pnr)
            .orElseThrow(() -> new ResourceNotFoundException("PNR not found: " + pnr));
        return mapToResponse(b);
    }

    public BookingResponse getBookingById(Long id) {
        Booking b = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        return mapToResponse(b);
    }

    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByBookedAtDesc(userId)
            .stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getUser().getId().equals(userId))
            throw new BookingException("Unauthorized: booking does not belong to this user");
        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new BookingException("Booking already cancelled");

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationCharges(calcCancellationCharges(booking));
        bookingRepository.save(booking);

        promoteWaitlist(booking.getTrain().getId(), booking.getJourneyDate(), booking.getClassType());
        return mapToResponse(booking);
    }

    private void promoteWaitlist(Long trainId, LocalDate date, String classType) {
        List<Booking> wl = bookingRepository.findWaitlistedBookings(trainId, date, classType);
        if (!wl.isEmpty()) {
            Booking next = wl.get(0);
            next.setStatus(BookingStatus.CONFIRMED);
            next.setWaitlistNumber(null);
            bookingRepository.save(next);
            emailService.sendWaitlistConfirmation(next);
            log.info("Promoted WL booking {} to CONFIRMED", next.getPnr());
        }
    }

    private BigDecimal calcCancellationCharges(Booking b) {
        long hours = java.time.Duration.between(LocalDateTime.now(),
            b.getJourneyDate().atTime(b.getTrain().getDepartureTime())).toHours();
        if (hours > 24) return b.getTotalAmount().multiply(new BigDecimal("0.10"));
        if (hours > 12) return b.getTotalAmount().multiply(new BigDecimal("0.25"));
        if (hours > 4)  return b.getTotalAmount().multiply(new BigDecimal("0.50"));
        return b.getTotalAmount();
    }

    private String generatePNR() {
        Random rand = new Random();
        String pnr;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) sb.append(rand.nextInt(10));
            pnr = sb.toString();
        } while (bookingRepository.findByPnr(pnr).isPresent());
        return pnr;
    }

    public BookingResponse mapToResponse(Booking b) {
        return BookingResponse.builder()
            .id(b.getId()).pnr(b.getPnr())
            .trainNumber(b.getTrain().getTrainNumber())
            .trainName(b.getTrain().getTrainName())
            .sourceStation(b.getTrain().getSourceStation())
            .destStation(b.getTrain().getDestStation())
            .journeyDate(b.getJourneyDate())
            .classType(b.getClassType()).status(b.getStatus().name())
            .waitlistNumber(b.getWaitlistNumber())
            .totalAmount(b.getTotalAmount()).convenienceFee(b.getConvenienceFee())
            .bookedAt(b.getBookedAt())
            .passengers(b.getPassengers().stream().map(p ->
                BookingResponse.PassengerInfo.builder()
                    .name(p.getName()).age(p.getAge()).gender(p.getGender())
                    .seatNumber(p.getSeatNumber()).berthPref(p.getBerthPref())
                    .build()).toList())
            .build();
    }
}
