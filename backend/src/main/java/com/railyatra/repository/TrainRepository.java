package com.railyatra.repository;

import com.railyatra.entity.Train;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByTrainNumber(String trainNumber);

    @Query("SELECT t FROM Train t WHERE LOWER(t.sourceStation) = LOWER(:source) " +
           "AND LOWER(t.destStation) = LOWER(:dest) AND t.isActive = true")
    List<Train> findByRoute(@Param("source") String source, @Param("dest") String dest);

    @Query(value = "SELECT DISTINCT source_station FROM trains WHERE is_active = true " +
                   "UNION SELECT DISTINCT dest_station FROM trains WHERE is_active = true " +
                   "ORDER BY 1", nativeQuery = true)
    List<String> findAllStations();

    @Query("SELECT t FROM Train t WHERE t.isActive = true ORDER BY t.trainName ASC")
    List<Train> findAllActive(Pageable pageable);
}
