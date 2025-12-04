package com.bookstore.orders.mapper;

import com.bookstore.orders.dto.RankDto;
import com.bookstore.orders.entity.Rank;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
public class RankMapper {
    static public Rank toRank(RankDto rankDto, Rank rank){
        rank.setId(rankDto.getId());
        rank.setAccountId(rankDto.getAccountId());
        rank.setRank(rankDto.getRank());
        rank.setOldRank(rankDto.getOldRank());
        rank.setPoints(rankDto.getPoints());
        rank.setLastRankEvaluatedMonth(rankDto.getLastRankEvaluatedMonth().toString());
        rank.setRankAchievedDate(rankDto.getRankAchievedDate());
        return rank;
    }

    static public RankDto toRankDto(Rank rank, RankDto rankDto){
        rankDto.setId(rank.getId());
        rankDto.setAccountId(rank.getAccountId());
        rankDto.setRank(rank.getRank());
        rankDto.setOldRank(rank.getOldRank());
        rankDto.setPoints(rank.getPoints());
        rankDto.setLastRankEvaluatedMonth(YearMonth.parse(rank.getLastRankEvaluatedMonth()));
        rankDto.setRankAchievedDate(rank.getRankAchievedDate());
        return rankDto;
    }
}
