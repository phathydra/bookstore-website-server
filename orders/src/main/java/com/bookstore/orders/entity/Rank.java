package com.bookstore.orders.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Document(collection = "rank")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Rank {
    @Id
    private String id;

    private String accountId;

//    Ranking: num_rank_point
//    1_Dong_0
//    2_Bac_500
//    3_Vang_1500
//    4_KimCuong_300
    private int rank;

    private int oldRank;

    private Double points;

    private String lastRankEvaluatedMonth;

    private LocalDate rankAchievedDate;
}
