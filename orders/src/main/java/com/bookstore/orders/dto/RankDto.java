package com.bookstore.orders.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Data
public class RankDto {
    private String id;

    private String accountId;

    private int rank;

    private int oldRank;

    private Double points;

    private YearMonth lastRankEvaluatedMonth;

    private LocalDate rankAchievedDate;
}
