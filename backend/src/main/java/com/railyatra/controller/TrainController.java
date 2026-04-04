package com.railyatra.controller;

import com.railyatra.dto.request.SearchTrainRequest;
import com.railyatra.dto.response.ApiResponse;
import com.railyatra.dto.response.TrainResponse;
import com.railyatra.service.TrainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trains")
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TrainResponse>>> search(
            @Valid SearchTrainRequest req) {
        List<TrainResponse> trains = trainService.searchTrains(req);
        return ResponseEntity.ok(ApiResponse.success(trains, trains.size() + " train(s) found"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trainService.getTrainById(id), "Train details"));
    }

    @GetMapping("/stations")
    public ResponseEntity<ApiResponse<List<String>>> stations() {
        return ResponseEntity.ok(ApiResponse.success(trainService.getAllStations(), "Stations"));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<TrainResponse>>> popular() {
        return ResponseEntity.ok(ApiResponse.success(trainService.getPopularTrains(), "Popular trains"));
    }
}
