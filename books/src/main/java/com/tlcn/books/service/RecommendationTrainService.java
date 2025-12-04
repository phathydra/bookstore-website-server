package com.tlcn.books.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RecommendationTrainService {
    private final RestTemplate restTemplate;

    public RecommendationTrainService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public void callPrepareDataset() {
        restTemplate.postForObject(
                "http://localhost:8000/recommend/prepare_dataset",
                null,
                String.class
        );
    }

    public void callTrainModel() {
        restTemplate.postForObject(
                "http://localhost:8000/recommend/train",
                null,
                String.class
        );
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void dailyTrain() {

        callPrepareDataset();

        try {
            Thread.sleep(300_000);
        } catch (InterruptedException ignored) {}

        callTrainModel();
    }
}
