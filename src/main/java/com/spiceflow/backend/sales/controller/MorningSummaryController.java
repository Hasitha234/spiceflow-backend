package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.sales.dto.request.MorningSummaryRequest;
import com.spiceflow.backend.sales.dto.response.MorningSummaryResponse;
import com.spiceflow.backend.sales.service.MorningSummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/morning-summaries")
@RequiredArgsConstructor
public class MorningSummaryController {

    private final MorningSummaryService morningSummaryService;

    @PostMapping
    public ResponseEntity<MorningSummaryResponse> createMorningSummary(
            @RequestAttribute("tenantId") Long tenantId,
            @Valid @RequestBody MorningSummaryRequest request) {
        MorningSummaryResponse response = morningSummaryService.createMorningSummary(tenantId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<MorningSummaryResponse>> getAllMorningSummaries(
            @RequestAttribute("tenantId") Long tenantId,
            Pageable pageable) {
        return ResponseEntity.ok(morningSummaryService.getAllSummaries(tenantId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MorningSummaryResponse> getMorningSummaryById(
            @RequestAttribute("tenantId") Long tenantId,
            @PathVariable Long id) {
        return ResponseEntity.ok(morningSummaryService.getSummaryById(tenantId, id));
    }
}
