package com.yhs.inventroysystem.presentation.sales;

import com.yhs.inventroysystem.application.sales.SalesStatsService;
import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-stats")
@RequiredArgsConstructor
public class SalesStatsRestController {

    private final SalesStatsService salesStatsService;

    @GetMapping("/weekly/current")
    public ResponseEntity<WeeklySalesResponse> getThisWeekSales() {
        return ResponseEntity.ok(salesStatsService.getThisWeekSales());
    }

    @GetMapping("/weekly/last")
    public ResponseEntity<WeeklySalesResponse> getLastWeekSales() {
        return ResponseEntity.ok(salesStatsService.getLastWeekSales());
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlySalesByClientResponse> getYearlySalesByClient(
            @PathVariable int year) {
        return ResponseEntity.ok(salesStatsService.getYearlySalesByClient(year));
    }
}