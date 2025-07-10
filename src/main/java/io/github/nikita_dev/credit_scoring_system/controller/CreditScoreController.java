package io.github.nikita_dev.credit_scoring_system.controller;

import io.github.nikita_dev.credit_scoring_system.dto.CreditDecisionRequest;
import io.github.nikita_dev.credit_scoring_system.dto.CreditDecisionResponse;
import io.github.nikita_dev.credit_scoring_system.service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/credit-scores")
@RequiredArgsConstructor
public class CreditScoreController {

    private final PredictionService predictionService;

    @PostMapping("/predict")
    public ResponseEntity<CreditDecisionResponse> predict(@Valid @RequestBody CreditDecisionRequest request) {
        log.info("POST /predict: Received credit decision request.");
        CreditDecisionResponse response = predictionService.predict(request);
        log.info("POST /predict: Responded with decision: '{}', Score: {}.", response.decision(), response.score());
        return ResponseEntity.ok(response);
    }
}

