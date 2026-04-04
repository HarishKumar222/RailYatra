package com.railyatra.repository;

import com.railyatra.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPnr(String pnr);

    List<Booking> findByUserIdOrderByBookedAtDesc(Long userId);

    Page<Booking> findAllByOrderByBookedAtDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(SIZE(b.passengers)), 0) FROM Booking b " +
           "WHERE b.train.id = :trainId AND b.journeyDate = :date " +
           "AND b.classType = :classType AND b.status IN ('CONFIRMED','WAITLISTED')")
    int countBookedSeats(@Param("trainId") Long trainId,
                         @Param("date") LocalDate date,
                         @Param("classType") String classType);

    @Query("SELECT COALESCE(MAX(b.waitlistNumber), 0) FROM Booking b " +
           "WHERE b.train.id = :trainId AND b.journeyDate = :date " +
           "AND b.classType = :classType AND b.status = 'WAITLISTED'")
    int getMaxWaitlistNumber(@Param("trainId") Long trainId,
                             @Param("date") LocalDate date,
                             @Param("classType") String classType);

    @Query("SELECT b FROM Booking b WHERE b.train.id = :trainId " +
           "AND b.journeyDate = :date AND b.classType = :classType " +
           "AND b.status = 'WAITLISTED' ORDER BY b.waitlistNumber ASC")
    List<Booking> findWaitlistedBookings(@Param("trainId") Long trainId,
                                          @Param("date") LocalDate date,
                                          @Param("classType") String classType);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED'")
    long countConfirmedBookings();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal sumTotalRevenue();
}
